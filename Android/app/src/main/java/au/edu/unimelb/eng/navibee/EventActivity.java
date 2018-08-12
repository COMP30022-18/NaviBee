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

    ArrayList<String> eventList = new ArrayList<String>();
    ArrayAdapter<String> eventListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        //db = FirebaseFirestore.getInstance();
        //userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // create Adapter and bind with eventList
        eventListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                eventList);

        // find View and apply Adapter
        ListView eventListView = findViewById(R.id.event_list_view);
        eventListView.setAdapter(eventListAdapter);

        // load data
        for (int i=0; i < 100; i++){
            eventList.add(Integer.toString(i));
        }
        //loadContactList();

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
