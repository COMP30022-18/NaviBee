package au.edu.unimelb.eng.navibee.navigation

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.beust.klaxon.Klaxon

const val NAVIGATION_PREFERENCE_KEY = "navigation_preference"
const val RECENT_QUERIES = "recent_queries"
const val RECENT_QUERIES_LENGTH = 5
const val FIRST_TIME_SET_NEVER_ASK_MEAN_OF_TRANSPORT = "first_time_set_never_ask_mean_of_transport"

private const val MEAN_OF_TRANSPORT = "navigation_preferred_mean_of_transport"
private const val LAST_MEAN_OF_TRANSPORT = "navigation_last_mean_of_transport"
const val MEAN_OF_TRANSPORT_ALWAYS_ASK = "always_ask"
const val MEAN_OF_TRANSPORT_TRANSIT = "transit"
const val MEAN_OF_TRANSPORT_DRIVE = "drive"
const val MEAN_OF_TRANSPORT_WALK = "walk"

private const val SEARCH_REGION = "navigation_search_region"
private const val DEFAULT_SEARCH_REGION = "au"

fun getNavigationSharedPref(context: Context): SharedPreferences =
        context.getSharedPreferences(
                NAVIGATION_PREFERENCE_KEY, Context.MODE_PRIVATE)

fun getRecentSearchQueries(context: Context): List<LocationSearchHistory> {
    return Klaxon()
            .parseArray(getNavigationSharedPref(context)
                    .getString(RECENT_QUERIES, "[]") ?: "[]")
            ?: emptyList()
}

fun addRecentSearchQuery(context: Context, item: LocationSearchHistory): List<LocationSearchHistory> {
    val list = getRecentSearchQueries(context).toMutableList()
    if (item in list) {
        list.removeAt(list.indexOfFirst { i -> i == item })
    }
    list.add(0, item)
    val newList = list.take(RECENT_QUERIES_LENGTH)

    val json = Klaxon().toJsonString(newList)
    getNavigationSharedPref(context).edit {
        putString(RECENT_QUERIES, json)
    }

    return newList
}

fun isFirstTimeSetNeverAskMeanOfTransport(context: Context): Boolean {
    val pref = getNavigationSharedPref(context)
    val ans = pref.getBoolean(FIRST_TIME_SET_NEVER_ASK_MEAN_OF_TRANSPORT, true)
    if (ans) {
        pref.edit { putBoolean(FIRST_TIME_SET_NEVER_ASK_MEAN_OF_TRANSPORT, false) }
    }
    return ans
}

fun getPreviousMeanOfTransport(context: Context): String? =
    getNavigationSharedPref(context).getString(LAST_MEAN_OF_TRANSPORT, null)

fun setPreviousMeanOfTransport(context: Context, value: String) {
    getNavigationSharedPref(context).edit {
        putString(LAST_MEAN_OF_TRANSPORT, value)
    }
}

fun getMeanOfTransport(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString(MEAN_OF_TRANSPORT, MEAN_OF_TRANSPORT_ALWAYS_ASK)
            ?: MEAN_OF_TRANSPORT_ALWAYS_ASK

fun setMeanOfTransport(context: Context, value: String) {
    PreferenceManager.getDefaultSharedPreferences(context).edit {
        putString(MEAN_OF_TRANSPORT, value)
    }
}

fun getSearchRegion(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SEARCH_REGION, DEFAULT_SEARCH_REGION)
                ?: DEFAULT_SEARCH_REGION

data class LocationSearchHistory (
        val googlePlaceId: String,
        val name: CharSequence,
        val address: CharSequence,
        val lastSearchTime: Long = System.currentTimeMillis(),
        val photoReference: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other is LocationSearchHistory) {
            return googlePlaceId == other.googlePlaceId
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return "LocationSearchHistory-$googlePlaceId".hashCode()
    }
}