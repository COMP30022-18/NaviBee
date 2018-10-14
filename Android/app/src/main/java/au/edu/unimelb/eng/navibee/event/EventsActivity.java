package au.edu.unimelb.eng.navibee.event;

import android.content.Intent;
import android.os.Bundle;
import android.util.EventLogTags;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.EventRVDivider;
import au.edu.unimelb.eng.navibee.utils.EventRVEntry;
import au.edu.unimelb.eng.navibee.utils.EventRVIndefiniteProgressBar;
import au.edu.unimelb.eng.navibee.utils.EventRVItem;
import au.edu.unimelb.eng.navibee.utils.EventsRVAdaptor;

public class EventsActivity extends AppCompatActivity {

    // Event item class
    public static class EventItem {

        private String holder;
        private String name;
        private Timestamp time;
        private Map<String, Boolean> users;
        private ArrayList<String> images;
        private String placeName;
        private double longitude;
        private double latitude;
        private Boolean isPrivate;

        public EventItem(){}

        public EventItem(String name, String holder, Timestamp time, Map<String, Boolean> users, ArrayList<String> images, String placeName, double longitude, double latitude, Boolean isPrivate){
            this.holder = holder;
            this.name = name;
            this.users = users;
            this.time = time;
            this.images = images;
            this.placeName = placeName;
            this.longitude = longitude;
            this.latitude = latitude;
            this.isPrivate = isPrivate;
        }

        public EventItem(String name, String holder, Date time, Map<String, Boolean> users, ArrayList<String> images, String placeName, double longitude, double latitude, Boolean isPrivate){
            this.holder = holder;
            this.name = name;
            this.users = users;
            this.time = new Timestamp(time);
            this.images = images;
            this.placeName = placeName;
            this.longitude = longitude;
            this.latitude = latitude;
            this.isPrivate = isPrivate;
        }

        public Boolean getIsPrivate() { return isPrivate; }

        public String getPlaceName(){ return placeName; }

        public double getLongitude() {
            return longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public String getHolder() { return holder; }

        public String getName(){
            return name;
        }

        public Map<String, Boolean> getUsers(){
            return users;
        }

        public Timestamp getTime() { return time; }

        public ArrayList<String> getImages() {
            ArrayList<String> result = new ArrayList<>();
            if(images != null) {
                result.addAll(images);
            }
            return result;
        }

        @Exclude
        public Date getTime_() { return time.toDate(); }
    }

    private String uid;
    private FirebaseFirestore db;
    private Map<EventItem, String> eventIdMap;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter viewAdapter;
    private RecyclerView.LayoutManager viewManager;

    private ArrayList<EventItem> eventList;
    private ArrayList<EventRVItem> events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // loading
        events.add(new EventRVIndefiniteProgressBar());
        eventIdMap = new HashMap<>();

        // set up recycler view
        recyclerView = (RecyclerView) findViewById(R.id.events_recycler_view);
        viewManager = new LinearLayoutManager(this);
        viewAdapter = new EventsRVAdaptor(events);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(viewManager);
        recyclerView.setAdapter(viewAdapter);
//        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        loadEvents();

    }

    @Override
    public void onRestart() {
        super.onRestart();
        loadEvents();
    }

    private void loadEvents() {
        eventList = new ArrayList<>();

        db.collection("events").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        EventItem eventItem = document.toObject(EventItem.class);
                        eventIdMap.put(eventItem, document.getId());
                        eventList.add(eventItem);
                    }

                } else {
                    // fail to pull data
                }
                updateRecyclerVIew();
            }
        });
    }

    private void updateRecyclerVIew() {
        ArrayList<EventItem> organisedList = new ArrayList<>();
        ArrayList<EventItem> joinedList = new ArrayList<>();
        ArrayList<EventItem> recommendedList = new ArrayList<>();

        for (EventItem i : eventList) {
            if (i.getHolder().equals(uid)) {
                organisedList.add(i);
            }
            if ((!i.getHolder().equals(uid)) && i.getUsers().keySet().contains(uid)) {
                joinedList.add(i);
            }
            if (!i.getUsers().keySet().contains(uid) && !i.getIsPrivate()) {
                recommendedList.add(i);
            }
        }

        events.clear();
        // Organised event list
        if (!organisedList.isEmpty()) {
            events.add(new EventRVDivider(getResources().getString(R.string.event_organised_events)));
            addToEntry(organisedList);
        }

        // Joined event list
        if (!joinedList.isEmpty()) {
            events.add(new EventRVDivider(getResources().getString(R.string.event_joined_events)));
            addToEntry(joinedList);
        }

        // Recommended event list
        if (!recommendedList.isEmpty()) {
            events.add(new EventRVDivider(getResources().getString(R.string.event_recommended_events)));
            addToEntry(recommendedList);
        }

        viewAdapter.notifyDataSetChanged();
    }

    private void addToEntry(ArrayList<EventItem> list) {
        for (EventItem i : list) {
            String eventUrl = null;
            if (!i.images.isEmpty()) {
                eventUrl = i.images.get(0);
            }
            events.add(new EventRVEntry(
                    i.getName(),
                    i.getPlaceName(),
                    eventUrl,
                    view -> {
                        Intent intent = new Intent(EventsActivity.this, EventDetailsActivity.class);
                        intent.putExtra("eventId", eventIdMap.get(i));
                        startActivity(intent);
                    }
            ));
        }
    }

    public void selectFriends(View view) {
        startActivity(new Intent(this, EventEditActivity.class));
    }

}
