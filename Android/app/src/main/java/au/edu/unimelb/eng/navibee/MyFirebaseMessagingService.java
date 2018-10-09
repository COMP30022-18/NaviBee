package au.edu.unimelb.eng.navibee;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String CHANNEL_ID = "Default";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {
            Map<String, String> data = remoteMessage.getData();
            String content = "";

            // TODO: strings
            switch (Objects.requireNonNull(data.get("type"))) {
                case "text":
                    content = data.get("content");
                    break;
                case "image":
                    content = "sent you a photo";
                    break;
                case "voicecall":
                    content = "is calling you";
                    break;
                case "location":
                    content = "send you a location";
                    break;
                case "event":
                    content = "send you an event";
                    break;
            }

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            intent.putExtra("convID", data.get("convID"));
            intent.putExtra("msgID", data.get("msgID"));

            Notification notification =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                    // TODO: change icon
                    .setSmallIcon(R.drawable.ic_call_icon_white_70dp)
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
        }

    }

    private int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }


}
