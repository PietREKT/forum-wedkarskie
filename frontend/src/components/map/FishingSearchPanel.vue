<script setup>
const props = defineProps({
  spots: {
    type: Array,
    required: true,
  },
  selectedId: {
    type: [Number, String, null],
    default: null,
  },
})

const emit = defineEmits(['select'])
</script>

<template>
  <section
      class="bg-black/70 backdrop-blur p-4 flex flex-col gap-4 overflow-y-auto min-h-0"
  >
    <header class="flex flex-col gap-2">
      <h2 class="font-semibold text-sm uppercase tracking-wide">
        Wyszukiwarka
      </h2>
      <input
          type="text"
          placeholder="Szukaj po nazwie (na razie bez logiki)"
          class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1.5 text-xs outline-none"
      />
    </header>

    <div class="flex-1 overflow-y-auto space-y-2 text-xs">
      <article
          v-for="spot in props.spots"
          :key="spot.id"
          class="border rounded-lg px-3 py-2 cursor-pointer bg-white/10 hover:bg-white/20"
          :class="spot.id === selectedId ? 'border-white/80' : 'border-white/40'"
          @click="emit('select', spot)"
      >
        <header class="flex items-center justify-between">
          <h3 class="font-semibold text-sm">
            {{ spot.name }}
          </h3>
          <span class="text-[10px] uppercase opacity-90">
            {{ spot.ownerType || spot.type || '—' }}
          </span>
        </header>
        <p class="opacity-90 mt-1">
          {{ (spot.fish || []).join(', ') || 'Brak danych' }}
        </p>
      </article>
    </div>
  </section>
</template>
>