<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../../utils/axios.js'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const isAdmin = computed(() => auth.isAdmin)

const form = ref({
  title: '',
  content: '',
  methods: [],
  fishIds: [],
  photos: [],
})

const saving = ref(false)
const error = ref(null)

// metody połowu + etykiety PL
const methodLabels = {
  FLOAT: 'Spławik',
  SPINNING: 'Spinning',
  FLY: 'Muchówka',
  GROUND: 'Grunt',
  ICE: 'Podlodowe',
  NET: 'Połów sieciowy',
}
const allMethods = ['FLOAT', 'SPINNING', 'FLY', 'GROUND', 'ICE', 'NET']
function methodLabel(m) {
  return methodLabels[m] || m
}

// ryby – wyszukiwarka + checkboxy + lista wybranych
const fishSearch = ref('')
const fish = ref([])
const fishLoading = ref(false)
const fishError = ref(null)

const selectedFishList = ref([]) // [{id,name}]

// pomocnicze ID
function getFishId(f) {
  if (!f || typeof f !== 'object') return null
  return f.id ?? f.fishId ?? f.fish_id ?? null
}

function toggleMethod(method) {
  const idx = form.value.methods.indexOf(method)
  if (idx === -1) form.value.methods.push(method)
  else form.value.methods.splice(idx, 1)
}

// zaznaczanie / odznaczanie ryby
function toggleFish(fishObj) {
  const id = getFishId(fishObj)
  if (id == null) return

  const ids = form.value.fishIds
  const idx = ids.indexOf(id)

  if (idx === -1) {
    ids.push(id)
    if (!selectedFishList.value.some(f => f.id === id)) {
      selectedFishList.value.push({ id, name: fishObj.name })
    }
  } else {
    ids.splice(idx, 1)
    const idxList = selectedFishList.value.findIndex(f => f.id === id)
    if (idxList !== -1) selectedFishList.value.splice(idxList, 1)
  }
}

// szukanie ryb po nazwie
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

// zdjęcia
function onPhotosChange(event) {
  const files = Array.from(event.target.files || [])
  form.value.photos = files
}

async function submitForm() {
  if (!isAdmin.value) return

  if (!form.value.title.trim()) {
    error.value = 'Tytuł poradnika nie może być pusty.'
    return
  }
  if (!form.value.content.trim()) {
    error.value = 'Treść poradnika nie może być pusta.'
    return
  }
  if (!form.value.photos.length) {
    // obejście buga w backendzie – bez zdjęcia jest 500
    error.value = 'Dodaj przynajmniej jedno zdjęcie.'
    return
  }

  saving.value = true
  error.value = null

  try {
    const fd = new FormData()
    fd.append('title', form.value.title)
    fd.append('content', form.value.content)

    for (const m of form.value.methods) {
      fd.append('methods', m)
    }
    for (const id of form.value.fishIds) {
      fd.append('fishIds', String(id))
    }
    for (const photo of form.value.photos) {
      fd.append('photos', photo)
    }

    console.log('Tutorial create payload (FormData):', {
      title: form.value.title,
      content: form.value.content,
      methods: form.value.methods,
      fishIds: form.value.fishIds,
      photosCount: form.value.photos.length,
    })

    await apiClient.post('/tutorials/create', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    router.push('/guides')
  } catch (e) {
    console.error('Błąd tworzenia poradnika', e)
    error.value = 'Nie udało się utworzyć poradnika (błąd po stronie serwera).'
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

    <div v-if="!isAdmin" class="text-sm opacity-80">
      Brak uprawnień do dodawania poradników.
    </div>

    <form
        v-else
        class="space-y-4 max-w-3xl"
        @submit.prevent="submitForm"
    >
      <div>
        <label class="block text-sm font-medium mb-1">
          Tytuł poradnika
        </label>
        <input
            v-model="form.title"
            type="text"
            class="w-full border rounded-md px-3 py-2 bg-[var(--color-bg)] text-sm"
            placeholder="Np. Spinning na szczupaka i okonia"
        />
      </div>

      <div>
        <label class="block text-sm font-medium mb-1">
          Treść poradnika
        </label>
        <textarea
            v-model="form.content"
            rows="10"
            class="w-full border rounded-md px-3 py-2 bg-[var(--color-bg)] text-sm resize-y"
            placeholder="Opisz krok po kroku zestaw, przynęty, prowadzenie, wskazówki…"
        />
      </div>

      <div>
        <p class="text-sm font-medium mb-1">
          Metody połowu (opcjonalnie)
        </p>
        <div class="flex flex-wrap gap-2">
          <button
              v-for="m in allMethods"
              :key="m"
              type="button"
              class="px-2 py-1 text-xs rounded-full border"
              :class="form.methods.includes(m) ? 'bg-white/10 font-semibold' : 'bg-[var(--color-bg)] hover:bg-white/5'"
              @click="toggleMethod(m)"
          >
            {{ methodLabel(m) }}
          </button>
        </div>
      </div>

      <div>
        <p class="text-sm font-medium mb-1">
          Gatunki ryb (opcjonalnie)
        </p>

        <!-- wyszukiwarka ryb -->
        <div class="flex flex-wrap items-center gap-2 mb-2">
          <input
              v-model="fishSearch"
              type="text"
              class="px-2 py-1 text-sm border rounded-md bg-[var(--color-bg)] min-w-[200px]"
              placeholder="Np. szczupak, okoń…"
          />
          <button
              type="button"
              class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-white/5"
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

        <!-- checkboxy z wynikami -->
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

        <div
            v-else-if="fishSearch.length >= 2"
            class="text-xs opacity-70 mb-1"
        >
          Brak wyników.
        </div>

        <!-- lista wybranych gatunków -->
        <div
            v-if="selectedFishList.length"
            class="mt-2"
        >
          <div class="text-xs opacity-70 mb-1">
            Wybrane gatunki:
          </div>
          <div class="flex flex-wrap gap-2">
            <span
                v-for="sf in selectedFishList"
                :key="sf.id"
                class="text-xs px-2 py-0.5 rounded-full border border-white/20"
            >
              {{ sf.name }}
            </span>
          </div>
        </div>
      </div>

      <div>
        <label class="block text-sm font-medium mb-1">
          Zdjęcia (wymagane – minimum jedno)
        </label>
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
