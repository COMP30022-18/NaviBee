package au.edu.unimelb.eng.navibee

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber




class NaviBeeApplication : Application() {

    companion object {
        @JvmStatic lateinit var instance: NaviBeeApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AndroidThreeTen.init(this)
        instance = this
    }
}