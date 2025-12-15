<template>
  <div class="max-w-7xl mx-auto py-6 px-4 space-y-6">
    <header>
      <h1 class="text-2xl md:text-3xl font-semibold">Wydarzenia</h1>
    </header>

    <div class="grid gap-4 lg:grid-cols-[2fr_3fr_2fr] md:grid-cols-2 grid-cols-1">
      <EventsList
          :events="events"
          :selected-id="selectedEventId"
          :is-loading="store.isLoadingEvents"
          @select="store.selectEvent"
      />

      <section class="space-y-6">
        <EventForm
            :groups="groups"
            :spots="spots"
            :is-saving="store.isSavingEvent"
            :manageable-group-ids="store.manageableGroupIds"
            @save="handleSaveEvent"
        />

        <EventDetails
            :event="currentEvent"
            :is-joining="store.isJoiningEvent"
            @join="handleJoinEvent"
            @leave="handleLeaveEvent"
            @edit="handleEditEvent"
            @delete="handleDeleteEvent"
        />
      </section>

      <GroupsPanel
          :groups="groups"
          :invitations="invitations"
          :group-details="groupDetails"
          :group-candidates="groupCandidates"
          :is-loading-groups="store.isLoadingGroups"
          :is-loading-group-details="store.isLoadingGroupDetails"
          :is-loading-candidates="store.isLoadingGroupCandidates"
          :is-creating-group="store.isCreatingGroup"
          :is-kicking-member="store.isKickingMember"
          :is-accepting-candidate="store.isAcceptingCandidate"
          :is-rejecting-candidate="store.isRejectingCandidate"
          :is-searching="store.isSearchingGroups"
          @create-group="handleCreateGroup"
          @request-join="handleRequestJoinGroup"
          @select-group-manage="handleSelectGroupForManage"
          @kick-member="handleKickMember"
          @accept-candidate="handleAcceptCandidate"
          @reject-candidate="handleRejectCandidate"
          @search="handleSearchGroups"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useEventsStore } from '../stores/eventsStore'
import { useAuthStore } from '../stores/auth'

import EventsList from '../components/events/EventsList.vue'
import EventForm from '../components/events/EventForm.vue'
import EventDetails from '../components/events/EventDetails.vue'
import GroupsPanel from '../components/events/GroupsPanel.vue'

const store = useEventsStore()
const auth = useAuthStore()

const events = computed(() => store.events)
const groups = computed(() => store.groups)
const spots = computed(() => store.spots)
const invitations = computed(() => store.invitations)
const currentEvent = computed(() => store.currentEvent)
const selectedEventId = computed(() => store.selectedEventId)
const groupDetails = computed(() => store.groupDetails)
const groupCandidates = computed(() => store.groupCandidates)

async function reloadAll() {
  await Promise.all([
    store.fetchEvents(),
    store.fetchGroups(),
    store.fetchSpots(),
  ])
}

watch(
    () => auth.user?.id,
    async (newId, oldId) => {
      if (!newId) {
        store.hardReset()
        return
      }
      if (String(newId) === String(oldId)) return
      store.hardReset()
      await reloadAll()
    },
)

async function handleSaveEvent(payload) {
  await store.createEvent({
    name: payload.name,
    description: payload.description,
    dateTime: payload.dateTime,
    spotId: payload.spotId || null,
    groupId: payload.groupId || null,
  })
  await store.fetchEvents()
}

async function handleJoinEvent() {
  if (!currentEvent.value) return
  await store.joinEvent(currentEvent.value.id)
}

async function handleLeaveEvent() {
  if (!currentEvent.value) return
  await store.leaveEvent(currentEvent.value.id)
}

async function handleEditEvent(payloadFromModal) {
  if (!currentEvent.value) return
  if (!payloadFromModal) return
  await store.updateEvent(currentEvent.value.id, payloadFromModal)
}

async function handleDeleteEvent() {
  if (!currentEvent.value) return
  await store.deleteEvent(currentEvent.value.id)
}

// grupy

async function handleCreateGroup(name) {
  await store.createGroup(name)
  await store.fetchGroups()
}

async function handleRequestJoinGroup(groupId) {
  await store.requestJoinGroup(groupId)
  await store.fetchGroups()
}

async function handleSelectGroupForManage(groupId) {
  store.groupDetails = null
  store.groupCandidates = []
  await store.loadGroupDetails(groupId)
  await store.loadGroupCandidates(groupId)
}

async function handleKickMember(payload) {
  await store.kickMember(payload.groupId, payload.userId)
  await Promise.all([
    store.loadGroupDetails(payload.groupId),
    store.loadGroupCandidates(payload.groupId),
    store.fetchGroups(),
  ])
}

async function handleAcceptCandidate(payload) {
  await store.acceptCandidate(payload.groupId, payload.userId)
  await Promise.all([
    store.loadGroupCandidates(payload.groupId),
    store.loadGroupDetails(payload.groupId),
    store.fetchGroups(),
  ])
}

async function handleRejectCandidate(payload) {
  await store.rejectCandidate(payload.groupId, payload.userId)
  await Promise.all([
    store.loadGroupCandidates(payload.groupId),
    store.fetchGroups(),
  ])
}

async function handleSearchGroups(query) {
  await store.searchGroups(query)
}

onMounted(async () => {
  await reloadAll()
})
</script>
