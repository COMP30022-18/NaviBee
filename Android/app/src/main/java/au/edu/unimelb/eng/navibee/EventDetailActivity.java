package au.edu.unimelb.eng.navibee;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String uid;
    private String eid;
    private Map<String, Boolean> users;
    private String relationship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eid = getIntent().getStringExtra("eventId");
        relationship = getIntent().getStringExtra("relationship");

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
                    deleteEvent();
                    break;
                default:
                    break;
            }
        }
        else if(relationship.equals("participant")) {
            switch (item.getItemId()) {
                case Menu.FIRST + 0:
                    Toast.makeText(this, "Quit is clicked", Toast.LENGTH_SHORT).show();
                    quitEvent();
                    break;
                default:
                    break;
            }
        }
        else {
            switch (item.getItemId()) {
                case Menu.FIRST + 0:
                    Toast.makeText(this, "Join is clicked", Toast.LENGTH_SHORT).show();
                    joinEvent();
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

    private void quitEvent() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle( "Alert" );
        dialog.setMessage("Are you sure you want to QUIT this event?");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }});
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("users." + uid, FieldValue.delete());
                db.collection("events").document(eid).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Task completed successfully
                            finish();
                        } else {
                            // Task failed with an exception
                        }
                    }
                });
            }});
        dialog.show();
    }

    private void deleteEvent() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle( "Alert" );
        dialog.setMessage("Are you sure you want to DELETE this event?");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }});
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                db.collection("events").document(eid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Task completed successfully
                            finish();
                        } else {
                            // Task failed with an exception
                        }
                    }
                });
            }});
        dialog.show();
    }

    private void joinEvent() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle( "Alert" );
        dialog.setMessage("Are you sure you want to JOIN this event?");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }});
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("users." + uid, true);
                db.collection("events").document(eid).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Task completed successfully
                            finish();
                        } else {
                            // Task failed with an exception
                        }
                    }
                });
            }});
        dialog.show();
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
