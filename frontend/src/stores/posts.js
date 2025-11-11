import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios.js'

export const usePostsStore = defineStore('posts', () => {
    const items = ref([])
    const loading = ref(false)
    const page = ref(0)
    const size = ref(10)
    const total = ref(0)

    // sie zobaczy
    async function fetchNext(params = {}) {
        if (loading.value) return
        loading.value = true
        try {
            const resp = await apiClient.get('/posts', {
                params: { page: page.value, size: size.value, ...params },
                withCredentials: true,
            })
            const data = resp.data
            const list = Array.isArray(data) ? data : (data.content || [])
            if (page.value === 0) items.value = list
            else items.value = items.value.concat(list)
            total.value = data.totalElements ?? items.value.length
            page.value += 1
        } finally {
            loading.value = false
        }
    }

    function reset() {
        page.value = 0
        total.value = 0
        items.value = []
    }

    async function createPost({ content, files }) {
        const fd = new FormData()
        fd.append('data', content || '')
        ;(files || []).forEach(f => fd.append('photos', f))
        const resp = await apiClient.post('/posts/create', fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
            withCredentials: true,
        })
        // na początek listy
        items.value.unshift(resp.data)
        total.value += 1
        return resp.data
    }

    async function editPost({ id, content }) {
        const resp = await apiClient.patch('/posts/edit', { id, content }, { withCredentials: true })
        const idx = items.value.findIndex(p => getId(p) === id)
        if (idx !== -1) items.value[idx] = resp.data
        return resp.data
    }

    async function deletePost(id) {
        await apiClient.delete(`/posts/${id}`, { withCredentials: true })
        items.value = items.value.filter(p => getId(p) !== id)
        total.value = Math.max(0, total.value - 1)
    }

    async function voteUp(id) {
        const resp = await apiClient.patch(`/posts/${id}/upvote`, null, { withCredentials: true })
        _merge(resp.data)
    }
    async function voteDown(id) {
        const resp = await apiClient.patch(`/posts/${id}/downvote`, null, { withCredentials: true })
        _merge(resp.data)
    }

    async function reportPost({ postId, reason = 'OTHER' }) {
        await apiClient.post('/reports/posts', { postId, reason }, { withCredentials: true })
    }

    function _merge(updated) {
        const id = getId(updated)
        const i = items.value.findIndex(p => getId(p) === id)
        if (i !== -1) items.value[i] = updated
    }

    // Pomocnicze: ponieważ PostDto może nie mieć id, szukamy w możliwych polach
    function getId(p) {
        return p?.id ?? p?.postId ?? p?._id ?? null
    }

    return {
        items, loading, page, size, total,
        fetchNext, reset,
        createPost, editPost, deletePost,
        voteUp, voteDown, reportPost,
        getId,
    }
})
