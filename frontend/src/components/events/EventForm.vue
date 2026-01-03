<template>
  <section class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-6">
    <h2 class="text-lg font-semibold">Dodaj wydarzenie</h2>

    <form class="space-y-3 text-sm" @submit.prevent="onSubmit">
      <div class="space-y-1">
        <label class="text-xs font-medium">Nazwa wydarzenia</label>
        <input
            v-model="form.name"
            type="text"
            placeholder="Np. Wyjazd na jezioro X"
            class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
        />
      </div>

      <div class="grid md:grid-cols-3 gap-3">
        <div class="space-y-1 md:col-span-1">
          <label class="text-xs font-medium">Grupa</label>
          <select
              v-model="form.groupId"
              class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
          >
            <option value="">Wybierz grupę…</option>
            <option v-for="group in groups" :key="group.id" :value="group.id">
              {{ group.name }}
            </option>
          </select>

          <p v-if="groupRoleHint" class="text-[11px] text-[var(--color-muted)] mt-1">
            {{ groupRoleHint }}
          </p>
        </div>

        <div class="space-y-1 md:col-span-1">
          <label class="text-xs font-medium">Łowisko</label>
          <select
              v-model="form.spotId"
              class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
          >
            <option value="">Wybierz łowisko…</option>
            <option v-for="spot in spots" :key="spot.id" :value="spot.id">
              {{ spot.name }}
            </option>
          </select>
        </div>

        <div class="space-y-1 md:col-span-1">
          <label class="text-xs font-medium">Data i godzina</label>
          <input
              v-model="form.dateTime"
              type="datetime-local"
              class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
          />
        </div>
      </div>

      <div class="space-y-1">
        <label class="text-xs font-medium">Opis</label>
        <textarea
            v-model="form.description"
            rows="3"
            placeholder="Krótki opis wydarzenia"
            class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs resize-none"
        ></textarea>
      </div>

      <!-- ZAPROSZENIA: lista członków wybranej grupy (bez wyszukiwania) -->
      <div v-if="selectedGroupId" class="space-y-2 rounded-lg border theme-border bg-[var(--color-surface)] p-3">
        <div class="flex items-center justify-between gap-2">
          <div class="text-xs font-medium">Zaproś członków grupy</div>

          <div class="flex gap-2">
            <button
                type="button"
                class="px-2 py-1 rounded-lg text-[11px] border theme-border"
                :disabled="!canInviteFromLoadedGroup"
                @click="inviteAll"
                :title="!canInviteFromLoadedGroup ? inviteHint : ''"
            >
              Zaproś wszystkich
            </button>

            <button
                type="button"
                class="px-2 py-1 rounded-lg text-[11px] border theme-border"
                :disabled="form.invitedUsersIds.length === 0"
                @click="clearInvites"
            >
              Wyczyść
            </button>
          </div>
        </div>

        <div v-if="!canInviteFromLoadedGroup" class="text-[11px] text-[var(--color-muted)]">
          {{ inviteHint }}
        </div>

        <div v-else class="space-y-2">
          <div class="text-[11px] text-[var(--color-muted)]">
            Zaznaczeni: {{ form.invitedUsersIds.length }}
          </div>

          <ul class="space-y-1 max-h-40 overflow-y-auto pr-1 text-xs">
            <li
                v-for="m in inviteCandidates"
                :key="m.id"
                class="flex items-center justify-between gap-2 border border-[var(--color-border)] rounded-lg px-2 py-1"
            >
              <label class="flex items-center gap-2 cursor-pointer select-none">
                <input
                    type="checkbox"
                    class="accent-[var(--color-primary)]"
                    :checked="isInvited(m.id)"
                    @change="toggleInvite(m.id)"
                />
                <span class="font-medium">
                  {{ m.username || (m.name + ' ' + m.surname) }}
                </span>
              </label>

              <span class="text-[10px] text-[var(--color-muted)]">
                {{ m.initials }}
              </span>
            </li>
          </ul>
        </div>
      </div>

      <button
          type="submit"
          class="px-4 py-2 rounded-lg text-xs font-medium bg-[var(--color-primary)] text-white shadow disabled:opacity-50"
          :disabled="isSaving || !canSubmit || !canCreateInSelectedGroup"
          :title="!canCreateInSelectedGroup ? 'Nie masz uprawnień do tworzenia wydarzeń w tej grupie.' : ''"
      >
        {{ isSaving ? 'Zapisywanie...' : 'Zapisz wydarzenie' }}
      </button>
    </form>
  </section>
</template>

<script setup>
import { reactive, watch, computed } from 'vue'
import { useAuthStore } from '../../stores/auth.js'

