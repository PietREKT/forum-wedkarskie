/*
  TODO: DO WERSJI OSTATECZNEJ (po podpięciu backendu):

  1) Wydarzenia (events)
     - Podmienić wszystkie operacje na mockach na prawdziwe wywołania HTTP:
       • fetchEvents()     -> GET /api/events
       • createEvent(...)  -> POST /api/events
       • joinEvent(id)     -> PATCH /api/events/{id}/join
       • leaveEvent(id)    -> PATCH /api/events/{id}/leave
       • updateEvent(id, payload) -> PATCH /api/events/{id}
       • deleteEvent(id)   -> DELETE /api/events/{id}
     - Usunąć funkcje createInitialEvents() oraz resetAll() (zostaną tylko dane z backendu).
     - Upewnić się, że backend zwraca pola zgodne z tym, co wykorzystuje front:
       id, name, description, type, dateTime, spotId/spotName, organizer, participants[].
       Jeśli backend zwraca inne nazwy, dodać mapowanie odpowiedzi -> model w store.

  2) Łowiska (spots)
     - fetchSpots() podpiąć pod istniejący endpoint łowisk:
       • GET /spots (lub inny aktualny adres z backendu)
     - Usunąć createInitialSpots() po przejściu na realne dane.

  3) Grupy – lista i zaproszenia (pod panel z prawej)
     - Po stronie backendu dodać brakujące endpointy:
       • GET /api/users/groups/my           – lista grup zalogowanego użytkownika
       • GET /api/users/groups/invitations  – lista zaproszeń / zgłoszeń do grup
     - W store:
       • fetchGroups()        -> GET /api/users/groups/my
       • fetchInvitations()   -> GET /api/users/groups/invitations
       • requestJoinGroup(id) -> PATCH /api/users/groups/invite (ModifyMemberUserGroupDto)
       • createGroup()        -> POST  /api/users/groups/create (CreateUserGroupDto)
       • acceptInvitation(id) -> PATCH /api/users/groups/admin/candidates/accept
       • rejectInvitation(id) -> PATCH /api/users/groups/admin/candidates/reject
     - Usunąć createInitialGroups() oraz createInitialInvitations() po przejściu na backend.

  4) Aktualny użytkownik
     - Usunąć stałą CURRENT_USER z tego pliku.
     - Zamiast tego pobierać dane zalogowanego użytkownika z globalnego store (np. authStore)
       lub z JWT (id, username, inicjały) i używać ich przy:
       • tworzeniu wydarzenia,
       • dołączaniu do wydarzenia,
       • tworzeniu grupy.

  5) Porządki techniczne
     - Dodać realny klient HTTP (np. axios) i wstrzyknąć go tutaj lub korzystać z gotowego modułu apiClient.
     - Dodać obsługę błędów użytkownika (np. powiadomienia / toasty zamiast samego console.error).
     - Po wdrożeniu backendu usunąć wszystkie komentarze "tu kiedyś: ..." oraz pozostałe mocki,
       tak aby store był jedynym źródłem prawdziwych danych z API.

  6) Panel zarządzania grupą i członkami (osobny widok /groups, /groups/:id)
     - Backend ma już operacje na grupach:
       • GET    /api/users/groups/{groupId}                       – szczegóły grupy (owner, admins, members)
       • PATCH  /api/users/groups/admin/kick                      – wyrzucenie członka (admin)
       • PATCH  /api/users/groups/admin/{groupId}/resign          – rezygnacja admina
       • PATCH  /api/users/groups/owner/admins/add                – owner dodaje admina
       • PATCH  /api/users/groups/owner/admins/remove             – owner zabiera prawa admina
       • PATCH  /api/users/groups/owner/transfer                  – przekazanie własności grupy
       • DELETE /api/users/groups/owner/{groupId}                 – usunięcie grupy (owner)
     - W store dodać akcje pod panel zarządzania:
       • loadGroupDetails(groupId)    -> GET /api/users/groups/{groupId}
       • kickMember(groupId, userId)  -> PATCH /api/users/groups/admin/kick
       • addAdmin(groupId, userId)    -> PATCH /api/users/groups/owner/admins/add
       • removeAdmin(groupId, userId) -> PATCH /api/users/groups/owner/admins/remove
       • transferOwnership(groupId, userId) -> PATCH /api/users/groups/owner/transfer
       • deleteGroup(groupId)         -> DELETE /api/users/groups/owner/{groupId}
     - Na froncie:
       • dodać widok: GroupsView.vue (lista "moich" grup z /api/users/groups/my),
       • dodać widok: GroupDetailsView.vue (szczegóły jednej grupy, lista członków z akcjami:
         wyrzuć, nadaj/odbierz admina, przekaz właściciela, usuń grupę).
*/

import { defineStore } from 'pinia'

// tymczasowy "aktualny user" na potrzeby mocków
const CURRENT_USER = {
    id: 'me',
    initials: 'TY',
    name: 'Ty',
}

