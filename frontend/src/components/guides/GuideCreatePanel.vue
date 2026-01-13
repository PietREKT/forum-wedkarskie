<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../../utils/axios.js'
import { useAuthStore } from '../../stores/auth'
import { useGuidesStore } from '../../stores/guides.js'

const router = useRouter()
const auth = useAuthStore()
const guides = useGuidesStore()

const isLoggedIn = computed(() => !!auth.isAuthenticated)

const form = ref({
  title: '',
  content: '',
  method: '',
  fishIds: [],
  photos: [],
})

const saving = ref(false)
const error = ref(null)

const methodLabels = {
  FLOAT: 'Spławik',
  SPINNING: 'Spinning',
  FLY: 'Muchówka',
  GROUND: 'Grunt',
  ICE: 'Podlodowe',
  NET: 'Połów sieciowy',
}
const allMethods = [
  { value: '', label: 'Nie wybieram' },
  { value: 'FLOAT', label: 'Spławik' },
  { value: 'SPINNING', label: 'Spinning' },
  { value: 'FLY', label: 'Muchówka' },
  { value: 'GROUND', label: 'Grunt' },
  { value: 'ICE', label: 'Podlodowe' },
  { value: 'NET', label: 'Połów sieciowy' },
]

const fishSearch = ref('')
const fish = ref([])
const fishLoading = ref(false)
const fishError = ref(null)
const selectedFishList = ref([])

function getFishId(f) {
  if (!f || typeof f !== 'object') return null
  return f.id ?? f.fishId ?? f.fish_id ?? null
}

function toggleFish(fishObj) {
  const id = getFishId(fishObj)
  if (id == null) return

  const idx = form.value.fishIds.indexOf(id)
  if (idx === -1) {
    form.value.fishIds.push(id)
    if (!selectedFishList.value.some(x => x.id === id)) {
      selectedFishList.value.push({ id, name: fishObj.name })
    }
  } else {
    form.value.fishIds.splice(idx, 1)
    const idxList = selectedFishList.value.findIndex(x => x.id === id)
    if (idxList !== -1) selectedFishList.value.splice(idxList, 1)
  }
}

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
    const resp = await apiClient.get('/fish/search', { params: { name: q } })
    fish.value = Array.isArray(resp.data) ? resp.data : (resp.data?.content || [])
  } catch (e) {
    console.error('Błąd pobierania ryb', e)
    fishError.value = 'Nie udało się pobrać listy ryb.'
  } finally {
    fishLoading.value = false
  }
}

function onPhotosChange(event) {
  const files = Array.from(event.target.files || [])
  form.value.photos = files
}

function buildContentWithMeta(original) {
  const lines = []
  if (form.value.method) {
    lines.push(`Metoda: ${methodLabels[form.value.method] || form.value.method}`)
  }
  if (selectedFishList.value.length) {
    const names = selectedFishList.value.map(x => x.name).filter(Boolean)
    lines.push(`Ryby: ${names.join(', ')}`)
  }

  if (!lines.length) return original
  return `${lines.join('\n')}\n\n${original}`
}

