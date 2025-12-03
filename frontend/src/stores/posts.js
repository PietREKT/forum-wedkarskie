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

    // zostawione na przyszłość, ale nie blokujemy na tym przycisków
    const voting = ref(new Set())

    function clearError() {
        error.value = null
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

    function reset() {
        items.value = []
        page.value = 0
        total.value = 0
        error.value = null
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

        const rating =
            typeof raw.rating === 'number'
                ? raw.rating
                : 0

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
            const resp = await apiClient.get('/posts/recent', {
                params: {
                    page: page.value,
                    size: size.value,
                },
            })

            const data = resp.data
            const list = Array.isArray(data?.content) ? data.content : data

            const normalized = (list || []).map(normalizePost)

            if (page.value === 0) {
                items.value = normalized
            } else {
                items.value = [...items.value, ...normalized]
            }

            if (!Array.isArray(data) && typeof data?.totalElements === 'number') {
                total.value = data.totalElements
            } else {
                total.value = items.value.length
            }

            page.value += 1
        } catch (e) {
            console.error('fetchNext posts error', e)
            error.value = 'Nie udało się pobrać postów.'
        } finally {
            loading.value = false
        }
    }

    async function createPost({ content, files = [] }) {
        clearError()
        const fd = new FormData()
        fd.append('content', content)
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
            } else {
                error.value = 'Nie udało się utworzyć posta.'
            }
            throw e
        }
    }

    // edycja posta (tekst + zdjęcia) – z obejściem "__EMPTY__"
    async function editPost({ id, content, newPhotos = [], attachedPhotos = [] }) {
        clearError()
        try {
            const fd = new FormData()
            fd.append('id', id)
            fd.append('content', content || '')

            if (attachedPhotos && attachedPhotos.length) {
                attachedPhotos.forEach(name => {
                    fd.append('attachedPhotos', name)
                })
            } else {
                fd.append('attachedPhotos', '__EMPTY__')
            }

            newPhotos.forEach(file => {
                fd.append('newPhotos', file)
            })

            const resp = await apiClient.patch('/posts/edit', fd, {
                headers: { 'Content-Type': 'multipart/form-data' },
            })
            const updated = normalizePost(resp.data)
            const pid = getId(updated)

            setPostLocal(pid, () => updated)

            return updated
        } catch (e) {
            console.error('editPost error', e)
            const status = e?.response?.status
            if (status === 401) {
                error.value = 'Musisz być zalogowany, aby edytować post.'
            } else {
                error.value = 'Nie udało się zaktualizować posta.'
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
                error.value = 'Nie udało się usunąć posta.'
            }
            throw e
        }
    }

    // optymistyczna aktualizacja głosu
    function applyLocalVote(id, direction) {
        const idx = findIndexById(id)
        if (idx === -1) return null

        const post = items.value[idx]
        const prevVote = mapVoteToNumber(post.viewerVote)
        const prevRating =
            typeof post.rating === 'number' ? post.rating : 0

        let newVote = prevVote
        let newRating = prevRating

        if (prevVote === direction) {
            // cofnięcie głosu
            newVote = 0
            newRating = prevRating - direction
        } else if (prevVote === 0) {
            // nowy głos
            newVote = direction
            newRating = prevRating + direction
        } else if (prevVote === -direction) {
            // zmiana +1 ↔ -1
            newVote = direction
            newRating = prevRating + 2 * direction
        }

        const optimistic = {
            ...post,
            viewerVote: newVote,
            rating: newRating,
        }

        items.value.splice(idx, 1, optimistic)

        return { prevVote, prevRating }
    }

    async function voteUp(id) {
        clearError()
        const backup = applyLocalVote(id, 1)

        try {
            const resp = await apiClient.patch(`/posts/${id}/upvote`)
            const updated = normalizePost(resp.data)
            const pid = getId(updated)
            setPostLocal(pid, () => updated)
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
            const pid = getId(updated)
            setPostLocal(pid, () => updated)
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
                error.value = 'Nie udało się zgłosić posta.'
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
        voting,
        clearError,
        getId,
        reset,
        fetchNext,
        createPost,
        editPost,
        deletePost,
        voteUp,
        voteDown,
        reportPost,
    }
})
