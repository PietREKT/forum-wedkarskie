import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios.js'

export const useInvitesStore = defineStore('invites', () => {
    const groupInvites = ref([])
    const eventInvites = ref([])

    const loading = ref(false)
    const error = ref(null)

    async function fetchAll() {
        loading.value = true
        error.value = null
        try {
            const [g, e] = await Promise.all([
                apiClient.get('/users/me/groups/invites'),
                apiClient.get('/users/me/events/invites'),
            ])
            groupInvites.value = Array.isArray(g.data) ? g.data : []
            eventInvites.value = Array.isArray(e.data) ? e.data : []
        } catch (err) {
            console.error('invites fetchAll error', err)
            error.value = 'Nie udało się pobrać zaproszeń.'
            groupInvites.value = []
            eventInvites.value = []
        } finally {
            loading.value = false
        }
    }

    // Zależnie od backendu accept/reject mogą brać body (np. { inviteId }) albo query.
    // Tu zakładam najczęstszy wariant: body z id.
    async function acceptGroup(inviteId) {
        await apiClient.patch('/users/me/groups/invites/accept', { inviteId })
        groupInvites.value = groupInvites.value.filter(x => getInviteId(x) !== inviteId)
    }

    async function rejectGroup(inviteId) {
        await apiClient.patch('/users/me/groups/invites/reject', { inviteId })
        groupInvites.value = groupInvites.value.filter(x => getInviteId(x) !== inviteId)
    }

    async function acceptEvent(inviteId) {
        await apiClient.patch('/users/me/events/invites/accept', { inviteId })
        eventInvites.value = eventInvites.value.filter(x => getInviteId(x) !== inviteId)
    }

    async function rejectEvent(inviteId) {
        await apiClient.patch('/users/me/events/invites/reject', { inviteId })
        eventInvites.value = eventInvites.value.filter(x => getInviteId(x) !== inviteId)
    }

    function getInviteId(x) {
        return x?.inviteId ?? x?.id ?? x?.requestId ?? x?.invitationId ?? null
    }

    function getTitle(x) {
        return x?.name || x?.title || x?.groupName || x?.eventName || 'Zaproszenie'
    }

    function getFromUser(x) {
        return x?.from?.username || x?.inviter?.username || x?.sender?.username || ''
    }

    return {
        groupInvites,
        eventInvites,
        loading,
        error,
        fetchAll,
        acceptGroup,
        rejectGroup,
        acceptEvent,
        rejectEvent,
        getInviteId,
        getTitle,
        getFromUser,
    }
})
