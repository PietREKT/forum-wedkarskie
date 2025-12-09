<template>
  <section class="space-y-4">
    <!-- LISTA GRUP + TWORZENIE -->
    <div class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-4">
      <h2 class="text-lg font-semibold">Grupy wyjazdowe</h2>

      <div class="space-y-2">
        <input
            v-model="search"
            type="text"
            placeholder="Szukaj grupy…"
            class="w-full px-3 py-1.5 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
        />

        <div class="space-y-2">
          <button
              v-if="!creatingGroup"
              type="button"
              class="w-full px-3 py-1.5 rounded-lg text-xs font-medium border theme-border"
              @click="creatingGroup = true"
          >
            Utwórz nową grupę
          </button>

          <div v-else class="space-y-2">
            <input
                v-model="newGroupName"
                type="text"
                placeholder="Nazwa grupy"
                class="w-full px-3 py-1.5 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
            />

            <div class="flex gap-2">
              <button
                  type="button"
                  class="flex-1 px-3 py-1.5 rounded-lg text-xs font-medium border theme-border"
                  :disabled="isCreating || !newGroupNameTrimmed"
                  @click="onCreateGroup"
              >
                {{ isCreating ? 'Tworzenie...' : 'Zapisz grupę' }}
              </button>
              <button
                  type="button"
                  class="px-3 py-1.5 rounded-lg text-xs border theme-border"
                  @click="cancelCreate"
              >
                Anuluj
              </button>
            </div>

            <p v-if="createError" class="text-[11px] text-red-500">
              {{ createError }}
            </p>
          </div>
        </div>
      </div>

      <div v-if="isLoadingGroups" class="text-xs text-[var(--color-muted)]">
        Ładowanie grup...
      </div>

      <div v-else-if="!filteredGroups.length" class="text-xs text-[var(--color-muted)]">
        Brak grup do wyświetlenia.
      </div>

      <ul v-else class="space-y-2 text-sm max-h-64 overflow-y-auto pr-1">
        <li
            v-for="group in filteredGroups"
            :key="group.id"
            class="rounded-lg px-3 py-2 border theme-border bg-[var(--color-surface)] cursor-pointer hover:opacity-100 opacity-95"
            @click="$emit('select-group-manage', group.id)"
        >
          <div class="flex items-center justify-between">
            <span class="font-medium text-sm">{{ group.name }}</span>
            <span class="text-[10px] text-[var(--color-muted)]">
              {{ group.members }} członków
            </span>
          </div>

          <p class="text-[11px] text-[var(--color-muted)]">
            Kliknij, aby zarządzać grupą.
          </p>

          <button
              v-if="!group.isMine"
              type="button"
              class="mt-1 px-2 py-1 rounded-lg text-[11px] font-medium border theme-border"
              @click.stop="$emit('request-join', group.id)"
          >
            Wyślij prośbę o dołączenie
          </button>
        </li>
      </ul>
    </div>

    <!-- POWIADOMIENIA -->
    <div class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-3">
      <h3 class="text-sm font-semibold">Powiadomienia grup</h3>

      <div
          v-if="!invitations.length"
          class="text-xs text-[var(--color-muted)]"
      >
        Brak nowych powiadomień.
      </div>

      <div
          v-else
          class="space-y-2 max-h-40 overflow-y-auto pr-1"
      >
        <div
            v-for="inv in invitations"
            :key="inv.groupId"
            class="rounded-lg border theme-border p-3 text-xs"
        >
          Oczekujesz na dołączenie do grupy „{{ inv.groupName }}”
          ({{ inv.members }} członków).
        </div>
      </div>
    </div>

    <!-- PANEL ZARZĄDZANIA GRUPĄ -->
    <div class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-3">
      <h3 class="text-sm font-semibold">Zarządzanie grupą</h3>

      <div v-if="isLoadingGroupDetails" class="text-xs text-[var(--color-muted)]">
        Ładowanie szczegółów grupy...
      </div>

      <div v-else-if="!groupDetails" class="text-xs text-[var(--color-muted)]">
        Wybierz grupę z listy powyżej, aby zobaczyć jej członków.
      </div>

      <div v-else class="space-y-3">
        <div class="flex items-center justify-between">
          <p class="text-sm font-medium">
            {{ groupDetails.name }}
          </p>
          <span class="text-[11px] text-[var(--color-muted)]">
            {{ groupDetails.members.length }} członków
          </span>
        </div>

        <!-- pole do zapraszania użytkownika po nicku -->
        <div class="space-y-1">
          <label class="text-[11px] font-medium">Zaproś użytkownika</label>
          <div class="flex gap-2">
            <input
                v-model="inviteNick"
                type="text"
                placeholder="Nick użytkownika"
                class="flex-1 px-3 py-1.5 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
            />
            <button
                type="button"
                class="px-3 py-1.5 rounded-lg text-[11px] border theme-border"
                :disabled="!inviteNickTrimmed"
                @click="onInvite"
            >
              Zaproś
            </button>
          </div>
        </div>

        <ul class="space-y-1 max-h-52 overflow-y-auto pr-1 text-xs">
          <li
              v-for="m in groupDetails.members"
              :key="m.id"
              class="flex items-center justify-between gap-2 border border-[var(--color-border)] rounded-lg px-2 py-1"
          >
            <div class="flex items-center gap-2">
              <div
                  class="h-6 w-6 rounded-full border theme-border bg-[var(--color-bg)] grid place-items-center text-[10px] font-medium"
              >
                {{ m.initials }}
              </div>
              <div>
                <p class="text-xs font-medium">
                  {{ m.username || (m.name + ' ' + m.surname) }}
                </p>
                <p class="text-[10px] text-[var(--color-muted)]">
                  <span v-if="m.isAdmin">Administrator</span>
                  <span v-else>Członek</span>
                </p>
              </div>
            </div>

            <button
                type="button"
                class="px-2 py-1 rounded-lg text-[11px] border border-red-500 text-red-500"
                :disabled="isKickingMember"
                @click="$emit('kick-member', { groupId: groupDetails.id, userId: m.id })"
            >
              Wyrzuć
            </button>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  groups: { type: Array, required: true },
  invitations: { type: Array, required: true },
  groupDetails: { type: Object, default: null },
  isLoadingGroups: { type: Boolean, default: false },
  isLoadingGroupDetails: { type: Boolean, default: false },
  isKickingMember: { type: Boolean, default: false },
})

