<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../utils/axios.js'
import { useAuthStore } from '../stores/auth'

// metody z enuma backendu
const fishingMethods = [
  { value: '', label: 'Dowolna metoda' },
  { value: 'FLOAT', label: 'Spławik' },
  { value: 'SPINNING', label: 'Spinning' },
  { value: 'FLY', label: 'Muchówka' },
  { value: 'GROUND', label: 'Grunt' },
  { value: 'ICE', label: 'Podlodowe' },
  { value: 'NET', label: 'Połów sieciowy' },
]

// mapowanie enum -> etykieta PL
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

// spłaszczony obiekt poradnika:
const tutorials = ref([])
const loading = ref(false)
const error = ref(null)

// domyślnie „Dowolna metoda”
const selectedMethod = ref('')

// ryby – filtr po rybie
const fish = ref([])
const fishLoading = ref(false)
const fishError = ref(null)
const fishPanelOpen = ref(false)
const fishSearch = ref('')

// zaznaczone ryby
const selectedFishIds = ref([])
const selectedFishList = ref([])

// paginacja
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

// auth
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

// mapowanie PageDto / listy
function mapResponseToList(data) {
  if (Array.isArray(data)) return data
  if (data && Array.isArray(data.content)) return data.content
  return []
}

// ID ryby
function getFishId(f) {
  if (!f || typeof f !== 'object') return null
  return f.id ?? f.fishId ?? f.fish_id ?? null
}

// zaznaczanie / odznaczanie ryb
function toggleFish(fishObj) {
  const id = getFishId(fishObj)
  if (id == null) return

  const idx = selectedFishIds.value.indexOf(id)
  if (idx === -1) {
    selectedFishIds.value.push(id)
    if (!selectedFishList.value.some(f => f.id === id)) {
      selectedFishList.value.push({ id, name: fishObj.name })
    }
  } else {
    selectedFishIds.value.splice(idx, 1)
    const idxList = selectedFishList.value.findIndex(f => f.id === id)
    if (idxList !== -1) selectedFishList.value.splice(idxList, 1)
  }
}

// szukanie ryb
async function searchFish() {
  const q = fishSearch.value.trim()
  fishError.value = null

  if (q.length < 2) {
    fish.value = []
    fishError.value = 'Wpisz co najmniej 2 znaki nazwy ryby.'
    return
  }

  fishLoading.value = true
  try {
    const resp = await apiClient.get('/fish/search', {
      params: { name: q },
    })
    fish.value = Array.isArray(resp.data) ? resp.data : (resp.data?.content || [])
  } catch (e) {
    console.error('Błąd pobierania ryb', e)
    fishError.value = 'Nie udało się pobrać listy ryb.'
  } finally {
    fishLoading.value = false
  }
}

// normalizacja DTO
function normalizeFromDto(dto, listItem) {
  const id = listItem?.id ?? dto?.content?.id ?? null

  const fromList = listItem?.title?.trim()
  const fromContent = (dto?.content?.content || '').split('\n')[0].trim()
  const title = truncate(fromList || fromContent || 'Poradnik wędkarski', 80)

  return {
    id,
    title,
    authorUsername: dto?.content?.author?.username || 'nieznany',
    methods: Array.isArray(dto?.methods) ? dto.methods : [],
    fishMentioned: Array.isArray(dto?.fishMentioned) ? dto.fishMentioned : [],
    rating: listItem?.rating ?? dto?.content?.rating ?? null,
    snippet: dto?.content?.content || '',
  }
}

function normalizeFromListOnly(listItem) {
  const title = listItem?.title?.trim() || 'Poradnik wędkarski'
  return {
    id: listItem?.id ?? null,
    title: truncate(title, 80),
    authorUsername: 'nieznany',
    methods: [],
    fishMentioned: [],
    rating: listItem?.rating ?? null,
    snippet: '',
  }
}

