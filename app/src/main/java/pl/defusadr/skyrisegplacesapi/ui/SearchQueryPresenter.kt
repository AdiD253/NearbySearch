package pl.defusadr.skyrisegplacesapi.ui

interface SearchQueryPresenter{

    fun attachView(view: SearchQueryView)
    fun detachView()

    fun searchForPlaces(input: String, radius: Int, latitude: Double, longitude: Double, appKey: String)

}