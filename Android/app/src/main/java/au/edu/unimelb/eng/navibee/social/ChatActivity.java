package au.edu.unimelb.eng.navibee.social;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.commit451.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import com.commit451.modalbottomsheetdialogfragment.Option;
import com.commit451.modalbottomsheetdialogfragment.OptionRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import au.edu.unimelb.eng.navibee.BuildConfig;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.event.EventDetailsActivity;
import au.edu.unimelb.eng.navibee.navigation.NavigationSelectorActivity;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;
import au.edu.unimelb.eng.navibee.utils.URLImageViewCacheLoader;
import timber.log.Timber;


public class ChatActivity extends AppCompatActivity implements IPickResult, ModalBottomSheetDialogFragment.Listener {

    private Conversation conversation;
    private boolean isPrivate;

    private RecyclerView chatRecyclerView;
    private RecyclerView.Adapter chatAdapter;
    private LinearLayoutManager chatLayoutManager;

    protected HashMap<String, UserInfoManager.UserInfo> userInfos = new HashMap<>();

    private static final int PLACE_PICKER_REQUEST = 2;

    private static final int SEND_PHOTO = 1;
    private static final int SEND_LOCATION = 2;
    private static final int SEND_VOICE_CALL = 3;
    private static final int SEND_REALTIME_LOCATION = 4;

    private int currentMsgCount = 0;

    private ModalBottomSheetDialogFragment.Builder extraDialog;

