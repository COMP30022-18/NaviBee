package au.edu.unimelb.eng.navibee.social;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.ArrayList;
import java.util.UUID;

import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener, IPickResult {

    private Conversation conversation;
    private boolean isPrivate;

    private RecyclerView chatRecyclerView;
    private RecyclerView.Adapter chatAdapter;
    private RecyclerView.LayoutManager chatLayoutManager;

    int PLACE_PICKER_REQUEST = 2;


    private int currentMsgCount = 0;

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadNewMsg();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        String convId = getIntent().getStringExtra("CONV_ID");
        conversation = ConversationManager.getInstance().getConversation(convId);
        isPrivate = conversation instanceof PrivateConversation;

        chatRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_chat);
        chatRecyclerView.setHasFixedSize(true);
        chatLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(chatLayoutManager);
        chatAdapter = new ChatAdapter(conversation.getCurrentMessageList(), chatRecyclerView, this);
        chatRecyclerView.setAdapter(chatAdapter);

        currentMsgCount = conversation.getMessageCount();
        conversation.markAllAsRead();

        IntentFilter intFilt = new IntentFilter(Conversation.BROADCAST_NEW_MESSAGE);
        registerReceiver(br, intFilt);

        scrollToBottom();
    }

    private void loadNewMsg() {
        while (currentMsgCount < conversation.getMessageCount()) {
            ((ChatAdapter) chatAdapter).addMessage(conversation.getMessage(currentMsgCount));
            currentMsgCount += 1;
        }
        scrollToBottom();
        conversation.markAllAsRead();
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            conversation.sendPicture(r.getUri());
        } else {
            //Handle possible errors
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_message:
                EditText editText = (EditText)findViewById(R.id.edit_text_message);
                String text = editText.getText().toString();
                if (!text.equals("")) {
                    conversation.sendMessage("text", text);
                    editText.setText("");
                }
                break;

            case R.id.btn_send_extra:
                String[] items;

                if (isPrivate) {
                    items = new String[]{"Picture", "Location", "Voice Call"};
                } else {
                    items = new String[]{"Picture", "Location"};
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Send");
                builder.setItems(items, (dialog, which) -> {
                    if (which==0) {
                        PickImageDialog.build(new PickSetup().setSystemDialog(true)).show(ChatActivity.this);
                    } else if (which==1) {
                        try {
                            PlacePicker.IntentBuilder builder1 = new PlacePicker.IntentBuilder();
                            startActivityForResult(builder1.build(ChatActivity.this), PLACE_PICKER_REQUEST);
                        } catch (Exception e) {
                            Log.d("Chat", "send location error:" + e);
                        }
                    } else if (which==2) {
                        String voiceCallChannelId = UUID.randomUUID().toString();
                        conversation.sendMessage("voicecall", voiceCallChannelId);
                    }
                });
                builder.show();

                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                PlacePicker.getLatLngBounds(data);
                Place place = PlacePicker.getPlace(this, data);
                double[] coord = new double[2];
                coord[0] = place.getLatLng().latitude;
                coord[1] = place.getLatLng().longitude;
                Gson gson = new Gson();
                conversation.sendMessage("location", gson.toJson(coord));
            }
        }
    }


    public void scrollToBottom() {
            chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }


    public static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> implements View.OnClickListener {


        public static class MessageViewHolder extends RecyclerView.ViewHolder{

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private ArrayList<Conversation.Message> mDataset;
        private RecyclerView mRecyclerView;
        private ChatActivity chatActivity;

        private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        private static final int VT_SEND=0;
        private static final int VT_RECV=1;

        // Provide a suitable constructor (depends on the kind of dataset)
        public ChatAdapter(ArrayList<Conversation.Message> myDataset, RecyclerView recyclerView, ChatActivity chatActivity) {
            this.chatActivity = chatActivity;
            mDataset = myDataset;
            mRecyclerView = recyclerView;
        }

        public void addMessage(Conversation.Message message) {
            mDataset.add(message);
            this.notifyItemInserted(mDataset.size()-1);
        }

        @Override
        public int getItemViewType(int position) {
            // Just as an example, return 0 or 2 depending on position
            // Note that unlike in ListView adapters, types don't have to be contiguous
            return mDataset.get(position).getSender().equals(uid)? VT_SEND: VT_RECV;
        }


        // Create new views (invoked by the layout manager)
        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            int resource;
            if (chatActivity.isPrivate) {
                resource = (viewType==VT_RECV)? R.layout.layout_recipient_message: R.layout.layout_sender_message;
            } else {
                resource = (viewType==VT_RECV)? R.layout.layout_recipient_message_group: R.layout.layout_sender_message;
            }

            View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            v.setOnClickListener(this);
            MessageViewHolder vh = new MessageViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            Conversation.Message msg = mDataset.get(position);

            ((TextView) holder.itemView.findViewById(R.id.message_text)).setVisibility(View.GONE);
            ((ImageView) holder.itemView.findViewById(R.id.message_image)).setVisibility(View.GONE);

            if (msg.getType().equals("text")) {
                ((TextView) holder.itemView.findViewById(R.id.message_text)).setText(msg.getData());
                ((TextView) holder.itemView.findViewById(R.id.message_text)).setVisibility(View.VISIBLE);
            } else if (msg.getType().equals("image")) {
                ((ImageView) holder.itemView.findViewById(R.id.message_image)).setVisibility(View.VISIBLE);
                FirebaseStorageHelper.loadImage(((ImageView) holder.itemView.findViewById(R.id.message_image)), msg.getData(), true);
            } else if (msg.getType().equals("voicecall")) {
                ((TextView) holder.itemView.findViewById(R.id.message_text)).setText("[Voice Call]");
                ((TextView) holder.itemView.findViewById(R.id.message_text)).setVisibility(View.VISIBLE);
            } else if (msg.getType().equals("location")) {
                ((TextView) holder.itemView.findViewById(R.id.message_text)).setText("[Location]");
                ((TextView) holder.itemView.findViewById(R.id.message_text)).setVisibility(View.VISIBLE);
            }

            // set user name
            if (!msg.getSender().equals(uid) && !chatActivity.isPrivate) {
                UserInfoManager.getInstance().getUserInfo(msg.getSender(),
                        userInfo -> ((TextView) holder.itemView.findViewById(R.id.message_sender))
                                .setText(userInfo.getName()));
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public void onClick(final View view) {
            int itemPosition = mRecyclerView.getChildLayoutPosition(view);
            Conversation.Message msg = mDataset.get(itemPosition);
            if (msg.getType().equals("image")) {
                Intent intent = new Intent(chatActivity.getBaseContext(), ChatImageViewActivity.class);
                intent.putExtra("IMG_FS_PATH", msg.getData());
                chatActivity.startActivity(intent);
            } else if (msg.getType().equals("location")) {
                Gson gson = new Gson();
                double[] coord = gson.fromJson(msg.getData(), double[].class);
            }
        }

    }
}
