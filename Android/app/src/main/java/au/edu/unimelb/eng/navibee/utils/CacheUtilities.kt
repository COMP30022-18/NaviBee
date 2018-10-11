package au.edu.unimelb.eng.navibee.utils

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import au.edu.unimelb.eng.navibee.NaviBeeApplication
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
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
                              val singleJob: Boolean = true,
                              private var roundImage: Boolean = false):
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

    fun roundImage(value: Boolean): ImageViewCacheLoader {
        this.roundImage = value
        return this
    }

    override fun postLoad(file: File) {
        try {
            val bitmap = BitmapFactory.decodeStream(FileInputStream(file))
            if (roundImage) {
                val r = imageView.resources
                val drawable = RoundedBitmapDrawableFactory.create(r, bitmap)
                drawable.setAntiAlias(true)
                drawable.isCircular = true
                launch(UI) {
                    imageView.visibility = View.VISIBLE
                    imageView.setImageDrawable(drawable)
                }
            } else {
                launch(UI) {
                    imageView.visibility = View.VISIBLE
                    imageView.setImageBitmap(bitmap)
                }
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
                              singleJob: Boolean = true,
                              roundImage: Boolean = false):
        ImageViewCacheLoader(iv, prefix, singleJob, roundImage) {
    override val defaultKey = url
    override fun loadTask(file: File) {
        try {
            val input = java.net.URL(url).openStream()
            val output = FileOutputStream(file)
            ByteStreams.copy(input, output)
            output.close()
            postLoad(file)
        } catch (e: Exception) {
            Timber.e(e, "Failed loading image from URL.")
        }
    }
}


class URLChipDrawableCacheLoader
@JvmOverloads constructor(private val url: String,
                          private val chipDrawable: ChipDrawable,
                          private val resources: Resources,
                          prefix: String = "image-url"):
        CachedLoader(prefix) {
    override val defaultKey = url

    override fun loadTask(file: File) {
        try {
            val input = java.net.URL(url).openStream()
            val output = FileOutputStream(file)
            ByteStreams.copy(input, output)
            output.close()
            postLoad(file)
        } catch (e: Exception) {
            Timber.e(e, "Failed loading image from URL.")
        }
    }

    override fun postLoad(file: File) {
        try {
            val drawable = RoundedBitmapDrawableFactory
                    .create(resources, FileInputStream(file))
            drawable.setAntiAlias(true)
            drawable.isCircular = true

            launch(UI) {
                chipDrawable.chipIcon = drawable
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed loading image to chip.")
        }
    }
}


class URLChipCacheLoader
@JvmOverloads constructor(private val url: String,
                          private val chip: Chip,
                          private val resources: Resources,
                          prefix: String = "image-url"):
        CachedLoader(prefix) {
    override val defaultKey = url

    override fun loadTask(file: File) {
        try {
            val input = java.net.URL(url).openStream()
            val output = FileOutputStream(file)
            ByteStreams.copy(input, output)
            output.close()
            postLoad(file)
        } catch (e: Exception) {
            Timber.e(e, "Failed loading image from URL.")
        }
    }

    override fun postLoad(file: File) {
        try {
            val drawable = RoundedBitmapDrawableFactory
                    .create(resources, FileInputStream(file))
            drawable.setAntiAlias(true)
            drawable.isCircular = true

            launch(UI) {
                chip.chipIcon = drawable
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed loading image to chip.")
        }
    }
}


class URLActionBarIconCacheLoader
@JvmOverloads constructor(private val url: String,
                          private val actionBar: ActionBar,
                          private val resources: Resources,
                          prefix: String = "image-url"):
        CachedLoader(prefix) {
    override val defaultKey = url

    override fun loadTask(file: File) {
        try {
            val input = java.net.URL(url).openStream()
            val output = FileOutputStream(file)
            ByteStreams.copy(input, output)
            output.close()
            postLoad(file)
        } catch (e: Exception) {
            Timber.e(e, "Failed loading image from URL.")
        }
    }

    override fun postLoad(file: File) {
        try {
            val drawable = RoundedBitmapDrawableFactory
                    .create(resources, FileInputStream(file))
            drawable.setAntiAlias(true)
            drawable.isCircular = true

            launch(UI) {
                actionBar.setIcon(drawable)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed loading image to action bar.")
        }
    }
}