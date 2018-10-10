package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.URLImageViewCacheLoader;

public class FriendDetail extends AppCompatActivity {
    private String convId;
    private String friendId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);
        setSupportActionBar(findViewById(R.id.userProfile_actionBar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        convId = getIntent().getStringExtra("CONV_ID");
        friendId = getIntent().getStringExtra("FRIEND_ID");
        ImageView icon = findViewById(R.id.friend_detail_icon);
        TextView name = findViewById(R.id.friend_detail_name);
        UserInfoManager.getInstance().getUserInfo(friendId, userInfo -> {
            name.setText(userInfo.getName());
            new URLImageViewCacheLoader(userInfo.getHighResolutionPhotoUrl(), icon)
                    .roundImage(true).execute();
        });

    }
    public void onClickSendMessage(View view){
        Intent intent = new Intent(getBaseContext(), ChatActivity.class);
        intent.putExtra("CONV_ID", convId);
        startActivity(intent);
    }

    public void onClickDeleteFriend(View view){
        ConversationManager.getInstance().deleteFriend(friendId);
    }
}
