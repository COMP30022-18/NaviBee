package au.edu.unimelb.eng.navibee.social;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

import au.edu.unimelb.eng.navibee.utils.NetworkImageHelper;
import io.agora.rtc.RtcEngine;

import au.edu.unimelb.eng.navibee.R;

public class VoiceCallActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    public static final int VOCIECALL_EXPIRE = 60 * 1000;

    private static final int CONNECTING_TIMEOUT = 5 * 1000;
    private static final int ANSWER_TIMEOUT = 20 * 1000;
    private static final int WAITING_TIMEOUT = 40 * 1000;

    private static boolean working = false;

    private RtcEngine mRtcEngine = null;

    private String channelID = "testChannel";
    private boolean isInitiator;
    private Conversation conv;
    private Conversation.Message msg;

    private Timer timeoutTimer = new Timer();
    private Timer answerTimer = new Timer();

    private TextView textViewStatus;
    private TextView textViewTime;
    private ImageView buttonHangup;
    private ImageView buttonAccept;
    private ImageView buttonDecline;
    private ImageView friendIcon;
    private TextView friendName;
    private TextView changingDot;
    private int dotCount;

    private boolean callStarted = false;


    private long timeCount;
    Thread thread = new Thread() {

        @Override
        public void run() {
            try {
                while (!thread.isInterrupted()) {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeCount += 1;
                            String text = String.format("%d:%02d:%02d", (timeCount / 60 / 60) % 60,
                                        (timeCount / 60) % 60, timeCount % 60);
                            textViewTime.setText(text);

                        }
                    });
                }
            } catch (InterruptedException e) {
            }
        }
    };

    private final VoiceCallEngine.EventHandler mEventHandler = new VoiceCallEngine.EventHandler() {
        @Override
        public void onUserOffline(final int uid, final int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDialogAndClose("Call Cancelled.");
                }
            });
        }

        @Override
        public void  onUserJoined(int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!callStarted) {
                        // call started
                        callStarted = true;
                        timeoutTimer.cancel();
                        timeoutTimer.purge();
                        timeCount = 0;
                        textViewStatus.setText("");
                        thread.start();
                    }

                }
            });
        }
    };

    public static boolean isWorking() {
        return working;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        checkPermission();

        working = true;

        isInitiator = getIntent().getBooleanExtra("INITIATOR", false);
        conv = ConversationManager.getInstance().getConversation(getIntent().getStringExtra("CONV_ID"));
        msg = conv.getMessageById(getIntent().getStringExtra("MSG_ID"));
        channelID = msg.getData();

        textViewStatus = findViewById(R.id.voicecall_textView_status);
        textViewTime = findViewById(R.id.voicecall_textView_time);
        buttonHangup = findViewById(R.id.voicecall_button_hangup);
        buttonAccept = findViewById(R.id.voicecall_button_accept);
        buttonDecline = findViewById(R.id.voicecall_button_decline);

        friendIcon = findViewById(R.id.voicecall_friend_icon);
        friendName = findViewById(R.id.voicecall_button_username);
        changingDot = findViewById(R.id.voicecall_textView_dot);

        new Thread() {
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dotCount++;
                                switch (dotCount) {
                                    case 0:
                                        changingDot.setText("");
                                        break;
                                    case 1:
                                        changingDot.setText(".");
                                        break;
                                    case 2:
                                        changingDot.setText("..");
                                        break;
                                    case 3:
                                        changingDot.setText("...");
                                        dotCount = -1;
                                        break;
                                }
                            }
                        });
                        Thread.sleep(300);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();


        String targetUid = ConversationManager.getInstance().getUidOfPrivateConv(conv.getConvId());
        UserInfoManager.getInstance().getUserInfo(targetUid, userInfo -> {
            friendName.setText(userInfo.getName());
            NetworkImageHelper.loadImage(friendIcon, userInfo.getHighResolutionPhotoUrl());
        });


        textViewTime.setVisibility(View.INVISIBLE);

        if (isInitiator) {
            buttonAccept.setVisibility(View.INVISIBLE);
            buttonDecline.setVisibility(View.INVISIBLE);
            connect();
        } else {
            buttonHangup.setVisibility(View.INVISIBLE);

            answerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            showDialogAndClose("Call Cancelled.");
                        }
                    });
                }
            }, ANSWER_TIMEOUT);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.voicecall_button_accept:
                answerTimer.cancel();
                answerTimer.purge();
                textViewTime.setVisibility(View.VISIBLE);
                buttonHangup.setVisibility(View.VISIBLE);
                buttonAccept.setVisibility(View.INVISIBLE);
                buttonDecline.setVisibility(View.INVISIBLE);

                connect();
                break;
            case R.id.voicecall_button_decline:
                answerTimer.cancel();
                answerTimer.purge();
                closeVoiceCall();
                finish();
                break;
            case R.id.voicecall_button_hangup:
                closeVoiceCall();
                finish();
                break;
        }
    }

    private void connect() {
        if (isInitiator) {
            textViewStatus.setText("Waiting");

            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            showDialogAndClose("No response from the other end");
                        }
                    });
                }
            }, WAITING_TIMEOUT);

        } else {
            textViewStatus.setText("Connecting");

            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            showDialogAndClose("Unable to connect");
                        }
                    });
                }
            }, CONNECTING_TIMEOUT);
        }

        VoiceCallEngine.getInstance().joinChannel(channelID, mEventHandler);

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to cancel this call?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        closeVoiceCall();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
        }
    }

    private void showDialogAndClose(String msg) {
        closeVoiceCall();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // go back
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    };

    private void closeVoiceCall() {
        conv.markAllAsRead();
        working = false;

        timeoutTimer.cancel();
        timeoutTimer.purge();
        VoiceCallEngine.getInstance().leaveChannel();

        thread.interrupt();
    }




}
