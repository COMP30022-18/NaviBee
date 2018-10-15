package au.edu.unimelb.eng.navibee.social;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.maps.android.ui.IconGenerator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.URLImageViewCacheLoader;

import static au.edu.unimelb.eng.navibee.utils.DimensionsUtilitiesKt.getStatusBarHeight;

public class RealTimeLocationDisplayActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_CONVID = "convID";

    private static final float DEFAULT_ZOOM_LEVEL = 15.0f;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static final int UPDATE_INTERVAL = 4 * 1000;
    private static final long EXPIRE_TIME = 2 * 60 * 1000;

    private PrivateConversation conv;
    private String userName;

    private LatLng location = null;
    private LatLng myLocation = null;
    private Date lastUpdate = new Date();
    Marker marker = null;

    private MapView mapView;
    private GoogleMap googleMap = null;
    private Timer updateTimer = new Timer();

    private ListenerRegistration listener;


    TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(() -> update(true));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_display);

        mapView = findViewById(R.id.displayLocation_mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_FINE_LOCATION);
            }
        }

        findViewById(R.id.displayLocation_fab).setVisibility(View.GONE);

        updateTimer.scheduleAtFixedRate(updateTask,0, UPDATE_INTERVAL);


        TextView title = findViewById(R.id.chat_locationDisplay_title);
        TextView subtitle = findViewById(R.id.chat_locationDisplay_subtitle);
        ImageView icon = findViewById(R.id.chat_locationDisplay_icon);

        subtitle.setVisibility(View.GONE);


        conv = (PrivateConversation) ConversationManager.getInstance().getConversation(getIntent().getStringExtra(EXTRA_CONVID));
        UserInfoManager.getInstance().getUserInfo(conv.getTargetUid(),(userInfo -> {
            userName = userInfo.getName();
            if (marker!=null) marker.setIcon(BitmapDescriptorFactory.fromBitmap(new IconGenerator(this).makeIcon(userName)));
            title.setText(userName);
            new URLImageViewCacheLoader(userInfo.getPhotoUrl(), icon).roundImage(true).execute();
            update(false);
        }));


        listener = FirebaseFirestore.getInstance()
                .collection("conversations")
                .document(conv.conversationId)
                .collection("realtimeLocations")
                .document(conv.getTargetUid()).addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("RealtimeLocation", "listen:error", e);
                        return;
                    }

                    if (snapshots.exists()) {
                        Map<String, Object> data = snapshots.getData();
                        location = new LatLng((double) data.get("latitude"), (double) data.get("longitude"));
                        lastUpdate = ((Timestamp)data.get("time")).toDate();
                    } else {
                        if (marker != null) {
                            marker.remove();
                            location = null;
                        }
                    }

        });


    }

    private void update(boolean timer) {
        if (googleMap == null) return;
        if (location != null) {
            long oneAgo = System.currentTimeMillis() - EXPIRE_TIME;
            if (lastUpdate.getTime() < oneAgo) {
                if (marker != null) {
                    marker.remove();
                }
            } else {
                if (marker == null) {
                    marker = googleMap.addMarker(new MarkerOptions().position(location));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(new IconGenerator(this).makeIcon(userName)));
                } else {
                    marker.setPosition(location);
                }


            }
        }

        if (timer && myLocation!=null) {
            // update my location
            Map<String, Object> data = new HashMap<>();
            data.put("latitude", myLocation.latitude);
            data.put("longitude", myLocation.longitude);
            data.put("time", Timestamp.now());

            FirebaseFirestore.getInstance()
                    .collection("conversations")
                    .document(conv.conversationId)
                    .collection("realtimeLocations")
                    .document(FirebaseAuth.getInstance().getUid()).set(data);
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (googleMap != null) {
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
        listener.remove();

        FirebaseFirestore.getInstance()
                .collection("conversations")
                .document(conv.conversationId)
                .collection("realtimeLocations")
                .document(FirebaseAuth.getInstance().getUid()).delete();

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

        LocationServices.getFusedLocationProviderClient(this).getLastLocation()
        .addOnSuccessListener(location -> {
            if (location == null) return;
            LatLng currentLatLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
                    10));
        });

        update(false);

        googleMap.setOnMyLocationChangeListener(location1 -> {
            boolean firstTime = myLocation == null;
            myLocation = new LatLng(location1.getLatitude(), location1.getLongitude());
            if (firstTime) {
                update(true);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            }
        });
    }

    public void onFabClick(final View view) {
    }


}
