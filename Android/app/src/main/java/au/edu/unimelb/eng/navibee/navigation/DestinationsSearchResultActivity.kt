package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import au.edu.unimelb.eng.navibee.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import kotlinx.android.synthetic.main.activity_navigation_destinations_search_result.*
import org.jetbrains.anko.startActivityForResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Required arguments:
 *     intent action: Intent.ACTION_SEARCH
 *     SearchManager.QUERY: string, query to search
 *     ARGS_SEND_RESULT: boolean, set to true to send result back to the intent starter
 */
class DestinationsSearchResultActivity: AppCompatActivity() {

    companion object {
        const val CHECK_LOCATION_PERMISSION = 1
        const val ARGS_SEND_RESULT = "sendResult"
    }

    // Location service
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    // incoming parameters
    private var sendResult: Boolean = false


    // Recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val destinations = ArrayList<DestinationRVItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_destinations_search_result)

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


        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            navigation_destinations_search_result_collapsing.title = query
                    Timber.d("Handling intent on search query $query.")
            startActivityForResult<LocationPermissionRequestActivity>(
                    CHECK_LOCATION_PERMISSION,
                    "query" to query,
                    "snackBarLayout" to R.id.destinations_activity_coordinator_layout
            )
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            finish()
            return
        }

        fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    this.lastKnownLocation = location
                    Timber.d("Last known location: $location")
                    val builder = MapboxGeocoding.builder()
                            .accessToken(Mapbox.getAccessToken()!!)
                            .query(query)
                    if (location != null) {
                        builder.proximity(Point.fromLngLat(location.longitude, location.latitude, location.altitude))
                    }
                    val mapboxGeocoding = builder.build()

                    mapboxGeocoding.enqueueCall(object : Callback<GeocodingResponse> {
                        override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                            val results = response.body()!!.features()
                            if (results.size > 0) {

                                // Log the first results Point.
                                val firstResultPoint = results[0].center()
                                Timber.d("onResponse: $firstResultPoint (lat ${firstResultPoint!!.latitude()}, long ${firstResultPoint.longitude()})")

                                destinations.clear()
                                for (item in results) {
                                    destinations.add(DestinationRVEntry(
                                            name = item.text() ?: "",
                                            location = item.placeName() ?: "",
                                            wikiData = item.properties()?.get("wikidata")?.asString,
                                            onClick = View.OnClickListener {
                                                // TODO: Navigate to selected place.
                                            }
                                    ))
                                }

                                viewAdapter.notifyDataSetChanged()
                            } else {
                                renderErrorMessage(R.string.destination_search_no_result)
                            }
                        }

                        override fun onFailure(call: Call<GeocodingResponse>, throwable: Throwable) {
                            Timber.e(throwable, "Error occurred when trying to search for query: $call")
                            renderErrorMessage(R.string.destination_search_failed)
                        }
                    })
                }
                .addOnFailureListener {
                    Timber.e(it, "Error occurred when trying to get the last known location.")
                    renderErrorMessage(R.string.fail_to_get_last_location)
                }
    }

    private fun renderErrorMessage(text: Int) {
        destinations.clear()
        destinations.add(DestinationRVErrorMessage(resources.getString(text)))

        viewAdapter.notifyDataSetChanged()
    }

}
