<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '../../utils/axios'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

// w store masz computed isAdmin (ADMIN + ROOT)
const isAdmin = computed(() => auth.isAdmin)

const form = ref({
  content: '',
  methods: [],
  fishIds: [],
})

const saving = ref(false)
const error = ref(null)
const fish = ref([])
const fishLoading = ref(false)
const fishError = ref(null)

async function loadFish() {
  fishLoading.value = true
  fishError.value = null
  try {
    const resp = await apiClient.get('/fish')
    const list = Array.isArray(resp.data) ? resp.data : (resp.data?.content || [])
    fish.value = list
  } catch (e) {
    console.error('Błąd pobierania ryb', e)
    fishError.value = 'Nie udało się pobrać listy ryb.'
  } finally {
    fishLoading.value = false
  }
}

function toggleMethod(method) {
  const idx = form.value.methods.indexOf(method)
  if (idx === -1) form.value.methods.push(method)
  else form.value.methods.splice(idx, 1)
}

function toggleFish(id) {
  const idx = form.value.fishIds.indexOf(id)
  if (idx === -1) form.value.fishIds.push(id)
  else form.value.fishIds.splice(idx, 1)
}

async function submitForm() {
  if (!isAdmin.value) return

  if (!form.value.content.trim()) {
    error.value = 'Treść poradnika nie może być pusta.'
    return
  }

  saving.value = true
  error.value = null

  try {
    // BACKEND: TutorialCreateDto prawdopodobnie: { content, methods, fishIds }
    const payload = {
      content: form.value.content,
      methods: form.value.methods,
      fishIds: form.value.fishIds,
    }

    console.log('Tutorial create payload:', payload)

    await apiClient.post('/tutorials/create', payload)
    router.push('/guides')
  } catch (e) {
    console.error('Błąd tworzenia poradnika', e)
    // tu w razie czego można podejrzeć e.response?.data w konsoli
    error.value = 'Nie udało się utworzyć poradnika.'
  } finally {
    saving.value = false
  }
}

onMounted(loadFish)
</script>

<template>
  <section class="space-y-4">
    <header class="flex items-center justify-between gap-4">
      <h1 class="text-2xl font-semibold">
        Dodaj poradnik
      </h1>

      <RouterLink
          to="/guides"
          class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-white/5"
      >
        Wróć do listy
      </RouterLink>
    </header>

    <div v-if="!isAdmin" class="text-sm opacity-80">
      Brak uprawnień do dodawania poradników.
    </div>

    <form
        v-else
        class="space-y-4 max-w-3xl"
        @submit.prevent="submitForm"
    >
      <div>
        <label class="block text-sm font-medium mb-1">
          Treść poradnika
        </label>
        <textarea
            v-model="form.content"
            rows="10"
            class="w-full border rounded-md px-3 py-2 bg-[var(--color-bg)] text-sm resize-y"
            placeholder="Opisz krok po kroku zestaw, przynęty, prowadzenie, wskazówki…"
        />
      </div>

      <div>
        <p class="text-sm font-medium mb-1">
          Metody połowu
        </p>
        <div class="flex flex-wrap gap-2">
          <button
              v-for="m in ['FLOAT','SPINNING','FLY','GROUND','ICE','NET']"
              :key="m"
              type="button"
              class="px-2 py-1 text-xs rounded-full border"
              :class="form.methods.includes(m) ? 'bg-white/10 font-semibold' : 'bg-[var(--color-bg)] hover:bg-white/5'"
              @click="toggleMethod(m)"
          >
            {{ m }}
          </button>
        </div>
      </div>

      <div>
        <p class="text-sm font-medium mb-1">
          Gatunki ryb (opcjonalnie)
        </p>

        <div v-if="fishError" class="text-xs text-red-400 mb-1">
          {{ fishError }}
        </div>
        <div v-else-if="fishLoading" class="text-xs opacity-80 mb-1">
          Ładowanie listy ryb…
        </div>

        <div class="flex flex-wrap gap-2 max-h-56 overflow-auto border rounded-md p-2 bg-[var(--color-bg-elevated)]">
          <label
              v-for="f in fish"
              :key="f.id ?? f.name"
              class="flex items-center gap-2 text-xs cursor-pointer"
          >
            <input
                type="checkbox"
                class="accent-current"
                :checked="form.fishIds.includes(f.id)"
                @change="toggleFish(f.id)"
            />
            <span>{{ f.name }}</span>
          </label>
        </div>
      </div>

      <div v-if="error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
        {{ error }}
      </div>

      <button
          type="submit"
          class="px-4 py-2 text-sm rounded-md border bg-emerald-700/40 hover:bg-emerald-600/50 disabled:opacity-50"
          :disabled="saving"
      >
        Zapisz poradnik
      </button>
    </form>
  </section>
</template>
