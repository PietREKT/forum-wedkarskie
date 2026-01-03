<template>
  <section class="space-y-4">
    <header class="flex items-start justify-between gap-3">
      <div>
        <div class="text-base font-semibold">Znajomi / obserwacje</div>
        <div class="text-xs opacity-70">
          Obserwowani: <span class="font-medium">{{ followingSafe.length }}</span>
        </div>
      </div>

      <button
          type="button"
          class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5 disabled:opacity-50"
          :disabled="busy"
          @click="refresh"
      >
        Odśwież
      </button>
    </header>

    <!-- Wyszukiwarka użytkownika (tylko u siebie) -->
    <div v-if="isOwnProfile" class="rounded-2xl border p-3 bg-[var(--color-surface)] space-y-3">
      <div class="text-sm font-semibold">Znajdź użytkownika</div>

      <div class="flex flex-col md:flex-row gap-2">
        <input
            v-model="query"
            type="text"
            class="w-full h-10 px-3 rounded-md border bg-[var(--color-bg)] text-[var(--color-text)]"
            placeholder="Wpisz nazwę użytkownika…"
            @keydown.enter.prevent="onSearch"
        />

        <div class="flex gap-2">
          <button
              type="button"
              class="h-10 px-4 rounded-md border bg-[var(--color-bg)] hover:bg-black/5 disabled:opacity-50"
              :disabled="searchLoading || !queryTrimmed"
              @click="onSearch"
          >
            Szukaj
          </button>

          <button
              type="button"
              class="h-10 px-4 rounded-md border bg-[var(--color-bg)] hover:bg-black/5 disabled:opacity-50"
              :disabled="searchLoading || !selectedUsername"
              @click="goToUser(selectedUsername)"
          >
            Profil
          </button>
        </div>
      </div>

      <div v-if="searchError" class="text-sm text-red-500">
        {{ searchError }}
      </div>

      <div v-if="searchLoading" class="text-sm opacity-70">
        Szukanie…
      </div>

      <div v-if="!searchLoading && searchResults.length > 0" class="space-y-2">
        <div class="text-xs opacity-70">Wyniki:</div>

        <ul class="space-y-1">
          <li
              v-for="u in searchResults"
              :key="u?.id || u?.username || JSON.stringify(u)"
              class="flex items-center justify-between gap-2 rounded-xl border px-3 py-2 bg-[var(--color-bg)]"
          >
            <button
                type="button"
                class="text-sm font-medium truncate text-left hover:underline"
                @click="goToUser(u?.username)"
            >
              {{ userLabel(u) }}
            </button>

            <!-- opcjonalnie: mały przycisk "Obserwuj" -->
            <button
                type="button"
                class="px-3 py-1 text-xs rounded-md border bg-[var(--color-surface)] hover:bg-black/5 disabled:opacity-50"
                :disabled="busy || !u?.username"
                @click="follow(u?.username)"
                :title="u?.username ? 'Wyślij zaproszenie' : ''"
            >
              Obserwuj
            </button>
          </li>
        </ul>
      </div>
    </div>

    <div v-if="friendsStore.error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
      {{ friendsStore.error }}
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
      <!-- Obserwowani -->
      <div class="rounded-2xl border p-3 bg-[var(--color-surface)]">
        <div class="text-sm font-semibold mb-2">Obserwowani</div>

        <div v-if="followingSafe.length === 0" class="text-sm opacity-70">
          Brak obserwowanych.
        </div>

        <ul v-else class="space-y-1">
          <li
              v-for="f in followingSafe"
              :key="f?.id || f?.username || JSON.stringify(f)"
              class="text-sm"
          >
            <button
                type="button"
                class="hover:underline"
                @click="goToUser(f?.username)"
                :disabled="!f?.username"
                :title="f?.username ? 'Przejdź do profilu' : ''"
            >
              {{ userLabel(f) }}
            </button>
          </li>
        </ul>
      </div>

      <!-- Zaproszenia do zaakceptowania (TYLKO NA SWOIM PROFILU) -->
      <div v-if="isOwnProfile" class="rounded-2xl border p-3 bg-[var(--color-surface)]">
        <div class="text-sm font-semibold mb-2">Zaproszenia do zaakceptowania</div>

        <div v-if="incomingSafe.length === 0" class="text-sm opacity-70">
          Brak zaproszeń.
        </div>

        <ul v-else class="space-y-2">
          <li
              v-for="x in incomingSafe"
              :key="getReqId(x) || JSON.stringify(x)"
              class="text-sm"
          >
            <div class="flex items-center justify-between gap-2">
              <div class="min-w-0">
                <div class="truncate">
                  {{ userLabel(getUser(x)) }}
                </div>
                <div class="text-xs opacity-70">
                  ID: {{ getReqId(x) || 'brak' }}
                </div>
              </div>

              <div class="flex items-center gap-2 shrink-0">
                <button
                    type="button"
                    class="px-2.5 py-1 text-xs rounded-md border bg-[var(--color-bg)] hover:bg-black/5 disabled:opacity-50"
                    :disabled="busy || !getReqId(x)"
                    @click="acceptReq(x)"
                >
                  Akceptuj
                </button>

                <button
                    type="button"
                    class="px-2.5 py-1 text-xs rounded-md border border-red-500/70 bg-red-500/20 hover:bg-red-500/30 disabled:opacity-50"
                    :disabled="busy || !getReqId(x)"
                    @click="rejectReq(x)"
                >
                  Odrzuć
                </button>
              </div>
            </div>

            <div v-if="!getReqId(x)" class="text-xs opacity-70 mt-1">
              Backend nie zwraca ID zaproszenia (reqId). Bez tego akceptacja/odrzucenie nie zadziała.
            </div>
          </li>
        </ul>
      </div>
    </div>

    <div v-if="isOwnProfile" class="rounded-md border px-4 py-3 text-sm bg-[var(--color-bg-elevated)]">
      <div class="font-semibold mb-1">Info</div>
      <div class="opacity-80">
        „Zaproszenia do zaakceptowania” są widoczne tylko na Twoim profilu. Na profilach innych użytkowników nie pokazujemy tych danych.
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/userStore.js'
import { useFriendsStore } from '../../stores/friendsStore.js'
import { searchUsersByUsername } from '../../utils/usersApi.js'

