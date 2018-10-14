package au.edu.unimelb.eng.navibee.social;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.UserListArrayAdapter;

public class CreateGroupChatActivity extends AppCompatActivity {


    private ConversationManager cm = ConversationManager.getInstance();

    private Map<String, Boolean> selectedFriendMap = new HashMap<>();
    private ArrayList<String> friendList;
    private ArrayList<UserInfoManager.UserInfo> friendInfoList;
    private ListView friendListView;
    private UserListArrayAdapter ulAdapter;

    private boolean edited = false;
    private boolean submitting = false;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_chat);

        friendList = ConversationManager.getInstance().getFriendList();

        // add to selectedFriendMap
        for (String uid: friendList){
            selectedFriendMap.put(uid, false);
        }

        friendListView = findViewById(R.id.activity_createGroupChat_select_friends_list);
        friendListView.setChoiceMode(friendListView.CHOICE_MODE_MULTIPLE);

        UserInfoManager.getInstance().getUserInfo(friendList, stringUserInfoMap -> {
            friendInfoList = new ArrayList<>();
            for (String uid: friendList) {
                friendInfoList.add(stringUserInfoMap.get(uid));
            }

            ulAdapter = new UserListArrayAdapter(this,
                    R.layout.user_profile_checked_text_view,
                    android.R.id.text1, friendInfoList);
            friendListView.setAdapter(ulAdapter);

            friendListView.setOnItemClickListener(
                (arg0, v, position, id) -> {
                    Checkable item = (Checkable) v;
                    selectedFriendMap.put(friendList.get(position), item.isChecked());
                    edited = true;
                }
            );

        });

        EditText editText = findViewById(R.id.activity_createGroupChat_group_name);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                edited = true;
            }
        });

        dialog = new AlertDialog.Builder(this)
                .setMessage(R.string.chat_create_group_back_warning)
                .setPositiveButton(R.string.action_keep, (dialog, which) -> dialog.dismiss())
                .setNegativeButton(R.string.action_discard, (dialog, which) -> {
                    dialog.dismiss();
                    this.finish();
                })
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        ProgressBar pb = new ProgressBar(this);
        MenuItem progressItem = menu.findItem(R.id.menu_confirm_progress);
        int px = (int) (24 * Resources.getSystem().getDisplayMetrics().density);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(3*px, 3*px);
        pb.setLayoutParams(lp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pb.setIndeterminateTintList(
                ColorStateList.valueOf(0xff000000)
            );
        }
        pb.setPaddingRelative(px, px, px, px);
        progressItem.setActionView(pb);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_confirm_btn).setVisible(!submitting);
        menu.findItem(R.id.menu_confirm_progress).setVisible(submitting);
        return super.onPrepareOptionsMenu(menu);

    }

    private void setSubmitting(boolean sub) {
        submitting = sub;
        ulAdapter.setEnabled(!sub);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_confirm_btn:
                submit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (edited) {
            dialog.show();
        } else {
            this.finish();
        }
    }

    public void submit(){
        setSubmitting(true);
        EditText editText = findViewById(R.id.activity_createGroupChat_group_name);
        String groupName = editText.getText().toString();

        String groupCreator = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ArrayList<String> selectedUidList = new ArrayList<>();

        CoordinatorLayout c = findViewById(R.id.chat_createGroup_coordinator);

        for (String uid: selectedFriendMap.keySet()){
            if (selectedFriendMap.get(uid)){
                selectedUidList.add(uid);
            }
        }
        if (selectedUidList.size() < 1){
            Snackbar.make(c, R.string.chat_group_create_friend_required, Snackbar.LENGTH_SHORT).show();
            setSubmitting(false);
        } else {
            selectedUidList.add(groupCreator);
            Task<HttpsCallableResult> createGroupTask = cm.createGroupChat(selectedUidList, groupName, "1");

            createGroupTask.addOnFailureListener(httpsCallableResult -> {
                Snackbar.make(c, R.string.error_failed_to_connect_to_server, Snackbar.LENGTH_SHORT).show();
                setSubmitting(false);
            });
            createGroupTask.addOnSuccessListener(httpsCallableResult -> {
                this.finish();
                setSubmitting(false);
            });
        }
    }
}
