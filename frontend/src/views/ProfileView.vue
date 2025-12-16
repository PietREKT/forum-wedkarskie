<!-- src/views/ProfileView.vue -->
<template>
  <div class="space-y-8">
    <section
        class="relative bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6 md:p-8"
    >
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 items-start">
        <!-- Avatar + pseudonim -->
        <div class="flex items-center md:block gap-5">
          <div
              class="h-28 w-28 rounded-full bg-[var(--color-bg)] ring-4 ring-[var(--color-border)] flex items-center justify-center overflow-hidden"
          >
            <img
                v-if="avatarUrl"
                :src="avatarUrl"
                alt="Zdjęcie profilowe"
                class="h-full w-full object-cover"
            />
            <span v-else class="text-[var(--color-muted)] text-sm">zdjęcie</span>
          </div>

          <div class="mt-4 md:mt-6">
            <p class="text-xs text-[var(--color-muted)]">Pseudonim</p>
            <h2 class="text-xl font-semibold">
              {{ displayedUsername || 'użytkownik' }}
            </h2>
          </div>
        </div>

        <!-- Dane tekstowe + akcje -->
        <div class="md:col-span-2">
          <h3 class="text-lg font-semibold mb-1" v-if="fullName">Imię i nazwisko</h3>
          <p class="text-base mb-4" v-if="fullName">
            {{ fullName }}
          </p>

          <div class="mt-2 flex flex-wrap gap-3">
            <RouterLink
                v-if="profileUser?.id"
                :to="`/posts?userId=${encodeURIComponent(profileUser.id)}`"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4 rounded-full text-xs font-medium bg-[var(--color-primary)] hover:bg-[var(--color-primary-600)] text-white transition shadow"
                title="Zobacz posty użytkownika"
            >
              Posty użytkownika
            </RouterLink>

            <UserFollowButton
                v-if="isAuthenticated && !isOwner && displayedUsername"
                :username="displayedUsername"
            />

            <button
                v-if="isOwner"
                type="button"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4 rounded-full text-xs font-medium border border-[var(--color-border)] bg-[var(--color-bg)] text-[var(--color-text)] hover:opacity-90"
                @click="toggleEdit"
            >
              {{ editMode ? 'Zamknij edycję' : 'Edytuj profil' }}
            </button>

            <button
                type="button"
                @click="onRefresh"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4 rounded-full text-xs font-medium border border-[var(--color-border)] bg-[var(--color-bg)] text-[var(--color-text)] hover:opacity-90"
                :disabled="loading"
            >
              Odśwież
            </button>
          </div>

          <!-- formularz edycji profilu (tylko zdjęcie) -->
          <div
              v-if="editMode && isOwner"
              class="mt-6 border-t border-[var(--color-border)] pt-4 space-y-4"
          >
            <h4 class="text-sm font-semibold">Edycja profilu</h4>

            <form class="space-y-4" @submit.prevent="onSaveProfile">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <label class="text-sm space-y-1">
                  <span class="block text-[var(--color-muted)]">Nowe zdjęcie</span>
                  <input
                      ref="avatarInput"
                      type="file"
                      accept="image/*"
                      class="block w-full text-sm text-[var(--color-muted)]"
                      @change="onAvatarChange"
                  />
                </label>
              </div>

              <div class="flex flex-wrap gap-3">
                <button
                    type="submit"
                    class="inline-flex items-center rounded-xl px-4 py-2 text-sm font-medium bg-[var(--color-primary)] hover:bg-[var(--color-primary-600)] text-white transition shadow disabled:opacity-60"
                    :disabled="saving || !selectedAvatarFile"
                >
                  Zapisz zdjęcie
                </button>
                <button
                    type="button"
                    class="inline-flex items-center rounded-xl px-4 py-2 text-sm font-medium border border-[var(--color-border)] bg-[var(--color-bg)] text-[var(--color-text)] hover:opacity-90"
                    @click="cancelEdit"
                    :disabled="saving"
                >
                  Anuluj
                </button>
              </div>

              <p v-if="saveError" class="text-sm text-red-600">
                {{ saveError }}
              </p>
              <p v-if="saveSuccess" class="text-sm text-emerald-500">
                Zapisano zdjęcie profilowe.
              </p>
            </form>
          </div>

          <p v-if="profileError" class="mt-3 text-sm text-red-600">
            {{ profileError }}
          </p>
        </div>
      </div>
    </section>

    <section class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <ProfileFriendsSection v-if="isAuthenticated" />

      <div class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6">
        <h3 class="text-lg font-semibold mb-4">Grupy użytkownika</h3>

        <div
            v-if="groupsLoading"
            class="h-28 rounded-xl border-2 border-[var(--color-border)] flex items-center justify-center text-[var(--color-muted)] text-center px-4"
        >
          Ładowanie listy grup...
        </div>

        <div
            v-else-if="groupsError"
            class="h-28 rounded-xl border-2 border-red-500/60 bg-red-500/5 flex items-center justify-center text-xs text-red-300 text-center px-4"
        >
          {{ groupsError }}
        </div>

        <div
            v-else-if="groups.length === 0"
            class="h-28 rounded-xl border-2 border-[var(--color-border)] flex items-center justify-center text-[var(--color-muted)] text-center px-4"
        >
          Brak grup użytkownika.
        </div>

        <div
            v-else
            class="max-h-48 rounded-xl border-2 border-[var(--color-border)] overflow-y-auto divide-y divide-[var(--color-border)]"
        >
          <div
              v-for="group in groups"
              :key="group.id"
              class="flex items-center justify-between px-4 py-3 gap-3"
          >
            <div>
              <p class="text-sm font-medium">{{ group.name }}</p>
              <p v-if="group.memberCount != null" class="text-xs text-[var(--color-muted)]">
                {{ group.memberCount }} członków
              </p>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6">
        <h3 class="text-lg font-semibold mb-4">Łowiska użytkownika</h3>

        <div
            v-if="favouriteSpots.length === 0"
            class="h-28 rounded-xl border-2 border-[var(--color-border)] flex items-center justify-center text-[var(--color-muted)] text-center px-4"
        >
          Brak ulubionych łowisk.
        </div>

        <div
            v-else
            class="max-h-48 rounded-xl border-2 border-[var(--color-border)] overflow-y-auto divide-y divide-[var(--color-border)]"
        >
          <div class="bg-black/10 px-4 py-2 text-xs text-[var(--color-muted)]">
            Ulubione łowiska
          </div>

          <RouterLink
              v-for="spot in favouriteSpots"
              :key="'fav-' + (spot.id ?? spotDisplayName(spot))"
              :to="spotLink(spot)"
              class="px-4 py-2 flex flex-col gap-0.5 hover:bg-black/5"
              title="Przejdź do mapy"
          >
            <span class="text-sm font-medium">{{ spotDisplayName(spot) }}</span>
            <span v-if="spotLocation(spot)" class="text-xs text-[var(--color-muted)]">
              {{ spotLocation(spot) }}
            </span>
          </RouterLink>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { useUserStore } from '../stores/userStore.js'
