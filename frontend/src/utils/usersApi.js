import { apiClient } from './axios.js'

export function getMe() {
    return apiClient.get('/users/me')
}

// backend oczekuje parametru: q
export function searchUsersByUsername(username) {
    return apiClient.get('/users/search', {
        params: { q: username },
    })
}

export function sendFriendInvite(userId) {
    return apiClient.post('/users/friends/invite', {
        id: userId,
    })
}

export function removeFriend(userId) {
    return apiClient.delete(`/users/friends/${userId}`)
}
