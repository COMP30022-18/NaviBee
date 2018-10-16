package au.edu.unimelb.eng.navibee

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.preference.PreferenceManager
import au.edu.unimelb.eng.navibee.social.ConversationManager
import au.edu.unimelb.eng.navibee.sos.FallDetection
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import com.jakewharton.threetenabp.AndroidThreeTen
import net.time4j.android.ApplicationStarter
import timber.log.Timber
import java.util.*

class NaviBeeApplication : Application() {


    public var inited = false
        private set

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

        init()
    }

    public fun init() {
        if (FirebaseAuth.getInstance().currentUser != null && !inited) {
            firestoreTimestamp()

            inited = true
            ConversationManager.init()
            setFCMToken()

            val isEnabled = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("countdown_enabled", true)

            if (isEnabled) {
                FallDetection.getInstance().start()
            }

            // notification channel

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = getString(R.string.message_notification_channel)
                val description = getString(R.string.message_notification_channel_desc)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(MyFirebaseMessagingService.CHANNEL_ID, name, importance)
                channel.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager!!.createNotificationChannel(channel)
            }

        }
    }

    public fun uninit() {
        ConversationManager.getInstance().stopListening();
        inited = false;
        FallDetection.getInstance().stop();
    }


    private fun setFCMToken() {
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Get Instance ID token
                        val token = task.result!!.token
                        val uid = Objects.requireNonNull<FirebaseUser>(FirebaseAuth.getInstance()
                                .currentUser).uid

                        val docData = HashMap<String, Any>()
                        docData["uid"] = uid
                        docData["lastSeen"] = Timestamp(Date())
                        val db = FirebaseFirestore.getInstance()
                        db.collection("fcmTokens").document(token).set(docData)
                    }

                }
    }


    // The behavior for java.util.Date objects stored in Firestore is going to chang
    private fun firestoreTimestamp() {
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}