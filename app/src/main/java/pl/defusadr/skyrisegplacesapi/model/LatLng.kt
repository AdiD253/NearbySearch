package pl.defusadr.skyrisegplacesapi.model

import com.google.gson.annotations.SerializedName

class LatLng(
        @SerializedName("lat")
        var lat: Double,

        @SerializedName("lng")
        var lng: Double
) {
    override fun toString() = "$lat,$lng"
}