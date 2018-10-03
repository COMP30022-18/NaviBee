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

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.NetworkImageHelper;

public class FriendDetail extends AppCompatActivity {
    private String convId;
    private String friendId;
    private ConversationManager cm = ConversationManager.getInstance();
    private Button sendMessageButton;
    private Button deleteFriendButton;
    private Button addFriendButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        progressBar = findViewById(R.id.activity_friend_detail_addFriend_progress);
        progressBar.setVisibility(View.GONE);
        friendId = getIntent().getStringExtra("FRIEND_ID");
        sendMessageButton = findViewById(R.id.activity_friend_detail_sendMessage_button);
        deleteFriendButton = findViewById(R.id.activity_friend_detail_deleteFriend_button);
        addFriendButton = findViewById(R.id.activity_friend_detail_addFriend_button);

        loadButton();

        ImageView icon = findViewById(R.id.friend_detail_icon);
        TextView name = findViewById(R.id.friend_detail_name);
        UserInfoManager.getInstance().getUserInfo(friendId, userInfo -> {
            name.setText(userInfo.getName());
            NetworkImageHelper.loadImage(icon, userInfo.getHighResolutionPhotoUrl());
        });

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadButton();
            }
        }, new IntentFilter(ConversationManager.BROADCAST_FRIEND_UPDATED));

    }
    public void loadButton(){
        if (cm.getFriendList().contains(friendId)){
            convId = ConversationManager.getInstance().getPrivateConvId(friendId);
            addFriendButton.setVisibility(View.GONE);
            sendMessageButton.setVisibility(View.VISIBLE);
            deleteFriendButton.setVisibility(View.VISIBLE);
        }
        else{
            addFriendButton.setVisibility(View.VISIBLE);
            sendMessageButton.setVisibility(View.GONE);
            deleteFriendButton.setVisibility(View.GONE);
        }
    }
    protected void onClick(View view){
        switch (view.getId()) {
            case R.id.activity_friend_detail_sendMessage_button:
                Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                intent.putExtra("CONV_ID", convId);
                startActivity(intent);
                break;
            case R.id.activity_friend_detail_deleteFriend_button:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Alert");
                dialog.setMessage("Are you sure you want to delete this friend?");
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }
                });
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        ConversationManager.getInstance().deleteFriend(friendId);
                        finish();
                    }
                });
                dialog.show();
                break;
            case R.id.activity_friend_detail_addFriend_button:
                Task<HttpsCallableResult> task = ConversationManager.getInstance().addFriend(friendId);
                progressBar.setVisibility(View.VISIBLE);

                task.addOnFailureListener(httpsCallableResult -> {
                    Toast.makeText(this, "Network error, add friend failed", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                });
                task.addOnSuccessListener(httpsCallableResult -> {
                    progressBar.setVisibility(View.GONE);
                    final Map<String, Object> res = ((Map<String, Object>) httpsCallableResult.getData());
                    if (((Integer) res.get("code"))==0) {
                        Toast.makeText(this, "Success, you will soon be able to chat with your friend", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, ((String) res.get("msg")), Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }
}
