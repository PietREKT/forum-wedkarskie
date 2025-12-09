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

const sectionOpen = ref(true)

/* status nad listą */

const statusMessage = ref('')
const statusType = ref('info') // 'info' | 'success' | 'error'
let statusTimer = null

const statusClass = computed(() => {
  if (!statusMessage.value) return ''
  if (statusType.value === 'success') return 'border-green-500 text-green-600'
  if (statusType.value === 'error') return 'border-red-500 text-red-600'
  return 'border-slate-400 text-slate-600'
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
  { code: 'SPAM', label: 'Spam lub treści bezwartościowe' },
  { code: 'UNPAID_AD', label: 'Ukryta / nieoznaczona reklama' },
  { code: 'HARASSMENT', label: 'Nękanie, obraźliwe treści' },
  { code: 'SEXUAL_CONTENT', label: 'Treści o charakterze seksualnym' },
]

onMounted(() => {
  if (!state.value.list.length) {
    comments.fetchNext(props.postId)
  }
})

onBeforeUnmount(() => {
  if (statusTimer) clearTimeout(statusTimer)
})

function author(c) {
  return c?.author?.username || 'Anonim'
}

function date(d) {
  return d ? new Date(d).toLocaleString('pl-PL') : ''
}

/* dodawanie komentarza */

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

function startReply(c) {
  replyToId.value = c.id
  replyContent.value = ''
  menuFor.value = null
}

function cancelReply() {
  replyToId.value = null
  replyContent.value = ''
}

async function sendReply() {
  const text = (replyContent.value || '').trim()
  if (!text) return
  const parentId = replyToId.value
  if (!parentId) return

  try {
    await comments.add(props.postId, {
      content: text,
      parentId,
    })
    showStatus('Odpowiedź została dodana.', 'success')
  } catch {
    showStatus('Nie udało się dodać odpowiedzi.', 'error')
  } finally {
    cancelReply()
  }
}

/* dzieci / drzewo */

function childrenOf(id) {
  comments.fetchChildren(props.postId, id)
  return (state.value.list || []).filter(c => c.parentId === id)
}

const showAllReplies = ref({})

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

const topLevelComments = computed(() => {
  const list = state.value.list || []
  const postIdNum = Number(props.postId)
  return list.filter(c => !c.parentId || c.parentId === postIdNum)
})

const totalCount = computed(() => state.value.list.length)

/* menu / uprawnienia */

function toggleMenu(id) {
  menuFor.value = menuFor.value === id ? null : id
}

// edycja/usuwanie tylko przez autora (nie przez admina)
function canEditOrDelete(c) {
  if (!currentUser.value) return false
  return currentUser.value.username === (c?.author?.username || '')
}

// zgłoszenie: zalogowany i nie jest autorem
function canReport(c) {
  if (!currentUser.value) return false
  return currentUser.value.username !== (c?.author?.username || '')
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

/* usuwanie */

function confirmRemove(c) {
  deleteConfirmComment.value = c
  menuFor.value = null
}

async function doRemove() {
  if (!deleteConfirmComment.value) return
  const id = deleteConfirmComment.value.id
  try {
    await comments.remove({ id, postId: props.postId })
    showStatus('Komentarz został usunięty.', 'success')

    if (replyToId.value === id) {
      cancelReply()
    }
  } catch {
    showStatus('Nie udało się usunąć komentarza.', 'error')
  } finally {
    deleteConfirmComment.value = null
  }
}

/* ładowanie kolejnych stron */

function loadMore() {
  if (!state.value.loading) comments.fetchNext(props.postId)
}

/* zgłoszenia */

function openReportPanel(c) {
  if (!canReport(c)) return
  reportForComment.value = c
  menuFor.value = null
}

function closeReportPanel() {
  reportForComment.value = null
  reportSubmitting.value = false
}

async function sendReport(reason) {
  if (!reportForComment.value || !reason) return
  reportSubmitting.value = true
  try {
    await comments.report({
      id: reportForComment.value.id,
      reason,
    })
    showStatus('Zgłoszenie zostało wysłane.', 'success')
    closeReportPanel()
  } catch {
    showStatus('Nie udało się wysłać zgłoszenia.', 'error')
    reportSubmitting.value = false
  }
}
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
      <!-- formularz dodawania komentarza -->
      <form v-if="isAuth" class="space-y-2" @submit.prevent="submit">
        <textarea
            v-model="content"
            class="w-full resize-none border rounded-md px-3 py-2 text-sm theme-border theme-bg theme-text"
            rows="3"
            placeholder="Dodaj komentarz..."
        />
        <div class="flex justify-end">
          <button
              type="submit"
              class="px-4 py-1 rounded-md text-sm theme-button"
              :disabled="submitting || !content.trim()"
          >
            {{ submitting ? 'Wysyłanie...' : 'Wyślij' }}
          </button>
        </div>
      </form>

      <p v-else class="text-xs theme-muted">
        Zaloguj się, aby dodać komentarz.
      </p>

      <!-- status -->
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
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-xs font-semibold theme-text">
                  {{ author(c) }}
                </span>
                <span class="text-[10px] theme-muted">
                  {{ date(c.createdAt) }}
                </span>
              </div>

              <div v-if="editingId === c.id" class="space-y-2">
                <textarea
                    v-model="editContent"
                    class="w-full resize-none border rounded-md px-2 py-1 text-xs theme-border theme-bg theme-text"
                    rows="3"
                />
                <div class="flex gap-2 justify-end">
                  <button
                      type="button"
                      class="px-2 py-1 text-[11px] rounded-md border theme-border"
                      @click="cancelEdit"
                  >
                    Anuluj
                  </button>
                  <button
                      type="button"
                      class="px-2 py-1 text-[11px] rounded-md theme-button"
                      @click="applyEdit(c)"
                  >
                    Zapisz
                  </button>
                </div>
              </div>
              <p v-else class="text-xs whitespace-pre-wrap theme-text">
                {{ c.content }}
              </p>
            </div>

            <!-- menu akcji – tylko dla zalogowanych -->
            <div v-if="isAuth" class="relative">
              <button
                  type="button"
                  class="text-xs px-2 py-1 rounded-md border theme-border theme-bg"
                  @click="toggleMenu(c.id)"
              >
                Akcje
              </button>

              <div
                  v-if="menuFor === c.id"
                  class="absolute right-0 mt-1 w-40 rounded-md border theme-border theme-card shadow-lg z-10"
              >
                <ul class="text-xs">
                  <li>
                    <button
                        type="button"
                        class="w-full text-left px-3 py-1 hover:theme-hover"
                        @click="startReply(c)"
                    >
                      Odpowiedz
                    </button>
                  </li>
                  <li v-if="canEditOrDelete(c)">
                    <button
                        type="button"
                        class="w-full text-left px-3 py-1 hover:theme-hover"
                        @click="startEdit(c)"
                    >
                      Edytuj
                    </button>
                  </li>
                  <li v-if="canEditOrDelete(c)">
                    <button
                        type="button"
                        class="w-full text-left px-3 py-1 hover:theme-hover text-red-500"
                        @click="confirmRemove(c)"
                    >
                      Usuń
                    </button>
                  </li>
                  <li v-if="canReport(c)">
                    <button
                        type="button"
                        class="w-full text-left px-3 py-1 hover:theme-hover"
                        @click="openReportPanel(c)"
                    >
                      Zgłoś
                    </button>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- odpowiedzi -->
          <div class="mt-2 border-l pl-3 space-y-2">
            <div
                v-for="r in visibleChildrenOf(c.id)"
                :key="r.id ?? r.createdAt"
                class="text-xs theme-text"
            >
              <div class="flex justify-between gap-2">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <span class="text-[11px] font-semibold theme-text">
                      {{ author(r) }}
                    </span>
                    <span class="text-[9px] theme-muted">
                      {{ date(r.createdAt) }}
                    </span>
                  </div>
                  <p class="whitespace-pre-wrap">
                    {{ r.content }}
                  </p>
                </div>

                <!-- menu akcji dla odpowiedzi – tylko zalogowani -->
                <div v-if="isAuth" class="relative">
                  <button
                      type="button"
                      class="text-[11px] px-2 py-1 rounded-md border theme-border theme-bg"
                      @click="toggleMenu(r.id)"
                  >
                    Akcje
                  </button>

                  <div
                      v-if="menuFor === r.id"
                      class="absolute right-0 mt-1 w-40 rounded-md border theme-border theme-card shadow-lg z-10"
                  >
                    <ul class="text-xs">
                      <li>
                        <button
                            type="button"
                            class="w-full text-left px-3 py-1 hover:theme-hover"
                            @click="startReply(c)"
                        >
                          Odpowiedz
                        </button>
                      </li>
                      <li v-if="canEditOrDelete(r)">
                        <button
                            type="button"
                            class="w-full text-left px-3 py-1 hover:theme-hover"
                            @click="startEdit(r)"
                        >
                          Edytuj
                        </button>
                      </li>
                      <li v-if="canEditOrDelete(r)">
                        <button
                            type="button"
                            class="w-full text-left px-3 py-1 hover:theme-hover text-red-500"
                            @click="confirmRemove(r)"
                        >
                          Usuń
                        </button>
                      </li>
                      <li v-if="canReport(r)">
                        <button
                            type="button"
                            class="w-full text-left px-3 py-1 hover:theme-hover"
                            @click="openReportPanel(r)"
                        >
                          Zgłoś
                        </button>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>

            <!-- przycisk „pokaż więcej odpowiedzi” -->
            <button
                v-if="childrenOf(c.id).length > 2"
                type="button"
                class="text-[11px] theme-link"
                @click="toggleReplies(c.id)"
            >
              <span v-if="showAllReplies[c.id]">Ukryj odpowiedzi</span>
              <span v-else>
                Pokaż wszystkie odpowiedzi ({{ childrenOf(c.id).length }})
              </span>
            </button>

            <!-- formularz odpowiedzi  -->
            <form
                v-if="replyToId === c.id"
                class="mt-2 space-y-2"
                @submit.prevent="sendReply"
            >
              <textarea
                  v-model="replyContent"
                  class="w-full resize-none border rounded-md px-2 py-1 text-xs theme-border theme-bg theme-text"
                  rows="2"
                  placeholder="Napisz odpowiedź..."
              />
              <div class="flex gap-2 justify-end">
                <button
                    type="button"
                    class="px-2 py-1 text-[11px] rounded-md border theme-border"
                    @click="cancelReply"
                >
                  Anuluj
                </button>
                <button
                    type="submit"
                    class="px-2 py-1 text-[11px] rounded-md theme-button"
                    :disabled="!replyContent.trim()"
                >
                  Wyślij odpowiedź
                </button>
              </div>
            </form>
          </div>
        </li>
      </ul>

      <!-- „załaduj więcej” -->
      <div v-if="state.hasMore" class="flex justify-center mt-2">
        <button
            type="button"
            class="px-3 py-1 text-xs rounded-md border theme-border"
            :disabled="state.loading"
            @click="loadMore"
        >
          {{ state.loading ? 'Ładowanie...' : 'Załaduj więcej' }}
        </button>
      </div>
    </div>

    <!-- Overlay zgłoszeń -->
    <CommentReportModal
        v-if="reportForComment"
        :comment="reportForComment"
        :reasons="REPORT_REASONS"
        :loading="reportSubmitting"
        @close="closeReportPanel"
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
