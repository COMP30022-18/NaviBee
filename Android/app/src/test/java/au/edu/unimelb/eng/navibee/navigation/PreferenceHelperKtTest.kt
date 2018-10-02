package au.edu.unimelb.eng.navibee.navigation

import android.content.Context
import android.content.SharedPreferences
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
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

    }

    @Test
    fun testGetRecentSearchQueries() {

        `when`(mSharedPreferences.getString(eq(RECENT_QUERIES), anyString())).thenReturn("""
            [
                {
                    "googlePlaceId": "google place id",
                    "name": "name",
                    "address": "address",
                    "lastSearchTime": 2333333,
                    "photoReference": "photo reference"
                },
                {
                    "googlePlaceId": "google place id 2",
                    "name": "name 2",
                    "address": "address 2",
                    "lastSearchTime": 4666666,
                    "photoReference": "photo reference 2"
                },
                {
                    "googlePlaceId": "google place id 3",
                    "name": "name 3",
                    "address": "address 3",
                    "lastSearchTime": 6999999,
                    "photoReference": "photo reference 3"
                }
            ]
        """.trimIndent())

        `when`(mContext.getSharedPreferences(eq(NAVIGATION_PREFERENCE_KEY), anyInt())).thenReturn(mSharedPreferences)

        val expected = listOf(
                LocationSearchHistory(
                        googlePlaceId = "google place id",
                        name = "name",
                        address = "address",
                        lastSearchTime = 2333333,
                        photoReference = "photo reference"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 2",
                        name = "name 2",
                        address = "address 2",
                        lastSearchTime = 4666666,
                        photoReference = "photo reference 2"
                ),
                LocationSearchHistory(
                        googlePlaceId = "google place id 3",
                        name = "name 3",
                        address = "address 3",
                        lastSearchTime = 6999999,
                        photoReference = "photo reference 3"
                )
        )

        assertEquals(expected, getRecentSearchQueries(mContext))

    }

    @After
    fun tearDown() {
    }

}