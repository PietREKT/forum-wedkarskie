<template>
  <div class="min-h-[70vh] grid grid-cols-1 md:grid-cols-2 gap-8">
    <!-- LEWA -->
    <section class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-8">
      <h1 class="text-3xl font-semibold mb-6">Logowanie</h1>

      <form @submit.prevent="onSubmit" class="space-y-5">
        <div>
          <label class="block text-sm text-[var(--color-muted)] mb-1">Nazwa użytkownika</label>
          <div class="relative">
            <input
                v-model="username"
                type="text"
                autocomplete="username"
                required
                class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                     outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm text-[var(--color-muted)] mb-1">Hasło</label>
          <div class="relative">
            <input
                v-model="password"
                type="password"
                autocomplete="current-password"
                required
                class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                     outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
            />
          </div>
        </div>

        <div class="flex items-center justify-between">
          <label class="inline-flex items-center gap-2 text-sm text-[var(--color-muted)]">
            <input type="checkbox" class="rounded border-[var(--color-border)]" />
            Zapamiętaj mnie
          </label>
          <a class="text-sm text-[var(--color-accent-600)] hover:underline" href="#">
            Nie pamiętam hasła
          </a>
        </div>

        <button
            :disabled="auth.status === 'loading'"
            class="w-full rounded-xl py-2.5 font-medium transition
                 bg-[var(--color-primary)] hover:bg-[var(--color-primary-600)] text-white
                 disabled:opacity-60 disabled:cursor-not-allowed shadow"
        >
          {{ auth.status === 'loading' ? 'Logowanie…' : 'Zaloguj' }}
        </button>

        <p v-if="auth.error" class="text-sm text-red-600">{{ auth.error }}</p>

        <div class="pt-2 text-sm text-[var(--color-muted)]">
          Nie masz konta?
          <RouterLink to="/register" class="font-medium text-[var(--color-accent-600)] hover:underline">
            Zarejestruj się
          </RouterLink>
        </div>
      </form>
    </section>

    <!-- PRAWA-->
    <aside
        class="relative overflow-hidden rounded-2xl p-0 md:p-8 bg-gradient-to-br
             from-[var(--header-from)] to-[var(--header-to)]"
    >
      <div class="absolute inset-0 opacity-20 dark:opacity-10 pointer-events-none"
           style="background-image: radial-gradient(#fff 1px, transparent 1px);
                  background-size: 14px 14px;"></div>

      <div class="relative h-full flex flex-col text-white">
        <header>
          <h2 class="text-2xl md:text-3xl font-semibold drop-shadow-sm">Witaj na Forum Wędkarzy</h2>
          <p class="opacity-90 mt-1">Dołącz do społeczności, dziel się połowami i poradami.</p>
        </header>

        <!-- siatka avatarów -->
        <div class="mt-6 grid grid-cols-4 sm:grid-cols-6 gap-3 md:gap-4 auto-rows-fr">
          <div v-for="a in avatars" :key="a.id"
               class="aspect-square rounded-xl flex items-center justify-center text-sm font-semibold
                      bg-white/20 text-white backdrop-blur-sm border border-white/30">
            <span>{{ a.initials }}</span>
          </div>
        </div>

        <!-- dolny pasek informacji -->
        <div class="mt-auto pt-6 text-white/90 text-sm">
          <ul class="space-y-1">
            <li>• Posty z łowisk w całej Polsce</li>
            <li>• Poradniki i kalendarz połowów</li>
            <li>• Wydarzenia i zawody PZW</li>
          </ul>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const username = ref('')
const password = ref('')

async function onSubmit() {
  try {
    await auth.login(username.value, password.value)
  } catch {
  }
}

const avatars = ref([
  { id: 1, initials: 'AK' }, { id: 2, initials: 'MS' }, { id: 3, initials: 'JP' },
  { id: 4, initials: 'ŁB' }, { id: 5, initials: 'KO' }, { id: 6, initials: 'ZS' },
  { id: 7, initials: 'PW' }, { id: 8, initials: 'NM' }, { id: 9, initials: 'TS' },
  { id: 10, initials: 'EW' }, { id: 11, initials: 'RS' }, { id: 12, initials: 'DK' },
])
</script>

