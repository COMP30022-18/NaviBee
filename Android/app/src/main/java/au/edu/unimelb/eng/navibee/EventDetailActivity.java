package au.edu.unimelb.eng.navibee;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class EventDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail);

        db = FirebaseFirestore.getInstance();
        eid = getIntent().getStringExtra("eventId");

        getEventInfo();

    }

    private void getEventInfo() {

        TextView eventName = findViewById(R.id.eventName);
        TextView eventAddress = findViewById(R.id.eventAddress);
        TextView eventTime = findViewById(R.id.eventTime);

        ArrayList<String> userList = new ArrayList<>();

        db.collection("events").document(eid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    String name = document.getString("name");
                    Date date = document.getDate("time");
                    String time = new SimpleDateFormat("EEE, MMM d, HH:mm").format(date);
                    String address = document.getString("location");

                    eventName.setText(name);
                    eventTime.setText(time);
                    eventAddress.setText(address);

                    Map<String, Boolean> docs = ((Map<String, Boolean>) document.getData().get("users"));

                    for (Map.Entry<String, Boolean> entry : docs.entrySet()) {
                        if (entry.getValue()) {
                            userList.add(entry.getKey());
                        }
                    }

                } else {
                    // TODO if fails
                }
            }
        });
    }
}
