package au.edu.unimelb.eng.navibee.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.DownloadImageTask;

public class FriendActivity extends AppCompatActivity {

    public static class FriendAdapter extends BaseAdapter {
        private ArrayList<FriendManager.ContactPerson> contactList;
        private LayoutInflater l_Inflater;

        public FriendAdapter(Context context, ArrayList<FriendManager.ContactPerson> contactList){
            this.contactList = contactList;
            l_Inflater = LayoutInflater.from(context);
        }

        public int getCount(){
            return contactList.size();
        }
        public FriendManager.ContactPerson getItem(int position){
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
                FriendManager.ContactPerson tempPerson = contactList.get(position);
                holder.text.setText(tempPerson.getName());
                new DownloadImageTask(holder.image)
                        .execute(tempPerson.getUrl());
                holder.lastTime.setText(tempPerson.getLastMessageTime());
                holder.unread.setText(Integer.toString(tempPerson.getUnreadMessage()));
                if (tempPerson.getUnreadMessage() == 0){
                    holder.unread.setVisibility(View.INVISIBLE);
                }
                else{
                    holder.unread.setVisibility(View.VISIBLE);
                }
                if (tempPerson.hasMessage()){
                    holder.lastMessage.setText(tempPerson.getLastMessage());
                }
                else{
                    holder.lastMessage.setText("");
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

    ArrayList<FriendManager.ContactPerson> contactList = new ArrayList<FriendManager.ContactPerson>();
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

        IntentFilter intFilt = new IntentFilter(FriendManager.BROADCAST_FRIEND_UPDATED);
        registerReceiver(br, intFilt);

        registerReceiver(brMsgReadState, new IntentFilter(ConversationManager.BROADCAST_MESSAGE_READ_CHANGE));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long l) {
                //using switch case, to check the condition.

                String targetUID= contactListAdapter.getItem(pos).getUid();

                if (!ConversationManager.getInstance().isConversationExists(targetUID)) {
                    Toast.makeText(FriendActivity.this, "ERROR: conversation not exists", Toast.LENGTH_LONG).show();

                } else {
                    Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                    intent.putExtra("TARGET_USER_ID", targetUID);
                    startActivity(intent);
                }


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

        FriendManager.getInstance().fetchContactPersonList(contactList);
        ConversationManager.getInstance().updateConvInfoForContactList(contactList);
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