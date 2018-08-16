package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import au.edu.unimelb.eng.navibee.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import org.jetbrains.anko.startActivityForResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class DestinationsSearchResultActivity: AppCompatActivity(), SearchResultRetryListener {

    companion object {
        const val CHECK_LOCATION_PERMISSION = 1
        const val VOICE_SEARCH_REQUEST = 2
    }

    // Location service
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY)
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
                    searchForLocation(data!!.getStringExtra("query"), false)
            }
        }
    }

    private fun searchForLocation(query: String, isVoice: Boolean) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return

        fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    this.lastKnownLocation = location
                    Timber.d("$location")
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

                                val bundle = Bundle()
                                bundle.putSerializable("location", results[0])
                                bundle.putBoolean("isVoice", isVoice)

                                // Popup to confirm the location (debug only)
                                SearchResultFragment().let {
                                    it.arguments = bundle
                                    it.show(supportFragmentManager, "searchResult")
                                }
                            } else {
                                // No result for your request were found.
                                Timber.d("onResponse: No result found")
                            }
                        }

                        override fun onFailure(call: Call<GeocodingResponse>, throwable: Throwable) {
                            throwable.printStackTrace()
                        }
                    })
                }
    }

    override fun onSearchResultRetry(dialog: DialogFragment) {
        // == true is to avoid getBoolean return null
        if (dialog.arguments?.getBoolean("isVoice") == true) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}


interface SearchResultRetryListener {
    fun onSearchResultRetry(dialog: DialogFragment)
}

class SearchResultFragment: DialogFragment() {

    private lateinit var searchResultRetryListener: SearchResultRetryListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val location = this.arguments?.getSerializable("location") as CarmenFeature
        builder.let {
            it.setTitle(R.string.we_have_found)
            it.setMessage("${location.placeName()} (${location.text()}) at ${location.center()}")
            it.setPositiveButton(R.string.button_go) { dialog, id ->
                TODO("not implemented")
            }
            it.setNegativeButton(R.string.button_retry) { dialog, id ->
                this@SearchResultFragment.dialog.cancel()
                searchResultRetryListener.onSearchResultRetry(this)
            }
        }

        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            searchResultRetryListener = context as SearchResultRetryListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement SearchResultRetryListener")
        }
    }
}