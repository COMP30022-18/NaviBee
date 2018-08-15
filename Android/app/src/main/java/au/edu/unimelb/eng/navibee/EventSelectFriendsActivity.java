package au.edu.unimelb.eng.navibee;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class EventSelectFriendsActivity extends AppCompatActivity {

    String[] city= {
            "Bangalore",
            "Chennai",
            "Mumbai",
            "Pune",
            "Delhi",
            "Jabalpur",
            "Indore",
            "Ranchi",
            "Hyderabad",
            "Ahmedabad",
            "Kolkata",
            "Bhopal"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_select_friends_list);

        ListView friendListView = findViewById(R.id.event_select_friends_list);
        friendListView.setChoiceMode(friendListView.CHOICE_MODE_MULTIPLE);

        friendListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_checked,city));

        friendListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View v,
                                            int position, long id) {
                        CheckedTextView item = (CheckedTextView) v;

                        Toast.makeText(getApplicationContext(), city[position] + " checked : " +
                                item.isChecked(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }
}
