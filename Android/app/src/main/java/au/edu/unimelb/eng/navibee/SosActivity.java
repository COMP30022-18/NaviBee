package au.edu.unimelb.eng.navibee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SosActivity extends AppCompatActivity {

    private static final int REQUEST_PHONE_CALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        Button callButton = findViewById(R.id.callButton);
        EditText phoneNumber = findViewById(R.id.phoneNumber);

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
