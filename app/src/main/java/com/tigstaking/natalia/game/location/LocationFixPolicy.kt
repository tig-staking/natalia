package com.tigstaking.natalia.game.location

import com.tigstaking.natalia.game.Coordinates

data class DeviceLocation(
    val coordinates: Coordinates,
    val accuracyMeters: Float,
    val capturedAtMillis: Long,
)

class LocationPermissionRequiredException : IllegalStateException("Allow location access while using the app")
class LocationServicesDisabledException : IllegalStateException("Turn on location services and try again")
class CurrentLocationUnavailableException : IllegalStateException("Current location is not available yet")

object LocationFixPolicy {
    private const val MAX_AGE_MILLIS = 30_000L
    private const val MAX_FUTURE_SKEW_MILLIS = 15_000L

    fun validate(fix: DeviceLocation, nowMillis: Long = System.currentTimeMillis()): DeviceLocation {
        val point = fix.coordinates
        if (!point.latitude.isFinite() || point.latitude !in -90.0..90.0 ||
            !point.longitude.isFinite() || point.longitude !in -180.0..180.0 ||
            !fix.accuracyMeters.isFinite() || fix.accuracyMeters <= 0f
        ) throw CurrentLocationUnavailableException()

        val ageMillis = nowMillis - fix.capturedAtMillis
        if (ageMillis > MAX_AGE_MILLIS || ageMillis < -MAX_FUTURE_SKEW_MILLIS) {
            throw CurrentLocationUnavailableException()
        }
        return fix
    }
}
