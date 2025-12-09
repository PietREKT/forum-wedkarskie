<template>
  <div class="max-w-6xl mx-auto py-6">

    <h1 class="text-2xl font-semibold mb-4">Panel administratora</h1>

    <!-- ───── ZGŁOSZENIA ───── -->
    <section class="border rounded-md p-4 mb-8">
      <h2 class="font-semibold text-lg mb-3">Zgłoszenia postów</h2>

      <button
          @click="loadReports"
          class="px-3 py-1 rounded bg-blue-600 text-white mb-3"
      >
        Odśwież
      </button>

      <div v-if="reports.length === 0" class="text-gray-400 text-sm">
        Brak zgłoszeń
      </div>

      <div v-for="r in reports" :key="r.id"
           class="border rounded p-3 mb-3 bg-black/20">

        <div class="flex justify-between mb-2">
          <div>
            <p class="font-semibold">Post ID: {{ r.postId }}</p>
            <p class="text-gray-400 text-sm">Powód: {{ r.reason }}</p>
            <p class="text-gray-500 text-xs">Zgłoszono: {{ r.createdAt }}</p>
          </div>

          <RouterLink
              :to="`/posts/${r.postId}`"
              class="text-blue-400 hover:underline"
          >
            Otwórz post →
          </RouterLink>
        </div>

        <div class="flex gap-2 mt-2">
          <button
              @click="resolve(r.id)"
              class="px-2 py-1 rounded bg-green-600 text-white"
          >
            Oznacz rozwiązane
          </button>

          <button
              @click="remove(r.id)"
              class="px-2 py-1 rounded bg-yellow-600 text-white"
          >
            Usuń zgłoszenie
          </button>

          <button
              @click="removePost(r.postId)"
              class="px-2 py-1 rounded bg-red-700 text-white"
          >
            Usuń post
          </button>
        </div>
      </div>
    </section>

    <!-- ───── UŻYTKOWNICY ───── -->
    <section class="border rounded-md p-4">
      <h2 class="font-semibold text-lg mb-3">Użytkownicy</h2>

      <button
          @click="loadUsers"
          class="px-3 py-1 rounded bg-blue-600 text-white mb-3"
      >
        Odśwież
      </button>

      <table class="w-full text-sm">
        <tr class="border-b border-gray-700 text-gray-300">
          <th class="text-left py-2">Nick</th>
          <th class="text-left py-2">Mail</th>
          <th class="py-2 text-center">Ban</th>
        </tr>

        <tr v-for="u in users" :key="u.id" class="border-b border-gray-800">
          <td class="py-2">{{ u.username }}</td>
          <td class="py-2">{{ u.email }}</td>
          <td class="py-2 text-center">
            <button
                v-if="!u.banned"
                @click="ban(u.id)"
                class="px-2 py-1 bg-red-700 text-white rounded"
            >
              Banuj
            </button>

            <button
                v-else
                @click="unban(u.id)"
                class="px-2 py-1 bg-green-700 text-white rounded"
            >
              Odbanuj
            </button>
          </td>
        </tr>
      </table>
    </section>

  </div>
</template>

<script setup>
import { useAdminStore } from '../stores/admin'
import { onMounted } from 'vue'

const admin = useAdminStore()

const reports = admin.reports
const users = admin.users

const loadReports = admin.loadReports
const resolve = admin.resolveReport
const remove = admin.deleteReport
const removePost = admin.deletePost

const loadUsers = admin.loadUsers
const ban = admin.banUser
const unban = admin.unbanUser

onMounted(() => {
  loadReports()
  loadUsers()
})
</script>
