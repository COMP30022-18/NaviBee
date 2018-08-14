package au.edu.unimelb.eng.navibee.Social;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

public class Conversation {

    public static final String BROADCAST_NEW_MESSAGE = "broadcast.conversation.newmessage";


    private final String conversationId;
    private final String uid;
    FirebaseFirestore db;
    private ArrayList<Message> messages = new ArrayList<>();

    private static final String TAG = "conv";

    public Conversation(String id, String uid) {
        conversationId = id;
        this.uid = uid;
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
                                    messages.add(dc.getDocument().toObject(Message.class));
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
                    }
                });
    }

    public void sendMessage(String type, String data) {
        Message message = new Message(data, uid, new Date(), type);

        db.collection("conversations")
                .document(conversationId).collection("messages").add(message);
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


    public static class Message{
        private String data, type, sender;
        private Date time;

        public Message() {
        }

        public Message(String data, String sender, Date time, String type) {
            this.data = data;
            this.sender = sender;
            this.time = time;
            this.type = type;
        }

        public Date getTime() {
            return time;
        }

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