import { apiClient } from '../utils/axios.js'
import UserFollowButton from '../components/users/UserFollowButton.vue'
import ProfileFriendsSection from '../components/users/ProfileFriendsSection.vue'

const route = useRoute()
const auth = useAuthStore()
const userStore = useUserStore()

const loading = ref(false)
const profileError = ref('')

const isAuthenticated = computed(() => !!(userStore.me || auth.user))
const loggedUser = computed(() => userStore.me || auth.user || null)

const targetUsername = computed(() => {
  const fromQuery = route.query.u
  if (typeof fromQuery === 'string' && fromQuery.trim().length > 0) return fromQuery.trim()
  return loggedUser.value?.username || ''
})

const displayedUsername = computed(() => targetUsername.value || '')

const isOwner = computed(() => {
  if (!isAuthenticated.value) return false
  if (!loggedUser.value?.username || !targetUsername.value) return false
  return String(loggedUser.value.username) === String(targetUsername.value)
})

const profileUser = ref(null)

const avatarBust = ref(0)

const rawAvatarUrl = computed(() => {
  return profileUser.value?.avatarUrl || profileUser.value?.profilePicUrl || ''
})

const avatarUrl = computed(() => {
  const u = rawAvatarUrl.value
  if (!u) return ''
  const sep = u.includes('?') ? '&' : '?'
  return `${u}${sep}v=${avatarBust.value}`
})

const fullName = computed(() => {
  const p = profileUser.value || {}
  const first = String(p.firstName ?? p.name ?? '').trim()
  const last = String(p.lastName ?? p.surname ?? '').trim()
  const joined = [first, last].filter(Boolean).join(' ')
  return joined || ''
})

const editMode = ref(false)
const saving = ref(false)
const saveError = ref('')
const saveSuccess = ref(false)
const avatarInput = ref(null)
const selectedAvatarFile = ref(null)

function onAvatarChange(e) {
  selectedAvatarFile.value = e?.target?.files?.[0] || null
  saveError.value = ''
  saveSuccess.value = false
}

watch(
    () => editMode.value,
    value => {
      if (value) {
        saveError.value = ''
        saveSuccess.value = false
        selectedAvatarFile.value = null
        if (avatarInput.value) avatarInput.value.value = ''
      }
    },
)

function toggleEdit() {
  editMode.value = !editMode.value
}

function cancelEdit() {
  editMode.value = false
}

async function uploadAvatar(file) {
  const form = new FormData()
  form.append('file', file)
  const { data } = await apiClient.post('/users/me/pic', form)
  return data
}

