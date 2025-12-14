<script setup>
import { onMounted, onBeforeUnmount, ref, computed, watch } from 'vue'
import L from 'leaflet'

import FishingFiltersPanel from '../components/map/FishingFiltersPanel.vue'
import FishingSearchPanel from '../components/map/FishingSearchPanel.vue'
import FishingDetailsPanel from '../components/map/FishingDetailsPanel.vue'
import { apiClient } from '../utils/axios.js'
import { useAuthStore } from '../stores/auth'

const MODERATION_API = {
  listPending: '/admin/spots/pending',
  approve: (id) => `/admin/spots/${id}/approve`,
  reject: (id) => `/admin/spots/${id}/reject`,
}

const auth = useAuthStore()

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

const myOpinion = ref(null)

// favourites
const favouritesIds = ref(new Set())
const favouritesLoading = ref(false)

// moderation
const moderationOpen = ref(false)
const pendingSpots = ref([])
const pendingLoading = ref(false)
const pendingError = ref(null)
const pendingSelectedId = ref(null)
const moderationBusy = ref(false)

const filters = ref({
  spotType: 'ALL', // ALL | PUBLIC | PRIVATE
})

const selectedSpotId = computed(() => selectedSpot.value?.id ?? null)

const visibleSpots = computed(() =>
    spots.value.filter((spot) => {
      const f = filters.value
      if (f.spotType === 'PUBLIC' && spot.type !== 'PUBLIC') return false
      if (f.spotType === 'PRIVATE' && spot.type !== 'PRIVATE') return false
      return true
    }),
)

const isSelectedFavourite = computed(() => {
  const id = selectedSpotId.value
  if (!id) return false
  return favouritesIds.value.has(id)
})

const isAdminOrRoot = computed(() => !!auth.isAdmin)

const pendingIds = computed(() => new Set((pendingSpots.value || []).map((s) => s?.id).filter(Boolean)))
const isSelectedPending = computed(() => {
  const id = selectedSpotId.value
  if (!id) return false
  return pendingIds.value.has(id)
})

function toggleSidePanels() {
  sidePanelsVisible.value = !sidePanelsVisible.value
}

function toggleDetails() {
  detailsVisible.value = !detailsVisible.value
}

function onApplyFilters(snapshot) {
  filters.value = { ...filters.value, ...snapshot }
  renderMarkers()
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

  instance.on('moveend', scheduleReloadFromMap)
  instance.on('zoomend', scheduleReloadFromMap)

  renderMarkers()
}

function clearMarkers() {
  markersLayer.value?.clearLayers()
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
      params: { page: 0, size: 500 },
    })

    const list = Array.isArray(data) ? data : data?.content ?? []
    spots.value = list

    if (!spots.value.length) {
      await selectSpot(null)
    } else {
      const currentId = selectedSpot.value?.id
      const next = spots.value.find((s) => s.id === currentId) ?? spots.value[0]
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

function getRadiusKmFromMap() {
  if (!map.value) return null
  const b = map.value.getBounds()
  const center = map.value.getCenter()
  const northEast = b.getNorthEast()
  const meters = center.distanceTo(northEast)
  const km = Math.max(1, Math.round(meters / 1000))
  return km
}

async function loadSpotsByRadius() {
  if (!map.value) return
  loading.value = true
  error.value = null
  try {
    const center = map.value.getCenter()
    const radiusKm = getRadiusKmFromMap()
    if (!radiusKm) {
      await loadAllSpots()
      return
    }

    const { data } = await apiClient.get('/spots/radius', {
      params: {
        x: center.lng,
        y: center.lat,
        radiusKm,
        page: 0,
        size: 500,
      },
    })

    const list = Array.isArray(data) ? data : data?.content ?? []
    spots.value = list

    if (selectedSpot.value?.id) {
      const stillExists = spots.value.some((s) => s.id === selectedSpot.value.id)
      if (!stillExists) {
        await selectSpot(spots.value[0] ?? null)
      }
    } else {
      await selectSpot(spots.value[0] ?? null)
    }

    renderMarkers()
  } catch (e) {
    await loadAllSpots()
  } finally {
    loading.value = false
  }
}

let reloadTimer = null
function scheduleReloadFromMap() {
  if (reloadTimer) clearTimeout(reloadTimer)
  reloadTimer = setTimeout(() => {
    loadSpotsByRadius()
  }, 350)
}

// SZCZEGÓŁY / OPINIE / WYDARZENIA / WŁAŚCICIEL

async function selectSpot(spot) {
  if (!spot) {
    selectedSpot.value = null
    opinions.value = []
    events.value = []
    ownerInfo.value = { is_owner: false }
    myOpinion.value = null
    pendingSelectedId.value = null
    return
  }

  selectedSpot.value = spot
  pendingSelectedId.value = spot.id ?? null

  const { lat, lng } = getSpotLatLng(spot)
  if (lat != null && lng != null && map.value) {
    map.value.setView([lat, lng], Math.max(map.value.getZoom(), 11))
  }

  await loadSpotExtras(spot.id)
}

async function loadSpotDetails(id) {
  try {
    const { data } = await apiClient.get(`/spots/${id}`)
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

    const list = Array.isArray(data) ? data : data?.content ?? []
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
      params: { page: 0, size: 50 },
    })
    events.value = Array.isArray(data) ? data : data?.content ?? []
  } catch (e) {
    events.value = []
  } finally {
    eventsLoading.value = false
  }
}

