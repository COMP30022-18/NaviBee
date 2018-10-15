package au.edu.unimelb.eng.navibee.social;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.FormatUtilityKt;
import au.edu.unimelb.eng.navibee.utils.URLImageViewCacheLoader;
import kotlinx.coroutines.experimental.Job;

public class FriendActivity extends AppCompatActivity {
    // TODO magic string

    public static class ContactItem {
        private Conversation conv;

        public ContactItem(Conversation conv) {
            this.conv = conv;
        }

        public Conversation getConv() {
            return conv;
        }


        public int getUnreadMessage(){
            return conv.getUnreadMsgCount();
        }


        public String getLastMessage() {
            if (!hasMessage()) return "";
            return conv.getMessage(conv.getMessageCount()-1).getSummary();
        }

        public Boolean hasMessage() {
            return conv.getMessageCount()>0;
        }

        public Long getLastMessageTime (){
            Date date = conv.getMessage(conv.getMessageCount() - 1).getTime_();
            return date.getTime();
        }

        public Date getTimeForSort() {
            if (hasMessage()) {
                return conv.getMessage(conv.getMessageCount()-1).getTime_();
            } else {
                return conv.getCreateTimestamp();
            }
        }

        public void displayNameAndIcon(TextView textView, ImageView imageView) {
            textView.setTag(conv.getConvId());

            if (conv instanceof PrivateConversation) {
                UserInfoManager.getInstance().getUserInfo(((PrivateConversation) conv).getTargetUid(), userInfo -> {
                    // text view haven't changed
                    if (textView.getTag().equals(conv.getConvId())) {
                        textView.setText(userInfo.getName());
                        new URLImageViewCacheLoader(userInfo.getPhotoUrl(), imageView)
                                .roundImage(true).execute();
                    }
                });
            } else {
                if (imageView.getTag() instanceof Job)
                    ((Job) imageView.getTag()).cancel(null);
                Resources r = textView.getResources();
                textView.setText(((GroupConversation) conv).getName());
                imageView.setImageDrawable(((GroupConversation) conv).getRoundIconDrawable(r));
            }
        }


    }

    public static class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> implements View.OnClickListener {
        public static class FriendViewHolder extends RecyclerView.ViewHolder{

            public FriendViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private ArrayList<ContactItem> contactList;
        private RecyclerView mRecyclerView;
        private Context friendActivity;
        private Boolean isChatList;


        public FriendAdapter(Context context, ArrayList<ContactItem> contactList, RecyclerView mRecyclerView, Boolean isChatList){
            this.friendActivity = context;
            this.contactList = contactList;
            this.mRecyclerView = mRecyclerView;
            this.isChatList = isChatList;
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }

        public FriendAdapter.FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v;
            if (isChatList){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
            }
            else{
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
            }
            v.setOnClickListener(this);
            FriendViewHolder vh = new FriendViewHolder(v);
            return vh;
        }

        public void onBindViewHolder(FriendViewHolder holder, int position){
            if (isChatList){
                ContactItem tempChat = contactList.get(position);
                tempChat.displayNameAndIcon(holder.itemView.findViewById(R.id.chat_list_item_name),
                        holder.itemView.findViewById(R.id.chat_list_item_icon));
                ((TextView) holder.itemView.findViewById(R.id.chat_list_item_unread)).setText(Integer.toString(tempChat.getUnreadMessage()));
                if (tempChat.getUnreadMessage() == 0){
                    holder.itemView.findViewById(R.id.chat_list_item_unread).setVisibility(View.INVISIBLE);
                } else {
                    holder.itemView.findViewById(R.id.chat_list_item_unread).setVisibility(View.VISIBLE);
                }
                if (tempChat.hasMessage()){
                    ((TextView) holder.itemView.findViewById(R.id.chat_list_item_last_message)).setText(tempChat.getLastMessage());
                    Long time = tempChat.getLastMessageTime();
                    CharSequence relativeTime = FormatUtilityKt.chatDateShortFormat(time);
                    ((TextView) holder.itemView.findViewById(R.id.chat_list_item_time)).setText(relativeTime);
                } else {
                    ((TextView) holder.itemView.findViewById(R.id.chat_list_item_last_message)).setText("");
                    ((TextView) holder.itemView.findViewById(R.id.chat_list_item_time)).setText("");
                }

            } else {
                ContactItem tempPerson = contactList.get(position);
                tempPerson.displayNameAndIcon(holder.itemView.findViewById(R.id.friend_item_name),
                        holder.itemView.findViewById(R.id.friend_item_icon));
            }
        }

