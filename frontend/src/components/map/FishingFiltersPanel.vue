<script setup>
const emit = defineEmits(['hide'])
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

    <div class="flex flex-col gap-1 text-xs">
      <label class="font-medium">Województwo</label>
      <select
          class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
      >
        <option>Dowolne</option>
        <option>Warmińsko-mazurskie</option>
        <option>Mazowieckie</option>
        <option>Śląskie</option>
      </select>
    </div>

    <div class="flex flex-col gap-1 text-xs">
      <label class="font-medium">Typ zbiornika</label>
      <select
          class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
      >
        <option>Dowolny</option>
        <option>Rzeka</option>
        <option>Jezioro</option>
        <option>Staw</option>
        <option>Torfowisko</option>
        <option>Żwirownia</option>
        <option>Zalewisko</option>
      </select>
    </div>

    <div class="flex flex-col gap-1 text-xs">
      <label class="font-medium">Gatunki</label>
      <input
          type="text"
          placeholder="np. szczupak, sandacz"
          class="bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
      />
    </div>

    <div class="flex flex-col gap-1 text-xs">
      <label class="font-medium">Rodzaj łowiska</label>
      <select
          class="bg-white/15 text-white border border-white/60 rounded px-2 py-1 text-xs outline-none"
      >
        <option>Dowolne</option>
        <option>PZW / koło</option>
        <option>Prywatne / komercyjne</option>
        <option>Własne</option>
      </select>
    </div>
  </aside>
</template>
