// src/stores/admin.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios'

export const useAdminStore = defineStore('admin', () => {
    const reports = ref([])
    const users = ref([])

    // ───── REPORTS ─────────────────────────────

    async function loadReports() {
        try {
            const { data } = await apiClient.get('/admin/reports/posts')
            reports.value = data
        } catch (e) {
            console.error('loadReports error', e)
        }
    }

    async function resolveReport(id) {
        try {
            await apiClient.post(`/admin/reports/posts/${id}/resolve`)
            await loadReports()
        } catch (e) {
            console.error('resolveReport error', e)
        }
    }

    async function deleteReport(id) {
        try {
            await apiClient.delete(`/admin/reports/posts/${id}`)
            await loadReports()
        } catch (e) {
            console.error('deleteReport error', e)
        }
    }

    async function deletePost(postId) {
        try {
            await apiClient.delete(`/posts/${postId}`)
            await loadReports()
        } catch (e) {
            console.error('deletePost error', e)
        }
    }

    // ───── USERS ───────────────────────────────

    async function loadUsers() {
        try {
            const { data } = await apiClient.get('/admin/users')
            users.value = data
        } catch (e) {
            console.error('loadUsers error', e)
        }
    }

    async function banUser(id) {
        try {
            await apiClient.patch(`/admin/users/${id}/ban`)
            await loadUsers()
        } catch (e) {
            console.error('banUser error', e)
        }
    }

    async function unbanUser(id) {
        try {
            await apiClient.patch(`/admin/users/${id}/unban`)
            await loadUsers()
        } catch (e) {
            console.error('unbanUser error', e)
        }
    }

    return {
        reports,
        users,
        loadReports,
        resolveReport,
        deleteReport,
        deletePost,
        loadUsers,
        banUser,
        unbanUser,
    }
})
