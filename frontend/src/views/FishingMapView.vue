<script setup>
import { onMounted, onBeforeUnmount, computed, ref, watch } from 'vue'
import FishingFiltersPanel from '../components/map/FishingFiltersPanel.vue'
import FishingSearchPanel from '../components/map/FishingSearchPanel.vue'
import FishingDetailsPanel from '../components/map/FishingDetailsPanel.vue'
import { useAuthStore } from '../stores/auth'
import { useFishingSpotsStore } from '../stores/fishingSpots'
import { useFishingMapStore } from '../stores/fishingMap'

const auth = useAuthStore()
const spotsStore = useFishingSpotsStore()
const mapStore = useFishingMapStore()

const isAdmin = computed(() => auth.isAdmin)
const isLoggedIn = computed(() => !!auth.user)

const sidePanelsVisible = ref(true)
const detailsVisible = ref(true)

const uiMessage = ref(null) // { type, text }
let uiTimer = null
function notify(type, text) {
  uiMessage.value = { type, text }
  if (uiTimer) clearTimeout(uiTimer)
  uiTimer = setTimeout(() => {
    uiMessage.value = null
    uiTimer = null
  }, 3500)
}

const filters = ref({ spotType: 'ALL' })

const visibleSpots = computed(() =>
    (spotsStore.spots || []).filter((s) => {
      if (filters.value.spotType === 'PUBLIC' && s.type !== 'PUBLIC') return false
      if (filters.value.spotType === 'PRIVATE' && s.type !== 'PRIVATE') return false
      return true
    }),
)

const selectedSpotId = computed(() => spotsStore.selectedSpot?.id ?? null)

const isSelectedFavourite = computed(() => {
  const id = selectedSpotId.value
  if (!id) return false
  return spotsStore.favouritesIds.has(id)
})

const isSelectedPending = computed(() => {
  const id = selectedSpotId.value
  if (!id) return false
  return (spotsStore.pendingSpots || []).some((p) => p?.id === id)
})

async function reloadByMap() {
  const center = mapStore.getCenter()
  const radiusKm = mapStore.getRadiusKm()
  if (!center || !radiusKm) {
    await spotsStore.loadAll()
    return
  }
  await spotsStore.loadByRadius({ ...center, radiusKm })
}

async function onSelectSpot(spot) {
  await spotsStore.selectSpot(spot, { isLoggedIn: isLoggedIn.value })
  const { lat, lng } = mapStore.getLatLng(spot)
  mapStore.setViewIfCoords(lat, lng, 11)
}

async function onToggleFavourite(spotId) {
  try {
    await spotsStore.toggleFavourite(spotId, isLoggedIn.value)
    notify('success', isSelectedFavourite.value ? 'Dodano do ulubionych.' : 'Usunięto z ulubionych.')
  } catch (e) {
    notify('error', e?.message || 'Nie udało się zmienić ulubionych.')
  }
}

async function onRateSpot(payload) {
  try {
    await spotsStore.rateSpot(payload, isLoggedIn.value)
    notify('success', 'Zapisano opinię.')
  } catch (e) {
    notify('error', e?.message || 'Nie udało się zapisać opinii.')
  }
}

async function onDeleteSpot() {
  const id = spotsStore.selectedSpot?.id
  if (!id) return
  try {
    await spotsStore.deleteSpot(id)
    notify('success', 'Łowisko zostało usunięte.')
    await reloadByMap()
    await spotsStore.selectSpot(spotsStore.spots[0] ?? null, { isLoggedIn: isLoggedIn.value })
  } catch (e) {
    notify('error', e?.message || 'Nie udało się usunąć łowiska.')
  }
}

async function onApproveSpot() {
  const id = spotsStore.selectedSpot?.id
  if (!id) return
  try {
    await spotsStore.acceptSpot(id, isAdmin.value)
    notify('success', 'Łowisko zaakceptowane.')
    await reloadByMap()
  } catch (e) {
    notify('error', e?.message || 'Nie udało się zaakceptować.')
  }
}

async function onRejectSpot() {
  const id = spotsStore.selectedSpot?.id
  if (!id) return
  try {
    await spotsStore.rejectSpot(id, isAdmin.value)
    notify('success', 'Łowisko odrzucone.')
    await reloadByMap()
  } catch (e) {
    notify('error', e?.message || 'Nie udało się odrzucić.')
  }
}

function toggleSidePanels() {
  sidePanelsVisible.value = !sidePanelsVisible.value
}
function toggleDetails() {
  detailsVisible.value = !detailsVisible.value
}

let offViewport = null

onMounted(async () => {
  mapStore.init('fishing-map')
  offViewport = mapStore.onViewportChanged(reloadByMap, 350)

  await reloadByMap()

  // bez 401 gdy niezalogowany
  await spotsStore.loadFavourites(isLoggedIn.value)

  if (spotsStore.spots.length) {
    await onSelectSpot(spotsStore.spots[0])
  }
})

onBeforeUnmount(() => {
  try { offViewport?.() } catch {}
  if (uiTimer) clearTimeout(uiTimer)
  mapStore.destroy()
})

watch(visibleSpots, (list) => {
  mapStore.renderMarkers(list, onSelectSpot)
})

