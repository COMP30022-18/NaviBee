package au.edu.unimelb.eng.navibee.navigation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import au.edu.unimelb.eng.navibee.NaviBeeApplication
import au.edu.unimelb.eng.navibee.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_transit_navigation.*
import kotlinx.android.synthetic.main.recycler_view_transit_destination.view.*
import kotlinx.android.synthetic.main.recycler_view_transit_origin.view.*
import kotlinx.android.synthetic.main.recycler_view_transit_segment_destination.view.*
import kotlinx.android.synthetic.main.recycler_view_transit_segment_intermediate.view.*
import kotlinx.android.synthetic.main.recycler_view_transit_segment_origin.view.*
import kotlinx.android.synthetic.main.recycler_view_transit_segment_toggle.view.*
import kotlinx.android.synthetic.main.recycler_view_transit_walking.view.*
import kotlinx.coroutines.experimental.launch
import net.time4j.Duration
import net.time4j.IsoUnit
import net.time4j.PrettyTime
import net.time4j.format.TextWidth
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor
import java.util.*

class TransitNavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    // Recycler view
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var viewAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<*>
    private lateinit var viewManager: androidx.recyclerview.widget.RecyclerView.LayoutManager

    private lateinit var viewModel: TransitNavigationViewModel

    private var listItems = listOf<TransitRouteRVData>()
    private var renderListItems =
        mutableListOf<TransitRouteRVData>(TransitRouteRVIndefiniteProgress())

    private var originName = "Debug origin name"
    private var destinationName = "Debug destination name"

    private var defaultColor = 0
    private var defaultTextColor = 0
    private var walkingLineColor = 0

    private var googleMap: GoogleMap? = null

    private var originLat = -37.799149
    private var originLon = 144.994426
    private var destLat = -37.83640
    private var destLon = 144.92214

    private val stopMarkers = mutableListOf<Marker>()

    companion object {
        private val NULL_LISTENER = View.OnClickListener { }
        private val WALKING_PATTERN = listOf(Dot(), Gap(16f))

        private const val LINE_WIDTH = 24f

        private lateinit var MARKER_STOP: BitmapDescriptor
        private lateinit var MARKER_TERMINUS: BitmapDescriptor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transit_navigation)

        defaultColor = ContextCompat.getColor(this, R.color.colorPrimary)
        defaultTextColor = ContextCompat.getColor(this, R.color.colorLightTextPrimary)
        walkingLineColor = ContextCompat.getColor(this, R.color.transitWalkingColor)

        MARKER_STOP = BitmapDescriptorFactory.fromBitmap(
            AppCompatResources.getDrawable(this, R.drawable.ic_navigation_transit_stop_marker)?.toBitmap())
        MARKER_TERMINUS = BitmapDescriptorFactory.fromBitmap(
            AppCompatResources.getDrawable(this, R.drawable.ic_navigation_transit_terminus_marker)?.toBitmap())

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.navigation_destinations_search_result_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setupRecyclerView()

        viewModel = ViewModelProviders.of(this).get(TransitNavigationViewModel::class.java)

        subscribe()

        viewModel.getRoute()
    }

    private fun setupRecyclerView() {
        viewManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        viewAdapter = TransitRouteRVAdapter(renderListItems)

        recyclerView = navigation_transit_navigation_recycler_view.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun subscribe() {
        viewModel.routeInfo.observe(this, Observer {
            it?.res?.connections?.connections?.let { connections ->
                if (!connections.isEmpty()) {
                    listItems = buildRawViewList(connections[0])
                    renderListItems.clear()
                    renderListItems.addAll(listItems)
                    viewAdapter.notifyDataSetChanged()
                    renderMap()
                }
            }
        })
    }

    private fun buildRawViewList(con: Connection): List<TransitRouteRVData> {
        val viewList = mutableListOf<TransitRouteRVData>()

        val segmentSummaries = mutableListOf<TransitSegments>()

        // Origin
        originName =
            con.dep.address?.name
            ?: con.dep.station?.name
            ?: originName
        viewList.add(TransitRouteRVOrigin(name = originName))

        // Sections
        for (sec in con.sections.secs) {
            if (sec.mode == TransportMode.WALK) {
                // Walking
                viewList.add(TransitRouteRVWalking(sec.journey.duration))
                segmentSummaries.add(TransitSegments(
                    type = sec.mode,
                    duration = sec.journey.duration
                ))
            } else {
                // Transit
                if (viewList.last() is TransitRouteRVOrigin)
                    viewList.add(TransitRouteRVDivider())
                val routeColor = sec.dep.transport?.at?.color ?: defaultColor
                val textColor = sec.dep.transport?.at?.textColor ?: defaultTextColor

                val routes = {
                    val r = mutableListOf(sec.dep.transport?.name)
                    sec.dep.frequency?.altDeps?.asSequence()
                        ?.map { it.transport?.name }
                        ?.filterNotNull()
                        ?.let { r.addAll(it) }
                    r.asSequence().filterNotNull().distinct().toList()
                }()

                val terminals = {
                    val t = mutableListOf(sec.dep.transport?.dir)
                    sec.dep.frequency?.altDeps?.asSequence()
                        ?.map { it.transport?.dir }
                        ?.filterNotNull()
                        ?.let { t.addAll(it) }
                    t.asSequence().filterNotNull().distinct().toList()
                }()

                segmentSummaries.add(TransitSegments(
                    type = sec.mode,
                    duration = sec.journey.duration,
                    routes = routes,
                    color = routeColor,
                    textColor = textColor
                ))

                viewList.add(TransitRouteRVOriginStation(
                    stop = sec.dep.station?.name.toString(),
                    type = sec.mode,
                    color = routeColor,
                    foregroundColor = textColor,
                    routes = routes,
                    terminusStations = terminals
                ))

                if (sec.journey.stops?.size ?: 0 > 2) {
                    val intermediates =
                        sec.journey.stops?.asSequence()?.map {
                            TransitRouteRVIntermediateStation(
                                color = routeColor,
                                name = it.stn.name
                            )
                        }?.drop(1)?.toList()?.dropLast(1) ?: emptyList()

                    val toggle = TransitRouteRVToggle(
                        id = sec.id ?: UUID.randomUUID().toString(),
                        color = routeColor,
                        intermediateStations = intermediates,
                        duration = sec.journey.duration,
                        status = false,
                        onClick = NULL_LISTENER
                    )

                    toggle.onClick = getToggleListener(toggle)

                    viewList.add(toggle)
                }

                viewList.add(
                    TransitRouteRVDestinationStation(
                        color = routeColor,
                        name = sec.arr.stn?.name.toString()
                    )
                )
            }
        }

        // Destination
        if (viewList.last() !is TransitRouteRVWalking)
            viewList.add(TransitRouteRVDivider())

        destinationName = con.arr.addr?.name
            ?: con.arr.stn?.name
            ?: destinationName

        viewList.add(TransitRouteRVDestination(
            name = destinationName
        ))

        viewList.add(0, TransitRouteRVSummary(
            con.duration,
            segmentSummaries.toList()
        ))

        return viewList.toList()
    }

    private fun getToggleListener(data: TransitRouteRVToggle): View.OnClickListener {
        return View.OnClickListener { view ->
            data.status = !data.status
            val toggle = view.navigation_transit_navigation_rv_trip_segment_extend_toggle_toggle

            if (data.status) {
                toggle.imageResource = R.drawable.ic_expand_more_black_24dp
            } else {
                toggle.imageResource = R.drawable.ic_expand_less_black_24dp
            }

            val list = mutableListOf<TransitRouteRVData>()
            for (i in listItems)
                when (i) {
                    is TransitRouteRVToggle -> {
                        list.add(i.copy())
                        if (i.status) {
                            list.addAll(i.intermediateStations)
                        }
                    }
                    else -> {
                        list.add(i)
                    }
                }

            val diffResult = DiffUtil.calculateDiff(ToggleRVDiffCallback(renderListItems, list))
            renderListItems.clear()
            renderListItems.addAll(list)
            diffResult.dispatchUpdatesTo(viewAdapter)
        }
    }

    private fun renderMap() {
        googleMap?.let { googleMap ->
            stopMarkers.clear()

            val latLngBoundsBuilder = LatLngBounds.Builder()
            val data = viewModel.routeInfo.value
            val con = data?.res?.connections?.connections?.get(0) ?: return
            val walkingManeuverIds =
                con.sections.secs.asSequence()
                    .filter { it -> it.mode == TransportMode.WALK }
                    .filterNotNull()
                    .map { it -> it.id }.toHashSet()


            // Walking
            data.res.guidance?.maneuvers
                ?.filter { it.secIds.split(" ").any { id -> id in walkingManeuverIds } }
                ?.forEach { i ->
                i.maneuvers?.forEachIndexed { manIdx, coord ->
                    val line = PolylineOptions()
                        .pattern(WALKING_PATTERN)
                        .color(walkingLineColor)
                        .width(LINE_WIDTH)
                    coord.graph?.split(" ")?.forEachIndexed { idx, it ->
                        val latLng = it.split(",").map { value -> value.toDouble() }
                        val loc = LatLng(latLng[0], latLng[1])
                        line.add(loc)
                        latLngBoundsBuilder.include(loc)
                        if (manIdx == 0 && idx == 0) {
                            googleMap.addMarker(MarkerOptions()
                                .position(loc).anchor(0.5f, 0.5f).icon(MARKER_TERMINUS))
                        }
                    }
                    googleMap.addPolyline(line)
                }
            }

            // Transit
            for (i in con.sections.secs) {
                if (i.mode != TransportMode.WALK) {
                    val line = PolylineOptions().color(i.dep.transport?.at?.color
                        ?: defaultColor).width(LINE_WIDTH)
                    val lastStop = (i.journey.stops?.size ?: 0) - 1

                    i.journey.stops?.forEachIndexed { idx, stop ->
                        val loc = LatLng(stop.stn.y.toDouble(), stop.stn.x.toDouble())
                        line.add(loc)
                        latLngBoundsBuilder.include(loc)
                        if (idx == 0) {
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(loc)
                                    .icon(MARKER_TERMINUS)
                                    .position(loc)
                                    .anchor(0.5f, 0.5f))
                        } else if (idx < lastStop) {
                            val marker = googleMap.addMarker(
                                 MarkerOptions()
                                     .position(loc)
                                     .icon(MARKER_STOP)
                                     .position(loc)
                                     .anchor(0.5f, 0.5f))
                            stopMarkers.add(marker)
                        }
                    }
                    googleMap.addPolyline(line)
                }
            }

            val mapDestLat: Double =
                con.arr.addr?.y?.toDouble() ?:
                con.arr.stn?.y?.toDouble() ?:
                destLat
            val mapDestLon: Double =
                con.arr.addr?.x?.toDouble() ?:
                con.arr.stn?.x?.toDouble() ?:
                destLon
            googleMap.addMarker(MarkerOptions().position(LatLng(mapDestLat, mapDestLon)))

            googleMap.animateCamera(CameraUpdateFactory
                .newLatLngBounds(latLngBoundsBuilder.build(), 128))

            googleMap.setOnCameraIdleListener {
                stopMarkers.forEach {
                    it.isVisible = googleMap.cameraPosition.zoom > 14
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(originLat, originLon)))
        }
    }
}

