<template>
  <article class="rounded-xl shadow-sm p-4 relative border theme-border theme-card theme-text">
    <!-- Autor + akcje -->
    <div class="flex items-start justify-between gap-4">
      <header class="flex items-center gap-3">
        <RouterLink
            :to="{ name: 'profile', query: { u: authorUsername } }"
            class="flex items-center gap-3 group"
        >
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
            {{ authorInitials }}
          </div>

          <div class="flex flex-col">
            <span class="text-sm font-medium group-hover:underline">
              {{ authorUsername || 'Użytkownik' }}
            </span>
            <span class="text-xs theme-muted">
              {{ createdAtFormatted }}
            </span>
          </div>
        </RouterLink>

        <!-- Obserwuj -->
        <UserFollowButton
            v-if="authorUsername"
            class="ml-2"
            :username="authorUsername"
        />
      </header>

      <!-- Menu Akcje -->
      <div class="relative">
        <button
            type="button"
            class="px-2 py-1 text-xs border rounded-md theme-border hover:bg-zinc-50 dark:hover:bg-zinc-800"
            @click.stop="toggleMenu"
        >
          Akcje
        </button>

        <div
            v-if="menuOpen"
            class="absolute right-0 mt-1 w-40 rounded-md border theme-border bg-[var(--color-card)] shadow-lg z-10"
        >
          <button
              v-if="canEdit"
              type="button"
              class="block w-full text-left px-3 py-1.5 text-xs hover:bg-zinc-100 dark:hover:bg-zinc-800"
              @click="onEdit"
          >
            Edytuj
          </button>
          <button
              v-if="canDelete"
              type="button"
              class="block w-full text-left px-3 py-1.5 text-xs text-red-600 hover:bg-red-50 dark:hover:bg-red-900/40"
              @click="onDelete"
          >
            Usuń
          </button>
          <button
              v-if="canReport"
              type="button"
              class="block w-full text-left px-3 py-1.5 text-xs hover:bg-zinc-100 dark:hover:bg-zinc-800"
              @click="onReport"
          >
            Zgłoś
          </button>
        </div>
      </div>
    </div>

    <!-- Treść posta -->
    <section class="mt-3">
      <!-- tryb edycji -->
      <div v-if="editing" class="space-y-2">
        <textarea
            v-model.trim="editContent"
            class="w-full rounded-md border theme-border theme-card px-3 py-2 text-sm"
            rows="3"
        />
        <div class="flex justify-end gap-2">
          <button
              type="button"
              class="px-3 py-1.5 text-xs border rounded-md"
              @click="cancelEdit"
              :disabled="savingEdit"
          >
            Anuluj
          </button>
          <button
              type="button"
              class="px-3 py-1.5 text-xs rounded-md bg-cyan-600 text-white disabled:opacity-60"
              @click="applyEdit"
              :disabled="savingEdit || !canSaveEdit"
          >
            {{ savingEdit ? 'Zapisywanie...' : 'Zapisz' }}
          </button>
        </div>
        <p v-if="localError" class="mt-1 text-xs text-red-600">
          {{ localError }}
        </p>
      </div>

      <!-- normalny widok -->
      <div v-else class="space-y-3">
        <p class="text-sm whitespace-pre-wrap">
          {{ post.content }}
        </p>

        <img
            v-if="firstPhoto"
            :src="firstPhoto"
            alt="Załączone zdjęcie"
            class="w-full max-h-80 h-auto object-cover rounded-lg border theme-border"
        />
      </div>
    </section>

    <!-- Głosowanie -->
    <footer class="mt-4 flex items-center gap-2">
      <button
          type="button"
          class="px-3 py-1.5 text-xs md:text-sm rounded-md border theme-border flex items-center gap-1"
          :class="post.viewerVote === 1 ? 'bg-emerald-600 text-white' : ''"
          :disabled="voting"
          @click="voteUp"
      >
        Podoba mi się
      </button>
      <button
          type="button"
          class="px-3 py-1.5 text-xs md:text-sm rounded-md border theme-border flex items-center gap-1"
          :class="post.viewerVote === -1 ? 'bg-red-600 text-white' : ''"
          :disabled="voting"
          @click="voteDown"
      >
        Nie podoba mi się
      </button>
      <span class="ml-2 text-xs md:text-sm theme-muted">
        Ocena: {{ post.rating ?? 0 }}
      </span>
    </footer>

    <!-- Komentarze -->
    <section class="mt-4">
      <CommentsSection :post-id="postId" />
    </section>
  </article>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink } from 'vue-router'
