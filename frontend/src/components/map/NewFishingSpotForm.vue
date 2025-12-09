<script setup>
import { ref, onMounted } from 'vue'
import { apiClient } from '../../utils/axios'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()

// lista gatunków z backendu – obiekty z id i name
const fishOptions = ref([])

// stan formularza
const newSpotForm = ref({
  name: '',
  type: '',
  voivodeship: '',
  address: '',
  latitude: '',
  longitude: '',
  ownerType: '',
  surfaceHa: '',
  hasPier: false,
  boatAccess: false,
  regulationText: '',
})

// zaznaczone ryby – LISTA ID (Long) zgodnie z CreateFishingSpotDto.fishIds
const selectedFishIds = ref([])

const regulationInput = ref(null)
const photosInput = ref(null)
const regulationFile = ref(null)
const photos = ref([])

const submitted = ref(false)
const errors = ref({})

// pobieranie gatunków z /api/fish (jak w poradnikach)
async function loadFish() {
  try {
    const resp = await apiClient.get('/fish')
    const list = Array.isArray(resp.data) ? resp.data : (resp.data?.content || [])
    fishOptions.value = list
  } catch (e) {
    console.error('Błąd pobierania listy ryb', e)
  }
}

onMounted(() => {
  loadFish()
})

// obsługa plików – na razie tylko w UI
function triggerRegulationFile() {
  regulationInput.value?.click()
}

function triggerPhotosFile() {
  photosInput.value?.click()
}

function onRegulationFileChange(e) {
  const files = e.target.files
  regulationFile.value = files && files[0] ? files[0] : null
}

function onPhotosChange(e) {
  const files = e.target.files
  photos.value = files ? Array.from(files) : []
}

// walidacja podstawowa
function validate() {
  const e = {}

  if (!newSpotForm.value.name.trim()) e.name = 'Nazwa jest wymagana.'
  if (!newSpotForm.value.type) e.type = 'Wybierz typ łowiska.'
  if (!newSpotForm.value.voivodeship) e.voivodeship = 'Wybierz województwo.'
  if (!newSpotForm.value.ownerType) e.ownerType = 'Wybierz rodzaj łowiska.'
  if (!newSpotForm.value.address.trim()) e.address = 'Adres / lokalizacja jest wymagana.'

  if (!auth.user) {
    e.form = 'Musisz być zalogowany, aby zgłosić łowisko.'
  }

  errors.value = e
  return Object.keys(e).length === 0
}

// reset formularza po sukcesie
function resetForm() {
  newSpotForm.value = {
    name: '',
    type: '',
    voivodeship: '',
    address: '',
    latitude: '',
    longitude: '',
    ownerType: '',
    surfaceHa: '',
    hasPier: false,
    boatAccess: false,
    regulationText: '',
  }
  selectedFishIds.value = []
  regulationFile.value = null
  photos.value = []
  errors.value = {}
}

// wysłanie zgłoszenia do backendu (CreateFishingSpotDto)
async function submit() {
  if (!validate()) return

  // typ łowiska: enum FISHING_SPOT_TYPE { PRIVATE, PUBLIC }
  const typeEnum =
      newSpotForm.value.ownerType === 'PZW'
          ? 'PUBLIC'
          : 'PRIVATE'

  const payload = {
    name: newSpotForm.value.name.trim(),
    description: newSpotForm.value.regulationText.trim() || null,
    type: typeEnum,
    managerIds: auth.user ? [auth.user.id] : [],     // List<UUID>
    fishIds: selectedFishIds.value,                  // List<Long>
    locationDto: {
      longitude: newSpotForm.value.longitude
          ? parseFloat(newSpotForm.value.longitude)
          : null,
      latitude: newSpotForm.value.latitude
          ? parseFloat(newSpotForm.value.latitude)
          : null,
      address: {
        countryCode: 'PL',
        municipality: null,
        city: null,
        street: null,
        propertyNo: null,
      },
    },
  }

  try {
    submitted.value = false
    errors.value = {}

    const { data } = await apiClient.post('/spots/create', payload)
    console.log('Utworzone łowisko:', data)

    submitted.value = true
    resetForm()

    setTimeout(() => {
      submitted.value = false
    }, 2500)
  } catch (e) {
    console.error('Błąd wysyłania zgłoszenia łowiska', e)
    errors.value = {
      ...errors.value,
      form: 'Nie udało się wysłać zgłoszenia.',
    }
  }
}
</script>

