package au.edu.unimelb.eng.navibee.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.FormatUtilityKt;
import au.edu.unimelb.eng.navibee.utils.URLImageViewCacheLoader;

public class GroupDetailActivity extends AppCompatActivity {
    private ConversationManager cm = ConversationManager.getInstance();
    private GroupConversation conv;
    private ArrayList<String> memberList;
    private String convId;

    AlertDialog chatDeletedDialog;
    AlertDialog deleteChatDialog;

    private BroadcastReceiver convUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConversationManager.getInstance().getConversation(convId) == null){
                chatDeletedDialog.show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        convId = getIntent().getStringExtra("CONV_ID");
        conv = (GroupConversation) cm.getConversation(convId);
        memberList = new ArrayList<>(conv.getMembers());

        setSupportActionBar(findViewById(R.id.chat_groupDetail_toolbar));
        String subtitleString = getResources().getQuantityString(
                R.plurals.chat_group_member_count,
                conv.getMembers().size(), conv.getMembers().size()
        );

        ImageView avatar = findViewById(R.id.cat_avatar);
        TextView title = findViewById(R.id.cat_title);
        TextView subtitle = findViewById(R.id.cat_subtitle);
        avatar.setImageDrawable(conv.getRoundIconDrawable(getResources()));
        title.setText(conv.getName());
        subtitle.setText(subtitleString);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String myUid = ConversationManager.getInstance().getUid();
        UserInfoManager.getInstance().getUserInfo(myUid, userInfo -> {
            TextView viewName = findViewById(R.id.groupDetail_selfName);
            ImageView viewIcon = findViewById(R.id.groupDetail_selfAvatar);
            View isAdmin = findViewById(R.id.groupDetail_selfIsAdmin);

            viewName.setText(userInfo.getName());
            new URLImageViewCacheLoader(userInfo.getPhotoUrl(), viewIcon)
                    .roundImage(true).execute();
            isAdmin.setVisibility(
                    conv.getCreator().equals(cm.getUid()) ?
                            View.VISIBLE : View.GONE
            );
        });

        TextView createDateLabel = findViewById(R.id.activity_group_detail_date_created_label);
        TextView createDate = findViewById(R.id.activity_group_detail_date_created);
        long date = conv.getCreateDate().getTime();
        createDate.setText(FormatUtilityKt.chatDateMediumFormat(date));
        createDateLabel.setText(
                getString(
                        R.string.chat_group_created_at,
                        FormatUtilityKt.chatDatePreposition(date)
                )
        );

        inflateMembers();

        registerReceiver(convUpdateReceiver, new IntentFilter(ConversationManager.BROADCAST_CONVERSATION_UPDATED));

        chatDeletedDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.chat_group_deleted)
                .setPositiveButton(R.string.action_ok, (dialog, i) -> finish())
                .create();

        deleteChatDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.chat_group_delete_confirm)
                .setNegativeButton(R.string.action_cancel, (dialog, i) -> dialog.cancel())
                .setPositiveButton(R.string.action_dismiss, (dialog, i) -> {
                    cm.deleteGroup(convId);
                    finish();
                })
                .create();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void inflateMembers() {
        LinearLayout nsv = findViewById(R.id.group_member_scroll_list);
        for (String uid: memberList) {
            if (cm.getUid().equals(uid))
                continue;
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.group_chat_member_item, nsv, false);
            TextView viewName = view.findViewById(R.id.group_member_name);
            ImageView viewIcon = view.findViewById(R.id.group_member_icon);
            View isAdmin = view.findViewById(R.id.group_member_is_admin);
            UserInfoManager.getInstance().getUserInfo(uid, userInfo -> {
                viewName.setText(userInfo.getName());
                new URLImageViewCacheLoader(userInfo.getPhotoUrl(), viewIcon)
                        .roundImage(true).execute();
                isAdmin.setVisibility(
                        conv.getCreator().equals(uid) ? View.VISIBLE : View.GONE
                );
            });
            view.setOnClickListener(v -> {
                if (!uid.equals(ConversationManager.getInstance().getUid())){
                    Intent intent = new Intent(this, FriendDetail.class);
                    intent.putExtra("FRIEND_ID", uid);
                    startActivity(intent);
                }
            });
            nsv.addView(view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!cm.getUid().equals(conv.getCreator()))
            return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(convUpdateReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_groupDetails_delete:
                deleteChatDialog.show();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
