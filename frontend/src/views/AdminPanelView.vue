<template>
  <div class="max-w-6xl mx-auto py-6">
    <div class="flex items-center justify-between gap-3 mb-4">
      <h1 class="text-2xl font-semibold text-slate-900 dark:text-slate-100">Panel administracyjny</h1>

      <button
          class="px-3 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50
               dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800
               disabled:opacity-60"
          :disabled="admin.loading"
          @click="refreshAll"
      >
        {{ admin.loading ? 'Odświeżam...' : 'Odśwież wszystko' }}
      </button>
    </div>

    <div v-if="admin.actionMessage" class="text-emerald-700 dark:text-emerald-300 text-sm mb-2">
      {{ admin.actionMessage }}
    </div>
    <div v-if="admin.actionError" class="text-red-700 dark:text-red-300 text-sm mb-2">
      {{ admin.actionError }}
    </div>
    <div v-if="admin.error" class="text-red-700 dark:text-red-300 text-sm mb-4">
      Wystąpił błąd pobierania danych.
    </div>

    <div class="flex flex-wrap gap-2 mb-4">
      <button
          class="px-3 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50
               dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
          :class="tab === 'reports' ? 'ring-2 ring-slate-300 dark:ring-slate-700' : ''"
          @click="tab = 'reports'"
      >
        Zgłoszenia
      </button>
      <button
          class="px-3 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50
               dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
          :class="tab === 'banned' ? 'ring-2 ring-slate-300 dark:ring-slate-700' : ''"
          @click="tab = 'banned'"
      >
        Zbanowani ({{ admin.bannedUsers.length }})
      </button>
      <button
          class="px-3 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50
               dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
          :class="tab === 'muted' ? 'ring-2 ring-slate-300 dark:ring-slate-700' : ''"
          @click="tab = 'muted'"
      >
        Wyciszeni ({{ admin.mutedUsers.length }})
      </button>
    </div>

    <!-- ZGŁOSZENIA -->
    <section
        v-if="tab === 'reports'"
        class="border border-slate-200 rounded-md p-4 bg-white text-slate-900
             dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
    >
      <div class="flex items-center justify-between gap-3 mb-3">
        <h2 class="font-semibold text-lg">Zgłoszone treści (posty i komentarze)</h2>

        <button
            class="px-3 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50
                 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                 disabled:opacity-60"
            :disabled="admin.loading"
            @click="refreshReports"
        >
          {{ admin.loading ? 'Odświeżam...' : 'Odśwież' }}
        </button>
      </div>

      <div v-if="admin.reportedContent.length === 0 && !admin.loading" class="text-slate-600 dark:text-slate-400 text-sm">
        Brak zgłoszeń.
      </div>

      <div
          v-for="item in admin.reportedContent"
          :key="item.content?.id"
          class="border border-slate-200 rounded p-3 mb-3 bg-slate-50
               dark:border-slate-700 dark:bg-slate-950/40"
      >
        <div class="min-w-0 w-full">
          <div class="flex flex-wrap items-center gap-2">
            <p class="font-semibold">ID: {{ item.content?.id }}</p>

            <span
                class="text-xs px-2 py-0.5 rounded border border-slate-300 text-slate-700
                     dark:border-slate-600 dark:text-slate-200"
            >
              {{ item.content?.contentType }}
            </span>

            <span class="text-xs text-slate-600 dark:text-slate-400">
              Zgłoszeń: {{ item.reportCount }}
            </span>
          </div>

          <p class="text-slate-700 dark:text-slate-300 text-sm mt-1">
            Autor: {{ item.content?.author?.username || 'brak' }}
            <span v-if="item.content?.author?.id" class="text-slate-500">
              (ID: {{ item.content.author.id }})
            </span>
          </p>

          <p class="text-slate-500 text-xs mt-1">
            Utworzono: {{ formatDate(item.content?.createdAt) }}
          </p>

          <p class="text-slate-900 dark:text-slate-100 text-sm mt-2 break-words">
            {{ item.content?.content }}
          </p>

          <div class="mt-3 flex flex-wrap items-center gap-2">
            <RouterLink
                v-if="item.content?.contentType === 'POST'"
                :to="`/posts/${item.content?.id}`"
                class="text-sky-700 hover:underline text-sm dark:text-sky-300"
            >
              Otwórz post
            </RouterLink>

            <button
                class="px-2 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 text-sm
                     dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                     disabled:opacity-60"
                :disabled="admin.loading"
                @click="openDetails(item.content?.id)"
            >
              Szczegóły zgłoszeń
            </button>

            <button
                class="px-2 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 text-sm
                     dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                     disabled:opacity-60"
                :disabled="admin.loading"
                @click="dismiss(item.content?.id)"
            >
              Odrzuć zgłoszenia
            </button>

            <button
                class="px-2 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 text-sm
                     dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                     disabled:opacity-60"
                :disabled="admin.loading"
                @click="deleteContent(item.content?.id, item.content?.contentType)"
            >
              Usuń treść
            </button>
          </div>

          <div
              v-if="admin.selectedContentId === item.content?.id && admin.selectedReport"
              class="mt-3 border-t border-slate-200 dark:border-slate-700 pt-3"
          >
            <p class="text-sm text-slate-700 dark:text-slate-300 mb-2">
              Podsumowanie: <span class="font-semibold">{{ admin.selectedReport.reportCount }}</span>
            </p>

            <div v-if="admin.selectedReport.reportReasonsCount" class="text-sm">
              <p class="text-slate-600 dark:text-slate-400 mb-1">Powody:</p>
              <ul class="ml-4 list-disc text-slate-700 dark:text-slate-300">
                <li v-for="(count, reason) in admin.selectedReport.reportReasonsCount" :key="reason">
                  {{ reason }} — {{ count }}
                </li>
              </ul>
            </div>

            <p v-else class="text-slate-600 dark:text-slate-400 text-sm">Brak danych o powodach.</p>
          </div>

          <div v-if="item.content?.author?.id" class="mt-4 border-t border-slate-200 dark:border-slate-700 pt-3">
            <p class="text-sm text-slate-700 dark:text-slate-300 mb-2">Kary dla autora:</p>

            <div class="flex flex-wrap gap-2 items-center">
              <input
                  v-model="ensurePunish(item.content.author.id).reason"
                  type="text"
                  class="px-2 py-1 rounded border border-slate-300 bg-white text-sm text-slate-900 w-[260px]
                       dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
                  placeholder="Powód (wymagany)"
              />

              <input
                  v-model.number="ensurePunish(item.content.author.id).minutes"
                  type="number"
                  min="1"
                  class="px-2 py-1 rounded border border-slate-300 bg-white text-sm text-slate-900 w-[140px]
                       dark:border-slate-600 dark:bg-slate-950 dark:text-slate-100"
                  placeholder="Minuty"
                  :disabled="ensurePunish(item.content.author.id).permanent"
              />

              <label class="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
                <input type="checkbox" v-model="ensurePunish(item.content.author.id).permanent" />
                Na stałe
              </label>

              <button
                  v-if="canMute"
                  class="px-2 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 text-sm
                       dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                       disabled:opacity-60"
                  :disabled="admin.loading || !canPunish(item.content.author.id)"
                  @click="mute(item.content.author.id)"
              >
                Wycisz
              </button>

              <button
                  v-if="canMute"
                  class="px-2 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 text-sm
                       dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                       disabled:opacity-60"
                  :disabled="admin.loading"
                  @click="unmute(item.content.author.id)"
              >
                Cofnij wyciszenie
              </button>

              <button
                  v-if="canBan"
                  class="px-2 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 text-sm
                       dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                       disabled:opacity-60"
                  :disabled="admin.loading || !canPunish(item.content.author.id)"
                  @click="ban(item.content.author.id)"
              >
                Zbanuj
              </button>

              <button
                  v-if="canBan"
                  class="px-2 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 text-sm
                       dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                       disabled:opacity-60"
                  :disabled="admin.loading"
                  @click="unban(item.content.author.id)"
              >
                Cofnij bana
              </button>
            </div>

            <p v-if="!hasReason(item.content.author.id)" class="text-xs text-slate-500 mt-2">
              Powód jest wymagany.
            </p>
            <p v-else-if="!hasUntil(item.content.author.id)" class="text-xs text-slate-500 mt-2">
              Podaj minuty albo zaznacz „Na stałe”.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- ZBANOWANI -->
    <section
        v-if="tab === 'banned'"
        class="border border-slate-200 rounded-md p-4 bg-white text-slate-900
             dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
    >
      <div class="flex items-center justify-between gap-3 mb-3">
        <h2 class="font-semibold text-lg">Zbanowani użytkownicy</h2>
        <button
            class="px-3 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50
                 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                 disabled:opacity-60"
            :disabled="admin.loading"
            @click="admin.loadBannedUsers()"
        >
          {{ admin.loading ? 'Odświeżam...' : 'Odśwież' }}
        </button>
      </div>

      <div v-if="admin.bannedUsers.length === 0 && !admin.loading" class="text-slate-600 dark:text-slate-400 text-sm">
        Brak zbanowanych.
      </div>

      <div
          v-for="u in admin.bannedUsers"
          :key="getUserId(u)"
          class="border border-slate-200 rounded p-3 mb-3 bg-slate-50
               dark:border-slate-700 dark:bg-slate-950/40"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <p class="font-semibold">
              {{ u.username || u.name || 'Użytkownik' }}
              <span class="text-slate-500 text-sm">ID: {{ getUserId(u) }}</span>
            </p>
            <p class="text-sm text-slate-700 dark:text-slate-300 mt-1" v-if="u.banUntil || u.punishedUntil">
              Do: {{ formatDate(u.banUntil || u.punishedUntil) }}
            </p>
          </div>

          <button
              v-if="canBan"
              class="px-2 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 text-sm
                   dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                   disabled:opacity-60"
              :disabled="admin.loading"
              @click="unban(getUserId(u))"
          >
            Cofnij bana
          </button>
        </div>
      </div>
    </section>

    <!-- WYCISZENI -->
    <section
        v-if="tab === 'muted'"
        class="border border-slate-200 rounded-md p-4 bg-white text-slate-900
             dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
    >
      <div class="flex items-center justify-between gap-3 mb-3">
        <h2 class="font-semibold text-lg">Wyciszeni użytkownicy</h2>
        <button
            class="px-3 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50
                 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                 disabled:opacity-60"
            :disabled="admin.loading"
            @click="admin.loadMutedUsers()"
        >
          {{ admin.loading ? 'Odświeżam...' : 'Odśwież' }}
        </button>
      </div>

      <div v-if="admin.mutedUsers.length === 0 && !admin.loading" class="text-slate-600 dark:text-slate-400 text-sm">
        Brak wyciszonych.
      </div>

      <div
          v-for="u in admin.mutedUsers"
          :key="getUserId(u)"
          class="border border-slate-200 rounded p-3 mb-3 bg-slate-50
               dark:border-slate-700 dark:bg-slate-950/40"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <p class="font-semibold">
              {{ u.username || u.name || 'Użytkownik' }}
              <span class="text-slate-500 text-sm">ID: {{ getUserId(u) }}</span>
            </p>
            <p class="text-sm text-slate-700 dark:text-slate-300 mt-1" v-if="u.mutedUntil || u.punishedUntil">
              Do: {{ formatDate(u.mutedUntil || u.punishedUntil) }}
            </p>
          </div>

          <button
              v-if="canMute"
              class="px-2 py-1 rounded border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 text-sm
                   dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700
                   disabled:opacity-60"
              :disabled="admin.loading"
              @click="unmute(getUserId(u))"
          >
            Cofnij wyciszenie
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useAdminStore } from '../stores/admin'
import { useAuthStore } from '../stores/auth'

