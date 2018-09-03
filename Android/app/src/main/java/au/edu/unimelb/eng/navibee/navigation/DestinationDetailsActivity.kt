package au.edu.unimelb.eng.navibee.navigation

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import au.edu.unimelb.eng.navibee.R
import au.edu.unimelb.eng.navibee.utils.*
import com.google.android.gms.location.places.*
import com.google.android.gms.location.places.internal.zzaq
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_destination_details.*
import kotlinx.android.synthetic.main.alert_dialog_navigation_choose_transport_manners.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.startActivity
import java.io.File
import java.io.FileOutputStream

val PlacePhotoMetadata.photoReference: String
    get() {
        return if (this is zzaq)
            zzah()
        else
            (freeze() as zzaq).zzah()
    }

/**
 * Show details of a destination from Google Maps
 *
 * Required extra information:
 *      DestinationDetailsActivity.EXTRA_PLACE_ID:
 *          String, the place ID as specified by Google Maps
 */
class DestinationDetailsActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PLACE_ID = "placeId"
    }

    // Recycler view
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var viewAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>
    private lateinit var viewManager: androidx.recyclerview.widget.RecyclerView.LayoutManager
    private val listItems = ArrayList<SimpleRecyclerViewItem>()

    private lateinit var geoDataClient: GeoDataClient

    private lateinit var place: Place

    private var titleRowHeight = -1

    private val attributions: ArrayList<CharSequence> = ArrayList()

    private val primaryColor: Int by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.colorPrimary, null)
        } else {
            resources.getColor(R.color.colorPrimary)
        }
    }


    // Image list
    private var photoMetadata: PlacePhotoMetadataBuffer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS and
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        setContentView(R.layout.activity_destination_details)

        // Action bar
        setSupportActionBar(navigation_destinations_details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // set padding for status bar
        navigation_destinations_details_toolbar_padding.apply {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                setOnApplyWindowInsetsListener { view, insets ->
                    view.apply {
                        val lp = layoutParams
                        lp.height = insets.systemWindowInsetTop
                        layoutParams = lp
                    }
                    insets
                }
            } else {
                val lp = layoutParams
                lp.height = getStatusBarHeight(this)
                layoutParams = lp

            }
        }



        // Remove redundant shadow in transparent app bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            navigation_destinations_details_appbar.outlineProvider = null
        }

        // Setup data for loading screen
        supportActionBar?.title = resources.getString(R.string.loading)
        navigation_destinations_details_toolbar.setTitleTextColor(0)
        listItems.add(SimpleRVIndefiniteProgressBar())

        // Recycler View
        viewManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        viewAdapter = SimpleRecyclerViewAdaptor(listItems)

        recyclerView = navigation_destinations_details_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        }

        // Carousel view
        navigation_destinations_details_image_preview.pageCount = 1
        navigation_destinations_details_image_preview.setImageListener { position, imageView ->
            photoMetadata?.let { pm ->
                val photo = pm[position]
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageDrawable(resources.getDrawable(R.drawable.navibee_placeholder, null))
                } else {
                    imageView.setImageDrawable(resources.getDrawable(R.drawable.navibee_placeholder))
                }
                object : ImageViewCacheLoader(imageView) {
                    override val defaultKey = photo.photoReference



                    override fun loadTask(file: File) {
                        geoDataClient.getPhoto(pm[position]).addOnSuccessListener { data ->
                            val outputStream = FileOutputStream(file)
                            data.bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                            outputStream.close()
                            postLoad(file)
                        }
                    }
                }.execute()
            }
        }

        // Layout adjustment
        val bottomSheetBehavior =
                BottomSheetBehavior.from(recyclerView)
        bottomSheetBehavior.peekHeight = (resources.displayMetrics.heightPixels * 0.618).toInt()
        recyclerView.minimumHeight = (resources.displayMetrics.heightPixels * 0.618).toInt()
        navigation_destinations_details_image_preview.apply {
            val param = layoutParams
            param.height = (resources.displayMetrics.heightPixels * 0.380).toInt()
            layoutParams = param
        }

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, offset: Float) {
                if (listItems.size > 0 && listItems[0] is SimpleRVTextPrimarySecondaryStatic) {
                    val item = viewManager.findViewByPosition(0) ?: return
                    setViewHeightPercent(item, 1 - offset,
                            supportActionBar?.height!!, titleRowHeight)
                    navigation_destinations_details_toolbar
                            .setTitleTextColor(colorRGBA(0, 0, 0, offset))
                    navigation_destinations_details_toolbar
                            .setBackgroundColor(colorA(primaryColor, offset))
                    navigation_destinations_details_toolbar_padding
                            .setBackgroundColor(colorA(primaryColor, offset))
                    navigation_destinations_details_fab_text.scaleX = 1 - offset
                    navigation_destinations_details_fab_text.scaleY = 1 - offset
                }
            }

            override fun onStateChanged(bottomSheet: View, state: Int) {
                if (listItems.size > 0 && listItems[0] is SimpleRVTextPrimarySecondaryStatic)
                    if (state == BottomSheetBehavior.STATE_DRAGGING && titleRowHeight == -1) {
                        titleRowHeight = viewManager.findViewByPosition(0)?.height ?: -1
                    }
            }

        })

        val placeId: String = intent.getStringExtra(EXTRA_PLACE_ID) ?: return finish()

        geoDataClient = Places.getGeoDataClient(this)

        launch(UI) {
            val photoTask = async { loadImage(placeId) }
            val placeTask = async { loadDetails(placeId) }

            val photo = photoTask.await()
            val place = placeTask.await()

            supportActionBar?.title = place?.name
            navigation_destinations_details_image_preview.pageCount = photo?.count ?: 0
            updateAttributionsRow(attributions)
            viewAdapter.notifyDataSetChanged()

            addRecentSearchQuery(applicationContext, LocationSearchHistory(
                    googlePlaceId = placeId,
                    name = place?.name ?: "",
                    address = place?.address ?: "",
                    photoReference = photo?.get(0)?.photoReference
            ))
        }


    }

    private fun updateAttributionsRow(attributions: ArrayList<CharSequence>) {
        val attrHTML = resources.getString(R.string.search_result_attributions) +
                "<br>" +
                attributions.joinToString(", ")
        val formattedHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(attrHTML, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(attrHTML)
        }
        if (listItems[listItems.size - 1] is SimpleRVAttributions) {
            listItems[listItems.size - 1] = SimpleRVAttributions(formattedHtml)
            viewAdapter.notifyItemChanged(listItems.size - 1)
        } else {
            listItems.add(SimpleRVAttributions(formattedHtml))
            viewAdapter.notifyItemInserted(listItems.size - 1)
        }
    }

    private fun setViewHeightPercent(view: View, percentage: Float, min: Int, max: Int) {
        val height = (min + (max - min) * percentage).toInt()
        val params = view.layoutParams
        params.height = height
        view.layoutParams = params
    }

    private fun colorA(color: Int, alpha: Float) =
            colorRGBA(Color.red(color), Color.green(color), Color.blue(color), alpha)


    private fun colorRGBA(red: Int, green: Int, blue: Int, alpha: Float): Int {
        return (alpha * 255).toInt() shl 24 or (red shl 16) or (green shl 8) or blue
    }

    fun onClickGo(view: View) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog_navigation_choose_transport_manners,
                null)
        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.alert_dialog_title_select_method_of_travel)
                .setView(dialogView)
                .setNegativeButton(R.string.button_cancel) { dialog, which ->
                    if (which == DialogInterface.BUTTON_NEGATIVE)
                        dialog.dismiss()
                }
                .create()
        dialogView.navigation_directions_transport_manners_dialog_walk.setOnClickListener {
            startActivity<NavigationActivity>(
                    NavigationActivity.EXTRA_DEST_LAT to place.latLng.latitude,
                    NavigationActivity.EXTRA_DEST_LON to place.latLng.longitude,
                    NavigationActivity.EXTRA_MEAN_OF_TRAVEL to NavigationActivity.MEAN_WALKING
            )
            dialog.dismiss()
        }
        dialogView.navigation_directions_transport_manners_dialog_transit.setOnClickListener {
            dialog.dismiss()
        }
        dialogView.navigation_directions_transport_manners_dialog_drive.setOnClickListener {
            startActivity<NavigationActivity>(
                    NavigationActivity.EXTRA_DEST_LAT to place.latLng.latitude,
                    NavigationActivity.EXTRA_DEST_LON to place.latLng.longitude,
                    NavigationActivity.EXTRA_MEAN_OF_TRAVEL to NavigationActivity.MEAN_DRIVING
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun loadImage(placeId: String): PlacePhotoMetadataBuffer? {
        val data = geoDataClient.getPlacePhotos(placeId).run {
            Tasks.await(this)
            this.result
        }.photoMetadata ?: return null

        if (data.count > 0) {
            photoMetadata = data
            for (i in photoMetadata!!) {
                if (!i.attributions.isNullOrBlank())
                    attributions.add(i.attributions)
            }
        }

        return data
    }

    private fun loadDetails(placeId: String): Place? {
        val place = geoDataClient.getPlaceById(placeId).run {
            Tasks.await(this)
            this.result
        }[0] ?: return null

        listItems.clear()
        listItems.add(
                SimpleRVTextPrimarySecondaryStatic(
                        primary = place.name,
                        secondary = place.placeTypes.joinToString(", ") { i ->
                            googlePlaceTypeIDToString(i, resources)
                        }
                )
        )
        if (place.address != null)
            listItems.add(
                    SimpleRVTextSecondaryPrimaryStatic(
                            secondary = resources.getString(R.string.place_details_address),
                            primary = place.address ?: ""
                    )
            )
        if (!place.phoneNumber.isNullOrBlank())
            listItems.add(
                    SimpleRVTextSecondaryPrimaryClickable(
                            secondary = resources.getString(R.string.place_details_phone_number),
                            primary = place.phoneNumber ?: "",
                            onClick = View.OnClickListener {
                                startActivity(Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${place.phoneNumber}")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }
                    )
            )
        if (place.rating >= 0)
            listItems.add(
                    SimpleRVTextSecondaryPrimaryStatic(
                            secondary = resources.getString(R.string.place_details_ratings),
                            primary = "%.1f/5.0".format(place.rating)
                    )
            )
        if (place.websiteUri != null)
            listItems.add(SimpleRVTextSecondaryPrimaryClickable(
                    secondary = resources.getString(R.string.place_details_website),
                    primary = "${place.websiteUri}",
                    onClick = View.OnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW).apply {
                            data = place.websiteUri
                        })
                    }
            ))
        if (!place.attributions.isNullOrBlank()) {
            attributions.add(place.attributions!!)
        }

        return place
    }
}
