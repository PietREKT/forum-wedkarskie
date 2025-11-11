<template>
  <div
      class="rounded-lg border bg-white text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100
           px-3 py-3 md:px-4 md:py-3"
  >
    <h3 class="font-medium mb-2 text-sm md:text-base">Nowy post</h3>

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
    ></textarea>

    <!-- Strefa zdjęć-->
    <div
        class="mt-2 rounded-md border-2 border-dashed cursor-pointer select-none
             border-zinc-300 hover:border-zinc-400 bg-zinc-50
             dark:border-zinc-600 dark:hover:border-zinc-500 dark:bg-zinc-800
             px-3 py-2"
        @click="openFile"
        @dragover.prevent
        @drop.prevent="onDrop"
    >
      <p class="text-sm">
        <span class="font-medium">Zdjęcia</span> – kliknij, aby wybrać lub upuść tutaj pliki
      </p>
      <p class="text-xs mt-1 text-zinc-600 dark:text-zinc-400">
        {{ files.length ? summary : 'Nie wybrano plików' }}
      </p>

      <input
          ref="fileInput"
          type="file"
          accept="image/*"
          multiple
          class="hidden"
          @change="onFiles"
      />
    </div>

    <!-- Podglądy  -->
    <div v-if="previews.length" class="mt-2 grid grid-cols-3 sm:grid-cols-4 gap-2">
      <img
          v-for="(src, i) in previews"
          :key="i"
          :src="src"
          class="w-full h-20 object-cover rounded-md border border-zinc-200 dark:border-zinc-700"
          alt="Podgląd zdjęcia"
      />
    </div>

    <div class="mt-3 flex items-center justify-end gap-2">
      <button
          class="px-3 py-1.5 rounded-md text-sm border
               border-zinc-300 hover:bg-zinc-100
               dark:border-zinc-600 dark:hover:bg-zinc-800"
          type="button"
          @click="onCancel"
      >
        Anuluj
      </button>
      <button
          class="px-3 py-1.5 rounded-md text-sm
               bg-zinc-900 text-white hover:bg-zinc-800
               dark:bg-white dark:text-zinc-900 dark:hover:bg-zinc-100
               disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="submitting || !content"
          type="button"
          @click="onSubmit"
      >
        {{ submitting ? 'Wysyłanie...' : 'Dodaj post' }}
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

const summary = computed(() => {
  if (!files.value.length) return ''
  if (files.value.length === 1) return `Wybrano: ${files.value[0].name}`
  return `Wybrano ${files.value.length} pliki(ów)`
})

function openFile() {
  fileInput.value?.click()
}

function onFiles(e) {
  const list = Array.from(e.target.files || [])
  setFiles(list)
}

function onDrop(e) {
  const list = Array.from(e.dataTransfer?.files || []).filter(f => f.type.startsWith('image/'))
  setFiles(files.value.concat(list))
}

function setFiles(list) {
  previews.value.forEach(u => URL.revokeObjectURL(u))
  files.value = list
  previews.value = files.value.map(f => URL.createObjectURL(f))
}

async function onSubmit() {
  if (!content.value) return
  submitting.value = true
  try {
    await store.createPost({ content: content.value, files: files.value })
    reset()
    emit('done')
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
  if (fileInput.value) fileInput.value.value = ''
}
</script>
