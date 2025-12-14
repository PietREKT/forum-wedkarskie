<script setup>
import { ref, onMounted } from 'vue'
import { apiClient } from '../../utils/axios.js'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()

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

async function loadFish() {
  try {
    const resp = await apiClient.get('/fish')
    fishOptions.value = Array.isArray(resp.data) ? resp.data : resp.data?.content ?? []
  } catch {
    fishOptions.value = []
  }
}

onMounted(loadFish)

function triggerPhotos() {
  photosInput.value?.click()
}

function onPhotosChange(e) {
  const files = e.target.files
  photos.value = files ? Array.from(files) : []
}

function validate() {
  const e = {}

  if (!auth.user) e.form = 'Musisz być zalogowany, aby zgłosić łowisko.'
  if (!form.value.name.trim()) e.name = 'Nazwa jest wymagana.'
  if (!form.value.ownerType) e.ownerType = 'Wybierz rodzaj łowiska.'
  if (!form.value.addressText.trim()) e.addressText = 'Adres / lokalizacja jest wymagana.'

  const lat = Number(form.value.latitude)
  const lng = Number(form.value.longitude)

  if (!form.value.latitude) e.latitude = 'Szerokość geograficzna jest wymagana.'
  else if (Number.isNaN(lat)) e.latitude = 'Niepoprawna szerokość geograficzna.'

  if (!form.value.longitude) e.longitude = 'Długość geograficzna jest wymagana.'
  else if (Number.isNaN(lng)) e.longitude = 'Niepoprawna długość geograficzna.'

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
}

async function submit() {
  if (!validate()) return

  const typeEnum = form.value.ownerType === 'PZW' ? 'PUBLIC' : 'PRIVATE'

  const payload = {
    name: form.value.name.trim(),
    description: form.value.regulationText.trim() || null,
    type: typeEnum,
    fishIds: selectedFishIds.value,
    locationDto: {
      longitude: Number(form.value.longitude),
      latitude: Number(form.value.latitude),
      address: {
        countryCode: 'PL',
        municipality: null,
        city: form.value.addressText.trim() || null,
        street: null,
        propertyNo: null,
      },
    },
  }

  try {
    errors.value = {}
    await apiClient.post('/spots/create', payload)
    submitted.value = true
    resetForm()
    setTimeout(() => (submitted.value = false), 2500)
  } catch {
    errors.value = { ...errors.value, form: 'Nie udało się wysłać zgłoszenia.' }
  }
}
</script>

<template>
  <div class="text-xs">
    <h3 class="font-semibold mb-2">Zgłoś nowe łowisko</h3>

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
        <label>Zdjęcia (tymczasowo bez wysyłki do backendu)</label>
        <input ref="photosInput" type="file" multiple class="hidden" @change="onPhotosChange" />
        <div class="flex items-center gap-2">
          <button type="button" class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs" @click="triggerPhotos">
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
          class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs"
          @click="submit"
      >
        Wyślij zgłoszenie
      </button>
      <span v-if="submitted" class="opacity-90">Zgłoszenie przyjęte.</span>
    </div>
  </div>
</template>
