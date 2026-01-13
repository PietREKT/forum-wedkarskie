<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiClient } from '../../utils/axios.js'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const tutorial = ref(null)
const loading = ref(false)
const error = ref(null)

const deleteSaving = ref(false)
const deleteError = ref(null)

const isAdmin = computed(() => auth.isAdmin)

// obsługa 401 dla niezalogowanych
const unauthorized = ref(false)

const methodLabels = {
  FLOAT: 'Spławik',
  SPINNING: 'Spinning',
  FLY: 'Muchówka',
  GROUND: 'Grunt',
  ICE: 'Podlodowe',
  NET: 'Połów sieciowy',
}
function methodLabel(m) {
  return methodLabels[m] || m
}

const showDeleteConfirm = ref(false)

function getTutorialText(t) {
  return t?.content ?? t?.tutorial_content?.content ?? ''
}

function getTutorialRating(t) {
  return t?.rating ?? t?.tutorial_content?.rating ?? t?.content?.rating ?? null
}

function getTutorialVote(t) {
  return t?.loggedUserVote ?? t?.tutorial_content?.loggedUserVote ?? t?.content?.loggedUserVote ?? null
}

function getAuthorUsername(t) {
  return (
      t?.author?.username ??
      t?.tutorial_content?.author?.username ??
      t?.content?.author?.username ??
      'nieznany'
  )
}

