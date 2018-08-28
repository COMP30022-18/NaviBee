package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import au.edu.unimelb.eng.navibee.social.FriendActivity;
import au.edu.unimelb.eng.navibee.social.FriendManager;
import au.edu.unimelb.eng.navibee.social.ConversationManager;

import au.edu.unimelb.eng.navibee.navigation.DestinationsActivity;

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

        TextView textView = (TextView)findViewById(R.id.textView);
        TextView textView2 = (TextView)findViewById(R.id.textView2);
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
                } catch (Exception e) {
                }


                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intent);
                this.finish();
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
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
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

                    }
                });
    }

}

