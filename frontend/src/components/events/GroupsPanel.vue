<template>
  <section class="space-y-4">
    <!-- LISTA GRUP + TWORZENIE -->
    <div class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-4">
      <h2 class="text-lg font-semibold">Grupy</h2>

      <div class="space-y-2">
        <input
            v-model="search"
            type="text"
            placeholder="Szukaj grupy…"
            class="w-full px-3 py-1.5 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
            @keyup.enter="onSearch"
        />

        <div class="flex gap-2">
          <button
              type="button"
              class="px-3 py-1.5 rounded-lg text-xs font-medium border theme-border"
              :disabled="isSearching"
              @click="onSearch"
          >
            {{ isSearching ? 'Szukam...' : 'Szukaj' }}
          </button>

          <button
              type="button"
              class="px-3 py-1.5 rounded-lg text-xs border theme-border"
              :disabled="isSearching"
              @click="onClear"
          >
            Wyczyść
          </button>
        </div>

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
                  :disabled="isCreatingGroup || !newGroupNameTrimmed"
                  @click="onCreateGroup"
              >
                {{ isCreatingGroup ? 'Tworzenie...' : 'Zapisz grupę' }}
              </button>
              <button
                  type="button"
                  class="px-3 py-1.5 rounded-lg text-xs border theme-border"
                  :disabled="isCreatingGroup"
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

      <div v-else-if="!groups.length" class="text-xs text-[var(--color-muted)]">
        Brak grup do wyświetlenia.
      </div>

      <ul v-else class="space-y-2 text-sm max-h-64 overflow-y-auto pr-1">
        <li
            v-for="group in groups"
            :key="group.id"
            class="rounded-lg px-3 py-2 border theme-border bg-[var(--color-surface)] opacity-95"
        >
          <div class="flex items-center justify-between">
            <span class="font-medium text-sm">{{ group.name }}</span>
            <span class="text-[10px] text-[var(--color-muted)]">
              {{ group.members }} członków
            </span>
          </div>

          <p class="text-[11px] text-[var(--color-muted)]">
            <span v-if="group.isMine">Twoja grupa</span>
            <span v-else-if="group.isPending">Oczekujesz na dołączenie</span>
            <span v-else>Kliknij, aby zobaczyć szczegóły</span>
          </p>

          <div class="flex flex-wrap gap-2 mt-2">
            <button
                type="button"
                class="px-2 py-1 rounded-lg text-[11px] font-medium border theme-border"
                @click="$emit('select-group-manage', group.id)"
            >
              Wybierz do zarządzania
            </button>

            <button
                v-if="!group.isMine && !group.isPending"
                type="button"
                class="px-2 py-1 rounded-lg text-[11px] font-medium border theme-border"
                @click="$emit('request-join', group.id)"
            >
              Wyślij prośbę o dołączenie
            </button>

            <span
                v-if="group.isPending"
                class="px-2 py-1 rounded-lg text-[11px] border theme-border text-[var(--color-muted)]"
            >
              Oczekuje
            </span>
          </div>
        </li>
      </ul>
    </div>

    <!-- POWIADOMIENIA -->
    <div class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-3">
      <h3 class="text-sm font-semibold">Powiadomienia grup</h3>

      <div v-if="!invitations.length" class="text-xs text-[var(--color-muted)]">
        Brak nowych powiadomień.
      </div>

      <div v-else class="space-y-2 max-h-40 overflow-y-auto pr-1">
        <div
            v-for="inv in invitations"
            :key="inv.groupId"
            class="rounded-lg border theme-border p-3 text-xs"
        >
          Oczekujesz na dołączenie do grupy „{{ inv.groupName }}” ({{ inv.members }} członków).
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

        <p v-if="!canManage" class="text-[11px] text-[var(--color-muted)]">
          Nie masz uprawnień do zarządzania tą grupą (podgląd tylko).
        </p>

        <!-- kandydaci -->
        <div class="space-y-2">
          <p class="text-[11px] font-medium">Kandydaci do grupy</p>

          <div v-if="isLoadingCandidates" class="text-xs text-[var(--color-muted)]">
            Ładowanie kandydatów...
          </div>

          <div v-else-if="!groupCandidates.length" class="text-xs text-[var(--color-muted)]">
            Brak kandydatów.
          </div>

          <ul v-else class="space-y-1 max-h-36 overflow-y-auto pr-1 text-xs">
            <li
                v-for="c in groupCandidates"
                :key="c.id"
                class="flex items-center justify-between gap-2 border border-[var(--color-border)] rounded-lg px-2 py-1"
            >
              <div class="flex items-center gap-2">
                <div
                    class="h-6 w-6 rounded-full border theme-border bg-[var(--color-bg)] grid place-items-center text-[10px] font-medium"
                >
                  {{ c.initials }}
                </div>
                <div>
                  <p class="text-xs font-medium">
                    {{ c.username || (c.name + ' ' + c.surname) }}
                  </p>
                </div>
              </div>

              <div v-if="canManage" class="flex gap-2">
                <button
                    type="button"
                    class="px-2 py-1 rounded-lg text-[11px] border theme-border"
                    :disabled="isAcceptingCandidate"
                    @click="$emit('accept-candidate', { groupId: groupDetails.id, userId: c.id })"
                >
                  Akceptuj
                </button>
                <button
                    type="button"
                    class="px-2 py-1 rounded-lg text-[11px] border border-red-500 text-red-500"
                    :disabled="isRejectingCandidate"
                    @click="$emit('reject-candidate', { groupId: groupDetails.id, userId: c.id })"
                >
                  Odrzuć
                </button>
              </div>
            </li>
          </ul>
        </div>

        <!-- członkowie -->
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
                  <span v-if="m.isOwner">Właściciel</span>
                  <span v-else-if="m.isAdmin">Administrator</span>
                  <span v-else>Członek</span>
                </p>
              </div>
            </div>

            <button
                v-if="canManage && !m.isOwner"
                type="button"
                class="px-2 py-1 rounded-lg text-[11px] border border-red-500 text-red-500"
                :disabled="isKickingMember"
                @click="$emit('kick-member', { groupId: groupDetails.id, userId: m.id })"
            >
              {{ isKickingMember ? '...' : 'Wyrzuć' }}
            </button>
          </li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '../../stores/auth.js'

