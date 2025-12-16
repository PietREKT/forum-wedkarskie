// src/stores/userStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiClient } from '../utils/axios.js'

export const useUserStore = defineStore('userStore', () => {
    const me = ref(null)
    const status = ref('idle') // idle | loading | error
    const error = ref(null)

    const viewedUser = ref(null)
    const viewedStatus = ref('idle') // idle | loading | error
    const viewedError = ref(null)

    const searchStatus = ref('idle') // idle | loading | error
    const searchError = ref(null)

    const followStatus = ref('idle') // idle | loading | error
    const followError = ref(null)

    const friends = computed(() =>
        me.value && Array.isArray(me.value.friends) ? me.value.friends : [],
    )

    async function fetchMe(force = false) {
        if (!force && me.value && status.value === 'idle') return me.value

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
        status.value = 'idle'
        error.value = null
    }

    async function searchUsers(query) {
        const q = String(query || '').trim()
        if (!q) return []

        searchStatus.value = 'loading'
        searchError.value = null

        try {
            const { data } = await apiClient.get('/users/search', {
                params: { q },
            })

            const list = Array.isArray(data)
                ? data
                : Array.isArray(data?.content)
                    ? data.content
                    : []

            searchStatus.value = 'idle'
            return list
        } catch (err) {
            searchStatus.value = 'error'
            searchError.value =
                err?.response?.data?.message ||
                err?.message ||
                'Nie udało się wyszukać użytkowników.'
            throw err
        }
    }

    async function fetchUserByUsername(username) {
        const u = String(username || '').trim()
        if (!u) {
            viewedUser.value = null
            viewedStatus.value = 'idle'
            viewedError.value = null
            return null
        }

        viewedStatus.value = 'loading'
        viewedError.value = null

        try {
            const list = await searchUsers(u)
            const found =
                list.find(
                    x => String(x?.username || '').toLowerCase() === u.toLowerCase(),
                ) || null

            if (!found?.id) {
                viewedUser.value = null
                viewedStatus.value = 'error'
                viewedError.value = 'Nie znaleziono użytkownika.'
                return null
            }

            return await fetchUserById(found.id)
        } catch (err) {
            viewedStatus.value = 'error'
            viewedError.value =
                err?.response?.data?.message ||
                err?.message ||
                'Nie udało się pobrać profilu użytkownika.'
            throw err
        }
    }

    async function fetchUserById(userId) {
        const id = userId
        if (id == null || id === '') {
            viewedUser.value = null
            viewedStatus.value = 'idle'
            viewedError.value = null
            return null
        }

        viewedStatus.value = 'loading'
        viewedError.value = null

        try {
            const { data } = await apiClient.get(`/users/${encodeURIComponent(id)}`)
            viewedUser.value = data
            viewedStatus.value = 'idle'
            return data
        } catch (err) {
            viewedStatus.value = 'error'
            viewedError.value =
                err?.response?.data?.message ||
                err?.message ||
                'Nie udało się pobrać profilu użytkownika.'
            throw err
        }
    }

    function setViewedToMe() {
        viewedUser.value = me.value
        viewedStatus.value = 'idle'
        viewedError.value = null
    }

    // „obserwuj” = POST /users/friends/invite { id: userId }
    async function followUserById(userId) {
        const id = userId
        if (!id) return

        followStatus.value = 'loading'
        followError.value = null

        try {
            // WAŻNE: backend odrzuca application/x-www-form-urlencoded (415),
            // więc wymuszamy JSON.
            await apiClient.post(
                '/users/friends/invite',
                { id },
                { headers: { 'Content-Type': 'application/json' } },
            )

            followStatus.value = 'idle'
            await fetchMe(true)
        } catch (err) {
            followStatus.value = 'error'
            followError.value =
                err?.response?.data?.message ||
                err?.message ||
                'Nie udało się wysłać obserwacji.'
            throw err
        }
    }

    async function followUserByUsername(username) {
        const u = String(username || '').trim()
        if (!u) return

        const list = await searchUsers(u)
        const found =
            list.find(x => String(x?.username || '').toLowerCase() === u.toLowerCase()) ||
            null

        if (!found?.id) {
            followStatus.value = 'error'
            followError.value = 'Nie znaleziono takiego użytkownika.'
            return
        }

        await followUserById(found.id)
    }

    return {
        me,
        status,
        error,
        friends,
        fetchMe,
        clearMe,

        viewedUser,
        viewedStatus,
        viewedError,
        setViewedToMe,
        fetchUserByUsername,
        fetchUserById,

        searchUsers,
        searchStatus,
        searchError,

        followUserById,
        followUserByUsername,
        followStatus,
        followError,
    }
})
