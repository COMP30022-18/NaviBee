package au.edu.unimelb.eng.navibee.utils

import android.content.Context
import android.util.DisplayMetrics
import timber.log.Timber

fun updateDpi(context: Context) {
    if (android.os.Build.PRODUCT == "taimen") {
        val displayMetrics = context.resources.displayMetrics
        Timber.d("Density before: ${context.resources.displayMetrics.densityDpi}")
        val config = context.resources.configuration
        displayMetrics.densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE
        config.densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE
        context.resources.displayMetrics.setTo(displayMetrics)
        context.resources.configuration.setTo(config)
        context.resources.updateConfiguration(config, displayMetrics)
        context.createConfigurationContext(config)
        Timber.d("Density after: ${context.resources.displayMetrics.densityDpi}")
    }
}