async function loadSpotOwnerInfo(id) {
  ownerLoading.value = true
  try {
    if (!auth.user) {
      ownerInfo.value = { is_owner: false }
      return
    }

    const { data } = await apiClient.get(`/spots/${id}/owner`)
    ownerInfo.value = data || { is_owner: false }
  } catch (e) {
    const status = e?.response?.status
    if (status === 401 || status === 403) {
      ownerInfo.value = { is_owner: false }
      return
    }
    ownerInfo.value = { is_owner: false }
  } finally {
    ownerLoading.value = false
  }
}

async function loadSpotExtras(id) {
  if (!id) return
  await Promise.all([loadSpotDetails(id), loadSpotOpinions(id), loadSpotEvents(id), loadSpotOwnerInfo(id)])
}

// FAVOURITES

async function loadFavourites() {
  favouritesIds.value = new Set()
  if (!auth.user) return
  favouritesLoading.value = true
  try {
    const { data } = await apiClient.get('/users/me/spots/favourites')
    const list = Array.isArray(data) ? data : data?.content ?? []
    favouritesIds.value = new Set(list.map((s) => s.id).filter(Boolean))
  } catch (e) {
    favouritesIds.value = new Set()
  } finally {
    favouritesLoading.value = false
  }
}

async function addFavourite(spotId) {
  await apiClient.post('/users/me/spots/favourites/add', { spotId })
}

async function removeFavourite(spotId) {
  await apiClient.post('/users/me/spots/favourites/remove', { spotId })
}

async function onToggleFavourite(spotId) {
  if (!spotId) return
  if (!auth.user) return

  const next = new Set(favouritesIds.value)

  try {
    if (next.has(spotId)) {
      next.delete(spotId)
      favouritesIds.value = next
      await removeFavourite(spotId)
    } else {
      next.add(spotId)
      favouritesIds.value = next
      await addFavourite(spotId)
    }
  } catch (e) {
    await loadFavourites()
  }
}

// OCENA + USUNIĘCIE

async function onRateSpot({ spotId, rating, opinionId, comment }) {
  if (!spotId || !rating) return
  try {
    if (opinionId) {
      await apiClient.patch(`/spots/opinions/${opinionId}`, {
        rating,
        comment: comment ?? myOpinion.value?.comment ?? null,
      })
    } else {
      await apiClient.post('/spots/opinions', {
        spotId,
        rating,
        comment: comment ?? null,
      })
    }

    await Promise.all([loadSpotOpinions(spotId), loadSpotDetails(spotId)])

    const { data } = await apiClient.get(`/spots/${spotId}`)
    const idx = spots.value.findIndex((s) => s.id === spotId)
    if (idx !== -1) spots.value[idx] = { ...spots.value[idx], ...data }
  } catch (e) {}
}

async function onDeleteSpot() {
  if (!selectedSpot.value?.id) return
  try {
    await apiClient.delete(`/spots/${selectedSpot.value.id}/delete`)
    await loadSpotsByRadius()
  } catch (e) {}
}

async function onSpotCreated(newSpot) {
  await loadSpotsByRadius()
  const createdId = newSpot?.id
  if (createdId) {
    const found = spots.value.find((s) => s.id === createdId)
    if (found) await selectSpot(found)
  }
}

// MODERACJA

async function toggleModeration() {
  if (!isAdminOrRoot.value) return
  moderationOpen.value = !moderationOpen.value
  if (moderationOpen.value) {
    await loadPendingSpots()
  } else {
    pendingError.value = null
  }
}

async function loadPendingSpots() {
  if (!isAdminOrRoot.value) return
  pendingLoading.value = true
  pendingError.value = null
  try {
    const { data } = await apiClient.get(MODERATION_API.listPending, { params: { page: 0, size: 200 } })
    const list = Array.isArray(data) ? data : data?.content ?? []
    pendingSpots.value = list
  } catch (e) {
    pendingError.value = 'Nie udało się pobrać zgłoszeń łowisk.'
    pendingSpots.value = []
  } finally {
    pendingLoading.value = false
  }
}

async function approveSelectedSpot() {
  if (!isAdminOrRoot.value) return
  const id = selectedSpot.value?.id
  if (!id) return

  moderationBusy.value = true
  try {
    await apiClient.post(MODERATION_API.approve(id))
    pendingSpots.value = (pendingSpots.value || []).filter((s) => s?.id !== id)
    await loadSpotsByRadius()
  } finally {
    moderationBusy.value = false
  }
}

