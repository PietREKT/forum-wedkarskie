<template>
  <button
      type="button"
      class="inline-flex items-center rounded-xl px-4 py-2 text-sm font-medium
           border
           bg-[var(--color-bg)]
           transition
           hover:bg-[var(--color-primary)] hover:text-white
           disabled:opacity-60 disabled:cursor-not-allowed"
      :class="{
      'border-[var(--color-border)] text-[var(--color-text)]': !isFollowing,
      'border-[var(--color-primary)] text-[var(--color-primary)]': isFollowing,
    }"
      :disabled="loading"
      @click="toggleFollow"
  >
    <span v-if="!isFollowing">Obserwuj</span>
    <span v-else>Przestań obserwować</span>
  </button>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  username: {
    type: String,
    required: true,
  },
})

const loading = ref(false)
const isFollowing = ref(false)

async function toggleFollow() {
  if (!props.username) return
  loading.value = true
  try {
    // tu kiedyś wyślemy żądanie HTTP
    await new Promise(resolve => setTimeout(resolve, 300))
    isFollowing.value = !isFollowing.value
  } finally {
    loading.value = false
  }
}
</script>
