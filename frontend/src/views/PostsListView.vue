<template>
  <div class="max-w-5xl mx-auto">
    <header class="mb-4 flex items-center justify-between">
      <h1 class="text-2xl font-semibold">Posty</h1>
      <button
          v-if="isAuth"
          class="px-3 py-1.5 border rounded-md text-sm"
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
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { usePostsStore } from '../stores/posts'
import PostComposer from '../components/posts/PostComposer.vue'
import PostCard from '../components/posts/PostCard.vue'

const auth = useAuthStore()
const posts = usePostsStore()

const composerOpen = ref(false)

const isAuth = computed(() => auth.isAuthenticated)

const hasMore = computed(() => {
  return posts.items.length < posts.total || posts.total === 0
})

onMounted(async () => {
  posts.reset()
  await posts.fetchNext()
})

async function loadMore() {
  if (!posts.loading) {
    await posts.fetchNext()
  }
}

function onCreated() {
  composerOpen.value = false
}
</script>

