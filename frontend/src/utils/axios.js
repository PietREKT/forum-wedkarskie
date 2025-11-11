import axios from 'axios'

const baseURL = import.meta.env.DEV
    ? '/api'
    : (import.meta.env.VITE_API_URL || '/api')

export const apiClient = axios.create({
    baseURL,
    withCredentials: true,
    timeout: 15000,
})

apiClient.defaults.headers.common['Accept'] = 'application/json'

apiClient.interceptors.response.use(
    r => r,
    err => {
        if (err?.response?.status === 401) {
            try { localStorage.removeItem('fw_user') } catch {}
        }
        return Promise.reject(err)
    }
)
