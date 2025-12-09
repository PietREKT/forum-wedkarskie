<template>
  <div class="max-w-3xl mx-auto">
    <header class="mb-4 flex items-center justify-between">
      <button
          type="button"
          class="px-3 py-1.5 text-sm border rounded-md"
          @click="$router.back()"
      >
        Wróć
      </button>
      <h1 class="text-lg font-semibold">
        Szczegóły posta
      </h1>
    </header>

    <p
        v-if="error"
        class="mb-3 text-sm text-red-600 dark:text-red-400"
    >
      {{ error }}
    </p>

    <div
        v-if="loading"
        class="py-8 text-center text-sm text-zinc-500"
    >
      Ładowanie posta...
    </div>

    <div v-else-if="post">
      <PostCard :post="post" />

      <section class="mt-6">
        <CommentsSection :post-id="post.id" />
      </section>
    </div>

    <p
        v-else
        class="py-8 text-center text-sm text-zinc-500"
    >
      Nie znaleziono posta.
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { usePostsStore } from '../stores/posts'
import PostCard from '../components/posts/PostCard.vue'
import CommentsSection from '../components/posts/CommentsSection.vue'

const route = useRoute()
const posts = usePostsStore()

const post = ref(null)
const loading = ref(false)
const error = ref(null)

onMounted(async () => {
  const id = route.params.id
  if (!id) {
    error.value = 'Brak identyfikatora posta.'
    return
  }

  loading.value = true
  error.value = null
  try {
    post.value = await posts.fetchById(id)
    if (!post.value) {
      error.value = 'Nie udało się wczytać posta lub został usunięty.'
    }
  } catch (err) {
    console.error('load post error', err)
    error.value =
        err?.response?.data?.message ||
        posts.error ||
        err?.message ||
        'Nie udało się wczytać posta.'
  } finally {
    loading.value = false
  }
})
</script>
