package au.edu.unimelb.eng.navibee.social;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.os.PowerManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yhao.floatwindow.FloatWindow;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;
import au.edu.unimelb.eng.navibee.utils.NetworkImageHelper;

import au.edu.unimelb.eng.navibee.R;

public class VoiceCallActivity extends AppCompatActivity {

    public static final int VOCIECALL_EXPIRE = 60 * 1000;

    private static final int OVERLAY_PERMISSION_REQ_CODE = 0x001;
    private static final int PERMISSIONS_RECORD_AUDIO = 1;
    private static final int CONNECTING_TIMEOUT = 5 * 1000;
    private static final int ANSWER_TIMEOUT = 20 * 1000;
    private static final int WAITING_TIMEOUT = 40 * 1000;

    private static boolean working = false;

    private VoiceCallService voiceCallService = VoiceCallService.getInstance();


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
    private ImageView buttonMic;
    private ImageView buttonSpeaker;
    private boolean micEnabled = true;
    private boolean speakerEnabled = false;

    private boolean callStarted = false;
    private int padding;

    private boolean minimiseEnabled = false;

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;


    private long timeCount;
    Thread thread = new Thread() {

        @Override
        public void run() {
            try {
                while (!thread.isInterrupted()) {
                    Thread.sleep(1000);
                    runOnUiThread(() -> {
                        timeCount += 1;
                        String text = String.format("%d:%02d:%02d", (timeCount / 60 / 60) % 60,
                                    (timeCount / 60) % 60, timeCount % 60);
                        textViewTime.setText(text);

                    });
                }
            } catch (InterruptedException e) {
            }
        }
    };

