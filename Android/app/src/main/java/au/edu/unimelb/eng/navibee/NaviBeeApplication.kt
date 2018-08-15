package au.edu.unimelb.eng.navibee

import android.app.Application
import timber.log.Timber


class NaviBeeApplication : Application() {

    companion object {
        lateinit var naviBeeApplication: NaviBeeApplication

        @JvmStatic fun getInstance() = naviBeeApplication
    }

    override fun onCreate() {
        super.onCreate()
        naviBeeApplication = this
        Timber.plant(Timber.DebugTree())
    }

}