package com.tigstaking.natalia.game.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tigstaking.natalia.game.Coordinates
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun interface LocationProvider {
    suspend fun currentLocation(): DeviceLocation
}

class FusedLocationProvider(context: Context) : LocationProvider {
    private val appContext = context.applicationContext
    private val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): DeviceLocation {
        val fineGranted = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) throw LocationPermissionRequiredException()

        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!enabled) throw LocationServicesDisabledException()

        val request = CurrentLocationRequest.Builder()
            .setPriority(if (fineGranted) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .setDurationMillis(15_000)
            .build()
        val cancellation = CancellationTokenSource()
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancellation.cancel() }
            fusedClient.getCurrentLocation(request, cancellation.token)
                .addOnSuccessListener { location ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    if (location == null) {
                        continuation.resumeWithException(CurrentLocationUnavailableException())
                    } else {
                        val validated = runCatching {
                            LocationFixPolicy.validate(DeviceLocation(
                                coordinates = Coordinates(location.latitude, location.longitude),
                                accuracyMeters = location.accuracy,
                                capturedAtMillis = location.time,
                            ))
                        }.getOrElse { error ->
                            continuation.resumeWithException(error)
                            return@addOnSuccessListener
                        }
                        continuation.resume(validated)
                    }
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
        }
    }
}
