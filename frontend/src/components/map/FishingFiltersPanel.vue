<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  filters: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['hide', 'update:filters', 'apply'])

// spotType:
//  - 'ALL'     → Dowolne
//  - 'PUBLIC'  → PZW / koło (lub inne „publiczne”)
//  - 'PRIVATE' → Prywatne / komercyjne
const localFilters = ref({
  spotType: props.filters.spotType ?? 'ALL',
  mode: props.filters.mode ?? 'ALL', // ALL | RADIUS
  radiusKm: props.filters.radiusKm ?? 50,
})

watch(
    () => props.filters,
    (val) => {
      localFilters.value = {
        spotType: val.spotType ?? 'ALL',
        mode: val.mode ?? 'ALL',
        radiusKm: val.radiusKm ?? 50,
      }
    },
    { deep: true },
)

function updateField(field, value) {
  localFilters.value = {
    ...localFilters.value,
    [field]: value,
  }
  emit('update:filters', { ...localFilters.value })
}

function applyFilters() {
  const snapshot = { ...localFilters.value }
  emit('update:filters', snapshot)
  emit('apply', snapshot)
}
</script>

<template>
  <aside
      class="bg-black/75 backdrop-blur p-4 flex flex-col gap-4 overflow-y-auto min-h-0"
  >
    <header class="flex items-center justify-between">
      <h2 class="font-semibold text-sm uppercase tracking-wide">
        Filtry
      </h2>
      <button
          class="text-xs border border-white/60 rounded px-2 py-0.5 hover:bg-white/10"
          @click="emit('hide')"
      >
        Ukryj
      </button>
    </header>

    <!-- Rodzaj łowiska (filtrowanie po polu type w DTO – PUBLIC/PRIVATE) -->
    <div class="flex flex-col gap-1 text-xs">
      <label class="font-medium">Rodzaj łowiska</label>
      <select
          class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
          :value="localFilters.spotType"
          @change="updateField('spotType', $event.target.value)"
      >
        <option value="ALL">Dowolne</option>
        <option value="PUBLIC">PZW / koło</option>
        <option value="PRIVATE">Prywatne / komercyjne</option>
      </select>
    </div>

    <!-- Tryb pobierania + promień -->
    <div class="flex flex-col gap-1 text-xs">
      <label class="font-medium">Tryb wyszukiwania</label>
      <select
          class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
          :value="localFilters.mode"
          @change="updateField('mode', $event.target.value)"
      >
        <option value="ALL">Wszystkie zaakceptowane łowiska</option>
        <option value="RADIUS">W promieniu od środka mapy</option>
      </select>
    </div>

    <div
        v-if="localFilters.mode === 'RADIUS'"
        class="flex flex-col gap-1 text-xs"
    >
      <label class="font-medium">Promień [km]</label>
      <input
          type="number"
          min="1"
          max="500"
          class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
          :value="localFilters.radiusKm"
          @input="updateField('radiusKm', Number($event.target.value) || 0)"
      />
      <p class="opacity-80">
        Użyje środka aktualnie widocznej mapy jako punktu odniesienia.
      </p>
    </div>

    <div class="mt-2">
      <button
          type="button"
          class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs"
          @click="applyFilters"
      >
        Zastosuj
      </button>
    </div>
  </aside>
</template>
