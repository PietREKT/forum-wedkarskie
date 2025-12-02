<template>
  <button
      v-if="canFollow"
      type="button"
      class="px-2 py-1 text-[11px] rounded-md border theme-border
           hover:bg-zinc-100 dark:hover:bg-zinc-800"
      @click="toggleFollow"
  >
    {{ isFollowing ? 'Obserwujesz' : 'Obserwuj' }}
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { useFollowsStore } from '../../stores/follows'
import { useAuthStore } from '../../stores/auth'

const props = defineProps({
  username: { type: String, required: true },
})

const follows = useFollowsStore()
const auth = useAuthStore()

const isFollowing = computed(() => follows.isFollowed(props.username))

const canFollow = computed(() => {
  const current = auth.user?.username
  if (!current) return false
  return current !== props.username
})

function toggleFollow() {
  follows.toggle(props.username)
}
</script>
