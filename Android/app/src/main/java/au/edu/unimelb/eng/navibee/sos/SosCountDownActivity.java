package au.edu.unimelb.eng.navibee.sos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.PrivateConversation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class SosCountDownActivity extends AppCompatActivity {

    private CountDownTimer countDownTimer;
    private TextView countDownTextView;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_count_down);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        countDownTextView = findViewById(R.id.sos_countdown_number);

        countDownTimer = new CountDownTimer(10 * 1000, 1000) {
            @Override
            public void onTick(long l) {
                countDownTextView.setText(Long.toString(l / 1000 + 1));
            }

            @Override
            public void onFinish() {
                triggerSOS();
            }
        };

        countDownTimer.start();
    }

    public void notifyOnClick(View view) {
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

        String phoneNumber = "123";
        String enmergecyContactUid = "bEQ9x53aA6TFttXxE6QBHsyIW4u2";


        // emergency message
        PrivateConversation conv = ConversationManager.getInstance().getPrivateConversation(enmergecyContactUid);
        conv.sendMessage("text", "Emergency!");

        // location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_LONG).show();
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location!=null) {
                       conv.sendLocation(location.getLatitude(), location.getLongitude());
                    }
                });
            }
        }


        // emergency phone call
        Intent callIntent = new Intent(Intent.ACTION_CALL);

        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Unable to make phone call", Toast.LENGTH_LONG).show();
        } else {
            startActivity(callIntent);
        }
        
        
        finish();
    }
}
