package au.edu.unimelb.eng.navibee.navigation

import au.edu.unimelb.eng.navibee.BuildConfig
import com.google.gson.internal.bind.util.ISO8601Utils
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.time4j.Duration
import net.time4j.IsoUnit
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.text.ParsePosition
import java.util.*



private val moshiAdapter: JsonAdapter<Response> =
        Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(ColorAdapter())
                .add(HereRfc3339DateJsonAdapter())
                .add(ISO8601DurationAdapter())
                .add(TransportModeAdapter())
                .add(BooleanAdapter())
                .add(RPriceAdapter())
                .build().adapter(Response::class.java)

private val httpClient = OkHttpClient()

fun parseResponseJson(json: String?): Response? =
        if (json == null) null else moshiAdapter.fromJson(json)

fun getTransitDirections(originLat: Double, originLon: Double,
                        destLat: Double, destLon: Double,
                        time: Date = Date()): Response? {
    val dateString = ISO8601Utils.format(time).dropLast(1)
    val response = httpClient.newCall(
        Request.Builder()
            .url(
                HttpUrl.Builder()
                    .scheme("https")
                    .host("transit.api.here.com")
                    .encodedPath("/v3/route.json")
                    .addQueryParameter("app_id", BuildConfig.HERE_APP_ID)
                    .addQueryParameter("app_code", BuildConfig.HERE_APP_CODE)
                    .addQueryParameter("routing", "all")
                    .addQueryParameter("dep", "$originLat, $originLon")
                    .addQueryParameter("arr", "$destLat, $destLon")
                    .addQueryParameter("time", dateString)
                    .addQueryParameter("max", "1")
                    .addQueryParameter("maneuvers", "1")
                    .build()
            )
            .get()
            .build()
    ).execute()

    val json = response.body()?.string()

//    Timber.v("Response: $json")

    return parseResponseJson(json)
}

@JsonClass(generateAdapter = true)
data class Response(
    @Json(name = "Res")
    val res: Result
)

@JsonClass(generateAdapter = true)
data class Result(
    val serviceUrl: String?,
    @Json(name = "Message")
    val message: Message?,
    @Json(name = "NextDepartures")
    val nextDepartures: NextDepartures?,
    @Json(name = "Alerts")
    val alerts: Alerts?,
    @Json(name = "MultiNextDepartures")
    val multiNextDepartures: MultiNextDepartures?,
    @Json(name = "Connections")
    val connections: Connections?,
    @Json(name = "Guidance")
    val guidance: Guidance?,
    @Json(name = "Stations")
    val stations: Stations?,
    @Json(name = "Coverage")
    val coverage: Coverage?,
    @Json(name = "LocalCoverage")
    val localCoverage: LocalCoverage?,
    @Json(name = "Logos")
    val logos: Logos?,
    @Json(name = "Isochrone")
    val isochrone: Isochrone?,
    @Json(name = "LineInfos")
    val lineInfos: LineInfos?,
    @Json(name = "PathSegments")
    val pathSegments: PathSegments?
)

@JsonClass(generateAdapter = true)
data class Message(
    val code: String,
    val subcode: String?,
    val level: Level
)

@JsonClass(generateAdapter = true)
data class NextDepartures(
    @Json(name = "Dep")
    val dep: List<Dep>,
    @Json(name = "Operators")
    val operators: Operators?,
    @Json(name = "Attributions")
    val attributions: Attributions?
)

/**
 * Dep contains information about a departure and includes time,
 * station, address, platform and real time information if
 * available. The Transport attributes provide information
 * about the transport that need to be used for this departure.
 */
@JsonClass(generateAdapter = true)
data class Dep(
    val platform: String?,
    val time: Date,
    @Json(name = "RT")
    val rt: RT?,
    @Json(name = "Stn")
    val station: Stn?,
    @Json(name = "Addr")
    val address: Addr?,
    @Json(name = "Transport")
    val transport: Transport?,
    @Json(name = "AP")
    val ap: AP?,
    @Json(name = "Freq")
    val frequency: Freq?,
    @Json(name = "Activities")
    val activities: Activities?
)

