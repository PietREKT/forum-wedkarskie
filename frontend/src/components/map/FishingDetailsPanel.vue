<script setup>
import { computed, ref } from 'vue'
import NewFishingSpotForm from './NewFishingSpotForm.vue'

const props = defineProps({
  spot: {
    type: Object,
    default: null,
  },
  // nowe: dane z backendu
  opinions: {
    type: Array,
    default: () => [],
  },
  opinionsLoading: {
    type: Boolean,
    default: false,
  },
  averageRating: {
    type: Number,
    default: null,
  },
  events: {
    type: Array,
    default: () => [],
  },
  eventsLoading: {
    type: Boolean,
    default: false,
  },
  ownerInfo: {
    type: Object,
    default: () => ({ is_owner: false }),
  },
})

const emit = defineEmits([
  'rate-spot',     // { spotId, rating }
  'delete-spot',   // bez payloadu
])

// lokalna pamięć oceny – żeby od razu wizualnie podbić gwiazdki
const userRatings = ref({})

const currentRating = computed(() => {
  if (!props.spot) return 0
  return userRatings.value[props.spot.id] || 0
})

// średnia ocena: najpierw z props.averageRating (z backendu),
// jeśli brak – fallback do spot.avgRating (stary front)
const displayAverageRating = computed(() => {
  if (props.averageRating != null) return props.averageRating
  if (!props.spot || props.spot.avgRating == null) return null
  return props.spot.avgRating
})

function setRating(star) {
  if (!props.spot) return
  userRatings.value[props.spot.id] = star
  emit('rate-spot', {
    spotId: props.spot.id,
    rating: star,
  })
}

// trasa – Google Maps
const userAddress = ref('')

function showRoute() {
  if (!props.spot) return
  // nowe: obsługa pól z backendu (locationY/locationX) + fallback do lat/lng
  const lat = props.spot.lat ?? props.spot.locationY
  const lng = props.spot.lng ?? props.spot.locationX
  if (lat == null || lng == null) return

  const origin = encodeURIComponent(userAddress.value || '')
  const destination = `${lat},${lng}`
  const url = `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(
      destination,
  )}${origin ? `&origin=${origin}` : ''}`

  window.open(url, '_blank')
}

// formularz zgłoszenia
const showNewSpotForm = ref(false)

// posortowane opinie (najnowsze na górze)
const sortedOpinions = computed(() => {
  const list = props.opinions || []
  return [...list].sort(
      (a, b) =>
          new Date(b.createdAt || 0).getTime() -
          new Date(a.createdAt || 0).getTime(),
  )
})

const isOwner = computed(() => !!props.ownerInfo?.is_owner)
</script>

