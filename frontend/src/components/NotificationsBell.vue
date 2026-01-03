<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { RouterLink } from 'vue-router'
import { useNotificationsStore } from '../stores/notifications.js'

const notif = useNotificationsStore()
const open = ref(false)
const boxRef = ref(null)

const unread = computed(() => notif.unreadCount)

function toggle() {
  open.value = !open.value
  if (open.value) notif.fetchMy()
}

function close() {
  open.value = false
}

function onDocClick(e) {
  const el = boxRef.value
  if (!el) return
  if (!el.contains(e.target)) close()
}

function getTitle(n) {
  return String(n?.title || n?.type || 'Powiadomienie')
}

function getBody(n) {
  return String(n?.message || n?.content || n?.text || '')
}

function getCreatedAt(n) {
  return n?.createdAt || n?.created_at || n?.date || n?.timestamp || null
}

function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  if (Number.isNaN(date.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(date.getDate())}.${pad(date.getMonth() + 1)} ${pad(date.getHours())}:${pad(
      date.getMinutes(),
  )}`
}

async function markRead(n) {
  const id = notif.getId(n)
  if (!id) return
  try {
    await notif.markRead(id)
  } catch (e) {
    console.error('markRead error', e)
  }
}

async function removeOne(n) {
  const id = notif.getId(n)
  if (!id) return
  try {
    await notif.remove(id)
  } catch (e) {
    console.error('remove error', e)
  }
}

onMounted(async () => {
  document.addEventListener('click', onDocClick)
  await notif.fetchMy()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
})
</script>

<template>
  <div class="relative" ref="boxRef">
    <button
        type="button"
        class="relative px-2 py-1 rounded-md border
             !bg-white !text-gray-900 !border-gray-300
             hover:!bg-gray-100
             dark:!bg-[#0b0f14] dark:!text-gray-100 dark:!border-[#273043]
             dark:hover:!bg-[#111826]"
        @click.stop="toggle"
        :disabled="notif.loading && !open"
        aria-label="Powiadomienia"
        title="Powiadomienia"
    >
      <svg
          class="h-5 w-5"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          aria-hidden="true"
      >
        <path d="M18 8a6 6 0 10-12 0c0 7-3 7-3 7h18s-3 0-3-7" />
        <path d="M13.73 21a2 2 0 01-3.46 0" />
      </svg>

      <span
          v-if="unread > 0"
          class="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 text-[11px]
               rounded-full !bg-red-600 !text-white flex items-center justify-center"
      >
        {{ unread > 99 ? '99+' : unread }}
      </span>
    </button>

    <!-- DROPDOWN: tło wymuszone (zero przezroczystości) -->
    <div
        v-if="open"
        class="absolute right-0 mt-2 w-[360px] max-w-[90vw] rounded-xl border shadow-2xl p-3 space-y-2 z-50
             !bg-white !text-gray-900 !border-gray-200
             dark:!bg-[#0b0f14] dark:!text-gray-100 dark:!border-[#273043]"
    >
      <div class="flex items-center justify-between gap-2 border-b pb-2
                  !border-gray-200 dark:!border-[#273043]">
        <div class="text-sm font-semibold">Powiadomienia</div>

        <div class="flex items-center gap-2">
          <button
              type="button"
              class="px-2.5 py-1 text-xs rounded-md border
                   !bg-white !text-gray-900 !border-gray-200 hover:!bg-gray-100
                   dark:!bg-[#111826] dark:!text-gray-100 dark:!border-[#273043] dark:hover:!bg-[#172033]
                   disabled:opacity-50"
              :disabled="notif.loading"
              @click="notif.fetchMy"
          >
            Odśwież
          </button>

          <button
              type="button"
              class="px-2.5 py-1 text-xs rounded-md border
                   !bg-white !text-gray-900 !border-gray-200 hover:!bg-gray-100
                   dark:!bg-[#111826] dark:!text-gray-100 dark:!border-[#273043] dark:hover:!bg-[#172033]
                   disabled:opacity-50"
              :disabled="notif.loading || unread === 0"
              @click="notif.markAllRead"
          >
            Oznacz wszystkie
          </button>
        </div>
      </div>

      <div
          v-if="notif.error"
          class="text-xs rounded-md border px-3 py-2
               !border-red-300 !bg-red-50 !text-red-700
               dark:!border-red-500/70 dark:!bg-red-500/15 dark:!text-red-200"
      >
        {{ notif.error }}
      </div>

      <div v-if="notif.loading" class="text-xs !text-gray-700 dark:!text-gray-300">
        Ładowanie…
      </div>

      <div v-if="!notif.loading && !notif.error && notif.items.length === 0" class="text-xs !text-gray-700 dark:!text-gray-300">
        Brak powiadomień.
      </div>

      <div v-if="!notif.loading && !notif.error && notif.items.length > 0" class="space-y-2">
        <article
            v-for="n in notif.items.slice(0, 10)"
            :key="notif.getId(n) ?? JSON.stringify(n)"
            class="rounded-lg border p-2
                 !bg-gray-50 !border-gray-200
                 hover:!bg-gray-100
                 dark:!bg-[#111826] dark:!border-[#273043] dark:hover:!bg-[#172033]"
            :class="notif.isRead(n) ? 'opacity-80' : ''"
        >
          <div class="flex items-start justify-between gap-2">
            <div class="min-w-0">
              <div class="text-xs font-semibold truncate">{{ getTitle(n) }}</div>
              <div class="text-[11px] !text-gray-600 dark:!text-gray-400 mt-0.5">
                {{ formatDate(getCreatedAt(n)) }}
              </div>
            </div>

            <div class="flex items-center gap-1 shrink-0">
              <button
                  v-if="!notif.isRead(n)"
                  type="button"
                  class="px-2 py-0.5 text-[11px] rounded-md border
                       !bg-white !text-gray-900 !border-gray-200 hover:!bg-gray-100
                       dark:!bg-[#0b0f14] dark:!text-gray-100 dark:!border-[#273043] dark:hover:!bg-[#111826]"
                  @click="markRead(n)"
                  title="Oznacz jako przeczytane"
              >
                OK
              </button>

              <button
                  type="button"
                  class="px-2 py-0.5 text-[11px] rounded-md border
                       !border-red-400 !bg-red-50 !text-red-700 hover:!bg-red-100
                       dark:!border-red-500/70 dark:!bg-red-500/15 dark:!text-red-200 dark:hover:!bg-red-500/25"
                  @click="removeOne(n)"
                  title="Usuń powiadomienie"
              >
                Usuń
              </button>
            </div>
          </div>

          <div v-if="getBody(n)" class="text-xs mt-1 whitespace-pre-wrap">
            {{ getBody(n) }}
          </div>
        </article>

        <RouterLink
            to="/notifications"
            class="block text-xs text-center px-3 py-2 rounded-md border
                 !bg-white !text-gray-900 !border-gray-200 hover:!bg-gray-100
                 dark:!bg-[#111826] dark:!text-gray-100 dark:!border-[#273043] dark:hover:!bg-[#172033]"
            @click="close"
        >
          Zobacz wszystkie
        </RouterLink>
      </div>
    </div>
  </div>
</template>
