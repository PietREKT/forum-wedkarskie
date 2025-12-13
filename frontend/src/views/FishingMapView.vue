<script setup>
import { onMounted, onBeforeUnmount, ref, computed, watch } from 'vue'
import L from 'leaflet'

import FishingFiltersPanel from '../components/map/FishingFiltersPanel.vue'
import FishingSearchPanel from '../components/map/FishingSearchPanel.vue'
import FishingDetailsPanel from '../components/map/FishingDetailsPanel.vue'
import { apiClient } from '../utils/axios.js'
import { useAuthStore } from '../stores/auth'

const map = ref(null)
const markersLayer = ref(null)

const sidePanelsVisible = ref(true)
const detailsVisible = ref(true)

const spots = ref([])
const loading = ref(false)
const error = ref(null)

const selectedSpot = ref(null)

const opinions = ref([])
const opinionsLoading = ref(false)

const events = ref([])
const eventsLoading = ref(false)

const ownerInfo = ref({ is_owner: false })
const ownerLoading = ref(false)

const auth = useAuthStore()
const myOpinion = ref(null)

// tylko rodzaj łowiska
const filters = ref({
  spotType: 'ALL', // ALL | PUBLIC | PRIVATE
})

const averageRating = computed(() => {
  if (!opinions.value.length) return null
  const sum = opinions.value.reduce(
      (acc, op) => acc + (typeof op.rating === 'number' ? op.rating : 0),
      0,
  )
  return opinions.value.length ? sum / opinions.value.length : null
})

const visibleSpots = computed(() =>
    spots.value.filter((spot) => {
      const f = filters.value

      if (f.spotType === 'PUBLIC' && spot.type !== 'PUBLIC') return false
      if (f.spotType === 'PRIVATE' && spot.type !== 'PRIVATE') return false

      return true
    }),
)

const selectedSpotId = computed(() => selectedSpot.value?.id ?? null)

function toggleSidePanels() {
  sidePanelsVisible.value = !sidePanelsVisible.value
}

function toggleDetails() {
  detailsVisible.value = !detailsVisible.value
}

async function selectSpot(spot) {
  if (!spot) {
    selectedSpot.value = null
    opinions.value = []
    events.value = []
    ownerInfo.value = { is_owner: false }
    myOpinion.value = null
    return
  }

  selectedSpot.value = spot

  const { lat, lng } = getSpotLatLng(spot)
  if (lat != null && lng != null && map.value) {
    map.value.setView([lat, lng], 11)
  }

  await loadSpotExtras(spot.id)
}

function onApplyFilters(snapshot) {
  filters.value = { ...filters.value, ...snapshot }
  reloadSpots()
}

// MAPA

function initMap() {
  const container = document.getElementById('fishing-map')
  if (!container) return

  if (map.value) {
    map.value.remove()
    map.value = null
  }

  const instance = L.map(container).setView([52.2297, 21.0122], 6)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors',
    maxZoom: 19,
  }).addTo(instance)

  markersLayer.value = L.layerGroup().addTo(instance)
  map.value = instance

  renderMarkers()
}

function clearMarkers() {
  if (markersLayer.value) {
    markersLayer.value.clearLayers()
  }
}

function getSpotLatLng(spot) {
  if (spot.locationY != null && spot.locationX != null) {
    return { lat: spot.locationY, lng: spot.locationX }
  }
  if (spot.lat != null && spot.lng != null) {
    return { lat: spot.lat, lng: spot.lng }
  }
  return { lat: null, lng: null }
}

function renderMarkers() {
  if (!map.value || !markersLayer.value) return

  clearMarkers()

  for (const spot of visibleSpots.value) {
    const { lat, lng } = getSpotLatLng(spot)
    if (lat == null || lng == null) continue

    const marker = L.marker([lat, lng])
    marker.on('click', () => selectSpot(spot))
    markersLayer.value.addLayer(marker)
  }
}

// POBIERANIE ŁOWISK

