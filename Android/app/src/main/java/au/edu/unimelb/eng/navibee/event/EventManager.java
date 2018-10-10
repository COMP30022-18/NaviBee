//package au.edu.unimelb.eng.navibee;
//
//import android.support.annotation.NonNull;
//import android.util.EventLog;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//import com.google.firebase.firestore.QuerySnapshot;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class EventManager {
//
//    // helper class
//    public static class EventItem {
//        private String eventId;
//        private String name;
//        private String summary;
//        private Map<String, Boolean> users;
//        private Boolean isTag = false;
//
//        public EventItem() {}
//
//        public EventItem(String name, String summary, Map<String, Boolean> users){
//            this.name = name;
//            this.summary = summary;
//            this.users = users;
//        }
//
//        public String getName(){
//            return name;
//        }
//
//        public String getSummary(){
//            return summary;
//        }
//
//        public Map<String, Boolean> getUsers(){
//            return users;
//        }
//
//        public Boolean isTag() {
//            return isTag;
//        }
//
//        public void setTag(Boolean isTag){
//            this.isTag = isTag;
//        }
//
//        public String getEventId(){
//            return eventId;
//        }
//
//        public void setEventId(String eventId){
//            this.eventId = eventId;
//        }
//
//        public Map<String, Boolean> copyUsers() {
//            Map<String, Boolean> copy = new HashMap<>();
//            copy.putAll(users);
//            return copy;
//        }
//    }
//
//    // attributes
//    private static EventManager instance = null;
//
//    private FirebaseFirestore db;
//    private String userId;
//
//    private List<EventItem> eventList = new ArrayList<>();
//
//    public static Boolean loading;
//
//    // constructor
//    public EventManager() {
//        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        db = FirebaseFirestore.getInstance();
//
//        EventItem ForYouTag = new EventItem("FOR YOU", null, null);
//        ForYouTag.setTag(true);
//        eventList.add(ForYouTag);
//
//        loading = true;
//        db.collection("events").whereEqualTo("users." + userId, true).get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                EventItem eventItem = document.toObject(EventItem.class);
//                                eventItem.setEventId(document.getId());
//                                eventList.add(eventItem);
//                            }
//                        } else {
//                            // fail to pull data
//                        }
//                        // finished here
//                        loading = false;
//                    }
//                });
//    }
//
//    // methods
//    public static EventManager getInstance() {
//        if (instance == null){
//            instance = new EventManager();
//        }
//        return instance;
//    }
//
//    public void fetchEventList(ArrayList<EventItem> list) {
//        list.clear();
//        for(EventItem item: eventList) {
//            if(!item.isTag()) {
//                EventItem new_item = new EventItem(item.getName(), item.getSummary(), item.copyUsers());
//                new_item.setEventId(item.getEventId());
//                list.add(new_item);
//            }
//            else {
//                EventItem new_tag = new EventItem(item.getName(), null, null);
//                new_tag.setTag(true);
//                list.add(new_tag);
//            }
//        }
//    }
//
//}
