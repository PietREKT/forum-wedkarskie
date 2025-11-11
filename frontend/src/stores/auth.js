import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiClient } from '../utils/axios.js'

const LS_KEY = 'fw_user'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(
        typeof localStorage !== 'undefined'
            ? JSON.parse(localStorage.getItem(LS_KEY) || 'null')
            : null
    )
    const status = ref('idle')
    const error = ref(null)

    const isAuthenticated = computed(() => !!user.value)

    function _setUser(u) {
        user.value = u
        if (typeof localStorage !== 'undefined') {
            if (u) localStorage.setItem(LS_KEY, JSON.stringify(u))
            else localStorage.removeItem(LS_KEY)
        }
    }

    async function bootstrapSession() {
        return user.value
    }

    // LOGIN
    async function login(username, password) {
        status.value = 'loading'
        error.value = null
        try {
            const { data } = await apiClient.post('/auth/login', { username, password })
            const u = data && data.user ? data.user : data
            _setUser(u)
            status.value = 'idle'
            return u
        } catch (err) {
            status.value = 'error'
            error.value = err?.response?.data?.message || err?.message || 'Nie udało się zalogować.'
            _setUser(null)
            throw err
        }
    }

    // REGISTER
    async function register(payload) {
        status.value = 'loading'
        error.value = null
        try {
            const { data } = await apiClient.post('/auth/register', payload)
            const u = data && data.user ? data.user : data
            status.value = 'idle'
            return u
        } catch (err) {
            status.value = 'error'
            error.value = err?.response?.data?.message || err?.message || 'Nie udało się zarejestrować.'
            throw err
        }
    }

    // LOGOUT
    async function logout() {
        try {
            await apiClient.post('/auth/logout')
        } finally {
            _setUser(null)
            status.value = 'idle'
            error.value = null
        }
    }

    return {
        user,
        status,
        error,
        isAuthenticated,
        bootstrapSession,
        login,
        register,
        logout,
    }
})
