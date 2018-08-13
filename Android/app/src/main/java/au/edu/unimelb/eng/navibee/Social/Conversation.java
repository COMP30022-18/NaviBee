package au.edu.unimelb.eng.navibee.Social;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class Conversation {
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
                    }
                });
    }

    private void sendMessage(String type, String data) {
        Message message = new Message(data, uid, new Date(), type);

        db.collection("conversations")
                .document(conversationId).collection("messages").add(message);
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