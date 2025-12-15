<template>
  <section class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-4">
    <h2 class="text-lg font-semibold">Lista wydarzeń</h2>

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
  selectedId: { type: [Number, String, null], default: null },
  isLoading: { type: Boolean, default: false },
})

defineEmits(['select'])
</script>
