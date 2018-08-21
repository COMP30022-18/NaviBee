package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatDialogFragment
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
import org.jetbrains.anko.bundleOf
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
class DestinationsSearchResultActivity: AppCompatActivity(), SearchResultRetryListener {

    companion object {
        const val CHECK_LOCATION_PERMISSION = 1
        const val ARGS_SEND_RESULT = "sendResult"
    }

    // Location service
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    // incoming parameters
    private var sendResult: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set incoming parameters
        sendResult = intent.getBooleanExtra(ARGS_SEND_RESULT, false)

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

        val progressDialog: Dialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val builder = AlertDialog.Builder(this)
            builder.setView(R.layout.layout_indefinite_progress_dialog)
            progressDialog = builder.create()
        } else {
            progressDialog = ProgressDialog(this).apply {
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
            }
        }
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

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
                                // hide progress dialog
                                progressDialog.hide()

                                // Log the first results Point.
                                val firstResultPoint = results[0].center()
                                Timber.d("onResponse: $firstResultPoint (lat ${firstResultPoint!!.latitude()}, long ${firstResultPoint.longitude()})")

                                // Popup to confirm the location (debug only)
                                SearchResultFragment().let {
                                    it.arguments = bundleOf(
                                            SearchResultFragment.ARGS_LOCATION to results[0],
                                            ARGS_SEND_RESULT to sendResult
                                    )
                                    it.show(supportFragmentManager, "searchResult")
                                }
                            } else {
                                // hide progress dialog
                                progressDialog.hide()

                                Snackbar.make(findViewById(R.id.destinations_activity_coordinator_layout),
                                        R.string.destination_search_no_result,
                                        Snackbar.LENGTH_LONG).show()
                                finish()
                            }
                        }

                        override fun onFailure(call: Call<GeocodingResponse>, throwable: Throwable) {
                            Timber.e(throwable, "Error occurred when trying to search for query: $call")
                            // hide progress dialog
                            progressDialog.hide()

                            Snackbar.make(findViewById(R.id.destinations_activity_coordinator_layout),
                                    R.string.destination_search_failed,
                                    Snackbar.LENGTH_LONG).show()
                            finish()
                        }
                    })
                }
                .addOnFailureListener {
                    Timber.e(it, "Error occurred when trying to get the last known location.")

                    // hide progress dialog
                    progressDialog.hide()

                    Snackbar.make(findViewById(R.id.destinations_activity_coordinator_layout),
                            R.string.fail_to_get_last_location,
                            Snackbar.LENGTH_LONG).show()
                    finish()
                }
    }

    override fun onSearchResultRetry(dialog: AppCompatDialogFragment) {
        // == true is to avoid getBoolean return null
        if (dialog.arguments?.getBoolean(ARGS_SEND_RESULT) == true) {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    override fun onSearchResultCancel() {
        finish()
    }

    override fun onSearchResultOK() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


interface SearchResultRetryListener {
    fun onSearchResultRetry(dialog: AppCompatDialogFragment)
    fun onSearchResultCancel()
    fun onSearchResultOK()
}

/**
 * Arguments:
 *     ARGS_LOCATION: CarmenFeature, the mapbox location required
 */
class SearchResultFragment: AppCompatDialogFragment() {

    companion object {
        const val ARGS_LOCATION = "location"
    }

    private lateinit var searchResultRetryListener: SearchResultRetryListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val location = this.arguments?.getSerializable(ARGS_LOCATION) as CarmenFeature
        builder.let { it ->
            it.setTitle(R.string.destination_search_result_title)
            it.setMessage("${location.placeName()} (${location.text()}) at ${location.center()}")
            it.setPositiveButton(R.string.button_go) { _, _ ->
                searchResultRetryListener.onSearchResultOK()
            }
            it.setNegativeButton(R.string.button_retry) { _, _ ->
                this@SearchResultFragment.dialog.cancel()
                searchResultRetryListener.onSearchResultRetry(this)
            }
        }

        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface?) {
        searchResultRetryListener.onSearchResultCancel()
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