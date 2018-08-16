package au.edu.unimelb.eng.navibee.social;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConversationManager {

    public final static String BROADCAST_MESSAGE_READ_CHANGE = "broadcast.message.readchange";

    private static ConversationManager instance = null;

    public static ConversationManager getInstance() {
        return instance;
    }

    public static void init() {
        instance = new ConversationManager();
    }

    private static final String TAG = "convM";

    private String uid;
    private FirebaseFirestore db;

    private Map<String, Conversation> conversationMap = new HashMap<>();


    public ConversationManager() {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();


        db.collection("conversations").whereEqualTo("users."+uid, true)
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
                                    // read message timestamp
                                    Date timestamp =  ((Map<String, Date>) dc.getDocument().getData().get("readTimestamps")).get(uid);

                                    // load new conversation
                                    Conversation conv = new Conversation(dc.getDocument().getId(), uid, timestamp);
                                    String otherUid = "";

                                    Map<String, Boolean> users = (Map<String, Boolean>)(dc.getDocument().getData()).get("users");
                                    for (String userId: users.keySet()) {
                                        if (!userId.equals(uid)) {
                                            otherUid = userId;
                                        }
                                    }
                                    conversationMap.put(otherUid, conv);

                                    break;
                                case MODIFIED:

                                    break;
                                case REMOVED:
                                    //
                                    break;
                            }
                        }
                    }
                });
    }

    public Conversation getConversationByUID(String uid) {
        return conversationMap.get(uid);
    }

    public boolean isConversationExists(String uid) {
        return conversationMap.containsKey(uid);
    }

    public void updateConvInfoForContactList(ArrayList<FriendManager.ContactPerson> list) {
        for (FriendManager.ContactPerson cp: list) {
            Conversation conv = getConversationByUID(cp.getUid());
            if (conv!=null && conv.getMessageCount()>0) {
                cp.setLastMessageTime(conv.getMessage(conv.getMessageCount()-1).getTime());
                cp.setUnreadMessage(conv.getUnreadMsgCount());
            } else {
                cp.setUnreadMessage(0);
            }
        }
    }
}
