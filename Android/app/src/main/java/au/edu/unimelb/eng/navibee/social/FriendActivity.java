package au.edu.unimelb.eng.navibee.social;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
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

        public CharSequence getLastMessageTime (){
            if (!hasMessage()) return "";
            return FormatUtilityKt.chatDateShortFormat(
                    conv.getMessage(conv.getMessageCount() - 1).getTime_().getTime()
            );
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
                textView.setText(((GroupConversation) conv).getName());
                imageView.setImageResource(R.drawable.navibee_placeholder);
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
        private FriendActivity friendActivity;


        public FriendAdapter(FriendActivity context, ArrayList<ContactItem> contactList, RecyclerView mRecyclerView){
            this.friendActivity = context;
            this.contactList = contactList;
            this.mRecyclerView = mRecyclerView;
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }

        public FriendAdapter.FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
            v.setOnClickListener(this);
            FriendViewHolder vh = new FriendViewHolder(v);
            return vh;
        }

        public void onBindViewHolder(FriendViewHolder holder, int position){
            ContactItem tempPerson = contactList.get(position);
            tempPerson.displayNameAndIcon(holder.itemView.findViewById(R.id.friend_item_name),
                    holder.itemView.findViewById(R.id.friend_item_icon));

        }

        @Override
        public void onClick(final View view) {
            int itemPosition = mRecyclerView.getChildLayoutPosition(view);
            ContactItem tempPerson = contactList.get(itemPosition);
            Intent intent = new Intent(friendActivity.getBaseContext(), FriendDetail.class);
            PrivateConversation tempConv = (PrivateConversation)  tempPerson.getConv();
            intent.putExtra("CONV_ID", tempConv.getConvId());
            intent.putExtra("FRIEND_ID", tempConv.getTargetUid());
            friendActivity.startActivity(intent);

        }

    }

    public static class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> implements View.OnClickListener {
        public static class ChatViewHolder extends RecyclerView.ViewHolder{

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private ArrayList<ContactItem> chatsList;
        private RecyclerView mRecyclerView;
        private FriendActivity friendActivity;


        public ChatsAdapter(FriendActivity context, ArrayList<ContactItem> chatsList, RecyclerView mRecyclerView){
            this.friendActivity = context;
            this.chatsList = chatsList;
            this.mRecyclerView = mRecyclerView;
        }

        @Override
        public int getItemCount() {
            return chatsList.size();
        }

        public ChatsAdapter.ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
            v.setOnClickListener(this);
            ChatViewHolder vh = new ChatViewHolder(v);
            return vh;
        }

        public void onBindViewHolder(ChatViewHolder holder, int position){
            TextView unread = holder.itemView.findViewById(R.id.chat_list_item_unread);

            ContactItem tempChat = chatsList.get(position);

            tempChat.displayNameAndIcon(holder.itemView.findViewById(R.id.chat_list_item_name),
                    holder.itemView.findViewById(R.id.chat_list_item_icon));
            unread.setText(Integer.toString(tempChat.getUnreadMessage()));
            if (tempChat.getUnreadMessage() == 0){
                unread.setVisibility(View.INVISIBLE);
            }
            else{
                unread.setVisibility(View.VISIBLE);
            }
            if (tempChat.hasMessage()) {
                ((TextView) holder.itemView.findViewById(R.id.chat_list_item_last_message)).setText(tempChat.getLastMessage());
                ((TextView) holder.itemView.findViewById(R.id.chat_list_item_time)).setText(tempChat.getLastMessageTime());
            } else {
                ((TextView) holder.itemView.findViewById(R.id.chat_list_item_last_message)).setText("");
                ((TextView) holder.itemView.findViewById(R.id.chat_list_item_time)).setText("");
            }
        }
        @Override
        public void onClick(final View view) {
            int itemPosition = mRecyclerView.getChildLayoutPosition(view);
            ContactItem tempChat = chatsList.get(itemPosition);
            Intent intent = new Intent(friendActivity.getBaseContext(), ChatActivity.class);
            Conversation tempConv = tempChat.getConv();
            intent.putExtra("CONV_ID", tempConv.getConvId());
            friendActivity.startActivity(intent);
        }
    }

    private ConversationManager cm = ConversationManager.getInstance();
    private ArrayList<ContactItem> contactList = new ArrayList<>();
    private ArrayList<ContactItem> chatsList = new ArrayList<>();

    private ChatsAdapter recyclerChatsAdapter;
    private RecyclerView recyclerChatsList;
    private RecyclerView.LayoutManager recyclerChatsManager;


    private FriendAdapter recyclerFriendsAdapter;
    private RecyclerView recyclerFriendsList;
    private RecyclerView.LayoutManager recyclerFriendsManager;

    private BottomNavigationView navigation;


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
        // use a linear layout manager
        recyclerFriendsManager = new LinearLayoutManager(this);
        recyclerFriendsList.setLayoutManager(recyclerFriendsManager);
        // specify an adapter (see also next example)
        recyclerFriendsAdapter = new FriendAdapter(this, contactList, recyclerFriendsList);
        recyclerFriendsList.setAdapter(recyclerFriendsAdapter);
        recyclerFriendsList.setVisibility(View.INVISIBLE);

        recyclerFriendsList.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        recyclerChatsList = findViewById(R.id.chatsRecyclerView);
        recyclerChatsList.setHasFixedSize(true);
        // use a linear layout manager
        recyclerChatsManager = new LinearLayoutManager(this);
        recyclerChatsList.setLayoutManager(recyclerChatsManager);
        // specify an adapter (see also next example)
        recyclerChatsAdapter = new ChatsAdapter(this, chatsList, recyclerChatsList);
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

        loadChatsList();
        loadContactList();

        IntentFilter intFilt = new IntentFilter(ConversationManager.BROADCAST_FRIEND_UPDATED);
        registerReceiver(br, intFilt);

        registerReceiver(brMsgReadState, new IntentFilter(ConversationManager.BROADCAST_MESSAGE_READ_CHANGE));
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

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadContactList();
            loadChatsList();
        }
    };

    BroadcastReceiver brMsgReadState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recyclerFriendsAdapter.notifyDataSetChanged();
            sortChatsList();
            recyclerChatsAdapter.notifyDataSetChanged();
        }
    };

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
        chatsList.sort((p1, p2) -> p2.getTimeForSort().compareTo(p1.getTimeForSort()));
    }

}