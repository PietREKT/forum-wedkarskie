<!-- src/components/map/FishingDetailsPanel.vue -->
<script setup>
import { computed, ref, watch } from 'vue'
import NewFishingSpotForm from './NewFishingSpotForm.vue'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()

const props = defineProps({
  spot: { type: Object, default: null },
  opinions: { type: Array, default: () => [] },
  opinionsLoading: { type: Boolean, default: false },
  events: { type: Array, default: () => [] },
  eventsLoading: { type: Boolean, default: false },
  ownerInfo: { type: Object, default: () => ({ is_owner: false }) },

  isFavourite: { type: Boolean, default: false },
  favouritesLoading: { type: Boolean, default: false },

  showModeration: { type: Boolean, default: false },
  isPending: { type: Boolean, default: false },
  moderationBusy: { type: Boolean, default: false },

  isLoggedIn: { type: Boolean, default: false },
})

const emit = defineEmits(['rate-spot', 'delete-spot', 'toggle-favourite', 'approve-spot', 'reject-spot'])

const isOwnerOrAdmin = computed(() => !!props.ownerInfo?.is_owner || !!auth.isAdmin)

const confirmDeleteOpen = ref(false)
function requestDelete() {
  confirmDeleteOpen.value = true
}
function cancelDelete() {
  confirmDeleteOpen.value = false
}
function confirmDelete() {
  confirmDeleteOpen.value = false
  emit('delete-spot')
}

const showNewSpotForm = ref(false)

const sortedOpinions = computed(() => {
  const list = props.opinions || []
  return [...list].sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
})

const myOpinion = computed(() => {
  const uid = auth.user?.id
  if (!uid) return null
  return (props.opinions || []).find((o) => o?.author?.id === uid) || null
})

const myOpinionId = computed(() => myOpinion.value?.id ?? null)

const currentRating = computed(() => {
  const r = myOpinion.value?.rating
  return typeof r === 'number' ? r : 0
})

const displayAverageRating = computed(() => {
  if (!props.spot || props.spot.avgRating == null) return null
  return Number(props.spot.avgRating)
})

// UI do publikacji
const draftRating = ref(0)
const draftComment = ref('')

watch(
    () => props.spot?.id,
    () => {
      draftRating.value = currentRating.value || 0
      draftComment.value = myOpinion.value?.comment || ''
    },
    { immediate: true },
)

watch(
    () => myOpinion.value?.id,
    () => {
      draftRating.value = currentRating.value || 0
      draftComment.value = myOpinion.value?.comment || ''
    },
)

function pickStar(star) {
  draftRating.value = star
}

function publishOpinion() {
  if (!props.spot?.id) return
  emit('rate-spot', {
    spotId: props.spot.id,
    rating: draftRating.value,
    opinionId: myOpinionId.value,
    comment: draftComment.value?.trim() ? draftComment.value.trim() : null,
  })
}

const spotPhotoUrl = computed(() => {
  const u = props.spot?.photoUrl
  if (!u) return null
  return String(u)
})

const spotStatuteUrl = computed(() => {
  const u = props.spot?.statuteUrl
  if (!u) return null
  return String(u)
})

const addOrReportLabel = computed(() => (auth.isAdmin ? 'Dodaj łowisko' : 'Zgłoś łowisko'))
const toggleFormLabel = computed(() => (showNewSpotForm.value ? 'Ukryj formularz' : addOrReportLabel.value))
</script>

