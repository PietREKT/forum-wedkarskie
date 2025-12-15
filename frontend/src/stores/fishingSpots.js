import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios.js'

export const useFishingSpotsStore = defineStore('fishingSpots', () => {
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

    const favouritesIds = ref(new Set())
    const favouritesLoading = ref(false)

    const moderationOpen = ref(false)
    const pendingSpots = ref([])
    const pendingLoading = ref(false)
    const pendingError = ref(null)
    const moderationBusy = ref(false)

    const MODERATION_API = {
        list: '/admin/spots/unverified',
        accept: (id) => `/admin/spots/${id}/accept`,
        reject: (id) => `/admin/spots/${id}/reject`,
    }

    function normalize(data) {
        return Array.isArray(data) ? data : data?.content ?? []
    }

    function clearSelection() {
        selectedSpot.value = null
        opinions.value = []
        events.value = []
        ownerInfo.value = { is_owner: false }
    }

    function closeModeration() {
        moderationOpen.value = false
        pendingSpots.value = []
        pendingError.value = null
    }

    async function loadAll() {
        loading.value = true
        error.value = null
        try {
            const { data } = await apiClient.get('/spots', { params: { page: 0, size: 500 } })
            spots.value = normalize(data)
        } catch {
            error.value = 'Nie udało się pobrać listy łowisk.'
        } finally {
            loading.value = false
        }
    }

    async function loadByRadius({ x, y, radiusKm }) {
        loading.value = true
        error.value = null
        try {
            const { data } = await apiClient.get('/spots/radius', {
                params: { x, y, radiusKm, page: 0, size: 500 },
            })
            spots.value = normalize(data)
        } catch {
            await loadAll()
        } finally {
            loading.value = false
        }
    }

    async function selectSpot(spot, { isLoggedIn = false } = {}) {
        if (!spot) {
            clearSelection()
            return
        }

        selectedSpot.value = spot

        const tasks = [loadDetails(spot.id), loadOpinions(spot.id), loadEvents(spot.id)]

        if (isLoggedIn) tasks.push(loadOwnerInfo(spot.id))
        else ownerInfo.value = { is_owner: false }

        await Promise.all(tasks)
    }

    async function loadDetails(id) {
        try {
            const { data } = await apiClient.get(`/spots/${id}`)
            if (selectedSpot.value?.id === id) selectedSpot.value = data
        } catch {}
    }

    async function loadOpinions(id) {
        opinionsLoading.value = true
        try {
            const { data } = await apiClient.get(`/spots/opinions/${id}`, { params: { page: 0, size: 50 } })
            opinions.value = normalize(data)
        } catch {
            opinions.value = []
        } finally {
            opinionsLoading.value = false
        }
    }

    async function loadEvents(id) {
        eventsLoading.value = true
        try {
            const { data } = await apiClient.get(`/spots/${id}/events`, { params: { page: 0, size: 50 } })
            events.value = normalize(data)
        } catch {
            events.value = []
        } finally {
            eventsLoading.value = false
        }
    }

    async function loadOwnerInfo(id) {
        ownerLoading.value = true
        try {
            const { data } = await apiClient.get(`/spots/${id}/owner`)
            ownerInfo.value = data || { is_owner: false }
        } catch {
            ownerInfo.value = { is_owner: false }
        } finally {
            ownerLoading.value = false
        }
    }

    async function loadFavourites(isLoggedIn) {
        favouritesIds.value = new Set()
        if (!isLoggedIn) return

        favouritesLoading.value = true
        try {
            // backend zwraca Page => { content: [...] }
            const { data } = await apiClient.get('/users/me/spots/favourites', { params: { page: 0, size: 500 } })
            const list = normalize(data)
            favouritesIds.value = new Set(list.map((s) => s?.id).filter(Boolean))
        } catch {
            favouritesIds.value = new Set()
        } finally {
            favouritesLoading.value = false
        }
    }

    // WAŻNE: backend oczekuje DTO z polem "id", nie "spotId"
    async function addFavourite(id) {
        await apiClient.post('/users/me/spots/favourites/add', { id })
    }

    async function removeFavourite(id) {
        await apiClient.post('/users/me/spots/favourites/remove', { id })
    }

    async function toggleFavourite(id, isLoggedIn) {
        if (!id) return
        if (!isLoggedIn) throw new Error('Zaloguj się, aby dodać do ulubionych.')

        const next = new Set(favouritesIds.value)

        try {
            if (next.has(id)) {
                next.delete(id)
                favouritesIds.value = next
                await removeFavourite(id)
            } else {
                next.add(id)
                favouritesIds.value = next
                await addFavourite(id)
            }
        } catch {
            await loadFavourites(true)
            throw new Error('Nie udało się zmienić ulubionych.')
        }
    }

    async function rateSpot({ spotId, rating, opinionId, comment }, isLoggedIn) {
        if (!isLoggedIn) throw new Error('Zaloguj się, aby dodać ocenę.')
        if (!spotId || !rating) return

        try {
            if (opinionId) {
                await apiClient.patch(`/spots/opinions/${opinionId}`, { rating, comment: comment ?? null })
            } else {
                await apiClient.post('/spots/opinions', { spotId, rating, comment: comment ?? null })
            }

            await Promise.all([loadOpinions(spotId), loadDetails(spotId)])

            try {
                const { data } = await apiClient.get(`/spots/${spotId}`)
                const idx = spots.value.findIndex((s) => s?.id === spotId)
                if (idx !== -1) spots.value[idx] = { ...spots.value[idx], ...data }
            } catch {}
        } catch (err) {
            const status = err?.response?.status
            if (status === 409) {
                throw new Error('Już oceniłeś to łowisko. Edytuj swoją opinię.')
            }
            throw new Error('Nie udało się zapisać oceny.')
        }
    }

    async function deleteSpot(id) {
        if (!id) return
        try {
            await apiClient.delete(`/spots/${id}/delete`)
        } catch (err) {
            const status = err?.response?.status
            if (status === 409) {
                throw new Error('Nie można usunąć łowiska, ponieważ istnieją powiązane wydarzenia.')
            }
            throw new Error('Nie udało się usunąć łowiska.')
        }
    }

    async function toggleModeration(isAdmin) {
        if (!isAdmin) return
        moderationOpen.value = !moderationOpen.value
        if (moderationOpen.value) await loadPending(isAdmin)
        else pendingError.value = null
    }

    async function loadPending(isAdmin) {
        if (!isAdmin) return
        pendingLoading.value = true
        pendingError.value = null
        try {
            const { data } = await apiClient.get(MODERATION_API.list, { params: { page: 0, size: 200 } })
            pendingSpots.value = normalize(data)
        } catch {
            pendingSpots.value = []
            pendingError.value = 'Nie udało się pobrać zgłoszeń łowisk.'
        } finally {
            pendingLoading.value = false
        }
    }

    async function acceptSpot(id, isAdmin) {
        if (!isAdmin || !id) return
        moderationBusy.value = true
        try {
            await apiClient.post(MODERATION_API.accept(id))
            pendingSpots.value = pendingSpots.value.filter((s) => s?.id !== id)
        } catch {
            throw new Error('Nie udało się zaakceptować łowiska.')
        } finally {
            moderationBusy.value = false
        }
    }

    async function rejectSpot(id, isAdmin) {
        if (!isAdmin || !id) return
        moderationBusy.value = true
        try {
            await apiClient.post(MODERATION_API.reject(id))
            pendingSpots.value = pendingSpots.value.filter((s) => s?.id !== id)
        } catch {
            throw new Error('Nie udało się odrzucić łowiska.')
        } finally {
            moderationBusy.value = false
        }
    }

    return {
        spots,
        loading,
        error,
        selectedSpot,

        opinions,
        opinionsLoading,

        events,
        eventsLoading,

        ownerInfo,
        ownerLoading,

        favouritesIds,
        favouritesLoading,

        moderationOpen,
        pendingSpots,
        pendingLoading,
        pendingError,
        moderationBusy,

        clearSelection,
        closeModeration,

        loadAll,
        loadByRadius,
        selectSpot,

        loadFavourites,
        toggleFavourite,

        rateSpot,
        deleteSpot,

        toggleModeration,
        loadPending,
        acceptSpot,
        rejectSpot,
    }
})
