package au.edu.unimelb.eng.navibee.navigation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import au.edu.unimelb.eng.navibee.BuildConfig
import au.edu.unimelb.eng.navibee.R
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response




class NavigationActivity : AppCompatActivity() {

    private lateinit var navigation: MapboxNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        navigation = MapboxNavigation(this, BuildConfig.MAPBOX_API_TOKEN)
        val locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        navigation.locationEngine = locationEngine

        // Request a route
        val origin = Point.fromLngLat(-77.03613, 38.90992)
        val destination = Point.fromLngLat(-77.0365, 38.8977)

        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken()!!)
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(object : Callback<DirectionsResponse> {
                    override fun onResponse(call: Call<DirectionsResponse>,
                                            response: Response<DirectionsResponse>) {

                    }

                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {

                    }
                })

        navigation.addNavigationEventListener { running ->

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        navigation.stopNavigation()
        navigation.onDestroy()
    }
}
