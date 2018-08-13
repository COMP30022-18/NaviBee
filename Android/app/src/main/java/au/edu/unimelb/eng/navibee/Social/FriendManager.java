package au.edu.unimelb.eng.navibee.Social;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

public class FriendManager {

    public final static String BROADCAST_FRIEND_UPDATED = "broadcast.friend.updated";

    public static class ContactPerson {
        private String url;
        private String name;

        public ContactPerson(String name, String url) {
            this.url = url;
            this.name = name;
        }

        public String getUrl() {
            return this.url;
        }

        public String getName() {
            return this.name;
        }

        public void setUrl(String url){
            this.url = url;
        }
        public void setName(String name){
            this.name = name;
        }
    }


    private static FriendManager instance = null;

    public static FriendManager getInstance() {
        return instance;
    }

    public static void init() {
        instance = new FriendManager();
    }

    private static final String TAG = "friend";

    private String uid;
    private FirebaseFirestore db;

    private Map<String, Object> friendInfo = new HashMap<>();


    public FriendManager() {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        db.collection("users").whereEqualTo("contacts."+uid, true)
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
                                    friendInfo.put(dc.getDocument().getId(), dc.getDocument().getData());
                                    break;
                                case MODIFIED:
                                    friendInfo.put(dc.getDocument().getId(), dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    friendInfo.remove(dc.getDocument().getId());
                                    break;
                            }
                        }

                        Intent intent = new Intent(BROADCAST_FRIEND_UPDATED);
                        NaviBeeApplication.getInstance().sendBroadcast(intent);
                    }
                });
    }

    public void fetchContactPersonList(ArrayList<ContactPerson> list) {
        list.clear();
        for (Map.Entry<String, Object> entry: friendInfo.entrySet()) {
            Map <String, Object> value = (Map <String, Object>) entry.getValue();
            ContactPerson cp = new ContactPerson((String)value.get("name"), (String)value.get("photoURL"));
            list.add(cp);
        }
    }


}

