<template>
  <div class="space-y-8">
    <section
        class="relative bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6 md:p-8"
    >
      <!-- potwierdzenie zgłoszenia profilu -->
      <div
          v-if="reportSuccess"
          class="absolute top-3 right-4 z-40 pointer-events-none"
      >
        <div
            class="rounded-md border border-emerald-500
                 bg-emerald-100 dark:bg-emerald-900
                 px-3 py-1.5 text-xs
                 text-emerald-800 dark:text-emerald-100
                 shadow-lg"
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
              class="h-28 w-28 rounded-full bg-[var(--color-bg)]
                   ring-4 ring-[var(--color-border)] flex items-center justify-center overflow-hidden"
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
            <!-- posty danego użytkownika – tylko na własnym profilu -->
            <RouterLink
                v-if="isOwner"
                :to="`/posts?userId=${encodeURIComponent(user?.id ?? '')}`"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4
                     rounded-full text-xs font-medium
                     bg-[var(--color-primary)] hover:bg-[var(--color-primary-600)]
                     text-white transition shadow"
                title="Zobacz swoje posty"
            >
              Posty użytkownika
            </RouterLink>

            <!-- Obserwuj -->
            <UserFollowButton
                v-if="!isOwner && displayedUsername"
                :username="displayedUsername"
            />

            <!-- Zgłoś profil -->
            <button
                v-if="!isOwner && displayedUsername"
                type="button"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4
                     rounded-full text-xs font-medium
                     border border-red-500/80 text-red-500/90
                     bg-[var(--color-bg)] hover:bg-red-500/10"
                @click="toggleReportPanel"
            >
              Zgłoś profil
            </button>

            <!-- edycja profilu zabezp -->
            <button
                v-if="isOwner"
                type="button"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4
                     rounded-full text-xs font-medium
                     border border-[var(--color-border)]
                     bg-[var(--color-bg)] text-[var(--color-text)] hover:opacity-90"
                @click="toggleEdit"
            >
              {{ editMode ? 'Zamknij edycję' : 'Edytuj profil' }}
            </button>

            <!-- odświeżenie danych -->
            <button
                type="button"
                @click="onRefresh"
                class="inline-flex items-center justify-center min-w-[140px] h-9 px-4
                     rounded-full text-xs font-medium
                     border border-[var(--color-border)]
                     bg-[var(--color-bg)] text-[var(--color-text)] hover:opacity-90"
                :disabled="loading"
            >
              Odśwież
            </button>
          </div>

          <!-- formularz edycji profilu-->
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
                      class="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm"
                      autocomplete="off"
                  />
                </label>

                <label class="text-sm space-y-1">
                  <span class="block text-[var(--color-muted)]">Nowe zdjęcie</span>
                  <input
                      ref="avatarInput"
                      type="file"
                      accept="image/*"
                      class="block w-full text-sm text-[var(--color-muted)]"
                  />
                </label>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <label class="text-sm space-y-1">
                  <span class="block text-[var(--color-muted)]">Aktualne hasło</span>
                  <input
                      v-model="currentPassword"
                      type="password"
                      class="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm"
                      autocomplete="current-password"
                  />
                </label>

                <label class="text-sm space-y-1">
                  <span class="block text-[var(--color-muted)]">Nowe hasło</span>
                  <input
                      v-model="newPassword"
                      type="password"
                      class="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm"
                      autocomplete="new-password"
                  />
                </label>
              </div>

              <div class="flex flex-wrap gap-3">
                <button
                    type="submit"
                    class="inline-flex items-center rounded-xl px-4 py-2 text-sm font-medium
                         bg-[var(--color-primary)] hover:bg-[var(--color-primary-600)] text-white transition shadow"
                    :disabled="saving"
                >
                  Zapisz zmiany
                </button>
                <button
                    type="button"
                    class="inline-flex items-center rounded-xl px-4 py-2 text-sm font-medium
                         border border-[var(--color-border)]
                         bg-[var(--color-bg)] text-[var(--color-text)] hover:opacity-90"
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
                Zapisano zmiany (backend do podpięcia).
              </p>
            </form>
          </div>

          <p v-if="auth.error" class="mt-3 text-sm text-red-600">{{ auth.error }}</p>
        </div>
      </div>
    </section>

    <!-- Sekcje profilu -->
    <section class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Obserwowani – osobny komponent -->
      <ProfileFriendsSection />

      <!-- Grupy użytkownika -->
      <div
          class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6"
      >
        <h3 class="text-lg font-semibold mb-4">Grupy użytkownika</h3>

        <div
            v-if="groupsLoading"
            class="h-28 rounded-xl border-2 border-[var(--color-border)]
                 flex items-center justify-center text-[var(--color-muted)] text-center px-4"
        >
          Ładowanie listy grup...
        </div>

        <div
            v-else-if="groupsError"
            class="h-28 rounded-xl border-2 border-red-500/60 bg-red-500/5
                 flex items-center justify-center text-xs text-red-300 text-center px-4"
        >
          {{ groupsError }}
        </div>

        <div
            v-else-if="groups.length === 0"
            class="h-28 rounded-xl border-2 border-[var(--color-border)]
                 flex items-center justify-center text-[var(--color-muted)] text-center px-4"
        >
          Brak grup użytkownika.
        </div>

        <div
            v-else
            class="max-h-48 rounded-xl border-2 border-[var(--color-border)]
                 overflow-y-auto divide-y divide-[var(--color-border)]"
        >
          <div
              v-for="group in groups"
              :key="group.id"
              class="flex items-center justify-between px-4 py-3 gap-3"
          >
            <div>
              <p class="text-sm font-medium">
                {{ group.name }}
              </p>
              <p v-if="group.memberCount != null" class="text-xs text-[var(--color-muted)]">
                {{ group.memberCount }} członków
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Łowiska użytkownika -->
      <div
          class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6"
      >
        <h3 class="text-lg font-semibold mb-4">Łowiska użytkownika</h3>

        <div
            v-if="ownedSpots.length === 0 && favouriteSpots.length === 0"
            class="h-28 rounded-xl border-2 border-[var(--color-border)]
                 flex items-center justify-center text-[var(--color-muted)] text-center px-4"
        >
          Brak przypisanych łowisk. Ta sekcja pokaże polubione i zgłoszone łowiska
          użytkownika (z przekierowaniem do mapy, gdy backend będzie gotowy).
        </div>

        <div
            v-else
            class="max-h-48 rounded-xl border-2 border-[var(--color-border)]
                 overflow-y-auto divide-y divide-[var(--color-border)]"
        >
          <template v-if="ownedSpots.length">
            <div class="bg-black/10 px-4 py-2 text-xs text-[var(--color-muted)]">
              Twoje łowiska
            </div>
            <div
                v-for="spot in ownedSpots"
                :key="'own-' + (spot.id ?? spotDisplayName(spot))"
                class="px-4 py-2 flex flex-col gap-0.5"
            >
              <span class="text-sm font-medium">
                {{ spotDisplayName(spot) }}
              </span>
              <span
                  v-if="spotLocation(spot)"
                  class="text-xs text-[var(--color-muted)]"
              >
                {{ spotLocation(spot) }}
              </span>
            </div>
          </template>

          <template v-if="favouriteSpots.length">
            <div class="bg-black/10 px-4 py-2 text-xs text-[var(--color-muted)]">
              Polubione łowiska
            </div>
            <div
                v-for="spot in favouriteSpots"
                :key="'fav-' + (spot.id ?? spotDisplayName(spot))"
                class="px-4 py-2 flex flex-col gap-0.5"
            >
              <span class="text-sm font-medium">
                {{ spotDisplayName(spot) }}
              </span>
              <span
                  v-if="spotLocation(spot)"
                  class="text-xs text-[var(--color-muted)]"
              >
                {{ spotLocation(spot) }}
              </span>
            </div>
          </template>
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

