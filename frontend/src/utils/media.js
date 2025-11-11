export function mediaUrl(path) {
    if (!path) return ''
    const p = String(path)
    if (p.startsWith('http')) return p

    if (import.meta.env.DEV) return p

    const base = (import.meta.env.VITE_API_URL || '/api').replace(/\/api\/?$/, '')
    return `${base}${p}`
}
