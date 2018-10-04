package au.edu.unimelb.eng.navibee.social;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
            if (callback!=null) callback.callback(userInfoMap.get(id));
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
                    if (callback!=null) callback.callback(userInfo);
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
        for (String id: new HashSet<>(ids)) {
            getUserInfo(id, info -> {
                res.put(id, info);
                if (res.size()==ids.size()) {
                    callback.callback(res);
                }
            });
        }
    }

    public void warmCache(ArrayList<String> ids) {
        for (String id: ids) {
            getUserInfo(id,null);
        }
    }


    public interface Callback<T> {
        void callback(T t);
    }


    public static class UserInfo implements Parcelable, Serializable {
        private String name, photoUrl;

        public UserInfo(String name, String photoUrl) {
            this.name = name;
            this.photoUrl = photoUrl;
        }

        protected UserInfo(Parcel in) {
            name = in.readString();
            photoUrl = in.readString();
        }

        public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
            @Override
            public UserInfo createFromParcel(Parcel in) {
                return new UserInfo(in);
            }

            @Override
            public UserInfo[] newArray(int size) {
                return new UserInfo[size];
            }
        };

        public String getName() {
            return name;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public String getHighResolutionPhotoUrl() {
            return photoUrl.replace("s96-c", "s400-c");
        }

        public String getOriginalPhotoUrl() {
            return photoUrl.replace("/s96-c", "");
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(photoUrl);
        }
    }
}
