package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import au.edu.unimelb.eng.navibee.navigation.DestinationsActivity;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.FriendActivity;
import au.edu.unimelb.eng.navibee.utils.URLImageViewCacheLoader;
import timber.log.Timber;

import static au.edu.unimelb.eng.navibee.utils.DisplayUtilitiesKt.updateDpi;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private PopupAdapter mAdapter;
    private ListPopupWindow mPopupWindow;
    private ImageView mOverflowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Update app DPI for debug purpose.
        updateDpi(this);
        setContentView(R.layout.activity_main);

        firestoreTimestamp();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        findViewById(R.id.landing_sos_btn).setOnClickListener(this);
        findViewById(R.id.landing_social_btn).setOnClickListener(this);

        ConversationManager.init();
        setFCMToken();

        setupWelcomeBanner();

        setupOverflowMenu();

        MaterialButton naviBtn = findViewById(R.id.landing_navigation_btn);
        naviBtn.setBackgroundTintList(null);
        naviBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_landing_major_background));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            naviBtn.setClipToOutline(true);
        }
    }

    private void setupWelcomeBanner() {
        mOverflowButton = findViewById(R.id.landing_user_icon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mOverflowButton.setClipToOutline(true);
        }

        mOverflowButton.setOnClickListener(v -> mPopupWindow.show());

        new URLImageViewCacheLoader(
                Objects.requireNonNull(mUser.getPhotoUrl()).toString(),
                mOverflowButton).roundImage(true).execute();

        TextView welcomeLine = findViewById(R.id.landing_welcome_line);
        String format = getResources().getString(R.string.landing_welcome_line);
        format = format.replace("\n", "<br>");
        String name = Objects.requireNonNull(mUser.getDisplayName());
        name = Html.escapeHtml(name.split(" ", 2)[0]);
        String text = String.format(format, "<b>" + name + "</b>");
        Spanned spannedText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spannedText = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        } else {
            spannedText = Html.fromHtml(text);
        }
        welcomeLine.setText(spannedText);
    }

    private void setupOverflowMenu() {
        mPopupWindow = new ListPopupWindow(this);
        String[] mPopupWindowItems = new String[]{
                "%ROW_FOR_USER_PROFILE%",
                getResources().getString(R.string.action_settings),
                getResources().getString(R.string.action_log_out)
        };
        mAdapter = new PopupAdapter(mPopupWindowItems, menuClickListeners);

        findViewById(R.id.landing_layout).post(() -> {
            mPopupWindow.setModal(true);
            mPopupWindow.setAnchorView(mOverflowButton);
            mPopupWindow.setAdapter(mAdapter);
            mPopupWindow.setWidth(dpToPx(getResources().getInteger(R.integer.popup_menu_main_width)));
            mPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
            mPopupWindow.setVerticalOffset(-mOverflowButton.getLayoutParams().height);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mPopupWindow.setDropDownGravity(Gravity.START);
            } else {
                mPopupWindow.setDropDownGravity(Gravity.END);
            }
        });
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
            case R.id.landing_events_btn:
                startActivity(new Intent(this, EventsActivity.class));
                break;

            case R.id.landing_sos_btn:
                startActivity(new Intent(this, SosActivity.class));
                break;

            case R.id.landing_social_btn:
                startActivity(new Intent(this, FriendActivity.class));
                break;
        }
    }

    public void startNavigationActivity(View view) {
        startActivity(new Intent(this, DestinationsActivity.class));
    }

    private View.OnClickListener[] menuClickListeners = new View.OnClickListener[] {
            null,
            // Settings
            (View v) -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                mPopupWindow.dismiss();
            },
            // Log out
            (View v) -> logOut()
    };

    private void setFCMToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    // Get Instance ID token
                    String token = task.getResult().getToken();
                    String uid = Objects.requireNonNull(FirebaseAuth.getInstance()
                            .getCurrentUser()).getUid();

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
        } catch (Exception e) {
            Timber.e(e, "Error occurred while resetting tokens.");
        }


        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        this.finish();
    }

    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources()
                .getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }


    private class PopupAdapter extends BaseAdapter {
        private String[] menuItems;
        private View.OnClickListener[] listeners;

        PopupAdapter(String[] menuItems, View.OnClickListener[] listeners) {
            this.menuItems = menuItems;
            this.listeners = listeners;
        }

        @Override
        public int getCount() {
            return menuItems.length;
        }

        @Override
        public Object getItem(int position) {
            return menuItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (position == 0) {
                if (!(convertView instanceof ConstraintLayout)) {
                    holder = new ViewHolder();
                    convertView = getLayoutInflater().inflate(R.layout.popup_menu_main_profile, null);
                    holder.menuItemView = convertView;
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                ((TextView) holder.menuItemView.findViewById(R.id.popup_menu_main_profile_name))
                        .setText(mUser.getDisplayName());
                ((TextView) holder.menuItemView.findViewById(R.id.popup_menu_main_profile_secondary))
                        .setText(mUser.getEmail());
                ImageView profile = holder.menuItemView.findViewById(R.id.popup_menu_main_profile_picture);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    profile.setClipToOutline(true);
                }

                new URLImageViewCacheLoader(
                        Objects.requireNonNull(mUser.getPhotoUrl()).toString(),
                        profile).roundImage(true).execute();

            } else {
                if (!(convertView instanceof LinearLayout)) {
                    holder = new ViewHolder();
                    convertView = getLayoutInflater().inflate(R.layout.popup_menu_button, null);
                    holder.menuItemView = convertView;
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                TextView text = holder.menuItemView.findViewById(R.id.popup_menu_button_text);
                text.setText(menuItems[position]);
                holder.menuItemView.setOnClickListener(listeners[position]);
            }
            return convertView;
        }

        private class ViewHolder {
            View menuItemView;
        }

    }
}