@JsonClass(generateAdapter = true)
data class Stn(
    val x: Float,
    val y: Float,
    val name: String,
    val id: String?,
    val country: String?,
    /** 3 letter ISO 3166-1 country code */
    val ccode: String?,
    val state: String?,
    val city: String?,
    val postal: String?,
    val district: String?,
    val street: String?,
    val number: String?,
    val distance: Int?,
    val duration: Duration<IsoUnit>?,
    @Json(name = "has_board")
    val hasBoard: Int?,
    @Json(name = "Transports")
    val transports: Transports?,
    @Json(name = "Info")
    val info: String?,
    @Json(name = "At")
    val ats: List<At>?
)

@JsonClass(generateAdapter = true)
data class Addr(
    val x: Float,
    val y: Float,
    val name: String?,
    val id: String?,
    val country: String?,
    /** 3 letter ISO 3166-1 country code */
    val ccode: String?,
    val state: String?,
    val city: String?,
    val postal: String?,
    val district: String?,
    val street: String?,
    val number: String?,
    @Json(name = "Transports")
    val transports: Transports?,
    @Json(name = "Info")
    val info: String?,
    @Json(name = "At")
    val ats: List<At>?
)

@JsonClass(generateAdapter = true)
data class Transport(
    val name: String?,
    /** Numerical code of the transport mode. See Transit Modes. */
    val mode: TransportMode,
    /** Direction of the line,
     * usually specified as the destination station. */
    val dir: String?,
    @Json(name = "At")
    val at: At?,
    @Json(name = "Link")
    val links: List<Link>?
)

/**
 * RT, when present, contains the actual arrival or departure time,
 * and also the platform information (if available) for transit
 * stations.
 */
@JsonClass(generateAdapter = true)
data class RT(
    @Json(name = "has_arr")
    val hasArr: Boolean?,
    @Json(name = "has_dep")
    val hasDep: Boolean?,
    val arr: Date?,
    val dep: Date?,
    @Json(name = "new_stop")
    val newStop: Boolean?,
    val platform: String?,
    val status: String?
)

/**
 * At is an attribute within an list of attributes.
 * Each of these individual attributes contains an id and a value.
 * The id specifies the type of information related to the value.
 */
@JsonClass(generateAdapter = true)
data class At(
    val category: String?,
    val operator: String?,
    val bikeAllowed: Boolean?,
    val barrierFree: Boolean?,
    val escalator: Boolean?,
    val elevator: Boolean?,
    val blindGuide: Boolean?,
    @HexColor val color: Int?,
    @HexColor val textColor: Int?,
    @HexColor val outlineColor: Int?,
    val phone: String?,
    val email: String?,
    val tweetId: String?,
    val tweetTime: Date?,
    val tweetFullName: String?,
    val tweetUser: String?,
    val tweetAvatar: String?
)

@JsonClass(generateAdapter = true)
data class Link(
    val href: String?,
    val type: LinkType,
    @Json(name = "sec_ids")
    val secIds: String?,
    @Json(name = "href_text")
    val hrefText: String?
)

/**
 * AP contains all available information about an
 * individual transit access point (i.e. entrance/exit
 * of a transit station).
 */
@JsonClass(generateAdapter = true)
data class AP(
    val x: Float,
    val y: Float,
    val name: String?,
    val id: String?
)

/**
 * Freq contains the frequency of alternative departures.
 *
 * The min and max attributes specify the time intervals
 * between departures of a particular connection. The response
 * only contains this information if it is likely that the user
 * cannot reach a stop in time for the departure specified in
 * the response and may need to wait for the next one.
 */
@JsonClass(generateAdapter = true)
data class Freq(
    /* Number of minutes between transport scheduled departures. */
    val min: Int?,
    val max: Int?,
    /* number of minutes between expected transport real-time departures. */
    val minRT: Int?,
    val maxRT: Int?,
    @Json(name = "AltDep")
    val altDeps: List<AltDep>?
)

/**
 * AltDep contains information about an alternative departure,
 * including time. Transport contains the service name of the
 * alternative departure. If available, real time information
 * is provided.
 */
