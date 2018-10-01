package au.edu.unimelb.eng.navibee.social;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.NetworkImageHelper;

public class GroupDetailActivity extends AppCompatActivity {
    private ConversationManager cm = ConversationManager.getInstance();
    private GroupConversation conv;
    private ArrayList<String> memberList;
    private String convId;
    private String creator;
    private String createDate;

    public static class MemberAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> members;

        private static class ViewHolder{
            TextView name;
            ImageView icon;
        }

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
            return position%3;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            String member = members.get(position);
            if (convertView == null){
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.group_chat_member_item, parent, false);
                viewHolder.name = (TextView) convertView.findViewById(R.id.group_member_name);
                viewHolder.icon = (ImageView) convertView.findViewById(R.id.group_detail_icon);
            }
            else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            UserInfoManager.getInstance().getUserInfo(member, userInfo -> {
//                viewHolder.name.setText(userInfo.getName());
//                NetworkImageHelper.loadImage(viewHolder.icon, userInfo.getPhotoUrl());
            });
            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        convId = getIntent().getStringExtra("CONV_ID");
        conv = (GroupConversation) cm.getConversation(convId);
        creator = conv.getCreator();
        Date createDate = conv.getCreateDate();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm");
        this.createDate = dateFormat.format(createDate);
        memberList = new ArrayList<>(conv.getMembers());
        for (String str:memberList){
            System.out.println(str);
        }

        GridView gridview = (GridView) findViewById(R.id.activity_group_detail_members);
        gridview.setAdapter(new MemberAdapter(this, memberList));

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_group_detail_inviteFriend_button:
            break;
        }
    }
}
