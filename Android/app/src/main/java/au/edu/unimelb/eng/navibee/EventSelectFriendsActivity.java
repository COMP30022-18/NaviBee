package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
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

import au.edu.unimelb.eng.navibee.social.FriendManager;

public class EventSelectFriendsActivity extends AppCompatActivity {

    private Map<String, Boolean> selectedFriendMap;
    private ArrayList<String> friendNameList;
    private ArrayList<FriendManager.ContactPerson> friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_select_friends_list);

        friendNameList = new ArrayList<>();
        selectedFriendMap = new HashMap<>();
        friendList = new ArrayList<>();

        loadData();

        ListView friendListView = findViewById(R.id.event_select_friends_list);
        friendListView.setChoiceMode(friendListView.CHOICE_MODE_MULTIPLE);

        friendListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_checked, friendNameList));
        // load already selected friends
        for(int pos=0;pos<friendList.size();pos++){
            friendListView.setItemChecked(pos, selectedFriendMap.get(friendList.get(pos).getUid()));
        }

        friendListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View v,
                                            int position, long id) {
                        CheckedTextView item = (CheckedTextView) v;
                        Toast.makeText(getApplicationContext(), friendList.get(position).getName() + " checked : " +
                                item.isChecked(), Toast.LENGTH_SHORT).show();

                        selectedFriendMap.put(friendList.get(position).getUid(), item.isChecked());
                    }
                }
        );
    }

    private void loadData (){
        // fetch friend list
        FriendManager.getInstance().fetchContactPersonList(friendList);

        // add to selectedFriendMap
        for (FriendManager.ContactPerson p: friendList){
            selectedFriendMap.put(p.getUid(), false);
            friendNameList.add(p.getName());
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
        for(String uid: selectedFriendMap.keySet()){
            if(selectedFriendMap.get(uid)){
                selectedUidList.add(uid);
            }
        }
        // start next step
        Intent intent = new Intent(this, EventEditActivity.class);
        intent.putStringArrayListExtra("selectedUid", selectedUidList);
        setResult(RESULT_OK, intent);
        finish();
    }
}
