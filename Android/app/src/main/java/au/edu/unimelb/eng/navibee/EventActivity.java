package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    private ArrayList<String> eventName = new ArrayList<>();
    private ArrayList<String> eventSummary = new ArrayList<>();
    private ArrayList<Integer> eventImageId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // load data
        eventName.add("party");
        eventSummary.add("welcome everyone");
        eventImageId.add(R.mipmap.ic_launcher);

        List<Map<String, Object>> eventList = new ArrayList<>();
        for (int i=0; i<eventName.size(); i++) {
            Map<String, Object> eventItem = new HashMap<>();
            eventItem.put("Name", eventName.get(i));
            eventItem.put("Summary", eventSummary.get(i));
            eventItem.put("ImageId", eventImageId.get(i));
            eventList.add(eventItem);
        }

        // create Adapter and bind with eventList
        SimpleAdapter eventListAdapter = new SimpleAdapter(getApplicationContext(), eventList, R.layout.event_item,
                new String[]{"Name", "Summary", "ImageId"}, new int[]{R.id.name, R.id.summary, R.id.image});
        ListView eventListView = findViewById(R.id.event_list_view);
        eventListView.setAdapter(eventListAdapter);

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
