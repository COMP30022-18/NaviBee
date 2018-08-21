package au.edu.unimelb.eng.navibee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class EventEditActivity extends AppCompatActivity {

    private ArrayList<String> selectedUidList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_edit);

        Intent intent = getIntent();
        selectedUidList = intent.getStringArrayListExtra("selectedUid");
    }
}
