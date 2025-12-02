<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useCommentsStore } from '../../stores/comments'
import { useAuthStore } from '../../stores/auth'
import CommentReportModal from './CommentReportModal.vue'
import CommentDeleteModal from './CommentDeleteModal.vue'

const props = defineProps({
  postId: { type: [Number, String], required: true },
})

const comments = useCommentsStore()
const auth = useAuthStore()

const state = computed(
    () =>
        comments.byPost[props.postId] || {
          list: [],
          page: 0,
          loading: false,
          hasMore: true,
        },
)

const content = ref('')
const menuFor = ref(null)
const editingId = ref(null)
const editContent = ref('')
const submitting = ref(false)

const replyToId = ref(null)
const replyContent = ref('')

const reportForComment = ref(null)
const reportSubmitting = ref(false)

const deleteConfirmComment = ref(null)

const currentUser = computed(() => auth.user)
const isAuth = computed(() => !!auth.user)

/* zwijanie komentarzy */
const sectionOpen = ref(true)

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

/* powody zgłoszeń */

const REPORT_REASONS = [
  { code: 'SPAM',           label: 'Spam lub treści bezwartościowe' },
  { code: 'UNPAID_AD',      label: 'Ukryta / nieoznaczona reklama' },
  { code: 'HARASSMENT',     label: 'Nękanie, obraźliwe treści' },
  { code: 'SEXUAL_CONTENT', label: 'Treści o charakterze seksualnym' },
]

onMounted(() => {
  if (!state.value.list.length) {
    comments.fetchNext(props.postId)
  }
})

function author(c) {
  return c?.author?.username || 'Anonim'
}

function date(d) {
  return d ? new Date(d).toLocaleString('pl-PL') : ''
}

/* dodawanie komentarza – bez zdjęć */

async function submit() {
  const text = (content.value || '').trim()
  if (!text) return
  if (submitting.value) return

  submitting.value = true
  try {
    await comments.add(props.postId, { content: text })
    content.value = ''
    showStatus('Komentarz został dodany.', 'success')
  } catch {
    showStatus('Nie udało się dodać komentarza.', 'error')
  } finally {
    submitting.value = false
  }
}

function toggleMenu(id) {
  menuFor.value = menuFor.value === id ? null : id
}

function canEditOrDelete(c) {
  if (!currentUser.value) return false
  const isAdmin = currentUser.value.role === 'ADMIN'
  const sameAuthor =
      currentUser.value.username === (c?.author?.username || '')
  return isAdmin || sameAuthor
}

/* edycja */

function startEdit(c) {
  editingId.value = c.id
  editContent.value = c.content || ''
  menuFor.value = null
}

function cancelEdit() {
  editingId.value = null
  editContent.value = ''
}

async function applyEdit(c) {
  const text = (editContent.value || '').trim()
  if (!text) return
  try {
    await comments.edit({ id: c.id, content: text, postId: props.postId })
    showStatus('Komentarz został zaktualizowany.', 'success')
  } catch {
    showStatus('Nie udało się zaktualizować komentarza.', 'error')
  } finally {
    cancelEdit()
  }
}

/* odpowiedzi */

function startReply(c) {
  replyToId.value = c.id
  replyContent.value = ''
  menuFor.value = null
}

function cancelReply() {
  replyToId.value = null
  replyContent.value = ''
}

async function sendReply(parent) {
  const text = (replyContent.value || '').trim()
  if (!text) return
  try {
    await comments.add(props.postId, {
      content: text,
      parentCommentId: parent.id,
    })
    showStatus('Odpowiedź została dodana.', 'success')
  } catch {
    showStatus('Nie udało się dodać odpowiedzi.', 'error')
  } finally {
    cancelReply()
  }
}

const showAllReplies = ref({})

function childrenOf(id) {
  return (state.value.list || []).filter(c => c.parentId === id)
}

function visibleChildrenOf(id) {
  const all = childrenOf(id)
  if (showAllReplies.value[id]) return all
  return all.slice(0, 2)
}

function toggleReplies(id) {
  showAllReplies.value = {
    ...showAllReplies.value,
    [id]: !showAllReplies.value[id],
  }
}

/* zgłoszenia – overlay */

function openReportPanel(c) {
  reportForComment.value = c
  menuFor.value = null
}

async function sendReport(reasonCode) {
  const c = reportForComment.value
  if (!c) return
  reportSubmitting.value = true
  try {
    await comments.report({ commentId: c.id, reason: reasonCode })
    showStatus('Zgłoszenie zostało wysłane do moderacji.', 'success')
  } catch {
    showStatus('Nie udało się wysłać zgłoszenia (błąd serwera).', 'error')
  } finally {
    reportSubmitting.value = false
    reportForComment.value = null
  }
}

/* usuwanie komentarza */

