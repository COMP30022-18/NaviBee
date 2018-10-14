package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

import com.yhao.floatwindow.FloatWindow;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;
import au.edu.unimelb.eng.navibee.R;

public class VoiceCallService {

    public enum Status {Idle, Waiting, Calling, Connecting}

    private static final VoiceCallService ourInstance = new VoiceCallService();

    public static VoiceCallService getInstance() {
        return ourInstance;
    }

    public static final int VOCIECALL_EXPIRE = 60 * 1000;
    public static final String BROADCAST_VOICECALL_UPDATE = "broadcast.voicecall.update";


    private static final int CONNECTING_TIMEOUT = 5 * 1000;
    private static final int ANSWER_TIMEOUT = 20 * 1000;
    private static final int WAITING_TIMEOUT = 40 * 1000;



    private Status status = Status.Idle;
    private String channelId;
    private boolean isInitiator;
    PrivateConversation conv;
    private Date startTime;

    private boolean isMicEnabled = true;
    private boolean isSpeakerEnabled = false;


    Handler handler = new Handler(Looper.getMainLooper());
    private Timer timeoutTimer;
    private Timer answerTimer;

    private VoiceCallService() {

    }

    public void setup(PrivateConversation conv, Conversation.Message msg, boolean isInitiator) {
        // handle busy case
        if (status != Status.Idle) return;
        status = Status.Waiting;

        timeoutTimer = new Timer();
        answerTimer = new Timer();

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

        isMicEnabled = true;
        isSpeakerEnabled = false;

        startVoiceCallActivity();
    }

    private void startVoiceCallActivity() {
        Intent intent = new Intent(NaviBeeApplication.getInstance().getApplicationContext(),
                VoiceCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        NaviBeeApplication.getInstance().startActivity(intent);
    }

    private void connect() {
        status = Status.Connecting;

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
                    if (status == Status.Connecting) {
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
        Toast.makeText(NaviBeeApplication.getInstance(), msg, Toast.LENGTH_LONG).show();
    }

    public void answerCall() {
        if (status == Status.Waiting) {
            answerTimer.cancel();
            answerTimer.purge();
            connect();
        }
    }

    public void closeVoiceCall() {
        conv.markAllAsRead();
        status = Status.Idle;
        startTime = null;
        conv = null;

        timeoutTimer.cancel();
        timeoutTimer.purge();
        answerTimer.cancel();
        answerTimer.purge();
        VoiceCallEngine.getInstance().leaveChannel();

        if (FloatWindow.get() != null) {
            FloatWindow.get().hide();
            FloatWindow.destroy();
        }

        broadcastUpdate();
    }

    private void broadcastUpdate() {
        Intent intent = new Intent(BROADCAST_VOICECALL_UPDATE);
        NaviBeeApplication.getInstance().sendBroadcast(intent);
    }

    public void toggleSpeaker() {
        isSpeakerEnabled = !isSpeakerEnabled;

        if(isSpeakerEnabled) {
            VoiceCallEngine.getInstance().useSpeaker();
        } else {
            VoiceCallEngine.getInstance().useEarpiece();
        }
    }

    public void toggleMute() {
        isMicEnabled = !isMicEnabled;
        if (isMicEnabled) {
            VoiceCallEngine.getInstance().unmuteMic();
        } else {
            VoiceCallEngine.getInstance().muteMic();
        }

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

    public String getTargetUid() {
        return conv.getTargetUid();
    }

    public boolean getIsMicEnabled() {
        return isMicEnabled;
    }

    public boolean getIsSpeakerEnabled() {
        return isSpeakerEnabled;
    }

    public void showFloatWindow() {
        if (FloatWindow.get() == null) {
            ImageView imageView = new ImageView(NaviBeeApplication.getInstance());
            imageView.setImageResource(R.drawable.ic_call_black80_24dp);
            FloatWindow.with(NaviBeeApplication.getInstance())
                    .setView(imageView)
                    .setWidth(100)
                    .setHeight(100)
                    .setDesktopShow(true)
                    .build();

            imageView.setOnClickListener(view -> {
                startVoiceCallActivity();
                FloatWindow.get().hide();
            });
        }

        FloatWindow.get().show();
    }


}
