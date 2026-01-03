<template>
  <section class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-4">
    <h2 class="text-lg font-semibold">Lista wydarzeń</h2>

    <!-- INVITES (przypięte na górze) -->
    <div v-if="invitesLoading" class="text-xs text-[var(--color-muted)]">
      Ładowanie zaproszeń...
    </div>

    <div v-else-if="invites.length" class="space-y-2">
      <div class="text-xs font-medium opacity-80">
        Zaproszenia do wydarzeń ({{ invites.length }})
      </div>

      <ul class="space-y-2 text-sm">
        <li
            v-for="inv in invites"
            :key="String(inv.inviteId ?? inv.eventId ?? JSON.stringify(inv))"
            class="rounded-lg px-3 py-2 border theme-border bg-[var(--color-surface)] opacity-95"
        >
          <div class="flex items-center justify-between gap-3">
            <div class="min-w-0">
              <div class="font-medium truncate">
                {{ inv.eventName || 'Zaproszenie do wydarzenia' }}
              </div>
              <div class="text-[11px] text-[var(--color-muted)] mt-0.5">
                <span v-if="inv.fromUsername">Od: {{ inv.fromUsername }}</span>
                <span v-else>Masz zaproszenie do wydarzenia</span>
              </div>
            </div>

            <div class="flex items-center gap-2 shrink-0">
              <button
                  type="button"
                  class="px-2 py-1 rounded-lg text-[11px] font-medium border theme-border"
                  @click.stop="$emit('accept-invite', inv.inviteId)"
                  :disabled="!inv.inviteId"
                  :title="!inv.inviteId ? 'Brak ID zaproszenia z backendu.' : ''"
              >
                Akceptuj
              </button>
              <button
                  type="button"
                  class="px-2 py-1 rounded-lg text-[11px] font-medium border border-red-500 text-red-500"
                  @click.stop="$emit('reject-invite', inv.inviteId)"
                  :disabled="!inv.inviteId"
                  :title="!inv.inviteId ? 'Brak ID zaproszenia z backendu.' : ''"
              >
                Odrzuć
              </button>
            </div>
          </div>
        </li>
      </ul>

      <hr class="border-[var(--color-border)] opacity-60" />
    </div>

    <!-- EVENTS -->
    <div v-if="isLoading" class="text-xs text-[var(--color-muted)]">
      Ładowanie wydarzeń...
    </div>

    <div v-else-if="!events.length" class="text-xs text-[var(--color-muted)]">
      Brak wydarzeń do wyświetlenia.
    </div>

    <ul v-else class="space-y-2 text-sm">
      <li
          v-for="event in events"
          :key="event.id"
          class="rounded-lg px-3 py-2 border flex flex-col gap-0.5 cursor-pointer"
          :class="String(event.id) === String(selectedId)
          ? 'border-[var(--color-primary)] bg-[var(--color-surface)]'
          : 'theme-border bg-[var(--color-surface)] opacity-90 hover:opacity-100'"
          @click="$emit('select', event.id)"
      >
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ event.name }}</span>
          <span class="text-[10px] text-[var(--color-muted)]">
            {{ event.dateLabel }}
          </span>
        </div>

        <p class="text-[11px] text-[var(--color-muted)]">
          {{ event.groupName ? ('Grupa: ' + event.groupName) : 'Bez grupy' }} ·
          organizator: {{ event.organizer }}
          <span v-if="event.isPast" class="ml-1">· odbyło się</span>
        </p>
      </li>
    </ul>
  </section>
</template>

<script setup>
defineProps({
  events: { type: Array, required: true },

  invites: { type: Array, default: () => [] },
  invitesLoading: { type: Boolean, default: false },

  selectedId: { type: [Number, String, null], default: null },
  isLoading: { type: Boolean, default: false },
})

defineEmits(['select', 'accept-invite', 'reject-invite'])
</script>
