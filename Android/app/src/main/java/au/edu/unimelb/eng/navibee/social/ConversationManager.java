package au.edu.unimelb.eng.navibee.social;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

public class ConversationManager {

    public final static String BROADCAST_CONVERSATION_UPDATED = "broadcast.conversation.updated";
    public final static String BROADCAST_MESSAGE_READ_CHANGE = "broadcast.message.readchange";


    private static ConversationManager instance = null;

    public static ConversationManager getInstance() {
        return instance;
    }

    public static void init() {
        instance = new ConversationManager();

        instance.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        instance.db = FirebaseFirestore.getInstance();

        instance.listenPrivateConv();
        instance.listenGroupConv();
    }

    private static final String TAG = "convM";

    private String uid;
    private FirebaseFirestore db;

    private ArrayList<String> friendList = new ArrayList<>();
    private Map<String, String> uidToConvId = new HashMap<>();
    private Map<String, Conversation> convIdMap = new HashMap<>();

    private String waitingConvId = "";


    private boolean initialized = false;


    public ConversationManager() {
    }

    private void listenPrivateConv() {
        // private conversation (friend list)
        db.collection("conversations")
                .whereEqualTo("isDeleted", false)
                .whereEqualTo("users."+uid, true)
                .whereEqualTo("type", "private")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String friendUid = getOtherUserId(dc.getDocument().get("users"), uid);
                        String convId = dc.getDocument().getId();
                        switch (dc.getType()) {
                            case ADDED:
                                // read message timestamp
                                Timestamp timestamp =  ((Map<String, Timestamp>) dc.getDocument().get("readTimestamps")).get(uid);

                                Timestamp createTimestamp = (Timestamp) dc.getDocument().get("createTimestamp");

                                // load new conversation
                                Conversation conv = new PrivateConversation(convId, timestamp.toDate(), createTimestamp.toDate(), friendUid);

                                uidToConvId.put(friendUid, convId);
                                convIdMap.put(convId, conv);
                                friendList.add(friendUid);
                                break;

                            case MODIFIED:
                                break;

                            case REMOVED:
                                uidToConvId.remove(friendUid);
                                convIdMap.remove(dc.getDocument().getId());
                                friendList.remove(friendUid);
                                break;
                        }
                    }

                    if (!initialized) {
                        // initializing, warm user info cache
                        initialized = true;
                        UserInfoManager.getInstance().warmCache(friendList);
                    }

                    Intent intent = new Intent(BROADCAST_CONVERSATION_UPDATED);
                    NaviBeeApplication.getInstance().sendBroadcast(intent);

                    if (!waitingConvId.equals("")) {
                        if (openChatActivity(waitingConvId)) {
                            waitingConvId = "";
                        }
                    }

                });
    }

    private void listenGroupConv() {
        // private conversation (friend list)
        db.collection("conversations")
                .whereEqualTo("users."+uid, true)
                .whereEqualTo("isDeleted", false)
                .whereEqualTo("type", "group")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String convId = dc.getDocument().getId();
                        switch (dc.getType()) {
                            case ADDED:
                                // read message timestamp
                                Timestamp timestamp =  ((Map<String, Timestamp>) dc.getDocument().get("readTimestamps")).get(uid);

                                Timestamp createTimestamp = (Timestamp) dc.getDocument().get("createTimestamp");

                                // load new conversation
                                Conversation conv = new GroupConversation(convId, timestamp.toDate(), createTimestamp.toDate(),
                                        (String) dc.getDocument().get("name"), (String) dc.getDocument().get("icon"),
                                        (Map<String, Boolean>) dc.getDocument().get("users"), (String) dc.getDocument().get("creator"));
                                convIdMap.put(convId, conv);
                                break;

                            case MODIFIED:
                                break;

                            case REMOVED:
                                convIdMap.remove(dc.getDocument().getId());
                                break;
                        }
                    }

                    if (!waitingConvId.equals("")) {
                        if (openChatActivity(waitingConvId)) {
                            waitingConvId = "";
                        }
                    }
                    Intent intent = new Intent(BROADCAST_CONVERSATION_UPDATED);
                    NaviBeeApplication.getInstance().sendBroadcast(intent);
                });
    }

    private static String getOtherUserId(Object map, String myUid) {
        for (String key: ((Map<String, Boolean>) map).keySet()) {
            if(!key.equals(myUid)) {
                return key;
            }
        }
        return "";
    }

    public Conversation getConversation(String convId) {
        return convIdMap.get(convId);
    }

    public String getPrivateConvId(String userId) {
        return uidToConvId.get(userId);
    }

    public PrivateConversation getPrivateConversation(String userId) {
        return (PrivateConversation) getConversation(getPrivateConvId(userId));
    }

    public ArrayList<Conversation> getConversations() {
        return new ArrayList<>(convIdMap.values());
    }

    public ArrayList<String> getFriendList() {
        return new ArrayList<>(friendList);
    }


    public Task<HttpsCallableResult> addFriend(String targetUid) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("targetUid", targetUid);

        return mFunctions.getHttpsCallable("addFriend").call(data);
    }

    public Task<Void> deleteFriend(String targetUid) {
        String convId = getPrivateConvId(targetUid);
        return db.collection("conversations").document(convId).update("isDeleted", true);
    }

    public Task<HttpsCallableResult> createGroupChat(ArrayList<String> users, String name, String icon) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("users", users);
        data.put("name", name);
        data.put("icon", icon);

        return mFunctions.getHttpsCallable("createGroupChat").call(data);
    }

    public void waitForOpenChatAvtivity(String convId) {
        if (!openChatActivity(convId)) {
            waitingConvId = convId;
        }
    }

    private boolean openChatActivity(String convId) {
        if (getConversation(convId) != null) {
            Intent intent = new Intent(NaviBeeApplication.getInstance(), ChatActivity.class);
            intent.putExtra("CONV_ID", convId);
            NaviBeeApplication.getInstance().startActivity(intent);
            return true;
        }
        return false;
    }

    public void deleteGroup(String convId){
        db.collection("conversations").document(convId).update("isDeleted",true);
    }

    public String getUid(){ return this.uid; }
}
