import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios.js'

export const useFishingSpotsStore = defineStore('fishingSpots', () => {
    const spots = ref([])          // lista łowisk z /spots/radius
    const loading = ref(false)
    const error = ref(null)

    const selectedSpot = ref(null) // wybrane łowisko (z listy albo /spots/{id})

    async function loadSpotsInRadius ({ x, y, radiusKm = 20, page = 0, size = 100 } = {}) {
        if (loading.value) return
        loading.value = true
        error.value = null

        try {
            const { data } = await apiClient.get('/spots/radius', {
                params: { x, y, radiusKm, page, size },
            })

            const list =
                Array.isArray(data) ? data
                    : Array.isArray(data.content) ? data.content
                        : []

            spots.value = list

            if (!selectedSpot.value && list.length > 0) {
                selectedSpot.value = list[0]
            }
        } catch (e) {
            error.value = e
        } finally {
            loading.value = false
        }
    }

    async function loadSpotDetails (id) {
        if (!id) return
        loading.value = true
        error.value = null

        try {
            const { data } = await apiClient.get(`/spots/${id}`)
            selectedSpot.value = data
        } catch (e) {
            error.value = e
        } finally {
            loading.value = false
        }
    }

    function selectFromList (spot) {
        selectedSpot.value = spot
    }

    function reset () {
        spots.value = []
        selectedSpot.value = null
        error.value = null
        loading.value = false
    }

    return {
        spots,
        loading,
        error,
        selectedSpot,
        loadSpotsInRadius,
        loadSpotDetails,
        selectFromList,
        reset,
    }
})
