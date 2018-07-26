package pl.defusadr.skyrisegplacesapi.util

import android.location.Location
import com.google.android.gms.maps.model.Marker
import pl.defusadr.skyrisegplacesapi.model.LatLng
import pl.defusadr.skyrisegplacesapi.model.Place

fun MutableList<Marker>.removeAllMarkers() {
    forEach {
        it.remove()
    }
    clear()
}

fun List<Place>.filterByMaxRange(center: LatLng, radius: Int, maxResults: Int): List<Place> {
    val filteredList = mutableListOf<Place>()
    forEach {
        if (filteredList.size < maxResults) {
            val results = FloatArray(1)
            Location.distanceBetween(center.lat, center.lng, it.lat, it.lng, results)
            if (results[0] < radius) {
                filteredList.add(it)
            }
        }
    }
    return filteredList
}