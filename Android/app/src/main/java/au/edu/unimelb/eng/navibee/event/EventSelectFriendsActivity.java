package au.edu.unimelb.eng.navibee.event;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Checkable;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.UserInfoManager;
import au.edu.unimelb.eng.navibee.utils.UserListArrayAdapter;

public class EventSelectFriendsActivity extends AppCompatActivity {

    private Map<String, Boolean> selectedFriendMap;
    private ArrayList<String> friendList;
    private ArrayList<UserInfoManager.UserInfo> friendInfoList;
    private Map<String, UserInfoManager.UserInfo> stringUserInfoMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_select_friends_list);

        loadData();

        ListView friendListView = findViewById(R.id.event_select_friends_list);
        friendListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


        UserInfoManager.getInstance().getUserInfo(friendList, stringUserInfoMap -> {
            this.stringUserInfoMap = stringUserInfoMap;
            friendInfoList = new ArrayList<>();
            for (String uid: friendList) {
                friendInfoList.add(stringUserInfoMap.get(uid));
            }

            friendListView.setAdapter(new UserListArrayAdapter(this,
                    R.layout.user_profile_checked_text_view,
                    android.R.id.text1,
                    friendInfoList));
            // load already selected friends
            for (int pos = 0; pos < friendList.size(); pos++){
                friendListView.setItemChecked(pos, selectedFriendMap.get(friendList.get(pos)));
            }

            friendListView.setOnItemClickListener(
                    (arg0, v, position, id) -> {
                        Checkable item = (Checkable) v;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_confirm_btn:
                editEventDetail();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        editEventDetail();
    }

    public void editEventDetail(){
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
