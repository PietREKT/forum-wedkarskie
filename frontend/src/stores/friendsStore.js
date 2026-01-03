// src/stores/friendsStore.js
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import {
    acceptFriendRequest,
    cancelFriendRequest,
    rejectFriendRequest,
} from '../utils/usersApi.js'
import { useUserStore } from './userStore.js'

const LS_KEY = 'fw_friend_outgoing_pending_by_username'

function readPendingMap() {
    try {
        const raw = localStorage.getItem(LS_KEY)
        const obj = raw ? JSON.parse(raw) : {}
        return obj && typeof obj === 'object' ? obj : {}
    } catch {
        return {}
    }
}
function writePendingMap(map) {
    try {
        localStorage.setItem(LS_KEY, JSON.stringify(map || {}))
    } catch {
        // ignore
    }
}
function setPendingUsername(username, value) {
    const u = String(username || '').trim().toLowerCase()
    if (!u) return
    const map = readPendingMap()
    if (value) map[u] = Date.now()
    else delete map[u]
    writePendingMap(map)
}
function isPendingUsername(username) {
    const u = String(username || '').trim().toLowerCase()
    if (!u) return false
    const map = readPendingMap()
    return !!map[u]
}

function normalizeReqId(item) {
    return (
        item?.reqId ??
        item?.requestId ??
        item?.id ??
        item?.request_id ??
        item?.friendRequestId ??
        item?.friend_request_id ??
        null
    )
}

function normalizeUser(item) {
    return item?.sender || item?.receiver || item?.user || item?.from || item?.to || null
}

export const useFriendsStore = defineStore('friends', () => {
    const incoming = ref([])
    const outgoing = ref([])

    const loading = ref(false)
    const error = ref(null)

    const incomingIds = computed(() => new Set(incoming.value.map(normalizeReqId).filter(Boolean)))
    const outgoingIds = computed(() => new Set(outgoing.value.map(normalizeReqId).filter(Boolean)))

    function isOutgoingPendingByUsername(username) {
        return isPendingUsername(username)
    }

    async function refresh() {
        const userStore = useUserStore()
        loading.value = true
        error.value = null
        try {
            await userStore.fetchMe(true)
        } catch {
            // ignore
        } finally {
            loading.value = false
        }
    }

    async function accept(reqId) {
        if (!reqId) throw new Error('Brak reqId')
        loading.value = true
        error.value = null
        try {
            await acceptFriendRequest(reqId)
            await refresh()
        } catch (err) {
            error.value = err?.response?.data?.message || err?.message || 'Nie udało się zaakceptować zaproszenia.'
            throw err
        } finally {
            loading.value = false
        }
    }

    async function reject(reqId) {
        if (!reqId) throw new Error('Brak reqId')
        loading.value = true
        error.value = null
        try {
            await rejectFriendRequest(reqId)
            await refresh()
        } catch (err) {
            error.value = err?.response?.data?.message || err?.message || 'Nie udało się odrzucić zaproszenia.'
            throw err
        } finally {
            loading.value = false
        }
    }

    async function cancel(reqId, usernameForFallback) {
        if (!reqId) throw new Error('Brak reqId')
        loading.value = true
        error.value = null
        try {
            await cancelFriendRequest(reqId)
            if (usernameForFallback) setPendingUsername(usernameForFallback, false)
            await refresh()
        } catch (err) {
            error.value = err?.response?.data?.message || err?.message || 'Nie udało się anulować zaproszenia.'
            throw err
        } finally {
            loading.value = false
        }
    }

    function markOutgoingPending(username) {
        setPendingUsername(username, true)
    }

    function clearOutgoingPending(username) {
        setPendingUsername(username, false)
    }

    return {
        incoming,
        outgoing,
        incomingIds,
        outgoingIds,
        loading,
        error,

        refresh,
        accept,
        reject,
        cancel,

        isOutgoingPendingByUsername,
        markOutgoingPending,
        clearOutgoingPending,

        normalizeReqId,
        normalizeUser,
    }
})
