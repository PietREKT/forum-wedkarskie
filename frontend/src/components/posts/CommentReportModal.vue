<script setup>
const props = defineProps({
  comment: { type: Object, default: null },
  reasons: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
})

const emit = defineEmits(['close', 'submit'])
</script>

<template>
  <div class="fixed inset-0 z-40 flex items-center justify-center bg-black/60">
    <div class="w-full max-w-sm rounded-xl border theme-border theme-card p-4">
      <h3 class="text-sm font-semibold mb-2">Zgłoś komentarz</h3>

      <p v-if="comment" class="text-xs theme-muted mb-2">
        "{{ comment.content }}"
      </p>

      <p class="text-xs theme-muted mb-3">
        Wybierz powód zgłoszenia. Zgłoszenie zostanie przekazane moderatorowi.
      </p>

      <div class="flex flex-col gap-2">
        <button
            v-for="r in reasons"
            :key="r.code"
            class="px-3 py-1.5 rounded-md border text-xs text-left theme-border hover:bg-[var(--color-border)]/20 disabled:opacity-50"
            :disabled="loading"
            @click="emit('submit', r.code)"
        >
          {{ r.label }}
        </button>
      </div>

      <div class="mt-3 flex justify-end gap-2">
        <button
            class="px-3 py-1.5 rounded-md border text-xs theme-border"
            :disabled="loading"
            @click="emit('close')"
        >
          Anuluj
        </button>
      </div>
    </div>
  </div>
</template>
