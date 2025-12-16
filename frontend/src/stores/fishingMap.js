// src/stores/fishingMap.js
import { ref } from 'vue'
import L from 'leaflet'

// Vite + Leaflet: poprawne ścieżki do ikon (inaczej w Network leci spam requestów i markery znikają)
import markerIcon2xUrl from 'leaflet/dist/images/marker-icon-2x.png'
import markerIconUrl from 'leaflet/dist/images/marker-icon.png'
import markerShadowUrl from 'leaflet/dist/images/marker-shadow.png'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
    iconRetinaUrl: markerIcon2xUrl,
    iconUrl: markerIconUrl,
    shadowUrl: markerShadowUrl,
})

export const useFishingMapStore = () => {
    const map = ref(null)
    const markersLayer = ref(null)

    function init(containerId) {
        const el = document.getElementById(containerId)
        if (!el) return

        destroy()

        map.value = L.map(el).setView([52.2297, 21.0122], 6)

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors',
            maxZoom: 19,
        }).addTo(map.value)

        markersLayer.value = L.layerGroup().addTo(map.value)
    }

    function destroy() {
        if (!map.value) return
        try {
            map.value.off()
            map.value.remove()
        } catch {}
        map.value = null
        markersLayer.value = null
    }

    // Ujednolicenie współrzędnych z backendu:
    // - locationDto.latitude / locationDto.longitude
    // - location.latitude / location.longitude
    // - lat/lng
    // - locationY/locationX (stare)
    function getLatLng(spot) {
        if (!spot) return { lat: null, lng: null }

        const dtoLat = spot?.locationDto?.latitude
        const dtoLng = spot?.locationDto?.longitude
        if (dtoLat != null && dtoLng != null) return { lat: Number(dtoLat), lng: Number(dtoLng) }

        const locLat = spot?.location?.latitude
        const locLng = spot?.location?.longitude
        if (locLat != null && locLng != null) return { lat: Number(locLat), lng: Number(locLng) }

        if (spot?.lat != null && spot?.lng != null) return { lat: Number(spot.lat), lng: Number(spot.lng) }

        // locationY=lat, locationX=lng
        if (spot?.locationY != null && spot?.locationX != null) {
            return { lat: Number(spot.locationY), lng: Number(spot.locationX) }
        }

        return { lat: null, lng: null }
    }

    function renderMarkers(spots, onClick) {
        if (!map.value || !markersLayer.value) return
        markersLayer.value.clearLayers()

        for (const spot of spots || []) {
            const { lat, lng } = getLatLng(spot)
            if (lat == null || lng == null || Number.isNaN(lat) || Number.isNaN(lng)) continue
            const marker = L.marker([lat, lng])
            marker.on('click', () => onClick?.(spot))
            markersLayer.value.addLayer(marker)
        }
    }

    function setViewIfCoords(lat, lng, minZoom = 11) {
        if (!map.value || lat == null || lng == null) return
        const la = Number(lat)
        const ln = Number(lng)
        if (Number.isNaN(la) || Number.isNaN(ln)) return
        map.value.setView([la, ln], Math.max(map.value.getZoom(), minZoom))
    }

    // dla /spots/radius: x=lng, y=lat
    function getCenter() {
        if (!map.value) return null
        const c = map.value.getCenter()
        return { x: c.lng, y: c.lat }
    }

    function getRadiusKm() {
        if (!map.value) return null
        const b = map.value.getBounds()
        const c = map.value.getCenter()
        const ne = b.getNorthEast()
        return Math.max(1, Math.round(c.distanceTo(ne) / 1000))
    }

    function onViewportChanged(cb, debounceMs = 350) {
        if (!map.value) return () => {}

        let t = null
        const handler = () => {
            if (t) clearTimeout(t)
            t = setTimeout(() => cb?.(), debounceMs)
        }

        map.value.on('moveend', handler)
        map.value.on('zoomend', handler)

        return () => {
            if (t) clearTimeout(t)
            if (!map.value) return
            try {
                map.value.off('moveend', handler)
                map.value.off('zoomend', handler)
            } catch {}
        }
    }

    return {
        map,
        init,
        destroy,
        getLatLng,
        renderMarkers,
        setViewIfCoords,
        getCenter,
        getRadiusKm,
        onViewportChanged,
    }
}
