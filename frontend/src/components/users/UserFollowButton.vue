<template>
  <div class="inline-flex flex-col">
    <button
        type="button"
        class="min-w-[140px] h-9 px-4 rounded-full text-xs font-medium border transition
             flex items-center justify-center gap-1 disabled:opacity-60"
        :class="buttonClass"
        @click="onClick"
        :disabled="loading || isFollowing || isMe"
        :title="buttonTitle"
    >
      <span v-if="loading">...</span>
      <span v-else>
        {{ isFollowing ? 'Obserwujesz' : 'Obserwuj' }}
      </span>
    </button>

    <p v-if="info" class="mt-1 text-[10px] text-[var(--color-muted)] max-w-xs">
      {{ info }}
    </p>

    <p v-if="error" class="mt-1 text-[10px] text-red-500 max-w-xs">
      {{ error }}
    </p>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useUserStore } from '../../stores/userStore.js'

const props = defineProps({
  username: {
    type: String,
    required: true,
  },
})

const userStore = useUserStore()

const loading = ref(false)
const error = ref('')
const info = ref('')

watch(
    () => props.username,
    () => {
      error.value = ''
      info.value = ''
    },
)

// czy ten użytkownik to ja sam
const isMe = computed(() => {
  const me = userStore.me
  return !!me && me.username === props.username
})

// lista obserwowanych (friends)
const friends = computed(() => userStore.friends || [])

// czy już obserwuję
const isFollowing = computed(() =>
    friends.value.some(f => f.username === props.username),
)

const buttonClass = computed(() => {
  if (isFollowing.value) {
    return 'bg-[var(--color-primary)] border-[var(--color-primary)] text-white'
  }
  return 'bg-[var(--color-bg)] border-[var(--color-border)] text-[var(--color-text)] hover:bg-[var(--color-primary)] hover:text-white'
})

const buttonTitle = computed(() => {
  if (isMe.value) return 'To Twój profil'
  if (isFollowing.value) return 'Użytkownik jest już na liście obserwowanych'
  return 'Wyślij zaproszenie do obserwowania'
})

async function onClick() {
  error.value = ''
  info.value = ''

  if (isMe.value || !props.username) return
  if (isFollowing.value) return

  loading.value = true
  try {
    await userStore.followUserByUsername(props.username)
    info.value = 'Wysłano zaproszenie.'
  } catch (err) {
    console.error('followUserByUsername error', err)
    error.value =
        userStore.followError ||
        err?.response?.data?.message ||
        err?.message ||
        'Nie udało się wysłać zaproszenia (błąd serwera).'
  } finally {
    loading.value = false
  }
}
</script>
