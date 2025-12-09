<template>
  <div class="inline-flex flex-col">
    <button
        type="button"
        class="min-w-[140px] h-9 px-4 rounded-full text-xs font-medium border transition
         flex items-center justify-center gap-1"
        :class="buttonClass"
        @click="onClick"
        :disabled="loading"
    >
      <span v-if="loading">...</span>
      <span v-else>
        {{ isFollowing ? 'Obserwujesz' : 'Obserwuj' }}
      </span>
    </button>

    <p v-if="error" class="mt-1 text-[10px] text-red-500 max-w-xs">
      {{ error }}
    </p>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useUserStore } from '../../stores/userStore.js'
import {
  searchUsersByUsername,
  sendFriendInvite,
  removeFriend,
} from '../../utils/usersApi.js'

const props = defineProps({
  username: {
    type: String,
    required: true,
  },
})

// store z danymi zalogowanego usera
const userStore = useUserStore()

const loading = ref(false)
const error = ref('')

// czy ten użytkownik to ja sam
const isMe = computed(() => {
  const me = userStore.me
  return !!me && me.username === props.username
})

// lista  obserwowanych
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

async function onClick() {
  error.value = ''
  if (isMe.value || !props.username) return

  // anuluj obserwacje
  if (isFollowing.value) {
    const confirmUnfollow = window.confirm(
        'Czy na pewno chcesz zrezygnować z obserwowania tego użytkownika?',
    )
    if (!confirmUnfollow) return

    const friend = friends.value.find(f => f.username === props.username)
    if (!friend?.id) {
      error.value = 'Nie udało się znaleźć użytkownika na liście obserwowanych.'
      return
    }

    loading.value = true
    try {
      await removeFriend(friend.id)
      await userStore.fetchMe(true)
    } catch (err) {
      console.error('removeFriend error', err)
      error.value = 'Nie udało się usunąć z obserwowanych (błąd serwera).'
    } finally {
      loading.value = false
    }
    return
  }

  loading.value = true
  try {
    const { data } = await searchUsersByUsername(props.username)
    const list = Array.isArray(data) ? data : []
    const target = list.find(u => u.username === props.username)

    if (!target?.id) {
      error.value = 'Nie znaleziono takiego użytkownika.'
      return
    }

    await sendFriendInvite(target.id)
    await userStore.fetchMe(true)
  } catch (err) {
    console.error('sendFriendInvite/search error', err)
    error.value = 'Nie udało się wysłać zaproszenia (błąd serwera).'
  } finally {
    loading.value = false
  }
}
</script>
