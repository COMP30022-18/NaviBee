package au.edu.unimelb.eng.navibee;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import au.edu.unimelb.eng.navibee.social.Conversation;
import au.edu.unimelb.eng.navibee.social.ConversationManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String CHANNEL_ID = "Default";
    public static final String NOTIFICATION_PREFS_NAME = "NotificationIDs";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {

            Map<String, String> data = remoteMessage.getData();

            ConversationManager cm = ConversationManager.getInstance();
            if (cm != null) {
                // app is running
                Conversation conv = cm.getConversation(data.get("convID"));
                if (conv!=null) {
                    if ((conv.getMessageById(data.get("msgID")) != null)&&conv.getUnreadMsgCount()==0) {
                        // msg already been read
                        return;
                    }
                }
            }


            String content = "";

            // TODO: strings
            switch (Objects.requireNonNull(data.get("type"))) {
                case "text":
                    content = data.get("content");
                    break;
                case "image":
                    content = getString(R.string.notification_image);
                    break;
                case "voicecall":
                    content = getString(R.string.notification_voicecall);
                    break;
                case "location":
                    content = getString(R.string.notification_location);
                    break;
                case "event":
                    content = getString(R.string.notificaiton_event);
                    break;
            }

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("convID", data.get("convID"));
            intent.putExtra("msgID", data.get("msgID"));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_navibee_notification)
                    .setContentTitle(data.get("senderName"))
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // TODO: save notification id
            int id = createID();
            notificationManager.notify(id, notification);

            saveNotification(id, data.get("convID"));
        }

    }

    private void saveNotification(int id, String convID) {
        SharedPreferences prefs = getSharedPreferences(NOTIFICATION_PREFS_NAME, MODE_PRIVATE);
        Set<String> ids = prefs.getStringSet(convID, new HashSet<>());

        ids.add(Integer.toString(id));

        prefs.edit().putStringSet(convID, ids).apply();
    }


    private int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }


}
