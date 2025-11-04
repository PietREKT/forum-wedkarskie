<template>
  <article class="border rounded-lg p-4 bg-white dark:bg-zinc-900 relative">
    <div class="flex items-start justify-between gap-4">
      <header>
        <h3 class="font-medium">
          {{ authorName(post) }}
        </h3>
        <p class="text-xs text-zinc-500">
          {{ formatDate(post.postedAt) }}
        </p>
      </header>

      <div class="relative">
        <button class="px-2 py-1 border rounded-md text-xs" @click="open = !open">Akcje</button>
        <div v-if="open" class="absolute right-0 mt-1 w-40 border rounded-md bg-white dark:bg-zinc-800 shadow text-sm z-10">
          <button class="w-full text-left px-3 py-2 hover:bg-zinc-100 dark:hover:bg-zinc-700" @click="onReport">Zgłoś</button>
          <template v-if="canEditOrDelete">
            <button class="w-full text-left px-3 py-2 hover:bg-zinc-100 dark:hover:bg-zinc-700" @click="onEdit">Edytuj</button>
            <button class="w-full text-left px-3 py-2 text-red-600 hover:bg-zinc-100 dark:hover:bg-zinc-700" @click="onDelete">Usuń</button>
          </template>
        </div>
      </div>
    </div>

    <p class="mt-3 whitespace-pre-wrap">{{ post.content }}</p>

    <div v-if="post.attachedPhotos?.length" class="mt-3 grid grid-cols-2 sm:grid-cols-3 gap-2">
      <img v-for="(url,i) in post.attachedPhotos" :key="i" :src="url" class="w-full h-32 object-cover rounded-md border" />
    </div>

    <footer class="mt-4 flex items-center gap-3">
      <button class="px-2 py-1 border rounded-md text-xs" @click="vote(1)">Łapka w górę</button>
      <button class="px-2 py-1 border rounded-md text-xs" @click="vote(-1)">Łapka w dół</button>
      <span class="text-xs text-zinc-600">Ocena: {{ post.rating ?? 0 }}</span>
    </footer>

    <section class="mt-4">
      <CommentsSection :post-id="getId(post)" />
    </section>

    <div v-if="editing" class="mt-4 border-t pt-3">
      <textarea v-model.trim="editContent" class="w-full border rounded-md p-2 text-sm" rows="3"></textarea>
      <div class="mt-2 flex gap-2">
        <button class="px-3 py-1.5 border rounded-md text-sm" @click="editing=false">Anuluj</button>
        <button class="px-3 py-1.5 border rounded-md text-sm bg-black text-white dark:bg-white dark:text-black" @click="applyEdit">Zapisz</button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { ref, computed } from 'vue'
import { usePostsStore } from '../../stores/posts'
import CommentsSection from './CommentsSection.vue'

const props = defineProps({ post: { type: Object, required: true }, currentUser: { type: Object, default: null } })
const store = usePostsStore()
const open = ref(false)

const canEditOrDelete = computed(() => {
  if (!props.currentUser) return false
  const isAdmin = props.currentUser.role === 'ADMIN'
  const sameAuthor = authorName(props.post) && props.currentUser.username === authorName(props.post)
  return isAdmin || sameAuthor
})

function authorName(p) {
  return p?.author?.username || p?.authorName || p?.author_login || ''
}
function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleString('pl-PL')
}
function getId(p) {
  return store.getId(p)
}

async function vote(delta) {
  const id = getId(props.post)
  if (!id) return
  if (delta > 0) await store.voteUp(id)
  else await store.voteDown(id)
  open.value = false
}

const editing = ref(false)
const editContent = ref('')
function onEdit() {
  editing.value = true
  editContent.value = props.post.content || ''
  open.value = false
}
async function applyEdit() {
  const id = getId(props.post)
  if (!id) return
  await store.editPost({ id, content: editContent.value })
  editing.value = false
}

async function onDelete() {
  const id = getId(props.post)
  if (!id) return
  if (!confirm('Usunąć post?')) return
  await store.deletePost(id)
  open.value = false
}

async function onReport() {
  const id = getId(props.post)
  if (!id) return
  await store.reportPost({ postId: id, reason: 'OTHER' })
  open.value = false
}
</script>
