package au.edu.unimelb.eng.navibee.social;

import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;
import au.edu.unimelb.eng.navibee.R;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class VoiceCallEngine {
    private static final String LOG_TAG = "VC_ENGINE";
    private static VoiceCallEngine instance = null;

    public static VoiceCallEngine getInstance() {
        if(instance == null) {
            instance = new VoiceCallEngine();
        }
        return instance;
    }

    private RtcEngine mRtcEngine;
    private EventHandler handler = null;
    private final Lock mutex = new ReentrantLock(true);

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserOffline(final int uid, final int reason) {
            mutex.lock();
            if (handler!=null) handler.onUserOffline(uid, reason);
            mutex.unlock();
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            mutex.lock();
            if (handler!=null) handler.onUserJoined(uid, elapsed);
            mutex.unlock();
        }
    };

    private VoiceCallEngine() {
        try {
            mRtcEngine = RtcEngine.create(NaviBeeApplication.getInstance().getBaseContext(), NaviBeeApplication.getInstance().getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
    }

    public void joinChannel(String channelID, EventHandler handler) {
        mutex.lock();
        this.handler = handler;
        mutex.unlock();
        mRtcEngine.joinChannel(null, channelID, "", 0);
    }

    public void leaveChannel() {
        mutex.lock();
        handler = null;
        mutex.unlock();
        mRtcEngine.leaveChannel();
    }


    public static abstract class EventHandler{
        public abstract void onUserOffline(final int uid, final int reason);
        public abstract void onUserJoined(int uid, int elapsed);
    }
}
