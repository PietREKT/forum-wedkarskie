<script setup>
const props = defineProps({
  comment: { type: Object, default: null },
  loading: { type: Boolean, default: false },
})

const emit = defineEmits(['close', 'confirm'])
</script>

<template>
  <div class="fixed inset-0 z-40 flex items-center justify-center bg-black/60">
    <div class="w-full max-w-sm rounded-xl border theme-border theme-card p-4">
      <h3 class="text-sm font-semibold mb-2">Usunąć komentarz?</h3>

      <p v-if="comment" class="text-xs theme-muted mb-2">
        "{{ comment.content }}"
      </p>

      <p class="text-xs theme-muted mb-4">
        Tej operacji nie można cofnąć.
      </p>

      <div class="flex justify-end gap-2">
        <button
            class="px-3 py-1.5 rounded-md border text-xs theme-border"
            :disabled="loading"
            @click="emit('close')"
        >
          Anuluj
        </button>
        <button
            class="px-3 py-1.5 rounded-md text-xs bg-[var(--color-danger)] text-white disabled:opacity-50"
            :disabled="loading"
            @click="emit('confirm')"
        >
          Usuń
        </button>
      </div>
    </div>
  </div>
</template>
