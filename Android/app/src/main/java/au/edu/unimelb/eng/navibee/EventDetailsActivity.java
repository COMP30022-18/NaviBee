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

import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
                EventActivity.EventItem eventItem = documentSnapshot.toObject(EventActivity.EventItem.class);

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

                if (eventItem.getHolder() != null) {
                    listItems.add(new SimpleRVTextPrimarySecondaryStatic(
                            getResources().getString(R.string.event_details_organiser),
                            eventItem.getHolder()
                    ));
                }

                if (eventItem.getUsers() != null) {
                    listItems.add(new SimpleRVTextPrimarySecondaryStatic(
                            getResources().getString(R.string.event_details_participants),
                            eventItem.getUsers().keySet().toString()
                    ));
                }

                viewAdapter.notifyDataSetChanged();
            }
        });

    }
}