<template>
  <section class="bg-black/80 text-white backdrop-blur-lg p-4 md:p-5 flex flex-col gap-4 overflow-y-auto min-h-0 border-l border-white/40">
    <header class="flex items-start justify-between gap-4" v-if="spot">
      <div>
        <h2 class="text-lg font-semibold">{{ spot.name }}</h2>

        <p class="text-xs opacity-90 mt-1">{{ spot.type || '—' }}</p>

        <p class="text-xs opacity-90 mt-1">
          Ocena łowiska:
          <span v-if="displayAverageRating != null">{{ displayAverageRating.toFixed(1) }} / 5</span>
          <span v-else>brak oceny</span>
        </p>
      </div>

      <div class="flex flex-col gap-1 items-end">
        <button
            type="button"
            class="text-xs border border-white/60 rounded-full px-3 py-1 hover:bg-white/10 disabled:opacity-60"
            :disabled="!isLoggedIn || favouritesLoading"
            @click="spot && emit('toggle-favourite', spot.id)"
        >
          <span v-if="isFavourite">★ Ulubione</span>
          <span v-else>☆ Ulubione</span>
        </button>

        <button
            v-if="isOwnerOrAdmin"
            type="button"
            class="text-[10px] border border-red-500 text-red-400 rounded-full px-3 py-0.5 hover:bg-red-600 hover:text-white"
            @click="requestDelete"
        >
          Usuń łowisko
        </button>
      </div>
    </header>

    <div v-else class="text-xs opacity-90">Wybierz łowisko z listy, aby zobaczyć szczegóły.</div>

    <div v-if="spot && confirmDeleteOpen" class="text-xs border border-red-400/60 rounded-lg p-3 bg-red-600/10">
      <p class="font-semibold mb-2">Czy na pewno chcesz usunąć to łowisko?</p>
      <div class="flex gap-2">
        <button
            type="button"
            class="px-3 py-1 rounded-full border border-red-400/80 text-red-200 hover:bg-red-600/40 text-xs"
            @click="confirmDelete"
        >
          Usuń
        </button>
        <button
            type="button"
            class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs"
            @click="cancelDelete"
        >
          Anuluj
        </button>
      </div>
    </div>

    <div v-if="spot && showModeration && isPending" class="text-xs border border-yellow-300/60 rounded-lg p-3 bg-yellow-500/10">
      <h3 class="font-semibold mb-2">Moderacja zgłoszenia</h3>
      <div class="flex gap-2">
        <button
            type="button"
            class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs disabled:opacity-60"
            :disabled="moderationBusy"
            @click="emit('approve-spot')"
        >
          Akceptuj
        </button>
        <button
            type="button"
            class="px-3 py-1 rounded-full border border-red-400/80 text-red-200 hover:bg-red-600/40 text-xs disabled:opacity-60"
            :disabled="moderationBusy"
            @click="emit('reject-spot')"
        >
          Odrzuć
        </button>
      </div>
    </div>

    <div v-if="spot && (spotPhotoUrl || spotStatuteUrl)" class="text-xs">
      <h3 class="font-semibold mb-2">Materiały</h3>

      <div v-if="spotPhotoUrl" class="mb-2">
        <img :src="spotPhotoUrl" alt="Zdjęcie łowiska" class="w-full max-h-40 object-cover rounded border border-white/40" />
      </div>

      <div v-if="spotStatuteUrl" class="opacity-90">
        <a :href="spotStatuteUrl" target="_blank" rel="noreferrer" class="underline hover:opacity-80">
          Otwórz regulamin (plik)
        </a>
      </div>
    </div>

    <div v-if="spot" class="text-xs">
      <h3 class="font-semibold mb-1">Gatunki ryb</h3>
      <p class="opacity-90">
        {{ (spot.fish || []).map((f) => (typeof f === 'string' ? f : f.name)).join(', ') || 'Brak danych' }}
      </p>
    </div>

    <div v-if="spot" class="text-xs">
      <h3 class="font-semibold mb-1">Regulamin / opis</h3>
      <p class="opacity-90 leading-relaxed">{{ spot.description || 'Brak opisu.' }}</p>
    </div>

    <div v-if="spot" class="text-xs">
      <h3 class="font-semibold mb-1">Opinie innych wędkarzy</h3>

      <div v-if="opinionsLoading" class="opacity-80">Ładuję opinie...</div>
      <div v-else-if="!sortedOpinions.length" class="opacity-80">Brak opinii dla tego łowiska.</div>

      <ul v-else class="space-y-1 max-h-32 overflow-auto pr-1">
        <li v-for="op in sortedOpinions" :key="op.id" class="border border-white/40 rounded px-2 py-1 bg-black/40">
          <div class="flex items-center justify-between gap-2">
            <div>
              <span class="font-semibold">{{ op.author?.username || 'Użytkownik' }}</span>
              <span class="opacity-60 text-[10px] ml-1">
                {{ op.createdAt ? new Date(op.createdAt).toLocaleDateString() : '' }}
              </span>
            </div>
            <span class="font-semibold text-xs">{{ op.rating }}/5</span>
          </div>
          <p v-if="op.comment" class="mt-1 leading-snug">{{ op.comment }}</p>
        </li>
      </ul>
    </div>

    <div v-if="spot" class="text-xs">
      <h3 class="font-semibold mb-1">Wydarzenia na tym łowisku</h3>

      <div v-if="eventsLoading" class="opacity-80">Ładuję wydarzenia...</div>
      <div v-else-if="!events || !events.length" class="opacity-80">Brak przypisanych wydarzeń.</div>

      <ul v-else class="space-y-1 max-h-24 overflow-auto pr-1">
        <li v-for="ev in events" :key="ev.id" class="flex items-center justify-between gap-2">
          <span class="font-medium">{{ ev.name }}</span>
          <span class="opacity-70">{{ ev.startsAt ? new Date(ev.startsAt).toLocaleDateString() : '' }}</span>
        </li>
      </ul>
    </div>

    <div v-if="spot" class="text-xs">
      <h3 class="font-semibold mb-1">Twoja opinia</h3>

      <div v-if="!isLoggedIn" class="opacity-80">Zaloguj się, aby dodać opinię.</div>

      <div v-else class="space-y-2">
        <div class="flex items-center gap-1">
          <button
              v-for="star in 5"
              :key="star"
              type="button"
              class="w-7 h-7 text-sm border border-white/60 rounded-full grid place-items-center hover:bg-white/20"
              :class="draftRating >= star ? 'bg-white/40 text-black' : 'bg-black/40'"
              @click="pickStar(star)"
          >
            {{ star }}
          </button>

          <span class="ml-2 opacity-90">
            {{ draftRating ? `Wybrano ${draftRating}/5` : 'Wybierz ocenę' }}
          </span>
        </div>

        <textarea
            v-model="draftComment"
            rows="3"
            placeholder="Komentarz (opcjonalnie)"
            class="w-full bg-white/10 text-white placeholder:text-white/70 border border-white/60 rounded px-2 py-1 text-xs outline-none resize-none"
        />

        <div class="flex items-center gap-2">
          <button
              type="button"
              class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs disabled:opacity-60"
              :disabled="!draftRating"
              @click="publishOpinion"
          >
            Opublikuj
          </button>

          <span class="opacity-80" v-if="myOpinionId">Edytujesz swoją opinię.</span>
        </div>
      </div>
    </div>

    <div class="mt-2" v-if="isLoggedIn">
      <button
          type="button"
          class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs"
          @click="showNewSpotForm = !showNewSpotForm"
      >
        {{ toggleFormLabel }}
      </button>
    </div>

    <NewFishingSpotForm v-if="showNewSpotForm && isLoggedIn" class="mt-3 border-t border-white/40 pt-3" />
  </section>
</template>
