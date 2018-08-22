package au.edu.unimelb.eng.navibee.navigation

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.View
import au.edu.unimelb.eng.navibee.R
import kotlinx.android.synthetic.main.activity_destinations.*
import org.jetbrains.anko.startActivity


class DestinationsActivity : AppCompatActivity(){

    // Recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    private lateinit var viewManager: RecyclerView.LayoutManager

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
        destinations.add(DestinationRVDivider("Recent destinations"))
        destinations.add(DestinationRVEntry("Place 1", "Location 1",
                onClick = View.OnClickListener {  }))
        destinations.add(DestinationRVEntry("Place 2", "Location 2",
                onClick = View.OnClickListener {  }))
        destinations.add(DestinationRVDivider("Recommended place"))
        destinations.add(DestinationRVEntry("Place 3", "Location 3",
                onClick = View.OnClickListener {  }))
        destinations.add(DestinationRVEntry("Place 4", "Location 4",
                onClick = View.OnClickListener {  }))

        // setup recycler view
        viewManager = LinearLayoutManager(this)
        viewAdapter = DestinationsRVAdaptor(destinations)

        recyclerView = nav_dest_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
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


