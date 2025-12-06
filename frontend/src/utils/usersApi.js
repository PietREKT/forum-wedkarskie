import { apiClient } from './axios.js'

export function getMe() {
    return apiClient.get('/users/me')
}

// wyszukiwanie użytkowników po nicku
export function searchUsersByUsername(username) {
    return apiClient.get('/users/search', {
        params: { username },
    })
}

// wysłanie obserwowania
export function sendFriendInvite(userId) {
    return apiClient.post('/users/friends/invite', {
        id: userId,
    })
}

// usunięcie użytkownika z obserwowanych
export function removeFriend(userId) {
    return apiClient.delete(`/users/friends/${userId}`)
}
