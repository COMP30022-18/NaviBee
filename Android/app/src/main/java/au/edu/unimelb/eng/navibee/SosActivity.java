package au.edu.unimelb.eng.navibee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SosActivity extends AppCompatActivity {

    private static final int REQUEST_PHONE_CALL = 1;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        TextView phoneNumber = findViewById(R.id.phoneNumber);

        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                String emergency = document.getString("emergency");
                phoneNumber.setText(emergency);
            } else {
                // TODO if fails
            }
        });

        makePhoneCall(phoneNumber);

        startSosSettingActivity();

    }

    private void makePhoneCall(TextView phoneNumber) {
        Button callButton = findViewById(R.id.callButton);

        callButton.setOnClickListener((View view) -> {

            Intent callIntent = new Intent(Intent.ACTION_CALL);

            callIntent.setData(Uri.parse("tel:" + phoneNumber.getText().toString()));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
            } else {
                startActivity(callIntent);
            }
        });
    }

    private void startSosSettingActivity() {
        LinearLayout sosSetting = findViewById(R.id.settingLayout);
        Intent settingIntent = new Intent(this, SosSettingActivity.class);

        sosSetting.setOnClickListener(new LinearLayout.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(settingIntent);
            }
        });
    }

}
