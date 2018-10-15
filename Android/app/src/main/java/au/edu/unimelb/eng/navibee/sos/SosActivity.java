package au.edu.unimelb.eng.navibee.sos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.PrivateConversation;
import au.edu.unimelb.eng.navibee.utils.CircularProgressView;


public class SosActivity extends AppCompatActivity {

    private CountDownTimer countDownTimer;
    private CircularProgressView countDownView;
    private TextView countDownText;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        // If activity is called by fall detection
        String isEnabled = getIntent().getStringExtra("fall_detection");

        if (isEnabled != null && isEnabled.equals("Enable")) {
            TextView textView = findViewById(R.id.sos_countdown_pre_text);
            textView.setText(R.string.sos_pre_text);
        }

        checkPhoneCallPermission();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        countDownView = findViewById(R.id.sos_progress);
        countDownText = findViewById(R.id.sos_progress_text);
        countDownView.setMax(10 * 1000);
        countDownView.setProgress(10 * 1000);
        countDownView.setAnimationInterpolator(new LinearInterpolator());

        countDownTimer = new CountDownTimer(10 * 1000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long l) {
                countDownText.setText(Long.toString(l / 1000 + 1));
                countDownView.setProgress(l, true, 1000);
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

        PrivateConversation conv = ConversationManager.getInstance().getPrivateConversation(contactUid);
        if (conv != null) {

            // emergency message

            conv.sendMessage("text", getString(R.string.sos_emergency_message));

            // location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.sos_emergency_no_location), Toast.LENGTH_LONG).show();
                } else {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            conv.sendLocation(location.getLatitude(), location.getLongitude());
                            Toast.makeText(this, getString(R.string.sos_emergency_sent_location), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        }

        // check digit only
        if ((!phoneNumber.isEmpty() && !android.text.TextUtils.isDigitsOnly(phoneNumber)) || phoneNumber.isEmpty()) {
            Toast.makeText(this, getString(R.string.sos_emergency_phone), Toast.LENGTH_SHORT).show();
        } else {
            // emergency phone call
            Intent callIntent = new Intent(Intent.ACTION_CALL);

            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.sos_emergency_no_number), Toast.LENGTH_LONG).show();
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