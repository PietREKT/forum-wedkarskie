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

    <!-- Formularz dodawania posta -->
    <section v-if="composerOpen && isAuth" class="mb-6">
      <PostComposer @done="onCreated" @cancel="composerOpen = false" />
    </section>

    <!-- Lista postów -->
    <section class="space-y-4">
      <PostCard
          v-for="p in posts.items"
          :key="posts.getId(p) ?? p.postedAt"
          :post="p"
          :current-user="currentUser"
      />
    </section>

    <!-- Paginacja -->
    <div class="mt-6 text-center">
      <button
          v-if="!posts.loading && posts.items.length < posts.total"
          class="px-4 py-2 border rounded-md text-sm"
          :disabled="posts.loading"
          @click="loadMore"
      >
        Wczytaj więcej
      </button>

      <p v-else-if="posts.loading" class="text-sm text-zinc-500">
        Ładowanie...
      </p>

      <p v-else class="text-xs text-zinc-500">
        Brak kolejnych postów
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePostsStore } from '../stores/posts'
import { useAuthStore } from '../stores/auth'
import PostCard from '../components/posts/PostCard.vue'
import PostComposer from '../components/posts/PostComposer.vue'

const posts = usePostsStore()
const auth = useAuthStore()

const composerOpen = ref(false)

const currentUser = computed(() => auth.user)
const isAuth = computed(() => !!auth.user)

onMounted(() => {
  posts.reset()
  posts.fetchNext()
})

watch(
    () => auth.user && auth.user.username,
    () => {
      posts.reset()
      posts.fetchNext()
    }
)

function loadMore() {
  if (!posts.loading) {
    posts.fetchNext()
  }
}

function onCreated() {
  composerOpen.value = false
}
</script>
