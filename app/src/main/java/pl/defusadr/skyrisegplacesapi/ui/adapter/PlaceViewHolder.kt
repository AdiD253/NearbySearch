package pl.defusadr.skyrisegplacesapi.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.recycler_item_place.view.*
import pl.defusadr.skyrisegplacesapi.R
import pl.defusadr.skyrisegplacesapi.model.Place

class PlaceViewHolder(
        view: View,
        private val currentLocation: LatLng,
        private val showPlaceOnMap: (place: Place) -> Unit
) : RecyclerView.ViewHolder(view) {

    fun bind(place: Place) {
        itemView.placeName.text = place.name
        itemView.placeVicinity.text = place.vicinity
        val distance = "%.1f".format(place.getDistanceFromLocation(currentLocation) / 1000)

        itemView.placeDistanceFromLocation.text = itemView.context.getString(R.string.distance_km, distance)

        itemView.setOnClickListener {
            showPlaceOnMap.invoke(place)
        }
    }
}