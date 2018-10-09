package au.edu.unimelb.eng.navibee.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.acl.Group;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.NetworkImageHelper;
import jdenticon.Jdenticon;

public class GroupDetailActivity extends AppCompatActivity {
    private ConversationManager cm = ConversationManager.getInstance();
    private GroupConversation conv;
    private ArrayList<String> memberList;
    private String convId;

    private BroadcastReceiver convUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConversationManager.getInstance().getConversation(convId) == null){
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Alert");
                dialog.setMessage("Sorry, this group chat has been deleted by the creator.");
                dialog.setPositiveButton("Ok", (dialoginterface, i) -> finish());

                dialog.show();
            }
        }
    };


    public static class MemberAdapter extends BaseAdapter{
        private Context context;
        private ArrayList<String> members;

        public MemberAdapter(Context context, ArrayList<String> members) {
            this.context = context;
            this.members = members;

        }

        public int getCount() {
            return members.size();
        }

        public Object getItem(int position) {
            return members.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            String member = members.get(position);
            if (convertView == null){
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.group_chat_member_item, parent, false);
                TextView viewName = (TextView) view.findViewById(R.id.group_member_name);
                ImageView viewIcon = (ImageView) view.findViewById(R.id.group_member_icon);
                UserInfoManager.getInstance().getUserInfo(member, userInfo -> {
                    viewName.setText(userInfo.getName());
                    NetworkImageHelper.loadImage(viewIcon, userInfo.getPhotoUrl());
                });
            }
            else{
                view = convertView;
            }
            view.setId(position);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!member.equals(ConversationManager.getInstance().getUid())){
                        Intent intent = new Intent(context, FriendDetail.class);
                        intent.putExtra("FRIEND_ID", member);
                        context.startActivity(intent);
                    }
                }
            });
            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        convId = getIntent().getStringExtra("CONV_ID");
        conv = (GroupConversation) cm.getConversation(convId);
        memberList = new ArrayList<>(conv.getMembers());

        GridView gridview = (GridView) findViewById(R.id.activity_group_detail_members);
        gridview.setAdapter(new MemberAdapter(this, memberList));

        int memberCount = memberList.size();

        int numColomns = 4;
        int totalHeight = (memberList.size() / numColomns);
        if (memberCount % numColomns != 0) {
            totalHeight++;
        }
        ViewGroup.LayoutParams params = gridview.getLayoutParams();
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96*totalHeight + 12, getResources().getDisplayMetrics());
        params.height = height;
        gridview.setLayoutParams(params);
        gridview.requestLayout();

        TextView members = findViewById(R.id.activity_group_detail_members_text);
        String memberText = "Members (" + Integer.toString(memberCount) + ")";
        members.setText(memberText);


        TextView creator = findViewById(R.id.activity_group_detail_creator);
        UserInfoManager.getInstance().getUserInfo(conv.getCreator(), userInfo -> {
            creator.setText(userInfo.getName());
        });
        TextView createDate = findViewById(R.id.activity_group_detail_date_created);
        Date date = conv.getCreateDate();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        createDate.setText(dateFormat.format(date));
        TextView groupName = findViewById(R.id.group_detail_name);
        groupName.setText(conv.getName());
        ImageView groupIcon = findViewById(R.id.group_detail_icon);

        groupIcon.setImageBitmap(conv.getIconBitmap());

        Button button = findViewById(R.id.activity_group_detail_deleteGroup_button);
        if (!cm.getUid().equals(conv.getCreator())){
            button.setVisibility(View.GONE);
        }


        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Alert");
        dialog.setMessage("Sorry, this group chat has been deleted by the creator.");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                finish();
            }
        });

        registerReceiver(convUpdateReceiver, new IntentFilter(ConversationManager.BROADCAST_CONVERSATION_UPDATED));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(convUpdateReceiver);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_group_detail_deleteGroup_button:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Alert");
                dialog.setMessage("Are you sure you want to delete this group?");
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }
                });
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        cm.deleteGroup(convId);
                        finish();
                    }
                });
                dialog.show();
                break;
        }
    }
}
