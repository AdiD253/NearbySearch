package pl.defusadr.skyrisegplacesapi.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import pl.defusadr.skyrisegplacesapi.R
import pl.defusadr.skyrisegplacesapi.model.Place

class SearchQueryAdapter(
        private val showPlaceOnMap: (place: Place) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var currentLocation: LatLng
    private var placesList = listOf<Place>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_place, parent, false)
        return PlaceViewHolder(view, currentLocation, showPlaceOnMap)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? PlaceViewHolder)?.bind(placesList[position])
    }
    override fun getItemCount() = placesList.size

    fun setCurrentLocation(location: LatLng) {
        this.currentLocation = location
    }

    fun initList(places: List<Place>) {
        placesList = places.sortedBy { it.getDistanceFromLocation(currentLocation) }
        notifyDataSetChanged()
    }

    fun clear() {
        placesList = listOf()
        notifyDataSetChanged()
    }
}