@JsonClass(generateAdapter = true)
data class AltDep(
    val time: Date,
    @Json(name = "RT")
    val rt: RT?,
    @Json(name = "Transport")
    val transport: Transport?
)

@JsonClass(generateAdapter = true)
data class Activities(
    @Json(name = "Act")
    val act: List<Act>
)

@JsonClass(generateAdapter = true)
data class Transports(
    @Json(name = "Transport")
    val transport: List<Transport>
)

@JsonClass(generateAdapter = true)
data class Operators(
    @Json(name = "Op")
    val operators: List<Op>
)

@JsonClass(generateAdapter = true)
data class Act(
    val type: ActivityType,
    val duration: Duration<IsoUnit>
)

@JsonClass(generateAdapter = true)
data class Op(
    val code: String?,
    val name: String,
    val type: OperationType?,
    @Json(name = "short_name")
    val shortName: String?,
    val fare: Boolean?,
    val modes: String?,
    @Json(name = "At")
    val ats: List<At>?,
    @Json(name = "Link")
    val links: List<Link>?
)

@JsonClass(generateAdapter = true)
data class Attributions(
    @Json(name = "Link")
    val links: List<Link>
)

@JsonClass(generateAdapter = true)
data class Alerts(
    @Json(name = "Alert")
    val alerts: List<Alert>
)

@JsonClass(generateAdapter = true)
data class Alert(
    val origin: AlertOrigin,
    val severity: AlertSeverity?,
    val operator: String,
    val id: String,
    @Json(name = "valid_till")
    val validTill: Date?,
    @Json(name = "valid_from")
    val validFrom: Date?,
    val url: String?,
    @Json(name = "sec_ids")
    val secIds: String?,
    @Json(name = "Info")
    val info: String,
    @Json(name = "Link")
    val link: Link?,
    @Json(name = "Transports")
    val transports: Transports?,
    @Json(name = "Branding")
    val branding: Branding?
)

@JsonClass(generateAdapter = true)
data class Branding(
    @Json(name = "At")
    val ats: List<At>
)

@JsonClass(generateAdapter = true)
data class MultiNextDepartures(
    @Json(name = "MultiNextDeparture")
    val multiNextDepartures: List<MultiNextDeparture>
)

@JsonClass(generateAdapter = true)
data class MultiNextDeparture(
    @Json(name = "Stn")
    val stn: Stn,
    @Json(name = "NextDepartures")
    val nextDepartures: NextDepartures
)

@JsonClass(generateAdapter = true)
data class Connections(
    val context: String,
    @Json(name = "allow_direction")
    val allowDirection: String?,
    @Json(name = "valid_until")
    val validUntil: String?,
    @Json(name = "sup_changes")
    val supChanges: Boolean?,
    @Json(name = "sup_speed")
    val supSpeed: Boolean?,
    @Json(name = "sup_max_dist")
    val supMaxDist: Boolean?,
    @Json(name = "sup_prod")
    val supProd: Boolean?,
    @Json(name = "Connection")
    val connections: List<Connection>,
    @Json(name = "Operators")
    val operators: Operators,
    @Json(name = "Attributions")
    val attributions: Attributions?
)

@JsonClass(generateAdapter = true)
data class Connection(
    val id: String,
    val duration: Duration<IsoUnit>,
    val transfers: Int,
    val ridable: Boolean?,
    @Json(name = "has_alt")
    val hasAlt: Boolean?,
    val alt: Boolean?,
    @Json(name = "first_last_mile")
    val firstLastMile: Boolean?,
    @Json(name = "Dep")
    val dep: Dep,
    @Json(name = "Arr")
    val arr: Arr,
    @Json(name = "Sections")
    val sections: Sections,
    @Json(name = "Tariff")
    val tariff: Tariff?
)

@JsonClass(generateAdapter = true)
data class Arr(
    val platform: String?,
    val time: Date,
    @Json(name = "RT")
    val rt: RT?,
    @Json(name = "Addr")
    val addr: Addr?,
    @Json(name = "Stn")
    val stn: Stn?,
    @Json(name = "AP")
    val ap: AP?,
    @Json(name = "Activities")
    val activities: Activities?
)

