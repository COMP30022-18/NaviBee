package au.edu.unimelb.eng.navibee.sos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import com.app.progresviews.ProgressWheel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.PrivateConversation;


public class SosActivity extends AppCompatActivity {

    private CountDownTimer countDownTimer;
    private ProgressWheel countDownView;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        checkPhoneCallPermission();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        countDownView = findViewById(R.id.sos_progress);

        countDownTimer = new CountDownTimer(10 * 1000, 1000) {
            @Override
            public void onTick(long l) {
                Long val = l / 1000 + 1;
                int angle = (int) ((float) val / 10 * 360);
                countDownView.setStepCountText(Long.toString(val));
                countDownView.setPercentage(angle);
            }

            @Override
            public void onFinish() {
                triggerSOS();
            }
        };

        countDownTimer.start();
    }

    public void notifyOnClick(View view) {
        countDownTimer.cancel();
        triggerSOS();
    }

    public void cancelOnClick(View view) {
        cancel();
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    private void cancel() {
        countDownTimer.cancel();
        finish();
    }

    private void triggerSOS() {

        String phoneNumber = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .getString("sos_emergency_call", "");
        String contactUid = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .getString("sos_emergency_contact", "");

        if (!contactUid.isEmpty()) {

            // emergency message
            PrivateConversation conv = ConversationManager.getInstance().getPrivateConversation(contactUid);
            conv.sendMessage("text", "Emergency!");

            // location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_LONG).show();
                } else {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            conv.sendLocation(location.getLatitude(), location.getLongitude());
                        }
                    });
                }
            }

        }

        // check digit only
        if (!phoneNumber.isEmpty() && !android.text.TextUtils.isDigitsOnly(phoneNumber)) {
            Toast.makeText(this, "Digits Only!", Toast.LENGTH_SHORT).show();
        } else {
            // emergency phone call
            Intent callIntent = new Intent(Intent.ACTION_CALL);

            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Unable to make phone call", Toast.LENGTH_LONG).show();
            } else {
                startActivity(callIntent);
            }
        }

        finish();
    }

    // Check the phone call permission
    public void checkPhoneCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
    }

}