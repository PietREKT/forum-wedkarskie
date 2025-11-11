<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCommentsStore } from '../../stores/comments'
import { useAuthStore } from '../../stores/auth'
import { mediaUrl } from '../../utils/media'

const props = defineProps({ postId: { type: [Number, String], required: true } })
const comments = useCommentsStore()
const auth = useAuthStore()

const state = computed(
    () => comments.byPost[props.postId] || { list: [], page: 0, loading: false, hasMore: true }
)

const content = ref('')
const file = ref(null)
const fileInput = ref(null)
const menuFor = ref(null)
const editingId = ref(null)
const editContent = ref('')
const submitting = ref(false)

const currentUser = computed(() => auth.user)
const isAuth = computed(() => !!auth.user)

onMounted(() => { if (!state.value.list.length) comments.fetchNext(props.postId) })

function author(c) { return c?.author?.username || 'Anonim' }
function date(d) { return d ? new Date(d).toLocaleString('pl-PL') : '' }
function att(c) { return c?.attachment || c?.photo || c?.photoUrl || c?.image || c?.imageUrl || c?.file || c?.fileUrl || '' }

function onFile(e) {
  const f = e.target.files?.[0]
  file.value = f && f.type.startsWith('image/') ? f : null
}
async function submit() {
  if (submitting.value) return
  submitting.value = true
  try {
    await comments.add(props.postId, { content: content.value, file: file.value })
    content.value = ''
    if (fileInput.value) fileInput.value.value = ''
    file.value = null
  } finally { submitting.value = false }
}
function toggleMenu(id) { menuFor.value = menuFor.value === id ? null : id }
function canEditOrDelete(c) {
  if (!currentUser.value) return false
  const isAdmin = currentUser.value.role === 'ADMIN'
  const sameAuthor = currentUser.value.username === (c?.author?.username || '')
  return isAdmin || sameAuthor
}
function startEdit(c) { editingId.value = c.id; editContent.value = c.content || ''; menuFor.value = null }
function cancelEdit() { editingId.value = null; editContent.value = '' }
async function applyEdit(c) { await comments.edit({ id: c.id, content: editContent.value, postId: props.postId }); cancelEdit() }
async function remove(c) { if (!confirm('Usunąć komentarz?')) return; await comments.remove({ id: c.id, postId: props.postId }); menuFor.value = null }
async function report(c) { await comments.report({ commentId: c.id, reason: 'OTHER' }); menuFor.value = null }
function loadMore() { if (!state.value.loading) comments.fetchNext(props.postId) }
</script>

<template>
  <div class="mt-2">
    <details open>
      <summary class="cursor-pointer text-sm theme-text">Komentarze</summary>

      <div class="mt-3 space-y-3">
        <form v-if="isAuth" @submit.prevent="submit" class="flex items-start gap-2">
          <textarea v-model.trim="content" rows="2" class="flex-1 border rounded-md p-2 text-sm theme-border theme-card" placeholder="Dodaj komentarz"></textarea>
          <input ref="fileInput" type="file" accept="image/*" class="text-xs" @change="onFile" />
          <button class="px-3 py-1.5 border rounded-md text-sm theme-border theme-primary" :disabled="submitting || (!content && !file)">
            {{ submitting ? 'Wysyłanie...' : 'Wyślij' }}
          </button>
        </form>

        <ul>
          <li v-for="c in state.list" :key="c.id ?? c.createdAt" class="border rounded-md p-3 theme-border theme-card">
            <div class="flex items-start justify-between gap-3">
              <div>
                <p class="text-sm whitespace-pre-wrap">{{ editingId === (c.id ?? -1) ? editContent : c.content }}</p>
                <p class="text-xs theme-muted mt-1">{{ author(c) }} • {{ date(c.createdAt) }}</p>

                <!-- Załącznik -->
                <img
                    v-if="att(c)"
                    :src="mediaUrl(att(c))"
                    class="mt-2 w-full max-h-80 h-auto object-cover rounded-md border theme-border"
                    alt="Załącznik"
                />
              </div>

              <div class="relative">
                <button class="px-2 py-1 border rounded-md text-xs theme-border" @click="toggleMenu(c.id)">Akcje</button>
                <div v-if="menuFor === c.id" class="absolute right-0 mt-1 w-40 border rounded-md theme-border theme-card shadow text-sm z-10">
                  <button class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20" @click="report(c)">Zgłoś</button>
                  <template v-if="canEditOrDelete(c)">
                    <button class="w-full text-left px-3 py-2 hover:bg-[var(--color-border)]/20" @click="startEdit(c)">Edytuj</button>
                    <button class="w-full text-left px-3 py-2 text-[var(--color-danger)] hover:bg-[var(--color-border)]/20" @click="remove(c)">Usuń</button>
                  </template>
                </div>
              </div>
            </div>

            <div v-if="editingId === c.id" class="mt-2">
              <textarea v-model.trim="editContent" class="w-full border rounded-md p-2 text-sm theme-border theme-card" rows="2"></textarea>
              <div class="mt-2 flex gap-2">
                <button class="px-3 py-1.5 border rounded-md text-sm theme-border" @click="cancelEdit">Anuluj</button>
                <button class="px-3 py-1.5 border rounded-md text-sm theme-primary" @click="applyEdit(c)">Zapisz</button>
              </div>
            </div>
          </li>
        </ul>

        <div class="mt-2 text-center">
          <button v-if="state.hasMore && !state.loading" class="px-3 py-1.5 border rounded-md text-sm theme-border" :disabled="state.loading" @click="loadMore">
            Wczytaj więcej
          </button>
          <p v-else-if="state.loading" class="text-sm theme-muted">Ładowanie...</p>
          <p v-else class="text-xs theme-muted">Brak kolejnych komentarzy</p>
        </div>
      </div>
    </details>
  </div>
</template>
