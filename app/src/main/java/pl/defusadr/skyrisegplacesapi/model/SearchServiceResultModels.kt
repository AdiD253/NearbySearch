package pl.defusadr.skyrisegplacesapi.model

import com.google.gson.annotations.SerializedName

class SearchServiceResponse(
        @SerializedName("results")
        var results: List<SearchServiceResult>? = null,

        @SerializedName("status")
        var status: String,

        @SerializedName("error_message")
        var errorMessage: String?
)

class SearchServiceResult(
        @SerializedName("id")
        var id: String,

        @SerializedName("name")
        var name: String,

        @SerializedName("vicinity")
        var vicinity: String,

        @SerializedName("icon")
        var icon: String,

        @SerializedName("geometry")
        var geometry: SearchServiceGeometry
)

class SearchServiceGeometry(
        @SerializedName("location")
        var latLng: LatLng
)