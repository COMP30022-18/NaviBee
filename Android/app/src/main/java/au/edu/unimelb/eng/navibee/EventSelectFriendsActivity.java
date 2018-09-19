package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.UserInfoManager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventSelectFriendsActivity extends AppCompatActivity {

    private Map<String, Boolean> selectedFriendMap;
    private ArrayList<String> friendList;
    private ArrayList<String> friendNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_select_friends_list);

        loadData();

        ListView friendListView = findViewById(R.id.event_select_friends_list);
        friendListView.setChoiceMode(friendListView.CHOICE_MODE_MULTIPLE);


        UserInfoManager.getInstance().getUserInfo(friendList, stringUserInfoMap -> {
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
        ArrayList<String> haveSelected = intent.getStringArrayListExtra("selectedUid");
        if(haveSelected != null){
            for(String uid: haveSelected){
                selectedFriendMap.put(uid, true);
            }
        }
    }

    public void editEventDetail(View view){
        // get selected friends' uid
        ArrayList<String> selectedUidList = new ArrayList<>();
        ArrayList<String> selectedNameList = new ArrayList<>();
        for(String uid: selectedFriendMap.keySet()){
            if(selectedFriendMap.get(uid)){
                selectedUidList.add(uid);
                selectedNameList.add(uid2Name(uid));
            }
        }
        // start next step
        Intent intent = new Intent(this, EventEditActivity.class);
        intent.putStringArrayListExtra("selectedUid", selectedUidList);
        intent.putStringArrayListExtra("selectedName", selectedNameList);
        setResult(RESULT_OK, intent);
        finish();
    }

    private String uid2Name(String uid){ return friendNameList.get(friendList.indexOf(uid)); }
}
