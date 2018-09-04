package au.edu.unimelb.eng.navibee.navigation

import android.app.Application
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
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import au.edu.unimelb.eng.navibee.R
import au.edu.unimelb.eng.navibee.utils.*
import com.google.android.gms.location.places.*
import com.google.android.gms.location.places.internal.zzaq
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_destination_details.*
import kotlinx.android.synthetic.main.alert_dialog_navigation_choose_transport_manners.view.*
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
    private val listItems = mutableListOf<SimpleRecyclerViewItem>()

    private lateinit var placeId: String

    private lateinit var viewModel: DestinationDetailsViewModel

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

        placeId = intent.getStringExtra(EXTRA_PLACE_ID) ?: return finish()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS and
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        setContentView(R.layout.activity_destination_details)

        // Setup view model
        viewModel = ViewModelProviders.of(this)
                .get(DestinationDetailsViewModel::class.java)

        subscribe()

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setImageDrawable(resources.getDrawable(R.drawable.navibee_placeholder, null))
            } else {
                imageView.setImageDrawable(resources.getDrawable(R.drawable.navibee_placeholder))
            }

            viewModel.loadImageWithCache(position, imageView)
        }

        layoutAdjustment()

        viewModel.loadDetails(placeId)
        viewModel.loadImageList(placeId)

    }

    private fun subscribe() {
        viewModel.placeDetails.observe(this, Observer {
            if (it.status == Resource.Status.SUCCESS)
                it.data?.get(0)?.run {
                    renderPlaceDetails(this)
                    viewAdapter.notifyDataSetChanged()

                    addRecentSearchQuery(applicationContext, LocationSearchHistory(
                            googlePlaceId = id,
                            name = name ?: "",
                            address = address ?: ""
                    ))
                }
        } )

        viewModel.placePhotos.observe(this, Observer {
            if (it.status == Resource.Status.SUCCESS)
                it.data?.run {
                    navigation_destinations_details_image_preview.pageCount = count
                }
        } )

        viewModel.attributions.observe(this, Observer {
            updateAttributionsRow(it)
        })
    }

    private fun layoutAdjustment() {
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
    }

    private fun updateAttributionsRow(attributions: List<CharSequence>) {
        if (attributions.isEmpty()) {
            if (listItems.last() is SimpleRVAttributions) {
                listItems.removeAt(listItems.lastIndex)
                viewAdapter.notifyItemRemoved(listItems.size)
            }
            return
        }
        val attrHTML = resources.getString(R.string.search_result_attributions) +
                "<br>" +
                attributions.joinToString(", ")
        val formattedHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(attrHTML, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(attrHTML)
        }
        if (listItems.last() is SimpleRVAttributions) {
            listItems[listItems.lastIndex] = SimpleRVAttributions(formattedHtml)
            viewAdapter.notifyItemChanged(listItems.lastIndex)
        } else {
            listItems.add(SimpleRVAttributions(formattedHtml))
            viewAdapter.notifyItemInserted(listItems.lastIndex)
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
            viewModel.placeDetails.value?.data?.get(0)?.also { place ->
                startActivity<NavigationActivity>(
                        NavigationActivity.EXTRA_DEST_LAT to place.latLng.latitude,
                        NavigationActivity.EXTRA_DEST_LON to place.latLng.longitude,
                        NavigationActivity.EXTRA_MEAN_OF_TRAVEL to NavigationActivity.MEAN_WALKING
                )
            }
            dialog.dismiss()
        }
        dialogView.navigation_directions_transport_manners_dialog_transit.setOnClickListener {
            dialog.dismiss()
        }
        dialogView.navigation_directions_transport_manners_dialog_drive.setOnClickListener {
            viewModel.placeDetails.value?.data?.get(0)?.also { place ->
                startActivity<NavigationActivity>(
                        NavigationActivity.EXTRA_DEST_LAT to place.latLng.latitude,
                        NavigationActivity.EXTRA_DEST_LON to place.latLng.longitude,
                        NavigationActivity.EXTRA_MEAN_OF_TRAVEL to NavigationActivity.MEAN_DRIVING
                )
            }
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun renderPlaceDetails(place: Place) {
        supportActionBar?.title = place.name

        var attributions: SimpleRVAttributions? = null

        if (listItems.isNotEmpty() && listItems.last() is SimpleRVAttributions) {
            attributions = listItems.last() as SimpleRVAttributions
        }
        
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
        if (attributions != null)
            listItems.add(attributions)
    }
}

private class DestinationDetailsViewModel(private val context: Application):
        AndroidViewModel(context) {

    val placeDetails = MutableLiveData<Resource<PlaceBufferResponse>>()
    val placePhotos = MutableLiveData<Resource<PlacePhotoMetadataBuffer>>()
    val attributions = MediatorLiveData<List<CharSequence>>()

    private val attributionsObserver = Observer<Any> {
        val l = mutableListOf<CharSequence>()
        placeDetails.value?.run {
            if (status == Resource.Status.SUCCESS &&
                    !data?.attributions.isNullOrBlank())
                l.add(data?.attributions!!)
            data?.mapNotNull { i -> i.attributions }
                    ?.forEach { i -> if (!i.isBlank()) l.add(i) }
        }
        placePhotos.value?.run {
            if (status == Resource.Status.SUCCESS) {
                data?.mapNotNull { i -> i.attributions }
                    ?.forEach { i -> if (!i.isBlank()) l.add(i) }
            }
        }
        attributions.value = l.toList()
    }

    init {
        attributions.addSource(placeDetails, attributionsObserver)
        attributions.addSource(placePhotos, attributionsObserver)
    }

    private var geoDataClient = Places.getGeoDataClient(context)

    fun loadImageList(placeId: String) {
        if (placePhotos.value != null) return
        geoDataClient.getPlacePhotos(placeId).addOnSuccessListener {
            placePhotos.postValue(Resource.success(it.photoMetadata))
        }.addOnFailureListener {
            placePhotos.postValue(Resource.error(it))
        }
    }

    fun loadDetails(placeId: String) {
        if (placeDetails.value != null) return
        geoDataClient.getPlaceById(placeId).addOnSuccessListener {
            placeDetails.postValue(Resource.success(it))
        }.addOnFailureListener {
            placeDetails.postValue(Resource.error(it))
        }
    }

    fun loadImageWithCache(position: Int, imageView: ImageView) {
        val resources = context.resources

        placePhotos.value?.data?.let { pm ->
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
}