const emit = defineEmits([
  'create-group',
  'request-join',
  'select-group-manage',
  'kick-member',
])

const search = ref('')

const creatingGroup = ref(false)
const newGroupName = ref('')
const isCreating = ref(false)
const createError = ref('')

const inviteNick = ref('')

const newGroupNameTrimmed = computed(() => newGroupName.value.trim())
const inviteNickTrimmed = computed(() => inviteNick.value.trim())

const filteredGroups = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return props.groups
  return props.groups.filter(g => g.name.toLowerCase().includes(q))
})

function cancelCreate() {
  creatingGroup.value = false
  newGroupName.value = ''
  createError.value = ''
}

async function onCreateGroup() {
  if (!newGroupNameTrimmed.value) return
  isCreating.value = true
  createError.value = ''
  try {
    await emit('create-group', newGroupNameTrimmed.value)
    cancelCreate()
  } catch (err) {
    const msg =
        err?.response?.data?.message ||
        err?.message ||
        'Nie udało się utworzyć grupy.'
    createError.value = msg
  } finally {
    isCreating.value = false
  }
}

function onInvite() {
  // na razie tylko UI – bez podłączonego backendu
  if (!inviteNickTrimmed.value) return
  console.log('Zaproszenie użytkownika (placeholder):', inviteNickTrimmed.value)
  inviteNick.value = ''
}
</script>
