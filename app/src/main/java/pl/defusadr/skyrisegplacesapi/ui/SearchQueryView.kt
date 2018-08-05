package pl.defusadr.skyrisegplacesapi.ui

import pl.defusadr.skyrisegplacesapi.model.Place

interface SearchQueryView {

    fun showPlaces(places: List<Place>)
    fun setProgress(visible: Boolean?)
    fun showError(throwable: Throwable)
}