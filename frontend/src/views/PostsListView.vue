<template>
  <div class="max-w-5xl mx-auto">
    <header class="mb-4 flex items-start justify-between gap-4">
      <div class="flex-1">
        <h1 class="text-2xl font-semibold">
          {{
            isGroupFeed
                ? 'Posty grupy'
                : isUserFeed
                    ? 'Posty użytkownika'
                    : 'Posty'
          }}
        </h1>

        <div class="mt-2 flex flex-wrap items-center gap-2 text-sm">
          <div class="flex items-center gap-2">
            <button
                type="button"
                class="px-3 py-1.5 border rounded-md text-xs"
                :class="searchMode === 'user' ? 'bg-zinc-200 dark:bg-zinc-800' : ''"
                @click="setMode('user')"
            >
              Użytkownik
            </button>
            <button
                type="button"
                class="px-3 py-1.5 border rounded-md text-xs"
                :class="searchMode === 'group' ? 'bg-zinc-200 dark:bg-zinc-800' : ''"
                @click="setMode('group')"
            >
              Grupa
            </button>
          </div>

          <input
              v-model="searchText"
              type="text"
              class="px-3 py-1.5 border rounded-md text-sm flex-1 min-w-[220px]"
              :placeholder="searchMode === 'user'
                ? 'Wyszukaj użytkownika po nicku'
                : 'Wyszukaj grupę po nazwie'"
              @keyup.enter="onSearch"
          />

          <button
              class="px-3 py-1.5 border rounded-md text-sm disabled:opacity-50"
              :disabled="searchLoading"
              @click="onSearch"
          >
            {{ searchLoading ? 'Szukam...' : 'Szukaj' }}
          </button>

          <button
              v-if="isUserFeed || isGroupFeed"
              class="px-3 py-1.5 border rounded-md text-xs"
              @click="clearFilter"
          >
            Wróć do wszystkich postów
          </button>
        </div>

        <p
            v-if="searchError"
            class="mt-1 text-xs text-red-600 dark:text-red-400"
        >
          {{ searchError }}
        </p>

        <div
            v-if="results.length"
            class="mt-2 border rounded-md text-sm bg-white/70 dark:bg-zinc-900/80"
        >
          <div
              v-for="r in results"
              :key="r.id"
              class="px-3 py-1.5 flex items-center justify-between border-b last:border-b-0"
          >
            <span>
              {{ searchMode === 'user' ? r.username : r.name }}
            </span>

            <button
                class="px-2 py-1 border rounded-md text-xs"
                @click="selectResult(r)"
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
import { searchGroupsByName } from '../utils/groupsApi.js'
import PostComposer from '../components/posts/PostComposer.vue'
import PostCard from '../components/posts/PostCard.vue'

const auth = useAuthStore()
const posts = usePostsStore()
const route = useRoute()
const router = useRouter()

const composerOpen = ref(false)

const isAuth = computed(() => auth.isAuthenticated)

const currentUserId = computed(() => route.query.userId || null)
const currentGroupId = computed(() => route.query.groupId || null)

const isUserFeed = computed(() => !!currentUserId.value)
const isGroupFeed = computed(() => !!currentGroupId.value)

const hasMore = computed(() => posts.items.length < posts.total)

const searchMode = ref('user') // 'user' | 'group'
const searchText = ref('')
const results = ref([])
const searchLoading = ref(false)
const searchError = ref('')

function setMode(mode) {
  searchMode.value = mode
  results.value = []
  searchError.value = ''
}

async function onSearch() {
  results.value = []
  searchError.value = ''

  const q = searchText.value.trim()
  if (!q) {
    searchError.value = searchMode.value === 'user'
        ? 'Wpisz nick użytkownika.'
        : 'Wpisz nazwę grupy.'
    return
  }

  searchLoading.value = true
  try {
    if (searchMode.value === 'user') {
      const { data } = await searchUsersByUsername(q)
      results.value = Array.isArray(data) ? data : []
      if (!results.value.length) searchError.value = 'Brak użytkowników o takim nicku.'
    } else {
      const { data } = await searchGroupsByName(q)
      results.value = Array.isArray(data) ? data : []
      if (!results.value.length) searchError.value = 'Brak grup o takiej nazwie.'
    }
  } catch (err) {
    console.error('search error', err)
    searchError.value =
        err?.response?.data?.message ||
        err?.message ||
        'Nie udało się wykonać wyszukiwania.'
  } finally {
    searchLoading.value = false
  }
}

function selectResult(item) {
  if (!item?.id) return
  results.value = []

  if (searchMode.value === 'user') {
    router.push({ name: 'posts', query: { userId: item.id } })
  } else {
    router.push({ name: 'posts', query: { groupId: item.id } })
  }
}

function clearFilter() {
  router.push({ name: 'posts' })
}

async function loadFirstPage() {
  posts.reset({
    userId: currentUserId.value || null,
    groupId: currentGroupId.value || null,
  })
  try {
    await posts.fetchNext()
  } catch (err) {
    console.error('load posts error', err)
  }
}

onMounted(loadFirstPage)

watch(
    () => [currentUserId.value, currentGroupId.value],
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
  }
}

function onCreated() {
  composerOpen.value = false
}
</script>
