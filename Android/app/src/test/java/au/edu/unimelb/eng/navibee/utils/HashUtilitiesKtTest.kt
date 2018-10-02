package au.edu.unimelb.eng.navibee.utils

import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class HashUtilitiesKtTest {

    @Before
    fun setUp() {
    }

    @Test
    fun testSha256String() {

        val testString = "test"
        val hashString = "9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08"
        assertEquals(hashString.toLowerCase(), sha256String(testString))

        val testString2 = "NaviBee"
        val hashString2 = "1DDD932A4F0AA638841624EF9ABF995DDBEFC6FD7174B525793945ABFA0CDCAC"
        assertEquals(hashString2.toLowerCase(), sha256String(testString2))

    }

    @Test
    fun testByteToHex() {

        val bytes = byteArrayOf(10, 11, 12, 13, 14, 15)
        val value = "0A0B0C0D0E0F"
        assertEquals(value.toLowerCase(), byteToHex(bytes))

        val bytes2 = byteArrayOf(26, 43, 60, 77, 94, 111)
        val value2 = "1A2B3C4D5E6F"
        assertEquals(value2.toLowerCase(), byteToHex(bytes2))

    }

    @After
    fun tearDown() {
    }

}