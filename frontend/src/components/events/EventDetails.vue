<template>
  <section class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-6">
    <h3 class="text-lg font-semibold">Szczegóły wybranego wydarzenia</h3>

    <div v-if="!event" class="text-xs text-[var(--color-muted)]">
      Wybierz wydarzenie z listy po lewej.
    </div>

    <div
        v-else
        class="rounded-lg border theme-border bg-[var(--color-surface)] p-3 space-y-3"
    >
      <div class="flex items-start justify-between">
        <div>
          <p class="text-sm font-medium">{{ event.name }}</p>
          <p class="text-[11px] text-[var(--color-muted)]">
            Łowisko: {{ event.spotName }} · {{ event.dateLabel }}
          </p>
        </div>

        <span
            class="text-[10px] px-2 py-0.5 rounded-full border theme-border text-[var(--color-muted)]"
        >
          {{ event.groupName || 'Bez grupy' }}
        </span>
      </div>

      <p class="text-xs">{{ event.description }}</p>

      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="flex -space-x-2">
            <div
                v-for="p in event.participants"
                :key="p.id"
                class="h-7 w-7 rounded-full border theme-border bg-[var(--color-bg)]
                     grid place-items-center text-[10px] font-medium"
            >
              {{ p.initials }}
            </div>
          </div>
          <span class="text-[11px] text-[var(--color-muted)]">
            {{ event.participants.length }} uczestników
          </span>
        </div>

        <div class="flex items-center gap-2">
          <button
              v-if="!event.isParticipating"
              class="px-3 py-1.5 rounded-lg text-xs font-medium border theme-border"
              :disabled="isJoining"
              @click="$emit('join')"
          >
            {{ isJoining ? 'Dołączanie...' : 'Dołącz' }}
          </button>

          <button
              v-else
              class="px-3 py-1.5 rounded-lg text-xs font-medium border theme-border"
              :disabled="isJoining"
              @click="$emit('leave')"
          >
            {{ isJoining ? 'Opuszczanie...' : 'Opuść wydarzenie' }}
          </button>
        </div>
      </div>

      <div class="flex justify-end gap-2 pt-2 border-t border-[var(--color-border)] mt-2">
        <button
            class="px-2 py-1 rounded-lg text-[11px] border theme-border"
            @click="$emit('edit')"
        >
          Edytuj
        </button>
        <button
            class="px-2 py-1 rounded-lg text-[11px] border border-red-500 text-red-500"
            @click="$emit('delete')"
        >
          Usuń
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  event: { type: Object, default: null },
  isJoining: { type: Boolean, default: false },
})

defineEmits(['join', 'leave', 'edit', 'delete'])
</script>
