package au.edu.unimelb.eng.navibee.social;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;

public class CreateGroupChatActivity extends AppCompatActivity {


    private ConversationManager cm = ConversationManager.getInstance();
    private Map<String, Boolean> selectedFriendMap = new HashMap<>();
    private ArrayList<String> friendList;
    private ArrayList<String> friendNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_chat);

        friendList = ConversationManager.getInstance().getFriendList();

        // add to selectedFriendMap
        for (String uid: friendList){
            selectedFriendMap.put(uid, false);
        }

        ListView friendListView = findViewById(R.id.activity_createGroupChat_select_friends_list);
        friendListView.setChoiceMode(friendListView.CHOICE_MODE_MULTIPLE);

        UserInfoManager.getInstance().getUserInfo(friendList, stringUserInfoMap -> {
            friendNameList = new ArrayList<>();
            for (String uid: friendList) {
                friendNameList.add(stringUserInfoMap.get(uid).getName());
            }

            friendListView.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_checked, friendNameList));

            friendListView.setOnItemClickListener(
                    (arg0, v, position, id) -> {
                        CheckedTextView item = (CheckedTextView) v;
                        Toast.makeText(getApplicationContext(), friendNameList.get(position) + " checked : " +
                                item.isChecked(), Toast.LENGTH_SHORT).show();

                        selectedFriendMap.put(friendList.get(position), item.isChecked());
                    }
            );

        });
    }
    public void onCreateClicked(View view){
        EditText editText = (EditText) findViewById(R.id.activity_createGroupChat_group_name);
        String groupName = editText.getText().toString();

        String groupCreator = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ArrayList<String> selectedUidList = new ArrayList<>();
        for(String uid: selectedFriendMap.keySet()){
            if(selectedFriendMap.get(uid)){
                selectedUidList.add(uid);
            }
        }
        if (selectedUidList.size() < 1){
            Toast.makeText(getApplicationContext(), "At least one friend should be selected", Toast.LENGTH_SHORT).show();
        }
        else{
            selectedUidList.add(groupCreator);
            Task<HttpsCallableResult> createGroupTask = cm.createGroupChat(selectedUidList, groupName, "1");

            createGroupTask.addOnFailureListener(httpsCallableResult -> {
                Toast.makeText(this, "Network error.", Toast.LENGTH_LONG).show();
            });
            createGroupTask.addOnSuccessListener(httpsCallableResult -> {
                Toast.makeText(this, "Success.", Toast.LENGTH_LONG).show();

                this.finish();
            });
        }

    }
}
