package pl.defusadr.skyrisegplacesapi.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class Place(
        var id: String,
        var name: String,
        var icon: String,
        var vicinity: String,
        var location: LatLng,
        var placeMarker: Marker? = null
) {

    fun getDistanceFromLocation(location: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
                location.latitude,
                location.longitude,
                this.location.latitude,
                this.location.longitude,
                results
        )
        return results[0]
    }
}