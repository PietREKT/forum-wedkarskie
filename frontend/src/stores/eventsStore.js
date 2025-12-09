import { defineStore } from 'pinia'
import { apiClient } from '../utils/axios.js'
import { useAuthStore } from './auth.js'

function formatDateTime(isoString) {
    if (!isoString) return ''
    const d = new Date(isoString)
    if (Number.isNaN(d.getTime())) return ''
    const pad = n => String(n).padStart(2, '0')
    return `${pad(d.getDate())}.${pad(d.getMonth() + 1)}.${d.getFullYear()}, ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function makeInitials(user) {
    if (!user) return ''
    if (user.username) {
        const u = String(user.username)
        if (u.length >= 2) return u.slice(0, 2).toUpperCase()
        return u.toUpperCase()
    }
    const parts = []
    if (user.name) parts.push(user.name[0])
    if (user.surname) parts.push(user.surname[0])
    return parts.join('').toUpperCase()
}

function pageContent(data) {
    if (!data) return []
    if (Array.isArray(data)) return data
    if (Array.isArray(data.content)) return data.content
    return []
}

// mapowanie eventu z backendu
function mapEventDto(dto) {
    if (!dto) return null

    const spot = dto.location || {}
    const creator = dto.creator || {}
    const group = dto.group || {}
    const participantsRaw = Array.isArray(dto.userEvents) ? dto.userEvents : []

    const participants = participantsRaw.map(p => {
        const u = p.user || {}
        return {
            id: u.id,
            initials: makeInitials(u),
        }
    })

    return {
        id: dto.id,
        name: dto.name,
        description: dto.description,
        type: 'TRIP',
        dateTime: dto.startsAt,
        dateLabel: formatDateTime(dto.startsAt),
        spotId: spot.id ?? null,
        spotName: spot.name || 'Brak łowiska',
        organizerId: creator.id,
        organizer:
            creator.username ||
            [creator.name, creator.surname].filter(Boolean).join(' ') ||
            'Organizator',
        participants,
        isPrivate: !!dto.group,
        isParticipating: !!dto.isParticipating,
        groupName: group.name || null,
    }
}

function mapGroupListDto(dto) {
    if (!dto) return null
    return {
        id: dto.id,
        name: dto.name,
        members: dto.memberCount ?? 0,
        // /users/me/groups zwraca ZAWSZE „moje” grupy
        isMine: true,
    }
}

function mapGroupDetailsDto(dto) {
    if (!dto) return null

    const admins = Array.isArray(dto.admins) ? dto.admins : []
    const adminsIds = new Set(admins.map(a => a.id))

    const membersRaw = Array.isArray(dto.members) ? dto.members : []
    const members = membersRaw.map(m => ({
        id: m.id,
        username: m.username,
        name: m.name,
        surname: m.surname,
        initials: makeInitials(m),
        isAdmin: adminsIds.has(m.id),
    }))

    return {
        id: dto.id,
        name: dto.name,
        members,
    }
}

function mapInviteDto(dto) {
    if (!dto) return null
    return {
        groupId: dto.id,
        groupName: dto.name,
        members: dto.memberCount ?? 0,
        status: 'PENDING',
    }
}

export const useEventsStore = defineStore('events', {
    state: () => ({
        // wydarzenia
        events: [],
        selectedEventId: null,
        isLoadingEvents: false,
        isSavingEvent: false,
        isJoiningEvent: false,

        // grupy / łowiska / powiadomienia
        groups: [],
        spots: [],
        invitations: [],
        isLoadingGroups: false,
        isLoadingSpots: false,
        isLoadingInvitations: false,

        // panel zarządzania grupą
        groupDetails: null,
        isLoadingGroupDetails: false,
        isKickingMember: false,
    }),

    getters: {
        currentEvent(state) {
            return state.events.find(e => e.id === state.selectedEventId) || null
        },
    },

    actions: {
        selectEvent(id) {
            this.selectedEventId = id
        },

        // ===== WYDARZENIA =====

        async fetchEvents() {
            this.isLoadingEvents = true
            try {
                const { data } = await apiClient.get('/users/me/events/upcoming', {
                    params: { page: 0, size: 50 },
                })

                const now = Date.now()
                const list = pageContent(data)
                    .map(mapEventDto)
                    .filter(Boolean)
                    // usuwanie eventu po 24h
                    .filter(e => {
                        if (!e.dateTime) return true
                        const t = Date.parse(e.dateTime)
                        if (Number.isNaN(t)) return true
                        const expire = t + 24 * 60 * 60 * 1000
                        return expire >= now
                    })

                this.events = list

                if (!this.events.find(e => e.id === this.selectedEventId)) {
                    this.selectedEventId = this.events[0]?.id ?? null
                }
            } catch (err) {
                console.error('fetchEvents error', err)
            } finally {
                this.isLoadingEvents = false
            }
        },

        async createEvent(payload) {
            this.isSavingEvent = true
            const auth = useAuthStore()
            const user = auth.user

            try {
                const body = {
                    name: payload.name || '',
                    description: payload.description || '',
                    startsAt: payload.dateTime || null,
                    endsAt: null,
                    locationId: payload.spotId || null,
                    groupId: payload.groupId || null,
                    invitedUsersIds: [],
                }

                const { data } = await apiClient.post('/events', body)
                const event = mapEventDto(data)
                if (event) {
                    this.events.push(event)
                    this.selectedEventId = event.id
                    return
                }
            } catch (err) {
                console.error('createEvent error', err)

                // fallback lokalny – żeby coś działało nawet przy 500
                const localId =
                    (this.events.reduce((max, e) => Math.max(max, Number(e.id) || 0), 0) || 0) + 1

                const spot = this.spots.find(s => s.id === payload.spotId) || null
                const group = this.groups.find(g => g.id === payload.groupId) || null

                const event = {
                    id: localId,
                    name: payload.name || 'Nowe wydarzenie',
                    description: payload.description || '',
                    type: payload.type || 'TRIP',
                    dateTime: payload.dateTime || '',
                    dateLabel: formatDateTime(payload.dateTime),
                    spotId: payload.spotId || null,
                    spotName: spot ? spot.name : 'Brak łowiska',
                    organizerId: user?.id ?? null,
                    organizer:
                        user?.username ||
                        [user?.name, user?.surname].filter(Boolean).join(' ') ||
                        'Ty',
                    participants: user
                        ? [{ id: user.id, initials: makeInitials(user) }]
                        : [],
                    isPrivate: !!payload.groupId,
                    isParticipating: true,
                    groupName: group ? group.name : null,
                }

                this.events.push(event)
                this.selectedEventId = event.id
            } finally {
                this.isSavingEvent = false
            }
        },

        async joinEvent(eventId) {
            if (!eventId) return
            this.isJoiningEvent = true
            const auth = useAuthStore()
            const user = auth.user

            try {
                // optymistycznie zaktualizuj UI
                const ev = this.events.find(e => e.id === eventId)
                if (ev && user && !ev.participants.some(p => p.id === user.id)) {
                    ev.participants.push({ id: user.id, initials: makeInitials(user) })
                    ev.isParticipating = true
                }

                await apiClient.post(`/events/${eventId}/response`, {
                    status: 'CONFIRMED',
                })
            } catch (err) {
                console.error('joinEvent error', err)
            } finally {
                this.isJoiningEvent = false
            }
        },

        async leaveEvent(eventId) {
            if (!eventId) return
            this.isJoiningEvent = true
            const auth = useAuthStore()
            const user = auth.user
            const userId = user?.id

            try {
                const ev = this.events.find(e => e.id === eventId)
                if (ev && userId) {
                    ev.participants = ev.participants.filter(p => p.id !== userId)
                    ev.isParticipating = false
                }

                if (userId) {
                    await apiClient.delete(`/events/${eventId}/participants/${userId}`)
                }
            } catch (err) {
                console.error('leaveEvent error', err)
            } finally {
                this.isJoiningEvent = false
            }
        },

        async updateEvent(eventId, payload) {
            if (!eventId) return
            try {
                const body = {
                    name: payload.name ?? undefined,
                    description: payload.description ?? undefined,
                    startsAt: payload.dateTime ?? undefined,
                    endsAt: undefined,
                    locationId: payload.spotId ?? undefined,
                    groupId: payload.groupId ?? undefined,
                }

                await apiClient.patch(`/events/${eventId}`, body)

                const ev = this.events.find(e => e.id === eventId)
                if (!ev) return
                if (payload.name != null) ev.name = payload.name
                if (payload.description != null) ev.description = payload.description
                if (payload.dateTime != null) {
                    ev.dateTime = payload.dateTime
                    ev.dateLabel = formatDateTime(payload.dateTime)
                }
                if (payload.spotId != null) {
                    ev.spotId = payload.spotId
                    const spot = this.spots.find(s => s.id === payload.spotId) || null
                    ev.spotName = spot ? spot.name : 'Brak łowiska'
                }
            } catch (err) {
                console.error('updateEvent error', err)
            }
        },

        async deleteEvent(eventId) {
            if (!eventId) return
            try {
                await apiClient.delete(`/events/${eventId}`)
            } catch (err) {
                console.error('deleteEvent error', err)
            }
            this.events = this.events.filter(e => e.id !== eventId)
            if (this.selectedEventId === eventId) {
                this.selectedEventId = this.events[0]?.id ?? null
            }
        },

        // ===== GRUPY =====

        async fetchGroups() {
            this.isLoadingGroups = true
            try {
                const { data } = await apiClient.get('/users/me/groups', {
                    params: { page: 0, size: 50 },
                })
                const list = pageContent(data).map(mapGroupListDto).filter(Boolean)
                this.groups = list
            } catch (err) {
                console.error('fetchGroups error', err)
            } finally {
                this.isLoadingGroups = false
            }
        },

        async fetchInvitations() {
            this.isLoadingInvitations = true
            try {
                const { data } = await apiClient.get('/users/me/groups/invites', {
                    params: { page: 0, size: 50 },
                })
                const list = pageContent(data).map(mapInviteDto).filter(Boolean)
                this.invitations = list
            } catch (err) {
                console.error('fetchInvitations error', err)
            } finally {
                this.isLoadingInvitations = false
            }
        },

        async createGroup(name) {
            const trimmed = String(name || '').trim()
            if (!trimmed) throw new Error('Nazwa grupy jest wymagana.')

            const auth = useAuthStore()
            const user = auth.user

            try {
                const { data } = await apiClient.post('/users/groups/create', {
                    name: trimmed,
                    members: [],
                })
                const dto = mapGroupDetailsDto(data)
                const listItem = {
                    id: dto.id,
                    name: dto.name,
                    members: dto.members.length,
                    isMine: true,
                }
                this.groups.push(listItem)
                this.groupDetails = dto
                return dto
            } catch (err) {
                console.error('createGroup error', err)

                // fallback lokalny
                const localId =
                    (this.groups.reduce((max, g) => Math.max(max, Number(g.id) || 0), 0) || 0) + 1

                const listItem = {
                    id: localId,
                    name: trimmed,
                    members: 1,
                    isMine: true,
                }
                this.groups.push(listItem)

                this.groupDetails = {
                    id: localId,
                    name: trimmed,
                    members: user
                        ? [
                            {
                                id: user.id,
                                username: user.username,
                                name: user.name,
                                surname: user.surname,
                                initials: makeInitials(user),
                                isAdmin: true,
                            },
                        ]
                        : [],
                }

                return this.groupDetails
            }
        },

        async requestJoinGroup(groupId, userIdFromCaller) {
            const auth = useAuthStore()
            const userId = userIdFromCaller || auth.user?.id
            if (!groupId || !userId) return

            try {
                await apiClient.patch(`/users/groups/${groupId}/invite`, {
                    userId,
                })
                await this.fetchInvitations()
            } catch (err) {
                console.error('requestJoinGroup error', err)
            }
        },

        async loadGroupDetails(groupId) {
            if (!groupId) return
            this.isLoadingGroupDetails = true
            try {
                const { data } = await apiClient.get(`/users/groups/${groupId}`)
                this.groupDetails = mapGroupDetailsDto(data)
            } catch (err) {
                console.error('loadGroupDetails error', err)
                this.groupDetails = null
            } finally {
                this.isLoadingGroupDetails = false
            }
        },

        async kickMember(groupId, userId) {
            if (!groupId || !userId) return
            this.isKickingMember = true
            try {
                await apiClient.post(`/users/groups/${groupId}admin/kick`, {
                    userId,
                })
            } catch (err) {
                console.error('kickMember error', err)
            } finally {
                if (this.groupDetails && this.groupDetails.id === groupId) {
                    this.groupDetails = {
                        ...this.groupDetails,
                        members: this.groupDetails.members.filter(m => m.id !== userId),
                    }
                }
                this.isKickingMember = false
            }
        },

        // ===== ŁOWISKA =====

        async fetchSpots() {
            this.isLoadingSpots = true
            try {
                // backend jeszcze niegotowy – zostawiamy pustą listę
                this.spots = []
            } finally {
                this.isLoadingSpots = false
            }
        },
    },
})
