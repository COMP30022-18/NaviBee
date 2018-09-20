package au.edu.unimelb.eng.navibee.navigation

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import au.edu.unimelb.eng.navibee.BuildConfig
import au.edu.unimelb.eng.navibee.R
import au.edu.unimelb.eng.navibee.utils.ImageViewCacheLoader
import com.google.android.gms.location.places.Place
import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

private val geoContext = GeoApiContext
        .Builder().apiKey(BuildConfig.GOOGLE_PLACES_API_KEY).build()

class GoogleMapsPlaceIdCacheImageLoader(private val placeId: String,
                                        iv: ImageView,
                                        maxHeight: Int,
                                        index: Int = 0,
                                        singleJob: Boolean = true):
        GoogleMapsPhotoReferenceCacheImageLoader("", iv, maxHeight, singleJob) {
    override val defaultKey = "$placeId-$index"
    override fun loadTask(file: File) {
        val placeDetails = PlacesApi.placeDetails(geoContext, placeId).await()
        placeDetails?.photos?.run {
            if (isNotEmpty()) {
                photoReference = this[0]?.photoReference ?: ""
                if (photoReference.isNotBlank()) {
                    super.loadTask(file)
                }
            }
        }
    }
}

open class GoogleMapsPhotoReferenceCacheImageLoader(var photoReference: String,
                                                    iv: ImageView,
                                                    private val maxHeight: Int,
                                                    singleJob: Boolean = true):
        ImageViewCacheLoader(iv, prefix="gmpr", singleJob = singleJob) {

    override val defaultKey = photoReference
    override fun loadTask(file: File) {
        launch {
            val image = withContext(DefaultDispatcher) { loadImage() }
            if (image != null) {
                val outputStream = FileOutputStream(file)
                image.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                outputStream.close()
            }
            postLoad(file)
        }
    }

    private fun loadImage(): Bitmap? =
        try {
            val result = PlacesApi
                    .photo(geoContext, photoReference)
                    .maxHeight(maxHeight)
                    .await()

            BitmapFactory.decodeByteArray(result.imageData,0, result.imageData.size)
        } catch (e: Exception) {
            Timber.e(e,
                    "Error occurred while trying to download image from Google Maps Place SDK.")
            null
        }
}

