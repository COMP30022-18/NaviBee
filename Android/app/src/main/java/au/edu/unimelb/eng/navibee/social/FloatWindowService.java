package au.edu.unimelb.eng.navibee.social;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class FloatWindowService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public FloatWindowService getService() {
            return FloatWindowService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
