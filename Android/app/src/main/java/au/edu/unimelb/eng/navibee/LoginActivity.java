package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int RC_SIGN_IN = 0;
    private static final int RC_READ = 1;
    private static final int RC_SAVE = 2;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    CredentialsClient mCredentialsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mCredentialsClient = Credentials.getClient(this);
//        setSupportActionBar((Toolbar) findViewById(R.id.login_logo_toolbar));

        mAuth = FirebaseAuth.getInstance();

        if (!checkSignIn()) {
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            checkSmartLockSignIn();

            findViewById(R.id.sign_in_button).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // TODO: Google Sign In failed
                Toast.makeText(LoginActivity.this, "Sign in fails: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == RC_READ) {
            // Smart lock multi-account resolve result.
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                firebaseAuthWithSmartLock(credential);
            } else {
                Timber.e("Credential Read: NOT OK");
            }
            checkSignIn();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
                        storeCredentialToSmartLock(acct);
                    } else {
                        // TODO: If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Sign in fails", Toast.LENGTH_LONG).show();
                    }

                });
    }

    private void storeCredentialToSmartLock(GoogleSignInAccount acct) {
        // Save user's credential with smart lock
        Credential gsaCred = new Credential.Builder(acct.getEmail())
                .setAccountType(IdentityProviders.GOOGLE)
                .setName(acct.getDisplayName())
                .setProfilePictureUri(acct.getPhotoUrl())
                .build();
        mCredentialsClient.save(gsaCred).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                checkSignIn();
                return;
            }
            Exception e = task.getException();
            if (e instanceof ResolvableApiException) {
                // Try to resolve the save request. This will prompt the user if
                // the credential is new.
                ResolvableApiException rae = (ResolvableApiException) e;
                try {
                    rae.startResolutionForResult(this, RC_SAVE);
                } catch (IntentSender.SendIntentException ex) {
                    // Could not resolve the request
                    Timber.e(ex,"Failed to send resolution.");
                    checkSignIn();
                }
            }
        });
    }

    private void firebaseAuthWithSmartLock(Credential credential) {
        String accountType = credential.getAccountType();
        if (accountType == null) return;
        if (accountType.equals(IdentityProviders.GOOGLE)) {

            GoogleSignInClient client = GoogleSignIn.getClient(this,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .setAccountName(credential.getId())
                            .build());
            client.silentSignIn().addOnCompleteListener( task -> {
                if (task.isSuccessful() && task.getResult().getIdToken() != null) {
                    firebaseAuthWithGoogle(task.getResult());
                }
            });
        }
    }

    private void checkSmartLockSignIn() {
        CredentialRequest request = new CredentialRequest.Builder()
                .setPasswordLoginSupported(false)
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build();

        mCredentialsClient.request(request).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseAuthWithSmartLock(task.getResult().getCredential());
                checkSignIn();
                return;
            }

            Exception e = task.getException();
            if (e instanceof ResolvableApiException) {
                resolveResult((ResolvableApiException) e);
            }
        });
    }

    private void resolveResult(ResolvableApiException rae) {
        try {
            rae.startResolutionForResult(this, RC_READ);
        } catch (IntentSender.SendIntentException e) {
            Timber.e(e, "Failed to send resolution.");
        }
    }

    private boolean checkSignIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
            this.finish();
            return true;
        }
        return false;
    }
}
