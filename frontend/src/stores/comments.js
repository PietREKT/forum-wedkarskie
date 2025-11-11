import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios.js'

export const useCommentsStore = defineStore('comments', () => {
    const byPost = ref({})
    const PAGE_SIZE = 10

    function _ensure(postId) {
        if (!byPost.value[postId]) {
            byPost.value[postId] = { list: [], page: 0, loading: false, hasMore: true }
        }
        return byPost.value[postId]
    }

    function normalize(c = {}) {
        const att =
            c.attachment ??
            c.photo ??
            c.photoUrl ??
            c.image ??
            c.imageUrl ??
            c.file ??
            c.fileUrl ??
            null
        return { ...c, attachment: att }
    }

    async function fetchNext(postId) {
        const state = _ensure(postId)
        if (state.loading || !state.hasMore) return
        state.loading = true
        try {
            const resp = await apiClient.get(`/comments/${postId}`, {
                params: { page: state.page, size: PAGE_SIZE },
            })
            const data = resp.data || {}
            const listRaw = Array.isArray(data) ? data : (data.content || data.items || [])
            const list = listRaw.map(normalize)
            if (!list.length || data.last === true) state.hasMore = false
            state.list = state.list.concat(list)
            state.page += 1
        } finally {
            state.loading = false
        }
    }

    async function add(postId, { content, file }) {
        const fd = new FormData()
        fd.append('content', content || '')
        fd.append('postId', postId)

        if (file) {
            fd.append('attachment', file)
            fd.append('photo', file)
            fd.append('image', file)
            fd.append('file', file)
        }

        const resp = await apiClient.post('/comments', fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
        })
        const created = normalize(resp.data)
        _ensure(postId).list.unshift(created)
        return created
    }

    async function edit({ id, content, postId }) {
        const resp = await apiClient.patch('/comments', { id, content })
        const list = _ensure(postId).list
        const i = list.findIndex(c => c.id === id)
        if (i !== -1) list[i] = normalize(resp.data)
        return resp.data
    }

    async function remove({ id, postId }) {
        await apiClient.delete(`/comments/${id}`)
        const state = _ensure(postId)
        state.list = state.list.filter(c => c.id !== id)
    }

    async function report({ commentId, reason = 'OTHER' }) {
        await apiClient.post('/reports/comments/report', { commentId, reason })
    }

    return { byPost, fetchNext, add, edit, remove, report }
})
