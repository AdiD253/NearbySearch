package pl.defusadr.skyrisegplacesapi.ui

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import pl.defusadr.skyrisegplacesapi.model.LatLng
import pl.defusadr.skyrisegplacesapi.model.Place
import pl.defusadr.skyrisegplacesapi.service.GPlacesService
import pl.defusadr.skyrisegplacesapi.service.handleRxPlaceSearchResponse
import pl.defusadr.skyrisegplacesapi.util.filterByMaxRange
import javax.inject.Inject

class SearchQueryPresenterImpl @Inject constructor(
        private var service: GPlacesService
) : SearchQueryPresenter {

    private var view: SearchQueryView? = null
    private val disposable = CompositeDisposable()

    override fun attachView(view: SearchQueryView) {
        this.view = view
    }

    override fun detachView() {
        disposable.clear()
        this.view = null
    }

    override fun searchForPlaces(
            input: String,
            radius: Int,
            latitude: Double,
            longitude: Double,
            appKey: String
    ) {
        val location = LatLng(latitude, longitude)
        disposable +=
                service.getPlaces(
                        keyword = input,
                        location = location,
                        radius = radius,
                        appKey = appKey
                )
                        .handleRxPlaceSearchResponse()
                        .map {
                            it.results
                                    ?.map {
                                        Place(
                                                id = it.id,
                                                name = it.name,
                                                vicinity = it.vicinity,
                                                icon = it.icon,
                                                lat = it.geometry.latLng.lat,
                                                lng = it.geometry.latLng.lng
                                        )
                                    }
                                    ?.filterByMaxRange(
                                            center = location,
                                            radius = radius,
                                            maxResults = 3
                                    )
                                    ?: listOf()
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            view?.setProgress(true)
                        }
                        .subscribeBy(
                                onError = {
                                    it.printStackTrace()
                                    view?.setProgress(false)
                                    view?.showError(it)
                                },
                                onNext = {
                                    view?.setProgress(false)
                                    view?.showPlaces(it)
                                }
                        )
    }
}