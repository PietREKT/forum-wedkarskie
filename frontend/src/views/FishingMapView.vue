<script setup>
import { onMounted, onBeforeUnmount, ref, computed } from 'vue'
import L from 'leaflet'

import FishingFiltersPanel from '../components/map/FishingFiltersPanel.vue'
import FishingSearchPanel from '../components/map/FishingSearchPanel.vue'
import FishingDetailsPanel from '../components/map/FishingDetailsPanel.vue'

const map = ref(null)
const markersLayer = ref(null)
const panelsVisible = ref(true)

// DEMO dane łowisk (lekko zbliżone do struktury backendu)
const spots = ref([
  {
    id: 1,
    name: 'Jezioro Testowe',
    type: 'Jezioro',
    voivodeship: 'Warmińsko-mazurskie',
    ownerType: 'PZW',
    surfaceHa: 35,
    hasPier: true,
    boatAccess: true,
    fish: ['Szczupak', 'Sandacz', 'Leszcz'],
    description: 'Przykładowe jezioro do prezentacji widoku.',
    avgRating: 4.3,
    ratingCount: 27,
    lat: 53.8,
    lng: 20.5,
    locationX: 20.5,
    locationY: 53.8,
  },
  {
    id: 2,
    name: 'Łowisko komercyjne 1',
    type: 'Staw',
    voivodeship: 'Mazowieckie',
    ownerType: 'Komercyjne',
    surfaceHa: 12,
    hasPier: false,
    boatAccess: false,
    fish: ['Karp', 'Amur', 'Sum'],
    description: 'Komercyjne łowisko pokazowe.',
    avgRating: 4.8,
    ratingCount: 102,
    lat: 52.15,
    lng: 21.0,
    locationX: 21.0,
    locationY: 52.15,
  },
  {
    id: 3,
    name: 'Rzeka Przykładowa',
    type: 'Rzeka',
    voivodeship: 'Śląskie',
    ownerType: 'PZW',
    surfaceHa: null,
    hasPier: false,
    boatAccess: true,
    fish: ['Okoń', 'Jaź', 'Kleń'],
    description: 'Odcinek rzeki używany tylko jako przykład.',
    avgRating: 3.9,
    ratingCount: 11,
    lat: 50.3,
    lng: 18.9,
    locationX: 18.9,
    locationY: 50.3,
  },
])

const selectedSpot = ref(spots.value[0] || null)

// filtry (lokalne, tylko frontend)
const filters = ref({
  voivodeship: 'Dowolne',
  waterBodyType: 'Dowolny',
  fishQuery: '',
  spotType: 'Dowolne',
})

const visibleSpots = computed(() =>
    spots.value.filter((spot) => {
      const f = filters.value

      if (f.voivodeship !== 'Dowolne' && spot.voivodeship !== f.voivodeship) {
        return false
      }

      if (f.waterBodyType !== 'Dowolny' && spot.type !== f.waterBodyType) {
        return false
      }

      if (f.spotType !== 'Dowolne') {
        if (f.spotType === 'PZW / koło' && spot.ownerType !== 'PZW') return false
        if (f.spotType === 'Prywatne / komercyjne' && spot.ownerType !== 'Komercyjne') return false
        if (f.spotType === 'Własne' && spot.ownerType !== 'Własne') return false
      }

      if (f.fishQuery) {
        const q = f.fishQuery.toLowerCase()
        const hasFish = (spot.fish || []).some((name) =>
            name.toLowerCase().includes(q),
        )
        if (!hasFish) return false
      }

      return true
    }),
)

function togglePanels() {
  panelsVisible.value = !panelsVisible.value
}

function selectSpot(spot) {
  selectedSpot.value = spot
  if (spot.lat != null && spot.lng != null && map.value) {
    map.value.setView([spot.lat, spot.lng], 11)
  }
}

// MAPA + MARKERY

function initMap() {
  const container = document.getElementById('fishing-map')
  if (!container) return

  if (map.value) {
    map.value.remove()
    map.value = null
  }

  const startLat = 52.2297
  const startLng = 21.0122

  const instance = L.map(container).setView([startLat, startLng], 6)

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

function renderMarkers() {
  if (!map.value || !markersLayer.value) return
  clearMarkers()

  for (const spot of visibleSpots.value) {
    if (spot.lat == null || spot.lng == null) continue
    const marker = L.marker([spot.lat, spot.lng])
    marker.on('click', () => selectSpot(spot))
    markersLayer.value.addLayer(marker)
  }
}

onMounted(() => {
  initMap()
})

onBeforeUnmount(() => {
  if (map.value) {
    map.value.remove()
    map.value = null
  }
})
</script>

<template>
  <div class="fixed inset-x-0 bottom-0 top-[56px]">
    <div class="relative w-full h-full">
      <!-- Mapa -->
      <div id="fishing-map" class="absolute inset-0 z-0"></div>

      <!-- Przycisk "Pokaż panele"  -->
      <Transition name="fade-btn">
        <button
            v-if="!panelsVisible"
            @click="togglePanels"
            class="absolute left-4 top-4 z-20 px-3 py-1 rounded-full text-xs bg-black/80 text-white border border-white/60 backdrop-blur"
        >
          Pokaż panele
        </button>
      </Transition>

      <!-- Panele -->
      <Transition name="fade-panels">
        <div
            v-if="panelsVisible"
            class="relative z-10 h-full flex text-white min-h-0"
        >
          <!-- Lewe filtry -->
          <FishingFiltersPanel
              class="w-1/4 max-w-sm"
              v-model:filters="filters"
              @hide="togglePanels"
          />

          <!-- Środkowa lista -->
          <FishingSearchPanel
              class="w-1/4 max-w-sm"
              :spots="visibleSpots"
              :selected-id="selectedSpot?.id ?? null"
              @select="selectSpot"
          />

          <!-- Prawy panel szczegółów -->
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
