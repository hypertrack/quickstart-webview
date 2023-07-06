package com.hypertrack.quickstart.android.webview

import android.location.Location
import android.webkit.JavascriptInterface
import com.hypertrack.sdk.Blocker
import com.hypertrack.sdk.GeotagResult
import com.hypertrack.sdk.HyperTrack
import org.json.JSONArray
import org.json.JSONObject

/**
 * Kotlin doesn't have package-private visibility modifier,
 * so everything is put into one class to avoid exposing
 */
object HyperTrackJsApi {

    const val apiName = "HyperTrack"

    private const val KEY_TYPE = "type"
    private const val KEY_VALUE = "value"
    private const val KEY_GEOTAG_DATA = "data"
    private const val KEY_GEOTAG_EXPECTED_LOCATION = "expectedLocation"
    private const val KEY_LOCATION = "location"
    private const val KEY_DEVIATION = "deviation"

    private const val TYPE_RESULT_SUCCESS = "success"
    private const val TYPE_RESULT_FAILURE = "failure"
    private const val TYPE_LOCATION = "location"
    private const val TYPE_LOCATION_WITH_DEVIATION = "locationWithDeviation"
    private const val TYPE_HYPERTRACK_ERROR = "hyperTrackError"
    private const val TYPE_LOCATION_ERROR_NOT_RUNNING = "notRunning"
    private const val TYPE_LOCATION_ERROR_STARTING = "starting"
    private const val TYPE_LOCATION_ERROR_ERRORS = "errors"

    private const val KEY_LATITUDE = "latitude"
    private const val KEY_LONGITUDE = "longitude"

    private val sdkInstance by lazy {
        HyperTrack.getInstance("PasteYourPublishableKeyHere")
    }

    @JavascriptInterface
    fun getDeviceId(): String {
        return sdkInstance.deviceID
    }

    @JavascriptInterface
    fun setIsTracking(isTracking: Boolean) {
        if (isTracking) {
            sdkInstance.start()
        } else {
            sdkInstance.stop()
        }
    }

    @JavascriptInterface
    fun addGeotag(dataJsonString: String): String {
        return addGeotagWithExpectedLocation(dataJsonString, null)
    }

    @JavascriptInterface
    fun addGeotagWithExpectedLocation(
        dataJsonString: String,
        expectedLocationString: String?
    ): String {
        return deserializeJsGeotagData(dataJsonString, expectedLocationString)
            .flatMapSuccess { geotagDataMap ->
                addGeotag(geotagDataMap)
            }.let { result ->
                when (result) {
                    is Failure -> {
                        serializeJsFailure(mapOf("error" to result.failure.toString()))
                    }

                    is Success -> {
                        serializeJsSuccess(result.success)
                    }
                }
            }
    }

    /**
     * HyperTrackWrapper
     */
    private fun addGeotag(args: Map<String, Any?>): Result<Map<String, Any?>> {
        return deserializeGeotagData(args)
            .flatMapSuccess { geotag ->
                sdkInstance
                    .addGeotag(geotag.data, geotag.expectedLocation)
                    .let { result ->
                        if (geotag.expectedLocation == null) {
                            when (result) {
                                is GeotagResult.SuccessWithDeviation -> {
                                    // not supposed to happen
                                    serializeLocationSuccess(result.deviceLocation)
                                }

                                is GeotagResult.Success -> {
                                    serializeLocationSuccess(result.deviceLocation)
                                }

                                is GeotagResult.Error -> {
                                    serializeLocationErrorFailure(getLocationError(result.reason))
                                }

                                else -> {
                                    throw IllegalArgumentException()
                                }
                            }
                        } else {
                            when (result) {
                                is GeotagResult.SuccessWithDeviation -> {
                                    serializeLocationWithDeviationSuccess(
                                        result.deviceLocation,
                                        result.deviationDistance.toDouble()
                                    )
                                }

                                is GeotagResult.Success -> {
                                    // not supposed to happen
                                    serializeLocationWithDeviationSuccess(
                                        result.deviceLocation,
                                        0.0
                                    )
                                }

                                is GeotagResult.Error -> {
                                    serializeLocationErrorFailure(getLocationError(result.reason))
                                }

                                else -> {
                                    throw IllegalArgumentException()
                                }
                            }
                        }
                    }
                    .let {
                        Success(it)
                    }
            }
    }