<template>
  <section
      class="bg-black/65 backdrop-blur p-4 flex flex-col gap-4 overflow-y-auto min-h-0"
  >
    <!-- GŁÓWNE INFO ŁOWISKA -->
    <header class="flex items-start justify-between gap-4" v-if="spot">
      <div>
        <h2 class="text-lg font-semibold">
          {{ spot.name }}
        </h2>
        <p class="text-xs opacity-90 mt-1">
          {{ spot.type || '—' }} •
          {{ spot.voivodeship || '—' }} •
          {{ spot.ownerType || '—' }}
        </p>
        <p class="text-xs opacity-90 mt-1">
          Ocena łowiska:
          <span v-if="displayAverageRating != null">
            {{ displayAverageRating.toFixed(1) }} / 5
            <span class="opacity-75">
              ({{ spot.ratingCount || 0 }} głosów)
            </span>
          </span>
          <span v-else>
            brak oceny
          </span>
        </p>
      </div>

      <div class="flex flex-col gap-1 items-end">
        <button
            class="text-xs border border-white/60 rounded-full px-3 py-1 hover:bg-white/10"
        >
          ☆ Ulubione
        </button>
        <button
            v-if="isOwner"
            type="button"
            class="text-[10px] border border-red-500 text-red-500 rounded-full px-3 py-0.5 hover:bg-red-600 hover:text-white"
            @click="emit('delete-spot')"
        >
          Usuń łowisko
        </button>
      </div>
    </header>

    <div v-else class="text-xs opacity-90">
      Wybierz łowisko z listy, aby zobaczyć szczegóły.
    </div>

    <!-- ZDJĘCIA -->
    <div
        v-if="spot"
        class="aspect-video max-h-[40vh] w-full rounded-xl bg-black/50 border border-white/40 grid place-items-center text-xs opacity-80"
    >
      Tu będą zdjęcia łowiska
    </div>

    <!-- DANE SZCZEGÓŁOWE -->
    <div v-if="spot" class="text-xs grid grid-cols-1 sm:grid-cols-2 gap-3">
      <div>
        <h3 class="font-semibold mb-1">Gatunki ryb</h3>
        <p class="opacity-90">
          {{
            (spot.fish || [])
                .map(f => (typeof f === 'string' ? f : f.name))
                .join(', ') || 'Brak danych'
          }}
        </p>
      </div>

      <div>
        <h3 class="font-semibold mb-1">Powierzchnia</h3>
        <p class="opacity-90">
          {{
            spot.surfaceHa != null ? `${spot.surfaceHa} ha` : 'Brak danych'
          }}
        </p>
      </div>

      <div>
        <h3 class="font-semibold mb-1">Przystań</h3>
        <p class="opacity-90">
          {{ spot.hasPier ? 'Tak' : 'Nie' }}
        </p>
      </div>

      <div>
        <h3 class="font-semibold mb-1">Możliwość wodowania łódki</h3>
        <p class="opacity-90">
          {{ spot.boatAccess ? 'Tak' : 'Nie' }}
        </p>
      </div>

      <div class="sm:col-span-2">
        <h3 class="font-semibold mb-1">Regulamin / opis</h3>
        <p class="opacity-90 leading-relaxed">
          {{ spot.description || 'Brak opisu.' }}
        </p>
      </div>
    </div>

    <!-- NOWE: OPINIE Z BACKENDU -->
    <div v-if="spot" class="text-xs">
      <h3 class="font-semibold mb-1">Opinie innych wędkarzy</h3>

      <div v-if="opinionsLoading" class="opacity-80">
        Ładuję opinie...
      </div>

      <div v-else-if="!sortedOpinions.length" class="opacity-80">
        Brak opinii dla tego łowiska.
      </div>

      <ul
          v-else
          class="space-y-1 max-h-32 overflow-auto pr-1"
      >
        <li
            v-for="op in sortedOpinions"
            :key="op.id"
            class="border border-white/40 rounded px-2 py-1"
        >
          <div class="flex items-center justify-between gap-2">
            <div>
              <span class="font-semibold">
                {{ op.author?.username || 'Użytkownik' }}
              </span>
              <span class="opacity-60 text-[10px] ml-1">
                {{ op.createdAt ? new Date(op.createdAt).toLocaleDateString() : '' }}
              </span>
            </div>
            <span class="font-semibold text-xs">
              {{ op.rating }}/5
            </span>
          </div>
          <p v-if="op.comment" class="mt-1 leading-snug">
            {{ op.comment }}
          </p>
        </li>
      </ul>
    </div>

    <!-- NOWE: WYDARZENIA NA ŁOWISKU -->
    <div v-if="spot" class="text-xs">
      <h3 class="font-semibold mb-1">Wydarzenia na tym łowisku</h3>

      <div v-if="eventsLoading" class="opacity-80">
        Ładuję wydarzenia...
      </div>

      <div v-else-if="!events || !events.length" class="opacity-80">
        Brak przypisanych wydarzeń.
      </div>

      <ul v-else class="space-y-1 max-h-24 overflow-auto pr-1">
        <li
            v-for="ev in events"
            :key="ev.id"
            class="flex items-center justify-between gap-2"
        >
          <span class="font-medium">
            {{ ev.name }}
          </span>
          <span class="opacity-70">
            {{ ev.startsAt ? new Date(ev.startsAt).toLocaleDateString() : '' }}
          </span>
        </li>
      </ul>
    </div>

    <!-- TWOJA OCENA -->
    <div v-if="spot" class="text-xs">
      <h3 class="font-semibold mb-1">Twoja ocena łowiska</h3>
      <div class="flex items-center gap-1">
        <button
            v-for="star in 5"
            :key="star"
            type="button"
            class="w-6 h-6 text-sm border border-white/60 rounded-full grid place-items-center hover:bg-white/20"
            :class="currentRating >= star ? 'bg-white/40' : 'bg-black/30'"
            @click="setRating(star)"
        >
          {{ star }}
        </button>
        <span class="ml-2 opacity-90">
          {{
            currentRating
                ? `Oceniłeś na ${currentRating}/5`
                : 'Jeszcze nie oceniono'
          }}
        </span>
      </div>
    </div>

    <!-- TRASA -->
    <div v-if="spot" class="text-xs">
      <h3 class="font-semibold mb-1">Trasa do łowiska</h3>
      <div class="flex flex-col sm:flex-row gap-2 items-stretch sm:items-center">
        <input
            v-model="userAddress"
            type="text"
            placeholder="Twój adres (opcjonalnie)"
            class="flex-1 bg-white/15 text-white placeholder:text-white/80 border border-white/60 rounded px-2 py-1 text-xs outline-none"
        />
        <button
            type="button"
            class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs"
            @click="showRoute"
        >
          Pokaż trasę
        </button>
      </div>
    </div>

    <!-- PRZYCISK FORMULARZA -->
    <div class="mt-2">
      <button
          type="button"
          class="px-3 py-1 rounded-full border border-white/60 hover:bg-white/10 text-xs"
          @click="showNewSpotForm = !showNewSpotForm"
      >
        {{ showNewSpotForm ? 'Ukryj formularz zgłoszenia' : 'Zgłoś nowe łowisko' }}
      </button>
    </div>

    <!-- FORMULARZ W OSOBNYM KOMPONENCIE -->
    <NewFishingSpotForm
        v-if="showNewSpotForm"
        class="mt-3 border-t border-white/40 pt-3"
    />
  </section>
</template>
