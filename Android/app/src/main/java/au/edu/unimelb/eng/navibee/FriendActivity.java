package au.edu.unimelb.eng.navibee;

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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.edu.unimelb.eng.navibee.Social.FriendManager;

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
            }

            return convertView;
        }

        public static class ViewHolder {
            public ImageView image;
            public TextView text;
        }
        private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
            ImageView bmImage;

            public DownloadImageTask(ImageView bmImage) {
                this.bmImage = bmImage;
            }

            protected Bitmap doInBackground(String... urls) {
                String urldisplay = urls[0];
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
                return mIcon11;
            }

            protected void onPostExecute(Bitmap result) {
                bmImage.setImageBitmap(result);
            }
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

        contactListAdapter = new FriendAdapter(this,
                contactList);

        ListView listView = (ListView) findViewById(R.id.contactListView);
        listView.setAdapter(contactListAdapter);

        loadContactList();

        IntentFilter intFilt = new IntentFilter(FriendManager.BROADCAST_FRIEND_UPDATED);
        registerReceiver(br, intFilt);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long l) {
                //using switch case, to check the condition.
                Intent intent = new Intent(FriendActivity.this, ChatActivity.class);
                intent.putExtra("userID", contactListAdapter.getItem(pos).getUid());
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

    private void loadContactList() {

        FriendManager.getInstance().fetchContactPersonList(contactList);
        contactListAdapter.notifyDataSetChanged();
    }
}