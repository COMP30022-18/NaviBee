package au.edu.unimelb.eng.navibee.navigation

import android.content.Context
import au.edu.unimelb.eng.navibee.NaviBeeApplication
import com.beust.klaxon.Klaxon
import java.util.*

private const val NAVIGATION_PREFERENCE_KEY = "navigation_preference"
private const val RECENT_QUERIES = "recentQueries"
private const val RECENT_QUERIES_LENGTH = 5

private val sharedPref = NaviBeeApplication.instance.getSharedPreferences(
        NAVIGATION_PREFERENCE_KEY, Context.MODE_PRIVATE)

fun getRecentSearchQueries(): List<LocationSearchHistory> {
    return Klaxon()
            .parse(sharedPref.getString(RECENT_QUERIES, "[]") ?: "[]")
            ?: emptyList()
}

fun addRecentSearchQuery(item: LocationSearchHistory): List<LocationSearchHistory> {
    val list = getRecentSearchQueries().toMutableList()
    if (item in list) {
        list.removeAt(list.indexOfFirst { i -> i == item })
    }
    list.add(0, item)
    val newList = list.take(RECENT_QUERIES_LENGTH)

    val json = Klaxon().toJsonString(newList)
    sharedPref.edit().putString(RECENT_QUERIES, json).apply()
    return newList
}

data class LocationSearchHistory (
        val googlePlaceId: String,
        val name: String,
        val address: String,
        val lastSearchTime: Date = Date(),
        val photoReference: String
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