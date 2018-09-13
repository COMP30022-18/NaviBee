package au.edu.unimelb.eng.navibee.navigation

import android.graphics.Color
import com.squareup.moshi.*
import org.threeten.bp.Duration
import java.util.*

@JsonClass(generateAdapter = true)
data class Response(val res: Result)

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
    val stn: Stn?,
    @Json(name = "Addr")
    val addr: Addr?,
    @Json(name = "Transport")
    val transport: Transport?,
    @Json(name = "AP")
    val ap: AP?,
    @Json(name = "Freq")
    val freq: Freq?,
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
    val duration: Duration?,
    val has_board: Int?,
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
    val ats: List<At>,
    @Json(name = "Link")
    val links: List<Link>
)

/**
 * RT, when present, contains the actual arrival or departure time,
 * and also the platform information (if available) for transit
 * stations.
 */
@JsonClass(generateAdapter = true)
data class RT(
    val has_arr: Boolean?,
    val has_dep: Boolean?,
    val arr: Date?,
    val dep: Date?,
    val new_stop: Boolean?,
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
        val color: ColorInt?,
        val textColor: ColorInt?,
        val outlineColor: ColorInt?,
        val phone: String,
        val email: String,
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
        val duration: Duration
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
    val ats: List<At>,
    @Json(name = "Link")
    val links: List<Link>
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
        val valid_till: Date?,
        val valid_from: Date?,
        val url: String?,
        val sec_ids: String?,
        @Json(name = "Info")
        val info: String?,
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
    val multiNextDepartures: List<MultiNextDeparture>?
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
        @Json(name = "valid_until")
        val validUntil: String,
        @Json(name = "sup_changes")
        val supChanges: Boolean,
        @Json(name = "sup_speed")
        val supSpeed: Boolean,
        @Json(name = "sup_max_dist")
        val supMaxDist: Boolean,
        @Json(name = "sup_prod")
        val supProd: Boolean,
        @Json(name = "Connection")
        val connection: List<Connection>,
        @Json(name = "Operators")
        val operators: Operators,
        @Json(name = "Attributions")
        val attributions: Attributions
)

@JsonClass(generateAdapter = true)
data class Connection(
    val id: String,
    val duration: Duration,
    val transfers: Int,
    val ridable: Boolean?,
    @Json(name = "has_alt")
    val hasAlt: Boolean?,
    val alt: Boolean,
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

class ISO8601DurationAdapter: JsonAdapter<Duration>(){
    override fun fromJson(reader: JsonReader): Duration? {
        return Duration.parse(reader.nextString())
    }

    override fun toJson(writer: JsonWriter, value: Duration?) {
        writer.value(value.toString())
    }
}

class TransportModeAdapter: JsonAdapter<TransportMode>(){
    override fun fromJson(reader: JsonReader): TransportMode? {
        return TransportMode.getMode(reader.nextInt())
    }

    override fun toJson(writer: JsonWriter, value: TransportMode?) {
        writer.value(value?.value)
    }
}

data class ColorInt(val color: Int) {
    override fun toString(): String =
        "#${Integer.toHexString(color)}"
}

class ColorAdapter: JsonAdapter<ColorInt>(){
    override fun fromJson(reader: JsonReader): ColorInt? {
        return ColorInt(Color.parseColor(reader.nextString()))
    }

    override fun toJson(writer: JsonWriter, value: ColorInt?) {
        writer.value(value.toString())
    }
}

class BooleanAdapter: JsonAdapter<Boolean>(){
    override fun fromJson(reader: JsonReader): Boolean? {
        return reader.nextInt() != 0
    }

    override fun toJson(writer: JsonWriter, value: Boolean?) {
        writer.value(if (value == true) 1 else 0)
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
    FLIGHT(14);

    companion object {
        private var list = values().associateBy({it.value}, {it})

        fun getMode(id: Int): TransportMode? {
            return list[id]
        }
    }
}