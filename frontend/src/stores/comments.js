import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios.js'

export const useCommentsStore = defineStore('comments', () => {
    const byPost = ref({})

    function _ensure(postId) {
        if (!byPost.value[postId]) {
            byPost.value[postId] = { list: [], page: 0, loading: false, hasMore: true }
        }
        return byPost.value[postId]
    }

    async function fetchNext(postId) {
        const state = _ensure(postId)
        if (state.loading || !state.hasMore) return
        state.loading = true
        try {
            const resp = await apiClient.get(`/comments/${postId}`, {
                params: { page: state.page },
                withCredentials: true,
            })
            const data = resp.data
            const list = Array.isArray(data) ? data : (data.content || data.items || [])
            if (list.length === 0) state.hasMore = false
            state.list = state.list.concat(list)
            state.page += 1
        } finally {
            state.loading = false
        }
    }

    async function add(postId, { content, file }) {
        const fd = new FormData()
        fd.append('content', content || '')
        fd.append('post.id', postId)
        if (file) fd.append('attachment', file)
        const resp = await apiClient.post('/comments', fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
            withCredentials: true,
        })
        _ensure(postId).list.unshift(resp.data)
        return resp.data
    }

    async function edit({ id, content, postId }) {
        const resp = await apiClient.patch('/comments', { id, content }, { withCredentials: true })
        const list = _ensure(postId).list
        const i = list.findIndex(c => c.id === id)
        if (i !== -1) list[i] = resp.data
        return resp.data
    }

    async function remove({ id, postId }) {
        await apiClient.delete(`/comments/${id}`, { withCredentials: true })
        const state = _ensure(postId)
        state.list = state.list.filter(c => c.id !== id)
    }

    async function report({ commentId, reason = 'OTHER' }) {
        await apiClient.post('/reports/comments', { commentId, reason }, { withCredentials: true })
    }

    return { byPost, fetchNext, add, edit, remove, report }
})
