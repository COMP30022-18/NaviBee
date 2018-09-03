package au.edu.unimelb.eng.navibee.navigation

import android.content.Context
import com.beust.klaxon.Klaxon

const val NAVIGATION_PREFERENCE_KEY = "navigation_preference"
const val RECENT_QUERIES = "recentQueries"
const val RECENT_QUERIES_LENGTH = 5

fun getNavigationSharedPref(context: Context) =
        context.getSharedPreferences(
                NAVIGATION_PREFERENCE_KEY, Context.MODE_PRIVATE)!!

fun getRecentSearchQueries(context: Context): List<LocationSearchHistory> {
    return Klaxon()
            .parseArray(getNavigationSharedPref(context).getString(RECENT_QUERIES, "[]") ?: "[]")
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
    getNavigationSharedPref(context).edit()
            .putString(RECENT_QUERIES, json)
            .apply()


    return newList
}

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