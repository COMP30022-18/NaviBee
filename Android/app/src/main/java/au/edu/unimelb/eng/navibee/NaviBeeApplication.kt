package au.edu.unimelb.eng.navibee

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import net.time4j.android.ApplicationStarter
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
        ApplicationStarter.initialize(this, true) // with prefetch on background thread
        instance = this
    }
}