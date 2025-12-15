<!-- src/views/ProfileView.vue -->
<template>
  <div class="space-y-8">
    <section
        class="relative bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6 md:p-8"
    >
      <!-- potwierdzenie zgłoszenia profilu -->
      <div v-if="reportSuccess" class="absolute top-3 right-4 z-40 pointer-events-none">
        <div
            class="rounded-md border border-emerald-500 bg-emerald-100 dark:bg-emerald-900 px-3 py-1.5 text-xs text-emerald-800 dark:text-emerald-100 shadow-lg"
        >
          Zgłoszenie profilu zostało wysłane.
        </div>
      </div>

      <!-- panel wyboru powodu zgłoszenia -->
      <ReportPanel
          v-if="reporting"
          :reasons="reportReasons"
          :loading="sendingReport"
          :error="reportError"
          @select-reason="sendProfileReport"
          @cancel="cancelReport"
      />

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
          <h3 class="text-lg font-semibold mb-1">Imię i nazwisko</h3>
          <p class="text-base mb-4">
            {{ fullName }}
          </p>

          <div class="mt-2 flex flex-wrap gap-3">
            <!-- posty użytkownika (też dla cudzych profili) -->
            <RouterLink
                v-if="profileUser?.id"
                :to="`/posts?userId=${encodeURIComponent(profileUser.id)}`"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4 rounded-full text-xs font-medium bg-[var(--color-primary)] hover:bg-[var(--color-primary-600)] text-white transition shadow"
                title="Zobacz posty użytkownika"
            >
              Posty użytkownika
            </RouterLink>

            <!-- Obserwuj (tylko gdy zalogowany i nie swój profil) -->
            <UserFollowButton
                v-if="isAuthenticated && !isOwner && displayedUsername"
                :username="displayedUsername"
            />

            <!-- Zgłoś profil (tylko gdy zalogowany i nie swój profil) -->
            <button
                v-if="isAuthenticated && !isOwner && displayedUsername"
                type="button"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4 rounded-full text-xs font-medium border border-red-500/80 text-red-500/90 bg-[var(--color-bg)] hover:bg-red-500/10"
                @click="toggleReportPanel"
            >
              Zgłoś profil
            </button>

            <!-- edycja profilu -->
            <button
                v-if="isOwner"
                type="button"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4 rounded-full text-xs font-medium border border-[var(--color-border)] bg-[var(--color-bg)] text-[var(--color-text)] hover:opacity-90"
                @click="toggleEdit"
            >
              {{ editMode ? 'Zamknij edycję' : 'Edytuj profil' }}
            </button>

            <!-- odśwież -->
            <button
                type="button"
                @click="onRefresh"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4 rounded-full text-xs font-medium border border-[var(--color-border)] bg-[var(--color-bg)] text-[var(--color-text)] hover:opacity-90"
                :disabled="loading"
            >
              Odśwież
            </button>
          </div>

          <!-- formularz edycji profilu -->
          <div
              v-if="editMode && isOwner"
              class="mt-6 border-t border-[var(--color-border)] pt-4 space-y-4"
          >
            <h4 class="text-sm font-semibold">Edycja profilu</h4>

            <form class="space-y-4" @submit.prevent="onSaveProfile">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <label class="text-sm space-y-1">
                  <span class="block text-[var(--color-muted)]">Nowy pseudonim</span>
                  <input
                      v-model="editUsername"
                      type="text"
                      class="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm opacity-60"
                      autocomplete="off"
                      disabled
                  />
                  <span class="block text-[10px] text-[var(--color-muted)]">
                    Brak endpointu do zmiany pseudonimu po stronie serwera.
                  </span>
                </label>

                <label class="text-sm space-y-1">
                  <span class="block text-[var(--color-muted)]">Nowe zdjęcie</span>
                  <input
                      ref="avatarInput"
                      type="file"
                      accept="image/*"
                      class="block w-full text-sm text-[var(--color-muted)]"
                  />
                  <span class="block text-[10px] text-[var(--color-muted)]">
                    Wysyłane jako multipart pod kluczem <b>file</b> na <b>POST /users/me/pic</b>.
                  </span>
                </label>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <label class="text-sm space-y-1">
                  <span class="block text-[var(--color-muted)]">Aktualne hasło</span>
                  <input
                      v-model="currentPassword"
                      type="password"
                      class="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm opacity-60"
                      autocomplete="current-password"
                      disabled
                  />
                </label>

                <label class="text-sm space-y-1">
                  <span class="block text-[var(--color-muted)]">Nowe hasło</span>
                  <input
                      v-model="newPassword"
                      type="password"
                      class="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm opacity-60"
                      autocomplete="new-password"
                      disabled
                  />
                  <span class="block text-[10px] text-[var(--color-muted)]">
                    Brak endpointu do zmiany hasła po stronie serwera.
                  </span>
                </label>
              </div>

              <div class="flex flex-wrap gap-3">
                <button
                    type="submit"
                    class="inline-flex items-center rounded-xl px-4 py-2 text-sm font-medium bg-[var(--color-primary)] hover:bg-[var(--color-primary-600)] text-white transition shadow disabled:opacity-60"
                    :disabled="saving || !selectedAvatarFile"
                >
                  Zapisz zmiany
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

    <!-- Sekcje profilu -->
    <section class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Obserwowani – tylko na swoim profilu i tylko gdy zalogowany -->
      <ProfileFriendsSection v-if="isAuthenticated" />

      <!-- Grupy użytkownika -->
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

      <!-- Łowiska użytkownika -->
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
import ReportPanel from '../components/common/ReportPanel.vue'
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
  return loggedUser.value.username === targetUsername.value
})

