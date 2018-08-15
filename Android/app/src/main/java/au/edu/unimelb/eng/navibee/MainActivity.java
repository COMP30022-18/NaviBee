package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import au.edu.unimelb.eng.navibee.Social.FriendManager;
import au.edu.unimelb.eng.navibee.Social.ConversationManager;

import au.edu.unimelb.eng.navibee.navigation.DestinationsActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

}

