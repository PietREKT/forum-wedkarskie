// src/stores/guides.js
import { defineStore } from 'pinia'
import { apiClient } from '../utils/axios.js'

function mapResponseToList(data) {
    if (Array.isArray(data)) return data
    if (data && Array.isArray(data.content)) return data.content
    return []
}

function truncate(text, max = 200) {
    if (!text) return ''
    if (text.length <= max) return text
    return text.slice(0, max) + '…'
}

function isTutorialDtoLike(item) {
    return !!(item && typeof item === 'object' && item.content && typeof item.content === 'object')
}

function normalizeFromDto(dto, listItem) {
    const id = listItem?.id ?? dto?.content?.id ?? dto?.id ?? null

    const fromList = (listItem?.title || '').trim()
    const fromDtoTitle = (dto?.title || '').trim()
    const fromContent = (dto?.content?.content || '').split('\n')[0].trim()
    const title = truncate(fromList || fromDtoTitle || fromContent || 'Poradnik wędkarski', 80)

    const attached = Array.isArray(dto?.content?.attachedPhotos) ? dto.content.attachedPhotos : []
    const thumbnailPath = attached.length ? attached[0] : null

    return {
        id,
        title,
        author: dto?.content?.author || null,
        authorUsername: dto?.content?.author?.username || 'nieznany',
        methods: Array.isArray(dto?.methods) ? dto.methods : [],
        fishMentioned: Array.isArray(dto?.fishMentioned) ? dto.fishMentioned : [],
        rating: listItem?.rating ?? dto?.content?.rating ?? dto?.rating ?? null,
        snippet: dto?.content?.content || '',
        content: dto?.content?.content || '',
        attachedPhotos: attached,
        thumbnailPath,
        raw: dto,
    }
}

function normalizeFromListOnly(listItem) {
    const title = (listItem?.title || '').trim() || 'Poradnik wędkarski'
    return {
        id: listItem?.id ?? null,
        title: truncate(title, 80),
        author: null,
        authorUsername: 'nieznany',
        methods: [],
        fishMentioned: [],
        rating: listItem?.rating ?? null,
        snippet: '',
        content: '',
        attachedPhotos: [],
        thumbnailPath: null,
        raw: listItem,
    }
}

export const useGuidesStore = defineStore('guides', {
    state: () => ({
        tutorials: [],
        loading: false,
        error: null,
        lastFilter: { method: '', fishId: null },

        unverifiedTutorials: [],
        unverifiedLoading: false,
        unverifiedError: null,
    }),

    actions: {
        async loadTutorials({ method = '', fishId = null } = {}) {
            this.loading = true
            this.error = null
            this.tutorials = []
            this.lastFilter = { method: method || '', fishId: fishId ?? null }

            try {
                if (fishId != null) {
                    const resp = await apiClient.get('/tutorials/fish', { params: { fishId } })
                    const list = mapResponseToList(resp.data)
                    this.tutorials = list.map(item =>
                        isTutorialDtoLike(item) ? normalizeFromDto(item, null) : normalizeFromListOnly(item)
                    )
                    return
                }

                if (method) {
                    const resp = await apiClient.get('/tutorials/method', { params: { method } })
                    const list = mapResponseToList(resp.data)
                    this.tutorials = list.map(item =>
                        isTutorialDtoLike(item) ? normalizeFromDto(item, null) : normalizeFromListOnly(item)
                    )
                    return
                }

                const resp = await apiClient.get('/tutorials')
                const baseList = mapResponseToList(resp.data)

                const detailPromises = baseList.map(item =>
                    apiClient
                        .get(`/tutorials/${item.id}`)
                        .then(r => normalizeFromDto(r.data, item))
                        .catch(() => normalizeFromListOnly(item))
                )

                this.tutorials = await Promise.all(detailPromises)
            } catch (e) {
                console.error('loadTutorials error', e)
                this.error = 'Nie udało się pobrać poradników. Spróbuj ponownie później.'
            } finally {
                this.loading = false
            }
        },

        async loadTutorialDetail(id) {
            if (!id) throw new Error('Brak id poradnika')
            const resp = await apiClient.get(`/tutorials/${id}`)
            return resp.data
        },

        async deleteTutorial(id) {
            if (!id) throw new Error('Brak id poradnika')
            await apiClient.delete(`/tutorials/${id}`)
        },

        async createTutorial({ title, content, photos } = {}) {
            const fd = new FormData()
            fd.append('title', title || '')
            fd.append('content', content || '')

            const files = Array.isArray(photos) ? photos : []
            for (const photo of files) {
                fd.append('photos', photo)
            }

            await apiClient.post('/tutorials/create', fd, {
                headers: { 'Content-Type': 'multipart/form-data' },
            })
        },

        async loadUnverifiedTutorials() {
            this.unverifiedLoading = true
            this.unverifiedError = null
            this.unverifiedTutorials = []

            try {
                const resp = await apiClient.get('/admin/tutorials/unverified')
                const list = mapResponseToList(resp.data)
                this.unverifiedTutorials = list
            } catch (e) {
                console.error('loadUnverifiedTutorials error', e)
                this.unverifiedError = 'Nie udało się pobrać poradników do moderacji.'
            } finally {
                this.unverifiedLoading = false
            }
        },

        async acceptTutorial(id) {
            if (!id) throw new Error('Brak id poradnika')
            await apiClient.patch(`/admin/tutorials/${id}/accept`)
        },

        async rejectTutorial(id, rejectReason) {
            if (!id) throw new Error('Brak id poradnika')
            await apiClient.patch(`/admin/tutorials/${id}/reject`, {
                rejectReason: rejectReason || '',
            })
        },
    },
})
