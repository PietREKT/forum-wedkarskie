<script setup>
import { onMounted, onBeforeUnmount, ref, computed, watch } from 'vue'
import L from 'leaflet'

import FishingFiltersPanel from '../components/map/FishingFiltersPanel.vue'
import FishingSearchPanel from '../components/map/FishingSearchPanel.vue'
import FishingDetailsPanel from '../components/map/FishingDetailsPanel.vue'
import { apiClient } from '../utils/axios.js'

const map = ref(null)
const markersLayer = ref(null)
const panelsVisible = ref(true)

const spots = ref([])
const loading = ref(false)
const error = ref(null)

const selectedSpot = ref(null)

// dane dodatkowe
const opinions = ref([])
const opinionsLoading = ref(false)

const events = ref([])
const eventsLoading = ref(false)

const ownerInfo = ref({ is_owner: false })
const ownerLoading = ref(false)

const filters = ref({
  spotType: 'ALL', // ALL | PUBLIC | PRIVATE
  mode: 'ALL',     // ALL | RADIUS
  radiusKm: 50,
})

const averageRating = computed(() => {
  if (!opinions.value.length) return null
  const sum = opinions.value.reduce(
      (acc, op) => acc + (typeof op.rating === 'number' ? op.rating : 0),
      0,
  )
  return opinions.value.length ? sum / opinions.value.length : null
})

// filtrowanie po typie łowiska (PUBLIC/PRIVATE)
const visibleSpots = computed(() =>
    spots.value.filter((spot) => {
      const f = filters.value
      if (f.spotType === 'PUBLIC' && spot.type !== 'PUBLIC') return false
      if (f.spotType === 'PRIVATE' && spot.type !== 'PRIVATE') return false
      return true
    }),
)

function togglePanels() {
  panelsVisible.value = !panelsVisible.value
}

async function selectSpot(spot) {
  if (!spot) {
    selectedSpot.value = null
    opinions.value = []
    events.value = []
    ownerInfo.value = { is_owner: false }
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

// ---------------- MAPA ----------------

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

// ---------------- POBIERANIE ŁOWISK (ŚCIEŻKI BEZ /api!) ----------------

async function loadAllSpots() {
  loading.value = true
  error.value = null
  try {
    const { data } = await apiClient.get('/spots', {
      baseURL: '',          // ważne: bez /api
      params: { page: 0, size: 200 },
    })
    const content = Array.isArray(data) ? data : data.content ?? []
    spots.value = content

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

async function loadSpotsInRadius() {
  if (!map.value) {
    await loadAllSpots()
    return
  }
  loading.value = true
  error.value = null
  try {
    const center = map.value.getCenter()
    const x = center.lng
    const y = center.lat
    const radiusKm = filters.value.radiusKm || 50

    const { data } = await apiClient.get('/spots/radius', {
      baseURL: '',          // bez /api
      params: {
        x,
        y,
        radiusKm,
        page: 0,
        size: 200,
      },
    })

    const content = Array.isArray(data) ? data : data.content ?? []
    spots.value = content

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
    console.error('Błąd pobierania łowisk w promieniu', e)
    error.value = 'Nie udało się pobrać łowisk w promieniu.'
  } finally {
    loading.value = false
  }
}

async function reloadSpots() {
  if (filters.value.mode === 'RADIUS') {
    await loadSpotsInRadius()
  } else {
    await loadAllSpots()
  }
}

// ---------------- DODATKOWE DANE O ŁOWISKU ----------------

async function loadSpotDetails(id) {
  try {
    const { data } = await apiClient.get(`/spots/${id}`, {
      baseURL: '',          // bez /api
    })
    if (selectedSpot.value && selectedSpot.value.id === id) {
      selectedSpot.value = data
    }
  } catch (e) {
    console.error('Błąd pobierania szczegółów łowiska', e)
  }
}

async function loadSpotOpinions(id) {
  opinionsLoading.value = true
  try {
    // UWAGA: opinie są pod ${forum.api.prefix}/spots/opinions → tu zostawiamy /api
    const { data } = await apiClient.get(`/spots/opinions/${id}`, {
      params: { page: 0, size: 50 },
    })
    opinions.value = Array.isArray(data) ? data : data.content ?? []
  } catch (e) {
    console.error('Błąd pobierania opinii o łowisku', e)
    opinions.value = []
  } finally {
    opinionsLoading.value = false
  }
}

async function loadSpotEvents(id) {
  eventsLoading.value = true
  try {
    const { data } = await apiClient.get(`/spots/${id}/events`, {
      baseURL: '',          // bez /api
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
      baseURL: '',          // bez /api
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

async function onRateSpot({ spotId, rating }) {
  if (!spotId || !rating) return
  try {
    // POST /api/spots/opinions – tu ma zostać prefix /api
    await apiClient.post('/spots/opinions', {
      spotId,
      rating,
      comment: null,
    })
    await loadSpotOpinions(spotId)
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
      baseURL: '',          // bez /api
    })
    await reloadSpots()
  } catch (e) {
    console.error('Błąd usuwania łowiska', e)
  }
}

// ---------------- LIFECYCLE ----------------

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
            v-if="!panelsVisible"
            @click="togglePanels"
            class="absolute left-4 top-4 z-20 px-3 py-1 rounded-full text-xs bg-black/80 text-white border border-white/60 backdrop-blur"
        >
          Pokaż panele
        </button>
      </Transition>

      <Transition name="fade-panels">
        <div
            v-if="panelsVisible"
            class="relative z-10 h-full flex text-white min-h-0"
        >
          <FishingFiltersPanel
              class="w-1/4 max-w-sm"
              v-model:filters="filters"
              @hide="togglePanels"
              @apply="onApplyFilters"
          />

          <FishingSearchPanel
              class="w-1/4 max-w-sm"
              :spots="visibleSpots"
              :selected-id="selectedSpot?.id ?? null"
              @select="selectSpot"
          />

          <FishingDetailsPanel
              class="flex-1"
              :spot="selectedSpot"
              :opinions="opinions"
              :opinions-loading="opinionsLoading"
              :average-rating="averageRating"
              :events="events"
              :events-loading="eventsLoading"
              :owner-info="ownerInfo"
              @rate-spot="onRateSpot"
              @delete-spot="onDeleteSpot"
          />
        </div>
      </Transition>
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
