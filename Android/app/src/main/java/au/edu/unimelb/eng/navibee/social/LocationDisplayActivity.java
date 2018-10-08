package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.navigation.NavigationSelectorActivity;

public class LocationDisplayActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM_LEVEL = 15.0f;

    private double lat, lon;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_display);

        lat = getIntent().getDoubleExtra(NavigationSelectorActivity.EXTRA_LATITUDE, 0.0);
        lon = getIntent().getDoubleExtra(NavigationSelectorActivity.EXTRA_LONGITUDE, 0.0);

        mapView = findViewById(R.id.displayLocation_mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = new LatLng(lat, lon);
        googleMap.addMarker(new MarkerOptions().position(location));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    public void onFabClick(final View view) {
        Intent intent = new Intent(getBaseContext(), NavigationSelectorActivity.class);
        intent.putExtra(NavigationSelectorActivity.EXTRA_LATITUDE, lat);
        intent.putExtra(NavigationSelectorActivity.EXTRA_LONGITUDE, lon);

        startActivity(intent);
    }


}
