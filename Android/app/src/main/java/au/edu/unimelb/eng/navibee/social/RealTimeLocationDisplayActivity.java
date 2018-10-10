package au.edu.unimelb.eng.navibee.social;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;

public class RealTimeLocationDisplayActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_CONVID = "convID";

    private static final float DEFAULT_ZOOM_LEVEL = 15.0f;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static final int UPDATE_INTERVAL = 3 * 1000;
    private static final long EXPIRE_TIME = 1 * 10 * 1000;

    private PrivateConversation conv;
    private String userName;

    private LatLng location = null;
    private Date lastUpdate = new Date();
    Marker marker = null;

    private MapView mapView;
    private GoogleMap googleMap = null;
    private Timer updateTimer = new Timer();

    TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(() -> {
                update();
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_display);

        mapView = findViewById(R.id.displayLocation_mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_FINE_LOCATION);
        }

        findViewById(R.id.displayLocation_fab).setVisibility(View.GONE);

        updateTimer.scheduleAtFixedRate(updateTask,0, UPDATE_INTERVAL);

        conv = (PrivateConversation) ConversationManager.getInstance().getConversation(getIntent().getStringExtra(EXTRA_CONVID));
        UserInfoManager.getInstance().getUserInfo(conv.getTargetUid(),(userInfo -> {
            userName = userInfo.getName();
            update();
        }));
    }

    private void update() {
        if (googleMap==null) return;
        if (location!=null) {
            long oneAgo = System.currentTimeMillis() - EXPIRE_TIME;
            if (lastUpdate.getTime() < oneAgo) {
                if (marker!=null) {
                    marker.remove();
                }
            } else {
                if (marker==null) {
                    marker = googleMap.addMarker(new MarkerOptions().position(location));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
                } else {
                    marker.setPosition(location);
                }

                marker.setIcon(BitmapDescriptorFactory.fromBitmap(new IconGenerator(this).makeIcon(userName)));
            }
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
        updateTimer.cancel();
        updateTimer.purge();
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

        if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.getUiSettings().setMapToolbarEnabled(false);

        location = new LatLng(-37.8136, 144.9631);
        lastUpdate = new Date();
        update();
    }

    public void onFabClick(final View view) {
    }


}
