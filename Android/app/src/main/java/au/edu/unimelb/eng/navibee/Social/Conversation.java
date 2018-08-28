package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;

public class Conversation {

    public static final String BROADCAST_NEW_MESSAGE = "broadcast.conversation.newmessage";
    private static final String TAG = "conv";

    private final String conversationId;
    private final String uid;
    private final FirebaseFirestore db;

    private ArrayList<Message> messages = new ArrayList<>();
    private int unreadMsgCount = 0;
    private Date readTimestamp;


    public Conversation(String id, String uid, Date timestamp) {
        conversationId = id;
        this.uid = uid;
        this.readTimestamp = timestamp;
        db = FirebaseFirestore.getInstance();
        listen();

    }

    private void listen() {
        db.collection("conversations").document(conversationId)
                .collection("messages").orderBy("time")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    // new message
                                    Message msg = dc.getDocument().toObject(Message.class);
                                    messages.add(msg);
                                    if (msg.getTime_().after(readTimestamp)) {
                                       unreadMsgCount += 1;
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
                    }
                });
    }

    public void sendMessage(String type, String data) {
        Message message = new Message(data, uid, new Date(), type);

        db.collection("conversations")
                .document(conversationId).collection("messages").add(message);
    }

    public void sendPicture(Bitmap bitmap) {
        UploadTask uploadTask = FirebaseStorageHelper.uploadImage(bitmap,null, "message", 70);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                sendMessage("image", taskSnapshot.getStorage().getPath());
            }
        });


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
    }


    public static class Message{
        private String data, type, sender;
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

        public Timestamp getTime() {
            return time;
        }

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
    }

}