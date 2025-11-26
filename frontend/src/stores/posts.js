import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios.js'

export const usePostsStore = defineStore('posts', () => {
    const items = ref([])
    const page = ref(0)
    const size = ref(10)
    const total = ref(0)
    const loading = ref(false)

    const voting = ref(new Set())

    function getId(post) {
        return post?.id ?? post?.postId ?? null
    }

    function reset() {
        items.value = []
        page.value = 0
        total.value = 0
    }

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

                let backendVote = 0
                if (p.loggedUserVote === 'UPVOTE') backendVote = 1
                else if (p.loggedUserVote === 'DOWNVOTE') backendVote = -1

                const viewerVote = backendVote

                return { ...p, viewerVote }
            })

            if (page.value === 0) items.value = list
            else items.value = items.value.concat(list)

            total.value = data.totalElements ?? total.value
            page.value += 1
        } finally {
            loading.value = false
        }
    }

    async function createPost({ content, files = [] }) {
        const fd = new FormData()
        fd.append('content', content || '')
        ;(files || []).forEach(f => fd.append('photos', f))

        const resp = await apiClient.post('/posts/create', fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
        })

        const created = resp.data
        const id = getId(created)

        let backendVote = 0
        if (created.loggedUserVote === 'UPVOTE') backendVote = 1
        else if (created.loggedUserVote === 'DOWNVOTE') backendVote = -1

        items.value.unshift({ ...created, viewerVote: backendVote })
        total.value += 1
        return created
    }

    async function editPost({ id, content }) {
        const payload = { id, content: content || '' }

        const resp = await apiClient.patch('/posts/edit', payload)

        const updated = resp.data
        const idx = items.value.findIndex(p => getId(p) === id)
        if (idx !== -1) {
            // zachowujemy viewerVote (głos użytkownika)
            const viewerVote = items.value[idx].viewerVote ?? 0
            items.value[idx] = {
                ...items.value[idx],
                ...updated,
                viewerVote,
            }
        }
        return updated
    }

    async function deletePost(id) {
        await apiClient.delete(`/posts/${id}`)
        items.value = items.value.filter(p => getId(p) !== id)
        total.value = Math.max(0, total.value - 1)
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
            loggedUserVote:
                target === 1 ? 'UPVOTE' : target === -1 ? 'DOWNVOTE' : null,
        }
    }

    async function _applyVote(id, target) {
        if (voting.value.has(id)) return

        const post = items.value.find(p => getId(p) === id)
        const current = post?.viewerVote ?? 0
        if (target === current) return

        voting.value.add(id)
        try {
            const steps = target - current
            const times = Math.abs(steps)
            const fn =
                steps > 0
                    ? pid => apiClient.patch(`/posts/${pid}/upvote`)
                    : pid => apiClient.patch(`/posts/${pid}/downvote`)

            for (let k = 0; k < times; k++) {
                await fn(id)
            }

            _applyVoteLocal(id, target)
        } finally {
            voting.value.delete(id)
        }
    }

    async function voteUp(id) {
        const post = items.value.find(p => getId(p) === id)
        const current = post?.viewerVote ?? 0
        const target = current === 1 ? 0 : 1
        await _applyVote(id, target)
    }

    async function voteDown(id) {
        const post = items.value.find(p => getId(p) === id)
        const current = post?.viewerVote ?? 0
        const target = current === -1 ? 0 : -1
        await _applyVote(id, target)
    }

    async function reportPost({ postId, reason = 'SPAM' }) {
        await apiClient.post('/reports/posts/report', {
            postId,
            reason,
        })
    }

    return {
        items,
        page,
        size,
        total,
        loading,
        fetchNext,
        reset,
        createPost,
        editPost,
        deletePost,
        voteUp,
        voteDown,
        reportPost,
        getId,
        voting,
    }
})
