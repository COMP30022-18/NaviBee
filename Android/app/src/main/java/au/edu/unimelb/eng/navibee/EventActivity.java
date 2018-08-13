package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static java.util.stream.IntStream.range;

public class EventActivity extends AppCompatActivity {

    public final static String USER_ID = "au.edu.unimelb.eng.navibee.USERID";
    private FirebaseFirestore db;
    private String userId;

    private List<Map<String, Object>> eventList = new ArrayList<>();
    private List<Map<String, Object>> eventTag = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_event);

        // load data
        loadEventList();

        // create Adapter and bind with eventList
        ListView eventListView = findViewById(R.id.event_list_view);
        EventListAdapter adapter = new EventListAdapter();
        eventListView.setAdapter(adapter);

        // set On Click Listener on eventListView and start next activity
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long l) {
                //using switch case, to check the condition.
                Intent intent = new Intent(EventActivity.this, EventDetialActivity.class);
                intent.putExtra(USER_ID, "userId");
                startActivity(intent);
            }
        });

    }

    private void loadEventList() {
        Map<String, Object> eventListItem = new HashMap<>();
        eventListItem.put("Name", "EventName");
        eventListItem.put("Summary", "summary");
        eventListItem.put("ImageId", R.mipmap.ic_launcher);

        Map<String, Object> eventTagItem = new HashMap<>();
        eventTagItem.put("Name", "Event Tag");
        eventTag.add(eventTagItem);

        eventList.add(eventTagItem);
        for(int i = 0;i < 10;i++) {
            eventList.add(eventListItem);
        }
    }

    private class EventListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return eventList.size();
        }
        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return eventList.get(position);
        }
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }
        @Override
        public boolean isEnabled(int position) {
            // TODO Auto-generated method stub
            if(eventTag.contains(getItem(position))){
                return false;
            }
            return super.isEnabled(position);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View view=convertView;
            if(eventTag.contains(getItem(position))){
                view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.event_tag, null);

                TextView tagView = (TextView) view.findViewById(R.id.event_list_item_tag);
                tagView.setText((String) eventList.get(position).get("Name"));

            }else{
                view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.event_item, null);

                TextView name = (TextView) view.findViewById(R.id.event_name);
                name.setText((String) eventList.get(position).get("Name"));

                TextView summary = (TextView) view.findViewById(R.id.event_summary);
                summary.setText((String) eventList.get(position).get("Summary"));

                ImageView image = (ImageView) view.findViewById(R.id.event_image);
                image.setImageResource((Integer) eventList.get(position).get("ImageId"));

            }

            return view;
        }

    }




//    private void loadContactList() {
//
//        DocumentReference docRef = db.collection("users").document(userId);
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        ArrayList<String> contacts = (ArrayList<String>)document.get("contacts");
//
//
//                        contactList.clear();
//                        contactList.addAll(contacts);
//                        contactListAdapter.notifyDataSetChanged();
//
//
//
//                        Log.d("Firestore", "test");
//
//
//                    } else {
//                        Log.d("Firestore", "data not exists");
//                    }
//                } else {
//                    Log.d("Firestore", "get failed with ", task.getException());
//                }
//            }
//        });
//    }
}
