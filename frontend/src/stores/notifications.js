import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiClient } from '../utils/axios.js'

export const useNotificationsStore = defineStore('notifications', () => {
    const items = ref([])
    const loading = ref(false)
    const error = ref(null)

    function isRead(n) {
        return !!(n?.read || n?.isRead || n?.seen || n?.status === 'READ')
    }

    function getId(n) {
        return n?.notificationId ?? n?.id ?? null
    }

    const unreadCount = computed(() => items.value.filter(n => !isRead(n)).length)

    async function fetchMy() {
        loading.value = true
        error.value = null
        try {
            const res = await apiClient.get('/users/me/notifications')
            items.value = Array.isArray(res.data) ? res.data : []
        } catch (e) {
            console.error('notifications fetch error', e)
            error.value = 'Nie udało się pobrać powiadomień.'
            items.value = []
        } finally {
            loading.value = false
        }
    }

    async function markRead(id) {
        if (!id) return
        await apiClient.patch(`/users/me/notifications/${id}`)
        const idx = items.value.findIndex(n => getId(n) === id)
        if (idx !== -1) {
            items.value[idx] = { ...items.value[idx], read: true, isRead: true, seen: true, status: 'READ' }
        }
    }

    async function remove(id) {
        if (!id) return
        await apiClient.delete(`/users/me/notifications/${id}`)
        items.value = items.value.filter(n => getId(n) !== id)
    }

    async function markAllRead() {
        const unread = items.value.filter(n => !isRead(n))
        if (unread.length === 0) return

        for (const n of unread) {
            const id = getId(n)
            if (!id) continue
            try {
                await apiClient.patch(`/users/me/notifications/${id}`)
            } catch (e) {
                console.error('markAllRead item error', id, e)
            }
        }

        items.value = items.value.map(n =>
            isRead(n) ? n : { ...n, read: true, isRead: true, seen: true, status: 'READ' },
        )
    }

    return {
        items,
        loading,
        error,
        unreadCount,
        getId,
        isRead,
        fetchMy,
        markRead,
        remove,
        markAllRead,
    }
})
