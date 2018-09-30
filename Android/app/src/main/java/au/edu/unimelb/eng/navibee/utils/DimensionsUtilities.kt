package au.edu.unimelb.eng.navibee.utils

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import au.edu.unimelb.eng.navibee.R

fun getStatusBarHeight(activity: View): Int {
    val resId = activity.resources.getIdentifier(
            "status_bar_height", "dimen", "android")
    if (resId > 0)
        return activity.resources.getDimensionPixelSize(resId)
    return 1024
}

fun getActionBarHeight(context: Context): Int =
    TypedValue().let {
        if (context.theme.resolveAttribute(R.attr.actionBarSize, it, true))
            TypedValue.complexToDimensionPixelSize(it.data, context.resources.displayMetrics)
        else
            0
    }

fun View.retrieveTopObscureHeight(callback: (View, Int) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        setOnApplyWindowInsetsListener { view, insets ->
            callback(view, insets.systemWindowInsetTop)
            insets
        }
    } else {
        callback(this, getStatusBarHeight(this) + getActionBarHeight(context))
    }
}