    private final VoiceCallEngine.EventHandler mEventHandler = new VoiceCallEngine.EventHandler() {
        @Override
        public void onUserOffline(final int uid, final int reason) {
            runOnUiThread(() -> showDialogAndClose("Call Cancelled."));
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                if (!callStarted) {
                    // call started
                    callStarted = true;
                    timeoutTimer.cancel();
                    timeoutTimer.purge();
                    timeCount = 0;
                    textViewStatus.setText("");
                    textViewTime.setVisibility(View.VISIBLE);
                    changingDot.setVisibility(View.INVISIBLE);
                    buttonMic.setVisibility(View.VISIBLE);
                    buttonSpeaker.setVisibility(View.VISIBLE);
                    thread.start();

                    if (mWakeLock == null) {
                        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "Navibee:VoiceCall");
                    }
                    if (!mWakeLock.isHeld()) {
                        mWakeLock.acquire();
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

        textViewStatus = findViewById(R.id.voicecall_textView_status);
        textViewTime = findViewById(R.id.voicecall_textView_time);
        buttonHangup = findViewById(R.id.voicecall_button_hangup);
        buttonAccept = findViewById(R.id.voicecall_button_accept);
        buttonDecline = findViewById(R.id.voicecall_button_decline);
        buttonMic = findViewById(R.id.voicecall_button_mic);
        buttonSpeaker = findViewById(R.id.voicecall_button_speaker);
        buttonMic.setVisibility(View.INVISIBLE);
        buttonSpeaker.setVisibility(View.INVISIBLE);

        friendIcon = findViewById(R.id.voicecall_friend_icon);
        friendName = findViewById(R.id.voicecall_button_username);
        changingDot = findViewById(R.id.voicecall_textView_dot);

        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        updateUI();
    }


    private void updateUI() {
        VoiceCallService.Status status = voiceCallService.getStatus();

        if (status == VoiceCallService.Status.Idle) {
            // call ended
            finish();
        }

//        new Thread() {
//            public void run() {
//                try {
//                    while (!thread.isInterrupted()) {
//                        runOnUiThread(() -> {
//                            dotCount++;
//                            switch (dotCount) {
//                                case 0:
//                                    changingDot.setText("");
//                                    break;
//                                case 1:
//                                    changingDot.setText(".");
//                                    break;
//                                case 2:
//                                    changingDot.setText("..");
//                                    break;
//                                case 3:
//                                    changingDot.setText("...");
//                                    dotCount = -1;
//                                    break;
//                            }
//                        });
//                        Thread.sleep(300);
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();



        String targetUid = voiceCallService.getTargetUid();
        UserInfoManager.getInstance().getUserInfo(targetUid, userInfo -> {
            friendName.setText(userInfo.getName());
            NetworkImageHelper.loadImage(friendIcon, userInfo.getHighResolutionPhotoUrl());
        });



        if (voiceCallService.getStatus() == VoiceCallService.Status.Waiting) {
            textViewStatus.setVisibility(View.VISIBLE);
            textViewStatus.setText("Waiting");
            changingDot.setVisibility(View.VISIBLE);


            if (isInitiator){
                buttonMic.setVisibility(View.VISIBLE);
                buttonSpeaker.setVisibility(View.VISIBLE);
                buttonAccept.setVisibility(View.INVISIBLE);
                buttonDecline.setVisibility(View.INVISIBLE);
                buttonHangup.setVisibility(View.VISIBLE);
            }
            else{
                buttonMic.setVisibility(View.INVISIBLE);
                buttonSpeaker.setVisibility(View.INVISIBLE);
                buttonAccept.setVisibility(View.VISIBLE);
                buttonDecline.setVisibility(View.VISIBLE);
                buttonHangup.setVisibility(View.INVISIBLE);
            }
        }
        else if (voiceCallService.getStatus() == VoiceCallService.Status.Connecting){
            textViewStatus.setVisibility(View.VISIBLE);
            textViewStatus.setText("Connecting");
            changingDot.setVisibility(View.VISIBLE);
            buttonMic.setVisibility(View.VISIBLE);
            buttonSpeaker.setVisibility(View.VISIBLE);
            buttonAccept.setVisibility(View.INVISIBLE);
            buttonDecline.setVisibility(View.INVISIBLE);
            buttonHangup.setVisibility(View.VISIBLE);
        } else {
            textViewStatus.setVisibility(View.INVISIBLE);
            changingDot.setVisibility(View.INVISIBLE);
            buttonMic.setVisibility(View.VISIBLE);
            buttonSpeaker.setVisibility(View.VISIBLE);
            buttonAccept.setVisibility(View.INVISIBLE);
            buttonDecline.setVisibility(View.INVISIBLE);
            buttonHangup.setVisibility(View.VISIBLE);
        }


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
                    runOnUiThread(() -> showDialogAndClose("Call Cancelled."));
                }
            }, ANSWER_TIMEOUT);
        }


        mEventHandler.onUserJoined(0,0);
    }

    @Override
    protected void onPause() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (callStarted) {
            if (mWakeLock != null) {
                if (!mWakeLock.isHeld()) {
                    mWakeLock.acquire();
                }
            }
        }
        super.onResume();
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
            case R.id.voicecall_button_mic:
                if (micEnabled){
                    micEnabled = false;
                    VoiceCallEngine.getInstance().muteMic();
                    buttonMic.setImageResource(R.drawable.ic_mic_off_white_24dp);
                    buttonMic.setBackgroundResource(R.drawable.voicecall_button_background_white_hollow);
                }
                else{
                    VoiceCallEngine.getInstance().unmuteMic();
                    micEnabled = true;
                    buttonMic.setImageResource(R.drawable.ic_mic_black_24dp);
                    buttonMic.setBackgroundResource(R.drawable.voicecall_mic_background);
                }
                buttonMic.setPadding(padding, padding, padding, padding);
                break;
            case R.id.voicecall_button_speaker:
                if (speakerEnabled){
                    VoiceCallEngine.getInstance().useEarpiece();
                    speakerEnabled = false;
                    buttonSpeaker.setImageResource(R.drawable.ic_speaker_white_24dp);
                    buttonSpeaker.setBackgroundResource(R.drawable.voicecall_button_background_white_hollow);
                }
                else{
                    VoiceCallEngine.getInstance().useSpeaker();
                    speakerEnabled = true;
                    buttonSpeaker.setImageResource(R.drawable.ic_speaker_black_24dp);
                    buttonSpeaker.setBackgroundResource(R.drawable.voicecall_mic_background);
                }
                buttonSpeaker.setPadding(padding, padding, padding, padding);
                break;
        }
    }

//    @Override
//    public void onBackPressed() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Do you want to cancel this call?")
//                .setNegativeButton("No", null)
//                .setPositiveButton("Yes", (dialog, id) -> {
//                    closeVoiceCall();
//                    finish();
//                });
//        AlertDialog alert = builder.create();
//        alert.show();
//    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_RECORD_AUDIO);
        }
    }

//    private void showDialogAndClose(String msg) {
//        closeVoiceCall();
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(msg)
//                .setCancelable(false)
//                .setPositiveButton("OK", (dialog, id) -> {
//                    // go back
//                    finish();
//                });
//        AlertDialog alert = builder.create();
//        alert.show();
//    };
//
//    private void closeVoiceCall() {
//        conv.markAllAsRead();
//        working = false;
//
//        timeoutTimer.cancel();
//        timeoutTimer.purge();
////        VoiceCallEngine.getInstance().leaveChannel();
//
//        thread.interrupt();
//
//        if (mWakeLock != null && mWakeLock.isHeld()) {
//            mWakeLock.release();
//        }
//    }



}
