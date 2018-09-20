package au.edu.unimelb.eng.navibee.utils

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import au.edu.unimelb.eng.navibee.NaviBeeApplication
import com.google.common.io.ByteStreams
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * To implement this class:
 *     load picture to file in loadTask(file), when load is finished,
 *     call postLoad(file).
 *
 *     Set defaultKey to the default string used to hash for this file.
 *
 *     override prefix if you need a new prefix.
 *
 * When using:
 *     Create a new CachedLoader object and call execute() to start the task.
 *     Specify a key in execute if needed.
 */
abstract class CachedLoader(private val prefix: String = "general-cache") {
    abstract val defaultKey: String
    lateinit var job: Job

    @JvmOverloads
    open fun execute(key: String? = null) {
        job = launch {
            val k = key ?: defaultKey
            val file = File(
                NaviBeeApplication.instance.cacheDir,
                "$prefix-${sha256String(k)}"
            )

            if (file.exists()) {
                postLoad(file)
            } else {
                loadTask(file)
            }
        }
    }

    abstract fun postLoad(file: File)
    abstract fun loadTask(file: File)
}

abstract class ImageViewCacheLoader
    @JvmOverloads constructor(val imageView: ImageView,
                              prefix: String = "general-cache",
                              val singleJob: Boolean = true):
        CachedLoader(prefix) {
    private var tag: Any? = null

    override fun execute(key: String?) {
        if (singleJob) {
            if (imageView.tag is Job)
                (imageView.tag as Job).cancel()
            else
                tag = imageView.tag
        }
        super.execute(key)
        if (singleJob) {
            imageView.tag = job
        }
    }

    override fun postLoad(file: File) {
        try {
            imageView.visibility = View.VISIBLE
            val bitmap = BitmapFactory.decodeStream(FileInputStream(file))
            launch(UI) {
                imageView.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed loading image to ImageView.")
        }
        if (singleJob)
            imageView.tag = tag
    }
}

class URLImageViewCacheLoader
    @JvmOverloads constructor(private val url: String, iv: ImageView,
                              prefix: String = "image-url",
                              singleJob: Boolean = true):
        ImageViewCacheLoader(iv, prefix, singleJob) {
    override val defaultKey = url
    override fun loadTask(file: File) {
        try {
            val input = java.net.URL(url).openStream()
            val output = FileOutputStream(file)
            ByteStreams.copy(input, output)
            loadTask(file)
        } catch (e: Exception) {
            Timber.e(e, "Failed loading image from URL.")
        }
    }
}