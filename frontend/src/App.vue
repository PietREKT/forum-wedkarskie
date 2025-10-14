<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const router = useRouter()
const auth = useAuthStore()

// ciemny <html>
const theme = ref('light')
function applyTheme(t) {
  const root = document.documentElement
  if (t === 'dark') root.classList.add('dark')
  else root.classList.remove('dark')
}
function toggleTheme() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  localStorage.setItem('theme', theme.value)
  applyTheme(theme.value)
}
onMounted(() => {
  const saved = localStorage.getItem('theme')
  theme.value = saved === 'dark' ? 'dark' : 'light'
  applyTheme(theme.value)
})

async function onLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <header
      class="w-full bg-gradient-to-r from-[var(--header-from)] to-[var(--header-to)] text-white"
  >
    <div class="max-w-6xl mx-auto px-4 py-3 flex items-center gap-4">
      <!-- lewa: linki -->
      <nav class="flex items-center gap-6">
        <RouterLink class="hover:opacity-90" to="/login">Login</RouterLink>
        <RouterLink class="hover:opacity-90" to="/register">Rejestracja</RouterLink>
        <RouterLink class="hover:opacity-90" to="/profile">Profil</RouterLink>
      </nav>

      <!-- prawa: akcje -->
      <div class="ml-auto flex items-center gap-2">
        <button
            @click="toggleTheme"
            class="px-3 py-1.5 rounded-lg text-sm font-medium bg-white/10 hover:bg-white/20 border border-white/25 backdrop-blur
                 transition"
            :aria-label="theme==='dark' ? 'Przełącz na jasny' : 'Przełącz na ciemny'"
        >
          {{ theme === 'dark' ? 'Jasny' : 'Ciemny' }}
        </button>

        <!-- wyloguj tylko, gdy zalogowany -->
        <button
            v-if="auth.isAuthenticated"
            @click="onLogout"
            class="px-3 py-1.5 rounded-lg text-sm font-medium bg-white/10 hover:bg-white/20 border border-white/25 backdrop-blur
                 transition"
            title="Wyloguj"
        >
          Wyloguj
        </button>
      </div>
    </div>
  </header>

  <main class="min-h-[calc(100vh-140px)] bg-[var(--color-bg)] text-[var(--color-text)]">
    <div class="max-w-6xl mx-auto px-4 py-6">
      <router-view />
    </div>
  </main>

  <!-- stopa -->
  <footer
      class="w-full border-t border-[var(--color-border)]
           bg-[var(--color-surface)] text-[var(--color-text)]"
  >
    <div class="max-w-6xl mx-auto px-4 py-4 text-sm opacity-80">
      © Forum Wędkarzy — projekt zespołowy
    </div>
  </footer>
</template>

<style scoped>

</style>
