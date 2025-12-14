<script setup>
import { ref, onMounted } from 'vue'
import { apiClient } from '../../utils/axios.js'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const emit = defineEmits(['created'])

const fishOptions = ref([])

const newSpotForm = ref({
  name: '',
  addressText: '',
  latitude: '',
  longitude: '',
  ownerType: '', // PZW / Komercyjne / Własne -> mapowane na PUBLIC/PRIVATE
  regulationText: '',
})

const selectedFishIds = ref([])

const submitted = ref(false)
const errors = ref({})

async function loadFish() {
  try {
    // 1) spróbuj pełnej listy
    const resp = await apiClient.get('/fish')
    const list = Array.isArray(resp.data) ? resp.data : resp.data?.content ?? []
    fishOptions.value = list
  } catch (e1) {
    try {
      // 2) fallback: search
      const resp = await apiClient.get('/fish/search', { params: { q: '' } })
      const list = Array.isArray(resp.data) ? resp.data : resp.data?.content ?? []
      fishOptions.value = list
    } catch (e2) {
      console.error('Błąd pobierania listy ryb', e2)
      fishOptions.value = []
    }
  }
}

onMounted(loadFish)

function validate() {
  const e = {}

  if (!newSpotForm.value.name.trim()) e.name = 'Nazwa jest wymagana.'
  if (!newSpotForm.value.ownerType) e.ownerType = 'Wybierz rodzaj łowiska.'
  if (!newSpotForm.value.addressText.trim()) e.addressText = 'Adres / lokalizacja jest wymagana.'

  if (!auth.user) e.form = 'Musisz być zalogowany, aby zgłosić łowisko.'

  const lat = newSpotForm.value.latitude ? Number(newSpotForm.value.latitude) : null
  const lng = newSpotForm.value.longitude ? Number(newSpotForm.value.longitude) : null
  if (newSpotForm.value.latitude && (lat == null || Number.isNaN(lat))) e.latitude = 'Niepoprawna szerokość geograficzna.'
  if (newSpotForm.value.longitude && (lng == null || Number.isNaN(lng))) e.longitude = 'Niepoprawna długość geograficzna.'

  errors.value = e
  return Object.keys(e).length === 0
}

function resetForm() {
  newSpotForm.value = {
    name: '',
    addressText: '',
    latitude: '',
    longitude: '',
    ownerType: '',
    regulationText: '',
  }
  selectedFishIds.value = []
  errors.value = {}
}

async function submit() {
  if (!validate()) return

  const typeEnum = newSpotForm.value.ownerType === 'PZW' ? 'PUBLIC' : 'PRIVATE'

  const payload = {
    name: newSpotForm.value.name.trim(),
    description: newSpotForm.value.regulationText.trim() || null,
    type: typeEnum,
    managerIds: auth.user ? [auth.user.id] : [],
    fishIds: selectedFishIds.value,
    locationDto: {
      longitude: newSpotForm.value.longitude ? Number(newSpotForm.value.longitude) : null,
      latitude: newSpotForm.value.latitude ? Number(newSpotForm.value.latitude) : null,
      // w UI masz jedno pole tekstowe, więc wkładamy je jako city (żeby coś nie było puste)
      address: {
        countryCode: 'PL',
        municipality: null,
        city: newSpotForm.value.addressText.trim() || null,
        street: null,
        propertyNo: null,
      },
    },
  }

  try {
    submitted.value = false
    errors.value = {}

    const { data } = await apiClient.post('/spots/create', payload)

    emit('created', data)

    submitted.value = true
    resetForm()

    setTimeout(() => {
      submitted.value = false
    }, 2500)
  } catch (e) {
    console.error('Błąd wysyłania zgłoszenia łowiska', e)
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
            v-model="newSpotForm.name"
            type="text"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
            placeholder="np. Jezioro X"
        />
        <span v-if="errors.name" class="text-red-300">{{ errors.name }}</span>
      </div>

      <div class="flex flex-col gap-1">
        <label>Rodzaj łowiska <span class="text-red-300">*</span></label>
        <select
            v-model="newSpotForm.ownerType"
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
            v-model="newSpotForm.addressText"
            type="text"
            placeholder="np. miejscowość, opis dojazdu"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
        <span v-if="errors.addressText" class="text-red-300">{{ errors.addressText }}</span>
      </div>

      <div class="flex flex-col gap-1">
        <label>Szerokość geograficzna (lat)</label>
        <input
            v-model="newSpotForm.latitude"
            type="text"
            placeholder="np. 52.2297"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
        <span v-if="errors.latitude" class="text-red-300">{{ errors.latitude }}</span>
      </div>

      <div class="flex flex-col gap-1">
        <label>Długość geograficzna (lng)</label>
        <input
            v-model="newSpotForm.longitude"
            type="text"
            placeholder="np. 21.0122"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
        <span v-if="errors.longitude" class="text-red-300">{{ errors.longitude }}</span>
      </div>

      <div class="flex flex-col gap-1 md:col-span-2">
        <label>Gatunki ryb (można zaznaczyć kilka)</label>
        <div class="grid grid-cols-2 sm:grid-cols-3 gap-1">
          <label
              v-for="f in fishOptions"
              :key="f.id ?? f.name"
              class="inline-flex items-center gap-1 cursor-pointer"
          >
            <input type="checkbox" :value="f.id" v-model="selectedFishIds" class="accent-white" />
            <span>{{ f.name }}</span>
          </label>
        </div>
      </div>
    </div>

    <div class="flex flex-col gap-1 mb-2">
      <label>Regulamin / opis (tekst)</label>
      <textarea
          v-model="newSpotForm.regulationText"
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
