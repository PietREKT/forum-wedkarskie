<template>
  <div
      class="rounded-lg border bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100 px-3 py-3 md:px-4 md:py-3"
  >
    <h3 class="font-medium mb-2 text-sm md:text-base">Nowy post</h3>

    <!-- lokalny komunikat błędu -->
    <p
        v-if="localError"
        class="mb-2 text-xs text-red-600 dark:text-red-400"
    >
      {{ localError }}
    </p>

    <!-- Treść posta -->
    <textarea
        v-model.trim="content"
        class="w-full rounded-md px-3 py-2 text-sm
             bg-white text-zinc-900 placeholder-zinc-500
             dark:bg-zinc-800 dark:text-zinc-100 dark:placeholder-zinc-400
             border border-zinc-300 focus:outline-none focus:ring-2 focus:ring-cyan-500
             dark:border-zinc-600 dark:focus:ring-cyan-400"
        rows="3"
        placeholder="Napisz coś..."
    />

    <!-- Załączone pliki -->
    <div class="mt-3">
      <label class="inline-flex items-center gap-2 text-xs md:text-sm cursor-pointer">
        <span class="px-2 py-1 border rounded-md">Dodaj zdjęcia</span>
        <span class="text-zinc-500">
          {{ summary }}
        </span>
        <input
            ref="fileInput"
            type="file"
            class="hidden"
            accept="image/*"
            multiple
            @change="onFilesSelected"
        >
      </label>

      <!-- podglądy -->
      <div v-if="previews.length" class="mt-2 flex flex-wrap gap-2">
        <div
            v-for="(src, idx) in previews"
            :key="idx"
            class="relative w-20 h-20 rounded-md overflow-hidden border"
        >
          <img :src="src" alt="Podgląd" class="w-full h-full object-cover">
        </div>
      </div>
    </div>

    <!-- Przyciski -->
    <div class="mt-3 flex justify-end gap-2">
      <button
          type="button"
          class="px-3 py-1.5 text-xs md:text-sm border rounded-md"
          @click="onCancel"
          :disabled="submitting"
      >
        Anuluj
      </button>
      <button
          type="button"
          class="px-3 py-1.5 text-xs md:text-sm rounded-md bg-cyan-600 text-white disabled:opacity-60"
          :disabled="submitting || !canSubmit"
          @click="onSubmit"
      >
        {{ submitting ? 'Zapisywanie...' : 'Dodaj post' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
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
  if (!files.value.length) return ''
  if (files.value.length === 1) return '1 plik'
  return `${files.value.length} pliki(ów)`
})

const canSubmit = computed(() => {
  return content.value.trim().length > 0 || files.value.length > 0
})

function onFilesSelected(event) {
  const selected = Array.from(event.target.files || [])
  files.value = selected
  previews.value.forEach(u => URL.revokeObjectURL(u))
  previews.value = selected.map(f => URL.createObjectURL(f))
}

async function onSubmit() {
  localError.value = ''
  if (!canSubmit.value) {
    localError.value = 'Post musi mieć treść lub załącznik.'
    return
  }

  submitting.value = true
  try {
    await store.createPost({
      content: content.value,
      files: files.value,
    })
    reset()
    emit('done')
  } catch (e) {
    const status = e?.response?.status
    if (status === 401) {
      localError.value = 'Musisz być zalogowany, aby dodać post.'
    } else {
      // jeśli store.error ma treść – pokaż ją; w przeciwnym razie domyślny tekst
      localError.value = store.error || 'Nie udało się dodać posta.'
    }
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
  previews.value.forEach(u => URL.revokeObjectURL(u))
  previews.value = []
  localError.value = ''
  if (fileInput.value) fileInput.value.value = ''
}
</script>
