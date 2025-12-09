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
            <option
                v-for="group in groups"
                :key="group.id"
                :value="group.id"
            >
              {{ group.name }} (admin: {{ group.admin }})
            </option>
          </select>
        </div>

        <div class="space-y-1 md:col-span-1">
          <label class="text-xs font-medium">Łowisko</label>
          <select
              v-model="form.spotId"
              class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
          >
            <option value="">Wybierz łowisko…</option>
            <option
                v-for="spot in spots"
                :key="spot.id"
                :value="spot.id"
            >
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
            class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)]
                 text-xs resize-none"
        ></textarea>
      </div>

      <div class="flex items-center gap-3 text-xs">
        <span class="font-medium">Typ:</span>
        <button
            type="button"
            class="px-2 py-1 rounded-full border theme-border"
            :class="form.type === 'TRIP' ? 'bg-[var(--color-primary)] text-white' : ''"
            @click="form.type = 'TRIP'"
        >
          Wyjazd
        </button>
        <button
            type="button"
            class="px-2 py-1 rounded-full border theme-border"
            :class="form.type === 'COMPETITION' ? 'bg-[var(--color-primary)] text-white' : ''"
            @click="form.type = 'COMPETITION'"
        >
          Zawody
        </button>
      </div>

      <button
          type="submit"
          class="px-4 py-2 rounded-lg text-xs font-medium bg-[var(--color-primary)]
               text-white shadow"
          :disabled="isSaving"
      >
        {{ isSaving ? 'Zapisywanie...' : 'Zapisz wydarzenie' }}
      </button>
    </form>
  </section>
</template>

<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  groups: { type: Array, required: true },
  spots: { type: Array, required: true },
  isSaving: { type: Boolean, default: false },
})

const emit = defineEmits(['save'])

const empty = {
  name: '',
  groupId: '',
  spotId: '',
  dateTime: '',
  description: '',
  type: 'TRIP',
}

const form = reactive({ ...empty })

watch(
    () => props.isSaving,
    (saving, prev) => {
      if (prev && !saving) {
        Object.assign(form, empty)
      }
    },
)

function onSubmit() {
  emit('save', { ...form })
}
</script>