    private fun getLocationError(error: GeotagResult.Error.Reason): LocationError {
        val blockersErrors = getHyperTrackErrorsFromBlockers()
        return when (error) {
            GeotagResult.Error.Reason.NO_GPS_SIGNAL -> {
                Errors(setOf(HyperTrackError.gpsSignalLost) + blockersErrors)
            }

            GeotagResult.Error.Reason.MISSING_LOCATION_PERMISSION -> {
                Errors(setOf(HyperTrackError.locationPermissionsDenied) + blockersErrors)
            }

            GeotagResult.Error.Reason.LOCATION_SERVICE_DISABLED -> {
                Errors(setOf(HyperTrackError.locationServicesDisabled) + blockersErrors)
            }

            GeotagResult.Error.Reason.MISSING_ACTIVITY_PERMISSION -> {
                Errors(setOf(HyperTrackError.motionActivityPermissionsDenied) + blockersErrors)
            }

            GeotagResult.Error.Reason.NOT_TRACKING -> {
                NotRunning
            }

            GeotagResult.Error.Reason.START_HAS_NOT_FINISHED -> {
                Starting
            }
        }
    }

    private fun getHyperTrackErrorsFromBlockers(): Set<HyperTrackError> {
        return HyperTrack.getBlockers()
            .map {
                when (it) {
                    Blocker.LOCATION_PERMISSION_DENIED -> {
                        HyperTrackError.locationPermissionsDenied
                    }

                    Blocker.LOCATION_SERVICE_DISABLED -> {
                        HyperTrackError.locationServicesDisabled
                    }

                    Blocker.ACTIVITY_PERMISSION_DENIED -> {
                        HyperTrackError.motionActivityPermissionsDenied
                    }

                    Blocker.BACKGROUND_LOCATION_DENIED -> {
                        HyperTrackError.locationPermissionsInsufficientForBackground
                    }
                }
            }
            .toSet()
    }

    private sealed class LocationError
    private object NotRunning : LocationError()
    private object Starting : LocationError()
    private data class Errors(val errors: Set<HyperTrackError>) : LocationError()

    @Suppress("EnumEntryName")
    private enum class HyperTrackError {
        gpsSignalLost,
        locationMocked,
        locationPermissionsDenied,
        locationPermissionsInsufficientForBackground,
        locationPermissionsNotDetermined,
        locationPermissionsReducedAccuracy,
        locationPermissionsProvisional,
        locationPermissionsRestricted,
        locationServicesDisabled,
        locationServicesUnavailable,
        motionActivityPermissionsNotDetermined,
        motionActivityPermissionsDenied,
        motionActivityServicesDisabled,
        motionActivityServicesUnavailable,
        motionActivityPermissionsRestricted,
        networkConnectionUnavailable,
        invalidPublishableKey,
        blockedFromRunning
    }

    private data class GeotagData(
        val data: Map<String, Any?>,
        val expectedLocation: Location?
    )

    private sealed class Result<SuccessType> {
        fun <MappedSuccess> flatMapSuccess(
            onSuccess: (SuccessType) -> Result<MappedSuccess>
        ): Result<MappedSuccess> {
            return when (this) {
                is Success -> {
                    onSuccess.invoke(this.success)
                }

                is Failure -> {
                    Failure<MappedSuccess>(this.failure)
                }
            }
        }

        fun <MappedSuccess> mapSuccess(onSuccess: (SuccessType) -> MappedSuccess): Result<MappedSuccess> {
            return when (this) {
                is Success -> {
                    Success(onSuccess.invoke(this.success))
                }

                is Failure -> {
                    Failure<MappedSuccess>(this.failure)
                }
            }
        }

        fun getOrThrow(): SuccessType {
            return when (this) {
                is Success -> this.success
                is Failure -> throw Exception(
                    "Result unwrapping failed: ${this.failure}",
                    this.failure
                )
            }
        }

        companion object {
            fun <SuccessType> tryAsResult(block: () -> SuccessType): Result<SuccessType> {
                return try {
                    Success(block.invoke())
                } catch (e: Exception) {
                    Failure(e)
                }
            }
        }
    }

    private data class Success<SuccessType>(val success: SuccessType) : Result<SuccessType>()
    private data class Failure<SuccessType>(val failure: Throwable) : Result<SuccessType>()

    /**
     * Serialization
     */

