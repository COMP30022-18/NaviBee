package au.edu.unimelb.eng.navibee;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SosSettingActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_setting);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        EditText phoneNumber = findViewById(R.id.phoneText);
        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emergency = phoneNumber.getText().toString();
                db.collection("users").document(uid).update("emergency", emergency);

                Toast.makeText(SosSettingActivity.this, "Emergency contact added",
                        Toast.LENGTH_LONG).show();

                // wait 1 second
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                Intent returnIntent = new Intent();
//                returnIntent.putExtra("emergency", emergency);
//                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            }
        });

    }

}
