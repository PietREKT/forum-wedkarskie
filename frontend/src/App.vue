<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink, RouterView } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const auth = useAuthStore()

const { locale, t, te } = useI18n()
function tr(key, fallback) {
  try { return te && te(key) ? t(key) : fallback } catch { return fallback }
}
function setLang(lang) {
  locale.value = lang
  localStorage.setItem('lang', lang)
}

const theme = ref('light')
function applyTheme(mode) {
  theme.value = mode
  localStorage.setItem('theme', mode)
  const root = document.documentElement
  if (mode === 'dark') root.classList.add('dark')
  else root.classList.remove('dark')
}
onMounted(() => {
  applyTheme(localStorage.getItem('theme') ?? (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'))
  setLang(localStorage.getItem('lang') || 'pl')
})

async function onLogout() {
  try { await auth.logout() } finally { router.push({ name: 'login' }) }
}
</script>

<template>
  <div class="min-h-screen flex flex-col bg-[var(--color-bg)] text-[var(--color-text)]">
    <header class="sticky top-0 z-40 border-b border-white/10 bg-gradient-to-r from-[var(--header-from)] to-[var(--header-to)] text-white">
      <div class="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
        <nav class="flex items-center gap-6 text-sm font-medium">
          <!-- Home -> /posts -->
          <RouterLink class="hover:opacity-90 router-link" :to="{ path: '/posts' }">
            {{ tr('nav.home', 'Strona główna') }}
          </RouterLink>

          <!-- Posty -->
          <RouterLink
              v-if="auth.isAuthenticated"
              class="hover:opacity-90 router-link"
              :to="{ path: '/posts' }"
          >
            {{ tr('nav.posts', 'Posty') }}
          </RouterLink>

          <RouterLink
              v-if="auth.isAuthenticated"
              class="hover:opacity-90 router-link"
              :to="{ path: '/events' }"
          >
            {{ tr('nav.events', 'Wydarzenia') }}
          </RouterLink>

          <span class="opacity-70 cursor-not-allowed" title="Brak trasy — moduł w budowie">
            {{ tr('nav.map', 'Mapa') }}
          </span>
          <span class="opacity-70 cursor-not-allowed" title="Brak trasy — moduł w budowie">
            {{ tr('nav.guides', 'Poradniki') }}
          </span>

          <!-- Profil -->
          <RouterLink
              v-if="auth.isAuthenticated"
              class="hover:opacity-90 router-link"
              :to="{ path: '/profile' }"
          >
            {{ tr('nav.profile', 'Profil') }}
          </RouterLink>
        </nav>

        <div class="flex items-center gap-3">
          <RouterLink
              v-if="!auth.isAuthenticated"
              class="px-3 py-1.5 rounded-lg text-sm bg-white/10 hover:bg白/20 border border-white/25 backdrop-blur transition"
              :to="{ path: '/register' }"
          >
            {{ tr('auth.register', 'Rejestracja') }}
          </RouterLink>
          <RouterLink
              v-if="!auth.isAuthenticated"
              class="px-3 py-1.5 rounded-lg text-sm bg-white/10 hover:bg-white/20 border border-white/25 backdrop-blur transition"
              :to="{ path: '/login' }"
          >
            {{ tr('auth.login', 'Zaloguj') }}
          </RouterLink>

          <button
              v-if="auth.isAuthenticated"
              @click="onLogout"
              class="px-3 py-1.5 rounded-lg text-sm bg-white/10 hover:bg-white/20 border border-white/25 backdrop-blur transition"
          >
            {{ tr('auth.logout', 'Wyloguj') }}
          </button>

          <!-- Tryb -->
          <div class="flex items-center gap-1 bg-white/10 border border-white/25 rounded-lg p-1">
            <button class="px-2 py-1 rounded-md text-xs hover:bg-white/20" @click="applyTheme('dark')">Dark</button>
            <button class="px-2 py-1 rounded-md text-xs hover:bg-white/20" @click="applyTheme('light')">Light</button>
          </div>

          <!-- Język -->
          <div class="flex items-center gap-1 bg-white/10 border border-white/25 rounded-lg p-1">
            <button class="px-2 py-1 rounded-md text-xs hover:bg-white/20" @click="setLang('pl')">PL</button>
            <button class="px-2 py-1 rounded-md text-xs hover:bg-white/20" @click="setLang('en')">EN</button>
          </div>
        </div>
      </div>
    </header>

    <main class="flex-1">
      <div class="max-w-6xl mx-auto px-4 py-8">
        <RouterView />
      </div>
    </main>

    <footer class="border-t border-white/10 text-sm text-white/80">
      <div class="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
        <span>Forum Wędkarskie</span>
        <span>{{ new Date().getFullYear() }}</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.router-link-active.router-link,
.router-link-exact-active.router-link {
  text-decoration: underline;
  text-underline-offset: 4px;
}
</style>
