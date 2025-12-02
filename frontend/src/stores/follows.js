// src/stores/follows.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

const LS_KEY = 'fw_followed_users'

function loadFollowed() {
    if (typeof localStorage === 'undefined') return []
    try {
        const raw = localStorage.getItem(LS_KEY)
        if (!raw) return []
        const parsed = JSON.parse(raw)
        return Array.isArray(parsed) ? parsed.map(x => String(x)) : []
    } catch {
        return []
    }
}

function saveFollowed(list) {
    if (typeof localStorage === 'undefined') return
    try {
        localStorage.setItem(LS_KEY, JSON.stringify(list))
    } catch {
        // ignorujemy
    }
}

export const useFollowsStore = defineStore('follows', () => {
    // przechowujemy nazwy użytkowników
    const followed = ref(loadFollowed())

    function isFollowed(username) {
        if (!username) return false
        const key = String(username)
        return followed.value.includes(key)
    }

    function toggle(username) {
        if (!username) return
        const key = String(username)

        if (isFollowed(key)) {
            followed.value = followed.value.filter(x => x !== key)
        } else {
            followed.value = [...followed.value, key]
        }
        saveFollowed(followed.value)
    }

    function clearAll() {
        followed.value = []
        saveFollowed(followed.value)
    }

    return {
        followed,
        isFollowed,
        toggle,
        clearAll,
    }
})

