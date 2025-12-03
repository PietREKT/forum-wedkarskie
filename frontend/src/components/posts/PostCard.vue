<template>
  <article class="rounded-xl shadow-sm p-4 relative border theme-border theme-card theme-text">
    <!-- małe okienko potwierdzenia zgłoszenia -->
    <div
        v-if="reportSuccess"
        class="absolute top-2 right-4 z-40 pointer-events-none"
    >
      <div
          class="rounded-md border border-emerald-500
           bg-emerald-100 dark:bg-emerald-900
           px-3 py-1.5 text-xs
           text-emerald-800 dark:text-emerald-100
           shadow-lg"
      >
        Zgłoszenie zostało wysłane.
      </div>
    </div>

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
            class="px-2 py-1 text-xs border rounded-md
                 theme-border
                 bg-white dark:bg-zinc-900
                 text-zinc-800 dark:text-zinc-100
                 hover:bg-zinc-50 dark:hover:bg-zinc-800"
            @click.stop="toggleMenu"
        >
          Akcje
        </button>

        <div
            v-if="menuOpen"
            class="absolute right-0 mt-1 w-40 rounded-md border theme-border
                 bg-white dark:bg-zinc-900
                 text-black dark:text-white
                 shadow-lg z-20"
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
              @click="onDeleteClick"
          >
            Usuń
          </button>
          <button
              v-if="canReport"
              type="button"
              class="block w-full text-left px-3 py-1.5 text-xs hover:bg-zinc-100 dark:hover:bg-zinc-800"
              @click="toggleReportPanel"
          >
            Zgłoś
          </button>
        </div>
      </div>
    </div>

    <!-- Treść posta -->
    <section class="mt-3">
      <!-- tryb edycji -->
      <div v-if="editing" class="space-y-3">
        <textarea
            v-model.trim="editContent"
            class="w-full rounded-md border theme-border theme-card px-3 py-2 text-sm"
            rows="3"
        />

        <!-- edycja zdjęć -->
        <div class="space-y-2 text-xs">
          <div v-if="existingPhotos.length">
            <p class="font-semibold mb-1">Aktualne zdjęcia:</p>
            <div class="flex flex-wrap gap-2">
              <div
                  v-for="name in existingPhotos"
                  :key="name"
                  class="relative w-20 h-20 rounded-md overflow-hidden border theme-border"
              >
                <img
                    :src="mediaUrl(name)"
                    alt="Załączone zdjęcie"
                    class="w-full h-full object-cover"
                />
                <button
                    type="button"
                    class="absolute top-0 right-0 m-1 px-1.5 py-0.5 text-[10px] rounded bg-red-600 text-white"
                    @click.stop="removeExistingPhoto(name)"
                >
                  Usuń
                </button>
              </div>
            </div>
          </div>

          <div>
            <label class="inline-flex items-center gap-2 cursor-pointer">
              <span class="px-2 py-1 border rounded-md theme-border">
                Dodaj nowe zdjęcia
              </span>
              <input
                  type="file"
                  class="hidden"
                  accept="image/*"
                  multiple
                  @change="onNewPhotosSelected"
              />
            </label>

            <div v-if="newPreviews.length" class="mt-2 flex flex-wrap gap-2">
              <div
                  v-for="(src, idx) in newPreviews"
                  :key="idx"
                  class="relative w-20 h-20 rounded-md overflow-hidden border theme-border"
              >
                <img
                    :src="src"
                    alt="Podgląd"
                    class="w-full h-full object-cover"
                />
              </div>
            </div>
          </div>
        </div>

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
            class="w-full h-auto max-h-[32rem] object-contain rounded-lg border theme-border"
        />
      </div>
    </section>

    <!-- Małe okienko zgłoszenia (osobny komponent) -->
    <ReportPanel
        v-if="reporting"
        :reasons="reportReasons"
        :loading="sendingReport"
        :error="reportError"
        @select-reason="sendReport"
        @cancel="cancelReport"
    />

    <!-- Głosowanie -->
    <footer class="mt-4 flex items-center gap-2">
      <button
          type="button"
          class="px-3 py-1.5 text-xs md:text-sm rounded-md border theme-border flex items-center gap-1"
          :class="post.viewerVote === 1 ? 'bg-emerald-600 text-white' : ''"
          @click="voteUp"
      >
        Podoba mi się
      </button>
      <button
          type="button"
          class="px-3 py-1.5 text-xs md:text-sm rounded-md border theme-border flex items-center gap-1"
          :class="post.viewerVote === -1 ? 'bg-red-600 text-white' : ''"
          @click="voteDown"
      >
        Nie podoba mi się
      </button>
      <span class="ml-2 text-xs md:text-sm theme-muted">
        Ocena: {{ post.rating ?? 0 }}
      </span>
    </footer>

    <!-- Potwierdzenie usunięcia -->
    <section v-if="showDeleteConfirm" class="mt-4">
      <div class="rounded-lg border theme-border bg-red-50 dark:bg-red-900/20 px-3 py-2 text-xs flex items-start justify-between gap-3">
        <div>
          <p class="font-semibold text-red-700 dark:text-red-300">
            Usunąć ten post?
          </p>
          <p class="mt-0.5 text-[11px] text-red-800/80 dark:text-red-200/80">
            Tej operacji nie można cofnąć.
          </p>
        </div>
        <div class="flex items-center gap-2">
          <button
              type="button"
              class="px-2 py-1 rounded-md text-[11px] border theme-border"
              @click="cancelDelete"
              :disabled="deleting"
          >
            Anuluj
          </button>
          <button
              type="button"
              class="px-2 py-1 rounded-md text-[11px] bg-red-600 text-white disabled:opacity-60"
              @click="confirmDelete"
              :disabled="deleting"
          >
            {{ deleting ? 'Usuwanie...' : 'Usuń' }}
          </button>
        </div>
      </div>
    </section>

    <!-- Komentarze -->
    <section class="mt-4">
      <CommentsSection :post-id="postId" />
    </section>
  </article>