async function onSaveProfile() {
  saving.value = true
  saveError.value = ''
  saveSuccess.value = false

  try {
    const file = selectedAvatarFile.value
    if (!file) {
      saveError.value = 'Wybierz plik ze zdjęciem.'
      return
    }

    await uploadAvatar(file)

    await userStore.fetchMe(true)

    if (isOwner.value) {
      profileUser.value = userStore.me || auth.user || profileUser.value
    } else {
      await loadProfileUser()
    }

    avatarBust.value = Date.now()

    saveSuccess.value = true
    setTimeout(() => {
      saveSuccess.value = false
    }, 2500)
  } catch (err) {
    console.error('upload avatar error', err)
    saveError.value =
        err?.response?.data?.message || err?.message || 'Nie udało się zapisać zdjęcia.'
  } finally {
    saving.value = false
  }
}

const groups = ref([])
const groupsLoading = ref(false)
const groupsError = ref('')

async function loadUserGroups() {
  groupsLoading.value = true
  groupsError.value = ''
  groups.value = []
  try {
    if (!profileUser.value?.id) return

    if (isOwner.value) {
      const { data } = await apiClient.get('/users/me/groups')
      const items = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : []
      groups.value = items.map(g => ({ id: g.id, name: g.name, memberCount: g.memberCount ?? null }))
      return
    }

    const { data } = await apiClient.get(`/users/${encodeURIComponent(profileUser.value.id)}/groups`)
    const items = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : []
    groups.value = items.map(g => ({ id: g.id, name: g.name, memberCount: g.memberCount ?? null }))
  } catch (err) {
    console.error('loadUserGroups error', err)
    groupsError.value = 'Nie udało się pobrać grup użytkownika (błąd serwera).'
  } finally {
    groupsLoading.value = false
  }
}

const favouriteSpotsState = ref([])
const favouriteSpots = computed(() => (Array.isArray(favouriteSpotsState.value) ? favouriteSpotsState.value : []))

async function loadFavouriteSpots() {
  favouriteSpotsState.value = []
  try {
    if (!profileUser.value?.id) return

    if (isOwner.value) {
      const { data } = await apiClient.get('/users/me/spots/favourites')
      favouriteSpotsState.value = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : []
      return
    }

    const { data } = await apiClient.get(`/users/${encodeURIComponent(profileUser.value.id)}/spots/favourites`)
    favouriteSpotsState.value = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : []
  } catch (err) {
    console.error('loadFavouriteSpots error', err)
  }
}

function spotDisplayName(spot) {
  if (!spot) return 'Łowisko'
  if (typeof spot === 'string') return spot
  return spot.name || spot.title || 'Łowisko'
}

function spotLocation(spot) {
  if (!spot || typeof spot === 'string') return ''
  return spot.voivodeship || spot.region || spot.address || ''
}

function spotLink(spot) {
  if (spot?.id) return `/map?spotId=${encodeURIComponent(spot.id)}`
  if (spot?.latitude != null && spot?.longitude != null) {
    return `/map?lat=${encodeURIComponent(spot.latitude)}&lng=${encodeURIComponent(spot.longitude)}`
  }
  return '/map'
}

async function searchUserIdByUsername(username) {
  const u = String(username || '').trim()
  if (!u) return null

  const { data } = await apiClient.get('/users/search', { params: { q: u } })
  const list = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : []
  const found =
      list.find(x => String(x?.username || '').toLowerCase() === u.toLowerCase()) || list[0] || null

  return found?.id || null
}

async function loadProfileUser() {
  profileError.value = ''

  const u = targetUsername.value
  if (!u) {
    profileUser.value = null
    profileError.value = 'Nie wskazano użytkownika.'
    return
  }

  if (isAuthenticated.value) {
    try {
      await Promise.all([auth.bootstrapSession?.(), userStore.fetchMe(true)])
    } catch {
      // pomijamy
    }
  }

  if (isOwner.value) {
    profileUser.value = userStore.me || auth.user || null
    return
  }

  try {
    const id = await searchUserIdByUsername(u)
    if (!id) {
      profileUser.value = null
      profileError.value = 'Nie znaleziono użytkownika.'
      return
    }

    const full = await apiClient.get(`/users/${encodeURIComponent(id)}`)
    profileUser.value = full.data
  } catch (err) {
    console.error('loadProfileUser error', err)
    profileUser.value = null
    profileError.value = 'Nie udało się pobrać profilu użytkownika (błąd serwera).'
  }
}

async function onRefresh() {
  loading.value = true
  try {
    await loadProfileUser()
    await Promise.all([loadUserGroups(), loadFavouriteSpots()])
  } finally {
    loading.value = false
  }
}

watch(
    () => route.query.u,
    () => {
      onRefresh().catch(() => {})
    },
)

onMounted(() => {
  onRefresh().catch(() => {})
})
</script>