watch(
    () => auth.user,
    async () => {
      await spotsStore.loadFavourites(isLoggedIn.value)

      if (!isAdmin.value) {
        spotsStore.moderationOpen = false
        spotsStore.pendingSpots = []
        spotsStore.pendingError = null
      }

      if (spotsStore.selectedSpot?.id) {
        await spotsStore.selectSpot(spotsStore.selectedSpot, { isLoggedIn: isLoggedIn.value })
      } else {
        spotsStore.ownerInfo = { is_owner: false }
      }
    },
)
</script>

<template>
  <div class="fixed inset-x-0 bottom-0 top-[56px]">
    <div class="relative w-full h-full">
      <div id="fishing-map" class="absolute inset-0 z-0"></div>

      <div
          v-if="uiMessage"
          class="absolute left-4 top-4 z-30 px-3 py-2 rounded-lg text-xs border backdrop-blur"
          :class="uiMessage.type === 'success'
          ? 'bg-emerald-600/20 border-emerald-300/60 text-white'
          : uiMessage.type === 'info'
            ? 'bg-sky-600/20 border-sky-300/60 text-white'
            : 'bg-red-700/20 border-red-300/60 text-white'"
      >
        {{ uiMessage.text }}
      </div>

      <div
          v-if="spotsStore.loading"
          class="absolute left-4 bottom-4 z-20 px-3 py-1 rounded-full text-xs bg-black/80 text-white border border-white/60 backdrop-blur"
      >
        Ładowanie łowisk...
      </div>

      <div
          v-else-if="spotsStore.error"
          class="absolute left-4 bottom-4 z-20 px-3 py-1 rounded-full text-xs bg-red-700/80 text-white border border-white/60 backdrop-blur"
      >
        {{ spotsStore.error }}
      </div>

      <button
          v-if="!sidePanelsVisible"
          @click="toggleSidePanels"
          class="absolute left-4 top-4 z-20 px-3 py-1 rounded-full text-xs bg-black/80 text-white border border-white/60 backdrop-blur"
      >
        Pokaż filtry i wyszukiwarkę
      </button>

      <button
          v-if="!detailsVisible"
          @click="toggleDetails"
          class="absolute right-4 top-4 z-20 px-3 py-1 rounded-full text-xs bg-black/80 text-white border border-white/60 backdrop-blur"
      >
        Pokaż szczegóły łowiska
      </button>

      <div class="absolute inset-0 z-10 flex pointer-events-none">
        <div v-if="sidePanelsVisible" class="pointer-events-auto flex flex-col md:flex-row gap-0 w-full max-w-[640px]">
          <FishingFiltersPanel
              class="w-full md:w-1/2"
              v-model:filters="filters"
              :show-moderation-button="isAdmin"
              :moderation-open="spotsStore.moderationOpen"
              :pending-count="spotsStore.pendingSpots.length"
              @toggle-moderation="spotsStore.toggleModeration(isAdmin)"
              @hide="toggleSidePanels"
          />

          <FishingSearchPanel
              v-if="!spotsStore.moderationOpen"
              class="w-full md:w-1/2"
              :spots="visibleSpots"
              :selected-id="selectedSpotId"
              @select="onSelectSpot"
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
                  @click="spotsStore.loadPending(isAdmin)"
                  :disabled="spotsStore.pendingLoading"
              >
                Odśwież
              </button>
            </header>

            <div v-if="spotsStore.pendingLoading" class="text-xs opacity-80">Ładowanie...</div>
            <div v-else-if="spotsStore.pendingError" class="text-xs text-red-300">{{ spotsStore.pendingError }}</div>
            <div v-else-if="!spotsStore.pendingSpots.length" class="text-xs opacity-80">Brak oczekujących zgłoszeń.</div>

            <div v-else class="flex-1 overflow-y-auto space-y-2 text-xs">
              <article
                  v-for="p in spotsStore.pendingSpots"
                  :key="p.id"
                  class="border rounded-lg px-3 py-2 cursor-pointer bg-white/10 hover:bg-white/20"
                  :class="p.id === selectedSpotId ? 'border-white/80' : 'border-white/40'"
                  @click="onSelectSpot(p)"
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

        <div v-if="detailsVisible" class="pointer-events-auto ml-auto w-[460px] max-w-full h-full">
          <FishingDetailsPanel
              class="h-full"
              :spot="spotsStore.selectedSpot"
              :opinions="spotsStore.opinions"
              :opinions-loading="spotsStore.opinionsLoading"
              :events="spotsStore.events"
              :events-loading="spotsStore.eventsLoading"
              :owner-info="spotsStore.ownerInfo"
              :is-favourite="isSelectedFavourite"
              :favourites-loading="spotsStore.favouritesLoading"
              :show-moderation="isAdmin && spotsStore.moderationOpen"
              :is-pending="isSelectedPending"
              :moderation-busy="spotsStore.moderationBusy"
              :is-logged-in="isLoggedIn"
              @toggle-favourite="onToggleFavourite"
              @rate-spot="onRateSpot"
              @delete-spot="onDeleteSpot"
              @approve-spot="onApproveSpot"
              @reject-spot="onRejectSpot"
          />
        </div>
      </div>
    </div>
  </div>
</template>
