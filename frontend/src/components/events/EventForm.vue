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

const props = defineProps({
  groups: { type: Array, required: true },
  spots: { type: Array, required: true },
  isSaving: { type: Boolean, default: false },

  // przekazujesz: store.manageableGroupIds (Set)
  manageableGroupIds: { type: [Object, Array], default: null },
})

const emit = defineEmits(['save'])

const empty = {
  name: '',
  groupId: '',
  spotId: '',
  dateTime: '',
  description: '',
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

watch(
    () => props.isSaving,
    (saving, prev) => {
      if (prev && !saving) Object.assign(form, empty)
    },
)

function onSubmit() {
  if (!canSubmit.value) return
  if (!canCreateInSelectedGroup.value) return
  emit('save', { ...form })
}
</script>
