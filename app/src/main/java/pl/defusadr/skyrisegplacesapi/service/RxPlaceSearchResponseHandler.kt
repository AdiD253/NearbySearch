package pl.defusadr.skyrisegplacesapi.service

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function
import pl.defusadr.skyrisegplacesapi.model.SearchServiceResponse

fun Observable<SearchServiceResponse>.handleRxPlaceSearchResponse(): Observable<SearchServiceResponse> =
    this.flatMap { RxPlaceSearchResponseHandler().apply(it) }


class RxPlaceSearchResponseHandler
    : Function<SearchServiceResponse, ObservableSource<SearchServiceResponse>> {

    companion object {
        private const val ZERO_RESULTS = "ZERO_RESULTS"
        private const val OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT"
        private const val REQUEST_DENIED = "REQUEST_DENIED"
        private const val INVALID_REQUEST = "INVALID_REQUEST"
        private const val UNKNOWN_ERROR = "UNKNOWN_ERROR"
    }

    override fun apply(response: SearchServiceResponse): ObservableSource<SearchServiceResponse> {
        return when (response.status) {
            ZERO_RESULTS -> ObservableSource {
                it.onError(PlaceSearchZeroResultError())
            }
            OVER_QUERY_LIMIT -> ObservableSource {
                it.onError(PlaceSearchOverQueryError())
            }
            REQUEST_DENIED -> ObservableSource {
                it.onError(PlaceSearchRequestDeniedError(response.errorMessage))
            }
            INVALID_REQUEST -> ObservableSource {
                it.onError(PlaceSearchInvalidRequestError(response.errorMessage))
            }
            UNKNOWN_ERROR -> ObservableSource {
                it.onError(PlaceSearchUnknownError(response.errorMessage))
            }
            else -> ObservableSource {
                it.onNext(response)
            }
        }
    }
}

class PlaceSearchZeroResultError : Throwable()
class PlaceSearchOverQueryError : Throwable()
class PlaceSearchRequestDeniedError(message: String?) : Throwable(message)
class PlaceSearchInvalidRequestError(message: String?) : Throwable(message)
class PlaceSearchUnknownError(message: String?) : Throwable(message)

