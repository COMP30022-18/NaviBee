package au.edu.unimelb.eng.navibee.navigation

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.here.android.mpa.common.MapEngine
import com.here.android.mpa.routing.CoreRouter
import com.here.android.mpa.routing.Route
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.textView


class TransitNavigationActivity : AppCompatActivity() {

    private lateinit var viewModel: TransitNavigationViewModel
    private lateinit var debugTextView: TextView

    private var listItems = mutableListOf<TransitRouteRVData>()
    private var renderListItems = mutableListOf<TransitRouteRVData>()

    private var origin = "Demo origin"
    private var destination = "Demo destination"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_transit_navigation)
//        val cv = contentView as ConstraintLayout
//        debugTextView = TextView(this)
//        debugTextView.layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
//        cv.addView(debugTextView, 0)

        scrollView {
            debugTextView = textView("Loading...") {}
        }

        viewModel = ViewModelProviders.of(this).get(TransitNavigationViewModel::class.java)
//
        viewModel.debugText.observe(this, Observer {
            debugTextView.text = it
        })

        viewModel.getRoute()
    }

    private val routeObserver = Observer<Route> {
        renderListItems.clear()
        renderListItems.add(TransitRouteRVOrigin(name = origin))
        renderListItems.add(TransitRouteRVDestination(name = destination))

    }
}

private class TransitNavigationViewModel(private val context: Application):
        AndroidViewModel(context) {

    private val mapEngine = MapEngine.getInstance()
    private lateinit var router: CoreRouter

    val debugText = MutableLiveData<String>()
    val routeInfo = MutableLiveData<Response?>()
    val viewData = MutableLiveData<List<TransitRouteRVData>>()

    fun getRoute() {
        val originLat = -37.799149f
        val originLon = 144.994426f
        val destLat = -37.83640f
        val destLon = 144.92214f

        launch {
            getTransitDirections(originLat, originLon, destLat, destLon).let {
                routeInfo.postValue(it)
                val viewList = mutableListOf<TransitRouteRVData>()
                it?.res?.connections?.connections?.let { connections ->
                    for (con in connections) {
                    }
                }
            }

        }
    }

}

private class TransitRouteRVAdapter(private val listEntries: MutableList<TransitRouteRVData>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = 0
        return TransitNavigationRVHolder(LayoutInflater.from(parent.context)
                .inflate(layout, parent, false))
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

private abstract class TransitRouteRVData
private data class TransitRouteRVOrigin(val name: String): TransitRouteRVData()
private data class TransitRouteRVWalking(val time: Long): TransitRouteRVData()
private data class TransitRouteRVOriginStation(
        val stop: String, val route: String,
        val startingStation: String, val terminusStation: String,
        val color: Int
): TransitRouteRVData()
private data class TransitRouteRVToggle(
        val stops: Int, val duration: Long,
        val color: Int,
        val onClick: View.OnClickListener,
        val intermediateStations: List<TransitRouteRVIntermediateStation>,
        val status: Boolean
): TransitRouteRVData()
private data class TransitRouteRVIntermediateStation(
        val name: String,
        val color: Int
): TransitRouteRVData()
private data class TransitRouteRVDestinationStation(
        val name: String,
        val color: Int
): TransitRouteRVData()
private data class TransitRouteRVDestination(val name: String): TransitRouteRVData()
private class TransitRouteRVDivider: TransitRouteRVData()

private class TransitNavigationRVHolder(itemView: View):
        RecyclerView.ViewHolder(itemView)