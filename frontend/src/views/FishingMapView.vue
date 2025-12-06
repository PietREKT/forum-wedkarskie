<script setup>
import { onMounted, onBeforeUnmount, ref, computed, watch } from 'vue'
import L from 'leaflet'

import FishingFiltersPanel from '../components/map/FishingFiltersPanel.vue'
import FishingSearchPanel from '../components/map/FishingSearchPanel.vue'
import FishingDetailsPanel from '../components/map/FishingDetailsPanel.vue'

const map = ref(null)
const markersLayer = ref(null)
const panelsVisible = ref(true)

const spots = ref([])
const loading = ref(false)
const error = ref(null)

const selectedSpot = ref(null)

const filters = ref({
  spotType: 'Dowolne', // 'Dowolne' | 'PZW / koło' | 'Prywatne / komercyjne'
  mode: 'ALL',         // ALL | RADIUS
  radiusKm: 50,
})

// filtrowanie po typie łowiska (PUBLIC/PRIVATE)
const visibleSpots = computed(() =>
    spots.value.filter((spot) => {
      const f = filters.value
      if (f.spotType === 'PZW / koło' && spot.type !== 'PUBLIC') return false
      if (f.spotType === 'Prywatne / komercyjne' && spot.type !== 'PRIVATE') {
        return false
      }
      return true
    }),
)

function togglePanels() {
  panelsVisible.value = !panelsVisible.value
}

function selectSpot(spot) {
  selectedSpot.value = spot

  const { lat, lng } = getSpotLatLng(spot)
  if (lat != null && lng != null && map.value) {
    map.value.setView([lat, lng], 11)
  }
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
  // DTO listy ma locationX (lon) / locationY (lat)
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

// ---------------- POBIERANIE DANYCH ----------------

async function loadAllSpots() {
  loading.value = true
  error.value = null
  try {
    const res = await fetch('/spots?page=0&size=200')
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const body = await res.json()
    const content = Array.isArray(body) ? body : body.content ?? []
    spots.value = content
    if (!selectedSpot.value && spots.value.length > 0) {
      selectedSpot.value = spots.value[0]
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

    const url =
        `/spots/radius?x=${encodeURIComponent(x)}` +
        `&y=${encodeURIComponent(y)}` +
        `&radiusKm=${encodeURIComponent(radiusKm)}` +
        `&page=0&size=200`

    const res = await fetch(url)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const body = await res.json()
    const content = Array.isArray(body) ? body : body.content ?? []
    spots.value = content
    if (!selectedSpot.value && spots.value.length > 0) {
      selectedSpot.value = spots.value[0]
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
