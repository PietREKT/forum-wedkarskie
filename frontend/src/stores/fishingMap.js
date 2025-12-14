import { ref } from 'vue'
import L from 'leaflet'

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

    function getLatLng(spot) {
        if (spot?.locationY != null && spot?.locationX != null) return { lat: spot.locationY, lng: spot.locationX }
        if (spot?.lat != null && spot?.lng != null) return { lat: spot.lat, lng: spot.lng }
        return { lat: null, lng: null }
    }

    function renderMarkers(spots, onClick) {
        if (!map.value || !markersLayer.value) return
        markersLayer.value.clearLayers()

        for (const spot of spots || []) {
            const { lat, lng } = getLatLng(spot)
            if (lat == null || lng == null) continue
            const marker = L.marker([lat, lng])
            marker.on('click', () => onClick?.(spot))
            markersLayer.value.addLayer(marker)
        }
    }

    function setViewIfCoords(lat, lng, minZoom = 11) {
        if (!map.value || lat == null || lng == null) return
        map.value.setView([lat, lng], Math.max(map.value.getZoom(), minZoom))
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

    // zwraca off(), który odpina listenery
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