// ładowanie poradników
async function loadTutorials() {
  loading.value = true
  error.value = null
  tutorials.value = []

  try {
    if (selectedFishIds.value.length > 0) {
      const fishId = selectedFishIds.value[0]
      const resp = await apiClient.get('/tutorials/fish', {
        params: { fishId },
      })
      const list = mapResponseToList(resp.data)
      tutorials.value = list.map(dto => normalizeFromDto(dto, null))
    } else if (selectedMethod.value) {
      const method = selectedMethod.value
      const resp = await apiClient.get('/tutorials/method', {
        params: { method },
      })
      const list = mapResponseToList(resp.data)
      tutorials.value = list.map(dto => normalizeFromDto(dto, null))
    } else {
      const resp = await apiClient.get('/tutorials')
      const baseList = mapResponseToList(resp.data)

      const detailed = []
      for (const item of baseList) {
        try {
          const detailResp = await apiClient.get(`/tutorials/${item.id}`)
          detailed.push(normalizeFromDto(detailResp.data, item))
        } catch (e) {
          console.error('Błąd pobierania szczegółów poradnika', e)
          detailed.push(normalizeFromListOnly(item))
        }
      }
      tutorials.value = detailed
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
  loadTutorials()
})
</script>

<template>
  <section class="space-y-6">
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

          <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5"
              @click="fishPanelOpen = !fishPanelOpen"
          >
            Ryby
            <span v-if="selectedFishIds.length" class="opacity-70">
              ({{ selectedFishIds.length }} wybrane)
            </span>
          </button>

          <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5"
              @click="loadTutorials"
              :disabled="loading"
          >
            Szukaj
          </button>

          <button
              v-if="isAdmin"
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border bg-emerald-700/60 hover:bg-emerald-600/70 text-white"
              @click="goToCreateTutorial"
          >
            Dodaj poradnik
          </button>
        </div>
      </div>

      <!-- Panel z rybami -->
      <div
          v-if="fishPanelOpen"
          class="border border-gray-300 rounded-xl p-4 bg-[var(--color-bg-elevated)] space-y-3 shadow-sm"
      >
        <div class="text-sm font-medium">
          Wyszukaj gatunki ryb (filtr po rybach nadpisze filtr po metodzie).
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <input
              v-model="fishSearch"
              type="text"
              class="px-2 py-1 text-sm border rounded-md bg-[var(--color-bg)] min-w-[200px]"
              placeholder="Np. szczupak, okoń…"
          />
          <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5"
              @click="searchFish"
              :disabled="fishLoading"
          >
            Szukaj ryb
          </button>
        </div>

        <div v-if="fishError" class="text-xs text-red-400">
          {{ fishError }}
        </div>

        <div v-else-if="fishLoading" class="text-xs opacity-80">
          Ładowanie listy ryb…
        </div>

        <div
            v-else-if="fish.length > 0"
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
                @change="toggleFish(f)"
            />
            <span>{{ f.name }}</span>
          </label>
        </div>

        <div
            v-else-if="fishSearch.length >= 2"
            class="text-xs opacity-70"
        >
          Brak wyników.
        </div>

        <div
            v-if="selectedFishList.length"
            class="pt-3 border-t border-gray-200"
        >
          <div class="text-xs opacity-70 mb-1">
            Wybrane gatunki:
          </div>
          <div class="flex flex-wrap gap-2">
            <span
                v-for="sf in selectedFishList"
                :key="sf.id"
                class="text-xs px-2 py-0.5 rounded-full border border-gray-300"
            >
              {{ sf.name }}
            </span>
          </div>
        </div>
      </div>
    </header>

    <div v-if="error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
      {{ error }}
    </div>

    <div v-if="loading" class="text-sm opacity-80">
      Ładowanie poradników…
    </div>

    <div v-if="!loading && !error && tutorials.length === 0" class="text-sm opacity-80">
      Brak poradników dla wybranych filtrów.
    </div>

    <div
        v-if="!loading && !error && tutorials.length > 0"
        class="space-y-4"
    >
      <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <RouterLink
            v-for="t in paginatedTutorials"
            :key="t.id ?? JSON.stringify(t)"
            :to="`/guides/${t.id}`"
            class="block border border-gray-300 rounded-2xl p-4 bg-[var(--color-bg-elevated)] shadow-sm hover:shadow-md hover:border-gray-400 transition flex flex-col gap-3"
        >
          <header class="space-y-1">
            <h2 class="font-semibold text-base">
              {{ t.title }}
            </h2>
            <p class="text-xs opacity-70">
              Autor:
              <span class="font-medium">
                {{ t.authorUsername }}
              </span>
            </p>
            <p v-if="t.rating != null" class="text-xs opacity-70">
              Ocena: {{ t.rating }}/5
            </p>
          </header>

          <div
              v-if="t.methods && t.methods.length"
              class="flex flex-wrap gap-1 mt-1"
          >
            <span
                v-for="m in t.methods"
                :key="m"
                class="text-[11px] uppercase tracking-wide px-2 py-0.5 rounded-full border border-gray-300"
            >
              {{ methodLabel(m) }}
            </span>
          </div>

          <p
              v-if="t.snippet"
              class="text-sm leading-relaxed mt-2"
          >
            {{ truncate(t.snippet, 260) }}
          </p>

          <div
              v-if="t.fishMentioned && t.fishMentioned.length"
              class="flex flex-wrap gap-2 mt-auto pt-2 border-t border-gray-200"
          >
            <span
                v-for="fishItem in t.fishMentioned"
                :key="fishItem.id ?? fishItem.name"
                class="text-xs px-2 py-0.5 rounded-full border border-gray-300"
            >
              {{ fishItem.name }}
            </span>
          </div>
        </RouterLink>
      </div>

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
            :class="page === currentPage ? 'bg-black/5 font-semibold border-gray-500' : 'bg-[var(--color-bg)] hover:bg-black/5 border-gray-300'"
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