async function loadAllSpots() {
  loading.value = true
  error.value = null
  try {
    const { data } = await apiClient.get('/spots', {
      baseURL: '',
      params: { page: 0, size: 500 },
    })

    let list = []
    if (Array.isArray(data)) {
      list = data
    } else if (data && Array.isArray(data.content)) {
      list = data.content
    }

    spots.value = list

    if (!spots.value.length) {
      await selectSpot(null)
    } else {
      const currentId = selectedSpot.value?.id
      const next =
          spots.value.find((s) => s.id === currentId) ?? spots.value[0]
      await selectSpot(next)
    }

    renderMarkers()
  } catch (e) {
    console.error('Błąd pobierania łowisk', e)
    error.value = 'Nie udało się pobrać łowisk.'
  } finally {
    loading.value = false
  }
}

async function reloadSpots() {
  await loadAllSpots()
}

// SZCZEGÓŁY / OPINIE / WYDARZENIA / WŁAŚCICIEL

async function loadSpotDetails(id) {
  try {
    const { data } = await apiClient.get(`/spots/${id}`, {
      baseURL: '',
    })
    if (selectedSpot.value && selectedSpot.value.id === id) {
      selectedSpot.value = data
    }
  } catch (e) {
    console.error('Błąd pobierania szczegółów łowiska', e)
  }
}

async function loadSpotOpinions(id) {
  if (!id) return
  opinionsLoading.value = true
  try {
    const { data } = await apiClient.get(`/spots/opinions/${id}`, {
      params: { page: 0, size: 50 },
    })

    const list = Array.isArray(data) ? data : data.content ?? []
    opinions.value = list

    const currentUser = auth.user
    if (currentUser) {
      const uid = currentUser.id
      const uname = currentUser.username

      myOpinion.value =
          list.find((op) => {
            const a = op.author || {}
            if (uid && a.id && a.id === uid) return true
            if (uname && a.username && a.username === uname) return true
            return false
          }) || null
    } else {
      myOpinion.value = null
    }
  } catch (e) {
    console.error('Błąd pobierania opinii o łowisku', e)
    opinions.value = []
    myOpinion.value = null
  } finally {
    opinionsLoading.value = false
  }
}

async function loadSpotEvents(id) {
  eventsLoading.value = true
  try {
    const { data } = await apiClient.get(`/spots/${id}/events`, {
      baseURL: '',
      params: { page: 0, size: 50 },
    })
    events.value = Array.isArray(data) ? data : data.content ?? []
  } catch (e) {
    console.error('Błąd pobierania wydarzeń dla łowiska', e)
    events.value = []
  } finally {
    eventsLoading.value = false
  }
}

async function loadSpotOwnerInfo(id) {
  ownerLoading.value = true
  try {
    const { data } = await apiClient.get(`/spots/${id}/owner`, {
      baseURL: '',
    })
    ownerInfo.value = data || { is_owner: false }
  } catch (e) {
    console.error('Błąd pobierania informacji o właścicielu', e)
    ownerInfo.value = { is_owner: false }
  } finally {
    ownerLoading.value = false
  }
}

async function loadSpotExtras(id) {
  if (!id) return
  await Promise.all([
    loadSpotDetails(id),
    loadSpotOpinions(id),
    loadSpotEvents(id),
    loadSpotOwnerInfo(id),
  ])
}

// OCENA + USUNIĘCIE

async function onRateSpot({ spotId, rating, opinionId }) {
  if (!spotId || !rating) return
  try {
    if (opinionId) {
      await apiClient.patch(`/spots/opinions/${opinionId}`, {
        rating,
        comment: myOpinion.value?.comment ?? null,
      })
    } else {
      await apiClient.post('/spots/opinions', {
        spotId,
        rating,
        comment: null,
      })
    }

    // odśwież opinie
    await loadSpotOpinions(spotId)

    // przelicz średnią i liczbę głosów
    const avg = averageRating.value
    const count = opinions.value.length

    // zaktualizuj wybrane łowisko (panel po prawej)
    if (selectedSpot.value && selectedSpot.value.id === spotId) {
      selectedSpot.value = {
        ...selectedSpot.value,
        avgRating: avg,
        ratingCount: count,
      }
    }

    // zaktualizuj listę łowisk (panel po lewej)
    const idx = spots.value.findIndex((s) => s.id === spotId)
    if (idx !== -1) {
      spots.value[idx] = {
        ...spots.value[idx],
        avgRating: avg,
        ratingCount: count,
      }
    }
  } catch (e) {
    console.error('Błąd wysyłania oceny łowiska', e)
  }
}

