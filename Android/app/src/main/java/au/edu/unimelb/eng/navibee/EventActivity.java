package au.edu.unimelb.eng.navibee;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
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

public class EventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;

    ArrayList<String> eventList = new ArrayList<String>();
    ArrayAdapter<String> eventListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        eventListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                eventList);

        ListView eventListView = (ListView) findViewById(R.id.event_list_view);
        eventListView.setAdapter(eventListAdapter);

        //loadContactList();
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
