package au.edu.unimelb.eng.navibee.utils

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

fun sha256String(plainText: String): String {
    // Fallback to the plain text when something goes wrong
    var sha256 = plainText

    try {
        val crypt = MessageDigest.getInstance("SHA-256")
        crypt.reset()
        crypt.update(plainText.toByteArray(charset("UTF-8")))
        sha256 = byteToHex(crypt.digest())
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }
    return sha256
}

fun byteToHex(bytes: ByteArray): String {
    val formatter = Formatter()
    for (b in bytes) {
        formatter.format("%02x", b)
    }
    val result = formatter.toString()
    formatter.close()
    return result
}