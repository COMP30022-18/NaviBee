package au.edu.unimelb.eng.navibee.navigation

import android.app.Application
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import au.edu.unimelb.eng.navibee.R
import kotlinx.android.synthetic.main.activity_destinations.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.startActivity




class DestinationsActivity : AppCompatActivity(){

    // Recycler view
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var viewAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>
    private lateinit var viewManager: androidx.recyclerview.widget.RecyclerView.LayoutManager

    private lateinit var viewModel: DestinationSuggestionModel

    private val destinations = ArrayList<DestinationRVItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destinations)

        viewModel = ViewModelProviders.of(this).get(DestinationSuggestionModel::class.java)
        lifecycle.addObserver(viewModel)

        subscribe()

        // setup recycler view
        viewManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        viewAdapter = DestinationsRVAdaptor(destinations)

        recyclerView = nav_dest_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
//            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        }

        updateRecyclerView()

//        viewModel.getDestinationSuggestions()

    }

    private fun subscribe() {
        viewModel.searchHistory.observe(this, Observer { updateRecyclerView() })
    }

    private fun updateRecyclerView() {
        launch(UI){
            destinations.clear()
            destinations.add(DestinationRVButton(resources.getString(R.string.action_navigation_say_a_place),
                    R.drawable.ic_keyboard_voice_black_24dp,
                    View.OnClickListener {
                        startVoiceSearch()
                    })
            )
            destinations.add(DestinationRVEntry("Transit navigation", "For debug use",
                    onClick = View.OnClickListener {
                        startActivity<TransitNavigationActivity>()
                    }))
            viewModel.searchHistory.value?.run {
                if (this.isNotEmpty())
                    destinations.add(DestinationRVDivider(resources.getString(R.string.navigation_destinations_header_recent_destinations)))
                for (i in this) {
                    destinations.add(DestinationRVEntry(
                            name = i.name,
                            location = i.address,
                            googlePlaceId = i.googlePlaceId,
                            onClick = View.OnClickListener {
                                startActivity<DestinationDetailsActivity>(
                                        DestinationDetailsActivity.EXTRA_PLACE_ID to i.googlePlaceId
                                )
                            }
                    ))
                }
            }

            // TODO: Populate the list of destination suggestions with real data
            destinations.add(DestinationRVDivider(resources.getString(R.string.navigation_destinations_header_recommended_places)))
            destinations.add(DestinationRVEntry("Place 3", "Location 3",
                    onClick = View.OnClickListener {  }))
            destinations.add(DestinationRVEntry("Place 4", "Location 4",
                    onClick = View.OnClickListener {  }))

            viewAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        this.menuInflater.inflate(R.menu.menu_navigation_destinations_opitons, menu)

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


private class DestinationSuggestionModel(private val context: Application):
        AndroidViewModel(context), LifecycleObserver {
    val searchHistory = MutableLiveData<List<LocationSearchHistory>>()

    fun getDestinationSuggestions() {
        if (searchHistory.value != null)
            searchHistory.postValue(getRecentSearchQueries(context))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        launch {
            searchHistory.postValue(getRecentSearchQueries(context))
        }
    }
}