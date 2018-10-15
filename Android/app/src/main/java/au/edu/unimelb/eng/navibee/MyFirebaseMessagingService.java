package au.edu.unimelb.eng.navibee;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;
import au.edu.unimelb.eng.navibee.social.Conversation;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;
import au.edu.unimelb.eng.navibee.utils.URLCallbackCacheLoader;

import static androidx.core.content.FileProvider.getUriForFile;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String CHANNEL_ID = "Default";
    public static final String NOTIFICATION_PREFS_NAME = "NotificationIDs";

    public static String REPLY_ACTION = "navibee.REPLY_ACTION";
    private static final String KEY_TEXT_REPLY = "key_text_reply";

    public static SparseArray<ArrayList<NotificationCompat.MessagingStyle.Message>> messages =
            new SparseArray<>();

    private Person me;

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

            int id = createID(data.get("convID"));

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

            // Get user info.
            Person me = getMe();
            if (me == null) return;
            NotificationCompat.MessagingStyle mStyle = new NotificationCompat.MessagingStyle(me);

            // Get user avatar
            File userAvatarF = new URLCallbackCacheLoader(data.get("senderAvatar")) {
                @Override
                public void postLoad(@NotNull File file) {
                }
            }.getFileAndExecute();

            Person sender = new Person.Builder()
                    .setName(data.get("senderName"))
                    .setIcon(IconCompat.createWithContentUri(getContentUri(userAvatarF)))
                    .build();

            if (data.get("convType").equals("group")) {
                mStyle.setConversationTitle(data.get("chatName"));
                mStyle.setGroupConversation(true);
            } else {
                mStyle.setConversationTitle(data.get("senderName"));
                mStyle.setGroupConversation(false);
            }

            if (messages.get(id) != null) {
                for (NotificationCompat.MessagingStyle.Message msg: messages.get(id)) {
                    mStyle.addMessage(msg);
                }
            } else {
                messages.put(id, new ArrayList<>());
            }

            NotificationCompat.MessagingStyle.Message msg =
                    new NotificationCompat.MessagingStyle.Message(content,
                        System.currentTimeMillis(), sender);

            if (data.get("type").equals("image")) {
                // Load image thumb sent
                FirebaseStorageHelper.loadImage(
                    data.get("content"), true,
                    (FirebaseStorageHelper.FileCallback) (success, file)-> {
                        if (success)
                            msg.setData("image/",
                                    getContentUri(file));
                        mStyle.addMessage(msg);

                        messages.get(id).add(msg);

                        builder.setStyle(mStyle);
                        sendNotifcation(builder.build(), data.get("convID"), id);
                    });
            } else {
                mStyle.addMessage(msg);

                messages.get(id).add(msg);

                builder.setStyle(mStyle);
                sendNotifcation(builder.build(), data.get("convID"), id);
            }
        }

    }

    private Uri getContentUri(File file) {
        Uri uri = getUriForFile(this, "au.edu.unimelb.eng.navibee.com.vansuita.pickimage.provider", file);
        grantUriPermission("com.android.systemui", uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return uri;
    }

    private PendingIntent getMessageReplyIntent(String convId, int notiId) {
        Intent intent = NotificationBroadcastReceiver.getReplyMessageIntent(this, convId, notiId);
        return PendingIntent.getBroadcast(getApplicationContext(), 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void sendNotifcation(Notification notification, String convId, int id) {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        notificationManager.notify(id, notification);

        saveNotification(id, convId);
    }

    private void saveNotification(int id, String convID) {
        SharedPreferences prefs = getSharedPreferences(NOTIFICATION_PREFS_NAME, MODE_PRIVATE);
        Set<String> ids = prefs.getStringSet(convID, new HashSet<>());

        ids.add(Integer.toString(id));

        prefs.edit().putStringSet(convID, ids).apply();
    }


    private int createID(String convId){
        return convId.hashCode();
    }

    public static CharSequence getReplyMessage(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(KEY_TEXT_REPLY);
        }
        return null;
    }

    private Person getMe() {
        if (this.me != null) return this.me;
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) {
            return null;
        }
        Person.Builder pb = new Person.Builder().setBot(false)
                .setName(me.getDisplayName()).setKey(me.getUid());
        Uri photoUrl = me.getPhotoUrl();
        if (photoUrl == null) {
            this.me = pb.build();
            return this.me;
        }


        File f = new URLCallbackCacheLoader(photoUrl.toString()) {
            @Override
            public void postLoad(@NotNull File file) {
            }
        }.getFileAndExecute();

        pb.setIcon(IconCompat.createWithContentUri(getContentUri(f)));
        this.me = pb.build();
        return this.me;
    }

    private interface AsyncPersonCallback {
        void callback(Person person);
    }

}
