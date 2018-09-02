package au.edu.unimelb.eng.navibee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SosActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    private FirebaseFirestore db;
    private String uid;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        TextView phoneText = findViewById(R.id.phoneText);

        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                // check emergency field exists
                if (document.contains("emergency")) {
                    String emergency = document.getString("emergency");

                    // check field is empty string
                    if (emergency.isEmpty()) {
                        goSettingActivity();
                    } else {
                        phoneText.setText(emergency);
                    }

                } else {
                    // merge data to avoid overwriting
                    Map<String, Object> data = new HashMap<>();
                    data.put("emergency", "");
                    db.collection("users").document(uid).set(data, SetOptions.merge());

                    goSettingActivity();
                    checkPhoneCallPermission();
                }

            } else {
                Toast.makeText(SosActivity.this, "Load data fails", Toast.LENGTH_LONG).show();
            }
        });

        makePhoneCall(phoneText);

        startSosSettingActivity();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("emergency");

                TextView phoneText = findViewById(R.id.phoneText);
                phoneText.setText(result);

            }
        }
    }

    private void goSettingActivity() {
        Toast.makeText(SosActivity.this, "Go setting to add emergency contact",
                Toast.LENGTH_LONG).show();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        startActivity(new Intent(this, SosSettingActivity.class));
        startActivityForResult(new Intent(this, SosSettingActivity.class), REQUEST_CODE);
    }

    private void checkPhoneCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},REQUEST_CODE);
        }
    }

    private void makePhoneCall(TextView phoneText) {
        Button callButton = findViewById(R.id.callButton);

        callButton.setOnClickListener((View view) -> {

            Intent callIntent = new Intent(Intent.ACTION_CALL);

            callIntent.setData(Uri.parse("tel:" + phoneText.getText().toString()));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE);
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
//                startActivity(settingIntent);
                startActivityForResult(settingIntent, REQUEST_CODE);
            }
        });
    }

}
