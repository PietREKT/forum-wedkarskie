<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../utils/axios'
import { useAuthStore } from '../stores/auth'

// metody z enuma backendu (bez FEEDER i LIVE_BAIT)
const fishingMethods = [
  { value: '', label: 'Dowolna metoda' },
  { value: 'FLOAT', label: 'Spławik' },
  { value: 'SPINNING', label: 'Spinning' },
  { value: 'FLY', label: 'Muchówka' },
  { value: 'GROUND', label: 'Grunt' },
  { value: 'ICE', label: 'Podlodowe' },
  { value: 'NET', label: 'Sieci' },
]

// poradniki
const tutorials = ref([])
const loading = ref(false)
const error = ref(null)

// filtr metod
const selectedMethod = ref('SPINNING')

// ryby
const fish = ref([])
const fishLoading = ref(false)
const fishError = ref(null)
const fishPanelOpen = ref(false)
const selectedFishIds = ref([])

// paginacja – max 8 na stronę
const pageSize = 8
const currentPage = ref(1)

const totalPages = computed(() => {
  if (!tutorials.value.length) return 1
  return Math.ceil(tutorials.value.length / pageSize)
})

const paginatedTutorials = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return tutorials.value.slice(start, start + pageSize)
})

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value -= 1
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value += 1
}

// auth – isAdmin masz już w store
const auth = useAuthStore()
const isAdmin = computed(() => auth.isAdmin)

const router = useRouter()

function goToCreateTutorial() {
  router.push('/guides/create')
}

// przycinanie tekstu
function truncate(text, max = 200) {
  if (!text) return ''
  if (text.length <= max) return text
  return text.slice(0, max) + '…'
}

// tytuł = 1. linia treści
function getTitle(tutorial) {
  const raw = tutorial?.content?.content || ''
  const firstLine = raw.split('\n')[0].trim()
  if (firstLine.length === 0) return 'Poradnik wędkarski'
  return truncate(firstLine, 80)
}

// mapowanie PageDto / listy
function mapResponseToList(data) {
  if (Array.isArray(data)) return data
  if (data && Array.isArray(data.content)) return data.content
  return []
}

// bezpieczne ID ryby
function getFishId(f) {
  if (!f || typeof f !== 'object') return null
  return f.id ?? f.fishId ?? f.fish_id ?? null
}

// ładowanie ryb
async function loadFish() {
  fishLoading.value = true
  fishError.value = null
  try {
    const resp = await apiClient.get('/fish')
    const list = Array.isArray(resp.data) ? resp.data : (resp.data?.content || [])
    fish.value = list
  } catch (e) {
    console.error('Błąd pobierania ryb', e)
    fishError.value = 'Nie udało się pobrać listy ryb.'
  } finally {
    fishLoading.value = false
  }
}

function toggleFishById(idRaw) {
  const id = idRaw
  if (id == null) return
  const idx = selectedFishIds.value.indexOf(id)
  if (idx === -1) selectedFishIds.value.push(id)
  else selectedFishIds.value.splice(idx, 1)
}

// ładowanie poradników
async function loadTutorials() {
  loading.value = true
  error.value = null

  try {
    if (selectedFishIds.value.length > 0) {
      // jeżeli wybrano rybę – filtrujemy po rybie, metoda jest ignorowana
      const fishId = selectedFishIds.value[0]
      const resp = await apiClient.get('/tutorials/fish', {
        params: { fishId },
      })
      tutorials.value = mapResponseToList(resp.data)
    } else {
      // bez ryby – filtr po metodzie
      const method = selectedMethod.value || 'SPINNING'
      const resp = await apiClient.get('/tutorials/method', {
        params: { method },
      })
      tutorials.value = mapResponseToList(resp.data)
    }

    currentPage.value = 1
  } catch (e) {
    console.error('Błąd pobierania poradników', e)
    error.value = 'Nie udało się pobrać poradników. Spróbuj ponownie później.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFish()
  loadTutorials()
})
</script>

