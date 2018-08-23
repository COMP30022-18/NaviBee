package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import au.edu.unimelb.eng.navibee.BuildConfig
import au.edu.unimelb.eng.navibee.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
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
    private var sendResult: Boolean = false

    // Google Maps Geo API Context
    private lateinit var geoContext: GeoApiContext

    // Recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val destinations = ArrayList<DestinationRVItem>()

    private val searchResults = ArrayList<PlacesSearchResult>()
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_destinations_search_result)

        // Setup data for loading screen
        destinations.add(DestinationRVIndefiniteProgressBar())

        // setup recycler view
        viewManager = LinearLayoutManager(this)
        viewAdapter = DestinationsRVAdaptor(destinations)

        recyclerView = navigation_destinations_search_result_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // Setup location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set incoming parameters
        sendResult = intent.getBooleanExtra(ARGS_SEND_RESULT, false)

        // setup collapsible view
        setSupportActionBar(navigation_destinations_search_result_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigation_destinations_search_result_appbar.layoutParams.height =
                (resources.displayMetrics.heightPixels * 0.618).toInt()

        // setup Google Maps Geo API Context
        geoContext = GeoApiContext.Builder()
                .apiKey(BuildConfig.GOOGLE_PLACES_API_KEY)
                .build()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.navigation_destinations_search_result_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEARCH -> {
                val query = intent.getStringExtra(SearchManager.QUERY)
                navigation_destinations_search_result_collapsing.title = query
                Timber.d("Handling intent on search query $query.")
                startActivityForResult<LocationPermissionRequestActivity>(
                        CHECK_LOCATION_PERMISSION,
                        "query" to query,
                        "snackBarLayout" to R.id.destinations_activity_coordinator_layout
                )
            }
            Intent.ACTION_VIEW -> {
                val placeId = intent.extras?.getString(SearchManager.EXTRA_DATA_KEY)
                if (placeId != null)
                    startActivity<DestinationDetailsActivity>(
                            DestinationDetailsActivity.EXTRA_PLACE_ID to placeId
                    )
                finish()
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
                    this.lastKnownLocation = location
                    Timber.d("Last known location: $location")

                    val callback = object : PendingResult.Callback<PlacesSearchResponse> {
                        override fun onFailure(e: Throwable?) {
                            Timber.e(e, "Error on performing location search.")
                            renderErrorMessage(R.string.destination_search_failed)
                        }

                        override fun onResult(result: PlacesSearchResponse) {
                            destinations.clear()
                            val attributions: ArrayList<String> = ArrayList()

                            if (result.results.isEmpty()) {
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
                                            name = item.name,
                                            location = item.vicinity,
                                            googlePhotoReference = photoReference,
                                            onClick = View.OnClickListener {
                                                startActivity<DestinationDetailsActivity>(
                                                        DestinationDetailsActivity.EXTRA_PLACE_ID to item.placeId
                                                )
                                            }
                                    ))
                                }

                                val attrHTML = resources.getString(R.string.search_result_attributions) +
                                        "<br>" +
                                        attributions.joinToString(", ")
                                val formattedHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Html.fromHtml(attrHTML, Html.FROM_HTML_MODE_COMPACT)
                                } else {
                                    Html.fromHtml(attrHTML)
                                }
                                destinations.add(DestinationRVAttributes(formattedHtml))
                            }
                            runOnUiThread{initializeMap()}
                            runOnUiThread(viewAdapter::notifyDataSetChanged)
                        }

                    }

                    if (location != null) {
                        val request = PlacesApi
                                .nearbySearchQuery(geoContext, MapsLatLng(location.latitude, location.longitude))
                                .keyword(query)
                                .rankby(RankBy.DISTANCE)
                        request.setCallback(callback)
                    } else {
                        // TODO: Allow configurable region of search.
                        val request = PlacesApi
                                .textSearchQuery(geoContext, query)
                                .custom("region", "au")
                        request.setCallback(callback)
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
        val gm = googleMap
        if (gm != null) {
            val latLngBoundsBuilder = LatLngBounds.Builder()
            for ((i, item) in searchResults.withIndex()) {
                val coord = GmsLatLng(item.geometry.location.lat,
                        item.geometry.location.lng)
                latLngBoundsBuilder.include(coord)

                gm.addMarker(MarkerOptions().position(coord).title(item.name).let {
                    if (i >= 10) {
                        it.icon(BitmapDescriptorFactory.fromResource(
                                R.drawable.navigation_map_minor_marker))
                    } else {
                        it.icon(BitmapDescriptorFactory.fromBitmap(
                                IconGenerator(this).run {
                                    setColor(IconGenerator.STYLE_RED)
                                    makeIcon("${(i + 'A'.toInt()).toChar()}")
                                }))

                    }
                })
            }
            gm.animateCamera(CameraUpdateFactory
                    .newLatLngBounds(latLngBoundsBuilder.build(), 48))
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        val lkl = lastKnownLocation
        this.googleMap = googleMap
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
            googleMap.isMyLocationEnabled = true
        if (searchResults.size > 0) {
            initializeMap()
        } else if (lkl != null) {
            googleMap.moveCamera(
                    CameraUpdateFactory.newLatLng(
                            GmsLatLng(lkl.latitude, lkl.longitude)))
        }
    }
}