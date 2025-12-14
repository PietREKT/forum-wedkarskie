<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  filters: { type: Object, required: true },

  showModerationButton: { type: Boolean, default: false },
  moderationOpen: { type: Boolean, default: false },
  pendingCount: { type: Number, default: 0 },
})

const emit = defineEmits(['hide', 'update:filters', 'toggle-moderation'])

const localFilters = ref({
  spotType: props.filters.spotType ?? 'ALL',
})

watch(
    () => props.filters,
    (val) => {
      localFilters.value = { spotType: val.spotType ?? 'ALL' }
    },
    { deep: true },
)

function updateField(field, value) {
  localFilters.value = { ...localFilters.value, [field]: value }
  emit('update:filters', { ...localFilters.value })
}
</script>

<template>
  <aside class="bg-black/75 backdrop-blur p-4 flex flex-col gap-4 overflow-y-auto min-h-0 text-white">
    <header class="flex items-center justify-between">
      <h2 class="font-semibold text-sm uppercase tracking-wide">FILTRY</h2>
      <button class="text-xs border border-white/60 rounded px-2 py-0.5 hover:bg-white/10" @click="emit('hide')">
        Ukryj
      </button>
    </header>

    <button
        v-if="showModerationButton"
        type="button"
        class="text-xs border border-white/60 rounded-full px-3 py-1 hover:bg-white/10 w-fit"
        @click="emit('toggle-moderation')"
    >
      <span v-if="moderationOpen">Ukryj zgłoszenia</span>
      <span v-else>ZGŁOSZENIA ŁOWISK</span>
      <span class="opacity-80" v-if="pendingCount"> ({{ pendingCount }})</span>
    </button>

    <div class="flex flex-col gap-1 text-xs">
      <label class="font-medium">Rodzaj łowiska</label>
      <select
          class="bg-white text-black border border-white/60 rounded px-2 py-1 text-xs outline-none"
          :value="localFilters.spotType"
          @change="updateField('spotType', $event.target.value)"
      >
        <option value="ALL">Dowolne</option>
        <option value="PUBLIC">PZW / koło</option>
        <option value="PRIVATE">Prywatne / komercyjne</option>
      </select>
    </div>
  </aside>
</template>