function formatDateTime(isoString) {
    if (!isoString) return ''
    const d = new Date(isoString)
    if (Number.isNaN(d.getTime())) return ''
    const pad = n => String(n).padStart(2, '0')
    return `${pad(d.getDate())}.${pad(d.getMonth() + 1)}.${d.getFullYear()}, ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// MOCKI STARTOWE – tylko po to, żeby UI żył bez backendu
function createInitialSpots() {
    return [
        { id: 1, name: 'Jezioro Dębowiec' },
        { id: 2, name: 'Jezioro Sosnowiec' },
    ]
}

function createInitialEvents(spots) {
    return [
        {
            id: 1,
            name: 'Wyjazd na jezioro Dębowiec',
            spotId: 1,
            spotName: spots.find(s => s.id === 1)?.name || 'Jezioro Dębowiec',
            dateTime: '2025-12-15T05:00',
            dateLabel: '15.12.2025, 05:00',
            organizerId: 'u1',
            organizer: 'Marek',
            description:
                'Spotkanie przy jeziorze od strony wschodniej około godziny 5:00. Łowimy szczupaka i sandacza.',
            participants: [
                { id: 'u1', initials: 'M' },
                { id: 'u2', initials: 'K' },
            ],
            isPrivate: true,
            type: 'TRIP',
        },
        {
            id: 2,
            name: 'Nocne łowienie – Jezioro Sosnowiec',
            spotId: 2,
            spotName: spots.find(s => s.id === 2)?.name || 'Jezioro Sosnowiec',
            dateTime: '2025-12-20T21:00',
            dateLabel: '20.12.2025, 21:00',
            organizerId: 'u3',
            organizer: 'Piotr',
            description:
                'Zbiórka przy parkingu od strony południowej około 21:00. Nocne łowienie z brzegu.',
            participants: [
                { id: 'u3', initials: 'P' },
                { id: 'u4', initials: 'A' },
            ],
            isPrivate: false,
            type: 'TRIP',
        },
    ]
}

function createInitialGroups() {
    return [
        { id: 1, name: 'Ekipa jeziorowa', admin: 'Marek', members: 8, ownerId: 'u1', isMine: false },
        { id: 2, name: 'Spinning na Wiśle', admin: 'Piotr', members: 5, ownerId: 'u3', isMine: false },
    ]
}

function createInitialInvitations() {
    return [
        {
            groupId: 1,
            groupName: 'Ekipa jeziorowa',
            status: 'PENDING',
        },
    ]
}

export const useEventsStore = defineStore('events', {
    state: () => {
        const spots = createInitialSpots()
        const events = createInitialEvents(spots)
        const groups = createInitialGroups()
        const invitations = createInitialInvitations()

        return {
            // wydarzenia
            events,
            selectedEventId: events[0]?.id ?? null,
            isLoadingEvents: false,
            isSavingEvent: false,
            isJoiningEvent: false,

            // grupy / łowiska / zaproszenia
            groups,
            spots,
            invitations,
            isLoadingGroups: false,
            isLoadingSpots: false,
        }
    },

    getters: {
        currentEvent(state) {
            return state.events.find(e => e.id === state.selectedEventId) || null
        },
    },

    actions: {
        // reset całości do mocków
        resetAll() {
            this.spots = createInitialSpots()
            this.events = createInitialEvents(this.spots)
            this.groups = createInitialGroups()
            this.invitations = createInitialInvitations()
            this.selectedEventId = this.events[0]?.id ?? null
        },

        selectEvent(id) {
            this.selectedEventId = id
        },

        // LISTA WYDARZEŃ – na razie tylko odświeża mocki
        async fetchEvents() {
            try {
                this.isLoadingEvents = true
                // tu kiedyś: GET /api/events
                this.events = createInitialEvents(this.spots)
                if (!this.events.find(e => e.id === this.selectedEventId)) {
                    this.selectedEventId = this.events[0]?.id ?? null
                }
            } finally {
                this.isLoadingEvents = false
            }
        },

        // ZAPIS NOWEGO WYDARZENIA – działa w pamięci
        async createEvent(payload) {
            try {
                this.isSavingEvent = true

                const newId =
                    (this.events.reduce((max, e) => Math.max(max, Number(e.id) || 0), 0) || 0) + 1

                const spot = this.spots.find(s => s.id === payload.spotId) || null

                const event = {
                    id: newId,
                    name: payload.name,
                    description: payload.description,
                    type: payload.type || 'TRIP',
                    dateTime: payload.dateTime || '',
                    dateLabel: formatDateTime(payload.dateTime),
                    spotId: payload.spotId || null,
                    spotName: spot ? spot.name : 'Nieznane łowisko',
                    organizerId: CURRENT_USER.id,
                    organizer: CURRENT_USER.name,
                    participants: [
                        {
                            id: CURRENT_USER.id,
                            initials: CURRENT_USER.initials,
                        },
                    ],
                    isPrivate: true,
                }

                // tu kiedyś: POST /api/events
                this.events.push(event)
                this.selectedEventId = event.id
            } finally {
                this.isSavingEvent = false
            }
        },

        // DOŁĄCZ – na razie tylko po stronie frontu
        async joinEvent(eventId) {
            try {
                this.isJoiningEvent = true
                const ev = this.events.find(e => e.id === eventId)
                if (!ev) return

                const already = ev.participants.some(p => p.id === CURRENT_USER.id)
                if (!already) {
                    ev.participants.push({
                        id: CURRENT_USER.id,
                        initials: CURRENT_USER.initials,
                    })
                }

                // tu kiedyś: PATCH /api/events/{id}/join
            } finally {
                this.isJoiningEvent = false
            }
        },

        // OPUŚĆ – też lokalnie
        async leaveEvent(eventId) {
            try {
                this.isJoiningEvent = true
                const ev = this.events.find(e => e.id === eventId)
                if (!ev) return

                ev.participants = ev.participants.filter(p => p.id !== CURRENT_USER.id)

                // tu kiedyś: PATCH /api/events/{id}/leave
            } finally {
                this.isJoiningEvent = false
            }
        },

        // EDYCJA – lokalnie; payload jak z formularza
        async updateEvent(eventId, payload) {
            const ev = this.events.find(e => e.id === eventId)
            if (!ev) return

            if (payload.name != null) ev.name = payload.name
            if (payload.description != null) ev.description = payload.description
            if (payload.type != null) ev.type = payload.type

            if (payload.dateTime != null) {
                ev.dateTime = payload.dateTime
                ev.dateLabel = formatDateTime(payload.dateTime)
            }

            if (payload.spotId != null) {
                ev.spotId = payload.spotId
                const spot = this.spots.find(s => s.id === payload.spotId) || null
                ev.spotName = spot ? spot.name : 'Nieznane łowisko'
            }

            // tu kiedyś: PATCH /api/events/{id}
        },

        // USUNIĘCIE – lokalnie
        async deleteEvent(eventId) {
            this.events = this.events.filter(e => e.id !== eventId)
            if (this.selectedEventId === eventId) {
                this.selectedEventId = this.events[0]?.id ?? null
            }
            // tu kiedyś: DELETE /api/events/{id}
        },

        // GRUPY – na razie tylko mocki

        async fetchGroups() {
            try {
                this.isLoadingGroups = true
                // tu kiedyś: GET /api/users/groups/my
                this.groups = createInitialGroups()
            } finally {
                this.isLoadingGroups = false
            }
        },

        async fetchSpots() {
            try {
                this.isLoadingSpots = true
                // tu kiedyś: GET /spots
                this.spots = createInitialSpots()
            } finally {
                this.isLoadingSpots = false
            }
        },

        async fetchInvitations() {
            // tu kiedyś: GET /api/users/groups/invitations
            this.invitations = createInitialInvitations()
        },

        async requestJoinGroup(groupId) {
            const group = this.groups.find(g => g.id === groupId)
            if (!group) return

            // nie wysyłamy próśb do własnej grupy / grupy gdzie jesteśmy adminem/ownerem
            if (group.isMine || group.ownerId === CURRENT_USER.id || group.admin === CURRENT_USER.name) {
                console.warn('Już jesteś w tej grupie / jesteś adminem')
                return
            }

            const exists = this.invitations.some(
                inv => inv.groupId === groupId && inv.status === 'PENDING',
            )
            if (!exists) {
                this.invitations.push({
                    groupId,
                    groupName: group.name,
                    status: 'PENDING',
                })
            }
            // tu kiedyś: PATCH /api/users/groups/invite
        },

        async createGroup() {
            // frontendowy limit: jedna własna grupa na użytkownika
            const existingMine = this.groups.find(g => g.isMine)
            if (existingMine) {
                console.warn('Użytkownik ma już własną grupę:', existingMine.name)
                return
            }

            const newId =
                (this.groups.reduce((max, g) => Math.max(max, Number(g.id) || 0), 0) || 0) + 1

            const group = {
                id: newId,
                name: `Moja grupa ${newId}`,
                admin: CURRENT_USER.name,
                members: 1,
                ownerId: CURRENT_USER.id,
                isMine: true, // flaga potrzebna w UI
            }

            this.groups.push(group)
            // tu kiedyś: POST /api/users/groups/create
        },

        async acceptInvitation(groupId) {
            const inv = this.invitations.find(
                i => i.groupId === groupId && i.status === 'PENDING',
            )
            if (!inv) return

            inv.status = 'ACCEPTED'
            const group = this.groups.find(g => g.id === groupId)
            if (group) group.members += 1

            // tu kiedyś: PATCH /api/users/groups/admin/candidates/accept
        },

        async rejectInvitation(groupId) {
            const inv = this.invitations.find(
                i => i.groupId === groupId && i.status === 'PENDING',
            )
            if (!inv) return

            inv.status = 'REJECTED'
            // tu kiedyś: PATCH /api/users/groups/admin/candidates/reject
        },
    },
})