    private fun serializeErrors(errors: Set<HyperTrackError>): List<Map<String, String>> {
        return errors.map {
            serializeHyperTrackError(it)
        }
    }

    private fun serializeLocationSuccess(location: Location): Map<String, Any?> {
        return serializeSuccess(serializeLocation(location))
    }

    private fun serializeLocationWithDeviationSuccess(
        location: Location,
        deviation: Double
    ): Map<String, Any?> {
        return serializeSuccess(
            serializeLocationWithDeviation(
                location,
                deviation
            )
        )
    }

    private fun serializeLocationErrorFailure(locationError: LocationError): Map<String, Any?> {
        return serializeFailure(serializeLocationError(locationError))
    }

    private fun serializeHyperTrackError(error: HyperTrackError): Map<String, String> {
        return mapOf(
            KEY_TYPE to TYPE_HYPERTRACK_ERROR,
            KEY_VALUE to error.name
        )
    }

    private fun deserializeGeotagData(map: Map<String, Any?>): Result<GeotagData> {
        return parse(map) {
            val data = it
                .get<Map<String, Any?>>(KEY_GEOTAG_DATA)
                .getOrThrow()
            val locationData = it
                .getOptional<Map<String, Any?>>(KEY_GEOTAG_EXPECTED_LOCATION)
                .getOrThrow()
            val location = locationData?.let { deserializeLocation(it).getOrThrow() }
            GeotagData(data, location)
        }
    }

    private fun <T> parse(
        source: Map<String, Any?>,
        parseFunction: (Parser) -> T
    ): Result<T> {
        val parser = Parser(source)
        return try {
            if (parser.exceptions.isEmpty()) {
                Success(parseFunction.invoke(parser))
            } else {
                Failure(ParsingExceptions(source, parser.exceptions))
            }
        } catch (e: Exception) {
            Failure(
                if (parser.exceptions.isNotEmpty()) {
                    ParsingExceptions(source, parser.exceptions + e)
                } else {
                    e
                }
            )
        }
    }

    private class Parser(
        private val source: Map<String, Any?>
    ) {
        private val _exceptions = mutableListOf<Exception>()
        val exceptions: List<Exception> = _exceptions

        inline fun <reified T> get(
            key: String
        ): Result<T> {
            return try {
                Success(source[key]!! as T)
            } catch (e: Exception) {
                Failure(
                    ParsingException(key, e)
                        .also {
                            _exceptions.add(it)
                        }
                )
            }
        }

        inline fun <reified T> getOptional(
            key: String
        ): Result<T?> {
            return try {
                Success(source[key] as T?)
            } catch (e: Exception) {
                Failure(
                    ParsingException(key, e)
                        .also {
                            _exceptions.add(it)
                        }
                )
            }
        }

        inline fun <reified T> assertValue(
            key: String,
            value: Any
        ) {
            if (source[key] != value) {
                _exceptions.add(Exception("Assertion failed: $key != $value"))
            }
        }
    }

    private data class ParsingExceptions(
        val source: Any,
        val exceptions: List<Exception>
    ) : Throwable(
        exceptions.joinToString("\n")
            .let {
                "Invalid input:\n\n${source}\n\n$it"
            }
    )

    private class ParsingException(
        key: String,
        exception: Exception
    ) : Exception("Invalid value for '$key': $exception", exception)

    private fun deserializeLocation(map: Map<String, Any?>): Result<Location> {
        return parse(map) {
            it.assertValue<String>(key = KEY_TYPE, value = TYPE_LOCATION)
            val value = it
                .get<Map<String, Any?>>(KEY_VALUE)
                .getOrThrow()
            parse(value) { parser ->
                val latitude = parser
                    .get<Double>(KEY_LATITUDE)
                    .getOrThrow()
                val longitude = parser
                    .get<Double>(KEY_LONGITUDE)
                    .getOrThrow()
                Location("api").also {
                    it.latitude = latitude
                    it.longitude = longitude
                }
            }.getOrThrow()
        }
    }

    private fun serializeLocationWithDeviation(
        location: Location,
        deviation: Double
    ): Map<String, Any?> {
        return mapOf(
            KEY_TYPE to TYPE_LOCATION_WITH_DEVIATION,
            KEY_VALUE to mapOf(
                KEY_LOCATION to serializeLocation(location),
                KEY_DEVIATION to deviation
            )
        )
    }

