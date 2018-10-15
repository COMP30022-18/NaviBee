package au.edu.unimelb.eng.navibee.social;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.navigation.NavigationSelectorActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.navigation.NavigationSelectorActivity;
import au.edu.unimelb.eng.navibee.utils.URLImageViewCacheLoader;
import kotlin.reflect.jvm.internal.impl.load.java.Constant;

import static au.edu.unimelb.eng.navibee.utils.DimensionsUtilitiesKt.getStatusBarHeight;

public class LocationDisplayActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM_LEVEL = 15.0f;

    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_TIME = "time";

    private double lat, lon;
    private Date time;
    private UserInfoManager.UserInfo user;
    private MapView mapView;
    private GoogleMap googleMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_display);

        lat = getIntent().getDoubleExtra(NavigationSelectorActivity.EXTRA_LATITUDE, 0.0);
        lon = getIntent().getDoubleExtra(NavigationSelectorActivity.EXTRA_LONGITUDE, 0.0);
        time = (Date) getIntent().getSerializableExtra(EXTRA_TIME);

        if (getIntent().hasExtra(EXTRA_SENDER))
            user = getIntent().getParcelableExtra(EXTRA_SENDER);

        mapView = findViewById(R.id.displayLocation_mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_FINE_LOCATION);
        }

        TextView title = findViewById(R.id.chat_locationDisplay_title);
        TextView subtitle = findViewById(R.id.chat_locationDisplay_subtitle);
        ImageView icon = findViewById(R.id.chat_locationDisplay_icon);

        CharSequence timeStr = DateUtils.getRelativeTimeSpanString(time.getTime());

        if (user != null) {
            title.setText(user.getName());
            subtitle.setText(timeStr);
            new URLImageViewCacheLoader(user.getPhotoUrl(), icon).roundImage(true).execute();

        } else {
            title.setText(timeStr);
            subtitle.setVisibility(View.GONE);
            icon.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (googleMap!=null) {
                        googleMap.setMyLocationEnabled(true);
                    }

                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng location = new LatLng(lat, lon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED)) {
                googleMap.setMyLocationEnabled(true);
            }
        }
        googleMap.setPadding(0, getStatusBarHeight(mapView), 0, 0);

        googleMap.getUiSettings().setMapToolbarEnabled(false);

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
