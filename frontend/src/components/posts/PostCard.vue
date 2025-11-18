<template>
  <article
      class="rounded-xl shadow-sm p-4 relative border theme-border theme-card theme-text"
  >
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
            {{ initials }}
          </div>

          <div>
            <h3
                class="text-base md:text-lg font-semibold leading-tight group-hover:underline"
            >
              {{ authorUsername }}
            </h3>
            <p class="text-[11px] md:text-xs theme-muted">
              {{ formatDate(post.postedAt) }}
            </p>
          </div>
        </RouterLink>
      </header>

      <div class="relative">
        <button
            class="px-2 py-1 border rounded-md text-xs theme-border hover:bg-[var(--color-border)]/20"
            @click.stop="open = !open"
        >
          Akcje
        </button>
        <div
            v-if="open"
            class="absolute right-0 mt-1 w-44 bg-[var(--color-bg)] shadow text-sm z-10 rounded-md border theme-border theme-card"
        >
          <button
              class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20"
              @click="startReport"
          >
            Zgłoś
          </button>
          <template v-if="canEditOrDelete">
            <button
                class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20"
                @click="onEdit"
            >
              Edytuj
            </button>
            <button
                class="w-full text-left px-3 py-2 text-[var(--color-danger)] hover:bg-[var(--color-border)]/20"
                @click="askDelete"
            >
              Usuń
            </button>
          </template>
        </div>
      </div>
    </div>

    <!-- Formularz edycji treści posta widoczny po kliknięciu "Edytuj" -->
    <div
        v-if="editing"
        class="mt-4 border rounded-md p-3 theme-border theme-card"
    >
      <h3 class="text-sm font-medium mb-2">Edycja posta</h3>
      <textarea
          v-model.trim="editContent"
          class="w-full border rounded-md p-2 text-sm theme-border theme-card"
          rows="3"
          placeholder="Treść posta"
      ></textarea>
      <div class="mt-3 flex gap-2 justify-end">
        <button
            class="px-3 py-1.5 border rounded-md text-sm theme-border"
            @click="cancelEdit"
        >
          Anuluj
        </button>
        <button
            class="px-3 py-1.5 rounded-md text-sm bg-black text-white dark:bg-white dark:text-black"
            @click="applyEdit"
        >
          Zapisz
        </button>
      </div>
    </div>

    <!-- TREŚĆ POSTA -->
    <p class="mt-3 whitespace-pre-wrap leading-relaxed">
      {{ post.content }}
    </p>

    <!-- Zdjęcia -->
    <div
        v-if="post.attachedPhotos && post.attachedPhotos.length"
        class="mt-3 grid grid-cols-2 md:grid-cols-3 gap-2"
    >
      <img
          v-for="(photo, idx) in post.attachedPhotos"
          :key="idx"
          :src="mediaUrl(photo)"
          class="w-full h-32 md:h-40 object-cover rounded-lg border theme-border"
          alt="Zdjęcie z posta"
      />
    </div>

    <!-- Głosowanie -->
    <footer class="mt-4 flex flex-wrap items-center gap-2 border-t theme-border pt-3">
      <button
          class="px-2 py-1 rounded-md text-xs border theme-border hover:bg-[var(--color-primary)]/10"
          :class="userVote === 1 ? 'bg-[var(--color-primary)] text-white border-transparent' : ''"
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
      <span class="ml-2 text-xs md:text-sm theme-muted">
        Ocena: {{ post.rating ?? 0 }}
      </span>
    </footer>

    <!-- Komentarze -->
    <section class="mt-4">
      <CommentsSection :post-id="getId(post)" />
    </section>

    <!-- Toast -->
    <div
        v-if="statusMessage"
        class="mt-3 text-xs border rounded-md px-3 py-2"
        :class="statusClass"
    >
      {{ statusMessage }}
    </div>

    <!-- zgłoszenia posta -->
    <div
        v-if="reportOpen"
        class="fixed inset-0 z-40 flex items-center justify-center bg-black/60"
    >
      <div class="w-full max-w-sm rounded-xl border theme-border theme-card p-4">
        <h3 class="text-sm font-semibold mb-2">Zgłoś post</h3>
        <p class="text-xs theme-muted mb-3">
          Wybierz powód zgłoszenia. Zgłoszenie zostanie przekazane moderatorowi.
        </p>
        <div class="flex flex-col gap-2">
          <button
              v-for="r in REPORT_REASONS"
              :key="r.code"
              class="px-3 py-1.5 rounded-md border text-xs text-left theme-border hover:bg-[var(--color-border)]/20 disabled:opacity-50"
              :disabled="reportSubmitting"
              @click="sendReport(r.code)"
          >
            {{ r.label }}
          </button>
        </div>
        <div class="mt-3 flex justify-end gap-2">
          <button
              class="px-3 py-1.5 rounded-md border text-xs theme-border"
              :disabled="reportSubmitting"
              @click="reportOpen = false"
          >
            Anuluj
          </button>
        </div>
      </div>
    </div>

    <!-- usuwania posta -->
    <div
        v-if="deleteConfirmOpen"
        class="fixed inset-0 z-40 flex items-center justify-center bg-black/60"
    >
      <div class="w-full max-w-sm rounded-xl border theme-border theme-card p-4">
        <h3 class="text-sm font-semibold mb-2">Usunąć post?</h3>
        <p class="text-xs theme-muted mb-4">
          Tej operacji nie można cofnąć.
        </p>
        <div class="flex justify-end gap-2">
          <button
              class="px-3 py-1.5 rounded-md border text-xs theme-border"
              @click="cancelDelete"
          >
            Anuluj
          </button>
          <button
              class="px-3 py-1.5 rounded-md text-xs bg-[var(--color-danger)] text-white"
              @click="confirmDelete"
          >
            Usuń
          </button>
        </div>
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

