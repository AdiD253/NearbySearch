package pl.defusadr.skyrisegplacesapi.service

import io.reactivex.Observable
import pl.defusadr.skyrisegplacesapi.model.LatLngModel
import pl.defusadr.skyrisegplacesapi.model.SearchServiceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GPlacesService {

    @GET("/maps/api/place/nearbysearch/json")
    fun getPlaces(
            @Query("keyword") keyword: String,
            @Query("location") location: LatLngModel,
            @Query("radius") radius: Int,
            @Query("key") appKey: String
    ): Observable<SearchServiceResponse>
}