<template>
  <section class="space-y-6">
    <!-- Nagłówek + filtry -->
    <header class="flex flex-col gap-4">
      <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 class="text-2xl font-semibold">
            Poradniki wędkarskie
          </h1>
          <p class="text-xs opacity-70">
            Przeglądaj poradniki według metody połowu lub gatunków ryb.
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <!-- Metoda -->
          <label class="flex items-center gap-2 text-sm">
            <span>Metoda połowu:</span>
            <select
                v-model="selectedMethod"
                class="border rounded-md px-2 py-1 bg-[var(--color-bg)]"
            >
              <option
                  v-for="m in fishingMethods"
                  :key="m.value || 'ANY'"
                  :value="m.value"
              >
                {{ m.label }}
              </option>
            </select>
          </label>

          <!-- Ryby -->
          <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-white/5"
              @click="fishPanelOpen = !fishPanelOpen"
          >
            Ryby
            <span v-if="selectedFishIds.length" class="opacity-70">
              ({{ selectedFishIds.length }} wybrane)
            </span>
          </button>

          <!-- Szukaj -->
          <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-white/5"
              @click="loadTutorials"
              :disabled="loading"
          >
            Szukaj
          </button>

          <!-- Dodaj poradnik – tylko admin -->
          <button
              v-if="isAdmin"
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border bg-emerald-700/40 hover:bg-emerald-600/50"
              @click="goToCreateTutorial"
          >
            Dodaj poradnik
          </button>
        </div>
      </div>

      <!-- Panel z rybami -->
      <div
          v-if="fishPanelOpen"
          class="border rounded-xl p-4 bg-[var(--color-bg-elevated)] space-y-3"
      >
        <div class="text-sm font-medium">
          Wybierz gatunki ryb (filtr po rybach nadpisze filtr po metodzie).
        </div>

        <div v-if="fishError" class="text-xs text-red-400">
          {{ fishError }}
        </div>

        <div v-else-if="fishLoading" class="text-xs opacity-80">
          Ładowanie listy ryb…
        </div>

        <div
            v-else
            class="grid gap-2 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3"
        >
          <label
              v-for="f in fish"
              :key="getFishId(f) ?? f.name"
              class="flex items-center gap-2 text-sm cursor-pointer"
          >
            <input
                type="checkbox"
                class="accent-current"
                :checked="selectedFishIds.includes(getFishId(f))"
                @change="toggleFishById(getFishId(f))"
            />
            <span>{{ f.name }}</span>
          </label>
        </div>
      </div>
    </header>

    <!-- Komunikaty -->
    <div v-if="error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
      {{ error }}
    </div>

    <div v-if="loading" class="text-sm opacity-80">
      Ładowanie poradników…
    </div>

    <div v-if="!loading && !error && tutorials.length === 0" class="text-sm opacity-80">
      Brak poradników dla wybranych filtrów.
    </div>

    <!-- Lista + paginacja -->
    <div
        v-if="!loading && !error && tutorials.length > 0"
        class="space-y-4"
    >
      <!-- Kafelki -->
      <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <RouterLink
            v-for="t in paginatedTutorials"
            :key="t.content?.id ?? JSON.stringify(t)"
            :to="`/guides/${t.content?.id}`"
            class="block border rounded-2xl p-4 bg-[var(--color-bg-elevated)] shadow-sm hover:bg-white/5 hover:border-white/40 transition flex flex-col gap-3"
        >
          <header>
            <h2 class="font-semibold text-base mb-1">
              {{ getTitle(t) }}
            </h2>
            <p class="text-xs opacity-70">
              Autor:
              <span class="font-medium">
                {{ t.content?.author?.username || 'nieznany' }}
              </span>
            </p>
          </header>

          <div
              v-if="t.methods && t.methods.length"
              class="flex flex-wrap gap-1 mt-1"
          >
            <span
                v-for="m in t.methods"
                :key="m"
                class="text-[11px] uppercase tracking-wide px-2 py-0.5 rounded-full border border-white/20"
            >
              {{ m }}
            </span>
          </div>

          <p class="text-sm leading-relaxed mt-2">
            {{ truncate(t.content?.content, 260) }}
          </p>

          <div
              v-if="t.fishMentioned && t.fishMentioned.length"
              class="flex flex-wrap gap-2 mt-auto pt-2 border-t border-white/10"
          >
            <span
                v-for="fishItem in t.fishMentioned"
                :key="fishItem.id ?? fishItem.name"
                class="text-xs px-2 py-0.5 rounded-full border border-white/20"
            >
              {{ fishItem.name }}
            </span>
          </div>
        </RouterLink>
      </div>

      <!-- Paginacja -->
      <nav
          v-if="totalPages > 1"
          class="flex items-center justify-center gap-2 pt-2"
      >
        <button
            type="button"
            class="px-2 py-1 text-xs rounded border bg-[var(--color-bg)] disabled:opacity-40"
            :disabled="currentPage === 1"
            @click="prevPage"
        >
          « Poprzednia
        </button>

        <button
            v-for="page in totalPages"
            :key="page"
            type="button"
            class="min-w-[2rem] px-2 py-1 text-xs rounded border"
            :class="page === currentPage ? 'bg-white/10 font-semibold' : 'bg-[var(--color-bg)] hover:bg-white/5'"
            @click="goToPage(page)"
        >
          {{ page }}
        </button>

        <button
            type="button"
            class="px-2 py-1 text-xs rounded border bg-[var(--color-bg)] disabled:opacity-40"
            :disabled="currentPage === totalPages"
            @click="nextPage"
        >
          Następna »
        </button>
      </nav>
    </div>
  </section>
</template>