const auth = useAuthStore()

const props = defineProps({
  groups: { type: Array, required: true },
  spots: { type: Array, required: true },
  isSaving: { type: Boolean, default: false },

  // przekazujesz: store.manageableGroupIds (Set)
  manageableGroupIds: { type: [Object, Array], default: null },

  // wczytane szczegóły grupy (z panelu zarządzania)
  groupDetails: { type: Object, default: null },
})

const emit = defineEmits(['save'])

const empty = {
  name: '',
  groupId: '',
  spotId: '',
  dateTime: '',
  description: '',
  invitedUsersIds: [],
}

const form = reactive({ ...empty })

const manageableIdsSet = computed(() => {
  const v = props.manageableGroupIds
  if (!v) return new Set()
  if (v instanceof Set) return v
  if (Array.isArray(v)) return new Set(v.map(String))
  if (typeof v === 'object') return new Set(Object.keys(v).map(String))
  return new Set()
})

const selectedGroupId = computed(() => (form.groupId ? String(form.groupId) : ''))

// Jeśli mapa uprawnień jest pusta (backend jeszcze nie zwraca owner/admin) -> nie blokujemy.
// Jak backend zacznie zwracać -> zacznie blokować.
const canCreateInSelectedGroup = computed(() => {
  if (!selectedGroupId.value) return true
  if (manageableIdsSet.value.size === 0) return true
  return manageableIdsSet.value.has(selectedGroupId.value)
})

const groupRoleHint = computed(() => {
  if (!selectedGroupId.value) return ''
  if (manageableIdsSet.value.size === 0) {
    return 'Uprawnienia do tworzenia wydarzeń w grupie będą działały po dodaniu owner/admin w odpowiedzi backendu.'
  }
  if (!manageableIdsSet.value.has(selectedGroupId.value)) {
    return 'Nie masz uprawnień do tworzenia wydarzeń w tej grupie (wymagany właściciel lub administrator).'
  }
  return 'Masz uprawnienia do tworzenia wydarzeń w tej grupie.'
})

const canSubmit = computed(() => {
  return String(form.name || '').trim().length >= 1 && String(form.dateTime || '').trim().length >= 1
})

// zaproszenia: działają tylko jeśli groupDetails dotyczy wybranej grupy i ma members
const canInviteFromLoadedGroup = computed(() => {
  if (!selectedGroupId.value) return false
  const gd = props.groupDetails
  if (!gd || !gd.id) return false
  if (String(gd.id) !== String(selectedGroupId.value)) return false
  return Array.isArray(gd.members) && gd.members.length > 0
})

const inviteHint = computed(() => {
  if (!selectedGroupId.value) return ''
  if (!props.groupDetails) return 'Aby zapraszać, wybierz grupę do zarządzania w panelu po prawej (wczyta członków).'
  if (String(props.groupDetails?.id) !== String(selectedGroupId.value)) {
    return 'Aby zapraszać, w panelu po prawej wybierz do zarządzania tę samą grupę, którą wybrałeś w formularzu.'
  }
  if (!Array.isArray(props.groupDetails?.members) || props.groupDetails.members.length === 0) {
    return 'Brak listy członków w szczegółach grupy.'
  }
  return ''
})

const inviteCandidates = computed(() => {
  if (!canInviteFromLoadedGroup.value) return []
  const meId = auth.user?.id
  return (props.groupDetails?.members || []).filter(m => String(m.id) !== String(meId))
})

function isInvited(id) {
  return (form.invitedUsersIds || []).some(x => String(x) === String(id))
}

function toggleInvite(id) {
  const arr = Array.isArray(form.invitedUsersIds) ? form.invitedUsersIds : []
  const sid = String(id)
  if (arr.some(x => String(x) === sid)) {
    form.invitedUsersIds = arr.filter(x => String(x) !== sid)
  } else {
    form.invitedUsersIds = [...arr, id]
  }
}

function inviteAll() {
  if (!canInviteFromLoadedGroup.value) return
  form.invitedUsersIds = inviteCandidates.value.map(m => m.id)
}

function clearInvites() {
  form.invitedUsersIds = []
}

watch(
    () => props.isSaving,
    (saving, prev) => {
      if (prev && !saving) Object.assign(form, empty)
    },
)

watch(
    () => form.groupId,
    () => {
      // zmiana grupy -> czyścimy listę zaproszeń (żeby nie zapraszać do złej grupy)
      form.invitedUsersIds = []
    },
)

function onSubmit() {
  if (!canSubmit.value) return
  if (!canCreateInSelectedGroup.value) return
  emit('save', { ...form })
}
</script>
