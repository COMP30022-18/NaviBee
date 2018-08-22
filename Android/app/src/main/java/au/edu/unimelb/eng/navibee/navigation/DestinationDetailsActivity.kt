package au.edu.unimelb.eng.navibee.navigation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import au.edu.unimelb.eng.navibee.R
import au.edu.unimelb.eng.navibee.utils.*
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer
import com.google.android.gms.location.places.Places
import kotlinx.android.synthetic.main.activity_destination_details.*
import timber.log.Timber

class DestinationDetailsActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PLACE_ID = "placeId"
    }

    // Recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val listItems = ArrayList<SimpleRecyclerViewItem>()

    private lateinit var geoDataClient: GeoDataClient

    // Static text views
    private lateinit var primaryName: TextView
    private lateinit var secondaryName: TextView

    private lateinit var place: Place

    // Image list
    private var photoMetadata: PlacePhotoMetadataBuffer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination_details)

        // Setup text view variables
        primaryName = navigation_destinations_details_place_primary
        secondaryName = navigation_destinations_details_place_secondary

        // Action bar
        setSupportActionBar(navigation_destinations_details_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // Setup data for loading screen
        navigation_destinations_details_collapsing.apply {
            title = resources.getString(R.string.loading)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setExpandedTitleColor(resources.getColor(R.color.transparentBlack, null))
            } else {
                setExpandedTitleColor(resources.getColor(R.color.transparentBlack))
            }
        }
        listItems.add(SimpleRVIndefiniteProgressBar())
        primaryName.text = resources.getString(R.string.loading)
        secondaryName.text = resources.getString(R.string.loading)

        // Recycler View
        viewManager = LinearLayoutManager(this)
        viewAdapter = SimpleRecyclerViewAdaptor(listItems)

        recyclerView = navigation_destinations_details_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // Carousel view
        navigation_destinations_details_image_preview.pageCount = 1
        navigation_destinations_details_image_preview.setImageListener { position, imageView ->
            val pm = photoMetadata
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setImageDrawable(resources.getDrawable(R.drawable.navibee_placeholder, null))
            } else {
                imageView.setImageDrawable(resources.getDrawable(R.drawable.navibee_placeholder))
            }
            if (pm != null) {
                geoDataClient.getPhoto(pm[position]).addOnSuccessListener { data ->
                    imageView.setImageBitmap(data.bitmap)
                }
            }
        }

        val placeId: String = intent.getStringExtra(EXTRA_PLACE_ID) ?: return finish()

        Timber.v("Place ID retrieved: $placeId")

        geoDataClient = Places.getGeoDataClient(this)

        geoDataClient.getPlacePhotos(placeId).addOnSuccessListener { data ->
            if (data.photoMetadata.count > 0) {
                photoMetadata = data.photoMetadata
                navigation_destinations_details_image_preview.pageCount = data.photoMetadata.count
            }
        }

        geoDataClient.getPlaceById(placeId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                place = task.result[0]
                navigation_destinations_details_collapsing.title = place.name
                primaryName.text = place.name
                secondaryName.text = place.placeTypes.joinToString(", ") { i ->
                    googlePlaceTypeIDToString(i, resources)
                }
                listItems.clear()
                if (place.address != null)
                    listItems.add(
                            SimpleRVTextSecondaryPrimaryStatic(
                                    secondary = resources.getString(R.string.place_details_address),
                                    primary = place.address ?: ""
                            )
                    )
                if (place.phoneNumber != null)
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

            } else {
                listItems.clear()
            }
            viewAdapter.notifyDataSetChanged()
        }
    }
}
