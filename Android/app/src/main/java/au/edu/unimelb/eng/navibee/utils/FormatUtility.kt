package au.edu.unimelb.eng.navibee.utils

import android.text.format.DateUtils.*
import au.edu.unimelb.eng.navibee.NaviBeeApplication
import java.util.*

fun chatDateShortFormat(time: Long): String {

    val c = NaviBeeApplication.instance

    val thenCal = GregorianCalendar()
    thenCal.timeInMillis = time
    val nowCal = GregorianCalendar()
    nowCal.timeInMillis = System.currentTimeMillis()

    return if (thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
            && thenCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
            && thenCal.get(Calendar.DAY_OF_MONTH) == nowCal.get(Calendar.DAY_OF_MONTH)) {
        // Same day
        formatDateTime(c, time, FORMAT_SHOW_TIME)
    } else if (thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
            && thenCal.get(Calendar.WEEK_OF_YEAR) == nowCal.get(Calendar.WEEK_OF_YEAR)) {
        // Same week
        formatDateTime(c, time, FORMAT_SHOW_WEEKDAY or FORMAT_ABBREV_WEEKDAY)
    } else if (thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {
        // Same year
        formatDateTime(c, time, FORMAT_ABBREV_MONTH)
    } else {
        formatDateTime(c, time, FORMAT_SHOW_YEAR or FORMAT_ABBREV_MONTH)
    }
}


fun chatDateMediumFormat(time: Long): String {

    val c = NaviBeeApplication.instance

    val thenCal = GregorianCalendar()
    thenCal.timeInMillis = time
    val nowCal = GregorianCalendar()
    nowCal.timeInMillis = System.currentTimeMillis()

    return if (thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
            && thenCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
            && thenCal.get(Calendar.DAY_OF_MONTH) == nowCal.get(Calendar.DAY_OF_MONTH)) {
        // Same day
        formatDateTime(c, time, FORMAT_SHOW_TIME)
    } else if (thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
            && thenCal.get(Calendar.WEEK_OF_YEAR) == nowCal.get(Calendar.WEEK_OF_YEAR)) {
        // Same week
        formatDateTime(c, time, FORMAT_SHOW_WEEKDAY)
    } else if (thenCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {
        // Same year
        formatDateTime(c, time, 0)
    } else {
        formatDateTime(c, time, FORMAT_SHOW_YEAR)
    }
}