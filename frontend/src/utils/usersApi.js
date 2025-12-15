import { apiClient } from './axios.js'

export function getMe() {
    return apiClient.get('/users/me')
}

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

export function acceptFriendRequest(reqId) {
    return apiClient.patch(`/users/friends/${reqId}/accept`)
}

export function rejectFriendRequest(reqId) {
    return apiClient.patch(`/users/friends/${reqId}/reject`)
}

export function cancelFriendRequest(reqId) {
    return apiClient.patch(`/users/friends/${reqId}/cancel`)
}
