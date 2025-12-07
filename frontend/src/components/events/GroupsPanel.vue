<template>
  <section class="space-y-4">
    <div class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-4">
      <h2 class="text-lg font-semibold">Grupy wyjazdowe</h2>

      <div class="space-y-2">
        <input
            v-model="search"
            type="text"
            placeholder="Szukaj grupy…"
            class="w-full px-3 py-1.5 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
        />
        <button
            type="button"
            class="w-full px-3 py-1.5 rounded-lg text-xs font-medium border theme-border"
            @click="$emit('create-group')"
        >
          Utwórz nową grupę
        </button>
      </div>

      <div v-if="isLoadingGroups" class="text-xs text-[var(--color-muted)]">
        Ładowanie grup...
      </div>

      <div v-else-if="!filteredGroups.length" class="text-xs text-[var(--color-muted)]">
        Brak grup do wyświetlenia.
      </div>

      <ul v-else class="space-y-2 text-sm">
        <li
            v-for="group in filteredGroups"
            :key="group.id"
            class="rounded-lg px-3 py-2 border theme-border bg-[var(--color-surface)]"
        >
          <div class="flex items-center justify-between">
            <span class="font-medium text-sm">{{ group.name }}</span>
            <span class="text-[10px] text-[var(--color-muted)]">
              <span v-if="group.isMine">Twoja grupa (admin)</span>
              <span v-else>Prywatna</span>
            </span>
          </div>

          <p class="text-[11px] text-[var(--color-muted)]">
            Administrator: {{ group.admin }} · {{ group.members }} członków
          </p>

          <!-- przycisk tylko dla grup, do których jeszcze nie należysz -->
          <button
              v-if="!group.isMine"
              class="mt-1 px-2 py-1 rounded-lg text-[11px] font-medium border theme-border"
              @click="$emit('request-join', group.id)"
          >
            Wyślij prośbę o dołączenie
          </button>
        </li>
      </ul>
    </div>

    <div class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-3">
      <h3 class="text-sm font-semibold">Powiadomienia grup</h3>

      <div v-if="!invitations.length" class="text-xs text-[var(--color-muted)]">
        Brak nowych powiadomień.
      </div>

      <div
          v-for="inv in invitations"
          :key="inv.groupId"
          class="rounded-lg border theme-border p-3 text-xs"
      >
        Zaproszenie do grupy „{{ inv.groupName }}”.
        <div class="flex gap-2 mt-2">
          <button
              class="px-2 py-1 rounded-lg border theme-border text-[11px]"
              @click="$emit('accept-invitation', inv.groupId)"
          >
            Akceptuj
          </button>
          <button
              class="px-2 py-1 rounded-lg border theme-border text-[11px]"
              @click="$emit('reject-invitation', inv.groupId)"
          >
            Odrzuć
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  groups: { type: Array, required: true },
  invitations: { type: Array, required: true },
  isLoadingGroups: { type: Boolean, default: false },
})

defineEmits([
  'create-group',
  'request-join',
  'accept-invitation',
  'reject-invitation',
])

const search = ref('')

const filteredGroups = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return props.groups
  return props.groups.filter(g => g.name.toLowerCase().includes(q))
})
</script>
