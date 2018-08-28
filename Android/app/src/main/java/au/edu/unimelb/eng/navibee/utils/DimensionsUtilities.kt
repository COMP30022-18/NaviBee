package au.edu.unimelb.eng.navibee.utils

import android.view.View

fun getStatusBarHeight(activity: View): Int {
    val resId = activity.resources.getIdentifier(
            "status_bar_height", "dimen", "android")
    if (resId > 0)
        return activity.resources.getDimensionPixelSize(resId)
    return 1024
}