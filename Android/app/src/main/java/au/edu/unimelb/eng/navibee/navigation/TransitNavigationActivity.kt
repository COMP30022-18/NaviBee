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
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.MapRoute
import com.here.android.mpa.routing.*
import org.jetbrains.anko.scrollView
import org.jetbrains.anko.textView
import timber.log.Timber
import java.io.File


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

class TransitNavigationViewModel(private val context: Application):
        AndroidViewModel(context) {

    private val mapEngine = MapEngine.getInstance()
    private lateinit var router: CoreRouter

    val debugText = MutableLiveData<String>()
    val routeInfo = MutableLiveData<Route>()

    init {
        val appContext = ApplicationContext(context)
        if (!MapEngine.isInitialized()) {
            MapSettings.setIsolatedDiskCacheRootPath(
                context.cacheDir.absolutePath + File.separator + ".here-maps",
                "au.edu.unimelb.eng.navibee.hereService"
            )
            mapEngine.init(appContext) { error ->
                if (error != OnEngineInitListener.Error.NONE)
                    Timber.e(error.throwable, "Error while init here map engine: $error, ${error.details}")
                else {
                    router = CoreRouter()
                    getRoute()
                }
            }
        }

    }

    fun getRoute() {
        if (MapEngine.isInitialized()) {
            router.calculateRoute(
                    RoutePlan().apply {
                        routeOptions = RouteOptions().apply {
                            transportMode = RouteOptions.TransportMode.PUBLIC_TRANSPORT
                            routeType = RouteOptions.Type.BALANCED
                        }

                        addWaypoint(RouteWaypoint(GeoCoordinate(-37.799149, 144.994426)))
                        addWaypoint(RouteWaypoint(GeoCoordinate(-37.83640, 144.92214)))
                    },
                    routeListener
            )
        }
    }

    private val routeListener = object: CoreRouter.Listener {
        override fun onCalculateRouteFinished(routeResult: MutableList<RouteResult>, error: RoutingError) {
            if (error == RoutingError.NONE) {
                val transitRoute = routeResult[0]
                Timber.v("Route elements: ${transitRoute.route.routeElements}")
                if (transitRoute.route != null) {
                    routeInfo.postValue(transitRoute.route)
                }
                val debugMessages = mutableListOf<String>()

                for (routeElement in transitRoute.route.routeElements.elements) {
                    if (routeElement.roadElement != null) {
                        val rel = routeElement.roadElement
                        debugMessages.add("Road: ${rel.roadName} (${rel.routeName})")
                    } else if (routeElement.transitElement != null) {
                        val tel = routeElement.transitElement
                        debugMessages.add("Transit: ${tel.departureStop.name} - ${tel.arrivalStop.name}\n" +
                                "    Time: ${tel.departureTime.time} - ${tel.arrivalTime.time}\n" +
                                "    Type/Duration: ${tel.transitType.name}/${tel.duration} (sys: ${tel.systemOfficialName}/${tel.systemInformalName}/${tel.systemShortName})\n" +
                                "    Line name: ${tel.lineName}")
                        if (tel.hasPrimaryLineColor())
                            debugMessages.add("   Primary color: ${tel.primaryLineColor}")

                        if (tel.hasSecondaryLineColor())
                            debugMessages.add("   Secondary color: ${tel.secondaryLineColor}")
                    }
                }

                debugMessages.add("-------")

                for (maneuver in transitRoute.route.maneuvers) {
                    debugMessages.add("Maneuver: ${maneuver.action.name} - ${maneuver.roadName} ${maneuver.roadNumber}\n" +
                            "    Next: ${maneuver.nextRoadName} #${maneuver.nextRoadNumber}\n" +
                            "    Signpost: ${maneuver.signpost?.exitText} #${maneuver.signpost?.exitNumber}\n" +
                            "    Turn/Action: ${maneuver.turn?.name}, ${maneuver.icon.name}")
                    for (subElem in maneuver.routeElements) {
                        if (subElem.roadElement != null) {
                            val rel = subElem.roadElement
                            debugMessages.add("    | Road: ${rel.roadName} (${rel.routeName})")
                        } else if (subElem.transitElement != null) {
                            val tel = subElem.transitElement
                            debugMessages.add("    | Transit: ${tel.departureStop.name} - ${tel.arrivalStop.name}\n" +
                                    "    |     Time: ${tel.departureTime.time} - ${tel.arrivalTime.time}\n" +
                                    "    |     Type/Duration: ${tel.transitType.name}/${tel.duration} (sys: ${tel.systemOfficialName}/${tel.systemInformalName}/${tel.systemShortName})\n" +
                                    "    |     Line name: ${tel.lineName}")
                            if (tel.hasPrimaryLineColor())
                                debugMessages.add("    |    Primary color: ${Integer.toHexString(tel.primaryLineColor)}")

                            if (tel.hasSecondaryLineColor())
                                debugMessages.add("    |    Secondary color: ${Integer.toHexString(tel.secondaryLineColor)}")
                        }
                    }
                }

                // Routing zones are not supported for PUBLIC_TRANSPORT routes
//                for (zone in transitRoute.route.routingZones) {
//                    debugMessages.add("Zone: ${zone.name} (${zone.type})")
//                }

                for (wayPoint in transitRoute.route.routeWaypoints) {
                    debugMessages.add("Way point: ${wayPoint.identifier} ${wayPoint.waypointDirection.name} ${wayPoint.waypointType.name}\n" +
                            "    Road side: ${wayPoint.roadInfo.roadSide.name}")
                }

                debugText.postValue(debugMessages.joinToString("\n"))

                val mapRoute = MapRoute(transitRoute.route)
                // map.addMapObject(mapRoute)

                val geoBoundingBox = transitRoute.route.boundingBox
                // map.zoomTo(geoBoundingBox, Map.Animation.NONE, Map.MOVE_PRESERVE_ORIENTATION)

            } else {
                Timber.e("Error occurred while retrieving route: $error")
            }
        }

        override fun onProgress(percentage: Int) {
            Timber.v("In progress of loading transit route $percentage")
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