        @Override
        public void onClick(final View view) {
            if (isChatList) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                ContactItem tempChat = contactList.get(itemPosition);
                Intent intent = new Intent(friendActivity, ChatActivity.class);
                Conversation tempConv = tempChat.getConv();
                intent.putExtra("CONV_ID", tempConv.getConvId());
                friendActivity.startActivity(intent);
            } else {
                int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                ContactItem tempPerson = contactList.get(itemPosition);
                Intent intent = new Intent(friendActivity, FriendDetail.class);
                PrivateConversation tempConv = (PrivateConversation) tempPerson.getConv();
                intent.putExtra("FRIEND_ID", tempConv.getTargetUid());
                friendActivity.startActivity(intent);
            }
        }
    }

    private ConversationManager cm = ConversationManager.getInstance();
    private ArrayList<ContactItem> contactList = new ArrayList<>();
    private ArrayList<ContactItem> chatsList = new ArrayList<>();

    private FriendAdapter recyclerChatsAdapter;
    private RecyclerView recyclerChatsList;
    private RecyclerView.LayoutManager recyclerChatsManager;
    private BottomNavigationView navigation;

    private FriendAdapter recyclerFriendsAdapter;
    private RecyclerView recyclerFriendsList;
    private RecyclerView.LayoutManager recyclerFriendsManager;

    private BroadcastReceiver convUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadContactList();
            loadChatsList();
        }
    };
    private BroadcastReceiver msgUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recyclerFriendsAdapter.notifyDataSetChanged();
            sortChatsList();
            recyclerChatsAdapter.notifyDataSetChanged();
        }
    };



    private void fadeOutView(View view, int delay) {
        view.animate()
                .alpha(0.0f)
                .setStartDelay(delay)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    private void fadeInView(View view, int delay) {
        view.animate()
                .alpha(1.0f)
                .setStartDelay(delay)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        view.setVisibility(View.VISIBLE);
                    }
                });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            ActionBar tb = getSupportActionBar();
            switch (item.getItemId()) {
                case R.id.switch_friend_list:
                    tb.setTitle(R.string.friends_tab_friends);
                    fadeOutView(recyclerChatsList, 0);
                    fadeInView(recyclerFriendsList, 150);
                    return true;
                case R.id.switch_recent_chat:
                    tb.setTitle(R.string.friends_tab_chats);
                    fadeInView(recyclerChatsList, 150);
                    fadeOutView(recyclerFriendsList, 0);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);

        navigation = findViewById(R.id.friend_activity_navbar);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        recyclerFriendsList = findViewById(R.id.contactRecyclerView);
        recyclerFriendsList.setHasFixedSize(true);
        recyclerFriendsManager = new LinearLayoutManager(this);
        recyclerFriendsList.setLayoutManager(recyclerFriendsManager);
        recyclerFriendsAdapter = new FriendAdapter(this, contactList, recyclerFriendsList, false);
        recyclerFriendsList.setAdapter(recyclerFriendsAdapter);
        recyclerFriendsList.setVisibility(View.INVISIBLE);

        recyclerFriendsList.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        recyclerChatsList = findViewById(R.id.chatsRecyclerView);
        recyclerChatsList.setHasFixedSize(true);
        recyclerChatsManager = new LinearLayoutManager(this);
        recyclerChatsList.setLayoutManager(recyclerChatsManager);
        recyclerChatsAdapter = new FriendAdapter(this, chatsList, recyclerChatsList, true);
        recyclerChatsList.setAdapter(recyclerChatsAdapter);

        recyclerChatsList.setVisibility(View.VISIBLE);

        recyclerChatsList.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        getSupportActionBar().setTitle(R.string.friends_tab_chats);

        SpeedDialView fab = findViewById(R.id.friend_activity_fab);
        fab.getMainFab().setCustomSize(getResources().getDimensionPixelSize(R.dimen.fab_size));

        fab.addActionItem(
                new SpeedDialActionItem
                        .Builder(R.id.friend_fab_add_friend, R.drawable.ic_person_add_black_24dp)
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.colorLightTextPrimary, getTheme()))
                        .setLabel(getString(R.string.friends_add_friend))
                        .create()
        );
        fab.addActionItem(
                new SpeedDialActionItem
                        .Builder(R.id.friend_fab_create_group, R.drawable.ic_people_black_24dp)
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.colorLightTextPrimary, getTheme()))
                        .setLabel(getString(R.string.friends_create_group))
                        .create()
        );

        fab.setOnActionSelectedListener(fabListener);
    }

    SpeedDialView.OnActionSelectedListener fabListener = speedDialActionItem -> {
        switch (speedDialActionItem.getId()) {
            case R.id.friend_fab_add_friend:
                startActivity(new Intent(this,  AddFriendQRActivity.class));
                return false; // true to keep the Speed Dial open
            case R.id.friend_fab_create_group:
                startActivity(new Intent(this, CreateGroupChatActivity.class));
                return false;
            default:
                return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(convUpdateReceiver, new IntentFilter(ConversationManager.BROADCAST_CONVERSATION_UPDATED));
        registerReceiver(msgUpdateReceiver, new IntentFilter(ConversationManager.BROADCAST_MESSAGE_READ_CHANGE));
        loadContactList();
        loadChatsList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(convUpdateReceiver);
        unregisterReceiver(msgUpdateReceiver);
    }


    private void loadContactList() {
        contactList.clear();
        ArrayList<String> friendList = cm.getFriendList();

        for (String friendUid: friendList) {
            Conversation conv = cm.getConversation(cm.getPrivateConvId(friendUid));
            contactList.add(new ContactItem(conv));
        }

        recyclerFriendsAdapter.notifyDataSetChanged();
    }

    private void loadChatsList(){
        chatsList.clear();

        for (Conversation conv : cm.getConversations()){
            chatsList.add(new ContactItem(conv));
        }
        sortChatsList();
        recyclerChatsAdapter.notifyDataSetChanged();
    }

    private void sortChatsList() {
        // chatsList.sort((p1, p2) -> p2.getTimeForSort().compareTo(p1.getTimeForSort()));
        Collections.sort(chatsList, (p1, p2) -> p2.getTimeForSort().compareTo(p1.getTimeForSort()));
    }

}