const loggedUser = computed(() => userStore.me || auth.user || null)

const displayedUsername = computed(() => {
  const fromQuery = route.query.u
  if (typeof fromQuery === 'string' && fromQuery.trim().length > 0) {
    return fromQuery
  }
  return loggedUser.value?.username || ''
})

const user = computed(() => loggedUser.value)
const avatarUrl = computed(() => user.value?.avatarUrl || '')

const isOwner = computed(() => {
  if (!loggedUser.value?.username || !displayedUsername.value) return false
  return loggedUser.value.username === displayedUsername.value
})

const fullName = computed(() => {
  const first = user.value?.name || ''
  const last = user.value?.surname || ''
  const text = `${first} ${last}`.trim()
  return text || '—'
})

const loading = ref(false)

// EDYCJA PROFILU
const editMode = ref(false)
const editUsername = ref('')
const currentPassword = ref('')
const newPassword = ref('')
const saving = ref(false)
const saveError = ref('')
const saveSuccess = ref(false)
const avatarInput = ref(null)

watch(
    () => editMode.value,
    value => {
      if (value && user.value) {
        editUsername.value = user.value.username || ''
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

async function onSaveProfile() {
  saving.value = true
  saveError.value = ''
  saveSuccess.value = false
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    saveSuccess.value = true
  } catch (err) {
    saveError.value =
        err?.response?.data?.message || err?.message || 'Nie udało się zapisać zmian.'
  } finally {
    saving.value = false
  }
}

// ZGŁOSZENIE PROFILU
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

async function sendProfileReport(reasonKey) {
  sendingReport.value = true
  reportError.value = ''
  try {
    await new Promise(resolve => setTimeout(resolve, 400))
    reporting.value = false
    reportSuccess.value = true
    setTimeout(() => {
      reportSuccess.value = false
    }, 2500)
  } catch {
    reportError.value = 'Nie udało się wysłać zgłoszenia profilu.'
  } finally {
    sendingReport.value = false
  }
}

function cancelReport() {
  reporting.value = false
  reportError.value = ''
}

// GRUPY UŻYTKOWNIKA
const groups = ref([])
const groupsLoading = ref(false)
const groupsError = ref('')

async function loadUserGroups() {
  if (!isOwner.value) {
    groups.value = []
    groupsError.value = ''
    groupsLoading.value = false
    return
  }

  groupsLoading.value = true
  groupsError.value = ''

  try {
    const { data } = await apiClient.get('/users/me/groups')
    const items = Array.isArray(data?.content) ? data.content : []
    groups.value = items.map(g => ({
      id: g.id,
      name: g.name,
      memberCount: g.memberCount ?? null,
    }))
  } catch (err) {
    console.error('loadUserGroups error', err)
    groupsError.value = 'Nie udało się pobrać grup użytkownika (błąd serwera).'
  } finally {
    groupsLoading.value = false
  }
}

// ŁOWISKA – z danych użytkownika (jeśli backend je zwróci)
const favouriteSpots = computed(() => {
  const raw = user.value?.favouriteSpots || user.value?.favoriteSpots || []
  return Array.isArray(raw) ? raw : []
})

const ownedSpots = computed(() => {
  const raw = user.value?.ownedSpots || user.value?.mySpots || []
  return Array.isArray(raw) ? raw : []
})

function spotDisplayName(spot) {
  if (!spot) return 'Łowisko'
  if (typeof spot === 'string') return spot
  return spot.name || spot.title || 'Łowisko'
}

function spotLocation(spot) {
  if (!spot || typeof spot === 'string') return ''
  return spot.voivodeship || spot.region || spot.address || ''
}

// odświeżenie danych profilu
async function onRefresh() {
  loading.value = true
  try {
    await Promise.all([
      auth.bootstrapSession(),
      userStore.fetchMe(true),
    ])
    await loadUserGroups()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  onRefresh().catch(() => {})
})
</script>
