package com.tigstaking.natalia

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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tigstaking.natalia.game.Coordinates
import com.tigstaking.natalia.game.Proximity
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.fillOutlineColor
import org.maplibre.android.style.sources.GeoJsonSource
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val OPENFREEMAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"

internal data class JourneyMapStop(
    val number: Int,
    val name: String,
    val coordinates: Coordinates,
    val state: String,
    val color: Int,
)

@Composable
internal fun StreetMap(
    poi: Coordinates,
    user: Coordinates?,
    radiusMeters: Int,
    stops: List<JourneyMapStop>,
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
    var stopScreenPoints by remember(mapView) { mutableStateOf(emptyList<Offset>()) }

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
                        readyMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(poi.latitude, poi.longitude), 14.7))
                        map = readyMap
                        styleLoaded = true
                        mapMessage = ""
                    }
                }
                mapView
            },
            modifier = Modifier.matchParentSize(),
        )
        if (!styleLoaded && mapMessage == "Mapa ulic wymaga połączenia z internetem.") {
            OfflineCoordinatePreview(poi, user, radiusMeters, Modifier.matchParentSize())
        }
        if (mapMessage.isNotBlank()) {
            Text(
                text = mapMessage,
                color = ComposeColor.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.BottomCenter)
                    .background(ComposeColor(0xBB162238))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Canvas(modifier = Modifier.matchParentSize()) {
            stops.zip(stopScreenPoints).forEach { (stop, point) ->
                if (point.x !in 0f..size.width || point.y !in 0f..size.height) return@forEach
                val center = point
                val radius = 21.dp.toPx()
                drawCircle(ComposeColor.White, radius = radius + 4.dp.toPx(), center = center)
                drawCircle(ComposeColor(0xFFFF786B), radius = radius, center = center)
                drawCircle(ComposeColor.White, radius = radius - 5.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
                drawContext.canvas.nativeCanvas.drawText(
                    stop.number.toString().padStart(2, '0'),
                    center.x,
                    center.y + 5.dp.toPx(),
                    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.rgb(38, 51, 71)
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 12.dp.toPx()
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    },
                )
            }
        }
    }

    LaunchedEffect(map, styleLoaded, poi, user, radiusMeters, stops) {
        val readyMap = map?.takeIf { styleLoaded } ?: return@LaunchedEffect
        val style = readyMap.style ?: return@LaunchedEffect
        listOf("natalia-route-stop-circles", "natalia-route-stops", "natalia-gps-dot", "natalia-gps", "natalia-geofence-fill", "natalia-geofence").forEach { id ->
            style.removeLayer(id)
            style.removeSource(id)
        }
        style.addSource(GeoJsonSource("natalia-geofence", geofenceFeature(poi, radiusMeters)))
        style.addLayer(
            FillLayer("natalia-geofence-fill", "natalia-geofence").withProperties(
                fillColor(android.graphics.Color.rgb(111, 97, 237)),
                fillOpacity(0.11f),
                fillOutlineColor(android.graphics.Color.rgb(90, 75, 220)),
            ),
        )
        user?.takeIf { Proximity.distanceMeters(it, poi) > 25.0 }?.let { location ->
            style.addSource(GeoJsonSource("natalia-gps", pointFeature(location)))
            style.addLayer(
                CircleLayer("natalia-gps-dot", "natalia-gps").withProperties(
                    circleRadius(10f),
                    circleColor(android.graphics.Color.rgb(42, 130, 224)),
                    circleStrokeColor(android.graphics.Color.WHITE),
                    circleStrokeWidth(3f),
                ),
            )
        }
    }

    DisposableEffect(map, styleLoaded, stops) {
        val readyMap = map?.takeIf { styleLoaded }
        if (readyMap == null) {
            stopScreenPoints = emptyList()
            onDispose {}
        } else {
            fun updateStopScreenPoints() {
                stopScreenPoints = stops.map { stop ->
                    val screen = readyMap.projection.toScreenLocation(LatLng(stop.coordinates.latitude, stop.coordinates.longitude))
                    Offset(screen.x.toFloat(), screen.y.toFloat())
                }
            }
            val moveListener = MapLibreMap.OnCameraMoveListener { updateStopScreenPoints() }
            val idleListener = MapLibreMap.OnCameraIdleListener { updateStopScreenPoints() }
            updateStopScreenPoints()
            readyMap.addOnCameraMoveListener(moveListener)
            readyMap.addOnCameraIdleListener(idleListener)
            onDispose {
                readyMap.removeOnCameraMoveListener(moveListener)
                readyMap.removeOnCameraIdleListener(idleListener)
            }
        }
    }
}

private fun pointFeature(location: Coordinates): String =
    "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[${location.longitude},${location.latitude}]}}"

private fun geofenceFeature(center: Coordinates, radiusMeters: Int): String {
    val coordinates = geofencePolygon(center, radiusMeters).joinToString(",") {
        "[${it.longitude},${it.latitude}]"
    }
    return "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[$coordinates]]}}"
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
