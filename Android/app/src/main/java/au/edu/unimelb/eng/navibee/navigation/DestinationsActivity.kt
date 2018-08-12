package au.edu.unimelb.eng.navibee.navigation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
import au.edu.unimelb.eng.navibee.R
import java.net.URL

class DestinationsActivity : AppCompatActivity() {

    // Recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destinations)
        handleIntent(intent)

        val destinations = ArrayList<DestinationRVItem>()
        destinations.add(DestinationRVButton("Say a place", R.drawable.ic_keyboard_voice_black_24dp, View.OnClickListener {
            Log.d(javaClass.simpleName, "Clicked say a place.")
        }))
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
            Log.d(javaClass.simpleName, "Handling intent on search query $query.")
            // TODO: handle search query
        }
    }

}
