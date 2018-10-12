package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
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

public class LocationDisplayActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM_LEVEL = 15.0f;
    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_TIME = "time";

    private double lat, lon;
    private Date time;
    private UserInfoManager.UserInfo user;
    private MapView mapView;

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
