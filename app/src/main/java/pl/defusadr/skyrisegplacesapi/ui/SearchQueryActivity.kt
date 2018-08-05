package pl.defusadr.skyrisegplacesapi.ui

import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_search_query.*
import kotlinx.android.synthetic.main.bottom_sheet_search_results.*
import pl.defusadr.skyrisegplacesapi.R
import pl.defusadr.skyrisegplacesapi.model.Place
import pl.defusadr.skyrisegplacesapi.service.PlaceSearchInvalidRequestError
import pl.defusadr.skyrisegplacesapi.service.PlaceSearchOverQueryError
import pl.defusadr.skyrisegplacesapi.service.PlaceSearchRequestDeniedError
import pl.defusadr.skyrisegplacesapi.service.PlaceSearchZeroResultError
import pl.defusadr.skyrisegplacesapi.ui.adapter.SearchQueryAdapter
import pl.defusadr.skyrisegplacesapi.util.hideKeyboard
import pl.defusadr.skyrisegplacesapi.util.removeAllMarkers
import pl.defusadr.skyrisegplacesapi.util.setVisibility
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SearchQueryActivity : AppCompatActivity(), SearchQueryView, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    @Inject
    lateinit var presenter: SearchQueryPresenter

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchLocation: LatLng
    private lateinit var currentCircle: Circle
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var currentRadius = 5000

    private val skyrise = LatLng(50.2626303, 19.0140012)
    private val searchSubject = PublishSubject.create<String>()
    private var placeMarkers = mutableListOf<Marker>()

    private val placesAdapter: SearchQueryAdapter by lazy {
        SearchQueryAdapter {
            showPlaceOnMap(it)
        }
    }

    //region activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_query)
        AndroidInjection.inject(this@SearchQueryActivity)

        initMapComponents()
        initUIComponents()
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
        initSearchSubject()
    }

    override fun onStop() {
        presenter.detachView()
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            setUpMap(shouldAskForPermission = grantResults.contains(PackageManager.PERMISSION_GRANTED))
        }
    }

    //endregion

    //region interfaces

    override fun showPlaces(places: List<Place>) {
        placeMarkers.removeAllMarkers()
        placesAdapter.clear()
        places.forEach {
            placeMarkerOnMap(it.location, it.name).apply {
                it.placeMarker = this
                placeMarkers.add(this)
            }

        }
        placesAdapter.initList(places)
        zoomMap(false)
        showPlacesBottomSheet(getString(R.string.show_places, placeMarkers.size), true)
    }

    override fun setProgress(visible: Boolean?) {
        searchProgress.setVisibility(visible)
    }

    override fun showError(throwable: Throwable) {
        when (throwable) {
            is PlaceSearchZeroResultError -> {
                showPlacesBottomSheet(
                        getString(R.string.no_results_found_for_query, searchEditText.text.toString().trim()),
                        false)
            }
            is PlaceSearchOverQueryError -> {
                Toast.makeText(
                        this@SearchQueryActivity,
                        getString(R.string.app_has_reached_query_limit),
                        Toast.LENGTH_SHORT
                ).show()
            }
            is PlaceSearchRequestDeniedError -> {
                val msg = if (TextUtils.isEmpty(throwable.message))
                    getString(R.string.error_request_denied)
                else throwable.message
                Toast.makeText(
                        this@SearchQueryActivity,
                        msg,
                        Toast.LENGTH_SHORT
                ).show()
            }
            is PlaceSearchInvalidRequestError -> {
                Timber.e(throwable)
            }
            else -> {
                showPlacesBottomSheet(getString(R.string.an_error_occurred), false)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnMarkerClickListener(this)
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isMapToolbarEnabled = false
        }

        setUpMap(shouldAskForPermission = true)
    }

    override fun onMarkerClick(p0: Marker?) = false

    //endregion

    private fun initSearchSubject() {
        searchSubject
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (!TextUtils.isEmpty(it)) {
                        zoomMap(false)
                        hideKeyboard()
                        searchForPlacesByQuery(it)
                    }
                }
    }

    private fun searchForPlacesByQuery(input: String) {
        presenter.searchForPlaces(
                input.trim(),
                currentRadius,
                searchLocation,
                getString(R.string.google_maps_key)
        )
    }

    private fun initMapComponents() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@SearchQueryActivity)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initUIComponents() {
        initRecycler()
        initSeekBar()
        initPlacesBottomSheet()
        initListeners()
    }

    private fun initRecycler() {
        placesBottomSheetRecycler.apply {
            adapter = placesAdapter
            layoutManager = LinearLayoutManager(this@SearchQueryActivity)
            addItemDecoration(object : DividerItemDecoration(this@SearchQueryActivity, DividerItemDecoration.VERTICAL) {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    if (parent.getChildAdapterPosition(view) == parent.adapter.itemCount - 1) {
                        outRect.setEmpty()
                    } else {
                        super.getItemOffsets(outRect, view, parent, state)
                    }
                }
            })
        }
    }

    private fun initSeekBar() {
        val maxValue = 10000
        val minValue = 100

        searchRangeInfoTv.text = getString(R.string.search_radius, currentRadius)

        searchRangeSeekBar.apply {
            max = maxValue - minValue
            progress = currentRadius
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    onSeekBarProgressChanged(minValue, progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    placesBottomSheet.visibility = View.GONE
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun onSeekBarProgressChanged(minValue: Int, progress: Int) {
        val selectedValue = minValue + progress
        currentRadius = selectedValue
        searchRangeInfoTv.text = getString(R.string.search_radius, currentRadius)
        zoomMap(true)
        searchSubject.onNext(searchEditText.text.toString())
    }

    private fun initPlacesBottomSheet() {
        placesBottomSheet.visibility = View.GONE
        bottomSheetBehavior = BottomSheetBehavior.from(placesBottomSheet)
        bottomSheetBehavior.isHideable = false

        placesBottomSheetPeekTitle.setOnClickListener {
            changeBottomSheetState(bottomSheetBehavior)
        }

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> setBottomSheetStateExpanded(false)
                    BottomSheetBehavior.STATE_COLLAPSED -> setBottomSheetStateExpanded(true)
                }
            }
        })
    }

    private fun changeBottomSheetState(bottomSheetBehavior: BottomSheetBehavior<View>) {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_COLLAPSED -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun setBottomSheetStateExpanded(isCollapsed: Boolean) {
        placesBottomSheetPeekTitle.visibility = View.VISIBLE
        map.uiSettings.setAllGesturesEnabled(isCollapsed)
        val peekTitle =
                if (isCollapsed)
                    getString(R.string.show_places, placeMarkers.size)
                else
                    getString(R.string.places_for_query, searchEditText.text.trim())

        placesBottomSheetPeekTitle.text = peekTitle
    }

    private fun initListeners() {
        myLocationImage.setOnClickListener {
            zoomMap(false)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        searchClearQuery.setOnClickListener {
            restoreDefaultState()
        }

        searchEditText.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (TextUtils.isEmpty(s)) {
                        searchClearQuery.visibility = View.GONE
                        placeMarkers.removeAllMarkers()
                    } else {
                        searchClearQuery.visibility = View.VISIBLE
                    }
                    placesBottomSheet.visibility = View.GONE
                    map.uiSettings.setAllGesturesEnabled(true)
                }
            })

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = searchEditText.text.toString()
                    if (query.isNotEmpty())
                        prepareViewAndSearchByQuery(query)
                    true
                } else false
            }
        }
    }

    private fun restoreDefaultState() {
        zoomMap(false)
        searchEditText.setText("")
        hideKeyboard()
        placesBottomSheet.visibility = View.GONE
        map.uiSettings.setAllGesturesEnabled(true)
    }

    private fun prepareViewAndSearchByQuery(query: String) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        placesBottomSheet.visibility = View.GONE
        zoomMap(false)
        hideKeyboard()
        searchForPlacesByQuery(query)
    }

    private fun setUpMap(shouldAskForPermission: Boolean = false) {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initMapWithUserLocation()
        } else {
            if (shouldAskForPermission) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                initMapForLocation(skyrise)
                Toast.makeText(
                        this@SearchQueryActivity,
                        getString(R.string.no_location_permission_granted),
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @Throws(SecurityException::class)
    private fun initMapWithUserLocation() {
        map.isMyLocationEnabled = false
        fusedLocationClient.lastLocation.addOnSuccessListener(this) {
            it?.let {
                initMapForLocation(LatLng(it.latitude, it.longitude))
            }
        }
    }

    private fun initMapForLocation(location: LatLng) {
        searchLocation = location
        placesAdapter.setCurrentLocation(searchLocation)
        placeMarkerOnMap(searchLocation, iconRes = R.drawable.human_icon)
        zoomMap(true)
    }

    private fun placeMarkerOnMap(location: LatLng, title: String? = null, @DrawableRes iconRes: Int? = null): Marker {
        val markerOptions = MarkerOptions().position(location).title(title)

        iconRes?.let {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(it))
        }

        return map.addMarker(markerOptions)
    }

    private fun showPlaceOnMap(place: Place) {
        place.placeMarker?.showInfoWindow()
        zoomMap(false, place.location, 16)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun zoomMap(drawCircle: Boolean, customLocation: LatLng? = null, customZoom: Int? = null) {
        val locationToZoom = customLocation?.let { it } ?: searchLocation

        customZoom?.let {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(locationToZoom, it.toFloat()))
        } ?: kotlin.run {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    getBoundsFromCenterRadius(locationToZoom, currentRadius), 100)
            )
        }

        if (drawCircle)
            placeCircleOnMap(searchLocation, currentRadius)
    }

    private fun getBoundsFromCenterRadius(center: LatLng, radiusInMeters: Int): LatLngBounds {
        val distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0)
        val southwestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
        val northeastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
        return LatLngBounds(southwestCorner, northeastCorner)
    }

    private fun placeCircleOnMap(location: LatLng, radius: Int) {
        val circleOptions = CircleOptions()
                .center(location)
                .radius(radius.toDouble())
                .strokeColor(ContextCompat.getColor(this@SearchQueryActivity, R.color.colorAccent))
                .fillColor(ContextCompat.getColor(this@SearchQueryActivity, R.color.colorMapCircleFill))

        if (this::currentCircle.isInitialized)
            this.currentCircle.remove()
        this.currentCircle = map.addCircle(circleOptions)
    }

    private fun showPlacesBottomSheet(titleText: String, clickable: Boolean) {
        placesBottomSheet.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        placesBottomSheetPeekTitle.apply {
            isClickable = clickable
            text = titleText
        }
    }
}