</template>

<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { usePostsStore } from '../../stores/posts'
import { useAuthStore } from '../../stores/auth'
import UserFollowButton from '../users/UserFollowButton.vue'
import CommentsSection from './CommentsSection.vue'
import ReportPanel from '../common/ReportPanel.vue'
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

// stan zdjęć w trybie edycji
const existingPhotos = ref(
    props.post?.attachedPhotos ? [...props.post.attachedPhotos] : [],
)
const newFiles = ref([])
const newPreviews = ref([])

// usuwanie
const showDeleteConfirm = ref(false)
const deleting = ref(false)

// zgłoszenia
const reporting = ref(false)
const sendingReport = ref(false)
const reportError = ref('')
const reportSuccess = ref(false)

const reportReasons = [
  { key: 'SPAM', label: 'Spam' },
  { key: 'UNPAID_AD', label: 'Reklama bez zgody' },
  { key: 'HARASSMENT', label: 'Obraźliwa treść / nękanie' },
  { key: 'SEXUAL_CONTENT', label: 'Treści erotyczne' },
]

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

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function resetEditPhotos() {
  existingPhotos.value = props.post?.attachedPhotos
      ? [...props.post.attachedPhotos]
      : []
  newFiles.value = []
  newPreviews.value.forEach(u => URL.revokeObjectURL(u))
  newPreviews.value = []
}

function onNewPhotosSelected(event) {
  const selected = Array.from(event.target.files || [])
  newFiles.value = selected
  newPreviews.value.forEach(u => URL.revokeObjectURL(u))
  newPreviews.value = selected.map(f => URL.createObjectURL(f))
}

function removeExistingPhoto(name) {
  existingPhotos.value = existingPhotos.value.filter(p => p !== name)
}

function onEdit() {
  editing.value = true
  editContent.value = props.post.content || ''
  localError.value = ''
  resetEditPhotos()
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
    await posts.editPost({
      id: postId.value,
      content: editContent.value,
      newPhotos: newFiles.value,
      attachedPhotos: existingPhotos.value,
    })
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
  resetEditPhotos()
}

// kliknięcie "Usuń" w menu
function onDeleteClick() {
  showDeleteConfirm.value = true
  menuOpen.value = false
}

function cancelDelete() {
  showDeleteConfirm.value = false
}

// potwierdzenie usunięcia
async function confirmDelete() {
  if (!postId.value) return
  deleting.value = true
  try {
    await posts.deletePost(postId.value)
  } catch {
    posts.items = posts.items.filter(p => posts.getId(p) !== postId.value)
    posts.clearError()
  } finally {
    deleting.value = false
    showDeleteConfirm.value = false
  }
}

// panel zgłoszenia
function toggleReportPanel() {
  reportError.value = ''
  reporting.value = !reporting.value
  if (reporting.value) {
    menuOpen.value = false
  }
}

async function sendReport(reasonKey) {
  if (!postId.value) return
  sendingReport.value = true
  reportError.value = ''
  try {
    await posts.reportPost({ postId: postId.value, reason: reasonKey })
    reporting.value = false
    reportSuccess.value = true
    setTimeout(() => {
      reportSuccess.value = false
    }, 3000)
  } catch {
    reportError.value = 'Nie udało się wysłać zgłoszenia.'
  } finally {
    sendingReport.value = false
  }
}

function cancelReport() {
  reporting.value = false
  reportError.value = ''
}

async function voteUp() {
  if (!postId.value) return
  try {
    await posts.voteUp(postId.value)
  } catch {
  }
}

async function voteDown() {
  if (!postId.value) return
  try {
    await posts.voteDown(postId.value)
  } catch {
  }
}
</script>
