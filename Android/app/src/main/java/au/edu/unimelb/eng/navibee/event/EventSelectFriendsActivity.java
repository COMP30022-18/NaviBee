package au.edu.unimelb.eng.navibee.event;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.UserInfoManager;

public class EventSelectFriendsActivity extends AppCompatActivity {

    private Map<String, Boolean> selectedFriendMap;
    private ArrayList<String> friendList;
    private ArrayList<String> friendNameList;
    private Map<String, UserInfoManager.UserInfo> stringUserInfoMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_select_friends_list);

        loadData();

        ListView friendListView = findViewById(R.id.event_select_friends_list);
        friendListView.setChoiceMode(friendListView.CHOICE_MODE_MULTIPLE);


        UserInfoManager.getInstance().getUserInfo(friendList, stringUserInfoMap -> {
            this.stringUserInfoMap = stringUserInfoMap;
            friendNameList = new ArrayList<>();
            for (String uid: friendList) {
                friendNameList.add(stringUserInfoMap.get(uid).getName());
            }

            friendListView.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_checked, friendNameList));
            // load already selected friends
            for(int pos=0;pos<friendList.size();pos++){
                friendListView.setItemChecked(pos, selectedFriendMap.get(friendList.get(pos)));
            }

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

    private void loadData() {

        selectedFriendMap = new HashMap<>();
        // fetch friend list
        friendList = ConversationManager.getInstance().getFriendList();

        // add to selectedFriendMap
        for (String uid: friendList){
            selectedFriendMap.put(uid, false);
        }
        // load already selected friends uid from previews activity
        Intent intent = getIntent();
        HashMap<String, UserInfoManager.UserInfo> haveSelected =
                (HashMap<String, UserInfoManager.UserInfo>)
                intent.getSerializableExtra("selected");
        if (haveSelected != null){
            for (String user: haveSelected.keySet()){
                selectedFriendMap.put(user, true);
            }
        }
    }

    public void editEventDetail(View view){
        // get selected friends' uid
        if (selectedFriendMap == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        HashMap<String, UserInfoManager.UserInfo> selected = new HashMap<>();
        for (String uid: selectedFriendMap.keySet()){
            if (selectedFriendMap.get(uid) == true) {
                selected.put(uid, stringUserInfoMap.get(uid));
            }
        }
        // start next step
        Intent intent = new Intent(this, EventEditActivity.class);
        intent.putExtra("selected", selected);
        setResult(RESULT_OK, intent);
        finish();
    }

}
