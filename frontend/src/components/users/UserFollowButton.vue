<template>
  <div v-if="showButton" class="inline-flex flex-col">
    <button
        type="button"
        class="min-w-[140px] h-9 px-4 rounded-full text-xs font-medium border transition
             flex items-center justify-center gap-1 disabled:opacity-60"
        :class="buttonClass"
        @click="onClick"
        :disabled="loading || isFollowing || pendingInvite"
        :title="buttonTitle"
    >
      <span v-if="loading">...</span>
      <span v-else>
        {{ buttonText }}
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
import { computed, ref, watch, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../../stores/userStore.js'

const props = defineProps({
  username: { type: String, required: true },
})

const userStore = useUserStore()
const { me, friends } = storeToRefs(userStore)

const loading = ref(false)
const error = ref('')
const info = ref('')
const pendingInvite = ref(false)

watch(
    () => props.username,
    () => {
      error.value = ''
      info.value = ''
      pendingInvite.value = false
    },
)

onMounted(async () => {
  if (!me.value) {
    try {
      await userStore.fetchMe()
    } catch {
      // niezalogowany -> nie pokażemy przycisku
    }
  }
})

const isLogged = computed(() => !!me.value)

const isMe = computed(() => {
  const my = me.value
  const u = String(props.username || '').trim()
  if (!my || !u) return false
  return String(my.username || '').toLowerCase() === u.toLowerCase()
})

const isFollowing = computed(() => {
  const u = String(props.username || '').trim().toLowerCase()
  if (!u) return false
  return (friends.value || []).some(f => String(f?.username || '').toLowerCase() === u)
})

const showButton = computed(() => {
  if (!isLogged.value) return false
  if (!props.username) return false
  if (isMe.value) return false
  return true
})

const buttonText = computed(() => {
  if (isFollowing.value) return 'Obserwujesz'
  if (pendingInvite.value) return 'Zaproszenie wysłane'
  return 'Obserwuj'
})

const buttonClass = computed(() => {
  if (isFollowing.value) {
    return 'bg-[var(--color-primary)] border-[var(--color-primary)] text-white'
  }
  if (pendingInvite.value) {
    return 'bg-[var(--color-primary)] border-[var(--color-primary)] text-white opacity-80'
  }
  return 'bg-[var(--color-bg)] border-[var(--color-border)] text-[var(--color-text)] hover:bg-[var(--color-primary)] hover:text-white'
})

const buttonTitle = computed(() => {
  if (isFollowing.value) return 'Użytkownik jest już na liście obserwowanych'
  if (pendingInvite.value) return 'Zaproszenie zostało już wysłane'
  return 'Wyślij zaproszenie'
})

function normalizeBackendMessage(err) {
  const status = err?.response?.status
  const msg =
      err?.response?.data?.message ||
      err?.response?.data?.error ||
      err?.message ||
      ''

  // jeśli backend zwraca 409 (conflict) albo komunikat o istniejącym zaproszeniu
  const msgLower = String(msg).toLowerCase()
  if (
      status === 409 ||
      msgLower.includes('already') ||
      msgLower.includes('exists') ||
      msgLower.includes('zapros') ||
      msgLower.includes('invite')
  ) {
    return { kind: 'already_sent', text: 'Zaproszenie zostało już wysłane.' }
  }

  if (status === 401) return { kind: 'auth', text: 'Zaloguj się, aby obserwować.' }
  if (status === 403) return { kind: 'forbidden', text: 'Brak uprawnień do wykonania tej akcji.' }
  if (status === 404) return { kind: 'not_found', text: 'Nie znaleziono użytkownika.' }

  return { kind: 'other', text: 'Nie udało się wysłać zaproszenia (błąd serwera).' }
}

async function onClick() {
  error.value = ''
  info.value = ''

  if (!showButton.value) return

  // jeśli już obserwuje -> nic nie rób
  if (isFollowing.value) return

  // jeśli już wysłane w tej sesji -> pokaż komunikat zamiast walić requestem
  if (pendingInvite.value) {
    info.value = 'Zaproszenie zostało już wysłane.'
    return
  }

  loading.value = true
  try {
    await userStore.followUserByUsername(props.username)

    pendingInvite.value = true
    info.value = 'Wysłano zaproszenie.'

    // opcjonalnie: jeżeli masz endpoint "friends/pending", to tu byś odświeżył
    // await userStore.fetchPendingInvites()
  } catch (err) {
    console.error('followUserByUsername error', err)

    const mapped = normalizeBackendMessage(err)
    if (mapped.kind === 'already_sent') {
      pendingInvite.value = true
      info.value = mapped.text
      error.value = ''
    } else {
      error.value =
          userStore.followError ||
          mapped.text
    }
  } finally {
    loading.value = false
  }
}
</script>
