package au.edu.unimelb.eng.navibee

import android.app.Application
import timber.log.Timber


class NaviBeeApplication : Application() {

    companion object {
        lateinit var _instance: NaviBeeApplication

        @JvmStatic fun getInstance() = _instance
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
        Timber.plant(Timber.DebugTree())
    }

}