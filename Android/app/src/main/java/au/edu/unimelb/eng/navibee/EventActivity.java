package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EventActivity extends AppCompatActivity {

    public static class EventItem {

        private String holder;
        private String eventId;
        private String name;
        private String location;
        private Timestamp time;
        private Map<String, Boolean> users;
        private Boolean isTag = false;

        public EventItem(){}

        public EventItem(String name, String holder, String location, Timestamp time, Map<String, Boolean> users){
            this.holder = holder;
            this.name = name;
            this.location = location;
            this.users = users;
            this.time = time;
        }

        public EventItem(String name, String holder, String location, Date time, Map<String, Boolean> users){
            this.holder = holder;
            this.name = name;
            this.location = location;
            this.users = users;
            this.time = new Timestamp(time);
        }

        public String getHolder() { return holder; }

        public String getName(){
            return name;
        }

        public String getLocation(){
            return location;
        }

        public Map<String, Boolean> getUsers(){
            return users;
        }

        public Boolean isTag() {
            return isTag;
        }

        public void setTag(Boolean isTag){
            this.isTag = isTag;
        }

        public String getEventId(){
            return eventId;
        }

        public void setEventId(String eventId){
            this.eventId = eventId;
        }

        public Timestamp getTime() { return time; }

        public Date getTime_() { return time.toDate(); }
    }

    private FirebaseFirestore db;
    private String userId;

    private List<EventItem> eventList;
    private List<EventItem> preLoadEventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // load EventList
        loadPreLoadEventList();

    }

    @Override
    public void onRestart() {
        super.onRestart();
        loadPreLoadEventList();
    }

    private void loadPreLoadEventList() {
        preLoadEventList = new ArrayList<>();
        db.collection("events").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                EventItem eventItem = document.toObject(EventItem.class);
                                eventItem.setEventId(document.getId());
                                preLoadEventList.add(eventItem);
                            }
                        } else {
                            // fail to pull data
                        }
                        loadEventList();
                        finalizeEventList();
                    }
                });
    }

    private void loadEventList() {
        ArrayList<EventItem> holdingList = new ArrayList<>();
        ArrayList<EventItem> joinedList = new ArrayList<>();
        ArrayList<EventItem> recommendList = new ArrayList<>();

        EventItem holdingTag = new EventItem("YOU ARE HOLDING", null, null, new Date(), null);
        holdingTag.setTag(true);
        holdingList.add(holdingTag);
        EventItem YouJoinedTag = new EventItem("YOU JOINED", null, null, new Date(), null);
        YouJoinedTag.setTag(true);
        joinedList.add(YouJoinedTag);
        EventItem recommendTag = new EventItem("RECOMMEND EVENT", null, null, new Date(), null);
        recommendTag.setTag(true);
        recommendList.add(recommendTag);

        for(EventItem i: preLoadEventList) {
            if(i.getHolder().equals(userId)){
                holdingList.add(i);
            }
            if((!i.getHolder().equals(userId)) && i.getUsers().keySet().contains(userId)){
                joinedList.add(i);
            }
            if(!i.getUsers().keySet().contains(userId)){
                recommendList.add(i);
            }
        }
        eventList = new ArrayList<>();
        eventList.addAll(holdingList);
        eventList.addAll(joinedList);
        eventList.addAll(recommendList);

    }

    private void finalizeEventList(){
        // create Adapter and bind with eventList
        ListView eventListView = findViewById(R.id.event_list_view);
        EventListAdapter adapter = new EventListAdapter();
        eventListView.setAdapter(adapter);

        // set On Click Listener on eventListView and start next activity
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long l) {
                //using switch case, to check the condition.
                String relationship;
                if(eventList.get(pos).getHolder().equals(userId)) {
                    relationship = "holder";
                }
                else if(eventList.get(pos).getUsers().keySet().contains(userId)){
                    relationship = "participant";
                }
                else {
                    relationship = "passerby";
                }
                Intent intent = new Intent(EventActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventId", eventList.get(pos).getEventId());
                intent.putExtra("relationship", relationship);
                startActivity(intent);
            }
        });
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
            if(((EventItem)getItem(position)).isTag()){
                return false;
            }
            return super.isEnabled(position);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View view=convertView;
            if(((EventItem)getItem(position)).isTag()){
                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.event_tag, null);

                TextView tagView = (TextView) view.findViewById(R.id.event_list_item_tag);
                tagView.setText((String) eventList.get(position).getName());

            }else{
                view=LayoutInflater.from(getApplicationContext()).inflate(R.layout.event_item, null);

                TextView name = (TextView) view.findViewById(R.id.event_name);
                name.setText((String) eventList.get(position).getName());

                TextView summary = (TextView) view.findViewById(R.id.event_summary);
                String summaryText = new SimpleDateFormat("EEE, MMM d, HH:mm").format(eventList.get(position).getTime_());
                summary.setText((String) summaryText);

                ImageView image = (ImageView) view.findViewById(R.id.event_image);
                image.setImageResource(R.mipmap.ic_launcher);

            }
            return view;
        }

    }

    public void selectFriends(View view) {
        startActivity(new Intent(this, EventEditActivity.class));
    }

}