@JsonClass(generateAdapter = true)
data class Sections(
    @Json(name = "Sec")
    val secs: List<Sec>
)

@JsonClass(generateAdapter = true)
data class Sec(
    val uncertainty: Int?,
    val id: String?,
    val mode: TransportMode,
    val context: String?,
    @Json(name = "Dep")
    val dep: Dep,
    @Json(name = "Journey")
    val journey: Journey,
    @Json(name = "Arr")
    val arr: Arr,
    @Json(name = "Graph")
    val graph: String?
)

@JsonClass(generateAdapter = true)
data class Journey(
    val duration: Duration<IsoUnit>,
    val intermediate: Int?,
    val distance: Int?,
    @Json(name = "Stop")
    val stops: List<Stop>?
)

@JsonClass(generateAdapter = true)
data class Stop(
    val arr: Date?,
    val dep: Date?,
    @Json(name = "RT")
    val rt: RT?,
    @Json(name = "Stn")
    val stn: Stn
)

@JsonClass(generateAdapter = true)
data class Tariff(
    @Json(name = "Fares")
    val fares: List<Fares>
)

@JsonClass(generateAdapter = true)
data class Fares(
    @Json(name = "Fare")
    val fares: List<Fare>
)

@JsonClass(generateAdapter = true)
data class Fare(
    val name: String,
    val currency: String,
    val price: RPrice,
    @Json(name = "sec_ids")
    val secIds: String,
    val estimated: Boolean?,
    // Reason for the cost described in this Fare element. The default is ride.
    val reason: String?,
    @Json(name = "Link")
    val links: List<Link>
)

@JsonClass(generateAdapter = true)
data class Guidance(
    @Json(name = "Maneuvers")
    val maneuvers: List<Maneuvers>
)

@JsonClass(generateAdapter = true)
data class Maneuvers(
    @Json(name = "sec_ids")
    val secIds: String,
    @Json(name = "Maneuver")
    val maneuvers: List<Maneuver>?
)

@JsonClass(generateAdapter = true)
data class Maneuver(
    val direction: String,
    val action: String,
    val duration: Duration<IsoUnit>,
    @Json(name = "next_road")
    val nextRoad: String?,
    @Json(name = "next_number")
    val nextNumber: String?,
    val distance: Int?,
    val traffic: Float?,
    val instruction: String?,
    val graph: String?
)

@JsonClass(generateAdapter = true)
data class Stations(
    @Json(name = "Stn")
    val stations: List<Stn>
)

@JsonClass(generateAdapter = true)
data class Coverage(
    @Json(name = "ref_time")
    val refTime: Date,
    @Json(name = "CityCount")
    val cityCount: CityCount,
    @Json(name = "Cities")
    val cities: Cities?,
    @Json(name = "NearbyCities")
    val nearbyCities: NearbyCities?
)

@JsonClass(generateAdapter = true)
data class CityCount(
    @Json(name = "TT")
    val tt: Int,
    @Json(name = "SR")
    val sr: Int,
    @Json(name = "RT")
    val rt: Int
)

@JsonClass(generateAdapter = true)
data class Cities(
    @Json(name = "City")
    val cities: List<City>
)

@JsonClass(generateAdapter = true)
data class City(
    val name: String,
    val x: Float,
    val y: Float,
    val country: String,
    val created: Date,
    val updated: Date,
    @Json(name = "display_name")
    val displayName: String?,
    val state: String?,
    val relevancy: Float?,
    val distance: Int?,
    @Json(name = "Cvg")
    val cvg: Cvg,
    @Json(name = "Operators")
    val operators: Operators?,
    @Json(name = "Providers")
    val providers: Providers?,
    @Json(name = "MissingCoverage")
    val missingCoverage: MissingCoverage?,
    // Pop specifies the population of a city.
    @Json(name = "Pop")
    val pop: Int?
)

@JsonClass(generateAdapter = true)
data class Cvg(
    val quality: Float,
    val lines: Int?,
    val stops: Int?
)