const props = defineProps({
  post: Object,
  currentUser: Object,
})

const store = usePostsStore()
const open = ref(false)

const authorUsername = computed(
    () => props.post?.author?.username || 'użytkownik',
)
const avatarSrc = computed(() => {
  const avatar = props.post?.author?.avatar || props.post?.author?.photo || ''
  return avatar ? mediaUrl(avatar) : ''
})
const initials = computed(() => authorUsername.value.slice(0, 2).toUpperCase())

const canEditOrDelete = computed(() => {
  if (!props.currentUser) return false
  const isAdmin = props.currentUser.role === 'ADMIN'
  const sameAuthor =
      props.currentUser.username === (props.post?.author?.username || '')
  return isAdmin || sameAuthor
})

const userVote = computed(() => props.post?.viewerVote ?? 0)
const isVoting = computed(() => store.voting.has(getId(props.post)))

function formatDate(d) {
  return d ? new Date(d).toLocaleString('pl-PL') : ''
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

/* Logika edycji posta: otwieranie formularza, anulowanie, zapisywanie */

const editing = ref(false)
const editContent = ref('')

function onEdit() {
  editing.value = true
  editContent.value = props.post.content || ''
  open.value = false
}

function cancelEdit() {
  editing.value = false
  editContent.value = ''
}

async function applyEdit() {
  const id = getId(props.post)
  if (!id) return
  const content = (editContent.value || '').trim()
  if (!content) return
  await store.editPost({ id, content })
  editing.value = false
}

/* TOAST */

const statusMessage = ref('')
const statusType = ref('info')
let statusTimer = null

const statusClass = computed(() => {
  if (statusType.value === 'error') {
    return 'bg-red-500/15 border-red-500/40 text-red-100'
  }
  if (statusType.value === 'success') {
    return 'bg-emerald-500/15 border-emerald-500/40 text-emerald-100'
  }
  return 'bg-zinc-500/10 border-zinc-500/30 text-zinc-100'
})

function showStatus(msg, type = 'info') {
  statusMessage.value = msg
  statusType.value = type
  if (statusTimer) clearTimeout(statusTimer)
  statusTimer = setTimeout(() => {
    statusMessage.value = ''
  }, 5000)
}

/* USUWANIE POSTA */

const deleteConfirmOpen = ref(false)

function askDelete() {
  deleteConfirmOpen.value = true
  open.value = false
}

function cancelDelete() {
  deleteConfirmOpen.value = false
}

async function confirmDelete() {
  const id = getId(props.post)
  if (!id) return
  deleteConfirmOpen.value = false
  try {
    await store.deletePost(id)
    showStatus('Post został usunięty.', 'success')
  } catch (e) {
    const status = e?.response?.status
    if (status === 403) {
      showStatus('Nie masz uprawnień do usunięcia tego posta.', 'error')
    } else if (status === 500) {
      showStatus('Nie udało się usunąć posta (błąd serwera).', 'error')
    } else {
      showStatus('Nie udało się usunąć posta.', 'error')
    }
  }
}

/* ZGŁOSZENIA POSTA */

const REPORT_REASONS = [
  { code: 'SPAM', label: 'Spam lub treści bezwartościowe' },
  { code: 'UNPAID_AD', label: 'Ukryta / nieoznaczona reklama' },
  { code: 'HARASSMENT', label: 'Nękanie, obraźliwe treści' },
  { code: 'SEXUAL_CONTENT', label: 'Treści o charakterze seksualnym' },
]

const reportOpen = ref(false)
const reportSubmitting = ref(false)

function startReport() {
  reportOpen.value = true
  open.value = false
}

async function sendReport(reasonCode) {
  const id = getId(props.post)
  if (!id) return
  reportSubmitting.value = true
  try {
    await store.reportPost({ postId: id, reason: reasonCode })
    showStatus('Zgłoszenie zostało wysłane do moderacji.', 'success')
  } catch {
    showStatus('Nie udało się wysłać zgłoszenia (błąd serwera).', 'error')
  } finally {
    reportSubmitting.value = false
    reportOpen.value = false
  }
}

/* Automatyczne zamykanie menu "Akcje" po kliknięciu poza komponent */

function onDocClick(e) {
  if (!open.value) return
  const el = e.target.closest('article')
  if (!el) open.value = false
}

onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
  if (statusTimer) clearTimeout(statusTimer)
})
</script>
