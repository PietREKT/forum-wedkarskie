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

        normalizeComment(postId, c) {
            const postIdNum = Number(postId)
            const parentIdRaw = c?.parent?.id ?? null
            const parentIdNum = parentIdRaw != null ? Number(parentIdRaw) : null

            // Backend: root komentarze często mają parent.id == postId
            // UI: root komentarz ma parentId = null, reply ma parentId = id komentarza-rodzica
            const isRoot = parentIdNum != null && parentIdNum === postIdNum

            return {
                ...c,
                parent: c.parent || null,
                parentId: isRoot ? null : parentIdRaw,
                _postId: postId,
            }
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

                const mapped = items.map(c => this.normalizeComment(postId, c))

                if (state.page === 0) {
                    state.list = mapped
                } else {
                    const existingIds = new Set(state.list.map(c => c.id))
                    mapped.forEach(c => {
                        if (!existingIds.has(c.id)) state.list.push(c)
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
            state.list.unshift(this.normalizeComment(postId, comment))
        },

        async add(postId, payload) {
            const form = new FormData()
            form.append('content', payload.content)

            // Backend wymaga:
            // - root komentarz: parentId = postId
            // - reply: parentId = id komentarza-rodzica
            const parentIdForBackend =
                payload.parentId != null ? payload.parentId : postId

            form.append('parentId', parentIdForBackend)

            if (payload.file) {
                form.append('photos', payload.file)
            }

            this.error = null

            try {
                const res = await apiClient.post('/comments', form, {
                    headers: { 'Content-Type': 'multipart/form-data' },
                })

                const created = res.data || {}

                if (payload.parentId != null && !created.parent) {
                    created.parent = { id: payload.parentId }
                }

                const normalized = this.normalizeComment(postId, created)
                this.addToStore(postId, normalized)

                if (payload.parentId != null) {
                    this.childrenLoaded[payload.parentId] = true
                }

                return normalized
            } catch (e) {
                console.error(e)
                const status = e?.response?.status
                if (status === 403) {
                    this.error = 'Nie możesz dodać komentarza — zostałeś wyciszony.'
                } else {
                    this.error = 'Nie udało się dodać komentarza.'
                }
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

                const merged = {
                    ...existing,
                    ...updatedRaw,
                    parent: updatedRaw.parent || existing.parent || null,
                }

                const normalized = this.normalizeComment(postId, merged)

                if (normalized.parentId == null && existing.parentId != null) {
                    normalized.parentId = existing.parentId
                    normalized.parent = existing.parent || null
                }

                state.list = state.list.map(c => (c.id === id ? normalized : c))
                return normalized
            } catch (e) {
                console.error(e)
                const status = e?.response?.status
                if (status === 403) {
                    this.error = 'Nie możesz edytować — zostałeś wyciszony.'
                } else {
                    this.error = 'Nie udało się zaktualizować komentarza.'
                }
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

            state.list = state.list.filter(c => c.id !== id && c.parentId !== id)
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

                const mapped = items.map(c => {
                    const normalized = this.normalizeComment(postId, c)
                    normalized.parentId = parentId
                    normalized.parent = normalized.parent || { id: parentId }
                    return normalized
                })

                const state = this.ensurePostState(postId)
                const existingIds = new Set(state.list.map(c => c.id))
                mapped.forEach(c => {
                    if (!existingIds.has(c.id)) state.list.push(c)
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
