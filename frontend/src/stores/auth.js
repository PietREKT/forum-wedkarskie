import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiClient } from '../utils/axios.js'

const LS_KEY = 'fw_user'

export const useAuthStore = defineStore('auth', () => {

    // obiekt zalogowanego użytkownika
    const user = ref(
        typeof localStorage !== 'undefined'
            ? JSON.parse(localStorage.getItem(LS_KEY) || 'null')
            : null
    )
    const status = ref('idle')
    const error = ref(null)

    //czy zalogowany
    const isAuthenticated = computed(() => !!user.value)

    function _setUser(u) {
        user.value = u
        if (typeof localStorage !== 'undefined') {
            if (u) localStorage.setItem(LS_KEY, JSON.stringify(u)) // zapisujemy zalogowanego
            else localStorage.removeItem(LS_KEY)                   // czyścimy przy wylogowaniu
        }
    }

    async function bootstrapSession() {
        return user.value
    }

    //login
    async function login(username, password) {
        status.value = 'loading'
        error.value = null
        try {
            const { data } = await apiClient.post('/api/auth/login', { username, password })
            const u = data && data.user ? data.user : data
            _setUser(u)
            status.value = 'idle'
            return u
        } catch (err) {
            status.value = 'error'
            error.value = (err && err.message) ? err.message : 'Login failed'
            _setUser(null)
            throw err
        }
    }

    //register
    async function register(payload) {
        status.value = 'loading'
        error.value = null
        try {
            const { data } = await apiClient.post('/api/auth/register', payload)
            const u = data && data.user ? data.user : data
            _setUser(u)
            status.value = 'idle'
            return u
        } catch (err) {
            status.value = 'error'
            error.value = (err && err.message) ? err.message : 'Registration failed'
            throw err
        }
    }

    // logout
    async function logout() {
        try {
            await apiClient.post('/api/auth/logout')
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
