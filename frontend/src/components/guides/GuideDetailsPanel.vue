<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiClient } from '../../utils/axios'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const tutorial = ref(null)
const loading = ref(false)
const error = ref(null)

const deleteSaving = ref(false)
const deleteError = ref(null)

// tu po prostu bierzemy ref ze store – to już jest computed<boolean>
const isAdmin = auth.isAdmin
const isAuthenticated = computed(() => auth.isAuthenticated)

// prosty stan do potwierdzenia na stronie
const showDeleteConfirm = ref(false)

async function loadTutorial() {
  loading.value = true
  error.value = null
  deleteError.value = null

  try {
    const id = route.params.id
    const resp = await apiClient.get(`/tutorials/${id}`)
    tutorial.value = resp.data
  } catch (e) {
    console.error('Błąd pobierania poradnika', e)
    error.value = 'Nie udało się pobrać poradnika.'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

const title = computed(() => {
  if (!tutorial.value) return 'Poradnik wędkarski'
  if (tutorial.value.title && tutorial.value.title.trim().length > 0) {
    return tutorial.value.title
  }
  const raw = tutorial.value.content?.content || ''
  const firstLine = raw.split('\n')[0].trim()
  return firstLine.length ? firstLine : 'Poradnik wędkarski'
})

// rating tylko do odczytu – z pola tutorial.rating (0–5)
const displayRating = computed(() => {
  const raw = tutorial.value?.rating ?? 0
  if (raw == null) return 0
  return Math.max(0, Math.min(5, Number(raw)))
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
            {{ tutorial?.content?.author?.username || 'nieznany' }}
          </span>
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
            v-if="isAdmin"
            type="button"
            class="px-3 py-1.5 text-sm rounded-md border border-red-500/70 bg-red-500/10 hover:bg-red-500/20 disabled:opacity-50"
            :disabled="deleteSaving"
            @click="askDelete"
        >
          Usuń poradnik
        </button>

        <button
            type="button"
            class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-white/5"
            @click="goBack"
        >
          Wróć
        </button>
      </div>
    </header>

    <!-- Potwierdzenie usuwania na stronie -->
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
            @click="confirmDelete"
        >
          Usuń
        </button>
        <button
            type="button"
            class="px-3 py-1 rounded-md border bg-[var(--color-bg)] hover:bg-white/5 text-xs"
            :disabled="deleteSaving"
            @click="cancelDelete"
        >
          Anuluj
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-sm opacity-80">
      Ładowanie poradnika…
    </div>

    <div v-if="error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
      {{ error }}
    </div>

    <div
        v-if="!loading && !error && tutorial"
        class="space-y-4"
    >
      <!-- Metody -->
      <div
          v-if="tutorial.methods && tutorial.methods.length"
          class="flex flex-wrap gap-2"
      >
        <span
            v-for="m in tutorial.methods"
            :key="m"
            class="text-[11px] uppercase tracking-wide px-2 py-0.5 rounded-full border border-white/20"
        >
          {{ m }}
        </span>
      </div>

      <!-- Ryby -->
      <div
          v-if="tutorial.fishMentioned && tutorial.fishMentioned.length"
          class="flex flex-wrap gap-2"
      >
        <span
            v-for="fishItem in tutorial.fishMentioned"
            :key="fishItem.id ?? fishItem.name"
            class="text-xs px-2 py-0.5 rounded-full border border-white/20"
        >
          {{ fishItem.name }}
        </span>
      </div>

      <!-- Rating 0–5 gwiazdek (tylko odczyt) -->
      <div class="flex items-center gap-2 text-sm">
        <span>Ocena:</span>
        <span>
          <span
              v-for="n in 5"
              :key="n"
              class="text-lg leading-none"
              :class="n <= displayRating ? 'opacity-100' : 'opacity-30'"
          >
            ★
          </span>
        </span>
        <span class="text-xs opacity-70">
          {{ displayRating }}/5
          <span v-if="!isAuthenticated">(ocena tylko do podglądu)</span>
        </span>
      </div>

      <div v-if="deleteError" class="text-xs text-red-400">
        {{ deleteError }}
      </div>

      <!-- Treść -->
      <article class="border rounded-2xl p-4 bg-[var(--color-bg-elevated)] text-sm leading-relaxed whitespace-pre-wrap">
        {{ tutorial.content?.content }}
      </article>
    </div>
  </section>
</template>