async function rejectSelectedSpot() {
  if (!isAdminOrRoot.value) return
  const id = selectedSpot.value?.id
  if (!id) return

  const reason = window.prompt('Powód odrzucenia (opcjonalnie):', '') ?? ''
  moderationBusy.value = true
  try {
    await apiClient.post(MODERATION_API.reject(id), { reason: reason.trim() || null })
    pendingSpots.value = (pendingSpots.value || []).filter((s) => s?.id !== id)
    await loadSpotsByRadius()
  } finally {
    moderationBusy.value = false
  }
}

// LIFECYCLE

onMounted(async () => {
  initMap()
  await Promise.all([loadSpotsByRadius(), loadFavourites()])
})

onBeforeUnmount(() => {
  if (reloadTimer) clearTimeout(reloadTimer)
  if (map.value) {
    map.value.off('moveend', scheduleReloadFromMap)
    map.value.off('zoomend', scheduleReloadFromMap)
    map.value.remove()
    map.value = null
  }
})

watch(
    () => auth.user,
    async () => {
      await loadFavourites()
      if (selectedSpot.value?.id) {
        await Promise.all([loadSpotOpinions(selectedSpot.value.id), loadSpotOwnerInfo(selectedSpot.value.id)])
      } else {
        ownerInfo.value = { is_owner: false }
        myOpinion.value = null
      }

      if (!isAdminOrRoot.value) {
        moderationOpen.value = false
        pendingSpots.value = []
        pendingSelectedId.value = null
        pendingError.value = null
      }
    },
)

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
          <div v-if="sidePanelsVisible" class="pointer-events-auto flex flex-col md:flex-row gap-0 w-full max-w-[640px]">
            <FishingFiltersPanel
                class="w-full md:w-1/2"
                v-model:filters="filters"
                :show-moderation-button="isAdminOrRoot"
                :moderation-open="moderationOpen"
                :pending-count="pendingSpots.length"
                @toggle-moderation="toggleModeration"
                @hide="toggleSidePanels"
                @apply="onApplyFilters"
            />

            <FishingSearchPanel
                v-if="!moderationOpen"
                class="w-full md:w-1/2"
                :spots="visibleSpots"
                :selected-id="selectedSpotId"
                @select="selectSpot"
            />

            <section
                v-else
                class="bg-black/70 backdrop-blur p-4 flex flex-col gap-3 overflow-y-auto min-h-0 text-white w-full md:w-1/2"
            >
              <header class="flex items-center justify-between">
                <h2 class="font-semibold text-sm uppercase tracking-wide">ZGŁOSZENIA ŁOWISK</h2>
                <button
                    type="button"
                    class="text-xs border border-white/60 rounded px-2 py-0.5 hover:bg-white/10 disabled:opacity-60"
                    @click="loadPendingSpots"
                    :disabled="pendingLoading"
                >
                  Odśwież
                </button>
              </header>

              <div v-if="pendingLoading" class="text-xs opacity-80">Ładowanie...</div>
              <div v-else-if="pendingError" class="text-xs text-red-300">{{ pendingError }}</div>
              <div v-else-if="!pendingSpots.length" class="text-xs opacity-80">Brak oczekujących zgłoszeń.</div>

              <div v-else class="flex-1 overflow-y-auto space-y-2 text-xs">
                <article
                    v-for="p in pendingSpots"
                    :key="p.id"
                    class="border rounded-lg px-3 py-2 cursor-pointer bg-white/10 hover:bg-white/20"
                    :class="p.id === pendingSelectedId ? 'border-white/80' : 'border-white/40'"
                    @click="selectSpot(p)"
                >
                  <header class="flex items-center justify-between">
                    <h3 class="font-semibold text-sm">{{ p.name }}</h3>
                    <span class="text-[10px] uppercase opacity-90">{{ p.type || '—' }}</span>
                  </header>
                  <p class="opacity-80 mt-0.5 text-[10px]">ID: {{ p.id }}</p>
                </article>
              </div>
            </section>
          </div>
        </Transition>

        <Transition name="fade-panels">
          <div v-if="detailsVisible" class="pointer-events-auto ml-auto w-[460px] max-w-full h-full">
            <FishingDetailsPanel
                class="h-full"
                :spot="selectedSpot"
                :opinions="opinions"
                :opinions-loading="opinionsLoading"
                :events="events"
                :events-loading="eventsLoading"
                :owner-info="ownerInfo"
                :user-opinion="myOpinion"
                :is-favourite="isSelectedFavourite"
                :favourites-loading="favouritesLoading"
                :show-moderation="isAdminOrRoot && moderationOpen"
                :is-pending="isSelectedPending"
                :moderation-busy="moderationBusy"
                @toggle-favourite="onToggleFavourite"
                @rate-spot="onRateSpot"
                @delete-spot="onDeleteSpot"
                @spot-created="onSpotCreated"
                @approve-spot="approveSelectedSpot"
                @reject-spot="rejectSelectedSpot"
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
