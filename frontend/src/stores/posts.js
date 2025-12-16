// src/stores/posts.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { apiClient } from '../utils/axios.js'

export const usePostsStore = defineStore('posts', () => {
    const items = ref([])
    const loading = ref(false)
    const page = ref(0)
    const size = ref(10)
    const total = ref(0)
    const error = ref(null)

    // null = globalny feed (/posts/recent)
    // nie-null = posty użytkownika (/posts/user/{userId})
    const userFilterId = ref(null)

    function clearError() {
        error.value = null
    }

    function setErrorFromAxios(e, fallbackMessage) {
        const backendMessage =
            e?.response?.data?.message ||
            e?.response?.data?.error ||
            e?.response?.data?.detail

        error.value = backendMessage || fallbackMessage
    }

    function getId(p) {
        return p?.id ?? p?.contentId ?? p?.postId
    }

    function findIndexById(id) {
        return items.value.findIndex(p => getId(p) === id)
    }

    function setPostLocal(id, updater) {
        const idx = findIndexById(id)
        if (idx === -1) return
        const updated = updater(items.value[idx])
        items.value.splice(idx, 1, updated)
    }

    function reset(arg = null) {
        let userId = null

        if (arg && typeof arg === 'object') {
            userId = arg.userId ?? null
        } else {
            userId = arg ?? null
        }

        items.value = []
        page.value = 0
        total.value = 0
        error.value = null
        userFilterId.value = userId
    }

    function mapVoteToNumber(v) {
        if (typeof v === 'number') return v
        if (!v) return 0
        if (v === 'UPVOTE') return 1
        if (v === 'DOWNVOTE') return -1
        return 0
    }

    function normalizePost(raw) {
        if (!raw || typeof raw !== 'object') return raw

        const voteSource =
            raw.viewerVote ?? raw.loggedUserVote ?? raw.userVote ?? raw.vote

        const viewerVote = mapVoteToNumber(voteSource)
        const rating = typeof raw.rating === 'number' ? raw.rating : 0

        return {
            ...raw,
            viewerVote,
            rating,
        }
    }

    async function fetchNext() {
        if (loading.value) return
        clearError()
        loading.value = true

        try {
            const basePath = userFilterId.value
                ? `/posts/user/${encodeURIComponent(String(userFilterId.value))}`
                : '/posts/recent'

            const resp = await apiClient.get(basePath, {
                params: { page: page.value, size: size.value },
            })

            const data = resp.data
            const list = Array.isArray(data?.content) ? data.content : data
            const normalized = (list || []).map(normalizePost)

            if (page.value === 0) items.value = normalized
            else items.value = [...items.value, ...normalized]

            if (!Array.isArray(data) && typeof data?.totalElements === 'number') {
                total.value = data.totalElements
            } else {
                total.value = items.value.length
            }

            page.value += 1
        } catch (e) {
            console.error('fetchNext posts error', e)
            setErrorFromAxios(e, 'Nie udało się pobrać postów.')
            throw e
        } finally {
            loading.value = false
        }
    }

    async function fetchById(id) {
        clearError()
        loading.value = true
        try {
            const resp = await apiClient.get(`/posts/${id}`)
            const post = normalizePost(resp.data)

            const existingIdx = findIndexById(getId(post))
            if (existingIdx !== -1) {
                items.value.splice(existingIdx, 1, post)
            }

            return post
        } catch (e) {
            console.error('fetchById post error', e)
            setErrorFromAxios(e, 'Nie udało się pobrać posta.')
            throw e
        } finally {
            loading.value = false
        }
    }

    async function createPost({ content, files = [] }) {
        clearError()

        const text = (content || '').trim()
        const hasFiles = Array.isArray(files) && files.length > 0

        if (!text && !hasFiles) {
            error.value = 'Post musi mieć treść lub załącznik.'
            throw new Error('Missing content/files')
        }

        const fd = new FormData()
        fd.append('content', text)
        files.forEach(f => fd.append('photos', f))

        try {
            const resp = await apiClient.post('/posts/create', fd, {
                headers: { 'Content-Type': 'multipart/form-data' },
            })
            const created = normalizePost(resp.data)

            items.value = [created, ...items.value]
            total.value += 1

            return created
        } catch (e) {
            console.error('createPost error', e)
            const status = e?.response?.status

            if (status === 401) {
                error.value = 'Musisz być zalogowany, aby dodać post.'
            } else if (status === 403) {
                // komunikat dla wyciszonego
                error.value = 'Nie możesz publikować — zostałeś wyciszony.'
            } else {
                setErrorFromAxios(e, 'Nie udało się utworzyć posta.')
            }
            throw e
        }
    }

    async function editPost({ id, content, newPhotos = [], attachedPhotos = [] }) {
        clearError()

        const text = (content || '').trim()
        if (!text) {
            error.value = 'Treść posta nie może być pusta.'
            throw new Error('Missing content')
        }

        try {
            const fd = new FormData()
            fd.append('id', id)
            fd.append('content', text)

            if (Array.isArray(attachedPhotos) && attachedPhotos.length) {
                attachedPhotos.forEach(name => fd.append('attachedPhotos', name))
            } else {
                fd.append('attachedPhotos', '__EMPTY__')
            }

            newPhotos.forEach(file => fd.append('newPhotos', file))

            const resp = await apiClient.patch('/posts/edit', fd, {
                headers: { 'Content-Type': 'multipart/form-data' },
            })

            const updated = normalizePost(resp.data)
            setPostLocal(getId(updated), () => updated)

            return updated
        } catch (e) {
            console.error('editPost error', e)
            const status = e?.response?.status
            if (status === 401) {
                error.value = 'Musisz być zalogowany, aby edytować post.'
            } else if (status === 403) {
                error.value = 'Nie możesz publikować — zostałeś wyciszony.'
            } else {
                setErrorFromAxios(e, 'Nie udało się zaktualizować posta.')
            }
            throw e
        }
    }

    async function deletePost(id) {
        clearError()
        try {
            await apiClient.delete(`/posts/${id}`)
            items.value = items.value.filter(p => getId(p) !== id)
            total.value = Math.max(0, total.value - 1)
        } catch (e) {
            console.error('deletePost error', e)
            const status = e?.response?.status
            if (status === 401) {
                error.value = 'Musisz być zalogowany, aby usuwać posty.'
            } else {
                setErrorFromAxios(e, 'Nie udało się usunąć posta.')
            }
            throw e
        }
    }

    function applyLocalVote(id, direction) {
        const idx = findIndexById(id)
        if (idx === -1) return null

        const post = items.value[idx]
        const prevVote = mapVoteToNumber(post.viewerVote)
        const prevRating = typeof post.rating === 'number' ? post.rating : 0

        let newVote = prevVote
        let newRating = prevRating

        if (prevVote === direction) {
            newVote = 0
            newRating = prevRating - direction
        } else if (prevVote === 0) {
            newVote = direction
            newRating = prevRating + direction
        } else if (prevVote === -direction) {
            newVote = direction
            newRating = prevRating + 2 * direction
        }

        items.value.splice(idx, 1, { ...post, viewerVote: newVote, rating: newRating })
        return { prevVote, prevRating }
    }

    async function voteUp(id) {
        clearError()
        const backup = applyLocalVote(id, 1)
        try {
            const resp = await apiClient.patch(`/posts/${id}/upvote`)
            const updated = normalizePost(resp.data)
            setPostLocal(getId(updated), () => updated)
        } catch (e) {
            console.error('voteUp error', e)
            if (backup) {
                const idx = findIndexById(id)
                if (idx !== -1) {
                    const post = items.value[idx]
                    items.value.splice(idx, 1, {
                        ...post,
                        viewerVote: backup.prevVote,
                        rating: backup.prevRating,
                    })
                }
            }
        }
    }

    async function voteDown(id) {
        clearError()
        const backup = applyLocalVote(id, -1)
        try {
            const resp = await apiClient.patch(`/posts/${id}/downvote`)
            const updated = normalizePost(resp.data)
            setPostLocal(getId(updated), () => updated)
        } catch (e) {
            console.error('voteDown error', e)
            if (backup) {
                const idx = findIndexById(id)
                if (idx !== -1) {
                    const post = items.value[idx]
                    items.value.splice(idx, 1, {
                        ...post,
                        viewerVote: backup.prevVote,
                        rating: backup.prevRating,
                    })
                }
            }
        }
    }

    async function reportPost({ postId, reason = 'SPAM' }) {
        clearError()

        if (!postId) {
            error.value = 'Brak identyfikatora zgłaszanego posta.'
            throw new Error('Missing postId')
        }

        try {
            await apiClient.post('/reports/posts/report', {
                contentId: postId,
                reason,
            })
        } catch (e) {
            console.error('reportPost error', e)
            const status = e?.response?.status
            if (status === 401) {
                error.value = 'Musisz być zalogowany, aby zgłaszać posty.'
            } else {
                setErrorFromAxios(e, 'Nie udało się zgłosić posta.')
            }
            throw e
        }
    }

    return {
        items,
        loading,
        page,
        size,
        total,
        error,
        userFilterId,

        clearError,
        getId,
        reset,

        fetchNext,
        fetchById,

        createPost,
        editPost,
        deletePost,

        voteUp,
        voteDown,

        reportPost,
    }
})
