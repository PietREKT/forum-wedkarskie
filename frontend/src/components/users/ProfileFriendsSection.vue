<!-- src/components/users/ProfileFriendsSection.vue -->
<template>
  <div
      v-if="isOwner"
      class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6 space-y-4"
  >
    <h3 class="text-lg font-semibold mb-2">Obserwowani</h3>

    <div class="space-y-2">
      <label class="block text-sm text-[var(--color-muted)]">
        Wyszukaj użytkownika po pseudonimie
      </label>
      <div class="flex gap-2">
        <input
            v-model="searchQuery"
            type="text"
            class="flex-1 rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm"
            placeholder="Podaj pseudonim użytkownika"
            @keyup.enter="onSearchUsers"
        />
        <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-medium bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-600)] disabled:opacity-60"
            @click="onSearchUsers"
            :disabled="searchLoading"
        >
          {{ searchLoading ? 'Szukam...' : 'Szukaj' }}
        </button>
      </div>
      <p v-if="searchError" class="text-xs text-red-500">
        {{ searchError }}
      </p>
    </div>

    <div
        v-if="searchResults.length"
        class="rounded-xl border border-[var(--color-border)] overflow-hidden"
    >
      <div class="bg-black/10 px-4 py-2 text-xs text-[var(--color-muted)]">
        Wyniki wyszukiwania
      </div>
      <div class="divide-y divide-[var(--color-border)] max-h-48 overflow-y-auto">
        <div
            v-for="u in searchResults"
            :key="u.id"
            class="flex items-center justify-between px-4 py-3 gap-3"
        >
          <div>
            <RouterLink
                :to="{ name: 'profile', query: { u: u.username } }"
                class="text-sm font-medium hover:underline"
            >
              {{ u.username }}
            </RouterLink>
            <p class="text-xs text-[var(--color-muted)]">
              {{ u.name || '—' }} {{ u.surname || '' }}
            </p>
          </div>

          <div class="flex flex-wrap gap-2">
            <RouterLink
                :to="{ name: 'profile', query: { u: u.username } }"
                class="px-3 py-1 text-xs rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] hover:opacity-90"
            >
              Profil
            </RouterLink>

            <button
                type="button"
                class="px-3 py-1 text-xs rounded-lg bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-600)] disabled:opacity-60"
                @click="onFollowUser(u)"
                :disabled="inviteLoadingId === u.id || isAlreadyFollowed(u)"
                :title="isAlreadyFollowed(u) ? 'Już obserwujesz' : 'Wyślij zaproszenie'"
            >
              {{
                isAlreadyFollowed(u)
                    ? 'Obserwujesz'
                    : inviteLoadingId === u.id
                        ? 'Wysyłanie...'
                        : 'Obserwuj'
              }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="space-y-2">
      <div class="flex items-center justify-between">
        <h4 class="text-sm font-semibold">Twoi obserwowani</h4>
      </div>

      <div
          v-if="friendsLoading"
          class="h-32 rounded-xl border-2 border-[var(--color-border)] flex items-center justify-center text-[var(--color-muted)] text-center px-4"
      >
        Ładowanie listy obserwowanych...
      </div>

      <div
          v-else-if="friends.length === 0"
          class="h-32 rounded-xl border-2 border-[var(--color-border)] flex items-center justify-center text-[var(--color-muted)] text-center px-4"
      >
        Brak obserwowanych użytkowników.
      </div>

      <div
          v-else
          class="max-h-48 rounded-xl border-2 border-[var(--color-border)] overflow-y-auto divide-y divide-[var(--color-border)]"
      >
        <div
            v-for="friend in friends"
            :key="friend.id"
            class="flex items-center justify-between px-4 py-3 gap-3"
        >
          <div>
            <RouterLink
                :to="{ name: 'profile', query: { u: friend.username } }"
                class="text-sm font-medium hover:underline"
            >
              {{ friend.username }}
            </RouterLink>
            <p class="text-xs text-[var(--color-muted)]">
              {{ friend.name || '—' }} {{ friend.surname || '' }}
            </p>
          </div>

          <div class="flex flex-wrap gap-2">
            <RouterLink
                :to="`/posts?userId=${encodeURIComponent(friend.id)}`"
                class="px-3 py-1 text-xs rounded-lg bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-600)]"
            >
              Posty
            </RouterLink>

            <span
                class="px-3 py-1 text-xs rounded-lg border border-[var(--color-border)] text-[var(--color-muted)]"
            >
              Brak opcji usunięcia
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/userStore.js'
import { useAuthStore } from '../../stores/auth.js'

const route = useRoute()
const userStore = useUserStore()
const authStore = useAuthStore()

const loggedUser = computed(() => userStore.me || authStore.user || null)

const displayedUsername = computed(() => {
  const fromQuery = route.query.u
  if (typeof fromQuery === 'string' && fromQuery.trim().length > 0) {
    return fromQuery
  }
  return loggedUser.value?.username || ''
})

const isOwner = computed(() => {
  if (!loggedUser.value?.username || !displayedUsername.value) return false
  return loggedUser.value.username === displayedUsername.value
})

const friends = computed(() => userStore.friends || [])
const friendsLoading = computed(() => userStore.status === 'loading' && !userStore.me)

const searchQuery = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const searchError = ref('')
const inviteLoadingId = ref(null)

function isAlreadyFollowed(u) {
  const username = String(u?.username || '')
  if (!username) return false
  return friends.value.some(f => f.username === username)
}

async function onSearchUsers() {
  const q = searchQuery.value.trim()
  if (!q) {
    searchResults.value = []
    searchError.value = ''
    return
  }

  searchLoading.value = true
  searchError.value = ''
  try {
    const list = await userStore.searchUsers(q)
    searchResults.value = Array.isArray(list) ? list : []
  } catch (err) {
    console.error('onSearchUsers error', err)
    searchError.value =
        userStore.searchError ||
        err?.response?.data?.message ||
        err?.message ||
        'Nie udało się wyszukać użytkowników (błąd serwera).'
  } finally {
    searchLoading.value = false
  }
}

async function onFollowUser(userToFollow) {
  if (!userToFollow?.id) return
  if (isAlreadyFollowed(userToFollow)) return

  inviteLoadingId.value = userToFollow.id
  try {
    await userStore.followUserById(userToFollow.id)
  } catch (err) {
    console.error('followUserById error', err)
  } finally {
    inviteLoadingId.value = null
  }
}
</script>
