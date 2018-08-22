package au.edu.unimelb.eng.navibee.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ImageView
import au.edu.unimelb.eng.navibee.NaviBeeApplication
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class DownloadImageToImageViewAsyncTask(imageView: ImageView) : AsyncTask<String, Void, Bitmap>() {
    private val bmImage: WeakReference<ImageView> = WeakReference(imageView)

    override fun doInBackground(vararg args: String): Bitmap? {
        val url = args[0]
        var bitmap: Bitmap? = null
        val hashedUrl = hashUrl(url)
        val fileNames = NaviBeeApplication.instance.fileList()
        if (!Arrays.asList(*fileNames).contains(hashedUrl)) {
            try {
                val input = java.net.URL(url).openStream()
                bitmap = BitmapFactory.decodeStream(input)
                val file = File(NaviBeeApplication.instance.cacheDir, hashedUrl)
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                Log.e("Error", e.message)
                e.printStackTrace()
            }

        } else {
            try {
                val inputStream = NaviBeeApplication.instance.openFileInput(hashedUrl)
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: FileNotFoundException) {
            }

        }
        return bitmap
    }

    override fun onPostExecute(result: Bitmap?) {
        if (result == null) return
        bmImage.get()?.visibility = View.VISIBLE
        bmImage.get()?.setImageBitmap(result)
    }

    private fun hashUrl(url: String): String {
        var sha256 = ""
        try {
            val crypt = MessageDigest.getInstance("SHA-256")
            crypt.reset()
            crypt.update(url.toByteArray(charset("UTF-8")))
            sha256 = byteToHex(crypt.digest())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return sha256
    }

    private fun byteToHex(hash: ByteArray): String {
        val formatter = Formatter()
        for (b in hash) {
            formatter.format("%02x", b)
        }
        val result = formatter.toString()
        formatter.close()
        return result
    }
}
