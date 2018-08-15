package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventActivity extends AppCompatActivity {

    public final static String EVENT_ID = "au.edu.unimelb.eng.navibee.EVENTID";
    private FirebaseFirestore db;
    private String userId;

    private List<EventItem> eventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // load EventList
        loadEventList();

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
                Intent intent = new Intent(EventActivity.this, EventDetailActivity.class);
                intent.putExtra(EVENT_ID, eventList.get(pos).getEventId());
                startActivity(intent);
            }
        });
    }

    private void loadEventList() {

        EventItem ForYouTag = new EventItem("FOR YOU", null, null);
        ForYouTag.setTag(true);
        eventList.add(ForYouTag);

        db.collection("events").whereEqualTo("users." + userId, true).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                EventItem eventItem = document.toObject(EventItem.class);
                                eventItem.setEventId(document.getId());
                                eventList.add(eventItem);
                            }
                        } else {
                           // fail to pull data
                        }

                        finalizeEventList();
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
                summary.setText((String) eventList.get(position).getSummary());

                ImageView image = (ImageView) view.findViewById(R.id.event_image);
                image.setImageResource(R.mipmap.ic_launcher);

            }
            return view;
        }

    }

    public static class EventItem {

        private String eventId;
        private String name;
        private String summary;
        private Map<String, Boolean> users;
        private Boolean isTag = false;

        public EventItem(){}

        public EventItem(String name, String summary, Map<String, Boolean> users){
            this.name = name;
            this.summary = summary;
            this.users = users;
            this.isTag = isTag;
        }

        public String getName(){
            return name;
        }

        public String getSummary(){
            return summary;
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
    }

}
