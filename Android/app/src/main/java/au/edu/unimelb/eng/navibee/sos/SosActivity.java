package au.edu.unimelb.eng.navibee.sos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import au.edu.unimelb.eng.navibee.R;

public class SosActivity extends AppCompatActivity {
    // TODO magic string

    private static final int REQUEST_CODE = 1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.sos_appbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.sos_toolbar);
        View toolbarPadding = (View) findViewById(R.id.sos_toolbar_padding);

        // Action Bar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // Set padding for status bar
        // Require API 20
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            toolbarPadding.setOnApplyWindowInsetsListener((view, windowInsets) -> {
                ViewGroup.LayoutParams layoutParams = toolbarPadding.getLayoutParams();
                layoutParams.height = windowInsets.getSystemWindowInsetTop();
                toolbarPadding.setLayoutParams(layoutParams);

                return windowInsets;
            });
        } else {
            ViewGroup.LayoutParams layoutParams = toolbarPadding.getLayoutParams();

            int resId = toolbarPadding.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resId > 0) {
                layoutParams.height = toolbarPadding.getResources().getDimensionPixelOffset(resId);
            } else {
                layoutParams.height = 1024;
            }
            toolbarPadding.setLayoutParams(layoutParams);
        }

        // Remove redundant shadow in transparent app bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appbar.setOutlineProvider(null);
        }

        checkPhoneCallPermission();

        TextView phoneText = findViewById(R.id.sos_phone_number);

        // Get emergency contact from preference
        String number = PreferenceManager.getDefaultSharedPreferences(this).getString("sos_emergency_call", "empty");

        if (number.equals(" ")) {
            phoneText.setText("Please go Setting");
            Toast.makeText(this, "Please go setting to set an emergency call number", Toast.LENGTH_SHORT).show();
        } else {
            phoneText.setText(number);
        }

        makePhoneCall(phoneText);

    }

    // Check the phone call permission
    private void checkPhoneCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE);
        }
    }

    // Call the provided phone number
    private void makePhoneCall(TextView phoneText) {

        Button callButton = findViewById(R.id.sos_call_button);

        callButton.setOnClickListener((View view) -> {

            // Checks empty string, no phone call
            if (phoneText.getText().toString().equals("")) {
                Toast.makeText(this, "Phone number is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Checks only digits existed, no phone call
            if (!android.text.TextUtils.isDigitsOnly(phoneText.getText().toString())) {
                Toast.makeText(this, "Digits Only!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent callIntent = new Intent(Intent.ACTION_CALL);

            callIntent.setData(Uri.parse("tel:" + phoneText.getText().toString()));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Check the phone call permission before make the call
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE);
            } else {
                startActivity(callIntent);
            }
        });
    }

}
