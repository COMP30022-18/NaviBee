package au.edu.unimelb.eng.navibee.social;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

import au.edu.unimelb.eng.navibee.R;

public class VoiceCallActivity extends AppCompatActivity {

    private static final String LOG_TAG = "VOICECALL";
    private static final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private RtcEngine mRtcEngine = null;

    private String channelID = "testChannel";
    private boolean isInitiator;

    private Timer timeoutTimer = new Timer();
    private Timer answerTimer = new Timer();

    private TextView textViewStatus;
    private TextView textViewTime;
    private Button buttonHangup;
    private Button buttonAccept;
    private Button buttonDecline;


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

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
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
                    // call started
                    timeoutTimer.cancel();
                    timeoutTimer.purge();
                    timeCount = 0;
                    textViewStatus.setText("");
                    thread.start();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        checkPermission();

        textViewStatus = findViewById(R.id.voicecall_textView_status);
        textViewTime = findViewById(R.id.voicecall_textView_time);
        buttonHangup = findViewById(R.id.voicecall_button_hangup);
        buttonAccept = findViewById(R.id.voicecall_button_accept);
        buttonDecline = findViewById(R.id.voicecall_button_decline);

        isInitiator = true;

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
            }, 20*1000);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.voicecall_button_accept:
                answerTimer.cancel();
                answerTimer.purge();
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
        initializeAgoraEngine();
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
            }, 40*1000);

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
            }, 5*1000);
        }

        mRtcEngine.joinChannel(null, channelID, "", 0);

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

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
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
        timeoutTimer.cancel();
        timeoutTimer.purge();
        if (mRtcEngine!=null) mRtcEngine.leaveChannel();
        mRtcEngine = null;
        thread.interrupt();
    }




}
