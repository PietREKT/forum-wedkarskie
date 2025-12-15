import { defineStore } from 'pinia'
import { apiClient } from '../utils/axios.js'
import { useAuthStore } from './auth.js'

const EVENT_EXPIRE_HOURS = 12

function formatDateTime(isoString) {
    if (!isoString) return ''
    const d = new Date(isoString)
    if (Number.isNaN(d.getTime())) return ''
    const pad = n => String(n).padStart(2, '0')
    return `${pad(d.getDate())}.${pad(d.getMonth() + 1)}.${d.getFullYear()}, ${pad(d.getHours())}:${pad(
        d.getMinutes(),
    )}`
}

function makeInitials(user) {
    if (!user) return ''
    if (user.username) {
        const u = String(user.username)
        if (u.length >= 2) return u.slice(0, 2).toUpperCase()
        return u.toUpperCase()
    }
    const parts = []
    if (user.name) parts.push(String(user.name)[0])
    if (user.surname) parts.push(String(user.surname)[0])
    return parts.join('').toUpperCase()
}

function pageContent(data) {
    if (!data) return []
    if (Array.isArray(data)) return data
    if (Array.isArray(data.content)) return data.content
    return []
}

function mapUserDto(dto) {
    if (!dto) return null
    return {
        id: dto.id,
        username: dto.username,
        name: dto.name,
        surname: dto.surname,
        initials: makeInitials(dto),
    }
}

