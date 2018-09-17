package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;

public class FriendDetail extends AppCompatActivity {
    private String convId;
    private String friendId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);
        convId = getIntent().getStringExtra("CONV_ID");
        friendId = getIntent().getStringExtra("FRIEND_ID");
    }
    protected void onClick(View view){
        switch (view.getId()) {
            case R.id.activity_friend_detail_sendMessage_button:
                Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                intent.putExtra("CONV_ID", convId);
                startActivity(intent);
                break;
        }
    }
}
