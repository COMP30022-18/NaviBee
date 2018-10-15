package au.edu.unimelb.eng.navibee;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import androidx.core.app.NotificationManagerCompat;
import au.edu.unimelb.eng.navibee.social.Conversation;

import static au.edu.unimelb.eng.navibee.MyFirebaseMessagingService.REPLY_ACTION;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    // Key for the string that's delivered in the action's intent.
    private static final String KEY_CONV_ID = "key_conv_id";
    private static final String KEY_NOTI_ID = "key_noti_id";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (REPLY_ACTION.equals(intent.getAction())) {
            // do whatever you want with the message. Send to the server or add to the db.
            // for this tutorial, we'll just show it in a toast;
            CharSequence message = MyFirebaseMessagingService.getReplyMessage(intent);
            String convId = intent.getStringExtra(KEY_CONV_ID);
            int notiId = intent.getIntExtra(KEY_NOTI_ID, 0);
            sendMessageByConvId(convId, "text", message);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(NaviBeeApplication.getInstance());
            notificationManager.cancel(notiId);
            MyFirebaseMessagingService.messages.remove(notiId);
        }
    }

    private void sendMessageByConvId(String convId, String type, CharSequence content) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();

        Conversation.Message message =
                new Conversation.Message(content.toString(), uid, new Date(), type);
        FirebaseFirestore.getInstance()
                .collection("conversations")
                .document(convId)
                .collection("messages")
                .add(message);
    }

    public static Intent getReplyMessageIntent(Context context, String convId, int notiId) {
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(REPLY_ACTION);
        intent.putExtra(KEY_CONV_ID, convId);
        intent.putExtra(KEY_NOTI_ID, notiId);
        return intent;
    }
}
