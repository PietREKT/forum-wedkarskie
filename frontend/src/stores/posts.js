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

    // zestaw postów, dla których trwa wysyłanie głosu
    const voting = ref(new Set())

    function clearError() {
        error.value = null
    }

    function getId(post) {
        if (!post) return null
        if (post.id != null) return post.id
        if (post.contentId != null) return post.contentId
        return null
    }

    function reset() {
        items.value = []
        loading.value = false
        page.value = 0
        total.value = 0
        error.value = null
        voting.value = new Set()
    }

    function setPostLocal(id, updater) {
        const idx = items.value.findIndex(p => getId(p) === id)
        if (idx === -1) return null
        const oldPost = items.value[idx]
        const newPost = updater(oldPost)
        if (newPost === oldPost) return oldPost

        const copy = items.value.slice()
        copy[idx] = newPost
        items.value = copy
        return newPost
    }

    async function fetchNext(params = {}) {
        if (loading.value) return
        clearError()
        loading.value = true

        try {
            const resp = await apiClient.get('/posts/recent', {
                params: { page: page.value, size: size.value, ...params },
            })

            const data = resp.data
            const raw = Array.isArray(data)
                ? data
                : data?.content ?? data?.items ?? []

            const list = raw.map(p => ({
                ...p,
                // viewerVote i rating przychodzą z backendu; jeśli brak, domyślnie 0
                viewerVote: typeof p.viewerVote === 'number' ? p.viewerVote : 0,
                rating: typeof p.rating === 'number' ? p.rating : 0,
            }))

            if (page.value === 0) {
                items.value = list
            } else {
                items.value = [...items.value, ...list]
            }

            if (!Array.isArray(data) && typeof data?.totalElements === 'number') {
                total.value = data.totalElements
            } else {
                total.value = items.value.length
            }

            page.value += 1
        } catch (e) {
            console.error('fetchNext error', e)
            const status = e?.response?.status
            if (status === 401) {
                error.value = 'Musisz być zalogowany, aby zobaczyć posty.'
            } else {
                error.value = 'Nie udało się pobrać postów. Spróbuj ponownie.'
            }
        } finally {
            loading.value = false
        }
    }

    async function createPost({ content, files }) {
        clearError()
        try {
            const fd = new FormData()
            fd.append('content', content || '')
            ;(files || []).forEach(f => fd.append('photos', f))

            const resp = await apiClient.post('/posts/create', fd, {
                headers: { 'Content-Type': 'multipart/form-data' },
            })

            const created = resp.data
            const withVote = {
                ...created,
                viewerVote:
                    typeof created.viewerVote === 'number' ? created.viewerVote : 0,
                rating: typeof created.rating === 'number' ? created.rating : 0,
            }

            items.value = [withVote, ...items.value]
            total.value += 1
            return withVote
        } catch (e) {
            console.error('createPost error', e)
            const status = e?.response?.status
            if (status === 401) {
                error.value = 'Musisz być zalogowany, aby dodać post.'
            } else if (status && status >= 400 && status < 500) {
                error.value = 'Nie udało się dodać posta (błąd żądania).'
            } else {
                error.value = 'Nie udało się dodać posta (błąd serwera).'
            }
            throw e
        }
    }

    async function editPost({ id, content }) {
        clearError()
        try {
            const resp = await apiClient.patch('/posts/edit', { id, content })
            const updated = resp.data
            const pid = getId(updated)

            setPostLocal(pid, () => ({
                ...updated,
                viewerVote:
                    typeof updated.viewerVote === 'number' ? updated.viewerVote : 0,
                rating: typeof updated.rating === 'number' ? updated.rating : 0,
            }))

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

    // lokalna zmiana głosu; zwraca poprzednią wartość viewerVote dla ewentualnego rollbacku
    function applyVoteLocal(id, target) {
        let prev = 0

        setPostLocal(id, old => {
            if (!old) return old

            const current = typeof old.viewerVote === 'number' ? old.viewerVote : 0
            prev = current

            // kliknięcie tego samego głosu drugi raz usuwa głos użytkownika
            const next = current === target ? 0 : target
            const rating = typeof old.rating === 'number' ? old.rating : 0
            const diff = next - current

            return {
                ...old,
                viewerVote: next,
                rating: rating + diff,
            }
        })

        return prev
    }

    async function voteInternal(id, target) {
        clearError()
        if (!id) return
        if (voting.value.has(id)) return

        const setCopy = new Set(voting.value)
        setCopy.add(id)
        voting.value = setCopy

        // zapamiętujemy poprzedni głos, żeby w razie błędu cofnąć
        const prev = applyVoteLocal(id, target)

        try {
            if (target === 1) {
                await apiClient.patch(`/posts/${id}/upvote`)
            } else if (target === -1) {
                await apiClient.patch(`/posts/${id}/downvote`)
            }
            // backend liczy głosy per użytkownik; my tylko pokazujemy stan bieżącego
        } catch (e) {
            console.error('vote error', e)
            const status = e?.response?.status
            if (status === 401) {
                error.value = 'Musisz być zalogowany, aby głosować.'
            } else {
                error.value = 'Nie udało się zapisać głosu.'
            }

            // rollback – przywracamy poprzedni głos
            applyVoteLocal(id, prev)
            throw e
        } finally {
            const s = new Set(voting.value)
            s.delete(id)
            voting.value = s
        }
    }

    async function voteUp(id) {
        return voteInternal(id, 1)
    }

    async function voteDown(id) {
        return voteInternal(id, -1)
    }

    // UWAGA: backend ma enum ReportReason bez wartości OTHER
    // używamy jednego z dozwolonych kodów, np. SPAM
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
