<template>
  <div class="rounded-lg border theme-border theme-card px-3 py-3 md:px-4 md:py-3">
    <h3 class="font-medium mb-2 text-sm md:text-base theme-text">Nowy post</h3>

    <p v-if="localError" class="mb-2 text-xs text-red-600 dark:text-red-400">
      {{ localError }}
    </p>

    <textarea
        v-model="content"
        class="w-full rounded-md px-3 py-2 text-sm theme-border theme-bg theme-text"
        rows="3"
        placeholder="Napisz coś..."
    />

    <div class="mt-3">
      <label class="inline-flex items-center gap-2 text-xs md:text-sm cursor-pointer">
        <span class="px-2 py-1 border rounded-md theme-border">Dodaj zdjęcia</span>
        <span class="theme-muted">{{ summary }}</span>
        <input
            ref="fileInput"
            type="file"
            class="hidden"
            accept="image/*"
            multiple
            @change="onFilesSelected"
        >
      </label>

      <div v-if="previews.length" class="mt-2 flex flex-wrap gap-2">
        <div
            v-for="(src, idx) in previews"
            :key="idx"
            class="relative w-20 h-20 rounded-md overflow-hidden border theme-border"
        >
          <img :src="src" alt="Podgląd" class="w-full h-full object-cover">
        </div>
      </div>
    </div>

    <div class="mt-3 flex justify-end gap-2">
      <button
          type="button"
          class="px-3 py-1.5 text-xs md:text-sm border rounded-md theme-border"
          @click="onCancel"
          :disabled="submitting"
      >
        Anuluj
      </button>

      <button
          type="button"
          class="px-3 py-1.5 text-xs md:text-sm rounded-md theme-button disabled:opacity-60"
          :disabled="submitting || !canSubmit"
          @click="onSubmit"
      >
        {{ submitting ? 'Zapisywanie...' : 'Dodaj post' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount } from 'vue'
import { usePostsStore } from '../../stores/posts'

const emit = defineEmits(['done', 'cancel'])
const store = usePostsStore()

const content = ref('')
const files = ref([])
const previews = ref([])
const submitting = ref(false)
const fileInput = ref(null)
const localError = ref('')

const summary = computed(() => {
  const n = files.value.length
  if (!n) return ''
  if (n === 1) return '1 plik'
  if (n >= 2 && n <= 4) return `${n} pliki`
  return `${n} plików`
})

const canSubmit = computed(() => {
  return content.value.trim().length > 0 || files.value.length > 0
})

function cleanupPreviews() {
  previews.value.forEach(u => URL.revokeObjectURL(u))
  previews.value = []
}

function onFilesSelected(event) {
  const selected = Array.from(event.target.files || [])
  files.value = selected
  cleanupPreviews()
  previews.value = selected.map(f => URL.createObjectURL(f))
}

async function onSubmit() {
  localError.value = ''
  const text = content.value.trim()

  if (!text) {
    localError.value = 'Treść posta jest wymagana.'
    return
  }

  submitting.value = true
  try {
    await store.createPost({
      content: text,
      files: files.value,
    })
    reset()
    emit('done')
  } catch (e) {
    const status = e?.response?.status
    if (status === 401) localError.value = 'Musisz być zalogowany, aby dodać post.'
    else localError.value = store.error || 'Nie udało się dodać posta.'
  } finally {
    submitting.value = false
  }
}

function onCancel() {
  reset()
  emit('cancel')
}

function reset() {
  content.value = ''
  files.value = []
  cleanupPreviews()
  localError.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

onBeforeUnmount(() => {
  cleanupPreviews()
})
</script>
