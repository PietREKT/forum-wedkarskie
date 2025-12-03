import { defineStore } from 'pinia'
import { apiClient } from '../utils/axios'

export const useCommentsStore = defineStore('comments', {
    state: () => ({
        byPost: {},
        error: null,
        childrenLoaded: {},
    }),

    actions: {
        ensurePostState(postId) {
            if (!this.byPost[postId]) {
                this.byPost[postId] = {
                    list: [],
                    page: 0,
                    loading: false,
                    hasMore: true,
                }
            }
            return this.byPost[postId]
        },

        async fetchNext(postId) {
            const state = this.ensurePostState(postId)
            if (state.loading || !state.hasMore) return

            state.loading = true
            this.error = null

            try {
                const res = await apiClient.get(`/comments/${postId}`, {
                    params: { page: state.page },
                })

                const page = res.data || {}
                const items = page.items || page.content || []

                const mapped = items.map(c => ({
                    ...c,
                    parent: c.parent || null,
                    parentId: c.parent ? c.parent.id : postId,
                }))

                if (state.page === 0) {
                    state.list = mapped
                } else {
                    const existingIds = new Set(state.list.map(c => c.id))
                    mapped.forEach(c => {
                        if (!existingIds.has(c.id)) {
                            state.list.push(c)
                        }
                    })
                }

                state.page += 1
                state.hasMore =
                    page.hasNext !== undefined
                        ? page.hasNext
                        : page.totalPages !== undefined && page.page !== undefined
                            ? page.page + 1 < page.totalPages
                            : items.length > 0
            } catch (e) {
                console.error(e)
                this.error = 'Nie udało się wczytać komentarzy.'
            } finally {
                state.loading = false
            }
        },

        addToStore(postId, comment) {
            const state = this.ensurePostState(postId)
            state.list.unshift({
                ...comment,
                parent: comment.parent || null,
                parentId: comment.parent ? comment.parent.id : comment.parentId ?? null,
            })
        },

        async add(postId, payload) {
            const form = new FormData()
            form.append('content', payload.content)

            const parentId = payload.parentId != null ? payload.parentId : postId
            form.append('parentId', parentId)

            if (payload.file) {
                form.append('photos', payload.file)
            }

            this.error = null

            try {
                const res = await apiClient.post('/comments', form, {
                    headers: { 'Content-Type': 'multipart/form-data' },
                })

                const created = res.data || {}
                const withParent = {
                    ...created,
                    parent: created.parent || { id: parentId },
                    parentId: created.parent ? created.parent.id : parentId,
                }

                this.addToStore(postId, withParent)

                this.childrenLoaded[parentId] = true

                return withParent
            } catch (e) {
                console.error(e)
                this.error = 'Nie udało się dodać komentarza.'
                throw e
            }
        },

        async edit({ id, content, postId }) {
            this.error = null
            const state = this.ensurePostState(postId)

            try {
                const res = await apiClient.patch('/comments', {
                    id,
                    content,
                    attachedPhotos: [],
                    newPhotos: [],
                })

                const updatedRaw = res.data || {}
                const existing = state.list.find(c => c.id === id) || {}
                const updated = {
                    ...existing,
                    ...updatedRaw,
                    parent: updatedRaw.parent || existing.parent || null,
                }
                updated.parentId = updated.parent
                    ? updated.parent.id
                    : existing.parentId ?? null

                state.list = state.list.map(c => (c.id === id ? updated : c))

                return updated
            } catch (e) {
                console.error(e)
                this.error = 'Nie udało się zaktualizować komentarza.'
                throw e
            }
        },

        async remove({ id, postId }) {
            const state = this.ensurePostState(postId)
            this.error = null

            try {
                await apiClient.delete(`/comments/${id}`)
            } catch (e) {
                if (!(e.response && e.response.status === 500)) {
                    console.error(e)
                    this.error = 'Nie udało się usunąć komentarza.'
                    throw e
                }
            }

            // usuń komentarz + jego odpowiedzi
            state.list = state.list.filter(
                c => c.id !== id && c.parentId !== id,
            )
        },

        async report({ id, reason }) {
            this.error = null
            try {
                await apiClient.post('/reports/posts/report', {
                    contentId: id,
                    reason,
                })
            } catch (e) {
                console.error(e)
                this.error = 'Nie udało się wysłać zgłoszenia.'
                throw e
            }
        },

        async fetchChildren(postId, parentId) {
            if (this.childrenLoaded[parentId]) return
            this.childrenLoaded[parentId] = true

            try {
                const res = await apiClient.get(`/comments/${parentId}`, {
                    params: { page: 0 },
                })

                const page = res.data || {}
                const items = page.items || page.content || []

                if (!items.length) return

                const mapped = items.map(c => ({
                    ...c,
                    parent: c.parent || { id: parentId },
                    parentId,
                }))

                const state = this.ensurePostState(postId)
                const existingIds = new Set(state.list.map(c => c.id))
                mapped.forEach(c => {
                    if (!existingIds.has(c.id)) {
                        state.list.push(c)
                    }
                })
            } catch (e) {
                console.error(e)
            }
        },

        clearError() {
            this.error = null
        },
    },
})