async function submitForm() {
  if (!isLoggedIn.value) return

  // blokada podwójnego submitu
  if (saving.value) return

  if (!form.value.title.trim()) {
    error.value = 'Tytuł poradnika nie może być pusty.'
    return
  }
  if (!form.value.content.trim()) {
    error.value = 'Treść poradnika nie może być pusta.'
    return
  }
  if (!form.value.photos.length) {
    error.value = 'W tej wersji serwera wymagane jest przynajmniej jedno zdjęcie.'
    return
  }

  saving.value = true
  error.value = null

  try {
    const finalContent = buildContentWithMeta(form.value.content)

    // createTutorial rzuca błąd jeśli backend nie zwróci id/status nie jest 200/201
    const res = await guides.createTutorial({
      title: form.value.title,
      content: finalContent,
      photos: form.value.photos,
    })

    if (!res?.id) {
      throw new Error('Brak id poradnika w odpowiedzi.')
    }

    router.push({ path: '/guides', query: { submitted: '1' } })
  } catch (e) {
    console.error('Błąd tworzenia poradnika', e)
    const status = e?.response?.status
    if (status === 401) {
      error.value = 'Zaloguj się ponownie, aby dodać poradnik.'
    } else {
      error.value = 'Nie udało się utworzyć poradnika (błąd po stronie serwera).'
    }
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section class="space-y-4">
    <header class="flex items-center justify-between gap-4">
      <h1 class="text-2xl font-semibold">
        Dodaj poradnik
      </h1>

      <RouterLink
          to="/guides"
          class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-white/5"
      >
        Wróć do listy
      </RouterLink>
    </header>

    <div v-if="!isLoggedIn" class="text-sm opacity-80">
      Zaloguj się, aby dodać poradnik.
    </div>

    <form v-else class="space-y-4 max-w-3xl" @submit.prevent="submitForm">
      <div class="rounded-md border border-gray-300 bg-[var(--color-bg-elevated)] px-4 py-3 text-sm">
        Po zapisaniu poradnik trafi do moderacji i będzie widoczny publicznie dopiero po akceptacji administratora.
      </div>

      <div>
        <label class="block text-sm font-medium mb-1">Tytuł poradnika</label>
        <input
            v-model="form.title"
            type="text"
            class="w-full border rounded-md px-3 py-2 bg-[var(--color-bg)] text-sm"
        />
      </div>

      <div>
        <label class="block text-sm font-medium mb-1">Metoda połowu (opcjonalnie)</label>
        <select v-model="form.method" class="w-full border rounded-md px-3 py-2 bg-[var(--color-bg)] text-sm">
          <option v-for="m in allMethods" :key="m.value || 'NONE'" :value="m.value">
            {{ m.label }}
          </option>
        </select>
      </div>

      <div>
        <label class="block text-sm font-medium mb-1">Gatunki ryb (opcjonalnie)</label>

        <div class="flex flex-wrap items-center gap-2 mb-2">
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

        <div v-if="fishError" class="text-xs text-red-400 mb-1">
          {{ fishError }}
        </div>
        <div v-else-if="fishLoading" class="text-xs opacity-80 mb-1">
          Ładowanie listy ryb…
        </div>

        <div
            v-else-if="fish.length > 0"
            class="flex flex-wrap gap-2 max-h-56 overflow-auto border rounded-md p-2 bg-[var(--color-bg-elevated)]"
        >
          <label
              v-for="f in fish"
              :key="getFishId(f) ?? f.name"
              class="flex items-center gap-2 text-xs cursor-pointer"
          >
            <input
                type="checkbox"
                class="accent-current"
                :checked="form.fishIds.includes(getFishId(f))"
                @change="toggleFish(f)"
            />
            <span>{{ f.name }}</span>
          </label>
        </div>

        <div v-else-if="fishSearch.length >= 2" class="text-xs opacity-70 mb-1">
          Brak wyników.
        </div>

        <div v-if="selectedFishList.length" class="mt-2">
          <div class="text-xs opacity-70 mb-1">Wybrane gatunki:</div>
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

      <div>
        <label class="block text-sm font-medium mb-1">Treść poradnika</label>
        <textarea
            v-model="form.content"
            rows="10"
            class="w-full border rounded-md px-3 py-2 bg-[var(--color-bg)] text-sm resize-y"
        />
      </div>

      <div>
        <label class="block text-sm font-medium mb-1">Zdjęcia (wymagane – minimum jedno)</label>
        <input
            type="file"
            multiple
            accept="image/*"
            class="w-full text-sm"
            @change="onPhotosChange"
        />
        <p v-if="form.photos.length" class="text-xs opacity-70 mt-1">
          Wybrane pliki: {{ form.photos.length }}
        </p>
      </div>

      <div v-if="error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
        {{ error }}
      </div>

      <button
          type="submit"
          class="px-4 py-2 text-sm rounded-md border bg-emerald-700/40 hover:bg-emerald-600/50 disabled:opacity-50"
          :disabled="saving"
      >
        Zapisz poradnik
      </button>
    </form>
  </section>
</template>
