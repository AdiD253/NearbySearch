package pl.defusadr.skyrisegplacesapi.ui

import com.google.android.gms.maps.model.LatLng

interface SearchQueryPresenter{

    fun attachView(view: SearchQueryView)
    fun detachView()

    fun searchForPlaces(input: String, radius: Int, location: LatLng, appKey: String)

}