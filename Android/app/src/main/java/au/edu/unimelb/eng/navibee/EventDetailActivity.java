package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        Intent intent = getIntent();
        String eventId = intent.getStringExtra(EventActivity.EVENT_ID);

        TextView eventNameView = (TextView) findViewById(R.id.textView5);
        eventNameView.setText(eventId);
    }
}