const profileUser = ref(null)

const avatarUrl = computed(() => profileUser.value?.avatarUrl || profileUser.value?.profilePicUrl || '')

const fullName = computed(() => {
  const first = profileUser.value?.name || ''
  const last = profileUser.value?.surname || ''
  const text = `${first} ${last}`.trim()
  return text || '—'
})

/* EDYCJA PROFILU (na razie tylko zdjęcie) */
const editMode = ref(false)
const editUsername = ref('')
const currentPassword = ref('')
const newPassword = ref('')
const saving = ref(false)
const saveError = ref('')
const saveSuccess = ref(false)
const avatarInput = ref(null)
const selectedAvatarFile = computed(() => avatarInput.value?.files?.[0] || null)

watch(
    () => editMode.value,
    value => {
      if (value && profileUser.value) {
        editUsername.value = profileUser.value.username || ''
        currentPassword.value = ''
        newPassword.value = ''
        saveError.value = ''
        saveSuccess.value = false
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

  // ważne: NIE ustawiaj ręcznie Content-Type, axios sam doda boundary
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

    // odśwież dane zalogowanego i aktualny profil
    await userStore.fetchMe(true)
    if (isOwner.value) {
      profileUser.value = loggedUser.value
    } else {
      await loadProfileUser()
    }

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

/* ZGŁOSZENIE PROFILU (UI gotowe, backend do podpięcia jeśli brak) */
const reporting = ref(false)
const sendingReport = ref(false)
const reportError = ref('')
const reportSuccess = ref(false)

const reportReasons = [
  { key: 'INAPPROPRIATE_PHOTO', label: 'Nieodpowiednie zdjęcie profilowe' },
  { key: 'INAPPROPRIATE_NICK', label: 'Nieodpowiedni nick' },
  { key: 'HARASSMENT', label: 'Nękanie / obraźliwe treści' },
  { key: 'SPAM', label: 'Spam / reklama' },
  { key: 'OTHER', label: 'Inny powód' },
]

function toggleReportPanel() {
  reportError.value = ''
  reporting.value = !reporting.value
}

async function sendProfileReport() {
  sendingReport.value = true
  reportError.value = ''
  try {
    // brak pewnego endpointu -> zostawiamy jako informacja dla backendu
    reportError.value = 'Brak endpointu do zgłoszeń profili po stronie serwera.'
  } finally {
    sendingReport.value = false
  }
}

function cancelReport() {
  reporting.value = false
  reportError.value = ''
}

/* GRUPY */
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

    // jeśli backend nie ma takiego endpointu, to wyjdzie błąd -> pokażemy komunikat
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

/* ULUBIONE ŁOWISKA */
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
  // załóżmy, że masz trasę /map (dopasuj jeśli inaczej)
  if (spot?.id) return `/map?spotId=${encodeURIComponent(spot.id)}`
  if (spot?.latitude != null && spot?.longitude != null) {
    return `/map?lat=${encodeURIComponent(spot.latitude)}&lng=${encodeURIComponent(spot.longitude)}`
  }
  return '/map'
}

/* ŁADOWANIE PROFILU */
async function searchUserIdByUsername(username) {
  const u = String(username || '').trim()
  if (!u) return null

  // backend u Ciebie używa parametru q
  const { data } = await apiClient.get('/users/search', { params: { q: u } })
  const list = Array.isArray(data) ? data : Array.isArray(data?.content) ? data.content : []
  const found =
      list.find(x => String(x?.username || '').toLowerCase() === u.toLowerCase()) || list[0] || null

  return found?.id || null
}

async function loadProfileUser() {
  profileError.value = ''

  // jeśli ktoś nie jest zalogowany i nie poda ?u=... to nie mamy co pokazać
  const u = targetUsername.value
  if (!u) {
    profileUser.value = null
    profileError.value = 'Nie wskazano użytkownika.'
    return
  }

  // jeśli zalogowany, to pobierz me (żeby działało isOwner + follow listy)
  if (isAuthenticated.value) {
    try {
      await Promise.all([auth.bootstrapSession?.(), userStore.fetchMe(true)])
    } catch {
      // jeśli sesja padła, traktujemy jak niezalogowany
    }
  }

  if (isOwner.value) {
    profileUser.value = loggedUser.value
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
