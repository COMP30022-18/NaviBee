package au.edu.unimelb.eng.navibee.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.URLImageViewCacheLoader;

public class FriendDetail extends AppCompatActivity {
    private String convId;
    private String friendId;
    private ConversationManager cm = ConversationManager.getInstance();
    private View sendMessageButton;
    private View deleteFriendButton;
    private View addFriendButton;

    private View sendMessageDivider;
    private View friendDivider;

    private CoordinatorLayout coord;

    private boolean isEnabled = false;

    private UserInfoManager.UserInfo user;

    private AlertDialog deleteFriendDialog;

    private BroadcastReceiver convUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadButton();
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);
        setSupportActionBar(findViewById(R.id.userProfile_actionBar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        convId = getIntent().getStringExtra("CONV_ID");
        friendId = getIntent().getStringExtra("FRIEND_ID");
        sendMessageButton = findViewById(R.id.friendDetail_sendMessageBtn);
        deleteFriendButton = findViewById(R.id.friendDetail_deleteFriendBtn);
        addFriendButton = findViewById(R.id.friendDetail_addFriendBtn);
        sendMessageDivider = findViewById(R.id.friendDetail_sendMessageDvdr);
        friendDivider = findViewById(R.id.friendDetail_friendDvdr);
        coord = findViewById(R.id.chat_friendDetail_coordinator);

        loadButton();

        ImageView icon = findViewById(R.id.friend_detail_icon);
        TextView name = findViewById(R.id.friend_detail_name);
        UserInfoManager.getInstance().getUserInfo(friendId, userInfo -> {
            user = userInfo;
            name.setText(userInfo.getName());
            new URLImageViewCacheLoader(userInfo.getHighResolutionPhotoUrl(), icon)
                    .roundImage(true).execute();
        });

        registerReceiver(convUpdateReceiver, new IntentFilter(ConversationManager.BROADCAST_CONVERSATION_UPDATED));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(convUpdateReceiver);
    }

    public void loadButton() {
        loadButton(cm.getFriendList().contains(friendId));
    }

    public void toggleButtons(boolean enabled) {
        sendMessageButton.setEnabled(enabled);
        sendMessageButton.setClickable(enabled);
        deleteFriendButton.setEnabled(enabled);
        deleteFriendButton.setClickable(enabled);
        addFriendButton.setEnabled(enabled);
        addFriendButton.setClickable(enabled);
    }

    public void loadButton(boolean isFriend){
        toggleButtons(true);
        friendDivider.setVisibility(View.VISIBLE);
        if (isFriend){
            convId = ConversationManager.getInstance().getPrivateConvId(friendId);
            addFriendButton.setVisibility(View.GONE);
            deleteFriendButton.setVisibility(View.VISIBLE);
            sendMessageButton.setVisibility(View.VISIBLE);
            sendMessageDivider.setVisibility(View.VISIBLE);
        } else {
            addFriendButton.setVisibility(View.VISIBLE);
            deleteFriendButton.setVisibility(View.GONE);
            sendMessageButton.setVisibility(View.GONE);
            sendMessageDivider.setVisibility(View.GONE);
        }
    }
    public void onClickSendMessage(View view){
        Intent intent = new Intent(getBaseContext(), ChatActivity.class);
        intent.putExtra("CONV_ID", convId);
        startActivity(intent);
    }

    public void onClickAddFriend(View view) {
        loadButton(true);
        toggleButtons(false);
        ConversationManager.getInstance().addFriend(friendId)
                .addOnFailureListener(result -> {
                    Snackbar.make(coord, R.string.error_failed_to_connect_to_server, Snackbar.LENGTH_LONG).show();
                    loadButton(false);
                    toggleButtons(true);
                })
                .addOnSuccessListener(result -> {
                    final Map<String, Object> res = ((Map<String, Object>) result.getData());
                    if (((Integer) res.get("code")) == 0) {
                        Snackbar.make(coord, getString(R.string.friend_added, user.getName()), Snackbar.LENGTH_LONG).show();
                        toggleButtons(true);
                    } else {
                        Toast.makeText(this, ((String) res.get("msg")), Toast.LENGTH_LONG).show();
                        loadButton(false);
                        toggleButtons(true);
                    }
                });
    }

    public void onClickDeleteFriend(View view){
        if (deleteFriendDialog == null) {
            deleteFriendDialog = new AlertDialog.Builder(this).
                    setMessage(getString(R.string.chat_delete_friend, user.getName())).
                    setNegativeButton(R.string.action_keep, (dialog, i) -> dialog.cancel()).
                    setPositiveButton(R.string.action_remove, (dialog, i) -> {
                        loadButton(false);
                        toggleButtons(false);
                        ConversationManager.getInstance().deleteFriend(friendId).addOnCompleteListener(
                                task -> {
                                    toggleButtons(true);
                                    if (!task.isSuccessful())
                                        loadButton(true);
                                }
                        );
                    }).create();
        }
        deleteFriendDialog.show();
    }
}
