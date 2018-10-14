package au.edu.unimelb.eng.navibee.social;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.TypedValue;
import android.os.PowerManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import au.edu.unimelb.eng.navibee.utils.NetworkImageHelper;

import au.edu.unimelb.eng.navibee.R;

public class VoiceCallActivity extends AppCompatActivity {

    private static final int PERMISSIONS_RECORD_AUDIO = 1;


    private VoiceCallService voiceCallService = VoiceCallService.getInstance();

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

    private int padding;

    private PowerManager.WakeLock mWakeLock;



    Thread thread = new Thread() {

        @Override
        public void run() {
            try {
                while (!thread.isInterrupted()) {
                    Thread.sleep(500);
                    runOnUiThread(() -> {

                        // time
                        Date startTime = voiceCallService.getStartTime();
                        if (startTime == null) {
                            textViewTime.setText("");
                        } else {
                            long secondEscapse = (new Date().getTime() - startTime.getTime()) / 1000;

                            String text = String.format("%d:%02d:%02d", (secondEscapse / 60 / 60) % 60,
                                    (secondEscapse / 60) % 60, secondEscapse % 60);

                            textViewTime.setText(text);
                        }

                        // dot
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

                    });
                }
            } catch (InterruptedException e) {
            }
        }
    };

    BroadcastReceiver voicecallUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
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
        buttonMic = findViewById(R.id.voicecall_button_mic);
        buttonSpeaker = findViewById(R.id.voicecall_button_speaker);
        buttonMic.setVisibility(View.INVISIBLE);
        buttonSpeaker.setVisibility(View.INVISIBLE);

        friendIcon = findViewById(R.id.voicecall_friend_icon);
        friendName = findViewById(R.id.voicecall_button_username);
        changingDot = findViewById(R.id.voicecall_textView_dot);

        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        PowerManager mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "Navibee:VoiceCall");

        thread.start();
        updateUI();

    }


    private void updateUI() {
        VoiceCallService.Status status = voiceCallService.getStatus();

        if (status == VoiceCallService.Status.Idle) {
            // call ended
            finish();
            return;
        }

        String targetUid = voiceCallService.getTargetUid();
        UserInfoManager.getInstance().getUserInfo(targetUid, userInfo -> {
            friendName.setText(userInfo.getName());
            NetworkImageHelper.loadImage(friendIcon, userInfo.getHighResolutionPhotoUrl());
        });

        // case waiting for response
        if (voiceCallService.getStatus() == VoiceCallService.Status.Waiting) {
            textViewStatus.setVisibility(View.VISIBLE);
            textViewStatus.setText("Waiting");
            changingDot.setVisibility(View.VISIBLE);
            buttonMic.setVisibility(View.INVISIBLE);
            buttonSpeaker.setVisibility(View.INVISIBLE);

            buttonAccept.setVisibility(View.VISIBLE);
            buttonDecline.setVisibility(View.VISIBLE);
            buttonHangup.setVisibility(View.INVISIBLE);
        }
        // case accepted and connecting
        else if (voiceCallService.getStatus() == VoiceCallService.Status.Connecting){
            textViewStatus.setVisibility(View.VISIBLE);
            textViewStatus.setText("Connecting");
            changingDot.setVisibility(View.VISIBLE);
            buttonMic.setVisibility(View.INVISIBLE);
            buttonSpeaker.setVisibility(View.INVISIBLE);
            buttonAccept.setVisibility(View.INVISIBLE);
            buttonDecline.setVisibility(View.INVISIBLE);
            buttonHangup.setVisibility(View.VISIBLE);
        }
        // case calling
        else {
            textViewStatus.setVisibility(View.INVISIBLE);
            changingDot.setVisibility(View.INVISIBLE);
            buttonMic.setVisibility(View.VISIBLE);
            buttonSpeaker.setVisibility(View.VISIBLE);
            buttonAccept.setVisibility(View.INVISIBLE);
            buttonDecline.setVisibility(View.INVISIBLE);
            buttonHangup.setVisibility(View.VISIBLE);
            if (voiceCallService.getIsMicEnabled()){
                buttonMic.setImageResource(R.drawable.ic_mic_black_24dp);
                buttonMic.setBackgroundResource(R.drawable.voicecall_mic_background);
            }
            else{
                buttonMic.setImageResource(R.drawable.ic_mic_off_white_24dp);
                buttonMic.setBackgroundResource(R.drawable.voicecall_button_background_white_hollow);
            }
            if (voiceCallService.getIsSpeakerEnabled()){
                buttonSpeaker.setImageResource(R.drawable.ic_speaker_black_24dp);
                buttonSpeaker.setBackgroundResource(R.drawable.voicecall_mic_background);
            }
            else{
                buttonSpeaker.setImageResource(R.drawable.ic_speaker_white_24dp);
                buttonSpeaker.setBackgroundResource(R.drawable.voicecall_button_background_white_hollow);
            }
            buttonMic.setPadding(padding, padding, padding, padding);
            buttonSpeaker.setPadding(padding, padding, padding, padding);
        }

        // mWakeLock
        if (voiceCallService.getStatus() == VoiceCallService.Status.Calling) {
            if (mWakeLock != null) {
                if (!mWakeLock.isHeld()) {
                    mWakeLock.acquire();
                }
            }
        } else {
            if (mWakeLock != null) {
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        thread.interrupt();
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        unregisterReceiver(voicecallUpdateReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        registerReceiver(voicecallUpdateReceiver, new IntentFilter(VoiceCallService.BROADCAST_VOICECALL_UPDATE));
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.voicecall_button_accept:
                voiceCallService.answerCall();
                updateUI();
                break;
            case R.id.voicecall_button_decline:
                voiceCallService.closeVoiceCall();
                finish();
                break;
            case R.id.voicecall_button_hangup:
                voiceCallService.closeVoiceCall();
                finish();
                break;
            case R.id.voicecall_button_mic:
                voiceCallService.toggleMute();
                updateUI();
                break;
            case R.id.voicecall_button_speaker:
                voiceCallService.toggleSpeaker();
                updateUI();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        voiceCallService.showFloatWindow();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_RECORD_AUDIO);
        }
    }

}
