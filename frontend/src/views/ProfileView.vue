<template>
  <div class="space-y-8">
    <section
        class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6 md:p-8"
    >
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 items-start">
        <!-- Avatar + nik -->
        <div class="flex items-center md:block gap-5">
          <div
              class="h-28 w-28 rounded-full bg-[var(--color-bg)]
                   ring-4 ring-[var(--color-border)] flex items-center justify-center overflow-hidden"
          >
            <img
                v-if="avatarUrl"
                :src="avatarUrl"
                alt="Zdjęcie profilowe"
                class="h-full w-full object-cover"
            />
            <span v-else class="text-[var(--color-muted)] text-sm">zdjęcie</span>
          </div>

          <div class="md:mt-4">
            <p class="text-sm text-[var(--color-muted)]">nik</p>
            <h2 class="text-2xl font-semibold">
              {{ user?.username || 'użytkownik' }}
            </h2>
          </div>
        </div>

        <!-- Dane tekstowe -->
        <div class="md:col-span-2">
          <h3 class="text-lg font-semibold mb-3">Dane</h3>
          <dl class="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-3">
            <div>
              <dt class="text-sm text-[var(--color-muted)]">Imię</dt>
              <dd class="text-base">{{ user?.name || '—' }}</dd>
            </div>
            <div>
              <dt class="text-sm text-[var(--color-muted)]">Nazwisko</dt>
              <dd class="text-base">{{ user?.surname || '—' }}</dd>
            </div>
            <div>
              <dt class="text-sm text-[var(--color-muted)]">E-mail</dt>
              <dd class="text-base">{{ user?.email || '—' }}</dd>
            </div>
            <div>
              <dt class="text-sm text-[var(--color-muted)]">ID</dt>
              <dd class="text-base">{{ user?.id ?? '—' }}</dd>
            </div>
          </dl>

          <div class="mt-6 flex flex-wrap gap-3">
            <!--do postów użytkownika -->
            <RouterLink
                :to="`/posts?userId=${encodeURIComponent(user?.id ?? '')}`"
                class="inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-medium
                     bg-[var(--color-primary)] hover:bg-[var(--color-primary-600)] text-white transition shadow"
                title="Zobacz posty użytkownika"
            >
              Posty użytkownika
            </RouterLink>

            <!-- Odśwież dopóki nie ma /me) -->
            <button
                @click="onRefresh"
                class="inline-flex items-center rounded-xl px-4 py-2 text-sm font-medium
                     border border-[var(--color-border)]
                     bg-[var(--color-bg)] text-[var(--color-text)] hover:opacity-90"
                :disabled="loading"
            >
              Odśwież
            </button>
          </div>

          <p v-if="auth.error" class="mt-3 text-sm text-red-600">{{ auth.error }}</p>
        </div>
      </div>
    </section>

    <section class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Galerie -->
      <div
          class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6"
      >
        <h3 class="text-lg font-semibold mb-4">galerie</h3>
        <div
            class="h-40 md:h-44 rounded-xl border-2 border-[var(--color-border)]
                 flex items-center justify-center text-[var(--color-muted)]"
        >
          galeria
        </div>
        <!-- do zrobienia lista galerii -->
      </div>

      <!-- dane -->
      <div
          class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6"
      >
        <h3 class="text-lg font-semibold mb-4">sekcja</h3>
        <div
            class="h-40 md:h-44 rounded-xl border-2 border-[var(--color-border)]
                 flex items-center justify-center text-[var(--color-muted)]"
        >
          blok
        </div>
        <!-- do zrobienia np. łowiska OWNER / wydarzenia PZW -->
      </div>

      <!-- Dwa mniejsze boksy -->
      <div
          class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6"
      >
        <h3 class="text-lg font-semibold mb-4">sekcja</h3>
        <div
            class="h-28 rounded-xl border-2 border-[var(--color-border)]
                 flex items-center justify-center text-[var(--color-muted)]"
        >
          blok
        </div>
        <!-- do zrobienia ostatnie połowy -->
      </div>

      <div
          class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-6"
      >
        <h3 class="text-lg font-semibold mb-4">sekcja</h3>
        <div
            class="h-28 rounded-xl border-2 border-[var(--color-border)]
                 flex items-center justify-center text-[var(--color-muted)]"
        >
          blok
        </div>
        <!-- do zrobienia np. wydarzenia, odznaki itp. -->
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const user = computed(() => auth.user)
const avatarUrl = computed(() => user.value?.avatarUrl || '')
const loading = ref(false)

// do zrobienia aktualne dane profilu
async function onRefresh() {
  loading.value = true
  try {
  } finally {
    loading.value = false
  }
}
</script>