fun googlePlaceTypeIDToString(id: Int, resources: Resources) = when (id) {
    Place.TYPE_ACCOUNTING -> resources.getString(R.string.place_type_accounting)
    Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_1 -> resources.getString(R.string.place_type_administrative_area_level_1)
    Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_2 -> resources.getString(R.string.place_type_administrative_area_level_2)
    Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_3 -> resources.getString(R.string.place_type_administrative_area_level_3)
    Place.TYPE_AIRPORT -> resources.getString(R.string.place_type_airport)
    Place.TYPE_AMUSEMENT_PARK -> resources.getString(R.string.place_type_amusement_park)
    Place.TYPE_AQUARIUM -> resources.getString(R.string.place_type_aquarium)
    Place.TYPE_ART_GALLERY -> resources.getString(R.string.place_type_art_gallery)
    Place.TYPE_ATM -> resources.getString(R.string.place_type_atm)
    Place.TYPE_BAKERY -> resources.getString(R.string.place_type_bakery)
    Place.TYPE_BANK -> resources.getString(R.string.place_type_bank)
    Place.TYPE_BAR -> resources.getString(R.string.place_type_bar)
    Place.TYPE_BEAUTY_SALON -> resources.getString(R.string.place_type_beauty_salon)
    Place.TYPE_BICYCLE_STORE -> resources.getString(R.string.place_type_bicycle_store)
    Place.TYPE_BOOK_STORE -> resources.getString(R.string.place_type_book_store)
    Place.TYPE_BOWLING_ALLEY -> resources.getString(R.string.place_type_bowling_alley)
    Place.TYPE_BUS_STATION -> resources.getString(R.string.place_type_bus_station)
    Place.TYPE_CAFE -> resources.getString(R.string.place_type_cafe)
    Place.TYPE_CAMPGROUND -> resources.getString(R.string.place_type_campground)
    Place.TYPE_CAR_DEALER -> resources.getString(R.string.place_type_car_dealer)
    Place.TYPE_CAR_RENTAL -> resources.getString(R.string.place_type_car_rental)
    Place.TYPE_CAR_REPAIR -> resources.getString(R.string.place_type_car_repair)
    Place.TYPE_CAR_WASH -> resources.getString(R.string.place_type_car_wash)
    Place.TYPE_CASINO -> resources.getString(R.string.place_type_casino)
    Place.TYPE_CEMETERY -> resources.getString(R.string.place_type_cemetery)
    Place.TYPE_CHURCH -> resources.getString(R.string.place_type_church)
    Place.TYPE_CITY_HALL -> resources.getString(R.string.place_type_city_hall)
    Place.TYPE_CLOTHING_STORE -> resources.getString(R.string.place_type_clothing_store)
    Place.TYPE_COLLOQUIAL_AREA -> resources.getString(R.string.place_type_colloquial_area)
    Place.TYPE_CONVENIENCE_STORE -> resources.getString(R.string.place_type_convenience_store)
    Place.TYPE_COUNTRY -> resources.getString(R.string.place_type_country)
    Place.TYPE_COURTHOUSE -> resources.getString(R.string.place_type_courthouse)
    Place.TYPE_DENTIST -> resources.getString(R.string.place_type_dentist)
    Place.TYPE_DEPARTMENT_STORE -> resources.getString(R.string.place_type_department_store)
    Place.TYPE_DOCTOR -> resources.getString(R.string.place_type_doctor)
    Place.TYPE_ELECTRICIAN -> resources.getString(R.string.place_type_electrician)
    Place.TYPE_ELECTRONICS_STORE -> resources.getString(R.string.place_type_electronics_store)
    Place.TYPE_EMBASSY -> resources.getString(R.string.place_type_embassy)
    Place.TYPE_ESTABLISHMENT -> resources.getString(R.string.place_type_establishment)
    Place.TYPE_FINANCE -> resources.getString(R.string.place_type_finance)
    Place.TYPE_FIRE_STATION -> resources.getString(R.string.place_type_fire_station)
    Place.TYPE_FLOOR -> resources.getString(R.string.place_type_floor)
    Place.TYPE_FLORIST -> resources.getString(R.string.place_type_florist)
    Place.TYPE_FOOD -> resources.getString(R.string.place_type_food)
    Place.TYPE_FUNERAL_HOME -> resources.getString(R.string.place_type_funeral_home)
    Place.TYPE_FURNITURE_STORE -> resources.getString(R.string.place_type_furniture_store)
    Place.TYPE_GAS_STATION -> resources.getString(R.string.place_type_gas_station)
    Place.TYPE_GENERAL_CONTRACTOR -> resources.getString(R.string.place_type_general_contractor)
    Place.TYPE_GEOCODE -> resources.getString(R.string.place_type_geocode)
    Place.TYPE_GROCERY_OR_SUPERMARKET -> resources.getString(R.string.place_type_grocery_or_supermarket)
    Place.TYPE_GYM -> resources.getString(R.string.place_type_gym)
    Place.TYPE_HAIR_CARE -> resources.getString(R.string.place_type_hair_care)
    Place.TYPE_HARDWARE_STORE -> resources.getString(R.string.place_type_hardware_store)
    Place.TYPE_HEALTH -> resources.getString(R.string.place_type_health)
    Place.TYPE_HINDU_TEMPLE -> resources.getString(R.string.place_type_hindu_temple)
    Place.TYPE_HOME_GOODS_STORE -> resources.getString(R.string.place_type_home_goods_store)
    Place.TYPE_HOSPITAL -> resources.getString(R.string.place_type_hospital)
    Place.TYPE_INSURANCE_AGENCY -> resources.getString(R.string.place_type_insurance_agency)
    Place.TYPE_INTERSECTION -> resources.getString(R.string.place_type_intersection)
    Place.TYPE_JEWELRY_STORE -> resources.getString(R.string.place_type_jewelry_store)
    Place.TYPE_LAUNDRY -> resources.getString(R.string.place_type_laundry)
    Place.TYPE_LAWYER -> resources.getString(R.string.place_type_lawyer)
    Place.TYPE_LIBRARY -> resources.getString(R.string.place_type_library)
    Place.TYPE_LIQUOR_STORE -> resources.getString(R.string.place_type_liquor_store)
    Place.TYPE_LOCALITY -> resources.getString(R.string.place_type_locality)
    Place.TYPE_LOCAL_GOVERNMENT_OFFICE -> resources.getString(R.string.place_type_local_government_office)
    Place.TYPE_LOCKSMITH -> resources.getString(R.string.place_type_locksmith)
    Place.TYPE_LODGING -> resources.getString(R.string.place_type_lodging)
    Place.TYPE_MEAL_DELIVERY -> resources.getString(R.string.place_type_meal_delivery)
    Place.TYPE_MEAL_TAKEAWAY -> resources.getString(R.string.place_type_meal_takeaway)
    Place.TYPE_MOSQUE -> resources.getString(R.string.place_type_mosque)
    Place.TYPE_MOVIE_RENTAL -> resources.getString(R.string.place_type_movie_rental)
    Place.TYPE_MOVIE_THEATER -> resources.getString(R.string.place_type_movie_theater)
    Place.TYPE_MOVING_COMPANY -> resources.getString(R.string.place_type_moving_company)
    Place.TYPE_MUSEUM -> resources.getString(R.string.place_type_museum)
    Place.TYPE_NATURAL_FEATURE -> resources.getString(R.string.place_type_natural_feature)
    Place.TYPE_NEIGHBORHOOD -> resources.getString(R.string.place_type_neighborhood)
    Place.TYPE_NIGHT_CLUB -> resources.getString(R.string.place_type_night_club)
    Place.TYPE_OTHER -> resources.getString(R.string.place_type_other)
    Place.TYPE_PAINTER -> resources.getString(R.string.place_type_painter)
    Place.TYPE_PARK -> resources.getString(R.string.place_type_park)
    Place.TYPE_PARKING -> resources.getString(R.string.place_type_parking)
    Place.TYPE_PET_STORE -> resources.getString(R.string.place_type_pet_store)
    Place.TYPE_PHARMACY -> resources.getString(R.string.place_type_pharmacy)
    Place.TYPE_PHYSIOTHERAPIST -> resources.getString(R.string.place_type_physiotherapist)
    Place.TYPE_PLACE_OF_WORSHIP -> resources.getString(R.string.place_type_place_of_worship)
    Place.TYPE_PLUMBER -> resources.getString(R.string.place_type_plumber)
    Place.TYPE_POINT_OF_INTEREST -> resources.getString(R.string.place_type_point_of_interest)
    Place.TYPE_POLICE -> resources.getString(R.string.place_type_police)
    Place.TYPE_POLITICAL -> resources.getString(R.string.place_type_political)
    Place.TYPE_POSTAL_CODE -> resources.getString(R.string.place_type_postal_code)
    Place.TYPE_POSTAL_CODE_PREFIX -> resources.getString(R.string.place_type_postal_code_prefix)
    Place.TYPE_POSTAL_TOWN -> resources.getString(R.string.place_type_postal_town)
    Place.TYPE_POST_BOX -> resources.getString(R.string.place_type_post_box)
    Place.TYPE_POST_OFFICE -> resources.getString(R.string.place_type_post_office)
    Place.TYPE_PREMISE -> resources.getString(R.string.place_type_premise)
    Place.TYPE_REAL_ESTATE_AGENCY -> resources.getString(R.string.place_type_real_estate_agency)
    Place.TYPE_RESTAURANT -> resources.getString(R.string.place_type_restaurant)
    Place.TYPE_ROOFING_CONTRACTOR -> resources.getString(R.string.place_type_roofing_contractor)
    Place.TYPE_ROOM -> resources.getString(R.string.place_type_room)
    Place.TYPE_ROUTE -> resources.getString(R.string.place_type_route)
    Place.TYPE_RV_PARK -> resources.getString(R.string.place_type_rv_park)
    Place.TYPE_SCHOOL -> resources.getString(R.string.place_type_school)
    Place.TYPE_SHOE_STORE -> resources.getString(R.string.place_type_shoe_store)
    Place.TYPE_SHOPPING_MALL -> resources.getString(R.string.place_type_shopping_mall)
    Place.TYPE_SPA -> resources.getString(R.string.place_type_spa)
    Place.TYPE_STADIUM -> resources.getString(R.string.place_type_stadium)
    Place.TYPE_STORAGE -> resources.getString(R.string.place_type_storage)
    Place.TYPE_STORE -> resources.getString(R.string.place_type_store)
    Place.TYPE_STREET_ADDRESS -> resources.getString(R.string.place_type_street_address)
    Place.TYPE_SUBLOCALITY -> resources.getString(R.string.place_type_sublocality)
    Place.TYPE_SUBLOCALITY_LEVEL_1 -> resources.getString(R.string.place_type_sublocality_level_1)
    Place.TYPE_SUBLOCALITY_LEVEL_2 -> resources.getString(R.string.place_type_sublocality_level_2)
    Place.TYPE_SUBLOCALITY_LEVEL_3 -> resources.getString(R.string.place_type_sublocality_level_3)
    Place.TYPE_SUBLOCALITY_LEVEL_4 -> resources.getString(R.string.place_type_sublocality_level_4)
    Place.TYPE_SUBLOCALITY_LEVEL_5 -> resources.getString(R.string.place_type_sublocality_level_5)
    Place.TYPE_SUBPREMISE -> resources.getString(R.string.place_type_subpremise)
    Place.TYPE_SUBWAY_STATION -> resources.getString(R.string.place_type_subway_station)
    Place.TYPE_SYNAGOGUE -> resources.getString(R.string.place_type_synagogue)
    Place.TYPE_SYNTHETIC_GEOCODE -> resources.getString(R.string.place_type_synthetic_geocode)
    Place.TYPE_TAXI_STAND -> resources.getString(R.string.place_type_taxi_stand)
    Place.TYPE_TRAIN_STATION -> resources.getString(R.string.place_type_train_station)
    Place.TYPE_TRANSIT_STATION -> resources.getString(R.string.place_type_transit_station)
    Place.TYPE_TRAVEL_AGENCY -> resources.getString(R.string.place_type_travel_agency)
    Place.TYPE_UNIVERSITY -> resources.getString(R.string.place_type_university)
    Place.TYPE_VETERINARY_CARE -> resources.getString(R.string.place_type_veterinary_care)
    Place.TYPE_ZOO -> resources.getString(R.string.place_type_zoo)
    else -> resources.getString(R.string.place_unknown)
} ?: ""
