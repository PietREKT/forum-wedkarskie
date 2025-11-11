<template>
  <article class="rounded-xl shadow-sm p-4 relative border theme-border theme-card theme-text">
    <!-- Autor -->
    <div class="flex items-start justify-between gap-4">
      <header class="flex items-center gap-3">
        <RouterLink :to="{ name: 'profile', query: { u: authorUsername } }" class="flex items-center gap-3 group">
          <img
              v-if="avatarSrc"
              :src="avatarSrc"
              alt="Avatar"
              class="w-9 h-9 rounded-full object-cover ring-1 theme-border"
          />
          <div
              v-else
              class="w-9 h-9 rounded-full grid place-items-center text-xs font-semibold
                   bg-[var(--color-border)] text-[var(--color-text)]/70"
          >
            {{ initials }}
          </div>

          <div>
            <h3 class="text-base md:text-lg font-semibold leading-tight group-hover:underline">{{ authorUsername }}</h3>
            <p class="text-[11px] md:text-xs theme-muted">{{ formatDate(post.postedAt) }}</p>
          </div>
        </RouterLink>
      </header>

      <!-- Akcje -->
      <div class="relative">
        <button class="px-2 py-1 border rounded-md text-xs theme-border hover:bg-[var(--color-border)]/20" @click="open = !open">
          Akcje
        </button>
        <div v-if="open" class="absolute right-0 mt-1 w-44 overflow-hidden shadow text-sm z-10 rounded-md border theme-border theme-card">
          <button class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20" @click="onReport">Zgłoś</button>
          <template v-if="canEditOrDelete">
            <button class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20" @click="onEdit">Edytuj</button>
            <button class="w-full text-left px-3 py-2 text-[var(--color-danger)] hover:bg-[var(--color-border)]/20" @click="onDelete">Usuń</button>
          </template>
        </div>
      </div>
    </div>

    <!-- Treść -->
    <p class="mt-3 whitespace-pre-wrap leading-relaxed">{{ post.content }}</p>

    <!-- Zdjęcia -->
    <div v-if="post.attachedPhotos?.length" class="mt-3">
      <div v-if="post.attachedPhotos.length === 1">
        <img
            :src="mediaUrl(post.attachedPhotos[0])"
            class="w-full max-h-[520px] h-auto object-cover rounded-xl border theme-border"
            alt="Zdjęcie z posta"
        />
      </div>
      <div v-else class="grid grid-cols-2 md:grid-cols-3 gap-3">
        <img
            v-for="(url,i) in post.attachedPhotos"
            :key="i"
            :src="mediaUrl(url)"
            class="w-full h-48 md:h-56 object-cover rounded-lg border theme-border"
            alt="Zdjęcie z posta"
        />
      </div>
    </div>

    <!-- Głosowanie -->
    <footer class="mt-4 flex items-center gap-2 md:gap-3">
      <button
          class="px-2 py-1 rounded-md text-xs border theme-border hover:bg-[var(--color-success)]/10"
          :class="userVote === 1 ? 'bg-[var(--color-success)] text-white border-transparent' : ''"
          :disabled="isVoting"
          @click="vote(1)"
      >
        Podoba mi się
      </button>
      <button
          class="px-2 py-1 rounded-md text-xs border theme-border hover:bg-[var(--color-danger)]/10"
          :class="userVote === -1 ? 'bg-[var(--color-danger)] text-white border-transparent' : ''"
          :disabled="isVoting"
          @click="vote(-1)"
      >
        Nie podoba mi się
      </button>
      <span class="ml-2 text-xs md:text-sm theme-muted">Ocena: {{ post.rating ?? 0 }}</span>
    </footer>

    <!-- Komentarze -->
    <section class="mt-4">
      <CommentsSection :post-id="getId(post)" />
    </section>

    <!-- Edycja -->
    <div v-if="editing" class="mt-4 border-t theme-border pt-3">
      <textarea v-model.trim="editContent" class="w-full border rounded-md p-2 text-sm theme-border theme-card" rows="3"></textarea>
      <div class="mt-2 flex gap-2">
        <button class="px-3 py-1.5 border rounded-md text-sm theme-border" @click="editing=false">Anuluj</button>
        <button class="px-3 py-1.5 rounded-md text-sm bg-black text-white dark:bg-white dark:text-black" @click="applyEdit">
          Zapisz
        </button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink } from 'vue-router'
import { usePostsStore } from '../../stores/posts'
import { mediaUrl } from '../../utils/media'
import CommentsSection from './CommentsSection.vue'

const props = defineProps({ post: Object, currentUser: Object })
const store = usePostsStore()
const open = ref(false)

const authorUsername = computed(() => props.post?.author?.username || 'użytkownik')
const avatarSrc = computed(() => {
  const avatar = props.post?.author?.avatar || props.post?.author?.photo || ''
  return avatar ? mediaUrl(avatar) : ''
})
const initials = computed(() => authorUsername.value.slice(0, 2).toUpperCase())

const canEditOrDelete = computed(() => {
  if (!props.currentUser) return false
  const isAdmin = props.currentUser.role === 'ADMIN'
  const sameAuthor = props.currentUser.username === (props.post?.author?.username || '')
  return isAdmin || sameAuthor
})

const userVote = computed(() => store.voted.get(getId(props.post)) ?? 0)
const isVoting = computed(() => store.voting.has(getId(props.post)))

function formatDate(d) { return d ? new Date(d).toLocaleString('pl-PL') : '' }
function getId(p) { return store.getId(p) }

async function vote(delta) {
  const id = getId(props.post)
  if (!id) return
  if (delta > 0) await store.voteUp(id)
  else await store.voteDown(id)
  open.value = false
}

const editing = ref(false)
const editContent = ref('')
function onEdit() { editing.value = true; editContent.value = props.post.content || ''; open.value = false }
async function applyEdit() { const id = getId(props.post); if (!id) return; await store.editPost({ id, content: editContent.value }); editing.value = false }
async function onDelete() { const id = getId(props.post); if (!id) return; if (!confirm('Usunąć post?')) return; await store.deletePost(id); open.value = false }
async function onReport() { const id = getId(props.post); if (!id) return; await store.reportPost({ postId: id, reason: 'OTHER' }); open.value = false }

function onDocClick(e) {
  if (!open.value) return
  const el = e.target.closest('article')
  if (!el) open.value = false
}
onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))
</script>
