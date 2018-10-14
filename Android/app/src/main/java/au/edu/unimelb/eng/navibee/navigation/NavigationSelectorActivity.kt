package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import au.edu.unimelb.eng.navibee.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.bottomsheetdialog_navigation_choose_mean_of_transport.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.internals.AnkoInternals.createIntent
import org.jetbrains.anko.startActivityForResult

/**
 * Navigation method selector activity.
 *
 * Start this activity to start an activity.
 *
 * Args:
 *      EXTRA_LATITUDE: double, latitude of destination
 *      EXTRA_LONGITUDE: double, longitude of destination
 *      EXTRA_ORIGIN_NAME: CharSequence, name of origin, optional
 *      EXTRA_DESTINATION_NAME: CharSequence, name of destination, optional
 */
class NavigationSelectorActivity : AppCompatActivity() {

    private var latitude = 0.0
    private var longitude = 0.0
    private lateinit var originName: CharSequence
    private lateinit var destinationName: CharSequence

    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_ORIGIN_NAME = "origin_name"
        const val EXTRA_DESTINATION_NAME = "destination_name"
        private const val CHECK_LOCATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!intent.hasExtra(EXTRA_LATITUDE) || !intent.hasExtra(EXTRA_LONGITUDE)) {
            throw IllegalArgumentException("Latitude and longitude is required for navigation.")
        }

        latitude = intent.getDoubleExtra(EXTRA_LATITUDE, latitude)
        longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, longitude)
        originName = intent.getCharSequenceExtra(EXTRA_ORIGIN_NAME)
                ?: resources.getString(R.string.navigation_your_location)
        destinationName = intent.getCharSequenceExtra(EXTRA_DESTINATION_NAME)
                ?: resources.getString(R.string.navigation_default_destination)

        if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            showPopUp()
        } else {
            startActivityForResult<LocationPermissionRequestActivity>(
                    CHECK_LOCATION_PERMISSION
            )
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CHECK_LOCATION_PERMISSION -> {
                if (resultCode == Activity.RESULT_OK)
                    showPopUp()
            }
        }
    }

    private fun showPopUp() {
        when (getMeanOfTransport(applicationContext)) {
            MEAN_OF_TRANSPORT_ALWAYS_ASK -> {
                MeanOfTransportBottomSheetDialogFragment().run {
                    show(supportFragmentManager, tag)
                }
            }
            MEAN_OF_TRANSPORT_DRIVE -> startDrivingNavigation()
            MEAN_OF_TRANSPORT_WALK -> startWalkingNavigation()
            MEAN_OF_TRANSPORT_TRANSIT -> startTransitNavigation()
        }
    }

    fun startWalkingNavigation() {

        val intent = createIntent(this, NavigationActivity::class.java, arrayOf(
            NavigationActivity.EXTRA_DEST_LAT to latitude,
            NavigationActivity.EXTRA_DEST_LON to longitude,
            NavigationActivity.EXTRA_MEAN_OF_TRAVEL to NavigationActivity.MEAN_WALKING
        ))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        this.finish()
    }

    fun startDrivingNavigation() {

        val intent = createIntent(this, NavigationActivity::class.java, arrayOf(
            NavigationActivity.EXTRA_DEST_LAT to latitude,
            NavigationActivity.EXTRA_DEST_LON to longitude,
            NavigationActivity.EXTRA_MEAN_OF_TRAVEL to NavigationActivity.MEAN_DRIVING
        ))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        this.finish()
    }

    fun startTransitNavigation() {

        val intent = createIntent(this, TransitNavigationActivity::class.java, arrayOf(
            TransitNavigationActivity.EXTRA_LATITUDE to latitude,
            TransitNavigationActivity.EXTRA_LONGITUDE to longitude,
            TransitNavigationActivity.EXTRA_DESTINATION_NAME to destinationName,
            TransitNavigationActivity.EXTRA_ORIGIN_NAME to originName
        ))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        this.finish()
    }
}


class MeanOfTransportBottomSheetDialogFragment: BottomSheetDialogFragment() {
    private var previousChoice: String? = null

    private var chosenMethod: String? = null

    private lateinit var justOnce: MaterialButton
    private lateinit var always: MaterialButton

    private var white: Int = 0
    private var highlight: Int = 0

