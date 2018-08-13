package au.edu.unimelb.eng.navibee;

import android.app.Application;

public class NaviBeeApplication extends Application {

    private static NaviBeeApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static NaviBeeApplication getInstance() {
        return instance;
    }
}
