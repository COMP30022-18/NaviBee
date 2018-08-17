package au.edu.unimelb.eng.navibee.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.View
import android.widget.ImageView
import timber.log.Timber
import java.lang.ref.WeakReference


class DownloadImageToImageViewAsyncTask(imageView: ImageView) : AsyncTask<String, Void, Bitmap>() {
    private val bmImage: WeakReference<ImageView> = WeakReference(imageView)

    override fun doInBackground(vararg args: String): Bitmap? {
        val url = args[0]
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeStream(java.net.URL(url).openStream())
        } catch (e: Exception) {
            Timber.e(e, "Error occurred while downloading image for image view.")
        }
        return bitmap
    }

    override fun onPostExecute(result: Bitmap) {
        bmImage.get()?.visibility = View.VISIBLE
        bmImage.get()?.setImageBitmap(result)
    }
}
