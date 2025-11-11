<!-- src/App.vue -->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink, RouterView } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const auth = useAuthStore()

// i18n z helperem: jeśli brak klucza, użyj fallbacku
const { locale, t, te } = useI18n()
function tr(key, fallback) {
  try {
    return te && te(key) ? t(key) : fallback
  } catch {
    return fallback
  }
}
function setLang(lang) {
  locale.value = lang
  localStorage.setItem('lang', lang)
}

// Motyw ciemny/jasny
const theme = ref('light')
function applyTheme(mode) {
  theme.value = mode
  localStorage.setItem('theme', mode)
  const root = document.documentElement
  if (mode === 'dark') root.classList.add('dark')
  else root.classList.remove('dark')
}
function toggleTheme() {
  applyTheme(theme.value === 'dark' ? 'light' : 'dark')
}

onMounted(() => {
  const savedTheme = localStorage.getItem('theme')
  const initialTheme = savedTheme ?? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
  applyTheme(initialTheme)

  const savedLang = localStorage.getItem('lang') || 'pl'
  setLang(savedLang)
})

async function onLogout() {
  try {
    await auth.logout()
  } finally {
    router.push({ name: 'login' })
  }
}
</script>

<template>
  <div class="min-h-screen flex flex-col bg-[var(--color-bg)] text-[var(--color-text)]">
    <!-- HEADER -->
    <header class="sticky top-0 z-40 border-b border-white/10 bg-gradient-to-r from-[var(--header-from)] to-[var(--header-to)] text-white">
      <div class="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
        <!-- Lewa strona: nawigacja -->
        <nav class="flex items-center gap-6 text-sm font-medium">
          <!-- Strona główna -->
          <RouterLink class="hover:opacity-90 router-link" :to="{ path: '/' }">
            {{ tr('nav.home', 'Strona główna') }}
          </RouterLink>

          <!-- Posty (po zalogowaniu) -->
          <RouterLink
              v-if="auth.isAuthenticated"
              class="hover:opacity-90 router-link"
              :to="{ path: '/posts' }"
          >
            {{ tr('nav.posts', 'Posty') }}
          </RouterLink>

          <!-- Wydarzenia (po zalogowaniu) -->
          <RouterLink
              v-if="auth.isAuthenticated"
              class="hover:opacity-90 router-link"
              :to="{ path: '/events' }"
          >
            {{ tr('nav.events', 'Wydarzenia') }}
          </RouterLink>

          <!-- Mapa (zawsze) -->
          <RouterLink class="hover:opacity-90 router-link" :to="{ path: '/map' }">
            {{ tr('nav.map', 'Mapa') }}
          </RouterLink>

          <!-- Poradniki (zawsze) -->
          <RouterLink class="hover:opacity-90 router-link" :to="{ path: '/guides' }">
            {{ tr('nav.guides', 'Poradniki') }}
          </RouterLink>

          <!-- Profil (po zalogowaniu) -->
          <RouterLink
              v-if="auth.isAuthenticated"
              class="hover:opacity-90 router-link"
              :to="{ path: '/profile' }"
          >
            {{ tr('nav.profile', 'Profil') }}
          </RouterLink>
        </nav>

        <!-- Prawa strona: auth + tryb + język -->
        <div class="flex items-center gap-3">
          <!-- Rejestracja (niezalogowany) -->
          <RouterLink
              v-if="!auth.isAuthenticated"
              class="px-3 py-1.5 rounded-lg text-sm bg-white/10 hover:bg-white/20 border border-white/25 backdrop-blur transition"
              :to="{ path: '/register' }"
          >
            {{ tr('auth.register', 'Rejestracja') }}
          </RouterLink>

          <!-- Zaloguj (niezalogowany) -->
          <RouterLink
              v-if="!auth.isAuthenticated"
              class="px-3 py-1.5 rounded-lg text-sm bg-white/10 hover:bg-white/20 border border-white/25 backdrop-blur transition"
              :to="{ path: '/login' }"
          >
            {{ tr('auth.login', 'Zaloguj') }}
          </RouterLink>

          <!-- Wyloguj (zalogowany) -->
          <button
              v-if="auth.isAuthenticated"
              @click="onLogout"
              class="px-3 py-1.5 rounded-lg text-sm bg-white/10 hover:bg-white/20 border border-white/25 backdrop-blur transition"
          >
            {{ tr('auth.logout', 'Wyloguj') }}
          </button>

          <!-- Przełącznik trybu: Dark / Light -->
          <div class="flex items-center gap-1 bg-white/10 border border-white/25 rounded-lg p-1">
            <button
                class="px-2 py-1 rounded-md text-xs hover:bg-white/20"
                :class="theme === 'dark' ? 'bg-white/20' : ''"
                @click="applyTheme('dark')"
            >
              Dark
            </button>
            <button
                class="px-2 py-1 rounded-md text-xs hover:bg-white/20"
                :class="theme === 'light' ? 'bg-white/20' : ''"
                @click="applyTheme('light')"
            >
              Light
            </button>
          </div>

          <!-- Przełącznik języka: PL / EN -->
          <div class="flex items-center gap-1 bg-white/10 border border-white/25 rounded-lg p-1">
            <button
                class="px-2 py-1 rounded-md text-xs hover:bg-white/20"
                :class="locale === 'pl' ? 'bg-white/20' : ''"
                @click="setLang('pl')"
            >
              PL
            </button>
            <button
                class="px-2 py-1 rounded-md text-xs hover:bg-white/20"
                :class="locale === 'en' ? 'bg-white/20' : ''"
                @click="setLang('en')"
            >
              EN
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- MAIN -->
    <main class="flex-1">
      <div class="max-w-6xl mx-auto px-4 py-8">
        <RouterView />
      </div>
    </main>

    <!-- FOOTER -->
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