const router = useRouter()

const userStore = useUserStore()
const friendsStore = useFriendsStore()

const { me, friends } = storeToRefs(userStore)
const { incoming } = storeToRefs(friendsStore)

/*
  WAŻNE:
  Żeby poprawnie rozróżnić "mój profil" vs "czyjś profil",
  parent powinien przekazać username profilu.
*/
const props = defineProps({
  profileUsername: { type: String, required: false, default: '' },
})

const isOwnProfile = computed(() => {
  const my = String(me.value?.username || '').toLowerCase()
  const p = String(props.profileUsername || '').toLowerCase()
  if (!my) return true // fallback: jeśli nie wiemy, traktuj jako własny
  if (!p) return true
  return my === p
})

const busy = computed(() => friendsStore.loading)

const followingSafe = computed(() => (Array.isArray(friends.value) ? friends.value : []))
const incomingSafe = computed(() => (Array.isArray(incoming.value) ? incoming.value : []))

function userLabel(u) {
  if (!u) return 'Użytkownik'
  const username = u?.username ? String(u.username) : ''
  const name = u?.name ? String(u.name) : ''
  const surname = u?.surname ? String(u.surname) : ''
  const full = `${name} ${surname}`.trim()
  if (username && full) return `${username} (${full})`
  if (username) return username
  if (full) return full
  return 'Użytkownik'
}

function getReqId(x) {
  return friendsStore.normalizeReqId(x)
}
function getUser(x) {
  return friendsStore.normalizeUser(x)
}

async function acceptReq(x) {
  const id = getReqId(x)
  if (!id) return
  await friendsStore.accept(id)
}
async function rejectReq(x) {
  const id = getReqId(x)
  if (!id) return
  await friendsStore.reject(id)
}

async function refresh() {
  await friendsStore.refresh()
}

/* wyszukiwarka */
const query = ref('')
const searchLoading = ref(false)
const searchError = ref('')
const searchResults = ref([])
const selectedUsername = ref('')

const queryTrimmed = computed(() => String(query.value || '').trim())

async function onSearch() {
  searchError.value = ''
  searchResults.value = []
  selectedUsername.value = ''

  const q = queryTrimmed.value
  if (!q) return

  searchLoading.value = true
  try {
    const res = await searchUsersByUsername(q)
    const arr = Array.isArray(res?.data) ? res.data : []
    searchResults.value = arr
    selectedUsername.value = arr?.[0]?.username || ''
  } catch (err) {
    searchError.value = err?.response?.data?.message || err?.message || 'Nie udało się wyszukać użytkownika.'
  } finally {
    searchLoading.value = false
  }
}

function goToUser(username) {
  const u = String(username || '').trim()
  if (!u) return
  router.push(`/users/${encodeURIComponent(u)}`)
}

/* szybkie zaproszenie z listy wyników */
async function follow(username) {
  const u = String(username || '').trim()
  if (!u) return
  try {
    await userStore.followUserByUsername(u)
    friendsStore.markOutgoingPending(u)
  } catch {
    // błąd jest obsługiwany w userStore / buttonach; tu nie spamujemy UI
  }
}

onMounted(async () => {
  if (!me.value) {
    try {
      await userStore.fetchMe()
    } catch {
      // niezalogowany: sekcja i tak pokaże obserwowanych jako puste
    }
  }
  await refresh()
})
</script>
