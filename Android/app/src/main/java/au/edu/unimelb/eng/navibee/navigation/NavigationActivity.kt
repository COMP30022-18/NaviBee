package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import au.edu.unimelb.eng.navibee.BuildConfig
import au.edu.unimelb.eng.navibee.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.services.android.navigation.ui.v5.NavigationView
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener
import com.mapbox.services.android.navigation.v5.milestone.Milestone
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import kotlinx.android.synthetic.main.activity_navigation.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Navigate to a place.
 *
 * Required extra information:
 *      EXTRA_DEST_LAT: Double, latitude of destination
 *      EXTRA_DEST_LON: Double, longitude of destination
 *      EXTRA_MEAN_OF_TRAVEL: String, the preferred travel method
 *          one of MEAN_DRIVING, MEAN_WALKING, MEAN_CYCLING
 */
class NavigationActivity : AppCompatActivity(), MilestoneEventListener,
        NavigationListener, OnNavigationReadyCallback {

    companion object {
        const val EXTRA_DEST_LAT = "destinationLatitude"
        const val EXTRA_DEST_LON = "destinationLongitude"
        const val EXTRA_MEAN_OF_TRAVEL = "destinationMeanOfTravel"
        const val MEAN_DRIVING = DirectionsCriteria.PROFILE_DRIVING
        const val MEAN_WALKING = DirectionsCriteria.PROFILE_WALKING
        const val MEAN_CYCLING = DirectionsCriteria.PROFILE_CYCLING
    }

    private lateinit var navigation: MapboxNavigation

    private lateinit var navigationView: NavigationView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, BuildConfig.MAPBOX_API_TOKEN)

        setContentView(R.layout.activity_navigation)

        // initialize location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Get destination location
        val (destLat, destLon, mean) = getDestinationDetails()
        val destination = Point.fromLngLat(destLon, destLat)

        // Initialize navigation view
        navigationView = navigation_navigation_navigation_view
        navigationView.initialize(this)

        navigation = MapboxNavigation(this, BuildConfig.MAPBOX_API_TOKEN)

        val locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        navigation.locationEngine = locationEngine

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            finish()
        }


        // Retrieve current location
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            val origin = Point.fromLngLat(loc.longitude, loc.latitude)

            // Request a route
            NavigationRoute.builder(this)
                    .accessToken(Mapbox.getAccessToken()!!)
                    .origin(origin)
                    .destination(destination)
                    .voiceUnits(DirectionsCriteria.METRIC)
                    .profile(mean)
                    .build()
                    .getRoute(object : Callback<DirectionsResponse> {
                        override fun onResponse(call: Call<DirectionsResponse>,
                                                response: Response<DirectionsResponse>) {
                            val route = response.body()?.routes()?.get(0) ?: return
                            startNavigation(route)
                        }

                        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                            Timber.e(t, "Error occurred on getting a route: $call")
                        }
                    })
        }

        navigation.addMilestoneEventListener(this)

        navigation.addNavigationEventListener { running ->
            Timber.v("Navigation is now ${if (running) "Running" else "not running"}.")
        }

    }

    private fun getDestinationDetails(): Triple<Double, Double, String> {
        if (!intent.hasExtra(EXTRA_DEST_LAT) ||
                !intent.hasExtra(EXTRA_DEST_LON) ||
                !intent.hasExtra(EXTRA_MEAN_OF_TRAVEL))
            finish()

        val destLat = intent.getDoubleExtra(EXTRA_DEST_LAT, 0.0)
        val destLon = intent.getDoubleExtra(EXTRA_DEST_LON, 0.0)
        val mean = intent.getStringExtra(EXTRA_MEAN_OF_TRAVEL)

        return Triple(destLat, destLon, mean)
    }

    override fun onStart() {
        super.onStart()
        navigationView.onStart()
    }

    override fun onResume() {
        super.onResume()
        navigationView.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        navigationView.onLowMemory()
    }

    override fun onBackPressed() {
        if (!navigationView.onBackPressed())
            super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        navigationView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        navigationView.onRestoreInstanceState(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        navigationView.onPause()
    }

    override fun onStop() {
        super.onStop()
        navigationView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        navigation.stopNavigation()
        navigation.onDestroy()
        navigationView.onDestroy()
    }

    override fun onNavigationReady(isRunning: Boolean) {

    }

    override fun onNavigationFinished() {
        finish()
    }

    override fun onNavigationRunning() {
        // Intentionally empty
    }

    override fun onCancelNavigation() {
        finish()
    }

    private fun startNavigation(route: DirectionsRoute) {
        navigationView.startNavigation(
                NavigationViewOptions.builder()
                        .directionsRoute(route)
                        .shouldSimulateRoute(true)
                        .build()
                )
    }

    override fun onMilestoneEvent(routeProgress: RouteProgress?, instruction: String?, milestone: Milestone?) {

    }

}
