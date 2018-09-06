package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.navigation.DestinationsActivity;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.FriendActivity;
import au.edu.unimelb.eng.navibee.social.FriendManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firestoreTimestamp();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        String name = user.getDisplayName();
        String email = user.getEmail();

        TextView textView = findViewById(R.id.textView);
        TextView textView2 = findViewById(R.id.textView2);
        textView.setText(name);
        textView2.setText(email);

        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.landing_sos_btn).setOnClickListener(this);
        findViewById(R.id.landing_social_btn).setOnClickListener(this);

        FriendManager.init();
        ConversationManager.init();
        setFCMToken();
    }

    // The behavior for java.util.Date objects stored in Firestore is going to chang
    private void firestoreTimestamp() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_out_button:
                logOut();
                break;
            
            case R.id.landing_events_btn:
                startActivity(new Intent(this, EventActivity.class));
                break;

            case R.id.landing_sos_btn:
                startActivity(new Intent(getApplicationContext(), SosActivity.class));
                break;

            case R.id.landing_social_btn:
                startActivity(new Intent(this, FriendActivity.class));
                break;
        }
    }

    public void startNavigationActivity(View view) {
        Intent intent = new Intent(this, DestinationsActivity.class);
        startActivity(intent);
    }

    private void setFCMToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    // Get Instance ID token
                    String token = task.getResult().getToken();
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    Map<String, Object> docData = new HashMap<>();
                    docData.put("uid", uid);
                    docData.put("lastSeen", new Timestamp(new Date()));
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("fcmTokens").document(token).set(docData);

                });
    }

    private void logOut() {
        // sign out firebase
        FirebaseAuth.getInstance().signOut();

        // sign out google login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignIn.getClient(this, gso).signOut();

        // reset token to prevent further messages
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (Exception ignored) {
        }


        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_action_log_out:
                logOut();
                return true;
            case R.id.menu_main_action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

