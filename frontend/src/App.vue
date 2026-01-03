<!-- src/App.vue -->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink, RouterView } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useI18n } from 'vue-i18n'
import logoUrl from './assets/olow.png'
import NotificationsBell from './components/NotificationsBell.vue'

const router = useRouter()
const auth = useAuthStore()

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

const theme = ref('light')
function applyTheme(mode) {
  theme.value = mode
  localStorage.setItem('theme', mode)
  const root = document.documentElement
  if (mode === 'dark') root.classList.add('dark')
  else root.classList.remove('dark')
}

onMounted(() => {
  applyTheme(
      localStorage.getItem('theme') ??
      (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'),
  )
  setLang(localStorage.getItem('lang') || 'pl')
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
    <header
        class="sticky top-0 z-40 border-b border-white/10
             bg-gradient-to-r from-[var(--header-from)] to-[var(--header-to)] text-white"
    >
      <div class="max-w-7xl mx-auto px-6 py-3 flex items-center justify-between">
        <!-- LEWA CZĘŚĆ: LOGO + NAV -->
        <div class="flex items-center gap-8">
          <!-- LOGO (NIE JEST LINKIEM) -->
          <div class="flex items-center gap-3 ml-6 select-none">
            <img :src="logoUrl" alt="Połów" class="h-12 w-12" />
            <span class="text-2xl font-semibold tracking-wide">
              <span class="text-emerald-200 lowercase">p</span>
              <span class="uppercase">OŁÓW</span>
            </span>
          </div>

          <!-- NAWIGACJA -->
          <nav class="flex items-center gap-6 text-sm font-medium">
            <RouterLink class="hover:opacity-90 router-link" :to="{ path: '/posts' }">
              {{ tr('nav.posts', 'Posty') }}
            </RouterLink>

            <RouterLink
                v-if="auth.isAuthenticated"
                class="hover:opacity-90 router-link"
                :to="{ path: '/events' }"
            >
              {{ tr('nav.events', 'Wydarzenia') }}
            </RouterLink>

            <RouterLink class="hover:opacity-90 router-link" :to="{ path: '/map' }">
              {{ tr('nav.map', 'Mapa') }}
            </RouterLink>

            <RouterLink class="hover:opacity-90 router-link" :to="{ path: '/guides' }">
              {{ tr('nav.guides', 'Poradniki') }}
            </RouterLink>

            <RouterLink
                v-if="auth.isAuthenticated && auth.isAdmin"
                class="hover:opacity-90 router-link text-red-300"
                :to="{ path: '/admin' }"
            >
              Panel admina
            </RouterLink>

            <RouterLink
                v-if="auth.isAuthenticated"
                class="hover:opacity-90 router-link"
                :to="{ path: '/profile' }"
            >
              {{ tr('nav.profile', 'Profil') }}
            </RouterLink>
          </nav>
        </div>

        <!-- PRAWA CZĘŚĆ -->
        <div class="flex items-center gap-3">
          <NotificationsBell v-if="auth.isAuthenticated" />

          <RouterLink
              v-if="!auth.isAuthenticated"
              class="px-3 py-1.5 rounded-lg text-sm bg-white/10 hover:bg-white/20
                   border border-white/25 backdrop-blur transition"
              :to="{ path: '/register' }"
          >
            {{ tr('auth.register', 'Rejestracja') }}
          </RouterLink>

          <RouterLink
              v-if="!auth.isAuthenticated"
              class="px-3 py-1.5 rounded-lg text-sm bg-white/10 hover:bg-white/20
                   border border-white/25 backdrop-blur transition"
              :to="{ path: '/login' }"
          >
            {{ tr('auth.login', 'Zaloguj') }}
          </RouterLink>

          <button
              v-if="auth.isAuthenticated"
              @click="onLogout"
              class="px-3 py-1.5 rounded-lg text-sm bg-white/10 hover:bg-white/20
                   border border-white/25 backdrop-blur transition"
          >
            {{ tr('auth.logout', 'Wyloguj') }}
          </button>

          <!-- TRYB -->
          <div class="flex items-center gap-1 bg-white/10 border border-white/25 rounded-lg p-1">
            <button class="px-2 py-1 rounded-md text-xs hover:bg-white/20" @click="applyTheme('dark')">
              Dark
            </button>
            <button class="px-2 py-1 rounded-md text-xs hover:bg-white/20" @click="applyTheme('light')">
              Light
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- MAIN -->
    <main class="flex-1">
      <div class="max-w-7xl mx-auto px-6 py-8">
        <RouterView />
      </div>
    </main>

    <!-- FOOTER -->
    <footer class="border-t border-white/10 text-sm text-white/80">
      <div class="max-w-7xl mx-auto px-6 py-3 flex items-center justify-between">
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