private class ToggleRVDiffCallback(private val old: List<TransitRouteRVData>,
                                   private val new: List<TransitRouteRVData>): DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            if (old[oldItemPosition] is TransitRouteRVToggle)
                (old[oldItemPosition] as TransitRouteRVToggle).isSameItem(new[newItemPosition])
            else
                old[oldItemPosition] == new[newItemPosition]

    override fun getOldListSize() = old.size

    override fun getNewListSize() = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition] == new[newItemPosition]
}

private class TransitNavigationViewModel(context: Application):
        AndroidViewModel(context) {

    val routeInfo = MutableLiveData<Response?>()

    fun getRoute() {
        val originLat = -37.799149f
        val originLon = 144.994426f
        val destLat = -37.83640f
        val destLon = 144.92214f

        if (routeInfo.value == null)
            launch {
                getTransitDirections(originLat, originLon, destLat, destLon)?.let {
                    routeInfo.postValue(it)
                }
            }
    }

}

private class TransitRouteRVAdapter(private val listEntries: MutableList<TransitRouteRVData>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val prettyTime = PrettyTime.of(Locale.getDefault())

    override fun getItemViewType(position: Int) =
        when (listEntries[position]) {
            is TransitRouteRVOrigin -> 0
            is TransitRouteRVWalking -> 1
            is TransitRouteRVOriginStation -> 2
            is TransitRouteRVToggle -> 3
            is TransitRouteRVIntermediateStation -> 4
            is TransitRouteRVDestinationStation -> 5
            is TransitRouteRVDestination -> 6
            is TransitRouteRVDivider -> 7
            is TransitRouteRVIndefiniteProgress -> 8
            is TransitRouteRVSummary -> 9
            else -> -1
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = when (viewType) {
            0 -> R.layout.recycler_view_transit_origin
            1 -> R.layout.recycler_view_transit_walking
            2 -> R.layout.recycler_view_transit_segment_origin
            3 -> R.layout.recycler_view_transit_segment_toggle
            4 -> R.layout.recycler_view_transit_segment_intermediate
            5 -> R.layout.recycler_view_transit_segment_destination
            6 -> R.layout.recycler_view_transit_destination
            7 -> R.layout.recycler_view_transit_divider
            8 -> R.layout.recycler_view_indefinite_progress
            9 -> R.layout.recycler_view_transit_summary
            else -> 0
        }
        return TransitNavigationRVHolder(LayoutInflater.from(parent.context)
                .inflate(layout, parent, false))
    }

    override fun getItemCount(): Int = listEntries.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val resources = NaviBeeApplication.instance.resources
        val data = listEntries[position]
        val view = holder.itemView
        when (data) {
            is TransitRouteRVOrigin -> {
                view.navigation_transit_navigation_rv_origin_name.text = data.name
            }
            is TransitRouteRVWalking -> {
                val durationString = prettyTime.print(data.duration, TextWidth.ABBREVIATED)
                val walkingText = resources.getString(R.string.navigation_transit_walking, durationString)
                view.navigation_transit_navigation_rv_walking_text.text = walkingText
            }
            is TransitRouteRVOriginStation -> {
                ImageViewCompat.setImageTintList(view.navigation_transit_navigation_rv_trip_segment_origin_bar,
                    defaultTint(data.color))
                view.navigation_transit_navigation_rv_trip_segment_origin_name.text = data.stop
                view.navigation_transit_navigation_rv_trip_segment_origin_route.text = data.routes.joinToString(" / ")
                view.navigation_transit_navigation_rv_trip_segment_origin_route.textColor = data.foregroundColor
                view.navigation_transit_navigation_rv_trip_segment_origin_route.chipBackgroundColor =
                    defaultTint(data.color)
                view.navigation_transit_navigation_rv_trip_segment_origin_terminal.text =
                    data.terminusStations.joinToString(" / ")
                view.navigation_transit_navigation_rv_trip_segment_origin_icon.setImageResource(when (data.type) {
                    TransportMode.PRIVATE_BUS,
                    TransportMode.BUS_RAPID,
                    TransportMode.BUS -> R.drawable.ic_directions_bus_black80_24dp
                    TransportMode.MONORAIL -> R.drawable.ic_directions_railway_black80_24dp
                    TransportMode.HIGH_SPEED_TRAIN,
                    TransportMode.INTERCITY_TRAIN,
                    TransportMode.INTER_REGIONAL_TRAIN,
                    TransportMode.REGIONAL_TRAIN -> R.drawable.ic_train_black80_24dp
                    TransportMode.CITY_TRAIN,
                    TransportMode.LIGHT_RAIL -> R.drawable.ic_tram_black80_24dp
                    TransportMode.SUBWAY -> R.drawable.ic_directions_transit_black80_24dp
                    TransportMode.FERRY -> R.drawable.ic_directions_boat_black80_24dp
                    // TODO: Inclined, aerial, flight, walk
                    else -> R.drawable.ic_directions_transit_black80_24dp
                })
            }
            is TransitRouteRVToggle -> {
                val durationString = prettyTime.print(data.duration, TextWidth.ABBREVIATED)
                ImageViewCompat.setImageTintList(view.navigation_transit_navigation_rv_trip_segment_extend_toggle_bar,
                    defaultTint(data.color))
                view.navigation_transit_navigation_rv_trip_segment_extend_toggle_text.text =
                    resources.getQuantityString(R.plurals.navigation_transit_segment_length,
                        data.intermediateStations.size, data.intermediateStations.size, durationString)
                view.navigation_transit_navigation_rv_trip_segment_extend_toggle_clickable.setOnClickListener(data.onClick)
            }
            is TransitRouteRVIntermediateStation -> {
                ImageViewCompat.setImageTintList(view.navigation_transit_navigation_rv_trip_segment_intermediate_stop_bar,
                    defaultTint(data.color))
                view.navigation_transit_navigation_rv_trip_segment_intermediate_stop_name.text = data.name
            }
            is TransitRouteRVDestinationStation -> {
                ImageViewCompat.setImageTintList(view.navigation_transit_navigation_rv_trip_segment_destination_bar,
                    defaultTint(data.color))
                view.navigation_transit_navigation_rv_trip_segment_destination_name.text = data.name
            }
            is TransitRouteRVDestination -> {
                view.navigation_transit_navigation_rv_destination_name.text = data.name
            }
            is TransitRouteRVSummary -> {
                // TODO: Render summary.
            }
        }
    }

    private fun defaultTint(color: Int) =
        ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(color))
}

