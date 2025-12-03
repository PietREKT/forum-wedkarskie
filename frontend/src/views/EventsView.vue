<template>
  <div class="max-w-7xl mx-auto py-6 px-4 space-y-6">
    <header>
      <h1 class="text-2xl md:text-3xl font-semibold">Wydarzenia</h1>
    </header>

    <div class="grid gap-4 lg:grid-cols-[2fr_3fr_2fr] md:grid-cols-2 grid-cols-1">

      <!-- LISTA WYDARZEŃ -->
      <section class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-4">
        <h2 class="text-lg font-semibold">Lista wydarzeń</h2>

        <ul class="space-y-2 text-sm">
          <li
              v-for="event in events"
              :key="event.id"
              @click="selectEvent(event.id)"
              class="rounded-lg px-3 py-2 border flex flex-col gap-0.5 cursor-pointer"
              :class="event.id === selectedEventId
              ? 'border-[var(--color-primary)] bg-[var(--color-surface)]'
              : 'theme-border bg-[var(--color-surface)] opacity-90 hover:opacity-100'"
          >
            <div class="flex items-center justify-between">
              <span class="font-medium">{{ event.name }}</span>
              <span class="text-[10px] text-[var(--color-muted)]">{{ event.dateLabel }}</span>
            </div>

            <p class="text-[11px] text-[var(--color-muted)]">
              Prywatna grupa · organizator: {{ event.organizer }}
            </p>
          </li>
        </ul>
      </section>

      <!-- FORMULARZ + SZCZEGÓŁY -->
      <section class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-6">
        <h2 class="text-lg font-semibold">Dodaj wydarzenie</h2>

        <!-- FORMULARZ -->
        <div class="space-y-3 text-sm">
          <div class="space-y-1">
            <label class="text-xs font-medium">Nazwa wydarzenia</label>
            <input
                type="text"
                placeholder="Np. Wyjazd na jezioro X"
                class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
            />
          </div>

          <!-- GRUPA + ŁOWISKO + DATA -->
          <div class="grid md:grid-cols-3 gap-3">
            <!-- GRUPA WYJAZDOWA -->
            <div class="space-y-1 md:col-span-1">
              <label class="text-xs font-medium">Grupa</label>
              <select
                  class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
              >
                <option value="">Wybierz grupę…</option>
                <option
                    v-for="group in groups"
                    :key="group.id"
                    :value="group.id"
                >
                  {{ group.name }} (admin: {{ group.admin }})
                </option>
              </select>
            </div>

            <!-- ŁOWISKO -->
            <div class="space-y-1 md:col-span-1">
              <label class="text-xs font-medium">Łowisko</label>
              <select
                  class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
              >
                <option>Wybierz łowisko…</option>
              </select>
            </div>

            <!-- DATA -->
            <div class="space-y-1 md:col-span-1">
              <label class="text-xs font-medium">Data i godzina</label>
              <input
                  type="datetime-local"
                  class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
              />
            </div>
          </div>

          <div class="space-y-1">
            <label class="text-xs font-medium">Opis</label>
            <textarea
                rows="3"
                placeholder="Krótki opis wydarzenia"
                class="w-full px-3 py-2 rounded-lg border theme-border bg-[var(--color-bg)]
                     text-xs resize-none"
            ></textarea>
          </div>

          <div class="flex items-center gap-3 text-xs">
            <span class="font-medium">Typ:</span>
            <button class="px-2 py-1 rounded-full border theme-border">Wyjazd</button>
            <button class="px-2 py-1 rounded-full border theme-border">Zawody</button>
          </div>

          <button
              class="px-4 py-2 rounded-lg text-xs font-medium bg-[var(--color-primary)]
                   text-white shadow"
          >
            Zapisz wydarzenie
          </button>
        </div>

        <hr class="border-[var(--color-border)]" />

        <!-- SZCZEGÓŁY WYBRANEGO WYDARZENIA -->
        <div class="space-y-4 text-sm" v-if="currentEvent">
          <h3 class="font-semibold">Szczegóły wybranego wydarzenia</h3>

          <div class="rounded-lg border theme-border bg-[var(--color-surface)] p-3 space-y-3">
            <div class="flex items-start justify-between">
              <div>
                <p class="text-sm font-medium">{{ currentEvent.name }}</p>
                <p class="text-[11px] text-[var(--color-muted)]">
                  Łowisko: {{ currentEvent.spotName }} · {{ currentEvent.dateLabel }}
                </p>
              </div>

              <span
                  class="text-[10px] px-2 py-0.5 rounded-full border theme-border text-[var(--color-muted)]"
              >
                Prywatna
              </span>
            </div>

            <p class="text-xs">{{ currentEvent.description }}</p>

            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="flex -space-x-2">
                  <div
                      v-for="p in currentEvent.participants"
                      :key="p.id"
                      class="h-7 w-7 rounded-full border theme-border bg-[var(--color-bg)]
                           grid place-items-center text-[10px] font-medium"
                  >
                    {{ p.initials }}
                  </div>
                </div>
                <span class="text-[11px] text-[var(--color-muted)]">
                  {{ currentEvent.participants.length }} uczestników
                </span>
              </div>

              <button class="px-3 py-1.5 rounded-lg text-xs font-medium border theme-border">
                Dołącz do wydarzenia
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- GRUPY -->
      <section class="space-y-4">
        <div class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-4">
          <h2 class="text-lg font-semibold">Grupy wyjazdowe</h2>

          <div class="space-y-2">
            <input
                type="text"
                placeholder="Szukaj grupy…"
                class="w-full px-3 py-1.5 rounded-lg border theme-border bg-[var(--color-bg)] text-xs"
            />
            <button class="w-full px-3 py-1.5 rounded-lg text-xs font-medium border theme-border">
              Utwórz nową grupę
            </button>
          </div>

          <ul class="space-y-2 text-sm">
            <li
                v-for="group in groups"
                :key="group.id"
                class="rounded-lg px-3 py-2 border theme-border bg-[var(--color-surface)]"
            >
              <div class="flex items-center justify-between">
                <span class="font-medium text-sm">{{ group.name }}</span>
                <span class="text-[10px] text-[var(--color-muted)]">Prywatna</span>
              </div>

              <p class="text-[11px] text-[var(--color-muted)]">
                Administrator: {{ group.admin }} · {{ group.members }} członków
              </p>

              <button class="mt-1 px-2 py-1 rounded-lg text-[11px] font-medium border theme-border">
                Wyślij prośbę o dołączenie
              </button>
            </li>
          </ul>
        </div>

        <!-- POWIADOMIENIA -->
        <div class="rounded-xl shadow-sm p-4 border theme-border theme-card theme-text space-y-3">
          <h3 class="text-sm font-semibold">Powiadomienia grup</h3>

          <div class="rounded-lg border theme-border p-3 text-xs">
            Zaproszenie do grupy „Ekipa jeziorowa”.
            <div class="flex gap-2 mt-2">
              <button class="px-2 py-1 rounded-lg border theme-border text-[11px]">Akceptuj</button>
              <button class="px-2 py-1 rounded-lg border theme-border text-[11px]">Odrzuć</button>
            </div>
          </div>
        </div>
      </section>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const events = ref([
  {
    id: 1,
    name: 'Wyjazd na jezioro Dębowiec',
    spotName: 'Jezioro Dębowiec',
    dateLabel: '15.12.2025, 05:00',
    organizer: 'Marek',
    description:
        'Spotkanie przy jeziorze od strony wschodniej około godziny 5:00. Łowimy szczupaka i sandacza.',
    participants: [
      { id: 'u1', initials: 'M' },
      { id: 'u2', initials: 'K' },
    ],
  },
  {
    id: 2,
    name: 'Nocne łowienie – Jezioro Sosnowiec',
    spotName: 'Jezioro Sosnowiec',
    dateLabel: '20.12.2025, 21:00',
    organizer: 'Piotr',
    description:
        'Zbiórka przy parkingu od strony południowej około 21:00. Nocne łowienie z brzegu.',
    participants: [
      { id: 'u3', initials: 'P' },
      { id: 'u4', initials: 'A' },
    ],
  },
])

const selectedEventId = ref(events.value[0]?.id ?? null)

const currentEvent = computed(() =>
    events.value.find(e => e.id === selectedEventId.value) || null
)

function selectEvent(id) {
  selectedEventId.value = id
}

const groups = ref([
  { id: 1, name: 'Ekipa jeziorowa', admin: 'Marek', members: 8 },
  { id: 2, name: 'Spinning na Wiśle', admin: 'Piotr', members: 5 },
])
</script>
