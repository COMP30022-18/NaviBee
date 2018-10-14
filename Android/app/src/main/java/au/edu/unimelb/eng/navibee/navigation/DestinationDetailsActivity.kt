package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import au.edu.unimelb.eng.navibee.R
import au.edu.unimelb.eng.navibee.utils.*
import com.google.android.gms.location.places.*
import com.google.android.gms.location.places.internal.zzaq
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_destination_details.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
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
class DestinationDetailsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        const val EXTRA_PLACE_ID = "placeId"
    }

    // Recycler view
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var viewAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>
    private lateinit var viewManager: androidx.recyclerview.widget.RecyclerView.LayoutManager
    private val listItems = mutableListOf<SimpleRecyclerViewItem>()

    private lateinit var placeId: String

    private var googleMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private var rawMapFragment: Fragment? = null

    private lateinit var viewModel: DestinationDetailsViewModel

    private var titleRowHeight = -1
    private var topObscureSize = 0

    private var collapsed = false

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val primaryColor: Int by lazy {
        ContextCompat.getColor(this, R.color.colorPrimary)
    }

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
                    view.updateLayoutParams{
                        height = insets.systemWindowInsetTop
                    }
                    insets
                }
            } else {
                val height = getStatusBarHeight(this)
                updateLayoutParams {
                    this.height = height
                }
            }
        }

        // Remove redundant shadow in transparent app bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            navigation_destinations_details_appbar.outlineProvider = null
        }

        // Setup data for loading screen
        navigation_destinations_details_toolbar
                .setBackgroundColor(0x6fffffff)
        navigation_destinations_details_toolbar_padding
                .setBackgroundColor(0x6fffffff)
        supportActionBar?.title = resources.getString(R.string.prompt_loading)
        navigation_destinations_details_toolbar.setTitleTextColor(0)
        supportActionBar?.subtitle = resources.getString(R.string.prompt_loading)
        navigation_destinations_details_toolbar.setSubtitleTextColor(0)
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
        navigation_destinations_details_image_preview.retrieveTopObscureHeight { _, height ->
            topObscureSize = height
        }

        // Carousel view
        navigation_destinations_details_image_preview.pageCount = 1
        navigation_destinations_details_image_preview.setImageListener { position, imageView ->
            imageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.navibee_placeholder))
            viewModel.loadImageWithCache(position, imageView)
        }

        layoutAdjustment()

        // Load map view
        rawMapFragment = supportFragmentManager
            .findFragmentById(R.id.navigation_destinations_details_map)
        mapFragment = rawMapFragment as SupportMapFragment?
        supportFragmentManager.beginTransaction().hide(rawMapFragment!!).commit()

        viewModel.loadDetails(placeId)
        viewModel.loadImageList(placeId)

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_destination_go, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_action_go)?.isVisible = collapsed
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_action_go -> {
                onClickGo(null)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun subscribe() {
        viewModel.placeDetails.observe(this, Observer {
            if (it.status == Resource.Status.SUCCESS)
                it.data?.get(0)?.run {
                    renderPlaceDetails(this)
                    viewAdapter.notifyDataSetChanged()
                    addRecentSearchQuery(applicationContext, LocationSearchHistory(
                            googlePlaceId = id,
                            name = name.toString(),
                            address = address.toString()
                    ))
                }
        } )

        viewModel.placePhotos.observe(this, Observer {
            if (it.status == Resource.Status.SUCCESS)
                it.data?.let { buffer ->
                    navigation_destinations_details_image_preview.pageCount = buffer.count
                    if (buffer.count == 0) {
                        supportFragmentManager.beginTransaction().show(rawMapFragment!!).commit()
                        mapFragment?.getMapAsync(this)
                    }
                }
        } )

        viewModel.attributions.observe(this, Observer {
            updateAttributionsRow(it)
        })
    }

    private fun layoutAdjustment() {
        bottomSheetBehavior = BottomSheetBehavior.from(recyclerView)
        bottomSheetBehavior.peekHeight = (resources.displayMetrics.heightPixels * 0.618).toInt()
        recyclerView.minimumHeight = (resources.displayMetrics.heightPixels * 0.618).toInt()
        navigation_destinations_details_image_preview.updateLayoutParams {
            height = (resources.displayMetrics.heightPixels * 0.380).toInt()
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
                            .setSubtitleTextColor(colorRGBA(0, 0, 0, offset * 0.75f))
                    // navigation_destinations_details_toolbar
                    //         .setBackgroundColor(colorA(primaryColor, offset * 0.75f + 0.25f))
                    // navigation_destinations_details_toolbar_padding
                    //         .setBackgroundColor(colorA(primaryColor, offset * 0.75f + 0.25f))

                    navigation_destinations_details_toolbar
                            .setBackgroundColor(
                                    colorInterlace(primaryColor, 0x6fffffff, offset)
                            )
                    navigation_destinations_details_toolbar_padding
                            .setBackgroundColor(
                                    colorInterlace(primaryColor, 0x6fffffff, offset)
                            )

                    navigation_destinations_details_fab_button.scaleX = 1 - offset
                    navigation_destinations_details_fab_button.scaleY = 1 - offset
                }

                if (offset > 0.8 && !collapsed) {
                    collapsed = true
                    invalidateOptionsMenu()
                } else if (offset <= 0.8 && collapsed) {
                    collapsed = false
                    invalidateOptionsMenu()
                }
            }

            override fun onStateChanged(bottomSheet: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_DRAGGING)
                    updateTitleRowHeight()
            }
        })

        recyclerView.viewTreeObserver.addOnDrawListener {
            updateTitleRowHeight()
        }
    }

    private fun updateTitleRowHeight() {
        if (viewManager.itemCount > 0 && listItems[0] is SimpleRVTextPrimarySecondaryStatic)
            if (titleRowHeight == -1) {
                titleRowHeight = viewManager.findViewByPosition(0)?.height ?: -1
            }
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
            @Suppress("DEPRECATION")
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
        view.updateLayoutParams {
            height = (min + (max - min) * percentage).toInt()
        }
    }

    private fun colorA(color: Int, alpha: Float) =
            colorRGBA(Color.red(color), Color.green(color), Color.blue(color), alpha)

    private fun colorInterlace(color0: Int, color1: Int, offset: Float): Int {
        return colorRGBA(
                clampOffset(Color.red(color0), Color.red(color1), offset),
                clampOffset(Color.green(color0), Color.green(color1), offset),
                clampOffset(Color.blue(color0), Color.blue(color1), offset),
                clampOffset(Color.alpha(color0), Color.alpha(color1), offset)
        )
    }

    private fun clampOffset(val0: Int, val1: Int, offset: Float): Int {
        return ((val0 - val1) * offset + val1).toInt()
    }

    private fun colorRGBA(red: Int, green: Int, blue: Int, alpha: Int) =
            alpha shl 24 or (red shl 16) or (green shl 8) or blue
    private fun colorRGBA(red: Int, green: Int, blue: Int, alpha: Float) =
        (alpha * 255).toInt() shl 24 or (red shl 16) or (green shl 8) or blue

    fun onClickGo(view: View?) {
        viewModel.placeDetails.value?.data?.get(0)?.run {
            startActivity<NavigationSelectorActivity>(
                NavigationSelectorActivity.EXTRA_LATITUDE to latLng.latitude,
                NavigationSelectorActivity.EXTRA_LONGITUDE to latLng.longitude,
                NavigationSelectorActivity.EXTRA_DESTINATION_NAME to name
            )
        }
    }

    private fun renderPlaceDetails(place: Place) {

        var attributions: SimpleRVAttributions? = null

        if (listItems.isNotEmpty() && listItems.last() is SimpleRVAttributions) {
            attributions = listItems.last() as SimpleRVAttributions
        }

        val placeTypes = place.placeTypes.joinToString(", ") { i ->
            googlePlaceTypeIDToString(i, resources)
        }

        supportActionBar?.apply{
            title = place.name
            subtitle = placeTypes
        }

        listItems.clear()
        listItems.add(
                SimpleRVTextPrimarySecondaryStatic(
                        primary = place.name,
                        secondary = placeTypes
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
                    SimpleRVRatings(
                            title = resources.getString(R.string.place_details_ratings),
                            rating = place.rating,
                            maxRating = 5,
                            step = 0.1f
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

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.setPadding(0, topObscureSize, 0, bottomSheetBehavior.peekHeight)
        googleMap.uiSettings.isMapToolbarEnabled = false

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED)
            googleMap.isMyLocationEnabled = true

            launch(UI) {
                viewModel.placeDetails.value?.data?.get(0)?.run {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(this.latLng))
                    googleMap.addMarker(MarkerOptions().position(this.latLng))
                    delay(100)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(this.latLng))
                }
            }
    }
}

private class DestinationDetailsViewModel(private val context: Application):
        AndroidViewModel(context) {

    var placeId: String = ""
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
        this.placeId = placeId
        if (placePhotos.value != null) return
        geoDataClient.getPlacePhotos(placeId).addOnSuccessListener {
            placePhotos.postValue(Resource.success(it.photoMetadata))
        }.addOnFailureListener {
            placePhotos.postValue(Resource.error(it))
        }
    }

    fun loadDetails(placeId: String) {
        this.placeId = placeId
        if (placeDetails.value != null) return
        geoDataClient.getPlaceById(placeId).addOnSuccessListener {
            placeDetails.postValue(Resource.success(it))
        }.addOnFailureListener {
            placeDetails.postValue(Resource.error(it))
        }
    }

    fun loadImageWithCache(position: Int, imageView: ImageView) {
        placePhotos.value?.data?.let { pm ->
            imageView.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.navibee_placeholder))
            object : ImageViewCacheLoader(imageView) {
                override val defaultKey = "$placeId-$position"

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