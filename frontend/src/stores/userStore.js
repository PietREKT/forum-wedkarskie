import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiClient } from '../utils/axios.js'

export const useUserStore = defineStore('userStore', () => {
    const me = ref(null)
    const status = ref('idle')
    const error = ref(null)

    // lista zaakceptowanych obserwowanych
    const friends = computed(() =>
        me.value && Array.isArray(me.value.friends) ? me.value.friends : [],
    )

    async function fetchMe(force = false) {
        if (!force && me.value && status.value === 'idle') {
            return me.value
        }

        status.value = 'loading'
        error.value = null

        try {
            const { data } = await apiClient.get('/users/me')
            me.value = data
            status.value = 'idle'
            return data
        } catch (err) {
            status.value = 'error'
            error.value =
                err?.response?.data?.message ||
                err?.message ||
                'Nie udało się pobrać danych użytkownika.'
            throw err
        }
    }

    function clearMe() {
        me.value = null
    }

    return {
        me,
        status,
        error,
        friends,
        fetchMe,
        clearMe,
    }
})
