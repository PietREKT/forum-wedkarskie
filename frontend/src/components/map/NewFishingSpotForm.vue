<!-- src/components/map/NewFishingSpotForm.vue -->
<script setup>
import { ref, onMounted, computed } from 'vue'
import { apiClient } from '../../utils/axios.js'
import { useAuthStore } from '../../stores/auth'
import { useFishingSpotsStore } from '../../stores/fishingSpots'

const auth = useAuthStore()
const spotsStore = useFishingSpotsStore()

const isAdmin = computed(() => !!auth.isAdmin)
const isLoggedIn = computed(() => !!auth.user)

const fishOptions = ref([])
const selectedFishIds = ref([])

const photosInput = ref(null)
const photos = ref([])

const form = ref({
  name: '',
  addressText: '',
  latitude: '',
  longitude: '',
  ownerType: '',
  regulationText: '',
})

const submitted = ref(false)
const errors = ref({})
const sending = ref(false)

async function loadFish() {
  try {
    const { data } = await apiClient.get('/fish', { params: { page: 0, size: 500 } })
    fishOptions.value = Array.isArray(data) ? data : data?.content ?? []
  } catch {
    fishOptions.value = []
  }
}

onMounted(loadFish)

function triggerPhotos() {
  photosInput.value?.click()
}

function onPhotosChange(e) {
  const files = e?.target?.files
  photos.value = files ? Array.from(files) : []
}

function validate() {
  const e = {}

  if (!isLoggedIn.value) e.form = 'Musisz być zalogowany, aby dodać / zgłosić łowisko.'
  if (!form.value.name.trim()) e.name = 'Nazwa jest wymagana.'
  if (!form.value.ownerType) e.ownerType = 'Wybierz rodzaj łowiska.'
  if (!form.value.addressText.trim()) e.addressText = 'Adres / lokalizacja jest wymagana.'

  const lat = Number(String(form.value.latitude).replace(',', '.'))
  const lng = Number(String(form.value.longitude).replace(',', '.'))

  if (!String(form.value.latitude).trim()) e.latitude = 'Szerokość geograficzna jest wymagana.'
  else if (Number.isNaN(lat)) e.latitude = 'Niepoprawna szerokość geograficzna.'

  if (!String(form.value.longitude).trim()) e.longitude = 'Długość geograficzna jest wymagana.'
  else if (Number.isNaN(lng)) e.longitude = 'Niepoprawna długość geograficzna.'

  if (!e.latitude && (lat < -90 || lat > 90)) e.latitude = 'Szerokość geograficzna poza zakresem (-90..90).'
  if (!e.longitude && (lng < -180 || lng > 180)) e.longitude = 'Długość geograficzna poza zakresem (-180..180).'

  errors.value = e
  return Object.keys(e).length === 0
}

function resetForm() {
  form.value = {
    name: '',
    addressText: '',
    latitude: '',
    longitude: '',
    ownerType: '',
    regulationText: '',
  }
  selectedFishIds.value = []
  photos.value = []
  errors.value = {}
  if (photosInput.value) photosInput.value.value = ''
}

function extractBackendError(err) {
  const status = err?.response?.status
  const msg =
      err?.response?.data?.message ||
      err?.response?.data?.error ||
      err?.response?.data?.details ||
      (typeof err?.response?.data === 'string' ? err.response.data : null)

  if (status >= 500) return 'Błąd serwera (500). Spróbuj ponownie później.'
  if (status === 409) return 'Nie można wykonać operacji (konflikt danych).'
  if (status === 400) return 'Błędne dane formularza (400).'
  if (status === 401) return 'Brak autoryzacji (401).'
  if (status === 403) return 'Brak uprawnień (403).'
  if (msg) return String(msg)

  return 'Nie udało się wysłać zgłoszenia.'
}

function normalizeIds(list) {
  return (list || [])
      .map((x) => {
        const n = Number(x)
        return Number.isFinite(n) ? n : null
      })
      .filter((x) => x != null)
}

async function uploadSpotPic(spotId, file) {
  if (!spotId || !file) return
  const fd = new FormData()
  fd.append('file', file)

  // nie ustawiaj ręcznie Content-Type (axios doda boundary)
  await apiClient.post(`/spots/${spotId}/pic`, fd)
}

// Minimalny payload zgodny z backendem (bez address obiektu, bez spotType/ownerType)
function buildPayload() {
  const typeEnum = form.value.ownerType === 'PZW' ? 'PUBLIC' : 'PRIVATE'
  const lat = Number(String(form.value.latitude).replace(',', '.'))
  const lng = Number(String(form.value.longitude).replace(',', '.'))

  return {
    name: form.value.name.trim(),
    description: form.value.regulationText.trim() || null,
    type: typeEnum,
    managerIds: [],
    fishIds: normalizeIds(selectedFishIds.value),
    locationDto: {
      longitude: lng,
      latitude: lat,
      address: null,
    },
  }
}

async function refreshAfterCreate(createdId) {
  try {
    await spotsStore.loadAll()
    const found = spotsStore.spots?.find((s) => String(s?.id) === String(createdId))
    if (found) {
      await spotsStore.selectSpot(found, { isLoggedIn: isLoggedIn.value })
    }
  } catch {}
}

