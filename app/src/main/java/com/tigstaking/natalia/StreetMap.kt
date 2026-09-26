package com.tigstaking.natalia

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tigstaking.natalia.game.Coordinates
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val OPENFREEMAP_STYLE = "https://tiles.openfreemap.org/styles/positron"

@Composable
internal fun StreetMap(
    poi: Coordinates,
    user: Coordinates?,
    radiusMeters: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(context) {
        MapLibre.getInstance(context.applicationContext)
        MapView(context).apply {
            id = android.view.View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            onCreate(Bundle())
        }
    }
    var map by remember(mapView) { mutableStateOf<MapLibreMap?>(null) }
    var styleLoaded by remember(mapView) { mutableStateOf(false) }
    var mapMessage by remember(mapView) { mutableStateOf("Ładowanie mapy ulic…") }

    LaunchedEffect(mapView, styleLoaded) {
        if (!styleLoaded) {
            delay(12_000)
            if (!styleLoaded) mapMessage = "Mapa ulic wymaga połączenia z internetem."
        }
    }

    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(280.dp)) {
        AndroidView(
            factory = { _ ->
                mapView.getMapAsync { readyMap ->
                    readyMap.uiSettings.isLogoEnabled = true
                    readyMap.uiSettings.isAttributionEnabled = true
                    readyMap.uiSettings.isCompassEnabled = true
                    readyMap.setStyle(OPENFREEMAP_STYLE) {
                        readyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(poi.latitude, poi.longitude), 15.0))
                        map = readyMap
                        styleLoaded = true
                        mapMessage = "Mapa ulic gotowa"
                    }
                }
                mapView
            },
            modifier = Modifier.matchParentSize(),
        )
        if (!styleLoaded && mapMessage == "Mapa ulic wymaga połączenia z internetem.") {
            OfflineCoordinatePreview(poi, user, radiusMeters, Modifier.matchParentSize())
        }
        Text(
            text = mapMessage,
            color = ComposeColor.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.BottomCenter)
                .background(ComposeColor(0xBB162238))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }

    LaunchedEffect(map, styleLoaded, poi, user, radiusMeters) {
        val readyMap = map?.takeIf { styleLoaded } ?: return@LaunchedEffect
        readyMap.clear()
        readyMap.addPolygon(
            PolygonOptions()
                .addAll(geofencePolygon(poi, radiusMeters))
                .fillColor(Color.rgb(80, 110, 255))
                .strokeColor(Color.rgb(62, 86, 225))
                .alpha(0.18f),
        )
        readyMap.addMarker(
            MarkerOptions().position(LatLng(poi.latitude, poi.longitude)).title("Miejsce misji"),
        )
        user?.let { location ->
            readyMap.addMarker(
                MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("Twoja pozycja GPS"),
            )
        }
    }
}

@Composable
private fun OfflineCoordinatePreview(
    poi: Coordinates,
    user: Coordinates?,
    radiusMeters: Int,
    modifier: Modifier,
) {
    Canvas(modifier) {
        drawRect(androidx.compose.ui.graphics.Color(0xFFE7F1E3))
        val roadColor = androidx.compose.ui.graphics.Color(0xFFFCFCF7)
        for (index in 1..4) {
            val x = size.width * index / 5f
            val y = size.height * index / 5f
            drawLine(roadColor, Offset(x, 0f), Offset(x, size.height), 13f)
            drawLine(roadColor, Offset(0f, y), Offset(size.width, y), 12f)
        }
        val target = Offset(size.width / 2f, size.height / 2f)
        val metersPerPixel = 2.5
        drawCircle(androidx.compose.ui.graphics.Color(0x55607D8B), radiusMeters / metersPerPixel.toFloat(), target)
        drawCircle(androidx.compose.ui.graphics.Color(0xFF294C60), 11f, target)
        drawCircle(androidx.compose.ui.graphics.Color.White, 4f, target)
        if (user != null) {
            val latitudeMeters = (user.latitude - poi.latitude) * 111_320.0
            val longitudeMeters = (user.longitude - poi.longitude) * 111_320.0 * cos(Math.toRadians(poi.latitude))
            val point = Offset(
                target.x + (longitudeMeters / metersPerPixel).toFloat(),
                target.y - (latitudeMeters / metersPerPixel).toFloat(),
            )
            if (point.x in 0f..size.width && point.y in 0f..size.height) {
                drawCircle(androidx.compose.ui.graphics.Color.White, 12f, point)
                drawCircle(androidx.compose.ui.graphics.Color(0xFF3186D5), 8f, point)
            }
        }
    }
}

private fun geofencePolygon(center: Coordinates, radiusMeters: Int): List<LatLng> {
    val earthRadiusMeters = 6_371_000.0
    val angularRadius = radiusMeters / earthRadiusMeters
    val centerLatitude = Math.toRadians(center.latitude)
    val centerLongitude = Math.toRadians(center.longitude)
    return (0..48).map { index ->
        val bearing = 2.0 * Math.PI * index / 48
        val latitude = centerLatitude + angularRadius * cos(bearing)
        val longitude = centerLongitude + angularRadius * sin(bearing) / cos(centerLatitude)
        LatLng(Math.toDegrees(latitude), Math.toDegrees(longitude))
    }
}
