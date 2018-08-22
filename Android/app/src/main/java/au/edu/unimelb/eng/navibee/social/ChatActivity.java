package au.edu.unimelb.eng.navibee.social;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;

import au.edu.unimelb.eng.navibee.R;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private Conversation conversation;
    private String targetUID;

    private RecyclerView chatRecyclerView;
    private RecyclerView.Adapter chatAdapter;
    private RecyclerView.LayoutManager chatLayoutManager;

    private int PICK_IMAGE_REQUEST = 1;

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
        targetUID = getIntent().getStringExtra("TARGET_USER_ID");
        conversation = ConversationManager.getInstance().getConversationByUID(targetUID);


        chatRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_chat);
        chatRecyclerView.setHasFixedSize(true);
        chatLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(chatLayoutManager);
        chatAdapter = new ChatAdapter(conversation.getCurrentMessageList());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                conversation.sendPicture(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
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
                String[] items = {"Take a picture", "Choose from gallery"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Send");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0) {

                        } else if (which==1) {
                            Intent intent = new Intent();
                            // Show only images, no videos or anything else
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            // Always show the chooser (if there are multiple options available)
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                        }
                    }
                });
                builder.show();

                break;
        }

    }

    public void scrollToBottom() {
            chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }


    public static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {


        public static class MessageViewHolder extends RecyclerView.ViewHolder{

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private ArrayList<Conversation.Message> mDataset;

        private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        private static final int VT_SEND=0;
        private static final int VT_RECV=1;

        // Provide a suitable constructor (depends on the kind of dataset)
        public ChatAdapter(ArrayList<Conversation.Message> myDataset) {
            mDataset = myDataset;
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

            int resource = (viewType==VT_RECV)? R.layout.layout_recipient_message: R.layout.layout_sender_message;
            View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            MessageViewHolder vh = new MessageViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            ((TextView) holder.itemView.findViewById(R.id.message_text)).setText(mDataset.get(position).getData());

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}
