import axios from 'axios'

const baseURL = import.meta.env.DEV
    ? '/api'
    : (import.meta.env.VITE_API_URL || '/api')

export const apiClient = axios.create({
    baseURL,
    withCredentials: true,
    headers: { 'Content-Type': 'application/json' },
})