async function onDeleteSpot() {
  if (!selectedSpot.value?.id) return
  const ok = window.confirm('Czy na pewno chcesz usunąć to łowisko?')
  if (!ok) return
  try {
    await apiClient.delete(`/spots/${selectedSpot.value.id}/delete`, {
      baseURL: '',
    })
    await reloadSpots()
  } catch (e) {
    console.error('Błąd usuwania łowiska', e)
  }
}

// po utworzeniu łowiska (formularz)
async function onSpotCreated(newSpot) {
  await reloadSpots()
  const createdId = newSpot?.id
  if (createdId) {
    const found = spots.value.find((s) => s.id === createdId)
    if (found) {
      await selectSpot(found)
    }
  }
}

// LIFECYCLE

onMounted(async () => {
  initMap()
  await loadAllSpots()
})

onBeforeUnmount(() => {
  if (map.value) {
    map.value.remove()
    map.value = null
  }
})

watch(
    () => visibleSpots.value,
    () => {
      renderMarkers()
    },
)
</script>

<template>
  <div class="fixed inset-x-0 bottom-0 top-[56px]">
    <div class="relative w-full h-full">
      <div id="fishing-map" class="absolute inset-0 z-0"></div>

      <div
          v-if="loading"
          class="absolute left-4 bottom-4 z-20 px-3 py-1 rounded-full text-xs bg-black/80 text-white border border-white/60 backdrop-blur"
      >
        Ładowanie łowisk...
      </div>

      <div
          v-else-if="error"
          class="absolute left-4 bottom-4 z-20 px-3 py-1 rounded-full text-xs bg-red-700/80 text-white border border-white/60 backdrop-blur"
      >
        {{ error }}
      </div>

      <Transition name="fade-btn">
        <button
            v-if="!sidePanelsVisible"
            @click="toggleSidePanels"
            class="absolute left-4 top-4 z-20 px-3 py-1 rounded-full text-xs bg-black/80 text-white border border-white/60 backdrop-blur"
        >
          Pokaż filtry i wyszukiwarkę
        </button>
      </Transition>

      <Transition name="fade-btn">
        <button
            v-if="!detailsVisible"
            @click="toggleDetails"
            class="absolute right-4 top-4 z-20 px-3 py-1 rounded-full text-xs bg-black/80 text-white border border-white/60 backdrop-blur"
        >
          Pokaż szczegóły łowiska
        </button>
      </Transition>

      <div class="absolute inset-0 z-10 flex pointer-events-none">
        <Transition name="fade-panels">
          <div
              v-if="sidePanelsVisible"
              class="pointer-events-auto flex flex-col md:flex-row gap-0 w-full max-w-[640px]"
          >
            <FishingFiltersPanel
                class="w-full md:w-1/2"
                v-model:filters="filters"
                @hide="toggleSidePanels"
                @apply="onApplyFilters"
            />

            <FishingSearchPanel
                class="w-full md:w-1/2"
                :spots="visibleSpots"
                :selected-id="selectedSpotId"
                @select="selectSpot"
            />
          </div>
        </Transition>

        <Transition name="fade-panels">
          <div
              v-if="detailsVisible"
              class="pointer-events-auto ml-auto w-[460px] max-w-full h-full"
          >
            <FishingDetailsPanel
                class="h-full"
                :spot="selectedSpot"
                :opinions="opinions"
                :opinions-loading="opinionsLoading"
                :average-rating="averageRating"
                :events="events"
                :events-loading="eventsLoading"
                :owner-info="ownerInfo"
                :user-opinion="myOpinion"
                @rate-spot="onRateSpot"
                @delete-spot="onDeleteSpot"
                @spot-created="onSpotCreated"
            />
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-panels-enter-active,
.fade-panels-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.fade-panels-enter-from,
.fade-panels-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}
.fade-btn-enter-active,
.fade-btn-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.fade-btn-enter-from,
.fade-btn-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
