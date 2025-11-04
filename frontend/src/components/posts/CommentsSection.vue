<template>
  <div class="mt-2">
    <details open>
      <summary class="cursor-pointer text-sm">Komentarze</summary>

      <div class="mt-3 space-y-3">
        <form v-if="isAuth" @submit.prevent="submit" class="flex items-start gap-2">
          <textarea v-model.trim="content" rows="2" class="flex-1 border rounded-md p-2 text-sm" placeholder="Dodaj komentarz"></textarea>
          <input ref="fileInput" type="file" accept="image/*" class="text-xs" @change="onFile" />
          <button class="px-3 py-1.5 border rounded-md text-sm bg-black text-white dark:bg-white dark:text-black" :disabled="!content && !file">Wyślij</button>
        </form>

        <ul>
          <li v-for="c in state.list" :key="c.id" class="border rounded-md p-3 bg-zinc-50 dark:bg-zinc-900">
            <div class="flex items-start justify-between gap-3">
              <div>
                <p class="text-sm whitespace-pre-wrap">{{ editingId === c.id ? editContent : c.content }}</p>
                <p class="text-xs text-zinc-500 mt-1">{{ author(c) }} • {{ date(c.createdAt) }}</p>
              </div>

              <div class="relative">
                <button class="px-2 py-1 border rounded-md text-xs" @click="toggleMenu(c.id)">Akcje</button>
                <div v-if="menuFor === c.id" class="absolute right-0 mt-1 w-40 border rounded-md bg-white dark:bg-zinc-800 shadow text-sm z-10">
                  <button class="w-full text-left px-3 py-2 hover:bg-zinc-100 dark:hover:bg-zinc-700" @click="report(c)">Zgłoś</button>
                  <template v-if="canEditOrDelete(c)">
                    <button class="w-full text-left px-3 py-2 hover:bg-zinc-100 dark:hover:bg-zinc-700" @click="startEdit(c)">Edytuj</button>
                    <button class="w-full text-left px-3 py-2 text-red-600 hover:bg-zinc-100 dark:hover:bg-zinc-700" @click="remove(c)">Usuń</button>
                  </template>
                </div>
              </div>
            </div>

            <div v-if="editingId === c.id" class="mt-2">
              <textarea v-model.trim="editContent" class="w-full border rounded-md p-2 text-sm" rows="2"></textarea>
              <div class="mt-2 flex gap-2">
                <button class="px-3 py-1.5 border rounded-md text-sm" @click="cancelEdit">Anuluj</button>
                <button class="px-3 py-1.5 border rounded-md text-sm bg-black text-white dark:bg-white dark:text-black" @click="applyEdit(c)">Zapisz</button>
              </div>
            </div>
          </li>
        </ul>

        <div class="mt-2 text-center">
          <button v-if="state.hasMore && !state.loading" class="px-3 py-1.5 border rounded-md text-sm" @click="loadMore">Wczytaj więcej</button>
          <p v-else-if="state.loading" class="text-sm text-zinc-500">Ładowanie...</p>
          <p v-else class="text-xs text-zinc-500">Brak kolejnych komentarzy</p>
        </div>
      </div>
    </details>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCommentsStore } from '../../stores/comments'

const props = defineProps({ postId: { type: [Number, String], required: true } })
const store = useCommentsStore()
const state = computed(() => store.byPost[props.postId] || { list: [], page: 0, loading: false, hasMore: true })

const content = ref('')
const file = ref(null)
const fileInput = ref(null)
const menuFor = ref(null)
const editingId = ref(null)
const editContent = ref('')

const currentUser = computed(() => {
  try { return JSON.parse(localStorage.getItem('fw_user') || 'null') } catch { return null }
})
const isAuth = computed(() => !!currentUser.value)

onMounted(() => {
  if (!state.value.list.length) store.fetchNext(props.postId)
})

function author(c) {
  return c?.author?.username || 'Anonim'
}
function date(d) {
  return d ? new Date(d).toLocaleString('pl-PL') : ''
}
function onFile(e) {
  const f = e.target.files?.[0]
  file.value = f || null
}
async function submit() {
  await store.add(props.postId, { content: content.value, file: file.value })
  content.value = ''
  if (fileInput.value) fileInput.value.value = ''
  file.value = null
}
function toggleMenu(id) {
  menuFor.value = menuFor.value === id ? null : id
}
function canEditOrDelete(c) {
  if (!currentUser.value) return false
  const isAdmin = currentUser.value.role === 'ADMIN'
  const sameAuthor = author(c) && currentUser.value.username === author(c)
  return isAdmin || sameAuthor
}
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
  await store.edit({ id: c.id, content: editContent.value, postId: props.postId })
  cancelEdit()
}
async function remove(c) {
  if (!confirm('Usunąć komentarz?')) return
  await store.remove({ id: c.id, postId: props.postId })
  menuFor.value = null
}
async function report(c) {
  await store.report({ commentId: c.id, reason: 'OTHER' })
  menuFor.value = null
}
function loadMore() {
  store.fetchNext(props.postId)
}
</script>
