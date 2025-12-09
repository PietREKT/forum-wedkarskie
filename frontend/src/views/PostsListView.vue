<template>
  <div class="max-w-5xl mx-auto">
    <header class="mb-4 flex items-start justify-between gap-4">
      <div class="flex-1">
        <h1 class="text-2xl font-semibold">
          {{ isUserFeed ? 'Posty użytkownika' : 'Posty' }}
        </h1>

        <div class="mt-2 flex flex-wrap items-center gap-2 text-sm">
          <input
              v-model="userSearch"
              type="text"
              class="px-3 py-1.5 border rounded-md text-sm flex-1 min-w-[200px]"
              placeholder="Wyszukaj użytkownika po nicku"
              @keyup.enter="onSearchUser"
          />

          <button
              class="px-3 py-1.5 border rounded-md text-sm disabled:opacity-50"
              :disabled="userSearchLoading"
              @click="onSearchUser"
          >
            {{ userSearchLoading ? 'Szukam...' : 'Szukaj' }}
          </button>

          <button
              v-if="isUserFeed"
              class="px-3 py-1.5 border rounded-md text-xs"
              @click="clearUserFilter"
          >
            Wróć do wszystkich postów
          </button>
        </div>

        <p
            v-if="userSearchError"
            class="mt-1 text-xs text-red-600 dark:text-red-400"
        >
          {{ userSearchError }}
        </p>

        <div
            v-if="userResults.length"
            class="mt-2 border rounded-md text-sm bg-white/70 dark:bg-zinc-900/80"
        >
          <div
              v-for="u in userResults"
              :key="u.id"
              class="px-3 py-1.5 flex items-center justify-between border-b last:border-b-0"
          >
            <span>{{ u.username }}</span>

            <button
                class="px-2 py-1 border rounded-md text-xs"
                @click="showUserPosts(u)"
            >
              Pokaż posty
            </button>
          </div>
        </div>
      </div>

      <button
          v-if="isAuth"
          class="px-3 py-1.5 border rounded-md text-sm h-9 self-start"
          @click="composerOpen = !composerOpen"
      >
        {{ composerOpen ? 'Schowaj formularz' : 'Dodaj post' }}
      </button>
    </header>

    <p
        v-if="posts.error"
        class="mb-3 text-sm text-red-600 dark:text-red-400"
    >
      {{ posts.error }}
    </p>

    <section v-if="composerOpen && isAuth" class="mb-6">
      <PostComposer @done="onCreated" @cancel="composerOpen = false" />
    </section>

    <section>
      <div
          v-if="!posts.items.length && posts.loading"
          class="py-8 text-center text-sm text-zinc-500"
      >
        Ładowanie postów...
      </div>

      <div v-else-if="!posts.items.length">
        <p class="py-8 text-center text-sm text-zinc-500">
          Brak postów do wyświetlenia.
        </p>
      </div>

      <div v-else class="space-y-4">
        <PostCard
            v-for="post in posts.items"
            :key="posts.getId(post)"
            :post="post"
        />
      </div>

      <div class="mt-6 flex justify-center">
        <button
            v-if="hasMore"
            class="px-4 py-2 text-sm border rounded-md disabled:opacity-50"
            :disabled="posts.loading"
            @click="loadMore"
        >
          {{ posts.loading ? 'Ładowanie...' : 'Wczytaj więcej' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { usePostsStore } from '../stores/posts'
import { searchUsersByUsername } from '../utils/usersApi.js'
import PostComposer from '../components/posts/PostComposer.vue'
import PostCard from '../components/posts/PostCard.vue'

const auth = useAuthStore()
const posts = usePostsStore()
const route = useRoute()
const router = useRouter()

const composerOpen = ref(false)

const isAuth = computed(() => auth.isAuthenticated)
const currentUserId = computed(() => route.query.userId || null)
const isUserFeed = computed(() => !!currentUserId.value)

const hasMore = computed(() => posts.items.length < posts.total)

// wyszukiwanie użytkowników po nicku
const userSearch = ref('')
const userResults = ref([])
const userSearchLoading = ref(false)
const userSearchError = ref('')

async function onSearchUser() {
  userResults.value = []
  userSearchError.value = ''

  const q = userSearch.value.trim()
  if (!q) {
    userSearchError.value = 'Wpisz nick użytkownika.'
    return
  }

  userSearchLoading.value = true
  try {
    const { data } = await searchUsersByUsername(q)
    userResults.value = Array.isArray(data) ? data : []
    if (!userResults.value.length) {
      userSearchError.value = 'Brak użytkowników o takim nicku.'
    }
  } catch (err) {
    console.error('search user error', err)
    userSearchError.value =
        err?.response?.data?.message ||
        err?.message ||
        'Nie udało się wyszukać użytkownika.'
  } finally {
    userSearchLoading.value = false
  }
}

function showUserPosts(user) {
  if (!user?.id) return
  userResults.value = []
  router.push({ name: 'posts', query: { userId: user.id } })
}

function clearUserFilter() {
  router.push({ name: 'posts' })
}

// ładowanie listy postów
async function loadFirstPage() {
  posts.reset(currentUserId.value || null)
  try {
    await posts.fetchNext()
  } catch (err) {
    console.error('load posts error', err)
    // błąd już jest ustawiany w store.setErrorFromAxios w fetchNext()
  }
}

onMounted(loadFirstPage)

watch(
    () => currentUserId.value,
    () => {
      loadFirstPage()
    },
)

async function loadMore() {
  if (posts.loading) return
  try {
    await posts.fetchNext()
  } catch (err) {
    console.error('load more posts error', err)
    // błąd również obsłużony w store
  }
}

function onCreated() {
  composerOpen.value = false
}
</script>
