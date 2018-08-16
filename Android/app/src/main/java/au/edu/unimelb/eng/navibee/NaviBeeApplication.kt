package au.edu.unimelb.eng.navibee

import android.app.Application
import timber.log.Timber


class NaviBeeApplication : Application() {

    companion object {
        @JvmStatic lateinit var instance: NaviBeeApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Timber.plant(Timber.DebugTree())
    }

}