@JsonClass(generateAdapter = true)
data class Providers(
    // Specifies all available information about the data provider.
    @Json(name = "Pr")
    val prs: List<Pr>
)

@JsonClass(generateAdapter = true)
data class Pr(
    // Name of the provider.
    val name: String
)

@JsonClass(generateAdapter = true)
data class MissingCoverage(
    val type: String?,
    // Contains information about a particular operator.
    @Json(name = "Op")
    val ops: List<Op>,
    // Transports taken from the nearest 5 stops, sorted by mode.
    @Json(name = "Transport")
    val transports: List<Transport>
)

@JsonClass(generateAdapter = true)
data class NearbyCities(
    @Json(name = "City")
    val cities: List<City>
)

@JsonClass(generateAdapter = true)
data class LocalCoverage(
    val georef: String,
    @Json(name = "NearbyCoverage")
    val nearbyCoverage: NearbyCoverage,
    @Json(name = "ExploredCoverage")
    val exploredCoverage: ExploredCoverage?,
    @Json(name = "City")
    val city: City?
)

@JsonClass(generateAdapter = true)
data class NearbyCoverage(
    val radius: Int,
    val stops: Int,
    val lines: Int,
    val type: String?,
    val covered: Boolean
)

@JsonClass(generateAdapter = true)
data class ExploredCoverage(
    val radius: Int,
    val stops: Int,
    val lines: Int,
    @Json(name = "Stn")
    val stn: Stn?,
    @Json(name = "Transports")
    val transports: Transports?
)

@JsonClass(generateAdapter = true)
data class Logos(
    @Json(name = "Link")
    val links: List<Link>
)

@JsonClass(generateAdapter = true)
data class Isochrone(
    val x: Float,
    val y: Float,
    @Json(name = "max_dur")
    val maxDuration: Duration<IsoUnit>,
    @Json(name = "max_change")
    val maxChange: Int,
    val time: Date,
    val timespan: Int,
    @Json(name = "IsoDest")
    val isoDestinations: List<IsoDest>?
)

@JsonClass(generateAdapter = true)
data class IsoDest(
    val duration: Duration<IsoUnit>,
    @Json(name = "Stn")
    val stns: List<Stn>?
)

@JsonClass(generateAdapter = true)
data class LineInfos(
    @Json(name = "LineInfo")
    val lineInfos: List<LineInfo>
)

@JsonClass(generateAdapter = true)
data class LineInfo(
    @Json(name = "Transport")
    val transport: Transport,
    @Json(name = "LineSegments")
    val lineSegments: List<LineSegments>
)

@JsonClass(generateAdapter = true)
data class LineSegments(
    val type: String,
    @Json(name = "seg_ids")
    val segIds: String
)

@JsonClass(generateAdapter = true)
data class PathSegments(
    @Json(name = "PathSeg")
    val pathSegs: List<PathSeg>,
    @Json(name = "RefPathSeg")
    val refPathSegs: List<RefPathSeg>?
)

@JsonClass(generateAdapter = true)
data class PathSeg(
    val id: String,
    val from: String,
    val to: String,
    val duration: Duration<IsoUnit>?,
    @Json(name = "Graph")
    val graph: String?
)

@JsonClass(generateAdapter = true)
data class RefPathSeg(
    val id: String,
    @Json(name = "seg_id")
    val segId: String,
    val reverse: Boolean?,
    val duration: Duration<IsoUnit>?
)

@JsonClass(generateAdapter = true)
data class RPrice(
    val price: Float? = null,
    val priceRange: String? = null
)

enum class Level {
    @Json(name = "M") MESSAGE,
    @Json(name = "W") WARNING,
    @Json(name = "E") ERROR,
    @Json(name = "F") FATAL
}

