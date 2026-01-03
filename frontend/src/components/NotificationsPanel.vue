<script setup>
import { onMounted } from 'vue'
import { useNotificationsStore } from '../stores/notifications.js'
import { useUserStore } from '../stores/userStore.js'
import { acceptFriendRequest, rejectFriendRequest } from '../utils/usersApi.js'

const notif = useNotificationsStore()
const userStore = useUserStore()

function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  if (Number.isNaN(date.getTime())) return ''
  const pad = n => String(n).padStart(2, '0')
  return `${pad(date.getDate())}.${pad(date.getMonth() + 1)}.${date.getFullYear()} ${pad(date.getHours())}:${pad(
      date.getMinutes(),
  )}`
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

function getType(n) {
  return String(n?.type || n?.notificationType || n?.notification_type || '').toUpperCase()
}

function getResourceId(n) {
  return n?.resourceId ?? n?.resource_id ?? n?.reqId ?? n?.requestId ?? null
}

function isFriendInvitation(n) {
  return getType(n) === 'FRIEND_INVITATION'
}

async function onAcceptFriend(n) {
  const reqId = getResourceId(n)
  if (!reqId) return
  try {
    await acceptFriendRequest(reqId)
    await Promise.all([notif.fetchMy(), userStore.fetchMe(true)])
  } catch (e) {
    console.error('accept friend error', e)
  }
}

async function onRejectFriend(n) {
  const reqId = getResourceId(n)
  if (!reqId) return
  try {
    await rejectFriendRequest(reqId)
    await Promise.all([notif.fetchMy(), userStore.fetchMe(true)])
  } catch (e) {
    console.error('reject friend error', e)
  }
}

async function onMarkRead(n) {
  const id = notif.getId(n)
  if (!id) return
  try {
    await notif.markRead(id)
  } catch (e) {
    console.error('markRead error', e)
  }
}

async function onRemove(n) {
  const id = notif.getId(n)
  if (!id) return
  try {
    await notif.remove(id)
  } catch (e) {
    console.error('remove notif error', e)
  }
}

onMounted(() => {
  notif.fetchMy()
})
</script>

<template>
  <section class="space-y-3">
    <header class="flex items-center justify-between gap-3">
      <div>
        <div class="text-base font-semibold">Powiadomienia</div>
        <div class="text-xs opacity-70">
          Nieprzeczytane: <span class="font-medium">{{ notif.unreadCount }}</span>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <button
            type="button"
            class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5 disabled:opacity-50"
            :disabled="notif.loading"
            @click="notif.fetchMy"
        >
          Odśwież
        </button>

        <button
            type="button"
            class="px-3 py-1.5 text-sm rounded-md border bg-[var(--color-bg)] hover:bg-black/5 disabled:opacity-50"
            :disabled="notif.loading || notif.unreadCount === 0"
            @click="notif.markAllRead"
        >
          Oznacz wszystkie
        </button>
      </div>
    </header>

    <div v-if="notif.error" class="rounded-md border border-red-500/60 bg-red-500/10 px-4 py-3 text-sm">
      {{ notif.error }}
    </div>

    <div v-if="notif.loading" class="text-sm opacity-80">
      Ładowanie powiadomień…
    </div>

    <div v-if="!notif.loading && !notif.error && notif.items.length === 0" class="text-sm opacity-80">
      Brak powiadomień.
    </div>

    <div v-if="!notif.loading && !notif.error && notif.items.length > 0" class="space-y-2">
      <article
          v-for="n in notif.items"
          :key="notif.getId(n) ?? JSON.stringify(n)"
          class="border border-gray-300 rounded-2xl p-3 bg-[var(--color-bg-elevated)] shadow-sm"
          :class="notif.isRead(n) ? 'opacity-80' : ''"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <div class="text-sm font-semibold truncate">
              {{ getTitle(n) }}
            </div>
            <div class="text-xs opacity-70 mt-0.5">
              {{ formatDate(getCreatedAt(n)) }}
            </div>
          </div>

          <div class="flex items-center gap-2 shrink-0">
            <button
                v-if="!notif.isRead(n)"
                type="button"
                class="px-2.5 py-1 text-xs rounded-md border bg-[var(--color-bg)] hover:bg-black/5"
                @click="onMarkRead(n)"
            >
              Przeczytane
            </button>

            <button
                type="button"
                class="px-2.5 py-1 text-xs rounded-md border border-red-500/70 bg-red-500/20 hover:bg-red-500/30"
                @click="onRemove(n)"
            >
              Usuń
            </button>
          </div>
        </div>

        <div
            v-if="isFriendInvitation(n)"
            class="flex items-center gap-2 mt-3"
        >
          <button
              type="button"
              class="px-2.5 py-1 text-xs rounded-md border bg-[var(--color-bg)] hover:bg-black/5 disabled:opacity-50"
              :disabled="notif.loading || !getResourceId(n)"
              @click="onAcceptFriend(n)"
          >
            Akceptuj
          </button>
          <button
              type="button"
              class="px-2.5 py-1 text-xs rounded-md border border-red-500/70 bg-red-500/20 hover:bg-red-500/30 disabled:opacity-50"
              :disabled="notif.loading || !getResourceId(n)"
              @click="onRejectFriend(n)"
          >
            Odrzuć
          </button>

          <span v-if="!getResourceId(n)" class="text-xs opacity-70">
            (Brak ID zaproszenia z backendu)
          </span>
        </div>

        <div v-if="getBody(n)" class="text-sm whitespace-pre-wrap leading-relaxed mt-2">
          {{ getBody(n) }}
        </div>

        <div v-else class="text-xs opacity-60 mt-2">
          (Brak treści powiadomienia)
        </div>
      </article>
    </div>
  </section>
</template>
