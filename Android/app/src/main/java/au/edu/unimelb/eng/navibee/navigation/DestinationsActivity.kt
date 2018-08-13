package au.edu.unimelb.eng.navibee.navigation

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v4.app.DialogFragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import au.edu.unimelb.eng.navibee.BuildConfig
import au.edu.unimelb.eng.navibee.R
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.mapboxsdk.Mapbox
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber


class DestinationsActivity : AppCompatActivity(), SearchResultRetryListener {

    // Recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    // Voice recognition activity result ID
    val SPEECH_RECOGNITION_RESULT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destinations)
        handleIntent(intent)

        Timber.tag(javaClass.simpleName)

        val destinations = ArrayList<DestinationRVItem>()
        destinations.add(DestinationRVButton("Say a place",
                R.drawable.ic_keyboard_voice_black_24dp,
                View.OnClickListener {
                    startVoiceSearch()
                })
        )
        destinations.add(DestinationRVDivider("Recent destinations"))
        destinations.add(DestinationRVEntry("Place 1", "Location 1", "", View.OnClickListener {  }))
        destinations.add(DestinationRVEntry("Place 2", "Location 2", "", View.OnClickListener {  }))
        destinations.add(DestinationRVDivider("Recommended place"))
        destinations.add(DestinationRVEntry("Place 3", "Location 3", "", View.OnClickListener {  }))
        destinations.add(DestinationRVEntry("Place 4", "Location 4", "", View.OnClickListener {  }))

        // setup recycler view
        viewManager = LinearLayoutManager(this)
        viewAdapter = DestinationsRVAdaptor(destinations)  // TODO: write adaptor and dataset

        recyclerView = findViewById<RecyclerView>(R.id.nav_dest_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // Configure Mapbox SDK
        Mapbox.getInstance(this, BuildConfig.MAPBOX_API_TOKEN)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null)
            handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        this.menuInflater.inflate(R.menu.navigation_destinations_opiton_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        // Set up the search view
        (menu.findItem(R.id.nav_dest_optmnu_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))

            // Prevent the search view from collapsing within the subview
            setIconifiedByDefault(false)

            // Let the search view to fill the entire space
            maxWidth = Integer.MAX_VALUE

            setOnCloseListener {
                // TODO: Reset destination views
                false
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    return false // do the default
                }

                override fun onQueryTextChange(s: String): Boolean {
                    // NOTE: doing anything here is optional, onNewIntent is the important bit
                    if (s.length > 1) { // 2 chars or more
                        // TODO: filter/return results
                    } else if (s.isEmpty()) {
                        // TODO: reset the displayed data
                    }
                    return false
                }

            })


        }
        return true
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Timber.d("Handling intent on search query $query.")
            searchForLocation(query, false)
        }
    }

    private fun startVoiceSearch() {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).let {
            it.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            it.putExtra(RecognizerIntent.EXTRA_PROMPT, resources.getString(R.string.navigation_search_hint))
            startActivityForResult(it, SPEECH_RECOGNITION_RESULT)
        }
    }

    private fun searchForLocation(query: String, isVoice: Boolean) {
        val mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(Mapbox.getAccessToken()!!)
                .query(query)
                .build()

        mapboxGeocoding.enqueueCall(object : Callback<GeocodingResponse> {
            override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                val results = response.body()!!.features()
                if (results.size > 0) {
                    // Log the first results Point.
                    val firstResultPoint = results[0].center()
                    Timber.d("onResponse: $firstResultPoint (lat ${firstResultPoint!!.latitude()}, long ${firstResultPoint.longitude()})")

                    val bundle = Bundle()
                    bundle.putSerializable("location", results[0])
                    bundle.putBoolean("isVoice", isVoice)

                    // Popup to confirm the location (debug only)
                    SearchResultFragment().let {
                        it.arguments = bundle
                        it.show(supportFragmentManager, "tag")
                    }
                } else {
                    // No result for your request were found.
                    Timber.d("onResponse: No result found")
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, throwable: Throwable) {
                throwable.printStackTrace()
            }
        })
    }

    override fun onSearchResultRetry(dialog: DialogFragment) {
        // == true is to avoid getBoolean return null
        if (dialog.arguments?.getBoolean("isVoice") == true) {
            startVoiceSearch()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SPEECH_RECOGNITION_RESULT -> {
                if (resultCode == RESULT_OK) {
                    val results = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // TODO: Confirm for location.
                    searchForLocation(results[0], true)
                }
            }
        }
    }
}


interface SearchResultRetryListener {
    fun onSearchResultRetry(dialog: DialogFragment)
}

class SearchResultFragment: DialogFragment() {

    private lateinit var searchResultRetryListner: SearchResultRetryListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val location = this.arguments?.getSerializable("location") as CarmenFeature
        builder.let {
            it.setTitle(R.string.we_have_found)
            it.setMessage("${location.placeName()} (${location.text()}) at ${location.center()}")
            it.setPositiveButton(R.string.button_go) { dialog, id ->
                TODO("not implemented")
            }
            it.setNegativeButton(R.string.button_retry) { dialog, id ->
                this@SearchResultFragment.dialog.cancel()
                searchResultRetryListner.onSearchResultRetry(this)
            }
        }

        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            searchResultRetryListner = context as SearchResultRetryListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement SearchResultRetryListener")
        }
    }
}
