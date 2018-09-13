package au.edu.unimelb.eng.navibee.social;

import androidx.annotation.Nullable;
import au.edu.unimelb.eng.navibee.NaviBeeApplication;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConversationManager {

    public final static String BROADCAST_FRIEND_UPDATED = "broadcast.friend.updated";
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

    private ArrayList<String> friendList = new ArrayList<>();
    private Map<String, String> uidToConvId = new HashMap<>();
    private Map<String, Conversation> convIdMap = new HashMap<>();


    private boolean initialized = false;


    public ConversationManager() {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // private conversation (friend list)
        db.collection("conversations")
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

                                // load new conversation
                                Conversation conv = new Conversation(convId, uid, timestamp.toDate(), true);

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

                    Intent intent = new Intent(BROADCAST_FRIEND_UPDATED);
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

    public Conversation getPrivateConversation(String userId) {
        return getConversation(getPrivateConvId(userId));
    }

    public ArrayList<String> getFriendList() {
        return new ArrayList<>(friendList);
    }

    public ArrayList<Conversation> getAllInprivateConversation(){
        ArrayList<Conversation> inprivateConversation = new ArrayList<>();
        for (Conversation conv:convIdMap.values()){
            if (!conv.isPrivate()){
                inprivateConversation.add(conv);
            }
        }
        return inprivateConversation;
    }

    public void updateConvInfoForContactList(ArrayList<FriendActivity.ContactItem> list) {
        for (FriendActivity.ContactItem cp: list) {
            Conversation conv = getConversation(cp.getConvId());
            if (conv.getMessageCount()>0) {
                Conversation.Message msg = conv.getMessage(conv.getMessageCount()-1);
                String lastMsgText = "";
                switch (msg.getType()) {
                    case "text":
                        lastMsgText = msg.getData();
                        break;
                    case "image":
                        lastMsgText = "[Photo]";
                        break;
                    case "voicecall":
                        lastMsgText = "[Voice Call]";
                        break;
                }

                lastMsgText = lastMsgText.substring(0, Math.min(lastMsgText.length(), 50));

                cp.setLastMessage(lastMsgText);
                cp.setLastMessageTime(msg.getTime_());

                cp.setUnreadMessage(conv.getUnreadMsgCount());
            } else {
                cp.setUnreadMessage(0);
            }
        }
    }


    public Task<HttpsCallableResult> addFriend(String targetUid) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("targetUid", targetUid);

        return mFunctions.getHttpsCallable("addFriend").call(data);
    }

    public Task<HttpsCallableResult> createGroupChat(ArrayList<String> uses, String name, String icon) {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("users", uses);
        data.put("name", name);
        data.put("icon", icon);

        return mFunctions.getHttpsCallable("createGroupChat").call(data);
    }
}
