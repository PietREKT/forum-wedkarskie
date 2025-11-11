import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios.js'

const LS_KEY = 'fw_votes'

function loadVotes() {
    try {
        return new Map(
            Object.entries(JSON.parse(localStorage.getItem(LS_KEY) || '{}')).map(([k, v]) => [Number(k), Number(v)])
        )
    } catch {
        return new Map()
    }
}
function saveVotes(map) {
    const obj = {}
    map.forEach((v, k) => (obj[k] = v))
    try {
        localStorage.setItem(LS_KEY, JSON.stringify(obj))
    } catch {}
}

export const usePostsStore = defineStore('posts', () => {
    const items = ref([])
    const loading = ref(false)
    const page = ref(0)
    const size = ref(10)
    const total = ref(0)

    const voted = ref(loadVotes()) // Map<postId, -1|0|1>
    const voting = ref(new Set()) // aktywne głosowania

    async function fetchNext(params = {}) {
        if (loading.value) return
        loading.value = true
        try {
            const resp = await apiClient.get('/posts/recent', {
                params: { page: page.value, size: size.value, ...params },
            })
            const data = resp.data || {}
            const raw = Array.isArray(data) ? data : data.content || []
            const list = raw.map(p => {
                const id = getId(p)
                return { ...p, viewerVote: voted.value.get(id) ?? 0 }
            })
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
        fd.append('content', content || '')
        ;(files || []).forEach(f => fd.append('photos', f))
        const resp = await apiClient.post('/posts/create', fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
        })
        const created = resp.data
        const id = getId(created)
        items.value.unshift({ ...created, viewerVote: voted.value.get(id) ?? 0 })
        total.value += 1
        return created
    }

    async function editPost({ id, content }) {
        const resp = await apiClient.patch('/posts/edit', { id, content })
        const i = items.value.findIndex(p => getId(p) === id)
        if (i !== -1)
            items.value[i] = { ...resp.data, viewerVote: items.value[i].viewerVote ?? 0 }
        return resp.data
    }

    async function deletePost(id) {
        await apiClient.delete(`/posts/${id}`)
        items.value = items.value.filter(p => getId(p) !== id)
        total.value = Math.max(0, total.value - 1)
        voted.value.delete(id)
        saveVotes(voted.value)
    }

    function _applyVoteLocal(id, target) {
        const i = items.value.findIndex(p => getId(p) === id)
        if (i === -1) return
        const prev = items.value[i].viewerVote ?? 0
        const delta = (target ?? 0) - (prev ?? 0)
        items.value[i] = {
            ...items.value[i],
            viewerVote: target,
            rating: (items.value[i].rating ?? 0) + delta,
        }
        if (target === 0) voted.value.delete(id)
        else voted.value.set(id, target)
        saveVotes(voted.value)
    }

    async function _applyVote(id, target) {
        if (voting.value.has(id)) return
        const current = voted.value.get(id) ?? 0
        if (target === current) return

        voting.value.add(id)
        try {
            const steps = target - current
            const times = Math.abs(steps)
            const fn =
                steps > 0
                    ? pid => apiClient.patch(`/posts/${pid}/upvote`)
                    : pid => apiClient.patch(`/posts/${pid}/downvote`)
            for (let k = 0; k < times; k++) await fn(id)
            _applyVoteLocal(id, target)
        } finally {
            voting.value.delete(id)
        }
    }

    async function voteUp(id) {
        const current = voted.value.get(id) ?? 0
        const target = current === 1 ? 0 : 1
        await _applyVote(id, target)
    }

    async function voteDown(id) {
        const current = voted.value.get(id) ?? 0
        const target = current === -1 ? 0 : -1
        await _applyVote(id, target)
    }

    async function reportPost({ postId, reason = 'OTHER' }) {
        await apiClient.post('/reports/posts/report', { postId, reason })
    }

    function getId(p) {
        return p?.id ?? p?.postId ?? p?._id ?? null
    }

    return {
        items,
        loading,
        page,
        size,
        total,
        fetchNext,
        reset,
        createPost,
        editPost,
        deletePost,
        voteUp,
        voteDown,
        reportPost,
        getId,
        voted,
        voting,
    }
})