private abstract class TransitRouteRVData
private data class TransitRouteRVOrigin(
    val name: String
): TransitRouteRVData()
private data class TransitRouteRVWalking(val duration: Duration<IsoUnit>): TransitRouteRVData()
private data class TransitRouteRVOriginStation(
    val stop: String,
    val routes: List<String>,
    val terminusStations: List<String>,
    val color: Int,
    val foregroundColor: Int,
    val type: TransportMode
): TransitRouteRVData()
private data class TransitRouteRVToggle(
    val id: String,
    val duration: Duration<IsoUnit>,
    val color: Int,
    val intermediateStations: List<TransitRouteRVIntermediateStation>,
    var status: Boolean,
    var onClick: View.OnClickListener
): TransitRouteRVData() {
    fun isSameItem(i: Any) =
        i is TransitRouteRVToggle &&
            id == i.id
}
private data class TransitRouteRVIntermediateStation(
    val name: String,
    val color: Int
): TransitRouteRVData()
private data class TransitRouteRVDestinationStation(
    val name: String,
    val color: Int
): TransitRouteRVData()
private data class TransitRouteRVSummary(
    val duration: Duration<IsoUnit>,
    val segments: List<TransitSegments>
): TransitRouteRVData()
private data class TransitSegments(
    val type: TransportMode,
    val duration: Duration<IsoUnit>,
    val routes: List<String> = listOf(),
    val color: Int = 0,
    val textColor: Int = 0
)
private data class TransitRouteRVDestination(val name: String): TransitRouteRVData()
private class TransitRouteRVDivider: TransitRouteRVData()
private class TransitRouteRVIndefiniteProgress: TransitRouteRVData()

private class TransitNavigationRVHolder(itemView: View):
        RecyclerView.ViewHolder(itemView)
