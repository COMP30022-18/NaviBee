package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.content.ContextCompat
import au.edu.unimelb.eng.navibee.BuildConfig
import au.edu.unimelb.eng.navibee.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.StreetViewPanoramaOptions
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.StreetViewPanoramaCamera
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
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions
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

        private const val PANORAMA_ANIMATION_DURATION = 500L
        private const val PANORAMA_DISPLAY_DURATION = 10000L

        private const val STREETVIEW_BUNDLE_KEY = "StreetViewBundleKey"

        private val routeUtils = RouteUtils()
    }

    private lateinit var navigationView: NavigationView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var route: DirectionsRoute? = null
    private var notification: NaviBeeMapBoxNotification? = null

    private lateinit var panorama: StreetViewPanorama
    private var navigationPanorama: StreetViewPanoramaView? = null
    private var panoramaExpanded = false
    private val handler = Handler()

    private var showStreetView: Boolean = true
    private var useSimulation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, BuildConfig.MAPBOX_API_TOKEN)

        setContentView(R.layout.activity_navigation)

        // initialize location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize navigation view
        navigationView = navigation_navigation_navigation_view

        if (isNightModeEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#324148")
        }

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            finish()
        }

        navigationView.onCreate(savedInstanceState)
        navigationView.initialize(this)
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

    private fun onStreetViewPanoramaReady(p0: StreetViewPanorama?) {
        panorama = p0 ?: return
        panorama.isUserNavigationEnabled = false

        // Street view image setup
        val instructionLayout =
                findViewById<ViewGroup>(R.id.navigation_navigation_navigation_view)
                        .findViewById<ViewGroup>(R.id.instructionView)
                        .findViewById<View>(R.id.instructionLayout)
        val instructionHeight = instructionLayout.height

        navigationPanorama?.layoutParams = ConstraintLayout
                .LayoutParams(0, 0).apply {
                    topMargin = instructionHeight
                    height = dpToPx(200)
                    width = MATCH_CONSTRAINT
                    startToStart = PARENT_ID
                    topToTop = PARENT_ID
                    endToEnd = PARENT_ID
                }

        navigation_navigation_constraint.addView(navigationPanorama)

        navigationPanorama?.apply {
             scaleY = 0f
             pivotY = 0f
        }

    }

    private fun dpToPx(dp: Int): Int {
        val density = applicationContext.resources
                .displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    override fun finish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else {
            super.finish()
        }
    }

    override fun onStart() {
        super.onStart()
        navigationView.onStart()
    }

    override fun onResume() {
        super.onResume()
        navigationView.onResume()
        navigationPanorama ?.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        navigationView.onLowMemory()
    }

    override fun onBackPressed() {
        if (!navigationView.onBackPressed()) {
            moveTaskToBack(true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        navigationView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)

        var mStreetViewBundle = outState?.getBundle(STREETVIEW_BUNDLE_KEY)

        if (mStreetViewBundle == null) {
            mStreetViewBundle = Bundle()
            outState?.putBundle(STREETVIEW_BUNDLE_KEY, mStreetViewBundle)
        }

        navigationPanorama?.onSaveInstanceState(mStreetViewBundle)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        navigationView.onRestoreInstanceState(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        navigationView.onPause()
        navigationPanorama?.onPause()
    }

    override fun onStop() {
        super.onStop()
        navigationView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        notification?.unregisterReceiver(this)
        navigationView.onDestroy()
        navigationPanorama?.onDestroy()
    }

    @SuppressLint("MissingPermission")
    override fun onNavigationReady(isRunning: Boolean) {

        val context = this

        // Get destination location
        val (destLat, destLon, mean) = getDestinationDetails()
        val destination = Point.fromLngLat(destLon, destLat)

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

                            if (showStreetView) {
                                navigationPanorama = StreetViewPanoramaView(context,
                                        StreetViewPanoramaOptions().position(LatLng(destLat, destLon))
                                                .userNavigationEnabled(false))

                                navigationPanorama?.onCreate(null)
                                navigationPanorama?.getStreetViewPanoramaAsync {
                                    onStreetViewPanoramaReady(it)
                                }
                            }

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

        showStreetView = shouldShowStreetView(this)
        useSimulation = shouldUseSimulation(this)

        notification = NaviBeeMapBoxNotification(
                this,
                navigationView
        )

        val navigationOptions = MapboxNavigationOptions.builder()
                .navigationNotification(
                        notification
                )
                .build()

        navigationView.startNavigation(
                NavigationViewOptions.builder().let {
                    it.directionsRoute(route)
                    it.navigationListener(this)
                    it.navigationOptions(navigationOptions)
                    it.milestoneEventListener(this)
                    it.navigationListener(this)
                    it.shouldSimulateRoute(useSimulation)
                    it.build()
                })
    }

    override fun onMilestoneEvent(routeProgress: RouteProgress?, instruction: String?, milestone: Milestone?) {
        if (routeProgress != null && milestone != null) {
            if (routeUtils.isArrivalEvent(routeProgress, milestone)) {
                finish()
            } else {
                if (showStreetView) {
                    val intersection = routeProgress
                            .currentLegProgress()
                            .currentStepProgress()
                            .currentIntersection()
                    val pt = intersection.location()
                    panorama.setPosition(
                            LatLng(pt.latitude(), pt.longitude())
                    )
                    if (panorama.location != null) {
                        val bearing = intersection.bearings()
                                ?.get(intersection?.`in`() ?: 0)?.toFloat() ?: 0f
                        panorama.animateTo(
                                StreetViewPanoramaCamera.Builder()
                                        .zoom(0f).tilt(0f).bearing(bearing).build(),
                                PANORAMA_ANIMATION_DURATION
                        )
                        showPanorama()
                    } else {
                        hidePanorama()
                    }
                }
            }
        }
    }

    private fun showPanorama() {
        handler.removeCallbacks(hidePanoramaRunnable)
        if (!panoramaExpanded) {
            panoramaExpanded = true
            navigationPanorama
                    ?.animate()
                    ?.setDuration(PANORAMA_ANIMATION_DURATION)
                    ?.scaleY(1.0f)
        }
        handler.postDelayed(hidePanoramaRunnable, PANORAMA_DISPLAY_DURATION)
    }

    private val hidePanoramaRunnable: Runnable = Runnable {
        panoramaExpanded = false
        navigationPanorama
                ?.animate()
                ?.setDuration(PANORAMA_ANIMATION_DURATION)
                ?.scaleY(0f)
    }

    private fun hidePanorama() {
        handler.removeCallbacks(hidePanoramaRunnable)
        hidePanoramaRunnable.run()
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