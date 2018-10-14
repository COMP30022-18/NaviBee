package au.edu.unimelb.eng.navibee;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import au.edu.unimelb.eng.navibee.social.Conversation;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;
import au.edu.unimelb.eng.navibee.utils.URLCallbackCacheLoader;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String CHANNEL_ID = "Default";
    public static final String NOTIFICATION_PREFS_NAME = "NotificationIDs";

    public static String REPLY_ACTION = "navibee.REPLY_ACTION";
    private static final String KEY_TEXT_REPLY = "key_text_reply";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {

            Map<String, String> data = remoteMessage.getData();

            ConversationManager cm = ConversationManager.getInstance();
            if (cm != null) {
                // app is running
                Conversation conv = cm.getConversation(data.get("convID"));
                if (conv != null) {
                    if ((conv.getMessageById(data.get("msgID")) != null) && conv.getUnreadMsgCount() == 0) {
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
                case "realtimelocation":
                    content = getString(R.string.notification_realtimelocation);
                    break;
            }

            int id = createID();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("convID", data.get("convID"));
            intent.putExtra("msgID", data.get("msgID"));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_navibee_notification)
                    .setContentTitle(data.get("senderName"))
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (data.get("convType").equals("group")) {
                builder.setCategory(data.get("chatName"));
            }

            // setup reply button
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
                RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                        .setLabel(getResources().getString(R.string.action_reply))
                        .build();

                PendingIntent replyPendingIntent =
                        getMessageReplyIntent(data.get("convID"), id);

                // Create the reply action and add the remote input.
                NotificationCompat.Action action =
                        new NotificationCompat.Action.Builder(R.drawable.ic_reply_black_24dp,
                                getString(R.string.action_reply), replyPendingIntent)
                                .addRemoteInput(remoteInput)
                                .build();

                builder.addAction(action);
            }

            // Get user avatar
            new URLCallbackCacheLoader(data.get("senderAvatar")) {
                @Override
                public void postLoad(@NotNull File file) {
                    builder.setLargeIcon(getRoundBitmap(file, getResources()));
                    if (data.get("type").equals("image")) {
                        // Load image thumb sent
                        FirebaseStorageHelper.loadImage(
                                data.get("content"), true,
                                (isSuccess, bitmap) -> {
                                    if (bitmap != null)
                                        builder.setStyle(new NotificationCompat
                                                .BigPictureStyle()
                                                .bigPicture(bitmap));
                                    sendNotifcation(builder.build(), data.get("convID"), id);
                        });
                    } else {
                        sendNotifcation(builder.build(), data.get("convID"), id);
                    }
                }
            }.execute();

        }

    }

    private PendingIntent getMessageReplyIntent(String convId, int notiId) {
        Intent intent = NotificationBroadcastReceiver.getReplyMessageIntent(this, convId, notiId);
        return PendingIntent.getBroadcast(getApplicationContext(), 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void sendNotifcation(Notification notification, String convId, int id) {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // TODO: save notification id
        notificationManager.notify(id, notification);

        saveNotification(id, convId);
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

    public static CharSequence getReplyMessage(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(KEY_TEXT_REPLY);
        }
        return null;
    }


}