const auth = useAuthStore()

const props = defineProps({
  groups: { type: Array, required: true },
  invitations: { type: Array, required: true },
  groupDetails: { type: Object, default: null },
  groupCandidates: { type: Array, default: () => [] },

  isLoadingGroups: { type: Boolean, default: false },
  isLoadingGroupDetails: { type: Boolean, default: false },
  isLoadingCandidates: { type: Boolean, default: false },

  isCreatingGroup: { type: Boolean, default: false },
  isKickingMember: { type: Boolean, default: false },
  isAcceptingCandidate: { type: Boolean, default: false },
  isRejectingCandidate: { type: Boolean, default: false },

  isSearching: { type: Boolean, default: false },
})

const emit = defineEmits([
  'create-group',
  'request-join',
  'select-group-manage',
  'kick-member',
  'accept-candidate',
  'reject-candidate',
  'search',
])

const search = ref('')
const creatingGroup = ref(false)
const newGroupName = ref('')
const createError = ref('')

const newGroupNameTrimmed = computed(() => newGroupName.value.trim())

const canManage = computed(() => {
  const myId = auth.user?.id
  if (!props.groupDetails || !myId) return false

  const ownerId = props.groupDetails.owner?.id ?? null
  if (ownerId && String(ownerId) === String(myId)) return true

  const admins = Array.isArray(props.groupDetails.admins) ? props.groupDetails.admins : []
  return admins.some(a => String(a.id) === String(myId))
})

function cancelCreate() {
  creatingGroup.value = false
  newGroupName.value = ''
  createError.value = ''
}

async function onCreateGroup() {
  if (!newGroupNameTrimmed.value) return
  createError.value = ''
  try {
    await emit('create-group', newGroupNameTrimmed.value)
    cancelCreate()
  } catch (err) {
    const msg = err?.response?.data?.message || err?.message || 'Nie udało się utworzyć grupy.'
    createError.value = msg
  }
}

async function onSearch() {
  await emit('search', search.value)
}

async function onClear() {
  search.value = ''
  await emit('search', '')
}

watch(
    () => auth.user?.id,
    () => {
      // przy zmianie konta czyścimy lokalne UI stanu tworzenia/wyszukiwania
      cancelCreate()
      search.value = ''
    },
)
</script>