enum class LinkType {
    @Json(name = "appStore") APP_STORE,
    @Json(name = "tariff") TARIFF,
    @Json(name = "booking") BOOKING,
    @Json(name = "agency") AGENCY,
    @Json(name = "website") WEBSITE,
    @Json(name = "logo") LOGO,
    /**
     * Link to the original source of a transit alert
     * (eg. Twitter page). There is not text in the Link
     * element when the type is alert.
     */
    @Json(name = "alert") ALERT,
    @Json(name = "opIcon") OP_ICON,
    /**
     * Url to the Transport product icon.
     * For a transit type this is normally the
     * icon associated with the system. For a mobility
     * provider this represent the characteristic of the product.
     * When present, the link text refer to the product name.
     */
    @Json(name = "productIcon") PRODUCT_ICON,
    /**
     * Url to the Transport ride icon. For a transit type this
     * is normally the graphical representation of the line name.
     * For a mobility provider this represent the transport model.
     * When present, the link text refer to the ride name.
     */
    @Json(name = "rideIcon") RIDE_ICON
}

enum class ActivityType {
    @Json(name = "wait") WAIT,
    @Json(name = "setup") SETUP,
    @Json(name = "parking") PARKING,
}

enum class OperationType {
    @Json(name = "RT") REALTIME_ROUTING,
    @Json(name = "TT") TIMETABLE_ROUTING,
    @Json(name = "SR") ESTIMATED_ROUTING,
}

enum class AlertOrigin {
    API, // - received from API
    RSS, // - received from RSS
    WEB, // - obtained from WEB
    TWITTER, // - received from Twitter
    INVITRO // - alert issued internally
}

enum class AlertSeverity {
    DISRUPT, // The disruption exists but the severity is unknown
    INFO, // No disruption, just information
    LOW, // Low severity disruption
    MEDIUM, // Medium severity disruption
    HIGH // High severity disruption
}

class ISO8601DurationAdapter: JsonAdapter<Duration<IsoUnit>>(){
    @FromJson override fun fromJson(reader: JsonReader): Duration<IsoUnit>? {
        return Duration.parsePeriod(reader.nextString())
    }

    @ToJson override fun toJson(writer: JsonWriter, value: Duration<IsoUnit>?) {
        writer.value(value.toString())
    }
}

class TransportModeAdapter: JsonAdapter<TransportMode>(){
    @FromJson override fun fromJson(reader: JsonReader): TransportMode? {
        return TransportMode.getMode(reader.nextInt())
    }

    @ToJson override fun toJson(writer: JsonWriter, value: TransportMode?) {
        writer.value(value?.value)
    }
}

@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class HexColor

class BooleanAdapter: JsonAdapter<Boolean>(){
    @FromJson override fun fromJson(reader: JsonReader): Boolean? {
        return reader.nextInt() != 0
    }

    @ToJson override fun toJson(writer: JsonWriter, value: Boolean?) {
        writer.value(if (value == true) 1 else 0)
    }
}

class RPriceAdapter: JsonAdapter<RPrice>(){
    @FromJson override fun fromJson(reader: JsonReader): RPrice? {
        return try {
            RPrice(price = reader.nextDouble().toFloat())
        } catch (e: IOException) {
            RPrice(priceRange = reader.nextString())
        }
    }

    @ToJson override fun toJson(writer: JsonWriter, value: RPrice?) {
        if (value?.price != null) {
            writer.value(value.price)
        } else if (value?.priceRange != null) {
            writer.value(value.priceRange)
        }
    }
}

private class HereRfc3339DateJsonAdapter : JsonAdapter<Date>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Date {
        val string = reader.nextString()

        return ISO8601Utils.parse("${string}Z", ParsePosition(0))
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Date?) {
        val string = ISO8601Utils.format(value!!)
        writer.value(string.dropLast(1))
    }
}


enum class TransportMode (val value: Int) {
    HIGH_SPEED_TRAIN(0),
    INTERCITY_TRAIN(1),
    INTER_REGIONAL_TRAIN(2),
    REGIONAL_TRAIN(3),
    CITY_TRAIN(4),
    BUS(5),
    FERRY(6),
    SUBWAY(7),
    LIGHT_RAIL(8),
    PRIVATE_BUS(9),
    INCLINED(10),
    AERIAL(11),
    BUS_RAPID(12),
    MONORAIL(13),
    FLIGHT(14),
    WALK(20);

    companion object {
        private var list = values().associateBy({it.value}, {it})

        fun getMode(id: Int): TransportMode? {
            return list[id]
        }
    }
}
