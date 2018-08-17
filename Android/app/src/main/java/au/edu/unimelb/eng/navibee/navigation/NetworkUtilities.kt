package au.edu.unimelb.eng.navibee.navigation

import android.os.AsyncTask
import android.widget.ImageView
import au.edu.unimelb.eng.navibee.utils.DownloadImageToImageViewAsyncTask
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.security.MessageDigest

class DownloadImageFromWikiDataToImageViewAsyncTask(imageView: ImageView) : AsyncTask<String, Void, String>() {
    companion object {
        private val client = OkHttpClient()
        private val md5Instance = MessageDigest.getInstance("MD5")
    }

    private val bmImage: WeakReference<ImageView> = WeakReference(imageView)


    override fun doInBackground(vararg args: String): String? {
        val wikiData = args[0]
        val req = Request.Builder()
                .url("https://www.wikidata.org/w/api.php?" +
                        "action=wbgetclaims&" +
                        "entity=$wikiData&" +
                        "property=P18&" +
                        "format=json").build()
        val resp = client.newCall(req).execute()
        return resp.body()?.string()
    }

    override fun onPostExecute(result: String?) {
        if (result == null)
            return
        val json = JSONObject(result)
        if (!json.has("claims") || !json.getJSONObject("claims").has("P18"))
            return
        val pictures = json.getJSONObject("claims").getJSONArray("P18")
        if (pictures.length() == 0)
            return
        val imageName = pictures
                .getJSONObject(0)
                .getJSONObject("mainsnak")
                .getJSONObject("datavalue")
                .getString("value")
                .replace(' ', '_')

        if (imageName.isEmpty())
            return

        val md5Digest = md5Instance.digest(imageName.toByteArray())
        val md5FirstByte = String.format("%02x", md5Digest[0]).toLowerCase()

        val url = "https://upload.wikimedia.org/wikipedia/commons/" +
                "${md5FirstByte[0]}/$md5FirstByte/$imageName"

        val imageView = bmImage.get()
        if (imageView != null) {
            DownloadImageToImageViewAsyncTask(imageView).execute(url)
        }
    }
}

