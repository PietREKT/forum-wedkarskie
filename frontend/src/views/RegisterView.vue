<template>
  <div class="min-h-[70vh] grid grid-cols-1 md:grid-cols-2 gap-8">
    <!-- LEWA -->
    <section class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-8">
      <h1 class="text-3xl font-semibold mb-6">Rejestracja</h1>

      <form @submit.prevent="onSubmit" class="space-y-5">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-[var(--color-muted)] mb-1">Imię</label>
            <input
                v-model.trim="name"
                type="text"
                class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                     outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
            />
          </div>
          <div>
            <label class="block text-sm text-[var(--color-muted)] mb-1">Nazwisko</label>
            <input
                v-model.trim="surname"
                type="text"
                class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                     outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm text-[var(--color-muted)] mb-1">Nazwa użytkownika</label>
          <input
              v-model.trim="username"
              type="text"
              autocomplete="username"
              required
              class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                   outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
          />
        </div>

        <div>
          <label class="block text-sm text-[var(--color-muted)] mb-1">E-mail</label>
          <input
              v-model.trim="email"
              type="email"
              autocomplete="email"
              class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                   outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-[var(--color-muted)] mb-1">Hasło</label>
            <input
                v-model="password"
                type="password"
                autocomplete="new-password"
                required
                class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                     outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
            />
          </div>
          <div>
            <label class="block text-sm text-[var(--color-muted)] mb-1">Powtórz hasło</label>
            <input
                v-model="password2"
                type="password"
                autocomplete="new-password"
                required
                class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                     outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
            />
          </div>
        </div>

        <button
            :disabled="auth.status === 'loading'"
            class="w-full rounded-xl py-2.5 font-medium transition
                 bg-[var(--color-primary)] hover:bg-[var(--color-primary-600)] text-white
                 disabled:opacity-60 disabled:cursor-not-allowed shadow"
        >
          {{ auth.status === 'loading' ? 'Rejestrowanie…' : 'Zarejestruj' }}
        </button>

        <p v-if="localError" class="text-sm text-red-600">{{ localError }}</p>
        <p v-if="auth.error" class="text-sm text-red-600">{{ auth.error }}</p>

        <div class="pt-2 text-sm text-[var(--color-muted)]">
          Masz konto?
          <RouterLink to="/login" class="font-medium text-[var(--color-accent-600)] hover:underline">
            Zaloguj się
          </RouterLink>
        </div>
      </form>
    </section>

    <!-- PRAWA -->
    <aside
        class="relative overflow-hidden rounded-2xl p-0 md:p-8 bg-gradient-to-br
             from-[var(--header-from)] to-[var(--header-to)]"
    >
      <div class="absolute inset-0 opacity-20 dark:opacity-10 pointer-events-none"
           style="background-image: radial-gradient(#fff 1px, transparent 1px);
                  background-size: 14px 14px;"></div>

      <div class="relative h-full flex flex-col text-white">
        <header>
          <h2 class="text-2xl md:text-3xl font-semibold drop-shadow-sm">Załóż konto i dołącz</h2>
          <p class="opacity-90 mt-1">Publikuj połowy, śledź łowiska i wydarzenia.</p>
        </header>

        <div class="mt-6 grid grid-cols-4 sm:grid-cols-6 gap-3 md:gap-4 auto-rows-fr">
          <div v-for="a in avatars" :key="a.id"
               class="aspect-square rounded-xl flex items-center justify-center text-sm font-semibold
                      bg-white/20 text-white backdrop-blur-sm border border-white/30">
            <span>{{ a.initials }}</span>
          </div>
        </div>

        <div class="mt-auto pt-6 text-white/90 text-sm">
          <ul class="space-y-1">
            <li>• Twórz galerie połowów</li>
            <li>• Obserwuj ulubione łowiska</li>
            <li>• Bierz udział w zawodach PZW</li>
          </ul>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const auth = useAuthStore()

const name = ref('')
const surname = ref('')
const username = ref('')
const email = ref('')
const password = ref('')
const password2 = ref('')
const localError = ref('')

async function onSubmit() {
  localError.value = ''
  if (password.value !== password2.value) {
    localError.value = 'Hasła muszą być takie same.'
    return
  }
  if (!username.value || !password.value) {
    localError.value = 'Uzupełnij wymagane pola.'
    return
  }
  try {
    await auth.register({
      username: username.value,
      password: password.value,
      firstName: name.value || undefined,
      lastName: surname.value || undefined,
      email: email.value || undefined,
    })
    router.push({ path: '/login', query: { registered: '1' } })
  } catch {}
}

const avatars = ref([
  { id: 1, initials: 'AK' }, { id: 2, initials: 'MS' }, { id: 3, initials: 'JP' },
  { id: 4, initials: 'ŁB' }, { id: 5, initials: 'KO' }, { id: 6, initials: 'ZS' },
  { id: 7, initials: 'PW' }, { id: 8, initials: 'NM' }, { id: 9, initials: 'TS' },
  { id: 10, initials: 'EW' }, { id: 11, initials: 'RS' }, { id: 12, initials: 'DK' },
])
</script>

