package au.edu.unimelb.eng.navibee;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import au.edu.unimelb.eng.navibee.utils.SimpleRVIndefiniteProgressBar;
import au.edu.unimelb.eng.navibee.utils.SimpleRVTextPrimarySecondaryStatic;
import au.edu.unimelb.eng.navibee.utils.SimpleRecyclerViewAdaptor;
import au.edu.unimelb.eng.navibee.utils.SimpleRecyclerViewItem;

public class EventDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String uid;
    private String eid;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter viewAdapter;
    private RecyclerView.LayoutManager viewManager;
    private ArrayList<SimpleRecyclerViewItem> listItems = new ArrayList<>();

    private EventActivity.EventItem eventItem;
    private Map<String, String> result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eid = getIntent().getStringExtra("eventId");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.event_details_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        listItems.add(new SimpleRVIndefiniteProgressBar());

        // Recycler View
        recyclerView = (RecyclerView) findViewById(R.id.event_details_recycler_view);

        viewManager = new LinearLayoutManager(this);
        viewAdapter = new SimpleRecyclerViewAdaptor(listItems);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(viewManager);
        recyclerView.setAdapter(viewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        db.collection("events").document(eid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eventItem = documentSnapshot.toObject(EventActivity.EventItem.class);

                getEventInfo();
            }
        });

    }

    private void getEventInfo() {
        ArrayList<String> uidList = new ArrayList<>();

        uidList.add(eventItem.getHolder());
        uidList.addAll(eventItem.getUsers().keySet());

        Map<String, Object> data = new HashMap<>();
        data.put("uidList", uidList);

        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

        mFunctions
            .getHttpsCallable("getNamesFromUidList")
            .call(data)
            .addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                @Override
                public void onSuccess(HttpsCallableResult httpsCallableResult) {
                    result = (Map<String, String>) httpsCallableResult.getData();

                    listItems.clear();

                    if (eventItem.getName() != null && eventItem.getTime_() != null) {
                        listItems.add(new SimpleRVTextPrimarySecondaryStatic(
                                eventItem.getName(),
                                new SimpleDateFormat(getResources().getString(R.string.date_format)).format(eventItem.getTime_())
                        ));
                    }

                    if (eventItem.getLocation() != null) {
                        listItems.add(new SimpleRVTextPrimarySecondaryStatic(
                                getResources().getString(R.string.event_details_location),
                                eventItem.getLocation()
                        ));
                    }

                    String holder = null;
                    HashSet<String> participants = new HashSet<>();

                    for (Map.Entry<String, String> entry : result.entrySet()) {
                        if (entry.getKey().equals(eventItem.getHolder())) {
                            holder = entry.getValue();
                        } else {
                            participants.add(entry.getValue());
                        }
                    }

                    if (eventItem.getHolder() != null) {
                        listItems.add(new SimpleRVTextPrimarySecondaryStatic(
                                getResources().getString(R.string.event_details_organiser),
                                holder
                        ));
                    }

                    if (eventItem.getUsers() != null) {
                        listItems.add(new SimpleRVTextPrimarySecondaryStatic(
                                getResources().getString(R.string.event_details_participants),
                                participants.toString()
                        ));
                    }

                    viewAdapter.notifyDataSetChanged();
                }
            });
    }
}