async function submit() {
  if (!validate()) return
  if (sending.value) return

  const createUrl = isAdmin.value ? '/admin/spots' : '/spots/create'
  const payload = buildPayload()

  try {
    sending.value = true
    errors.value = {}

    const { data } = await apiClient.post(createUrl, payload)
    const createdId = data?.id ?? data?.spotId ?? null

    if (createdId && photos.value.length) {
      try {
        await uploadSpotPic(createdId, photos.value[0])
      } catch (err) {
        errors.value = {
          ...errors.value,
          form: extractBackendError(err) || 'Łowisko dodane, ale nie udało się wysłać zdjęcia.',
        }
      }
    }

    if (createdId) await refreshAfterCreate(createdId)

    submitted.value = true
    resetForm()
    setTimeout(() => (submitted.value = false), 2500)
  } catch (err) {
    errors.value = { ...errors.value, form: extractBackendError(err) }
  } finally {
    sending.value = false
  }
}

const headerText = computed(() => (isAdmin.value ? 'Dodaj nowe łowisko' : 'Zgłoś nowe łowisko'))
const buttonText = computed(() => (isAdmin.value ? 'Dodaj' : 'Wyślij zgłoszenie'))
</script>

<template>
  <div class="text-xs">
    <h3 class="font-semibold mb-2">{{ headerText }}</h3>

    <p v-if="errors.form" class="mb-2 text-red-300">{{ errors.form }}</p>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-2 mb-2">
      <div class="flex flex-col gap-1">
        <label>Nazwa łowiska <span class="text-red-300">*</span></label>
        <input
            v-model="form.name"
            type="text"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
            placeholder="np. Jezioro X"
        />
        <span v-if="errors.name" class="text-red-300">{{ errors.name }}</span>
      </div>

      <div class="flex flex-col gap-1">
        <label>Rodzaj łowiska <span class="text-red-300">*</span></label>
        <select
            v-model="form.ownerType"
            class="bg-white text-black border border-white/60 rounded px-2 py-1 text-xs outline-none"
        >
          <option value="">Wybierz rodzaj</option>
          <option value="PZW">PZW / koło</option>
          <option value="Komercyjne">Prywatne / komercyjne</option>
          <option value="Własne">Własne</option>
        </select>
        <span v-if="errors.ownerType" class="text-red-300">{{ errors.ownerType }}</span>
      </div>

      <div class="flex flex-col gap-1 md:col-span-2">
        <label>Adres / lokalizacja (opis) <span class="text-red-300">*</span></label>
        <input
            v-model="form.addressText"
            type="text"
            placeholder="np. miejscowość, opis dojazdu"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
        <span v-if="errors.addressText" class="text-red-300">{{ errors.addressText }}</span>
      </div>

      <div class="flex flex-col gap-1">
        <label>Szerokość geograficzna (lat) <span class="text-red-300">*</span></label>
        <input
            v-model="form.latitude"
            type="text"
            placeholder="np. 52.2297"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
        <span v-if="errors.latitude" class="text-red-300">{{ errors.latitude }}</span>
      </div>

      <div class="flex flex-col gap-1">
        <label>Długość geograficzna (lng) <span class="text-red-300">*</span></label>
        <input
            v-model="form.longitude"
            type="text"
            placeholder="np. 21.0122"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
        <span v-if="errors.longitude" class="text-red-300">{{ errors.longitude }}</span>
      </div>

      <div class="flex flex-col gap-1 md:col-span-2">
        <label>Zdjęcie główne łowiska (pierwszy plik będzie wysłany)</label>
        <input ref="photosInput" type="file" accept="image/*" multiple class="hidden" @change="onPhotosChange" />
        <div class="flex items-center gap-2">
          <button
              type="button"
              class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs"
              @click="triggerPhotos"
          >
            Wybierz zdjęcia
          </button>
          <span class="opacity-80" v-if="photos.length">Wybrano: {{ photos.length }}</span>
          <span class="opacity-80" v-else>Brak</span>
        </div>
      </div>

      <div class="flex flex-col gap-1 md:col-span-2">
        <label>Gatunki ryb (można zaznaczyć kilka)</label>
        <div class="grid grid-cols-2 sm:grid-cols-3 gap-1">
          <label v-for="f in fishOptions" :key="f.id ?? f.name" class="inline-flex items-center gap-1 cursor-pointer">
            <input type="checkbox" :value="f.id" v-model="selectedFishIds" class="accent-white" />
            <span>{{ f.name }}</span>
          </label>
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-1 mb-2">
      <label>Regulamin / opis (tekst)</label>
      <textarea
          v-model="form.regulationText"
          rows="3"
          placeholder="Tutaj można wpisać najważniejsze zasady, opłaty, ograniczenia."
          class="w-full bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none resize-none"
      />
    </div>

    <div class="flex items-center gap-3 mt-1">
      <button
          type="button"
          class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs disabled:opacity-60"
          :disabled="sending"
          @click="submit"
      >
        {{ sending ? 'Wysyłanie...' : buttonText }}
      </button>
      <span v-if="submitted" class="opacity-90">Zapisano.</span>
    </div>
  </div>
</template>
