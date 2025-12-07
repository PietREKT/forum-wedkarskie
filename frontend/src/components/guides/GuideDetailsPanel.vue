<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiClient } from '../../utils/axios'

const route = useRoute()
const router = useRouter()

const tutorial = ref(null)
const loading = ref(false)
const error = ref(null)

async function loadTutorial() {
  loading.value = true
  error.value = null
  try {
    const id = route.params.id
    const resp = await apiClient.get(`/tutorials/${id}`)
    tutorial.value = resp.data
  } catch (e) {
    console.error('Błąd pobierania poradnika', e)
    error.value = 'Nie udało się pobrać poradnika.'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(loadTutorial)
</script>

<template>
  <section class="space-y-4">
    <header class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-semibold">
          {{ tutorial?.content?.content?.split('\n')[0] || 'Poradnik wędkarski' }}
        </h1>
        <p class="text-xs opacity-70 mt-1">
          Autor:
          <span class="font-medium">
            {{ tutorial?.content?.author?.username || 'nieznany' }}
          </span>
        </p>
      </div>

      <button
          type="button"
          class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-white/5"
          @click="goBack"
      >
        Wróć
      </button>
    </header>

    <div v-if="loading" class="text-sm opacity-80">
      Ładowanie poradnika…
    </div>

    <div v-if="error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
      {{ error }}
    </div>

    <div
        v-if="!loading && !error && tutorial"
        class="space-y-4"
    >
      <div
          v-if="tutorial.methods && tutorial.methods.length"
          class="flex flex-wrap gap-2"
      >
        <span
            v-for="m in tutorial.methods"
            :key="m"
            class="text-[11px] uppercase tracking-wide px-2 py-0.5 rounded-full border border-white/20"
        >
          {{ m }}
        </span>
      </div>

      <div
          v-if="tutorial.fishMentioned && tutorial.fishMentioned.length"
          class="flex flex-wrap gap-2"
      >
        <span
            v-for="fishItem in tutorial.fishMentioned"
            :key="fishItem.id ?? fishItem.name"
            class="text-xs px-2 py-0.5 rounded-full border border-white/20"
        >
          {{ fishItem.name }}
        </span>
      </div>

      <article class="border rounded-2xl p-4 bg-[var(--color-bg-elevated)] text-sm leading-relaxed whitespace-pre-wrap">
        {{ tutorial.content?.content }}
      </article>
    </div>
  </section>
</template>
