package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import androidx.core.app.NotificationManagerCompat;
import au.edu.unimelb.eng.navibee.MyFirebaseMessagingService;
import au.edu.unimelb.eng.navibee.NaviBeeApplication;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;

public abstract class Conversation {

    public static final String BROADCAST_NEW_MESSAGE = "broadcast.conversation.newmessage";
    private static final String TAG = "conv";

    protected final String conversationId;
    protected final String uid;
    private final FirebaseFirestore db;

    private ArrayList<Message> messages = new ArrayList<>();
    private int unreadMsgCount = 0;
    private Date readTimestamp;
    private Date createTimestamp;

    private ListenerRegistration mListener;


    public Conversation(String id, Date readTimestamp, Date createTimestamp) {
        conversationId = id;
        this.readTimestamp = readTimestamp;
        this.createTimestamp = createTimestamp;
        this.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db = FirebaseFirestore.getInstance();
        listen();
    }

    public String getConvId() {
        return conversationId;
    }

    public Date getCreateTimestamp() {
        return createTimestamp;
    }

    private void listen() {
        mListener = db.collection("conversations").document(conversationId)
                .collection("messages").orderBy("time")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                // new message
                                Message msg = dc.getDocument().toObject(Message.class);
                                msg.setId(dc.getDocument().getId());
                                messages.add(msg);
                                if (msg.getTime_().after(readTimestamp)) {
                                   unreadMsgCount += 1;

                                   newUnreadMsg(msg);
                                }
                                break;
                            case MODIFIED:
                                //
                                break;
                            case REMOVED:
                                //
                                break;
                        }
                    }

                    Intent intent = new Intent(BROADCAST_NEW_MESSAGE);
                    NaviBeeApplication.getInstance().sendBroadcast(intent);

                    intent = new Intent(ConversationManager.BROADCAST_MESSAGE_READ_CHANGE);
                    NaviBeeApplication.getInstance().sendBroadcast(intent);
                });
    }

    public void stopListening() {
        mListener.remove();
    }

    protected abstract void newUnreadMsg(Message msg);

    public void sendMessage(String type, String data) {
        Message message = new Message(data, uid, new Date(), type);

        db.collection("conversations")
                .document(conversationId).collection("messages").add(message);
    }

    public void sendLocation(double latitude, double longitude) {
        double[] coord = new double[2];
        coord[0] = latitude;
        coord[1] = longitude;
        Gson gson = new Gson();
        sendMessage("location", gson.toJson(coord));

    }

    public void sendPicture(Uri uri) {
        try {
            FirebaseStorageHelper
                    .uploadImage(uri,null, "message", 70, false, ((isSuccess, path) -> {
                        if (isSuccess) {
                            sendMessage("image", path);
                        } else {
                            Toast.makeText(NaviBeeApplication.getInstance(), "Failed to send the photo.", Toast.LENGTH_LONG).show();
                        }
                    }));
        } catch (Exception e) {
            Log.w(TAG, "sendPicture", e);
        }
    }

    public Message getMessageById(String msgId) {
        for (Message m: messages) {
            String id = m.getId();
            if (id!=null && id.equals(msgId)) {
                return m;
            }
        }
        return null;
    }

    public Message getMessage(int index) {
        return messages.get(index);
    }

    public int getMessageCount() {
        return messages.size();
    }

    public ArrayList<Message> getCurrentMessageList() {
        return new ArrayList<>(messages);
    }

    public int getUnreadMsgCount() {
        return unreadMsgCount;
    }

    public void markAllAsRead() {
        unreadMsgCount = 0;
        if (messages.size()>0) {
            readTimestamp = messages.get(messages.size()-1).getTime_();

            // update timestamp on server
            db.collection("conversations").document(conversationId).update("readTimestamps." + uid, readTimestamp);
        }

        Intent intent = new Intent(ConversationManager.BROADCAST_MESSAGE_READ_CHANGE);
        NaviBeeApplication.getInstance().sendBroadcast(intent);

        // cancel notification
        SharedPreferences prefs = NaviBeeApplication.getInstance().getSharedPreferences(MyFirebaseMessagingService.NOTIFICATION_PREFS_NAME, NaviBeeApplication.getInstance().MODE_PRIVATE);
        Set<String> ids = prefs.getStringSet(conversationId, new HashSet<>());
        prefs.edit().remove(conversationId).apply();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(NaviBeeApplication.getInstance());

        for (String idString: ids) {
            int id = Integer.parseInt(idString);
            notificationManager.cancel(id);
        }
    }


    public static class Message{
        private String data, type, sender, id = null;
        private Timestamp time;

        public Message() {
        }

        public Message(String data, String sender, Date time, String type) {
            this.data = data;
            this.sender = sender;
            this.time = new Timestamp(time);
            this.type = type;
        }

        public Message(String data, String sender, Timestamp time, String type) {
            this.data = data;
            this.sender = sender;
            this.time = time;
            this.type = type;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Exclude
        public String getId() {
            return id;
        }

        public Timestamp getTime() {
            return time;
        }

        @Exclude
        public Date getTime_() { return time.toDate();}

        public String getData() {
            return data;
        }

        public String getSender() {
            return sender;
        }

        public String getType() {
            return type;
        }

        @Exclude
        public String getSummary() {
            String text = "";
            switch (getType()) {
                case "text":
                    text = getData();
                    break;
                case "image":
                    text = "[Photo]";
                    break;
                case "voicecall":
                    text = "[Voice Call]";
                    break;
                case "location":
                    text = "[Location]";
                    break;
                case "event":
                    text = "[Event]";
                    break;
            }

            return text.substring(0, Math.min(text.length(), 50));
        }
    }

}