<template>
  <div class="min-h-[70vh] grid grid-cols-1 md:grid-cols-2 gap-8">
    <!-- LEWA -->
    <section class="bg-[var(--color-surface)] text-[var(--color-text)] rounded-2xl shadow p-8">
      <h1 class="text-3xl font-semibold mb-6">Logowanie</h1>

      <p
          v-if="justRegistered"
          class="mb-4 rounded-xl border border-green-300 bg-green-50 text-green-800 px-3 py-2 text-sm"
      >
        Konto zostało utworzone. Możesz się teraz zalogować.
      </p>

      <form @submit.prevent="onSubmit" class="space-y-5">
        <div>
          <label class="block text-sm text-[var(--color-muted)] mb-1">Nazwa użytkownika</label>
          <input
              v-model="username"
              type="text"
              autocomplete="username"
              required
              class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                 outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
          />
        </div>

        <div>
          <label class="block text-sm text-[var(--color-muted)] mb-1">Hasło</label>
          <input
              v-model="password"
              type="password"
              autocomplete="current-password"
              required
              class="w-full rounded-xl border border-[var(--color-border)] bg-transparent px-3 py-2.5
                 outline-none focus:ring-2 focus:ring-[var(--color-accent)]"
          />
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

    <!-- PRAWA (NOWE, BEZ KAFELKÓW) -->
    <aside
        class="relative overflow-hidden rounded-2xl p-0 md:p-8 bg-gradient-to-br
           from-[var(--header-from)] to-[var(--header-to)]"
    >
      <div
          class="absolute inset-0 opacity-20 dark:opacity-10 pointer-events-none"
          style="background-image: radial-gradient(#fff 1px, transparent 1px); background-size: 14px 14px;"
      ></div>

      <div class="relative h-full flex flex-col text-white">
        <header>
          <h2 class="text-2xl md:text-3xl font-semibold drop-shadow-sm">Witaj ponownie</h2>
          <p class="opacity-90 mt-1">
            Zaloguj się, aby dodawać treści, zapisywać ulubione łowiska i brać udział w wydarzeniach.
          </p>
        </header>

        <div class="mt-6 rounded-2xl bg-white/10 border border-white/20 backdrop-blur-sm p-5">
          <p class="font-semibold">Po zalogowaniu możesz:</p>
          <ul class="mt-3 space-y-2 text-white/90 text-sm">
            <li>• dodawać posty oraz komentarze</li>
            <li>• oceniać treści i łowiska</li>
            <li>• zarządzać ulubionymi łowiskami</li>
            <li>• dołączać do wydarzeń i grup</li>
            <li>• korzystać z profilu i ustawień konta</li>
          </ul>
        </div>

        <div class="mt-6 rounded-2xl bg-white/10 border border-white/20 backdrop-blur-sm p-5">
          <p class="font-semibold">Nie masz konta?</p>
          <p class="mt-2 text-white/90 text-sm">
            Załóż je w minutę i odblokuj pełną funkcjonalność serwisu.
            <RouterLink to="/register" class="underline underline-offset-4 hover:opacity-90">
              Przejdź do rejestracji
            </RouterLink>
          </p>
        </div>

        <div class="mt-auto pt-6 text-white/80 text-xs">
          Jeśli konto jest zablokowane, serwer może zwrócić odmowę logowania (403).
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter, useRoute } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const username = ref('')
const password = ref('')

const justRegistered = computed(() => route.query.registered === '1')

async function onSubmit() {
  try {
    await auth.login(username.value, password.value)
    router.push('/profile')
  } catch {}
}
</script>
