import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios'

export const useAdminStore = defineStore('admin', () => {
    const reportedContent = ref([])
    const reportedTotal = ref(0)
    const reportedPage = ref(0)
    const reportedSize = ref(30)

    const selectedReport = ref(null)
    const selectedContentId = ref(null)

    const bannedUsers = ref([])
    const mutedUsers = ref([])

    const loading = ref(false)
    const error = ref(null)

    const actionMessage = ref(null)
    const actionError = ref(null)

    function clearAction() {
        actionMessage.value = null
        actionError.value = null
    }

    function asArray(payload) {
        if (Array.isArray(payload)) return payload
        if (Array.isArray(payload?.content)) return payload.content
        if (Array.isArray(payload?.items)) return payload.items
        return []
    }

    // Instant -> najlepiej ISO z Z
    function toInstantIsoFromMinutes(minutes) {
        const m = Number(minutes)
        if (!Number.isFinite(m) || m <= 0) return null
        const d = new Date(Date.now() + m * 60 * 1000)
        return d.toISOString() // np. 2025-12-15T08:15:20.000Z
    }

    const PERMANENT_UNTIL = '9999-12-31T23:59:59.000Z'

    async function loadReportedContentSummary(opts = {}) {
        const page = Number.isFinite(opts.page) ? opts.page : reportedPage.value
        const size = Number.isFinite(opts.size) ? opts.size : reportedSize.value
        const amount = opts.amount ?? 1
        const unit = opts.unit ?? 'WEEKS'

        loading.value = true
        error.value = null
        try {
            const { data } = await apiClient.get('/admin/reports/content/summary', {
                params: { page, size, amount, unit },
            })

            reportedContent.value = asArray(data)
            reportedTotal.value = Number(data?.totalElements ?? reportedContent.value.length ?? 0)
            reportedPage.value = Number(data?.page ?? page)
            reportedSize.value = Number(data?.size ?? size)
        } catch (e) {
            console.error('loadReportedContentSummary error', e)
            error.value = e
            reportedContent.value = []
            reportedTotal.value = 0
        } finally {
            loading.value = false
        }
    }

    async function loadContentReportDetails(contentId) {
        if (contentId === null || contentId === undefined) return
        loading.value = true
        error.value = null
        try {
            selectedContentId.value = contentId
            const { data } = await apiClient.get(`/admin/reports/content/${contentId}`)
            selectedReport.value = data ?? null
        } catch (e) {
            console.error('loadContentReportDetails error', e)
            error.value = e
            selectedReport.value = null
        } finally {
            loading.value = false
        }
    }

    async function dismissContentReports(contentId) {
        if (contentId === null || contentId === undefined) return
        loading.value = true
        error.value = null
        clearAction()
        try {
            await apiClient.post(`/admin/reports/content/${contentId}/dismiss`)
            actionMessage.value = 'Zgłoszenia zostały odrzucone.'
            await loadReportedContentSummary()
            if (selectedContentId.value === contentId) {
                selectedReport.value = null
                selectedContentId.value = null
            }
        } catch (e) {
            console.error('dismissContentReports error', e)
            error.value = e
            actionError.value = e?.response?.data?.message || 'Nie udało się odrzucić zgłoszeń.'
        } finally {
            loading.value = false
        }
    }

    async function deleteContentByType(contentId, contentType) {
        if (contentId === null || contentId === undefined) return
        const type = String(contentType || '').toUpperCase()

        loading.value = true
        error.value = null
        clearAction()
        try {
            if (type === 'POST') {
                await apiClient.delete(`/posts/${contentId}`)
            } else if (type === 'COMMENT') {
                await apiClient.delete(`/comments/${contentId}`)
            } else if (type === 'TUTORIAL') {
                await apiClient.delete(`/tutorials/${contentId}`)
            } else {
                throw new Error(`Nieznany typ treści: ${contentType}`)
            }

            actionMessage.value = 'Treść została usunięta.'
            await loadReportedContentSummary()
            if (selectedContentId.value === contentId) {
                selectedReport.value = null
                selectedContentId.value = null
            }
        } catch (e) {
            console.error('deleteContentByType error', e)
            error.value = e
            actionError.value = e?.response?.data?.message || 'Nie udało się usunąć treści.'
        } finally {
            loading.value = false
        }
    }

    // PunishUserDto: { id, reason*, punishedUntil: Instant }
    async function banUser({ userId, reason, minutes = null, permanent = false } = {}) {
        if (!userId) return
        loading.value = true
        error.value = null
        clearAction()
        try {
            const punishedUntil = permanent ? PERMANENT_UNTIL : toInstantIsoFromMinutes(minutes)

            await apiClient.post('/admin/users/ban', {
                id: userId,
                reason: String(reason || '').trim(),
                punishedUntil,
            })

            actionMessage.value = permanent ? 'Użytkownik został zbanowany na stałe.' : 'Użytkownik został zbanowany.'
            await loadBannedUsers()
        } catch (e) {
            console.error('banUser error', e)
            error.value = e
            actionError.value = e?.response?.data?.message || 'Nie udało się zbanować użytkownika.'
        } finally {
            loading.value = false
        }
    }

    async function unbanUser({ userId, reason = 'Cofnięcie bana' } = {}) {
        if (!userId) return
        loading.value = true
        error.value = null
        clearAction()
        try {
            await apiClient.post('/admin/users/unban', {
                id: userId,
                reason: String(reason || '').trim() || 'Cofnięcie bana',
                punishedUntil: null,
            })
            actionMessage.value = 'Ban został cofnięty.'
            await loadBannedUsers()
        } catch (e) {
            console.error('unbanUser error', e)
            error.value = e
            actionError.value = e?.response?.data?.message || 'Nie udało się cofnąć bana.'
        } finally {
            loading.value = false
        }
    }

    async function muteUser({ userId, reason, minutes = null, permanent = false } = {}) {
        if (!userId) return
        loading.value = true
        error.value = null
        clearAction()
        try {
            const punishedUntil = permanent ? PERMANENT_UNTIL : toInstantIsoFromMinutes(minutes)

            await apiClient.patch('/mod/users/mute', {
                id: userId,
                reason: String(reason || '').trim(),
                punishedUntil,
            })

            actionMessage.value = permanent ? 'Użytkownik został wyciszony na stałe.' : 'Użytkownik został wyciszony.'
            await loadMutedUsers()
        } catch (e) {
            console.error('muteUser error', e)
            error.value = e
            actionError.value = e?.response?.data?.message || 'Nie udało się wyciszyć użytkownika.'
        } finally {
            loading.value = false
        }
    }

    async function unmuteUser({ userId, reason = 'Cofnięcie wyciszenia' } = {}) {
        if (!userId) return
        loading.value = true
        error.value = null
        clearAction()
        try {
            await apiClient.patch('/mod/users/unmute', {
                id: userId,
                reason: String(reason || '').trim() || 'Cofnięcie wyciszenia',
                punishedUntil: null,
            })
            actionMessage.value = 'Wyciszenie zostało cofnięte.'
            await loadMutedUsers()
        } catch (e) {
            console.error('unmuteUser error', e)
            error.value = e
            actionError.value = e?.response?.data?.message || 'Nie udało się cofnąć wyciszenia.'
        } finally {
            loading.value = false
        }
    }

    async function loadBannedUsers() {
        loading.value = true
        error.value = null
        try {
            const { data } = await apiClient.get('/admin/users/banned')
            bannedUsers.value = asArray(data)
        } catch (e) {
            console.error('loadBannedUsers error', e)
            error.value = e
            bannedUsers.value = []
        } finally {
            loading.value = false
        }
    }

    async function loadMutedUsers() {
        loading.value = true
        error.value = null
        try {
            const { data } = await apiClient.get('/mod/users/muted')
            mutedUsers.value = asArray(data)
        } catch (e) {
            console.error('loadMutedUsers error', e)
            error.value = e
            mutedUsers.value = []
        } finally {
            loading.value = false
        }
    }

    async function refreshAll() {
        await Promise.allSettled([
            loadReportedContentSummary({ page: 0, size: reportedSize.value }),
            loadBannedUsers(),
            loadMutedUsers(),
        ])
    }

    return {
        reportedContent,
        reportedTotal,
        reportedPage,
        reportedSize,
        selectedReport,
        selectedContentId,

        bannedUsers,
        mutedUsers,

        loading,
        error,
        actionMessage,
        actionError,

        loadReportedContentSummary,
        loadContentReportDetails,
        dismissContentReports,
        deleteContentByType,

        banUser,
        unbanUser,
        muteUser,
        unmuteUser,

        loadBannedUsers,
        loadMutedUsers,
        refreshAll,
        clearAction,
    }
})
