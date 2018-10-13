package au.edu.unimelb.eng.navibee.social;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

public class VoiceCallService {

    public enum Status {Idle, Waiting, Calling}

    private static final VoiceCallService ourInstance = new VoiceCallService();

    public static VoiceCallService getInstance() {
        return ourInstance;
    }

    public final static String BROADCAST_VOICECALL_UPDATE = "broadcast.voicecall.update";
    private static final int CONNECTING_TIMEOUT = 5 * 1000;
    private static final int ANSWER_TIMEOUT = 20 * 1000;
    private static final int WAITING_TIMEOUT = 40 * 1000;



    private Status status = Status.Idle;
    private String channelId;
    private boolean isInitiator;
    PrivateConversation conv;
    private Date startTime;


    Handler handler = new Handler(Looper.getMainLooper());
    private Timer timeoutTimer = new Timer();
    private Timer answerTimer = new Timer();

    private VoiceCallService() {

    }

    public void setup(PrivateConversation conv, Conversation.Message msg, boolean isInitiator) {
        if (status != Status.Idle) return;

        status = Status.Waiting;

        channelId = msg.getData();
        this.isInitiator = isInitiator;
        this.conv = conv;

        if (isInitiator) {
            connect();
        } else {
            // wait for answering
            answerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(() -> showDialogAndClose("Call Cancelled."));
                }
            }, ANSWER_TIMEOUT);
        }

    }

    private void connect() {

        if (isInitiator) {
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(() -> showDialogAndClose("No response from the other end"));
                }
            }, WAITING_TIMEOUT);
        } else {
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(() -> showDialogAndClose("Unable to connect"));
                }
            }, CONNECTING_TIMEOUT);
        }

        VoiceCallEngine.EventHandler mEventHandler = new VoiceCallEngine.EventHandler() {
            @Override
            public void onUserOffline(final int uid, final int reason) {
                handler.post(() -> showDialogAndClose("Call Cancelled."));
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                handler.post(() -> {
                    if (status != Status.Calling) {
                        // call started
                        status = Status.Calling;
                        timeoutTimer.cancel();
                        timeoutTimer.purge();

                        startTime = new Date();

                        broadcastUpdate();
                    }
                });
            }
        };

        VoiceCallEngine.getInstance().joinChannel(channelId, mEventHandler);

    }

    private void showDialogAndClose(String msg) {
        closeVoiceCall();

        AlertDialog.Builder builder = new AlertDialog.Builder(NaviBeeApplication.getInstance());
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> {
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void closeVoiceCall() {
        conv.markAllAsRead();
        status = Status.Idle;
        startTime = null;

        timeoutTimer.cancel();
        timeoutTimer.purge();
        VoiceCallEngine.getInstance().leaveChannel();

        broadcastUpdate();
    }

    private void broadcastUpdate() {
        Intent intent = new Intent(BROADCAST_VOICECALL_UPDATE);
        NaviBeeApplication.getInstance().sendBroadcast(intent);
    }


    public Status getStatus() {
        return status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public boolean getIsInitiator() {
        return isInitiator;
    }
}