function mapSpotListDto(dto) {
    if (!dto) return null
    return {
        id: dto.id,
        name: dto.name,
        locationX: dto.locationX,
        locationY: dto.locationY,
        type: dto.type,
        avgRating: dto.avgRating,
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

function mapGroupDetailsDto(dto) {
    if (!dto) return null

    const admins = Array.isArray(dto.admins) ? dto.admins : []
    const adminsIds = new Set(admins.map(a => String(a.id)))

    const ownerId = dto.owner?.id ?? dto.ownerId ?? null

    const membersRaw = Array.isArray(dto.members) ? dto.members : []
    const members = membersRaw.map(m => ({
        id: m.id,
        username: m.username,
        name: m.name,
        surname: m.surname,
        initials: makeInitials(m),
        isAdmin: adminsIds.has(String(m.id)),
        isOwner: ownerId != null && String(ownerId) === String(m.id),
    }))

    return {
        id: dto.id,
        name: dto.name,
        members,
        admins: admins.map(a => ({
            id: a.id,
            username: a.username,
            name: a.name,
            surname: a.surname,
        })),
        owner: dto.owner || (ownerId ? { id: ownerId } : null),
        // opcjonalnie jeśli backend kiedyś doda:
        myRole: dto.myRole ?? null,
    }
}

function canManageGroup(groupDetails, myId) {
    if (!groupDetails || !myId) return false

    // jeśli backend zwróci myRole, to to jest najbardziej wiarygodne
    if (groupDetails.myRole) {
        const r = String(groupDetails.myRole).toUpperCase()
        return r === 'OWNER' || r === 'ADMIN'
    }

    const ownerId = groupDetails.owner?.id ?? null
    if (ownerId && String(ownerId) === String(myId)) return true

    const admins = Array.isArray(groupDetails.admins) ? groupDetails.admins : []
    return admins.some(a => String(a.id) === String(myId))
}

function mapEventDto(dto, meId) {
    if (!dto) return null

    const spot = dto.location || {}
    const creator = dto.creator || {}
    const group = dto.group || {}
    const participantsRaw = Array.isArray(dto.userEvents) ? dto.userEvents : []

    const participants = participantsRaw.map(p => {
        const u = p.user || {}
        return { id: u.id, initials: makeInitials(u) }
    })

    const creatorId = creator.id ?? null
    const isOwner = !!(meId && creatorId && String(meId) === String(creatorId))
    const isParticipating = dto.isParticipating != null ? !!dto.isParticipating : !!dto.participating

    const startsAt = dto.startsAt || null
    const t = startsAt ? Date.parse(startsAt) : NaN
    const now = Date.now()
    const isPast = Number.isFinite(t) ? t < now : false
    const isExpired = Number.isFinite(t) ? (t + EVENT_EXPIRE_HOURS * 60 * 60 * 1000) < now : false

    return {
        id: dto.id,
        name: dto.name,
        description: dto.description,
        dateTime: startsAt,
        dateLabel: formatDateTime(startsAt),

        spotId: spot.id ?? null,
        spotName: spot.name || 'Brak łowiska',

        organizerId: creatorId,
        organizer: creator.username || [creator.name, creator.surname].filter(Boolean).join(' ') || 'Organizator',

        participants,

        isPrivate: !!dto.group,
        isParticipating,

        groupId: group.id ?? null,
        groupName: group.name || null,

        isOwner,

        // UI logika czasu (12h po starcie jeszcze widoczne jako "odbyło się")
        isPast,
        isExpired,
    }
}

function localDateTimeToIsoZ(localStr) {
    const s = String(localStr || '').trim()
    if (!s) return null
    const d = new Date(s)
    if (Number.isNaN(d.getTime())) return null
    return d.toISOString()
}

function uniqById(list) {
    const map = new Map()
    for (const item of list || []) {
        if (!item) continue
        const k = String(item.id)
        if (!map.has(k)) map.set(k, item)
    }
    return Array.from(map.values())
}

export const useEventsStore = defineStore('events', {
    state: () => ({
        events: [],
        selectedEventId: null,
        isLoadingEvents: false,
        isSavingEvent: false,
        isJoiningEvent: false,

        groups: [],
        groupsMine: [],
        _myGroupIds: null,

        // mapa: groupId -> canManage (OWNER/ADMIN)
        groupManageMap: {},

        spots: [],
        invitations: [],

        isLoadingGroups: false,
        isSearchingGroups: false,
        isLoadingSpots: false,
        isLoadingInvitations: false,

        groupDetails: null,
        groupCandidates: [],

        isLoadingGroupDetails: false,
        isLoadingGroupCandidates: false,
        isCreatingGroup: false,
        isKickingMember: false,
        isAcceptingCandidate: false,
        isRejectingCandidate: false,

        lastGroupAction: null,
    }),

    getters: {
        currentEvent(state) {
            return state.events.find(e => String(e.id) === String(state.selectedEventId)) || null
        },
        pendingGroupIds(state) {
            return new Set((state.invitations || []).map(i => String(i.groupId)))
        },
        canManageSelectedGroup(state) {
            const auth = useAuthStore()
            const myId = auth.user?.id
            return canManageGroup(state.groupDetails, myId)
        },
        // do formularza: lista groupId, gdzie user jest OWNER/ADMIN (zadziała jak backend zacznie zwracać owner/admin/myRole)
        manageableGroupIds(state) {
            const ids = []
            for (const [gid, val] of Object.entries(state.groupManageMap || {})) {
                if (val) ids.push(gid)
            }
            return new Set(ids.map(String))
        },
    },

    actions: {
        hardReset() {
            this.events = []
            this.selectedEventId = null

            this.groups = []
            this.groupsMine = []
            this._myGroupIds = null
            this.groupManageMap = {}

            this.spots = []
            this.invitations = []

            this.groupDetails = null
            this.groupCandidates = []

            this.isLoadingEvents = false
            this.isSavingEvent = false
            this.isJoiningEvent = false

            this.isLoadingGroups = false
            this.isSearchingGroups = false
            this.isLoadingSpots = false
            this.isLoadingInvitations = false

            this.isLoadingGroupDetails = false
            this.isLoadingGroupCandidates = false
            this.isCreatingGroup = false
            this.isKickingMember = false
            this.isAcceptingCandidate = false
            this.isRejectingCandidate = false

            this.lastGroupAction = null
        },

        selectEvent(id) {
            this.selectedEventId = id
        },

        setGroupAction(type, message) {
            this.lastGroupAction = { type, message, ts: Date.now() }
        },

        async fetchEvents() {
            this.isLoadingEvents = true
            try {
                const auth = useAuthStore()
                const meId = auth.user?.id ?? null

                const upcomingReq = apiClient.get('/users/me/events/upcoming', { params: { page: 0, size: 50 } })
                const groupsReq = apiClient.get('/users/me/groups', { params: { page: 0, size: 200 } })

                const [upcomingRes, groupsRes] = await Promise.all([upcomingReq, groupsReq])

                const upcoming = pageContent(upcomingRes.data).map(dto => mapEventDto(dto, meId)).filter(Boolean)

                const myGroupsRaw = pageContent(groupsRes.data)
                const myGroupIds = myGroupsRaw.map(g => g?.id).filter(Boolean)
                this._myGroupIds = new Set(myGroupIds.map(id => String(id)))

                const groupEventsAll = []
                await Promise.all(
                    myGroupIds.map(async gid => {
                        try {
                            const { data } = await apiClient.get(`/users/groups/${gid}/events`, {
                                params: { page: 0, size: 50 },
                            })
                            const list = pageContent(data).map(dto => mapEventDto(dto, meId)).filter(Boolean)
                            groupEventsAll.push(...list)
                        } catch (e) {
                            console.error('fetch group events error', gid, e)
                        }
                    }),
                )

                const merged = uniqById([...upcoming, ...groupEventsAll])

                // filtr 12h po starcie (po tym czasie nie pokazujemy)
                const list = merged.filter(e => !e.isExpired)

                // sort po starcie rosnąco (braki daty na końcu)
                list.sort((a, b) => {
                    const ta = a?.dateTime ? Date.parse(a.dateTime) : Number.POSITIVE_INFINITY
                    const tb = b?.dateTime ? Date.parse(b.dateTime) : Number.POSITIVE_INFINITY
                    return ta - tb
                })

                this.events = list
                if (!this.events.find(e => String(e.id) === String(this.selectedEventId))) {
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
            try {
                const startsAtIso = localDateTimeToIsoZ(payload.dateTime)
                const spotId = payload.spotId ? Number(payload.spotId) : null

                const body = {
                    name: String(payload.name || '').trim(),
                    description: String(payload.description || '').trim(),
                    startsAt: startsAtIso,
                    endsAt: null,
                    locationId: Number.isFinite(spotId) ? spotId : null,
                    groupId: payload.groupId || null,
                    invitedUsersIds: [],
                }

                const { data } = await apiClient.post('/events', body)

                const auth = useAuthStore()
                const meId = auth.user?.id ?? null
                const event = mapEventDto(data, meId)

                if (event && !event.isExpired) {
                    this.events = uniqById([...(this.events || []), event])
                    this.selectedEventId = event.id
                }
            } catch (err) {
                console.error('createEvent error', err)
            } finally {
                this.isSavingEvent = false
            }
        },

        async joinEvent(eventId) {
            if (!eventId) return
            this.isJoiningEvent = true
            const auth = useAuthStore()
            const user = auth.user
            const userId = user?.id

            const ev = this.events.find(e => String(e.id) === String(eventId))
            const prev = ev ? { ...ev, participants: Array.isArray(ev.participants) ? [...ev.participants] : [] } : null

            try {
                if (ev && userId && !ev.participants.some(p => String(p.id) === String(userId))) {
                    ev.participants.push({ id: userId, initials: makeInitials(user) })
                    ev.isParticipating = true
                }
                await apiClient.post(`/events/${eventId}/response`, { status: 'CONFIRMED' })
            } catch (err) {
                if (prev && ev) {
                    ev.participants = prev.participants
                    ev.isParticipating = prev.isParticipating
                }
                console.error('joinEvent error', err)
            } finally {
                this.isJoiningEvent = false
            }
        },

        async leaveEvent(eventId) {
            if (!eventId) return
            this.isJoiningEvent = true
            const auth = useAuthStore()
            const userId = auth.user?.id

            const ev = this.events.find(e => String(e.id) === String(eventId))
            const prev = ev ? { ...ev, participants: Array.isArray(ev.participants) ? [...ev.participants] : [] } : null

            try {
                if (ev && userId) {
                    ev.participants = ev.participants.filter(p => String(p.id) !== String(userId))
                    ev.isParticipating = false
                }
                // poprawny endpoint dla wyjścia z wydarzenia
                await apiClient.delete(`/events/${eventId}/leave`)
            } catch (err) {
                if (prev && ev) {
                    ev.participants = prev.participants
                    ev.isParticipating = prev.isParticipating
                }
                console.error('leaveEvent error', err)
            } finally {
                this.isJoiningEvent = false
            }
        },

        async updateEvent(eventId, payload) {
            if (!eventId) return
            try {
                const startsAtIso = payload.dateTime != null ? localDateTimeToIsoZ(payload.dateTime) : undefined
                const spotId = payload.spotId != null && payload.spotId !== '' ? Number(payload.spotId) : undefined

                const body = {
                    name: payload.name ?? undefined,
                    description: payload.description ?? undefined,
                    startsAt: startsAtIso,
                    endsAt: payload.endsAt ?? undefined,
                    locationId: Number.isFinite(spotId) ? spotId : undefined,
                }

                await apiClient.patch(`/events/${eventId}`, body)

                const ev = this.events.find(e => String(e.id) === String(eventId))
                if (!ev) return

                if (payload.name != null) ev.name = payload.name
                if (payload.description != null) ev.description = payload.description
                if (payload.dateTime != null) {
                    ev.dateTime = startsAtIso
                    ev.dateLabel = formatDateTime(startsAtIso)

                    const t = startsAtIso ? Date.parse(startsAtIso) : NaN
                    const now = Date.now()
                    ev.isPast = Number.isFinite(t) ? t < now : false
                    ev.isExpired = Number.isFinite(t) ? (t + EVENT_EXPIRE_HOURS * 60 * 60 * 1000) < now : false
                }
                if (payload.spotId != null) {
                    ev.spotId = Number(payload.spotId) || null
                    const spot = this.spots.find(s => String(s.id) === String(ev.spotId)) || null
                    ev.spotName = spot ? spot.name : 'Brak łowiska'
                }

                // jeśli po edycji wydarzenie wypadło poza okno 12h, usuń z listy
                if (ev.isExpired) {
                    this.events = (this.events || []).filter(e => String(e.id) !== String(eventId))
                    if (String(this.selectedEventId) === String(eventId)) {
                        this.selectedEventId = this.events[0]?.id ?? null
                    }
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

            this.events = (this.events || []).filter(e => String(e.id) !== String(eventId))
            if (String(this.selectedEventId) === String(eventId)) {
                this.selectedEventId = this.events[0]?.id ?? null
            }
        },

        async fetchInvitations() {
            this.isLoadingInvitations = true
            try {
                const { data } = await apiClient.get('/users/me/groups/invites', { params: { page: 0, size: 50 } })
                this.invitations = pageContent(data).map(mapInviteDto).filter(Boolean)
            } catch (err) {
                console.error('fetchInvitations error', err)
            } finally {
                this.isLoadingInvitations = false
            }
        },

        async fetchGroups() {
            this.isLoadingGroups = true
            try {
                await this.fetchInvitations()

                const { data } = await apiClient.get('/users/me/groups', { params: { page: 0, size: 200 } })
                const myListRaw = pageContent(data)
                const myIds = new Set(myListRaw.map(g => String(g.id)))
                this._myGroupIds = myIds

                const pending = this.pendingGroupIds

                this.groupsMine = myListRaw
                    .map(dto => ({
                        id: dto.id,
                        name: dto.name,
                        members: dto.memberCount ?? 0,
                        isMine: true,
                        isPending: false,
                    }))
                    .filter(Boolean)

                const merged = [...this.groupsMine]
                for (const inv of this.invitations || []) {
                    const gid = String(inv.groupId)
                    if (!merged.some(x => String(x.id) === gid)) {
                        merged.push({
                            id: inv.groupId,
                            name: inv.groupName,
                            members: inv.members ?? 0,
                            isMine: false,
                            isPending: true,
                        })
                    }
                }

                this.groups = merged.map(g => ({
                    ...g,
                    isPending: g.isMine ? false : pending.has(String(g.id)),
                }))

                // dociągnięcie ról dla grup (zadziała dopiero jak backend zwróci owner/admin/myRole)
                await this.refreshGroupManageMap()
            } catch (err) {
                console.error('fetchGroups error', err)
            } finally {
                this.isLoadingGroups = false
            }
        },

        async refreshGroupManageMap() {
            const auth = useAuthStore()
            const myId = auth.user?.id
            if (!myId) {
                this.groupManageMap = {}
                return
            }

            const groups = Array.isArray(this.groupsMine) ? this.groupsMine : []
            const ids = groups.map(g => g?.id).filter(Boolean)

            const map = {}
            await Promise.all(
                ids.map(async gid => {
                    try {
                        const { data } = await apiClient.get(`/users/groups/${gid}`)
                        const details = mapGroupDetailsDto(data)
                        map[String(gid)] = canManageGroup(details, myId)
                    } catch (e) {
                        map[String(gid)] = false
                    }
                }),
            )

            this.groupManageMap = map
        },

        async searchGroups(query) {
            const q = String(query || '').trim()
            if (!q) {
                await this.fetchGroups()
                return
            }

            this.isSearchingGroups = true
            try {
                await this.fetchInvitations()

                const { data } = await apiClient.get('/users/groups/search', { params: { q } })
                const raw = pageContent(data)

                const myIds = this._myGroupIds || new Set(this.groupsMine.map(g => String(g.id)))
                const pending = this.pendingGroupIds

                const searched = raw
                    .map(dto => ({
                        id: dto.id,
                        name: dto.name,
                        members: dto.memberCount ?? 0,
                        isMine: myIds.has(String(dto.id)),
                        isPending: pending.has(String(dto.id)),
                    }))
                    .filter(Boolean)

                const merged = [...this.groupsMine]

                for (const inv of this.invitations || []) {
                    const gid = String(inv.groupId)
                    if (!merged.some(x => String(x.id) === gid)) {
                        merged.push({
                            id: inv.groupId,
                            name: inv.groupName,
                            members: inv.members ?? 0,
                            isMine: false,
                            isPending: true,
                        })
                    }
                }

                for (const g of searched) {
                    const idx = merged.findIndex(x => String(x.id) === String(g.id))
                    if (idx === -1) merged.push(g)
                    else merged[idx] = { ...merged[idx], ...g }
                }

                this.groups = merged
            } catch (err) {
                console.error('searchGroups error', err)
            } finally {
                this.isSearchingGroups = false
            }
        },

        async createGroup(name) {
            const trimmed = String(name || '').trim()
            if (!trimmed) throw new Error('Nazwa grupy jest wymagana.')

            const auth = useAuthStore()
            const meId = auth.user?.id
            if (!meId) throw new Error('Brak użytkownika.')

            this.isCreatingGroup = true
            try {
                const { data } = await apiClient.post('/users/groups/create', {
                    name: trimmed,
                    members: [{ id: meId }],
                })

                this.groupDetails = mapGroupDetailsDto(data)
                this.setGroupAction('success', 'Utworzono grupę.')
                await this.fetchGroups()
                return this.groupDetails
            } catch (err) {
                console.error('createGroup error', err)
                this.setGroupAction('error', 'Nie udało się utworzyć grupy.')
                throw err
            } finally {
                this.isCreatingGroup = false
            }
        },

        async requestJoinGroup(groupId) {
            const auth = useAuthStore()
            const userId = auth.user?.id
            if (!groupId || !userId) return

            const gid = String(groupId)
            if (this._myGroupIds && this._myGroupIds.has(gid)) return
            if (this.pendingGroupIds.has(gid)) return

            try {
                await apiClient.patch(`/users/groups/${groupId}/invite`, { userId })
                await this.fetchInvitations()
                const pending = this.pendingGroupIds

                this.groups = (this.groups || []).map(g => ({
                    ...g,
                    isPending: g.isMine ? false : pending.has(String(g.id)),
                }))

                this.setGroupAction('success', 'Wysłano prośbę o dołączenie.')
            } catch (err) {
                console.error('requestJoinGroup error', err)
                this.setGroupAction('error', 'Nie udało się wysłać prośby.')
            }
        },

        async loadGroupDetails(groupId) {
            if (!groupId) return
            this.isLoadingGroupDetails = true
            try {
                const { data } = await apiClient.get(`/users/groups/${groupId}`)
                this.groupDetails = mapGroupDetailsDto(data)

                // jeżeli to jest moja grupa, aktualizuj mapę uprawnień dla tej jednej
                const auth = useAuthStore()
                const myId = auth.user?.id
                if (myId && this.groupDetails?.id != null) {
                    this.groupManageMap = {
                        ...(this.groupManageMap || {}),
                        [String(this.groupDetails.id)]: canManageGroup(this.groupDetails, myId),
                    }
                }
            } catch (err) {
                console.error('loadGroupDetails error', err)
                this.groupDetails = null
            } finally {
                this.isLoadingGroupDetails = false
            }
        },

        async loadGroupCandidates(groupId) {
            if (!groupId) return
            this.isLoadingGroupCandidates = true
            try {
                const { data } = await apiClient.get(`/users/groups/${groupId}/admin/candidates`, {
                    params: { page: 0, size: 100 },
                })

                const auth = useAuthStore()
                const myId = String(auth.user?.id)

                const memberIds = new Set((this.groupDetails?.members || []).map(m => String(m.id)))

                this.groupCandidates = pageContent(data)
                    .map(mapUserDto)
                    .filter(Boolean)
                    .filter(u => !memberIds.has(String(u.id)) && String(u.id) !== myId)
            } catch (err) {
                console.error('loadGroupCandidates error', err)
                this.groupCandidates = []
            } finally {
                this.isLoadingGroupCandidates = false
            }
        },

        async acceptCandidate(groupId, userId) {
            if (!groupId || !userId) return
            this.isAcceptingCandidate = true
            try {
                await apiClient.post(`/users/groups/${groupId}/admin/candidates/accept`, { userId })
                this.setGroupAction('success', 'Zaakceptowano kandydata.')
            } catch (err) {
                console.error('acceptCandidate error', err)
                this.setGroupAction('error', 'Nie udało się zaakceptować kandydata.')
            } finally {
                this.isAcceptingCandidate = false
            }
        },

        async rejectCandidate(groupId, userId) {
            if (!groupId || !userId) return
            this.isRejectingCandidate = true
            try {
                await apiClient.post(`/users/groups/${groupId}/admin/candidates/reject`, { userId })
                this.setGroupAction('success', 'Odrzucono kandydata.')
            } catch (err) {
                console.error('rejectCandidate error', err)
                this.setGroupAction('error', 'Nie udało się odrzucić kandydata.')
            } finally {
                this.isRejectingCandidate = false
            }
        },

        async kickMember(groupId, userId) {
            if (!groupId || !userId) return
            this.isKickingMember = true
            try {
                await apiClient.post(`/users/groups/${groupId}/admin/kick`, { userId })
                this.setGroupAction('success', 'Usunięto członka z grupy.')
            } catch (err) {
                console.error('kickMember error', err)
                this.setGroupAction('error', 'Nie udało się usunąć członka.')
            } finally {
                this.isKickingMember = false
            }
        },

        async fetchSpots() {
            this.isLoadingSpots = true
            try {
                const { data } = await apiClient.get('/spots', { params: { page: 0, size: 200 } })
                this.spots = pageContent(data).map(mapSpotListDto).filter(Boolean)
            } catch (err) {
                console.error('fetchSpots error', err)
                this.spots = []
            } finally {
                this.isLoadingSpots = false
            }
        },
    },
})
