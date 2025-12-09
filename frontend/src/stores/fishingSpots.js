import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiClient } from '../utils/axios.js'

export const useFishingSpotsStore = defineStore('fishingSpots', () => {
    // Lista wszystkich zaakceptowanych łowisk (GET /api/spots)
    const spots = ref([])
    const spotsLoading = ref(false)
    const spotsError = ref(null)

    // Szczegóły wybranego łowiska (GET /api/spots/{id})
    const selectedSpot = ref(null)
    const selectedSpotLoading = ref(false)
    const selectedSpotError = ref(null)

    // Opinie o łowisku (GET /api/spots/opinions/{spotId})
    const opinions = ref([])
    const opinionsLast = ref(true)
    const opinionsTotalElements = ref(0)
    const opinionsLoading = ref(false)
    const opinionsError = ref(null)

    // Wydarzenia powiązane z łowiskiem (GET /api/spots/{spotId}/events)
    const events = ref([])
    const eventsLast = ref(true)
    const eventsTotalElements = ref(0)
    const eventsLoading = ref(false)
    const eventsError = ref(null)

    // Informacja o właścicielu (GET /api/spots/{spotId}/owner)
    const ownerInfo = ref({ is_owner: false })
    const ownerLoading = ref(false)
    const ownerError = ref(null)

    // Średnia ocena z opinii
    const averageRating = computed(() => {
        if (!opinions.value.length) return null
        const sum = opinions.value.reduce(
            (acc, op) => acc + (typeof op.rating === 'number' ? op.rating : 0),
            0,
        )
        return opinions.value.length ? sum / opinions.value.length : null
    })

    async function loadAllSpots({ page = 0, size = 500 } = {}) {
        spotsLoading.value = true
        spotsError.value = null
        try {
            const { data } = await apiClient.get('/spots', {
                params: { page, size },
            })
            if (Array.isArray(data)) {
                spots.value = data
            } else if (data && Array.isArray(data.content)) {
                spots.value = data.content
            } else {
                spots.value = []
            }
        } catch (e) {
            spotsError.value = 'Nie udało się załadować łowisk.'
            console.error(e)
        } finally {
            spotsLoading.value = false
        }
    }

    async function loadSpotDetails(id) {
        if (!id) return
        selectedSpotLoading.value = true
        selectedSpotError.value = null
        try {
            const { data } = await apiClient.get(`/spots/${id}`)
            selectedSpot.value = data
        } catch (e) {
            selectedSpotError.value = 'Nie udało się załadować szczegółów łowiska.'
            console.error(e)
        } finally {
            selectedSpotLoading.value = false
        }
    }

    function selectFromList(spot) {
        if (!spot) {
            selectedSpot.value = null
            return
        }
        // Ustawiamy minimalne dane, a resztę dociągamy z backendu
        selectedSpot.value = {
            id: spot.id,
            name: spot.name,
            type: spot.type,
            locationX: spot.locationX ?? spot.x,
            locationY: spot.locationY ?? spot.y,
        }
    }

    async function deleteSpot(id) {
        if (!id) return
        try {
            await apiClient.delete(`/spots/${id}/delete`)
            // po usunięciu odświeżamy listę i czyścimy szczegóły
            await loadAllSpots()
            if (selectedSpot.value?.id === id) {
                selectedSpot.value = null
            }
        } catch (e) {
            console.error(e)
            throw e
        }
    }

    async function loadOpinions(spotId, { page = 0, size = 20 } = {}) {
        if (!spotId) return
        opinionsLoading.value = true
        opinionsError.value = null
        try {
            const { data } = await apiClient.get(`/spots/opinions/${spotId}`, {
                params: { page, size },
            })
            if (Array.isArray(data)) {
                opinions.value = data
                opinionsLast.value = true
                opinionsTotalElements.value = data.length
            } else {
                opinions.value = data.content ?? []
                opinionsLast.value = !!data.last
                opinionsTotalElements.value = data.totalElements ?? 0
            }
        } catch (e) {
            opinionsError.value = 'Nie udało się załadować opinii.'
            console.error(e)
        } finally {
            opinionsLoading.value = false
        }
    }

    async function addOpinion({ spotId, rating, comment }) {
        if (!spotId || !rating) return
        try {
            await apiClient.post('/spots/opinions', {
                spotId,
                rating,
                comment,
            })
            await loadOpinions(spotId, { page: 0 })
        } catch (e) {
            console.error(e)
            throw e
        }
    }

    async function editOpinion({ opinionId, rating, comment, spotId }) {
        if (!opinionId || !rating) return
        try {
            await apiClient.patch(`/spots/opinions/${opinionId}`, {
                rating,
                comment,
            })
            if (spotId) {
                await loadOpinions(spotId, { page: 0 })
            }
        } catch (e) {
            console.error(e)
            throw e
        }
    }

    async function deleteOpinion({ opinionId, spotId }) {
        if (!opinionId) return
        try {
            await apiClient.delete(`/spots/opinions/${opinionId}`)
            if (spotId) {
                await loadOpinions(spotId, { page: 0 })
            }
        } catch (e) {
            console.error(e)
            throw e
        }
    }

    async function loadEventsForSpot(spotId, { page = 0, size = 10 } = {}) {
        if (!spotId) return
        eventsLoading.value = true
        eventsError.value = null
        try {
            const { data } = await apiClient.get(`/spots/${spotId}/events`, {
                params: { page, size },
            })
            if (Array.isArray(data)) {
                events.value = data
                eventsLast.value = true
                eventsTotalElements.value = data.length
            } else {
                events.value = data.content ?? []
                eventsLast.value = !!data.last
                eventsTotalElements.value = data.totalElements ?? 0
            }
        } catch (e) {
            eventsError.value = 'Nie udało się załadować wydarzeń.'
            console.error(e)
        } finally {
            eventsLoading.value = false
        }
    }

    async function loadOwnerInfo(spotId) {
        if (!spotId) return
        ownerLoading.value = true
        ownerError.value = null
        try {
            const { data } = await apiClient.get(`/spots/${spotId}/owner`)
            // backend zwraca mapę z kluczem "is_owner"
            ownerInfo.value = data || { is_owner: false }
        } catch (e) {
            ownerError.value = 'Nie udało się sprawdzić właściciela.'
            console.error(e)
        } finally {
            ownerLoading.value = false
        }
    }

    function reset() {
        spots.value = []
        spotsError.value = null
        selectedSpot.value = null
        selectedSpotError.value = null
        opinions.value = []
        opinionsError.value = null
        events.value = []
        eventsError.value = null
        ownerInfo.value = { is_owner: false }
        ownerError.value = null
    }

    return {
        spots,
        spotsLoading,
        spotsError,

        selectedSpot,
        selectedSpotLoading,
        selectedSpotError,

        opinions,
        opinionsLast,
        opinionsTotalElements,
        opinionsLoading,
        opinionsError,
        averageRating,

        events,
        eventsLast,
        eventsTotalElements,
        eventsLoading,
        eventsError,

        ownerInfo,
        ownerLoading,
        ownerError,

        loadAllSpots,
        loadSpotDetails,
        selectFromList,
        deleteSpot,

        loadOpinions,
        addOpinion,
        editOpinion,
        deleteOpinion,

        loadEventsForSpot,
        loadOwnerInfo,

        reset,
    }
})