    private fun serializeFailure(failure: Map<String, Any?>): Map<String, Any?> {
        return mapOf(
            KEY_TYPE to TYPE_RESULT_FAILURE,
            KEY_VALUE to failure
        )
    }

    private fun serializeSuccess(success: Map<String, Any?>): Map<String, Any?> {
        return mapOf(
            KEY_TYPE to TYPE_RESULT_SUCCESS,
            KEY_VALUE to success
        )
    }

    private fun serializeLocation(location: Location): Map<String, Any?> {
        return mapOf(
            KEY_TYPE to TYPE_LOCATION,
            KEY_VALUE to mapOf(
                KEY_LATITUDE to location.latitude,
                KEY_LONGITUDE to location.longitude
            )
        )
    }

    private fun serializeLocationError(locationError: LocationError): Map<String, Any?> {
        return when (locationError) {
            NotRunning -> {
                mapOf(KEY_TYPE to TYPE_LOCATION_ERROR_NOT_RUNNING)
            }

            Starting -> {
                mapOf(KEY_TYPE to TYPE_LOCATION_ERROR_STARTING)
            }

            is Errors -> {
                mapOf(
                    KEY_TYPE to TYPE_LOCATION_ERROR_ERRORS,
                    KEY_VALUE to locationError.errors
                        .map { serializeHyperTrackError(it) }
                )
            }
        }
    }

    /**
     * WebViewSerialization
     */

    private fun deserializeJsLocation(
        jsonString: String
    ): Map<String, Any?> {
        return mapOf(
            KEY_TYPE to TYPE_LOCATION,
            KEY_VALUE to JSONObject(jsonString).toMap()
        )
    }

    private fun deserializeJsGeotagData(
        dataJsonString: String,
        expectedLocationJsonString: String?
    ): Result<Map<String, Any?>> {
        return try {
            Success(
                mutableMapOf(
                    KEY_GEOTAG_DATA to JSONObject(dataJsonString).toMap(),
                ).apply {
                    if (expectedLocationJsonString != null) {
                        put(
                            KEY_GEOTAG_EXPECTED_LOCATION,
                            deserializeJsLocation(expectedLocationJsonString)
                        )
                    }
                })
        } catch (e: Exception) {
            Failure(e)
        }
    }

    private fun serializeJsSuccess(value: Map<String, Any?>): String {
        return mapOf(
            KEY_TYPE to TYPE_RESULT_SUCCESS,
            KEY_VALUE to value
        ).toJSONObject().toString()
    }

    private fun serializeJsFailure(value: Map<String, Any?>): String {
        return mapOf(
            KEY_TYPE to TYPE_RESULT_FAILURE,
            KEY_VALUE to value
        ).toJSONObject().toString()
    }

    private fun JSONObject.toMap(): Map<String, Any?> {
        return keys().asSequence().associateWith { key ->
            when (val value = this.get(key)) {
                is Boolean,
                is String,
                is Double,
                is Int -> {
                    value
                }

                is JSONArray -> {
                    value.toList()
                }

                is JSONObject -> {
                    value.toMap()
                }

                else -> {
                    null
                }
            }
        }
    }

    private fun JSONArray.toList(): List<Any> {
        return (0..length()).mapNotNull { index ->
            when (val value = this.get(index)) {
                is Boolean,
                is String,
                is Double,
                is Int -> {
                    value
                }

                is JSONArray -> {
                    value.toList()
                }

                is JSONObject -> {
                    value.toMap()
                }

                else -> {
                    null
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any?>.toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        for ((key, value) in this) {
            when (value) {
                is Map<*, *> -> jsonObject.put(key, (value as Map<String, Any?>).toJSONObject())
                is List<*> -> jsonObject.put(key, (value as List<Any?>).toJSONArray())
                else -> jsonObject.put(key, value)
            }
        }
        return jsonObject
    }

    @Suppress("UNCHECKED_CAST")
    private fun List<Any?>.toJSONArray(): JSONArray {
        val jsonArray = JSONArray()
        for (value in this) {
            when (value) {
                is Map<*, *> -> jsonArray.put((value as Map<String, Any?>).toJSONObject())
                is List<*> -> jsonArray.put((value as List<Any?>).toJSONArray())
                else -> jsonArray.put(value)
            }
        }
        return jsonArray
    }

}
