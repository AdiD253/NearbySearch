package pl.defusadr.skyrisegplacesapi.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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
            val distanceFromCenter = it.getDistanceFromLocation(LatLng(center.latitude, center.longitude))
            if (distanceFromCenter < radius) {
                filteredList.add(it)
            }
        }
    }
    return filteredList
}

fun View.setVisibility(isVisible: Boolean?) {
    when (isVisible) {
        true -> this.visibility = View.VISIBLE
        false -> this.visibility = View.GONE
        else -> this.visibility = View.INVISIBLE
    }
}

fun Activity.hideKeyboard() {
    currentFocus?.let {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(it.windowToken, 0)
    }
}