function askRemove(c) {
  deleteConfirmComment.value = c
  menuFor.value = null
}

async function doRemove() {
  const c = deleteConfirmComment.value
  if (!c) return
  deleteConfirmComment.value = null

  // id komentarza i jego odpowiedzi które znikną w UI
  const removedIds = [c.id, ...childrenOf(c.id).map(x => x.id)]

  try {
    await comments.remove({ id: c.id, postId: props.postId })
    showStatus('Komentarz został usunięty.', 'success')

    // jeśli edytowaliśmy/odpowiadaliśmy na usunięty komentarz/odpowiedź – wyczyść UI
    if (removedIds.includes(editingId.value)) {
      cancelEdit()
    }
    if (removedIds.includes(replyToId.value)) {
      cancelReply()
    }
  } catch {
    showStatus('Nie udało się usunąć komentarza.', 'error')
  }
}

function loadMore() {
  if (!state.value.loading) comments.fetchNext(props.postId)
}

/* struktura komentarzy */

const topLevelComments = computed(() =>
    (state.value.list || []).filter(c => !c.parentId),
)

const totalCount = computed(() => state.value.list.length)

onBeforeUnmount(() => {
  if (statusTimer) clearTimeout(statusTimer)
})
</script>

<template>
  <div class="mt-2">
    <!-- nagłówek sekcji -->
    <div class="flex items-center justify-between">
      <button
          class="text-sm theme-text font-medium"
          type="button"
          @click="sectionOpen = !sectionOpen"
      >
        Komentarze ({{ totalCount }})
        <span class="ml-1 text-xs theme-muted">
          {{ sectionOpen ? '▲' : '▼' }}
        </span>
      </button>
    </div>

    <div v-if="sectionOpen" class="mt-3 space-y-3">
      <!-- dodawanie komentarza (bez zdjęć) -->
      <form
          v-if="isAuth"
          @submit.prevent="submit"
          class="flex items-start gap-2"
      >
        <textarea
            v-model.trim="content"
            rows="2"
            class="flex-1 border rounded-md p-2 text-sm theme-border theme-card"
            placeholder="Dodaj komentarz"
        ></textarea>
        <button
            class="px-3 py-1.5 border rounded-md text-sm theme-border theme-primary"
            :disabled="submitting || !content.trim()"
        >
          {{ submitting ? 'Wysyłanie...' : 'Wyślij' }}
        </button>
      </form>

      <!-- toast -->
      <div
          v-if="statusMessage"
          class="text-xs border rounded-md px-3 py-2"
          :class="statusClass"
      >
        {{ statusMessage }}
      </div>

      <!-- lista komentarzy -->
      <ul>
        <li
            v-for="c in topLevelComments"
            :key="c.id ?? c.createdAt"
            class="border rounded-md p-3 theme-border theme-card mb-2"
        >
          <!-- komentarz główny -->
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-sm whitespace-pre-wrap">
                {{ editingId === (c.id ?? -1) ? editContent : c.content }}
              </p>
              <p class="text-xs theme-muted mt-1">
                {{ author(c) }} • {{ date(c.createdAt) }}
              </p>
            </div>

            <div class="relative">
              <button
                  class="px-2 py-1 border rounded-md text-xs theme-border"
                  @click="toggleMenu(c.id)"
              >
                Akcje
              </button>
              <div
                  v-if="menuFor === c.id"
                  class="absolute right-0 mt-1 w-44 border rounded-md theme-border theme-card shadow text-sm z-10"
              >
                <button
                    class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20"
                    @click="openReportPanel(c)"
                >
                  Zgłoś
                </button>
                <button
                    v-if="isAuth"
                    class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20"
                    @click="startReply(c)"
                >
                  Odpowiedz
                </button>
                <template v-if="canEditOrDelete(c)">
                  <button
                      class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20"
                      @click="startEdit(c)"
                  >
                    Edytuj
                  </button>
                  <button
                      class="w-full text-left px-3 py-2 text-[var(--color-danger)] hover:bg-[var(--color-border)]/20"
                      @click="askRemove(c)"
                  >
                    Usuń
                  </button>
                </template>
              </div>
            </div>
          </div>

          <!-- edycja komentarza głównego -->
          <div v-if="editingId === c.id" class="mt-2">
            <textarea
                v-model.trim="editContent"
                class="w-full border rounded-md p-2 text-sm theme-border theme-card"
                rows="2"
            ></textarea>
            <div class="mt-2 flex gap-2">
              <button
                  class="px-3 py-1.5 border rounded-md text-sm theme-border"
                  @click="cancelEdit"
              >
                Anuluj
              </button>
              <button
                  class="px-3 py-1.5 border rounded-md text-sm theme-primary"
                  @click="applyEdit(c)"
              >
                Zapisz
              </button>
            </div>
          </div>

          <!-- odpowiedzi -->
          <ul class="mt-3 space-y-2 pl-4 border-l border-dashed theme-border">
            <li
                v-for="r in visibleChildrenOf(c.id)"
                :key="r.id ?? r.createdAt"
                class="pt-1"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <p class="text-sm whitespace-pre-wrap">
                    {{ editingId === r.id ? editContent : r.content }}
                  </p>
                  <p class="text-xs theme-muted mt-1">
                    {{ author(r) }} • {{ date(r.createdAt) }}
                  </p>
                </div>

                <div class="relative">
                  <button
                      class="px-2 py-1 border rounded-md text-xs theme-border"
                      @click="toggleMenu(r.id)"
                  >
                    Akcje
                  </button>
                  <div
                      v-if="menuFor === r.id"
                      class="absolute right-0 mt-1 w-44 border rounded-md theme-border theme-card shadow text-sm z-10"
                  >
                    <button
                        class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20"
                        @click="openReportPanel(r)"
                    >
                      Zgłoś
                    </button>
                    <button
                        v-if="isAuth"
                        class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20"
                        @click="startReply(r)"
                    >
                      Odpowiedz
                    </button>
                    <template v-if="canEditOrDelete(r)">
                      <button
                          class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20"
                          @click="startEdit(r)"
                      >
                        Edytuj
                      </button>
                      <button
                          class="w-full text-left px-3 py-2 text-[var(--color-danger)] hover:bg-[var(--color-border)]/20"
                          @click="askRemove(r)"
                      >
                        Usuń
                      </button>
                    </template>
                  </div>
                </div>
              </div>

              <!-- edycja odpowiedzi -->
              <div v-if="editingId === r.id" class="mt-2">
                <textarea
                    v-model.trim="editContent"
                    class="w-full border rounded-md p-2 text-sm theme-border theme-card"
                    rows="2"
                ></textarea>
                <div class="mt-2 flex gap-2">
                  <button
                      class="px-3 py-1.5 border rounded-md text-sm theme-border"
                      @click="cancelEdit"
                  >
                    Anuluj
                  </button>
                  <button
                      class="px-3 py-1.5 border rounded-md text-sm theme-primary"
                      @click="applyEdit(r)"
                  >
                    Zapisz
                  </button>
                </div>
              </div>
            </li>

            <!-- formularz odpowiedzi (dla aktualnie wybranego komentarza) -->
            <li v-if="replyToId === c.id" class="pt-1">
              <div class="mt-2">
                <textarea
                    v-model.trim="replyContent"
                    class="w-full border rounded-md p-2 text-sm theme-border theme-card"
                    rows="2"
                    placeholder="Odpowiedz na komentarz"
                ></textarea>
                <div class="mt-2 flex gap-2">
                  <button
                      class="px-3 py-1.5 border rounded-md text-sm theme-border"
                      @click="cancelReply"
                  >
                    Anuluj
                  </button>
                  <button
                      class="px-3 py-1.5 border rounded-md text-sm theme-primary"
                      @click="sendReply(c)"
                  >
                    Wyślij odpowiedź
                  </button>
                </div>
              </div>
            </li>

            <!-- przycisk pokaż wszystkie / mniej odpowiedzi -->
            <li v-if="childrenOf(c.id).length > 2" class="pt-1">
              <button
                  class="text-[11px] underline theme-muted"
                  type="button"
                  @click="toggleReplies(c.id)"
              >
                {{ showAllReplies[c.id]
                  ? 'Pokaż mniej odpowiedzi'
                  : 'Pokaż wszystkie odpowiedzi' }}
              </button>
            </li>
          </ul>
        </li>
      </ul>

      <!-- wczytaj więcej stron -->
      <div class="mt-2 text-center">
        <button
            v-if="state.hasMore && !state.loading"
            class="px-3 py-1.5 border rounded-md text-sm theme-border"
            @click="loadMore"
        >
          Wczytaj więcej
        </button>
        <p v-else-if="state.loading" class="text-sm theme-muted">
          Ładowanie...
        </p>
        <p v-else class="text-xs theme-muted">
          Brak kolejnych komentarzy
        </p>
      </div>
    </div>

    <!-- zgłoszenia komentarza -->
    <CommentReportModal
        v-if="reportForComment"
        :comment="reportForComment"
        :reasons="REPORT_REASONS"
        :loading="reportSubmitting"
        @close="reportForComment = null"
        @submit="sendReport"
    />

    <!-- Overlay usuwania komentarza -->
    <CommentDeleteModal
        v-if="deleteConfirmComment"
        :comment="deleteConfirmComment"
        :loading="false"
        @close="deleteConfirmComment = null"
        @confirm="doRemove"
    />
  </div>
</template>
