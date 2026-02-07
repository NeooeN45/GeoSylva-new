package com.forestry.counter.presentation.screens.forestry

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.forestry.counter.R
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.domain.location.WktUtils
import com.forestry.counter.domain.model.Tige
import com.forestry.counter.domain.repository.TigeRepository
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlin.math.roundToInt

private const val OSM_STYLE = "https://tiles.openfreemap.org/styles/liberty"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    parcelleId: String,
    tigeRepository: TigeRepository,
    preferencesManager: UserPreferencesManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val tiges by tigeRepository.getTigesByParcelle(parcelleId).collectAsState(initial = emptyList())
    val animationsEnabled by preferencesManager.animationsEnabled.collectAsState(initial = true)

    // Initialiser MapLibre
    LaunchedEffect(Unit) {
        Mapbox.getInstance(context)
    }

    val geoTiges = remember(tiges) {
        tiges.mapNotNull { t ->
            val (lon, lat, _) = WktUtils.parsePointZ(t.gpsWkt)
            if (lon != null && lat != null) Triple(t, lon, lat) else null
        }
    }
    val withGps = geoTiges.size
    val total = tiges.size

    var mapReady by remember { mutableStateOf(false) }
    var mapLibreMap by remember { mutableStateOf<MapboxMap?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_parcelle_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Carte MapLibre ──
            val lifecycleOwner = LocalLifecycleOwner.current
            val mapView = remember { MapView(context) }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> mapView.onStart()
                        Lifecycle.Event.ON_RESUME -> mapView.onResume()
                        Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                        Lifecycle.Event.ON_STOP -> mapView.onStop()
                        Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    mapView.onDestroy()
                }
            }

            AndroidView(
                factory = {
                    mapView.apply {
                        getMapAsync { map ->
                            map.setStyle(OSM_STYLE) {
                                mapLibreMap = map
                                mapReady = true
                            }
                            map.uiSettings.apply {
                                isCompassEnabled = true
                                isRotateGesturesEnabled = true
                                isZoomGesturesEnabled = true
                                isScrollGesturesEnabled = true
                                isTiltGesturesEnabled = false
                                setAttributionMargins(16, 0, 0, 16)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Ajouter les marqueurs quand la carte et les données sont prêtes
            LaunchedEffect(mapReady, geoTiges) {
                val map = mapLibreMap ?: return@LaunchedEffect
                if (!mapReady || geoTiges.isEmpty()) return@LaunchedEffect

                map.annotations.clear()

                val iconFactory = IconFactory.getInstance(context)
                val essenceColors = mutableMapOf<String, Int>()
                val colorPalette = intArrayOf(
                    0xFF4CAF50.toInt(), // vert
                    0xFF2196F3.toInt(), // bleu
                    0xFFFF9800.toInt(), // orange
                    0xFF9C27B0.toInt(), // violet
                    0xFFF44336.toInt(), // rouge
                    0xFF00BCD4.toInt(), // cyan
                    0xFF795548.toInt(), // brun
                    0xFFCDDC39.toInt(), // lime
                    0xFFE91E63.toInt(), // rose
                    0xFF607D8B.toInt(), // gris-bleu
                )
                var colorIdx = 0

                val boundsBuilder = LatLngBounds.Builder()

                for ((tige, lon, lat) in geoTiges) {
                    val pos = LatLng(lat, lon)
                    boundsBuilder.include(pos)

                    val color = essenceColors.getOrPut(tige.essenceCode.uppercase()) {
                        colorPalette[colorIdx++ % colorPalette.size]
                    }

                    val radiusPx = ((tige.diamCm / 5.0).coerceIn(4.0, 20.0)).toInt()
                    val bmp = createCircleMarker(radiusPx * 2, color)
                    val icon = iconFactory.fromBitmap(bmp)

                    val snippet = buildString {
                        append("D=${tige.diamCm.roundToInt()} cm")
                        tige.hauteurM?.let { append(" · H=${it.roundToInt()} m") }
                        tige.precisionM?.let { append(" · ±${"%.1f".format(it)} m") }
                    }

                    map.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(tige.essenceCode)
                            .snippet(snippet)
                            .icon(icon)
                    )
                }

                try {
                    val bounds = boundsBuilder.build()
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, 80),
                        1000
                    )
                } catch (_: Throwable) {
                    // Si un seul point, centrer dessus
                    val first = geoTiges.first()
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(first.third, first.second), 17.0),
                        1000
                    )
                }
            }

            // ── Overlay : couverture GPS ──
            AnimatedVisibility(
                visible = total > 0,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.65f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "$withGps / $total GPS",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        val progressVal = if (total > 0) withGps.toFloat() / total.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progressVal },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }

            // ── Bouton recentrer ──
            if (geoTiges.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val map = mapLibreMap ?: return@FloatingActionButton
                        try {
                            val builder = LatLngBounds.Builder()
                            geoTiges.forEach { (_, lon, lat) -> builder.include(LatLng(lat, lon)) }
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 80), 600)
                        } catch (_: Throwable) {}
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                }
            }

            // ── Message si aucun GPS ──
            if (total > 0 && withGps == 0) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.map_no_gps_data),
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

private fun createCircleMarker(sizePx: Int, color: Int): Bitmap {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val r = sizePx / 2f
    canvas.drawCircle(r, r, r - 1f, paint)
    canvas.drawCircle(r, r, r - 1f, border)
    return bmp
}
