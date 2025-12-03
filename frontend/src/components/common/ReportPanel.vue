<template>
  <section class="absolute top-10 right-4 z-30">
    <div
        class="w-72 rounded-lg border border-red-500/80
             bg-white dark:bg-zinc-900
             shadow-lg px-3 py-2 text-xs"
    >
      <p class="font-semibold text-red-700 dark:text-red-300">
        Zgłoś post
      </p>
      <p class="mt-0.5 text-[11px] text-zinc-700 dark:text-zinc-300">
        Wybierz powód zgłoszenia. Zgłoszenie zostanie przekazane moderatorowi.
      </p>

      <div class="mt-2 flex flex-wrap gap-2">
        <button
            v-for="reason in reasons"
            :key="reason.key"
            type="button"
            class="px-3 py-1.5 rounded-md border theme-border
                 bg-red-50 dark:bg-red-900/40
                 text-[11px] text-red-800 dark:text-red-100
                 hover:bg-red-100 dark:hover:bg-red-900/70"
            :disabled="loading"
            @click="onSelect(reason.key)"
        >
          {{ reason.label }}
        </button>

        <button
            type="button"
            class="px-3 py-1.5 rounded-md border theme-border
                 bg-zinc-50 dark:bg-zinc-800
                 text-[11px] text-zinc-800 dark:text-zinc-100
                 hover:bg-zinc-100 dark:hover:bg-zinc-700"
            :disabled="loading"
            @click="onCancel"
        >
          Anuluj
        </button>
      </div>

      <p v-if="error" class="mt-1 text-red-700 dark:text-red-300">
        {{ error }}
      </p>
    </div>
  </section>
</template>

<script setup>
const props = defineProps({
  reasons: { type: Array, required: true },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
})

const emit = defineEmits(['select-reason', 'cancel'])

function onSelect(reasonKey) {
  emit('select-reason', reasonKey)
}

function onCancel() {
  emit('cancel')
}
</script>
