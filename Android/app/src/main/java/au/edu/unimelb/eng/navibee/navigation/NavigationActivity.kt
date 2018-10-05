package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import au.edu.unimelb.eng.navibee.BuildConfig
import au.edu.unimelb.eng.navibee.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import com.mapbox.services.android.navigation.v5.utils.RouteUtils
import kotlinx.android.synthetic.main.activity_navigation.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
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

        private val routeUtils = RouteUtils()
    }

    private lateinit var navigationView: NavigationView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isMapReady = false
    private var route: DirectionsRoute? = null

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

        if (isNightModeEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#324148")
        }

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
                            route = response.body()?.routes()?.get(0)
                            if (route == null) {
                                alert(R.string.navigation_failed_to_fetch_route) {
                                    okButton {
                                        finish()
                                    }
                                }.show()
                                return
                            }
                            if (isMapReady)
                                startNavigation(route!!)
                        }

                        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                            alert(R.string.navigation_connection_error) {
                                okButton {
                                    finish()
                                }
                            }.show()
                            Timber.e(t, "Error occurred on getting a route: $call")
                        }
                    })
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
        navigationView.onDestroy()
    }

    override fun onNavigationReady(isRunning: Boolean) {
        isMapReady = true
        if (route != null)
            startNavigation(route!!)
    }

    override fun onNavigationFinished() {
        // Intentionally empty
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
                        .navigationListener(this)
                        // .shouldSimulateRoute(true)
                        .milestoneEventListener(this)
                        .navigationListener(this)
                        .build()
                )
    }

    override fun onMilestoneEvent(routeProgress: RouteProgress?, instruction: String?, milestone: Milestone?) {
        if (routeProgress != null && milestone != null
                && routeUtils.isArrivalEvent(routeProgress, milestone)) {
            finish()
        }
    }

    private fun isNightModeEnabled(): Boolean {
        if (isNightModeFollowSystem()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        }
        val uiMode = retrieveCurrentUiMode()
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun isNightModeFollowSystem(): Boolean {
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        return nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    private fun retrieveCurrentUiMode(): Int {
        return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    }

}