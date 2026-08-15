package com.forestry.counter.presentation.screens.forestry

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import com.forestry.counter.domain.location.GpsParcelTracer
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.model.Tige
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.coroutines.delay

/**
 * Side-effects de rendu sur la carte MapLibre.
 */
@Composable
internal fun MapRenderEffects(
    mapLibreMap: MapboxMap?,
    mapReady: Boolean,
    hasLocationPermission: Boolean,
    displayedGeoTiges: List<Triple<Tige, Double, Double>>,
    filteredGeoTiges: List<Triple<Tige, Double, Double>>,
    geoTiges: List<Triple<Tige, Double, Double>>,
    essenceColors: Map<String, Int>,
    essenceMap: Map<String, Essence>,
    traceState: GpsParcelTracer.TraceState,
    gpsTracer: GpsParcelTracer,
    measurePoints: List<LatLng>,
    measureMode: MeasureMode,
    measureColor: androidx.compose.ui.graphics.Color
) {
    MapInitialGpsEffect(mapLibreMap, mapReady, hasLocationPermission, displayedGeoTiges)
    MapTigesRenderEffect(mapLibreMap, mapReady, filteredGeoTiges, displayedGeoTiges, essenceColors, essenceMap)
    MapTraceRenderEffect(mapLibreMap, mapReady, traceState, gpsTracer)
    MapMeasureRenderEffect(mapLibreMap, mapReady, measurePoints, measureMode, measureColor)
}

@Composable
private fun MapInitialGpsEffect(
    mapLibreMap: MapboxMap?,
    mapReady: Boolean,
    hasLocationPermission: Boolean,
    displayedGeoTiges: List<Triple<Tige, Double, Double>>
) {
    LaunchedEffect(mapReady, hasLocationPermission) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!mapReady) return@LaunchedEffect
        delay(600)
        if (displayedGeoTiges.isNotEmpty()) return@LaunchedEffect
        try {
            val lc = map.locationComponent
            val lastLoc = lc.lastKnownLocation
            if (lastLoc != null) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(lastLoc.latitude, lastLoc.longitude), 13.0
                    ), 1000
                )
            } else {
                // Ni tige ni fix GPS : repli sur un cadrage France plutôt
                // que la position par défaut de MapLibre, qui peut tomber
                // hors de la couverture des fonds de carte IGN (fond noir).
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(46.6, 2.5), 5.0), 800
                )
            }
        } catch (_: Throwable) { /* permission pas encore accordée */ }
    }
}

@Composable
private fun MapTigesRenderEffect(
    mapLibreMap: MapboxMap?,
    mapReady: Boolean,
    filteredGeoTiges: List<Triple<Tige, Double, Double>>,
    displayedGeoTiges: List<Triple<Tige, Double, Double>>,
    essenceColors: Map<String, Int>,
    essenceMap: Map<String, Essence>
) {
    LaunchedEffect(mapReady, filteredGeoTiges, essenceColors) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!mapReady) return@LaunchedEffect

        map.getStyle { style ->
            renderTigesOnMap(style, filteredGeoTiges, essenceMap, essenceColors)
        }

        if (displayedGeoTiges.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            displayedGeoTiges.forEach { (_, lon, lat) ->
                boundsBuilder.include(LatLng(lat, lon))
            }
            try {
                val bounds = boundsBuilder.build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 800)
            } catch (_: Throwable) {
                val first = displayedGeoTiges.first()
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(first.third, first.second), 17.0),
                    800
                )
            }
        }
    }
}

@Composable
private fun MapTraceRenderEffect(
    mapLibreMap: MapboxMap?,
    mapReady: Boolean,
    traceState: GpsParcelTracer.TraceState,
    gpsTracer: GpsParcelTracer
) {
    LaunchedEffect(mapReady, traceState) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!mapReady) return@LaunchedEffect
        map.getStyle { style -> renderTraceOnMap(style, gpsTracer) }
    }
}

@Composable
private fun MapMeasureRenderEffect(
    mapLibreMap: MapboxMap?,
    mapReady: Boolean,
    measurePoints: List<LatLng>,
    measureMode: MeasureMode,
    measureColor: androidx.compose.ui.graphics.Color
) {
    LaunchedEffect(mapReady, measurePoints, measureMode, measureColor) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (!mapReady) return@LaunchedEffect
        map.getStyle { style ->
            try { renderMeasureOnMap(style, measurePoints, measureMode, measureColor.toArgb()) }
            catch (e: Throwable) { Log.w("MapRenderEffects", "renderMeasureOnMap failed", e) }
        }
    }
}
