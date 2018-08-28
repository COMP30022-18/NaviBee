package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String uid;
    private String eid;
    private Map<String, Boolean> users;
    private String relationship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eid = getIntent().getStringExtra("eventId");
        relationship = getIntent().getStringExtra("relationship");

//        getEventInfo();
//
//        joinEvent();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(relationship.equals("holder")) {
            menu.add(Menu.NONE, Menu.FIRST + 0, 0, "Edit the Event");
            menu.add(Menu.NONE, Menu.FIRST + 1, 1, "Delete the Event");
        }
        else if(relationship.equals("participant")) {
            menu.add(Menu.NONE, Menu.FIRST + 0, 0, "Quit the Event");
        }
        else {
            menu.add(Menu.NONE, Menu.FIRST + 0, 0, "Join the Event");
            menu.add(Menu.NONE, Menu.FIRST + 1, 1, "Follow the Event");
        }
        getMenuInflater().inflate(R.menu.menu_event_detial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(relationship.equals("holder")) {
            switch (item.getItemId()) {
                case Menu.FIRST + 0:
                    Toast.makeText(this, "Edit is clicked", Toast.LENGTH_SHORT).show();
                    break;
                case Menu.FIRST + 1:
                    Toast.makeText(this, "Delete is clicked", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        else if(relationship.equals("participant")) {
            switch (item.getItemId()) {
                case Menu.FIRST + 0:
                    Toast.makeText(this, "Quit is clicked", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        else {
            switch (item.getItemId()) {
                case Menu.FIRST + 0:
                    Toast.makeText(this, "Join is clicked", Toast.LENGTH_SHORT).show();
                    break;
                case Menu.FIRST + 1:
                    Toast.makeText(this, "Follow is clicked", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        return true;
    }
















//    private void getEventInfo() {
//
//        TextView eventName = findViewById(R.id.eventName);
//        TextView eventAddress = findViewById(R.id.eventAddress);
//        TextView eventTime = findViewById(R.id.eventTime);
//
//        ArrayList<String> userList = new ArrayList<>();
//
//        db.collection("events").document(eid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//
//                    String name = document.getString("name");
//                    Date date = document.getDate("time");
//                    String time = new SimpleDateFormat("EEE, MMM d, HH:mm").format(date);
//                    String address = document.getString("location");
//
//                    eventName.setText(name);
//                    eventTime.setText(time);
//                    eventAddress.setText(address);
//
//                    users = ((Map<String, Boolean>) document.getData().get("users"));
//
//                    for (Map.Entry<String, Boolean> entry : users.entrySet()) {
//                        if (entry.getValue()) {
//                            userList.add(entry.getKey());
//                        }
//                    }
//
//                    // TODO get event participants' name
//
//                } else {
//                    // TODO if fails
//                }
//            }
//        });
//    }

//    private void joinEvent() {
//
//        Button joinBtn = findViewById(R.id.joinButton);
//
//        joinBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                // reget the user list of event
//                db.collection("events").document(eid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            users = ((Map<String, Boolean>) document.getData().get("users"));
//                        } else {
//                            // TODO if fails
//                        }
//                    }
//                });
//
//                // check if the user already join the event
//                for (Map.Entry<String, Boolean> entry : users.entrySet()) {
//                    if (entry.getKey().equals(uid) && entry.getValue().equals(true)) {
//                        Toast.makeText(EventDetailActivity.this,
//                                "Already join the event", Toast.LENGTH_LONG).show();
//                        return;
//                    }
//                }
//
//                Map<String, Object> data = new HashMap<>();
//                Map<String, Boolean> newData = new HashMap<>();
//
//                newData.put(uid, true);
//
//                data.put("users", newData);
//                db.collection("events").document(eid).set(data, SetOptions.merge());
//
//                Toast.makeText(EventDetailActivity.this,
//                        "Successfully join the event", Toast.LENGTH_LONG).show();
//
//            }
//        });
//    }
}
