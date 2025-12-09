<template>
  <div class="max-w-7xl mx-auto py-6 px-4 space-y-6">
    <header>
      <h1 class="text-2xl md:text-3xl font-semibold">Wydarzenia</h1>
    </header>

    <div class="grid gap-4 lg:grid-cols-[2fr_3fr_2fr] md:grid-cols-2 grid-cols-1">
      <!-- LEWA KOLUMNA – LISTA WYDARZEŃ -->
      <EventsList
          :events="events"
          :selected-id="selectedEventId"
          :is-loading="store.isLoadingEvents"
          @select="store.selectEvent"
      />

      <!-- ŚRODEK – FORMULARZ + SZCZEGÓŁY -->
      <section class="space-y-6">
        <EventForm
            :groups="groups"
            :spots="spots"
            :is-saving="store.isSavingEvent"
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

      <!-- PRAWA KOLUMNA – GRUPY + POWIADOMIENIA -->
      <GroupsPanel
          :groups="groups"
          :invitations="invitations"
          :is-loading-groups="store.isLoadingGroups"
          @create-group="handleCreateGroup"
          @request-join="handleRequestJoinGroup"
          @accept-invitation="handleAcceptInvitation"
          @reject-invitation="handleRejectInvitation"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useEventsStore } from '../stores/eventsStore'

import EventsList from '../components/events/EventsList.vue'
import EventForm from '../components/events/EventForm.vue'
import EventDetails from '../components/events/EventDetails.vue'
import GroupsPanel from '../components/events/GroupsPanel.vue'

const store = useEventsStore()

const events = computed(() => store.events)
const groups = computed(() => store.groups)
const spots = computed(() => store.spots)
const invitations = computed(() => store.invitations)
const currentEvent = computed(() => store.currentEvent)
const selectedEventId = computed(() => store.selectedEventId)

// HANDLERY ZDARZEŃ Z KOMPONENTÓW

async function handleSaveEvent(payload) {
  await store.createEvent({
    name: payload.name,
    description: payload.description,
    type: payload.type,
    dateTime: payload.dateTime,
    spotId: payload.spotId || null,
    groupId: payload.groupId || null,
  })
}

async function handleJoinEvent() {
  if (!currentEvent.value) return
  await store.joinEvent(currentEvent.value.id)
}

async function handleLeaveEvent() {
  if (!currentEvent.value) return
  await store.leaveEvent(currentEvent.value.id)
}

function handleEditEvent() {
  if (!currentEvent.value) return
  // w przyszłości można tu otworzyć modal edycji
  console.log('edit event', currentEvent.value.id)
}

async function handleDeleteEvent() {
  if (!currentEvent.value) return
  await store.deleteEvent(currentEvent.value.id)
}

async function handleRequestJoinGroup(groupId) {
  await store.requestJoinGroup(groupId)
}

async function handleCreateGroup() {
  await store.createGroup()
}

async function handleAcceptInvitation(groupId) {
  await store.acceptInvitation(groupId)
}

async function handleRejectInvitation(groupId) {
  await store.rejectInvitation(groupId)
}

onMounted(async () => {
  await Promise.all([
    store.fetchEvents(),
    store.fetchGroups(),
    store.fetchSpots(),
    store.fetchInvitations(),
  ])
})
</script>
