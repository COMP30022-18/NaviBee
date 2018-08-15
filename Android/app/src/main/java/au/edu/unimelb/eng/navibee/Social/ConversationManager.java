package au.edu.unimelb.eng.navibee.Social;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ConversationManager {

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
                                    // load new conversation
                                    Conversation conv = new Conversation(dc.getDocument().getId(), uid);
                                    String otherUid = "";

                                    Map<String, Boolean> users = (Map<String, Boolean>)((Map<String, Object>)dc.getDocument().getData()).get("users");
                                    for (String userId: users.keySet()) {
                                        if (userId!=uid) {
                                            otherUid = userId;
                                        }
                                    }
                                    conversationMap.put(otherUid, conv);

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

    public Conversation getConversationByUID(String uid) {
        return conversationMap.get(uid);
    }

    public boolean isConversationExists(String uid) {
        return conversationMap.containsKey(uid);
    }
}
