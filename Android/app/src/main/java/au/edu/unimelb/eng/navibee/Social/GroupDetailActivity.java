package au.edu.unimelb.eng.navibee.social;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import au.edu.unimelb.eng.navibee.R;

public class GroupDetailActivity extends AppCompatActivity {
    private ConversationManager cm = ConversationManager.getInstance();
    private ArrayList<String> memberList;
    private String convId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        convId = getIntent().getStringExtra("CONV_ID");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_group_detail_inviteFriend_button:
            break;
        }
    }
}