<template>
  <div class="text-xs">
    <h3 class="font-semibold mb-2">Zgłoś nowe łowisko</h3>

    <!-- komunikat błędu ogólnego -->
    <p v-if="errors.form" class="mb-2 text-red-300">
      {{ errors.form }}
    </p>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-2 mb-2">
      <div class="flex flex-col gap-1">
        <label>
          Nazwa łowiska
          <span class="text-red-300">*</span>
        </label>
        <input
            v-model="newSpotForm.name"
            type="text"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
            placeholder="np. Jezioro X"
        />
        <span v-if="errors.name" class="text-red-300">
          {{ errors.name }}
        </span>
      </div>

      <div class="flex flex-col gap-1">
        <label>
          Typ łowiska
          <span class="text-red-300">*</span>
        </label>
        <select
            v-model="newSpotForm.type"
            class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
        >
          <option value="">Wybierz typ</option>
          <option value="Rzeka">Rzeka</option>
          <option value="Jezioro">Jezioro</option>
          <option value="Torfowisko">Torfowisko</option>
          <option value="Żwirownia">Żwirownia</option>
          <option value="Staw">Staw</option>
          <option value="Zalewisko">Zalewisko</option>
        </select>
        <span v-if="errors.type" class="text-red-300">
          {{ errors.type }}
        </span>
      </div>

      <div class="flex flex-col gap-1">
        <label>
          Województwo
          <span class="text-red-300">*</span>
        </label>
        <select
            v-model="newSpotForm.voivodeship"
            class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
        >
          <option value="">Wybierz województwo</option>
          <option value="Dolnośląskie">Dolnośląskie</option>
          <option value="Kujawsko-pomorskie">Kujawsko-pomorskie</option>
          <option value="Lubelskie">Lubelskie</option>
          <option value="Lubuskie">Lubuskie</option>
          <option value="Łódzkie">Łódzkie</option>
          <option value="Małopolskie">Małopolskie</option>
          <option value="Mazowieckie">Mazowieckie</option>
          <option value="Opolskie">Opolskie</option>
          <option value="Podkarpackie">Podkarpackie</option>
          <option value="Podlaskie">Podlaskie</option>
          <option value="Pomorskie">Pomorskie</option>
          <option value="Śląskie">Śląskie</option>
          <option value="Świętokrzyskie">Świętokrzyskie</option>
          <option value="Warmińsko-mazurskie">Warmińsko-mazurskie</option>
          <option value="Wielkopolskie">Wielkopolskie</option>
          <option value="Zachodniopomorskie">Zachodniopomorskie</option>
        </select>
        <span v-if="errors.voivodeship" class="text-red-300">
          {{ errors.voivodeship }}
        </span>
      </div>

      <div class="flex flex-col gap-1">
        <label>
          Rodzaj łowiska
          <span class="text-red-300">*</span>
        </label>
        <select
            v-model="newSpotForm.ownerType"
            class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
        >
          <option value="">Wybierz rodzaj</option>
          <option value="PZW">PZW / koło</option>
          <option value="Komercyjne">Prywatne / komercyjne</option>
          <option value="Własne">Własne</option>
        </select>
        <span v-if="errors.ownerType" class="text-red-300">
          {{ errors.ownerType }}
        </span>
      </div>

      <div class="flex flex-col gap-1 md:col-span-2">
        <label>
          Adres / lokalizacja na mapie
          <span class="text-red-300">*</span>
        </label>
        <input
            v-model="newSpotForm.address"
            type="text"
            placeholder="np. miejscowość, opis dojazdu"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
        <span v-if="errors.address" class="text-red-300">
          {{ errors.address }}
        </span>
      </div>

      <div class="flex flex-col gap-1">
        <label>Szerokość geograficzna (lat)</label>
        <input
            v-model="newSpotForm.latitude"
            type="text"
            placeholder="np. 52.2297"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
      </div>

      <div class="flex flex-col gap-1">
        <label>Długość geograficzna (lng)</label>
        <input
            v-model="newSpotForm.longitude"
            type="text"
            placeholder="np. 21.0122"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
      </div>

      <div class="flex flex-col gap-1">
        <label>Powierzchnia (ha)</label>
        <input
            v-model="newSpotForm.surfaceHa"
            type="text"
            placeholder="np. 12"
            class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
      </div>

      <div class="flex flex-col gap-1">
        <label>Przystań</label>
        <select
            v-model="newSpotForm.hasPier"
            class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
        >
          <option :value="false">Nie</option>
          <option :value="true">Tak</option>
        </select>
      </div>

      <div class="flex flex-col gap-1">
        <label>Możliwość wodowania łódki</label>
        <select
            v-model="newSpotForm.boatAccess"
            class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
        >
          <option :value="false">Nie</option>
          <option :value="true">Tak</option>
        </select>
      </div>

      <div class="flex flex-col gap-1 md:col-span-2">
        <label>Gatunki ryb (można zaznaczyć kilka)</label>
        <div class="grid grid-cols-2 sm:grid-cols-3 gap-1">
          <label
              v-for="f in fishOptions"
              :key="f.id ?? f.name"
              class="inline-flex items-center gap-1 cursor-pointer"
          >
            <input
                type="checkbox"
                :value="f.id"
                v-model="selectedFishIds"
                class="accent-white"
            />
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

    <div class="flex flex-col sm:flex-row gap-3 mb-2">
      <div class="flex flex-col gap-1 text-xs flex-1">
        <label>Załącz regulamin (plik, np. PDF / DOC / TXT)</label>
        <input
            ref="regulationInput"
            type="file"
            class="hidden"
            @change="onRegulationFileChange"
        />
        <button
            type="button"
            class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs w-fit"
            @click="triggerRegulationFile"
        >
          Dodaj regulamin
        </button>
        <span v-if="regulationFile" class="opacity-90 mt-1">
          Wybrano: {{ regulationFile.name }}
        </span>
      </div>

      <div class="flex flex-col gap-1 text-xs flex-1">
        <label>Zdjęcia łowiska (można wybrać kilka)</label>
        <input
            ref="photosInput"
            type="file"
            multiple
            class="hidden"
            @change="onPhotosChange"
        />
        <button
            type="button"
            class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs w-fit"
            @click="triggerPhotosFile"
        >
          Dodaj zdjęcia
        </button>
        <span v-if="photos.length" class="opacity-90 mt-1">
          Wybrano plików: {{ photos.length }}
        </span>
      </div>
    </div>

    <div class="flex items-center gap-3 mt-1">
      <button
          type="button"
          class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs"
          @click="submit"
      >
        Wyślij zgłoszenie
      </button>
      <span v-if="submitted" class="opacity-90">
        Zgłoszenie przyjęte.
      </span>
    </div>
  </div>
</template>
