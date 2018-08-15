package au.edu.unimelb.eng.navibee

import android.app.Application
import timber.log.Timber


class NaviBeeApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}