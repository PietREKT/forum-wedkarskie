<template>
  <div
      class="absolute inset-x-0 top-0 z-30 flex justify-center"
  >
    <div
        class="mt-3 w-full max-w-md rounded-2xl border border-[var(--color-border)]
             bg-[var(--color-bg)] shadow-xl px-4 py-3 text-[var(--color-text)]"
    >
      <h4 class="text-sm font-semibold mb-2">
        Zgłoś profil
      </h4>
      <p class="text-xs text-[var(--color-muted)] mb-3">
        Wybierz powód zgłoszenia profilu. Zgłoszenie trafi do administratora.
      </p>

      <div class="space-y-1.5 max-h-40 overflow-y-auto pr-1">
        <button
            v-for="reason in reasons"
            :key="reason.key"
            type="button"
            class="w-full text-left text-xs rounded-lg px-3 py-1.5
                 border border-[var(--color-border)]
                 hover:bg-red-500/10 hover:border-red-500/60"
            :disabled="loading"
            @click="$emit('select-reason', reason.key)"
        >
          {{ reason.label }}
        </button>
      </div>

      <p v-if="error" class="mt-2 text-xs text-red-600">
        {{ error }}
      </p>

      <div class="mt-3 flex justify-end gap-2">
        <button
            type="button"
            class="px-3 py-1.5 text-xs rounded-lg
                 border border-[var(--color-border)]
                 bg-[var(--color-bg)] hover:opacity-90"
            :disabled="loading"
            @click="$emit('cancel')"
        >
          Anuluj
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  reasons: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: '',
  },
})
</script>