    private lateinit var view: LinearLayout
    private lateinit var parent: NavigationSelectorActivity
    private lateinit var appContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.bottomsheetdialog_navigation_choose_mean_of_transport,
                container, false) as LinearLayout
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        parent = activity as NavigationSelectorActivity
        appContext = context!!.applicationContext

        previousChoice = getPreviousMeanOfTransport(context!!)

        white = ContextCompat.getColor(context!!, R.color.white)
        highlight = ContextCompat.getColor(context!!, R.color.colorHighlight)

        justOnce = navigation_bsd_mean_of_transport_just_once
        always = navigation_bsd_mean_of_transport_always

        justOnce.setOnClickListener {
            chosenMethod?.let { method ->
                startNavigationByMethod(method)
                this.dismiss()
            }
        }

        always.setOnClickListener {
            chosenMethod?.let { method ->
                if (isFirstTimeSetNeverAskMeanOfTransport(context!!)) {
                    activity?.alert {
                        messageResource = R.string.prompt_first_time_always_preferred_mean_of_transport
                        positiveButton(R.string.button_got_it) { _ ->
                            setMeanOfTransport(appContext, method)
                            startNavigationByMethod(method)
                        }
                    }?.show()
                } else {
                    setMeanOfTransport(context!!, method)
                    startNavigationByMethod(method)

                }
                this.dismiss()
            }
        }

        if (previousChoice == null) {
            navigation_bsd_mean_of_transport_hr.visibility = View.GONE
            navigation_bsd_mean_of_transport_alt.visibility = View.GONE
            val actions = navigation_bsd_mean_of_transport_actions
            view.removeView(actions)
            view.addView(actions)

            justOnce.isEnabled = false
            always.isEnabled = false

            firstChoiceOnClick(MEAN_OF_TRANSPORT_DRIVE)
            firstChoiceOnClick(MEAN_OF_TRANSPORT_WALK)
            firstChoiceOnClick(MEAN_OF_TRANSPORT_TRANSIT)
        } else {
            navigation_bsd_mean_of_transport_title.visibility = View.GONE
            chosenMethod = previousChoice

            val pcFormat = resources.getString(R.string.dialog_method_of_travel_default)

            val pcView: TextView = when (previousChoice) {
                MEAN_OF_TRANSPORT_DRIVE ->
                    navigation_bsd_mean_of_transport_driving
                MEAN_OF_TRANSPORT_WALK ->
                    navigation_bsd_mean_of_transport_walking
                MEAN_OF_TRANSPORT_TRANSIT ->
                    navigation_bsd_mean_of_transport_transit
                else -> return
            }
            val altMethods = hashSetOf(
                    MEAN_OF_TRANSPORT_DRIVE,
                    MEAN_OF_TRANSPORT_WALK,
                    MEAN_OF_TRANSPORT_TRANSIT
            )
            altMethods.remove(previousChoice!!)
            pcView.text = pcFormat.format(pcView.text)
            pcView.isClickable = false
            pcView.isFocusable = false
            altMethods.forEach { altChoiceOnClick(it) }
            view.removeView(pcView)
            view.addView(pcView, 1)
        }
    }

    private fun firstChoiceOnClick(method: String) {
        val view = getButtonByMethod(method)
        justOnce.isEnabled = true
        always.isEnabled = true
        view.setOnClickListener {
            if (chosenMethod == method) {
                startNavigationByMethod(method)
                this.dismiss()
            } else {
                chosenMethod?.run {
                    getButtonByMethod(this).setBackgroundColor(white)
                }
                view.setBackgroundColor(highlight)
                chosenMethod = method
            }
        }
    }

    private fun altChoiceOnClick(method: String) {
        val view = getButtonByMethod(method)
        view.setOnClickListener {
            startNavigationByMethod(method)
            this.dismiss()
        }
    }

    private fun startNavigationByMethod(method: String) {
        setPreviousMeanOfTransport(appContext, method)
        when (method) {
            MEAN_OF_TRANSPORT_DRIVE -> parent.startDrivingNavigation()
            MEAN_OF_TRANSPORT_WALK -> parent.startWalkingNavigation()
            MEAN_OF_TRANSPORT_TRANSIT -> parent.startTransitNavigation()
        }
    }

    private fun getButtonByMethod(method: String) = when (method) {
        MEAN_OF_TRANSPORT_DRIVE -> navigation_bsd_mean_of_transport_driving
        MEAN_OF_TRANSPORT_WALK -> navigation_bsd_mean_of_transport_walking
        MEAN_OF_TRANSPORT_TRANSIT -> navigation_bsd_mean_of_transport_transit
        else -> throw IllegalArgumentException()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        parent.finish()
        super.onDismiss(dialog)
    }

    override fun onCancel(dialog: DialogInterface?) {
        parent.finish()
        super.onCancel(dialog)
    }
}