async function loadTutorial() {
  loading.value = true
  error.value = null
  deleteError.value = null
  unauthorized.value = false

  try {
    const id = route.params.id
    const resp = await apiClient.get(`/tutorials/${id}`)
    tutorial.value = resp.data
  } catch (e) {
    console.error('Błąd pobierania poradnika', e)
    const status = e?.response?.status
    if (status === 401) {
      unauthorized.value = true
      tutorial.value = null
      return
    }
    error.value = 'Nie udało się pobrać poradnika.'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/guides')
}

const title = computed(() => {
  if (!tutorial.value) return 'Poradnik wędkarski'
  if (tutorial.value.title && tutorial.value.title.trim().length > 0) {
    return tutorial.value.title
  }
  const raw = getTutorialText(tutorial.value) || ''
  const firstLine = raw.split('\n')[0].trim()
  return firstLine.length ? firstLine : 'Poradnik wędkarski'
})

function askDelete() {
  if (!isAdmin.value) return
  showDeleteConfirm.value = true
}

function cancelDelete() {
  showDeleteConfirm.value = false
}

async function confirmDelete() {
  deleteError.value = null
  if (!isAdmin.value) {
    showDeleteConfirm.value = false
    return
  }

  deleteSaving.value = true
  try {
    const id = route.params.id
    await apiClient.delete(`/tutorials/${id}`)
    showDeleteConfirm.value = false
    router.push('/guides')
  } catch (e) {
    console.error('Błąd usuwania poradnika', e)
    deleteError.value = 'Nie udało się usunąć poradnika.'
  } finally {
    deleteSaving.value = false
  }
}

const apiBase = computed(() => {
  const b = apiClient?.defaults?.baseURL || ''
  return b.replace(/\/api\/?$/, '')
})

function resolvePhotoUrl(path) {
  if (!path) return null
  if (typeof path !== 'string') return null
  if (path.startsWith('http://') || path.startsWith('https://')) return path

  const base = apiBase.value || ''
  if (!base) return path

  if (path.startsWith('/')) return `${base}${path}`
  return `${base}/${path}`
}

function getAttachedPhotos(t) {
  const a =
      t?.attachedPhotos ??
      t?.tutorial_content?.attachedPhotos ??
      t?.content?.attachedPhotos ??
      []
  return Array.isArray(a) ? a : []
}

const photoUrls = computed(() => {
  const raw = getAttachedPhotos(tutorial.value)
  return raw.map(resolvePhotoUrl).filter(Boolean)
})

const ratingText = computed(() => {
  const r = getTutorialRating(tutorial.value)
  if (r == null) return null
  return `${r}/5`
})

const voteText = computed(() => {
  const v = getTutorialVote(tutorial.value)
  if (!v) return null
  return String(v)
})

onMounted(loadTutorial)
</script>

<template>
  <section class="space-y-4">
    <header class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-semibold">
          {{ title }}
        </h1>
        <p class="text-xs opacity-70 mt-1">
          Autor:
          <span class="font-medium">
            {{ getAuthorUsername(tutorial) }}
          </span>
        </p>

        <div class="mt-1 flex flex-wrap items-center gap-2 text-xs opacity-80">
          <span v-if="ratingText">
            Ocena: <span class="font-medium">{{ ratingText }}</span>
          </span>
          <span v-if="voteText" class="opacity-70">
            (Twój głos: {{ voteText }})
          </span>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <button
            v-if="isAdmin"
            type="button"
            class="px-3 py-1.5 text-sm rounded-md border border-red-500/70 bg-red-500/20 hover:bg-red-500/30 disabled:opacity-50"
            :disabled="deleteSaving"
            @click.prevent="askDelete"
        >
          Usuń poradnik
        </button>

        <button
            type="button"
            class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5"
            @click.prevent="goBack"
        >
          Wróć
        </button>
      </div>
    </header>

    <div
        v-if="showDeleteConfirm"
        class="border border-red-500/60 bg-red-500/10 rounded-md px-4 py-3 text-sm flex items-center justify-between gap-3"
    >
      <span>Na pewno usunąć ten poradnik?</span>
      <div class="flex gap-2">
        <button
            type="button"
            class="px-3 py-1 rounded-md border border-red-500/70 bg-red-500/20 hover:bg-red-500/30 text-xs"
            :disabled="deleteSaving"
            @click.prevent="confirmDelete"
        >
          Usuń
        </button>
        <button
            type="button"
            class="px-3 py-1 rounded-md border bg-[var(--color-bg)] hover:bg-black/5 text-xs"
            :disabled="deleteSaving"
            @click.prevent="cancelDelete"
        >
          Anuluj
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-sm opacity-80">
      Ładowanie poradnika…
    </div>

    <div
        v-else-if="unauthorized"
        class="rounded-md border border-amber-500/60 bg-amber-500/10 px-4 py-3 text-sm"
    >
      Zaloguj się, aby zobaczyć poradnik.
    </div>

    <div v-else-if="error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
      {{ error }}
    </div>

    <div v-else-if="tutorial" class="space-y-4">
      <div v-if="tutorial.methods && tutorial.methods.length" class="flex flex-wrap gap-2">
        <span
            v-for="m in tutorial.methods"
            :key="m"
            class="text-[11px] uppercase tracking-wide px-2 py-0.5 rounded-full border border-gray-300"
        >
          {{ methodLabel(m) }}
        </span>
      </div>

      <div v-if="tutorial.fishMentioned && tutorial.fishMentioned.length" class="flex flex-wrap gap-2">
        <span
            v-for="fishItem in tutorial.fishMentioned"
            :key="fishItem.id ?? fishItem.name"
            class="text-xs px-2 py-0.5 rounded-full border border-gray-300"
        >
          {{ fishItem.name }}
        </span>
      </div>

      <div v-if="deleteError" class="text-xs text-red-400">
        {{ deleteError }}
      </div>

      <div v-if="photoUrls.length" class="space-y-2">
        <div class="text-xs opacity-70">
          Zdjęcia:
        </div>

        <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
          <a
              v-for="(url, idx) in photoUrls"
              :key="url + '_' + idx"
              :href="url"
              target="_blank"
              rel="noopener"
              class="block border border-gray-300 rounded-xl overflow-hidden bg-[var(--color-bg-elevated)]"
          >
            <img
                :src="url"
                class="w-full h-40 object-cover"
                alt="Zdjęcie poradnika"
                loading="lazy"
            />
          </a>
        </div>
      </div>

      <article class="border border-gray-300 rounded-2xl p-4 bg-[var(--color-bg-elevated)] text-sm leading-relaxed whitespace-pre-wrap shadow-sm">
        {{ getTutorialText(tutorial) }}
      </article>
    </div>
  </section>
</template>
