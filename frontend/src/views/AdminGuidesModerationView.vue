<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useGuidesStore } from '../stores/guides.js'

const router = useRouter()
const auth = useAuthStore()
const guides = useGuidesStore()

const isAdmin = computed(() => !!auth.isAdmin)

const loading = computed(() => guides.unverifiedLoading)
const error = computed(() => guides.unverifiedError)
const items = computed(() => guides.unverifiedTutorials)

const actionError = ref(null)
const actionSaving = ref(false)

const rejectModalOpen = ref(false)
const rejectReason = ref('')
const rejectItem = ref(null)

function getTutorialId(t) {
  if (!t || typeof t !== 'object') return null
  return (
      t.tutorialId ??
      t.id ??
      t.tutorial_id ??
      t.tutorialID ??
      t.tutorial?.tutorialId ??
      t.tutorial?.id ??
      t.tutorial_content?.tutorialId ??
      t.tutorial_content?.id ??
      t.tutorial_content?.contentId ??
      t.content?.tutorialId ??
      t.content?.id ??
      null
  )
}

function getTitle(t) {
  const title = (t?.title || t?.tutorial_content?.title || t?.content?.title || '').trim()
  if (title) return title

  const firstLine = String(t?.tutorial_content?.content || t?.content?.content || '')
      .split('\n')[0]
      .trim()
  return firstLine || 'Poradnik wędkarski'
}

function getAuthorUsername(t) {
  return t?.tutorial_content?.author?.username || t?.content?.author?.username || 'nieznany'
}

function getContentText(t) {
  return t?.tutorial_content?.content || t?.content?.content || ''
}

function goBack() {
  router.push('/guides')
}

async function refresh() {
  actionError.value = null
  await guides.loadUnverifiedTutorials()
}

async function onAccept(item) {
  actionError.value = null
  const id = getTutorialId(item)
  if (!id) {
    actionError.value = 'Nie znaleziono identyfikatora poradnika w danych z serwera.'
    return
  }

  actionSaving.value = true
  try {
    await guides.acceptTutorial(id)
    await refresh()
  } catch (e) {
    console.error('accept tutorial error', e)
    actionError.value = 'Nie udało się zaakceptować poradnika.'
  } finally {
    actionSaving.value = false
  }
}

function openReject(item) {
  rejectItem.value = item
  rejectReason.value = ''
  rejectModalOpen.value = true
}

function closeReject() {
  rejectModalOpen.value = false
  rejectReason.value = ''
  rejectItem.value = null
}

async function confirmReject() {
  actionError.value = null
  const id = getTutorialId(rejectItem.value)
  if (!id) {
    actionError.value = 'Nie znaleziono identyfikatora poradnika w danych z serwera.'
    return
  }

  const reason = rejectReason.value.trim()
  if (!reason) {
    actionError.value = 'Podaj powód odrzucenia.'
    return
  }

  actionSaving.value = true
  try {
    await guides.rejectTutorial(id, reason)
    closeReject()
    await refresh()
  } catch (e) {
    console.error('reject tutorial error', e)
    actionError.value = 'Nie udało się odrzucić poradnika.'
  } finally {
    actionSaving.value = false
  }
}

onMounted(async () => {
  if (!isAdmin.value) return
  await refresh()
})
</script>

<template>
  <section class="space-y-4">
    <header class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-semibold">
          Moderacja poradników
        </h1>
        <p class="text-xs opacity-70 mt-1">
          Akceptuj lub odrzucaj poradniki oczekujące na weryfikację.
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
            type="button"
            class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5"
            @click="refresh"
            :disabled="loading || actionSaving"
        >
          Odśwież
        </button>

        <button
            type="button"
            class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5"
            @click="goBack"
        >
          Wróć
        </button>
      </div>
    </header>

    <div v-if="!isAdmin" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
      Brak uprawnień.
    </div>

    <div v-else>
      <div v-if="error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
        {{ error }}
      </div>

      <div v-if="actionError" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm mt-2">
        {{ actionError }}
      </div>

      <div v-if="loading" class="text-sm opacity-80 mt-2">
        Ładowanie poradników do moderacji…
      </div>

      <div v-if="!loading && !error && items.length === 0" class="text-sm opacity-80 mt-2">
        Brak poradników do moderacji.
      </div>

      <div v-if="!loading && !error && items.length > 0" class="space-y-3 mt-2">
        <article
            v-for="t in items"
            :key="getTutorialId(t) ?? JSON.stringify(t)"
            class="border border-gray-300 rounded-2xl p-4 bg-[var(--color-bg-elevated)] shadow-sm space-y-3"
        >
          <div class="flex flex-col gap-1">
            <div class="text-base font-semibold">
              {{ getTitle(t) }}
            </div>

            <div class="text-xs opacity-70">
              Autor:
              <span class="font-medium">
                {{ getAuthorUsername(t) }}
              </span>
            </div>

            <div class="text-xs opacity-60">
              ID: {{ getTutorialId(t) ?? 'brak' }}
            </div>
          </div>

          <div class="text-sm whitespace-pre-wrap leading-relaxed border border-gray-200 rounded-xl p-3 bg-[var(--color-bg)]">
            {{ getContentText(t) }}
          </div>

          <div class="flex items-center gap-2">
            <button
                type="button"
                class="px-3 py-1.5 text-sm rounded-md border bg-emerald-700/60 hover:bg-emerald-600/70 text-white disabled:opacity-50"
                :disabled="actionSaving"
                @click="onAccept(t)"
            >
              Akceptuj
            </button>

            <button
                type="button"
                class="px-3 py-1.5 text-sm rounded-md border border-red-500/70 bg-red-500/20 hover:bg-red-500/30 disabled:opacity-50"
                :disabled="actionSaving"
                @click="openReject(t)"
            >
              Odrzuć
            </button>

            <RouterLink
                v-if="getTutorialId(t) != null"
                :to="`/guides/${getTutorialId(t)}`"
                class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5"
            >
              Podgląd
            </RouterLink>
          </div>
        </article>
      </div>

      <div
          v-if="rejectModalOpen"
          class="border border-red-500/60 bg-red-500/10 rounded-md px-4 py-3 text-sm mt-4 space-y-3"
      >
        <div class="font-medium">
          Powód odrzucenia
        </div>

        <textarea
            v-model="rejectReason"
            rows="4"
            class="w-full border rounded-md px-3 py-2 bg-[var(--color-bg)] text-sm resize-y"
            placeholder="Wpisz powód odrzucenia..."
        />

        <div class="flex gap-2">
          <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border border-red-500/70 bg-red-500/20 hover:bg-red-500/30 disabled:opacity-50"
              :disabled="actionSaving"
              @click="confirmReject"
          >
            Zatwierdź odrzucenie
          </button>

          <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5 disabled:opacity-50"
              :disabled="actionSaving"
              @click="closeReject"
          >
            Anuluj
          </button>
        </div>
      </div>
    </div>
  </section>
</template>