const admin = useAdminStore()
const auth = useAuthStore()
const tab = ref('reports')

const roleStr = computed(() => {
  let role = auth.user?.role
  if (!role) return ''
  if (typeof role === 'object' && role.name) role = role.name
  return String(role).toUpperCase()
})

const canBan = computed(() => roleStr.value.includes('ADMIN') || roleStr.value.includes('ROOT'))
const canMute = computed(() => canBan.value || roleStr.value.includes('MOD'))

const punish = reactive({})

function ensurePunish(userId) {
  if (!punish[userId]) {
    punish[userId] = { reason: 'Naruszenie regulaminu', minutes: null, permanent: false }
  }
  return punish[userId]
}

function hasReason(userId) {
  return String(ensurePunish(userId).reason || '').trim().length > 0
}

function hasUntil(userId) {
  const p = ensurePunish(userId)
  return p.permanent || (Number.isFinite(Number(p.minutes)) && Number(p.minutes) > 0)
}

function canPunish(userId) {
  return hasReason(userId) && hasUntil(userId)
}

function refreshReports() {
  admin.loadReportedContentSummary({ page: 0, size: 30 })
}

function refreshAll() {
  admin.refreshAll()
}

function openDetails(contentId) {
  admin.loadContentReportDetails(contentId)
}

function dismiss(contentId) {
  admin.dismissContentReports(contentId)
}

function deleteContent(contentId, contentType) {
  admin.deleteContentByType(contentId, contentType)
}

function ban(userId) {
  const p = ensurePunish(userId)
  admin.banUser({ userId, reason: p.reason, minutes: p.minutes, permanent: p.permanent })
}

function unban(userId) {
  admin.unbanUser({ userId })
}

function mute(userId) {
  const p = ensurePunish(userId)
  admin.muteUser({ userId, reason: p.reason, minutes: p.minutes, permanent: p.permanent })
}

function unmute(userId) {
  admin.unmuteUser({ userId })
}

function getUserId(u) {
  return u?.id || u?.userId || u?.uuid || u?._id || ''
}

function formatDate(v) {
  if (!v) return ''
  try {
    return new Date(v).toLocaleString()
  } catch {
    return String(v)
  }
}

onMounted(() => {
  refreshAll()
})
</script>
