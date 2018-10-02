package au.edu.unimelb.eng.navibee.navigation

import android.content.Context
import android.content.SharedPreferences
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate

@RunWith(PowerMockRunner::class)
@PowerMockRunnerDelegate(JUnit4::class)
@PrepareForTest(Context::class)
class PreferenceHelperKtTest {

    @Mock
    lateinit var mContext: Context
    @Mock
    lateinit var mSharedPreferences: SharedPreferences

    @Before
    fun setUp() {

        mContext = mock(Context::class.java)
        PowerMockito.mockStatic(mContext::class.java)

        mSharedPreferences = mock(SharedPreferences::class.java)

        assertNotNull(mContext)
        assertNotNull(mSharedPreferences)

        `when`(mSharedPreferences.getString(eq(RECENT_QUERIES), anyString())).thenReturn("""
            [
                {
                    "googlePlaceId": "google place id 1",
                    "name": "name 1",
                    "address": "address 1",
                    "lastSearchTime": 111111,
                    "photoReference": "photo reference 1"
                },
                {
                    "googlePlaceId": "google place id 2",
                    "name": "name 2",
                    "address": "address 2",
                    "lastSearchTime": 222222,
                    "photoReference": "photo reference 2"
                },
                {
                    "googlePlaceId": "google place id 3",
                    "name": "name 3",
                    "address": "address 3",
                    "lastSearchTime": 333333,
                    "photoReference": "photo reference 3"
                },
                {
                    "googlePlaceId": "google place id 4",
                    "name": "name 4",
                    "address": "address 4",
                    "lastSearchTime": 444444,
                    "photoReference": "photo reference 4"
                },
                {
                    "googlePlaceId": "google place id 5",
                    "name": "name 5",
                    "address": "address 5",
                    "lastSearchTime": 555555,
                    "photoReference": "photo reference 5"
                }
            ]
        """.trimIndent())

        `when`(mContext.getSharedPreferences(eq(NAVIGATION_PREFERENCE_KEY), anyInt())).thenReturn(mSharedPreferences)

    }

    @Test
    fun testGetRecentSearchQueries() {

        val expected = listOf(
                LocationSearchHistory(
                        googlePlaceId = "google place id 1",
                        name = "name 1",
                        address = "address 1",
                        lastSearchTime = 111111,
                        photoReference = "photo reference 1"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 2",
                        name = "name 2",
                        address = "address 2",
                        lastSearchTime = 222222,
                        photoReference = "photo reference 2"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 3",
                        name = "name 3",
                        address = "address 3",
                        lastSearchTime = 333333,
                        photoReference = "photo reference 3"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 4",
                        name = "name 4",
                        address = "address 4",
                        lastSearchTime = 444444,
                        photoReference = "photo reference 4"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 5",
                        name = "name 5",
                        address = "address 5",
                        lastSearchTime = 555555,
                        photoReference = "photo reference 5"
                )
        )

        assertEquals(expected, getRecentSearchQueries(mContext))

    }

    @Test
    fun testAddRecentSearchQuery() {

        `when`(mSharedPreferences.edit()).thenReturn(mock(SharedPreferences.Editor::class.java))

        var listRSQ = listOf(
                LocationSearchHistory(
                        googlePlaceId = "google place id 5",
                        name = "name 5",
                        address = "address 5",
                        lastSearchTime = 555555,
                        photoReference = "photo reference 5"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 1",
                        name = "name 1",
                        address = "address 1",
                        lastSearchTime = 111111,
                        photoReference = "photo reference 1"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 2",
                        name = "name 2",
                        address = "address 2",
                        lastSearchTime = 222222,
                        photoReference = "photo reference 2"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 3",
                        name = "name 3",
                        address = "address 3",
                        lastSearchTime = 333333,
                        photoReference = "photo reference 3"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 4",
                        name = "name 4",
                        address = "address 4",
                        lastSearchTime = 444444,
                        photoReference = "photo reference 4"
                )
        )

        var addItem = LocationSearchHistory(
                googlePlaceId = "google place id 5",
                name = "name 5",
                address = "address 5",
                lastSearchTime = 555555,
                photoReference = "photo reference 5"
        )

        assertEquals(listRSQ, addRecentSearchQuery(mContext, addItem))

        listRSQ = listOf(
                LocationSearchHistory(
                        googlePlaceId = "google place id 6",
                        name = "name 6",
                        address = "address 6",
                        lastSearchTime = 666666,
                        photoReference = "photo reference 6"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 1",
                        name = "name 1",
                        address = "address 1",
                        lastSearchTime = 111111,
                        photoReference = "photo reference 1"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 2",
                        name = "name 2",
                        address = "address 2",
                        lastSearchTime = 222222,
                        photoReference = "photo reference 2"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 3",
                        name = "name 3",
                        address = "address 3",
                        lastSearchTime = 333333,
                        photoReference = "photo reference 3"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 4",
                        name = "name 4",
                        address = "address 4",
                        lastSearchTime = 444444,
                        photoReference = "photo reference 4"
                )
        )

        addItem = LocationSearchHistory(
                googlePlaceId = "google place id 6",
                name = "name 6",
                address = "address 6",
                lastSearchTime = 666666,
                photoReference = "photo reference 6"
        )

        assertEquals(listRSQ, addRecentSearchQuery(mContext, addItem))

    }

    @After
    fun tearDown() {

        verify(mSharedPreferences, atLeastOnce()).getString(eq(RECENT_QUERIES), anyString())
        verify(mContext, atLeastOnce()).getSharedPreferences(eq(NAVIGATION_PREFERENCE_KEY), anyInt())

    }

}