import { usePostsStore } from '../../stores/posts'
import { useAuthStore } from '../../stores/auth'
import UserFollowButton from '../users/UserFollowButton.vue'
import CommentsSection from './CommentsSection.vue'
import { mediaUrl } from '../../utils/media'

const props = defineProps({
  post: { type: Object, required: true },
})

const posts = usePostsStore()
const auth = useAuthStore()

const menuOpen = ref(false)
const editing = ref(false)
const editContent = ref('')
const savingEdit = ref(false)
const localError = ref('')

const postId = computed(() => posts.getId(props.post))

const authorUsername = computed(() => props.post?.author?.username || '')
const avatarSrc = computed(() => {
  const url = props.post?.author?.avatarUrl || props.post?.author?.avatar
  return url ? mediaUrl(url) : ''
})
const authorInitials = computed(() => {
  const u = authorUsername.value
  if (!u) return '??'
  return u.slice(0, 2).toUpperCase()
})

const createdAtFormatted = computed(() => {
  const d = props.post?.createdAt
  if (!d) return ''
  try {
    return new Date(d).toLocaleString('pl-PL')
  } catch {
    return String(d)
  }
})

const firstPhoto = computed(() => {
  const arr = props.post?.attachedPhotos || props.post?.photos || []
  if (!arr || !arr.length) return ''
  return mediaUrl(arr[0])
})

const canSaveEdit = computed(() => editContent.value.trim().length > 0)

const currentUsername = computed(() => auth.user?.username || '')

const canEdit = computed(() => {
  return (
      currentUsername.value &&
      authorUsername.value &&
      currentUsername.value === authorUsername.value
  )
})
const canDelete = canEdit
const canReport = computed(() => {
  return (
      currentUsername.value &&
      authorUsername.value &&
      currentUsername.value !== authorUsername.value
  )
})

const voting = computed(() => posts.voting.has(postId.value))

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function onDocClick(e) {
  if (!menuOpen.value) return
  const article = e.target.closest('article')
  if (!article) menuOpen.value = false
}

onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))

function onEdit() {
  editing.value = true
  editContent.value = props.post.content || ''
  localError.value = ''
  menuOpen.value = false
}

async function applyEdit() {
  if (!postId.value) return
  if (!canSaveEdit.value) {
    localError.value = 'Treść posta nie może być pusta.'
    return
  }
  savingEdit.value = true
  localError.value = ''
  try {
    await posts.editPost({ id: postId.value, content: editContent.value })
    editing.value = false
  } catch {
    localError.value = posts.error || 'Nie udało się zapisać zmian.'
  } finally {
    savingEdit.value = false
  }
}

function cancelEdit() {
  editing.value = false
  localError.value = ''
}

async function onDelete() {
  if (!postId.value) return
  if (!confirm('Czy na pewno chcesz usunąć ten post?')) return
  try {
    await posts.deletePost(postId.value)
  } catch {
    // komunikat jest w posts.error
  } finally {
    menuOpen.value = false
  }
}

async function onReport() {
  if (!postId.value) return
  try {
    // używamy poprawnego kodu z enumu backendu; na razie na sztywno SPAM
    await posts.reportPost({ postId: postId.value, reason: 'SPAM' })
    alert('Zgłoszenie zostało wysłane.')
  } catch {
    // posts.error już ma komunikat
  } finally {
    menuOpen.value = false
  }
}

async function voteUp() {
  if (!postId.value) return
  try {
    await posts.voteUp(postId.value)
  } catch {
    // posts.error
  }
}

async function voteDown() {
  if (!postId.value) return
  try {
    await posts.voteDown(postId.value)
  } catch {
    // posts.error
  }
}
</script>
