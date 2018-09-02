package au.edu.unimelb.eng.navibee.navigation

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import au.edu.unimelb.eng.navibee.R
import kotlinx.android.synthetic.main.activity_destinations.*
import org.jetbrains.anko.startActivity


class DestinationsActivity : AppCompatActivity(){

    // Recycler view
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var viewAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>

    private lateinit var viewManager: androidx.recyclerview.widget.RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destinations)

        // TODO: Populate the list of destinations with real data
        val destinations = ArrayList<DestinationRVItem>()
        destinations.add(DestinationRVButton("Say a place",
                R.drawable.ic_keyboard_voice_black_24dp,
                View.OnClickListener {
                    startVoiceSearch()
                })
        )
        getRecentSearchQueries().run {
            if (this.isNotEmpty())
                destinations.add(DestinationRVDivider("Recent destinations"))
            for (i in this) {
                destinations.add(DestinationRVEntry(
                        name = i.name,
                        location = i.address,
                        googlePhotoReference = i.photoReference,
                        onClick = View.OnClickListener {
                            startActivity<DestinationDetailsActivity>(
                                    DestinationDetailsActivity.EXTRA_PLACE_ID to i.googlePlaceId
                            )
                        }
                ))
            }
        }
        destinations.add(DestinationRVDivider("Recommended place"))
        destinations.add(DestinationRVEntry("Place 3", "Location 3",
                onClick = View.OnClickListener {  }))
        destinations.add(DestinationRVEntry("Place 4", "Location 4",
                onClick = View.OnClickListener {  }))

        // setup recycler view
        viewManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        viewAdapter = DestinationsRVAdaptor(destinations)

        recyclerView = nav_dest_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        }

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
        }
        return true
    }

    private fun startVoiceSearch() {
        startActivity<DestinationsVoiceSearchActivity>()
    }

}


