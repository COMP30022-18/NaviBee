package au.edu.unimelb.eng.navibee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import au.edu.unimelb.eng.navibee.Social.Conversation;
import au.edu.unimelb.eng.navibee.Social.ConversationManager;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private Conversation conversation;
    private String targetUID;

    private RecyclerView chatRecyclerView;
    private RecyclerView.Adapter chatAdapter;
    private RecyclerView.LayoutManager chatLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        targetUID = getIntent().getStringExtra("TARGET_USER_ID");
        conversation = ConversationManager.getInstance().getConversationByUID(targetUID);


//        chatRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_chat);
//        chatLayoutManager = new LinearLayoutManager(this);
//        chatAdapter = new CatAdapter(chtDataset);
//        chatRecyclerView.setAdapter(chatAdapter);
    }

    @Override
    public void onClick(View view) {

    }
}