    BroadcastReceiver msgUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadNewMsg();
        }
    };

    BroadcastReceiver convUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConversationManager.getInstance().getConversation(conversation.getConvId()) == null){
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        String convId = getIntent().getStringExtra("CONV_ID");
        conversation = ConversationManager.getInstance().getConversation(convId);
        isPrivate = conversation instanceof PrivateConversation;

        chatRecyclerView = findViewById(R.id.recycler_view_chat);
        chatRecyclerView.setHasFixedSize(true);
        chatLayoutManager = new LinearLayoutManager(this);
        chatLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(chatLayoutManager);
        chatAdapter = new ChatAdapter(conversation.getCurrentMessageList(), chatRecyclerView, this);
        chatRecyclerView.setAdapter(chatAdapter);

        setSupportActionBar(findViewById(R.id.activity_chat_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView toolbarTitle = findViewById(R.id.chat_toolbar_title);
        TextView toolbarSubtitle = findViewById(R.id.chat_toolbar_subtitle);
        ImageView toolbarIcon = findViewById(R.id.chat_toolbar_icon);

        UserInfoManager.getInstance().getUserInfo(conversation.uid, userInfo -> {
            userInfos.put(conversation.uid, userInfo);
        });

        if (conversation instanceof PrivateConversation) {
            toolbarSubtitle.setText(R.string.chat_type_private);
            UserInfoManager.getInstance().getUserInfo(((PrivateConversation) conversation).getTargetUid(), userInfo -> {
                userInfos.put(((PrivateConversation) conversation).getTargetUid(), userInfo);
                toolbarTitle.setText(userInfo.getName());
                new URLImageViewCacheLoader(userInfo.getPhotoUrl(), toolbarIcon).roundImage(true).execute();
            });
        } else if (conversation instanceof GroupConversation) {
            GroupConversation group = (GroupConversation) conversation;
            toolbarTitle.setText(group.getName());
            String subtitle = getResources().getQuantityString(R.plurals.chat_group_member_count,
                    group.getMembers().size(), group.getMembers().size());
            toolbarSubtitle.setText(subtitle);
            toolbarIcon.setImageDrawable(group.getRoundIconDrawable(getResources()));
        }

        Resources r = getResources();

        extraDialog = new ModalBottomSheetDialogFragment.Builder()
                .header(r.getString(R.string.chat_send_multimedia), R.layout.modal_bottom_sheet_dialog_fragment_header)
                .add(new OptionRequest(SEND_PHOTO, r.getString(R.string.message_type_photo), R.drawable.ic_photo_black80_24dp))
                .add(new OptionRequest(SEND_LOCATION, r.getString(R.string.message_type_location), R.drawable.ic_place_black80_24dp ));

        if (isPrivate) {
            extraDialog = extraDialog
                    .add(new OptionRequest(SEND_REALTIME_LOCATION, r.getString(R.string.chat_realtime_location), R.drawable.ic_person_pin_black80_24dp))
                    .add(new OptionRequest(SEND_VOICE_CALL, r.getString(R.string.chat_voice_call), R.drawable.ic_call_black80_24dp));
        }

        currentMsgCount = conversation.getMessageCount();
        conversation.markAllAsRead();

        scrollToBottom();

        if (ConversationManager.getInstance().getConversation(convId) == null){
            finish();
        }

        registerReceiver(convUpdateReceiver, new IntentFilter(ConversationManager.BROADCAST_CONVERSATION_UPDATED));
        registerReceiver(msgUpdateReceiver, new IntentFilter(Conversation.BROADCAST_NEW_MESSAGE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(convUpdateReceiver);
        unregisterReceiver(msgUpdateReceiver);
    }

    public void onClickProfileName(View view) {
        if (isPrivate){
            Intent intent = new Intent(this, FriendDetail.class);
            intent.putExtra("CONV_ID", conversation.getConvId());
            intent.putExtra("FRIEND_ID", ((PrivateConversation) conversation).getTargetUid());
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, GroupDetailActivity.class);
            intent.putExtra("CONV_ID", conversation.getConvId());
            startActivity(intent);
        }
    }

    @Override
    public void onModalOptionSelected(@Nullable String s, @NotNull Option option) {
        switch (option.getId()) {
            case SEND_PHOTO:
                PickImageDialog.build(new PickSetup().setSystemDialog(true)).show(ChatActivity.this);
                break;
            case SEND_LOCATION:
                try {
                    PlacePicker.IntentBuilder builder1 = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder1.build(ChatActivity.this), PLACE_PICKER_REQUEST);
                } catch (Exception e) {
                    Timber.e(e, "send location error.");
                }
                break;
            case SEND_VOICE_CALL:
                String voiceCallChannelId = UUID.randomUUID().toString();
                conversation.sendMessage("voicecall", voiceCallChannelId);
                break;
            case SEND_REALTIME_LOCATION:
                Intent intent = new Intent(this, RealTimeLocationDisplayActivity.class);
                intent.putExtra(RealTimeLocationDisplayActivity.EXTRA_CONVID, conversation.getConvId());
                startActivity(intent);
                conversation.sendMessage("realtimelocation", "");
                break;
        }
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

    public void onClickSend(View view) {
        EditText editText = findViewById(R.id.edit_text_message);
        String text = editText.getText().toString().trim();
        if (!text.equals("")) {
            conversation.sendMessage("text", text);
            editText.setText("");
        }
    }

    public void onClickExtra(View view) {
        extraDialog.show(getSupportFragmentManager(), "extra");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                PlacePicker.getLatLngBounds(data);
                Place place = PlacePicker.getPlace(this, data);
                conversation.sendLocation(place.getLatLng().latitude, place.getLatLng().longitude);
            }
        }
    }


    public void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0)
            chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
    }


    public static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

        private Gson gson = new Gson();

        public static class MessageViewHolder extends RecyclerView.ViewHolder{

            private View content;

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            public void setContentView(View content) { this.content = content; }
            public View getContentView() { return content; }
        }

        private ArrayList<Conversation.Message> mDataset;
        private RecyclerView mRecyclerView;
        private ChatActivity chatActivity;

        private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        private static final int VT_CONVERSATION_TYPE = 0b1;
        private static final int VT_SEND = 0b0;
        private static final int VT_RECV = 0b1;
        private static final int VT_MESSAGE_TYPE = 0b1110;
        private static final int VT_TEXT = 0;
        private static final int VT_IMAGE = 1 << 1;
        private static final int VT_LOCATION = 2 << 1;
        private static final int VT_EVENT = 3 << 1;
        private static final int VT_VOICE_CALL = 4 << 1;
        private static final int VT_REALTIME_LOCATION = 5 << 1;

        private static final String STATIC_MAP_URL =
                "https://maps.googleapis.com/maps/api/staticmap?" +
                "zoom=16&size=640x320&scale=2&maptype=roadmap&sensor=true&center=%s" +
                "&markers=color:red|%s&key=" +
                BuildConfig.GOOGLE_PLACES_API_KEY;

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
            Conversation.Message m = mDataset.get(position);
            int type = m.getSender().equals(uid) ? VT_SEND : VT_RECV;
            switch (m.getType()) {
                case "text": type |= VT_TEXT; break;
                case "image": type |= VT_IMAGE; break;
                case "voicecall": type |= VT_VOICE_CALL; break;
                case "location": type |= VT_LOCATION; break;
                case "event": type |= VT_EVENT; break;
                case "realtimelocation": type |= VT_REALTIME_LOCATION; break;
            }
            return type;
        }


        // Create new views (invoked by the layout manager)
        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            int resource;
            int theme;

            if ((viewType & VT_CONVERSATION_TYPE) == VT_RECV) {
                if (chatActivity.isPrivate) {
                    resource = R.layout.layout_recipient_message;
                } else {
                    resource = R.layout.layout_recipient_message_group;
                }
                theme = R.style.AppTheme_Messaging_Other;
            } else {
                resource = R.layout.layout_sender_message;
                theme = R.style.AppTheme_Messaging_Self;
            }
            LayoutInflater lf = LayoutInflater.from(
                    new ContextThemeWrapper(parent.getContext(), theme)
            );
            View v = lf.inflate(resource, parent, false);
            RelativeLayout f = v.findViewById(R.id.chat_message_content);

            switch (viewType & VT_MESSAGE_TYPE) {
                default:
                case VT_TEXT:
                    resource = R.layout.layout_text_message;
                    break;
                case VT_LOCATION:
                case VT_IMAGE:
                    resource = R.layout.layout_photo_message;
                    break;
                case VT_VOICE_CALL:
                    resource = R.layout.layout_voice_message;
                    break;
                case VT_EVENT:
                    resource = R.layout.layout_event_message;
                    break;
                case VT_REALTIME_LOCATION:
                    resource = R.layout.layout_realtimelocation_message;
                    break;
            }

            View cv = lf.inflate(resource, f, false);

            if ((viewType & VT_CONVERSATION_TYPE) == VT_RECV) {
                f.setGravity(Gravity.START);
            } else {
                f.setGravity(Gravity.END);
            }

            f.addView(cv);

            MessageViewHolder vh = new MessageViewHolder(v);

            vh.setContentView(cv);

            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            Conversation.Message msg = mDataset.get(position);
            View content = holder.getContentView();


            content.setOnClickListener(genOnCLick(position));
            TextView textView = content.findViewById(R.id.message_text);
            ImageView imageView = content.findViewById(R.id.message_image);
            switch (msg.getType()) {
                case "text":
                    textView.setText(msg.getData());
                    break;
                case "image":
                    FirebaseStorageHelper.loadImage(imageView, msg.getData(), true);
                    break;
                case "voicecall":
                    CharSequence time =
                            DateUtils.getRelativeTimeSpanString(msg.getTime_().getTime());
                    textView.setText(time);
                    break;
                case "location":
                    double[] coord = gson.fromJson(msg.getData(), double[].class);
                    String coordText = coord[0] + "," + coord[1];
                    String previewUrl = String.format(STATIC_MAP_URL, coordText, coordText);
                    new URLImageViewCacheLoader(previewUrl, imageView).execute();
                    break;
                case "event":
                    Gson gson = new Gson();
                    Map<String, String> data = gson.fromJson(msg.getData(), Map.class);
                    String text = data.get("name");
                    textView.setText(text);
                    break;
                case "realtimelocation":
                    time = DateUtils.getRelativeTimeSpanString(msg.getTime_().getTime());
                    textView.setText(time);
                    break;
            }

            // set user name
            if (!msg.getSender().equals(uid) && !chatActivity.isPrivate) {
                UserInfoManager.getInstance().getUserInfo(msg.getSender(),
                        userInfo -> {
                            ((TextView) holder.itemView.findViewById(R.id.message_sender))
                                    .setText(userInfo.getName());
                            new URLImageViewCacheLoader(
                                    userInfo.getPhotoUrl(),
                                    holder.itemView.findViewById(R.id.message_sender_avatar)
                            ).roundImage(true).execute();
                        });

            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        private View.OnClickListener genOnCLick(int pos) {
            return v -> {
                Conversation.Message msg = mDataset.get(pos);
                switch (msg.getType()) {
                    case "image":
                        Intent intent = new Intent(chatActivity.getBaseContext(), ChatImageViewActivity.class);
                        intent.putExtra("IMG_FS_PATH", msg.getData());
                        chatActivity.startActivity(intent);
                        break;
                    case "location":
                        double[] coord = gson.fromJson(msg.getData(), double[].class);

                        intent = new Intent(chatActivity.getBaseContext(), LocationDisplayActivity.class);
                        intent.putExtra(NavigationSelectorActivity.EXTRA_LATITUDE, coord[0]);
                        intent.putExtra(NavigationSelectorActivity.EXTRA_LONGITUDE, coord[1]);
                        intent.putExtra(LocationDisplayActivity.EXTRA_TIME, msg.getTime_());
                        if (chatActivity.userInfos.containsKey(msg.getSender())) {
                            intent.putExtra(LocationDisplayActivity.EXTRA_SENDER, (Parcelable) chatActivity.userInfos.get(msg.getSender()));
                        }

                        chatActivity.startActivity(intent);

                        break;
                    case "event":
                        Map<String, String> data = gson.fromJson(msg.getData(), Map.class);

                        intent = new Intent(chatActivity.getBaseContext(), EventDetailsActivity.class);

                        intent.putExtra("eventId", data.get("eid"));
                        chatActivity.startActivity(intent);

                        break;
                    case "realtimelocation":
                        intent = new Intent(chatActivity.getBaseContext(), RealTimeLocationDisplayActivity.class);
                        intent.putExtra(RealTimeLocationDisplayActivity.EXTRA_CONVID, chatActivity.conversation.getConvId());
                        chatActivity.startActivity(intent);
                        break;
                }
            };
        }


    }
}
