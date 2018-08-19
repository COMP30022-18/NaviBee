package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import au.edu.unimelb.eng.navibee.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Request permission location.
 *
 * Incoming parameters:
 *      "query": string. Search query used.
 *      "snackBarLayout": int. Coordinator layout to show snack bar for error information
 */
class LocationPermissionRequestActivity: AppCompatActivity(), LocationPermissionRationaleConfirmListener {


    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        // Callback code for requesting fine location.
        private const val PERMISSIONS_REQUEST_FINE_LOCATION = 2
    }

    private var snackBarLayout = 0
    private lateinit var resultIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Get snack bar layout ID
        snackBarLayout = intent.getIntExtra("snackBarLayout", 0)
        resultIntent = Intent()
                .putExtra("query", intent.getStringExtra("query"))

        if (hasLocationPermission()) {
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission() = ContextCompat
            .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED


    override fun onAcknowledgedLocationPermissionRationale(dialog: DialogFragment) {
        requestLocationPermission(fromRationale = true)
    }


    private fun requestLocationPermission(fromRationale: Boolean = false) {
        if (!hasLocationPermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) && !fromRationale) {
                LocationPermissionRationalConfirmFragment().show(supportFragmentManager,
                        "locationPermissionRationale")
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSIONS_REQUEST_FINE_LOCATION)

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setResult(Activity.RESULT_OK, resultIntent)

                } else {
                    Snackbar.make(findViewById(snackBarLayout),
                            R.string.location_required_for_navigation,
                            Snackbar.LENGTH_LONG).show()
                    setResult(Activity.RESULT_CANCELED, resultIntent)
                }
                finish()
            }
        }
    }

}


interface LocationPermissionRationaleConfirmListener {
    fun onAcknowledgedLocationPermissionRationale(dialog: DialogFragment)
}

class LocationPermissionRationalConfirmFragment: DialogFragment() {

    private lateinit var listener: LocationPermissionRationaleConfirmListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.let {
            it.setMessage(R.string.location_required_for_navigation)
            it.setPositiveButton(R.string.button_got_it) { _, _ ->
                listener.onAcknowledgedLocationPermissionRationale(this)
            }
        }

        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = context as LocationPermissionRationaleConfirmListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement LocationPermissionRationaleConfirmListener")
        }
    }
}