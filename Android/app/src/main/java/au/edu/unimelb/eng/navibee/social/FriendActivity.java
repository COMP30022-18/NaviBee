package au.edu.unimelb.eng.navibee.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Toast;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Date;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.NetworkImageHelper;

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

        public String getLastMessageTime (){
            if (!hasMessage()) return "";
            return DateManager.DateformatTime(conv.getMessage(conv.getMessageCount()-1).getTime_());
        }

        public Date getTimeForSort() {
            if (hasMessage()) {
                return conv.getMessage(conv.getMessageCount()-1).getTime_();
            } else {
                return conv.getCreateTimestamp();
            }
        }

        public void displayNameAndIcon(TextView textView, ImageView imageView) {
            if (conv instanceof PrivateConversation) {
                UserInfoManager.getInstance().getUserInfo(((PrivateConversation) conv).getTargetUid(), userInfo -> {
                    textView.setText(userInfo.getName());
                    NetworkImageHelper.loadImage(imageView, userInfo.getPhotoUrl());
                });
            } else {
                textView.setText(((GroupConversation) conv).getName());
                imageView.setImageResource(R.drawable.ic_navibee_color);
            }
        }


    }

    public static class ViewHolder {
        public ImageView image;
        public TextView text;
        public TextView unread;
        public TextView lastTime;
        public TextView lastMessage;
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
            vh.setIsRecyclable(false);
            return vh;
        }

        public void onBindViewHolder(FriendViewHolder holder, int position){
            ContactItem tempPerson = contactList.get(position);
            tempPerson.displayNameAndIcon((TextView) holder.itemView.findViewById(R.id.friend_item_name),
                    (ImageView) holder.itemView.findViewById(R.id.friend_item_icon));

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
            vh.setIsRecyclable(false);
            return vh;
        }

        public void onBindViewHolder(ChatViewHolder holder, int position){
            ContactItem tempChat = chatsList.get(position);
            tempChat.displayNameAndIcon((TextView) holder.itemView.findViewById(R.id.chat_list_item_name),
                    (ImageView) holder.itemView.findViewById(R.id.chat_list_item_icon));
            ((TextView) holder.itemView.findViewById(R.id.chat_list_item_unread)).setText(Integer.toString(tempChat.getUnreadMessage()));
            if (tempChat.getUnreadMessage() == 0){
                holder.itemView.findViewById(R.id.chat_list_item_unread).setVisibility(View.INVISIBLE);
            }
            else{
                holder.itemView.findViewById(R.id.chat_list_item_unread).setVisibility(View.VISIBLE);
            }
            if (tempChat.hasMessage()){
                ((TextView) holder.itemView.findViewById(R.id.chat_list_item_last_message)).setText(tempChat.getLastMessage());
                ((TextView) holder.itemView.findViewById(R.id.chat_list_item_time)).setText(tempChat.getLastMessageTime());
            }
            else{
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
    private ArrayList<ContactItem> contactList = new ArrayList<ContactItem>();
    private ArrayList<ContactItem> chatsList = new ArrayList<ContactItem>();

    private ChatsAdapter recyclerChatsAdapter;
    private Button createGroupChatButton;
    private RecyclerView recyclerChatsList;
    private RecyclerView.LayoutManager recyclerChatsManager;


    private FriendAdapter recyclerFriendsAdapter;
    private Button addFriendButton;
    private RecyclerView recyclerFriendsList;
    private RecyclerView.LayoutManager recyclerFriendsManager;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.switch_friend_list:
                    recyclerChatsList.setVisibility(View.INVISIBLE);
                    createGroupChatButton.setVisibility(View.INVISIBLE);
                    recyclerFriendsList.setVisibility(View.VISIBLE);
                    addFriendButton.setVisibility(View.VISIBLE);
                    return true;
                case R.id.switch_recent_chat:
                    recyclerChatsList.setVisibility(View.VISIBLE);
                    createGroupChatButton.setVisibility(View.VISIBLE);
                    recyclerFriendsList.setVisibility(View.INVISIBLE);
                    addFriendButton.setVisibility(View.INVISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list);

//        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.friend_activity_navbar);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        recyclerFriendsList = (RecyclerView) findViewById(R.id.contactRecyclerView);
        recyclerFriendsList.setHasFixedSize(true);
        // use a linear layout manager
        recyclerFriendsManager = new LinearLayoutManager(this);
        recyclerFriendsList.setLayoutManager(recyclerFriendsManager);
        // specify an adapter (see also next example)
        recyclerFriendsAdapter = new FriendAdapter(this, contactList, recyclerFriendsList);
        recyclerFriendsList.setAdapter(recyclerFriendsAdapter);
        recyclerFriendsList.setVisibility(View.INVISIBLE);

        addFriendButton = (Button) findViewById(R.id.addFriendButton);
        addFriendButton.setVisibility(View.INVISIBLE);



        recyclerChatsList = (RecyclerView) findViewById(R.id.chatsRecyclerView);
        recyclerChatsList.setHasFixedSize(true);
        // use a linear layout manager
        recyclerChatsManager = new LinearLayoutManager(this);
        recyclerChatsList.setLayoutManager(recyclerChatsManager);
        // specify an adapter (see also next example)
        recyclerChatsAdapter = new ChatsAdapter(this, chatsList, recyclerChatsList);
        recyclerChatsList.setAdapter(recyclerChatsAdapter);
        recyclerChatsList.setVisibility(View.INVISIBLE);

        createGroupChatButton = (Button) findViewById(R.id.createGroupChatButton);
        recyclerChatsList.setVisibility(View.VISIBLE);




        loadChatsList();
        loadContactList();

        IntentFilter intFilt = new IntentFilter(ConversationManager.BROADCAST_FRIEND_UPDATED);
        registerReceiver(br, intFilt);

        registerReceiver(brMsgReadState, new IntentFilter(ConversationManager.BROADCAST_MESSAGE_READ_CHANGE));
    }

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

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addFriendButton:
                startActivity(new Intent(this,  AddFriendQRActivity.class));
                break;
            case R.id.createGroupChatButton:
                startActivity(new Intent(this, CreateGroupChatActivity.class));
                break;
        }
    }

}