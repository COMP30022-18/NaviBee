package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.text.Html
import android.util.DisplayMetrics
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import au.edu.unimelb.eng.navibee.BuildConfig
import au.edu.unimelb.eng.navibee.R
import au.edu.unimelb.eng.navibee.utils.Resource
import au.edu.unimelb.eng.navibee.utils.getActionBarHeight
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.PlacesApi
import com.google.maps.android.ui.IconGenerator
import com.google.maps.model.PlacesSearchResponse
import com.google.maps.model.PlacesSearchResult
import com.google.maps.model.RankBy
import kotlinx.android.synthetic.main.activity_navigation_destinations_search_result.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import timber.log.Timber
import com.google.android.gms.maps.model.LatLng as GmsLatLng
import com.google.maps.model.LatLng as MapsLatLng

/**
 * Required arguments:
 *     intent action: Intent.ACTION_SEARCH
 *     SearchManager.QUERY: string, query to search
 *     ARGS_SEND_RESULT: boolean, set to true to send result back to the intent starter
 */
class DestinationsSearchResultActivity: AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val CHECK_LOCATION_PERMISSION = 1
        const val ARGS_SEND_RESULT = "sendResult"
    }

    // Location service
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    // incoming parameters
    private var query: String? = null
    private var sendResult: Boolean = false

    // Recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val destinations = ArrayList<DestinationRVItem>()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val searchResults = ArrayList<PlacesSearchResult>()
    private var googleMap: GoogleMap? = null

    private lateinit var viewModel: DestinationSearchResultViewModel

    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_destinations_search_result)

        // Setup view model
        viewModel = ViewModelProviders.of(this).get(DestinationSearchResultViewModel::class.java)

        subscribe()

        // Setup data for loading screen
        destinations.add(DestinationRVIndefiniteProgressBar())

        // setup recycler view
        viewManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        viewAdapter = DestinationsRVAdaptor(destinations)

        recyclerView = navigation_destinations_search_result_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        // Setup location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set incoming parameters
        sendResult = intent.getBooleanExtra(ARGS_SEND_RESULT, false)

        // setup collapsible view
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        bottomSheetBehavior = BottomSheetBehavior.from(recyclerView)
        val actionBarHeight = getActionBarHeight(this)
        val heights = displayMetrics.heightPixels - actionBarHeight
        bottomSheetBehavior.peekHeight = (heights * 0.382).toInt()
        recyclerView.minimumHeight = (heights * 0.382).toInt()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.navigation_destinations_search_result_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        handleIntent(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        handleIntent(intent ?: return)
    }

    private fun subscribe() {
        viewModel.searchResult.observe(this, Observer {
            if (it.status == Resource.Status.ERROR) {
                Timber.e(it.throwable, "Error on performing location search.")
                renderErrorMessage(R.string.destination_search_failed)
            } else if (it.status == Resource.Status.SUCCESS) {
                val result = it.data

                destinations.clear()
                val attributions: ArrayList<String> = ArrayList()

                if (result?.results?.isEmpty() != false) {
                    renderErrorMessage(R.string.destination_search_no_result)
                } else {
                    attributions.addAll(result.htmlAttributions)

                    for (item in result.results) {
                        searchResults.add(item)

                        var photoReference: String? = null
                        if (item.photos?.isEmpty() == false) {
                            photoReference = item.photos[0].photoReference
                            attributions.addAll(item.photos[0].htmlAttributions)
                        }
                        destinations.add(DestinationRVEntry(
                                name = item.name ?: "",
                                location = item.vicinity ?: "",
                                googlePlaceId = item.placeId,
                                googlePhotoReference = photoReference
                        ).onClick(View.OnClickListener { _ ->
                            startActivity<DestinationDetailsActivity>(
                                    DestinationDetailsActivity.EXTRA_PLACE_ID to item.placeId
                            )
                        }))
                    }

                    val attrHTML = resources.getString(R.string.search_result_attributions) +
                            "<br>" +
                            attributions.joinToString(", ")
                    val formattedHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(attrHTML, Html.FROM_HTML_MODE_COMPACT)
                    } else {
                        @Suppress("DEPRECATION")
                        Html.fromHtml(attrHTML)
                    }
                    destinations.add(DestinationRVAttributions(formattedHtml))
                }
            }

            initializeMap()
            viewAdapter.notifyDataSetChanged()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        this.menuInflater.inflate(R.menu.menu_navigation_destinations_opitons, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        // Set up the search view
        val menuItemSearchView = menu.findItem(R.id.nav_dest_optmnu_search)
        searchView = (menuItemSearchView.actionView as SearchView)
        searchView?.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))

            // Prevent the search view from collapsing within the subview
            setIconifiedByDefault(false)

            // Let the search view to fill the entire space
            maxWidth = Integer.MAX_VALUE
        }
        searchView?.post {
            searchView?.setQuery(query, false)
        }
        return true
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val placeId = intent.extras?.getString(SearchManager.EXTRA_DATA_KEY)
                if (placeId != null) {
                    startActivity<DestinationDetailsActivity>(
                        DestinationDetailsActivity.EXTRA_PLACE_ID to placeId
                    )
                    finish()
                    return
                }
            }
            Intent.ACTION_SEARCH -> {
                query = intent.getStringExtra(SearchManager.QUERY)
                supportActionBar?.title = query
                SearchRecentSuggestions(this,
                        DestinationsSearchSuggestionsContentProvider.AUTHORITY,
                        DestinationsSearchSuggestionsContentProvider.MODE)
                        .saveRecentQuery(query, null)
                startActivityForResult<LocationPermissionRequestActivity>(
                        CHECK_LOCATION_PERMISSION,
                        "query" to query,
                        "snackBarLayout" to R.id.destinations_activity_coordinator_layout
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CHECK_LOCATION_PERMISSION -> {
                if (resultCode == Activity.RESULT_OK)
                    searchForLocation(data!!.getStringExtra("query"))
            }
        }
    }

    private fun searchForLocation(query: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            finish()
            return
        }

        fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    if (location != null) {
                        Timber.d("Last known location: $location")
                        lastKnownLocation = location
                        if (googleMap != null) onMapReady(googleMap!!)

                        viewModel.searchForLocation(query, location)
                    }

                }
                .addOnFailureListener {
                    Timber.e(it, "Error occurred when trying to get the last known location.")
                    renderErrorMessage(R.string.fail_to_get_last_location)
                }
    }

    private fun renderErrorMessage(text: Int) {
        destinations.clear()
        destinations.add(DestinationRVErrorMessage(resources.getString(text)))

        runOnUiThread(viewAdapter::notifyDataSetChanged)
    }

    private fun initializeMap() {
        googleMap?.let { gm ->
            if (searchResults.isNotEmpty()) {
                val latLngBoundsBuilder = LatLngBounds.Builder()
                for ((i, item) in searchResults.withIndex()) {
                    val coord = GmsLatLng(item.geometry.location.lat,
                        item.geometry.location.lng)

                    if (i < 10)
                        latLngBoundsBuilder.include(coord)
                    gm.addMarker(MarkerOptions().position(coord).title(item.name).let {
                        if (i >= 10) {
                            it.icon(BitmapDescriptorFactory.fromResource(
                                R.drawable.navigation_map_minor_marker))

                        } else {
                            it.icon(BitmapDescriptorFactory.fromBitmap(
                                IconGenerator(this).run {
                                    makeIcon("${(i + 'A'.toInt()).toChar()}")
                                }))

                        }
                    })
                }
                gm.animateCamera(CameraUpdateFactory
                    .newLatLngBounds(latLngBoundsBuilder.build(), 128))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val lkl = lastKnownLocation
        this.googleMap = googleMap

        googleMap.setPadding(0, 0, 0, bottomSheetBehavior.peekHeight)
        googleMap.uiSettings.isMapToolbarEnabled = false

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
            googleMap.isMyLocationEnabled = true

        if (lkl != null) {
            googleMap.moveCamera(
                    CameraUpdateFactory.newLatLng(
                            GmsLatLng(lkl.latitude, lkl.longitude)))
        }
    }
}


private class DestinationSearchResultViewModel(val context: Application):
        AndroidViewModel(context) {

    // Google Maps Geo API Context
    private var geoContext: GeoApiContext = GeoApiContext.Builder()
            .apiKey(BuildConfig.GOOGLE_PLACES_API_KEY)
            .build()

    val searchResult = MutableLiveData<Resource<PlacesSearchResponse>>()
    private val query: String? = null
    private val location: Location? = null

    fun searchForLocation(query: String, location: Location?) {
        if (this.query == query && this.location == location) return
        val callback = object : PendingResult.Callback<PlacesSearchResponse> {
            override fun onFailure(e: Throwable?) {
                if (e != null)
                    searchResult.postValue(Resource.error(e))
            }

            override fun onResult(result: PlacesSearchResponse) {
                searchResult.postValue(Resource.success(result))
            }

        }

        if (location != null) {
            val request = PlacesApi
                    .nearbySearchQuery(geoContext, MapsLatLng(location.latitude, location.longitude))
                    .keyword(query)
                    .rankby(RankBy.DISTANCE)
            request.setCallback(callback)
        } else {
            val request = PlacesApi
                    .textSearchQuery(geoContext, query)
                    .custom("region", getSearchRegion(context))
            request.setCallback(callback)
        }
    }
}