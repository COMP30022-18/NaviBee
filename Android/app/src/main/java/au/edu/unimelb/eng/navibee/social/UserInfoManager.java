package au.edu.unimelb.eng.navibee.social;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserInfoManager {
    private static final String TAG = "UserInfoManager";
    private static UserInfoManager instance;

    public static UserInfoManager getInstance() {
        if (instance == null) {
            instance = new UserInfoManager();
        }
        return instance;
    }


    private Map<String, UserInfo> userInfoMap = new HashMap<>();
    private String uid;
    private FirebaseFirestore db;


    private UserInfoManager() {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
    }
    
    public void getUserInfo(String id, Callback<UserInfo> callback) {
        if (userInfoMap.containsKey(id)) {
            callback.callback(userInfoMap.get(id));
            return;
        }

        db.collection("users").document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    UserInfo userInfo;
                    if (userInfoMap.containsKey(id)) {
                        userInfo = userInfoMap.get(id);
                    } else {
                        userInfo = new UserInfo("", "");
                        userInfoMap.put(id, userInfo);
                    }
                    userInfo.setPhotoUrl((String) doc.get("photoURL"));
                    userInfo.setName((String) doc.get("name"));
                    callback.callback(userInfo);
                } else {
                    Log.d(TAG, "get failed: user not exists" + id);
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    public void getUserInfo(ArrayList<String> ids, Callback<Map<String, UserInfo>> callback) {
        Map<String, UserInfo> res = new HashMap<>();
        for (String id: ids) {
            getUserInfo(id, info -> {
                res.put(id, info);
                if (res.size()==ids.size()) {
                    callback.callback(res);
                }
            });
        }
    }


    interface Callback<T> {
        void callback(T t);
    }


    public static class UserInfo {
        private String name, photoUrl;

        private UserInfo(String name, String photoUrl) {
            this.name = name;
            this.photoUrl = photoUrl;
        }


        public String getName() {
            return name;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }
    }
}
