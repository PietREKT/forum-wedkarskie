<template>
  <div class="max-w-5xl mx-auto">
    <header class="mb-4 flex items-center justify-between">
      <h1 class="text-2xl font-semibold">Posty</h1>
      <button v-if="isAuth" class="px-3 py-1.5 border rounded-md text-sm" @click="composerOpen = !composerOpen">
        {{ composerOpen ? 'Schowaj formularz' : 'Dodaj post' }}
      </button>
    </header>

    <section v-if="composerOpen && isAuth" class="mb-6">
      <PostComposer @done="onCreated" @cancel="composerOpen=false" />
    </section>

    <section class="space-y-4">
      <PostCard
          v-for="p in posts.items"
          :key="posts.getId(p) || JSON.stringify(p)"
          :post="p"
          :current-user="currentUser"
      />
    </section>

    <div class="mt-6 text-center">
      <button
          v-if="!posts.loading && posts.items.length < posts.total"
          class="px-4 py-2 border rounded-md text-sm"
          @click="loadMore"
      >
        Wczytaj więcej
      </button>
      <p v-else-if="posts.loading" class="text-sm text-zinc-500">Ładowanie...</p>
      <p v-else class="text-xs text-zinc-500">Brak kolejnych postów</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { usePostsStore } from '../stores/posts'
import PostCard from '../components/posts/PostCard.vue'
import PostComposer from '../components/posts/PostComposer.vue'

const posts = usePostsStore()
const composerOpen = ref(false)

const currentUser = computed(() => {
  try { return JSON.parse(localStorage.getItem('fw_user') || 'null') } catch { return null }
})
const isAuth = computed(() => !!currentUser.value)

onMounted(() => {
  if (!posts.items.length) {
    posts.reset()
    posts.fetchNext()
  }
})

function loadMore() {
  posts.fetchNext()
}
function onCreated() {
  composerOpen.value = false
}
</script>
