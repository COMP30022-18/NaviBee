package au.edu.unimelb.eng.navibee.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.NetworkImageHelper;

public class FriendActivity extends AppCompatActivity {

    public static class ContactItem {
        private String uid; // no need for group chat
        private String convId;
//        private boolean isPrivate;
        private int unreadMessage;
        private String lastMessageTime;
        private String lastMessage;
        private Boolean hasLastMessage;


        public ContactItem(String convId, String uid) {
            this.uid = uid;
            this.convId = convId;
//            this.isPrivate = isPrivate;

            this.unreadMessage = 0;
            Date date = new Date();
            setLastMessageTime(date);
            hasLastMessage = false;
        }

        public String getConvId() {
            return convId;
        }

        public String getUid(){
            return this.uid;
        }

        public int getUnreadMessage(){
            return this.unreadMessage;
        }
        public void setLastMessage(String message){
            lastMessage = message;
            hasLastMessage = true;
        }
        public String getLastMessage(){
            return this.lastMessage;
        }
        public void noMessage(){
            hasLastMessage = false;
        }

        public Boolean hasMessage() {
            return hasLastMessage;
        }

        public String getLastMessageTime (){
            return this.lastMessageTime;
        }

        public void setUnreadMessage(int i){
            this.unreadMessage = i;
        }

        public void setLastMessageTime(Date time){
            this.lastMessageTime = DateManager.DateformatTime(time);
        }

    }

    public static class FriendAdapter extends BaseAdapter {
        private ArrayList<ContactItem> contactList;

        private LayoutInflater l_Inflater;

        public FriendAdapter(Context context, ArrayList<ContactItem> contactList){
            this.contactList = contactList;
            l_Inflater = LayoutInflater.from(context);
        }

        public int getCount(){
            return contactList.size();
        }
        public ContactItem getItem(int position){
            return contactList.get(position);
        }
        public long getItemId(int position){
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            ViewHolder holder;
            if (convertView == null) {
                convertView = l_Inflater.inflate(R.layout.friend_item, null);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.friend_icon);
                holder.text = (TextView) convertView.findViewById(R.id.friend_name);
                holder.lastTime = (TextView) convertView.findViewById(R.id.time);
                holder.unread = (TextView) convertView.findViewById(R.id.unread);
                holder.lastMessage = (TextView) convertView.findViewById(R.id.last_message);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (contactList.size() <= 0){
                holder.text.setText("No Data");
            }
            else{
                ContactItem tempPerson = contactList.get(position);

                // set name and avatar
                UserInfoManager.getInstance().getUserInfo(tempPerson.getUid(), userInfo -> {
                    holder.text.setText(userInfo.getName());
                    NetworkImageHelper.loadImage(holder.image, userInfo.getPhotoUrl());
                });


                holder.unread.setText(Integer.toString(tempPerson.getUnreadMessage()));
                if (tempPerson.getUnreadMessage() == 0){
                    holder.unread.setVisibility(View.INVISIBLE);
                }
                else{
                    holder.unread.setVisibility(View.VISIBLE);
                }
                if (tempPerson.hasMessage()){
                    holder.lastMessage.setText(tempPerson.getLastMessage());
                    holder.lastTime.setText(tempPerson.getLastMessageTime());
                }
                else{
                    holder.lastMessage.setText("");
                    holder.lastTime.setText("");
                }

            }

            return convertView;
        }

        public static class ViewHolder {
            public ImageView image;
            public TextView text;
            public TextView unread;
            public TextView lastTime;
            public TextView lastMessage;
        }
    }

    ArrayList<ContactItem> contactList = new ArrayList<ContactItem>();
    FriendAdapter contactListAdapter;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
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
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        contactListAdapter = new FriendAdapter(this, contactList);

        ListView listView = (ListView) findViewById(R.id.contactListView);
        listView.setAdapter(contactListAdapter);


        loadContactList();

        IntentFilter intFilt = new IntentFilter(ConversationManager.BROADCAST_FRIEND_UPDATED);
        registerReceiver(br, intFilt);

        registerReceiver(brMsgReadState, new IntentFilter(ConversationManager.BROADCAST_MESSAGE_READ_CHANGE));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long l) {
                //using switch case, to check the condition.

                Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                intent.putExtra("CONV_ID", contactListAdapter.getItem(pos).getConvId());
                startActivity(intent);

            }
        });
    }

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadContactList();
        }
    };

    BroadcastReceiver brMsgReadState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConversationManager.getInstance().updateConvInfoForContactList(contactList);
            contactListAdapter.notifyDataSetChanged();
        }
    };

    private void loadContactList() {
        ConversationManager cm = ConversationManager.getInstance();

        contactList.clear();
        ArrayList<String> friendList = cm.getFriendList();

        for (String friendUid: friendList) {
            contactList.add(new ContactItem(cm.getPrivateConvId(friendUid), friendUid));
        }

        cm.updateConvInfoForContactList(contactList);
        contactListAdapter.notifyDataSetChanged();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addFriendButton:
                startActivity(new Intent(this,  AddFriendQRActivity.class));
                break;
        }
    }

}