package com.forestry.counter.presentation.screens.forestry

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.forestry.counter.R
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.domain.location.OfflineTileManager
import com.forestry.counter.domain.location.TreeNavigator
import com.forestry.counter.domain.location.WktUtils
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.model.Tige
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.repository.ParcelleRepository
import com.forestry.counter.domain.repository.TigeRepository
import com.forestry.counter.presentation.theme.Elevation
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.Space
import com.forestry.counter.presentation.theme.Touch
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private const val TAG = "MapRechercheScreen"

/**
 * Mode Recherche de l'onglet Carte — nouvelle base (esprit QField).
 *
 * Reprend le moteur de rendu MapLibre existant (fiable, ce n'est pas lui
 * qui posait problème) mais avec une interface entièrement repensée : une
 * seule barre d'outils flottante en bas, cibles tactiles 64dp, plus de
 * panneaux qui se chevauchent. V1 volontairement resserrée sur l'essentiel
 * (calques, mesure, légende, position, tiges géolocalisées, navigation
 * vers une tige) — tracé GPS de parcelle et shapefile reviendront sur
 * cette même base dans une passe dédiée plutôt que d'être portés tels
 * quels depuis l'ancien écran.
 */
@Composable
fun MapRechercheScreen(
    parcelleId: String,
    tigeRepository: TigeRepository,
    essenceRepository: EssenceRepository? = null,
    parcelleRepository: ParcelleRepository? = null,
    preferencesManager: UserPreferencesManager,
    offlineTileManager: OfflineTileManager? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val tigesFlow = remember(parcelleId) {
        when {
            parcelleId == "none" -> flowOf(emptyList<Tige>())
            parcelleId == "all" -> tigeRepository.getAllTiges()
            parcelleId.startsWith("forest_") -> {
                val forestId = parcelleId.removePrefix("forest_")
                if (parcelleRepository != null) {
                    parcelleRepository.getParcellesByForest(forestId).flatMapLatest { parcelles ->
                        if (parcelles.isEmpty()) flowOf(emptyList())
                        else combine(parcelles.map { tigeRepository.getTigesByParcelle(it.id) }) { it.flatMap { l -> l } }
                    }
                } else flowOf(emptyList())
            }
            else -> tigeRepository.getTigesByParcelle(parcelleId)
        }
    }
    val tiges by tigesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val essences by (essenceRepository?.getAllEssences() ?: flowOf(emptyList<Essence>()))
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val essenceMap = remember(essences) { essences.associateBy { it.code.uppercase() } }

    val mapLastLayerKey by preferencesManager.mapLastLayerKey.collectAsStateWithLifecycle(initialValue = "PLAN_IGN")
    val mapOnlyReliableGps by preferencesManager.mapOnlyReliableGps.collectAsStateWithLifecycle(initialValue = false)
    val mapReliableGpsThresholdM by preferencesManager.mapReliableGpsThresholdM.collectAsStateWithLifecycle(initialValue = 8f)

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) hasLocationPermission = true }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    remember(context) {
        try { Mapbox.getInstance(context) } catch (e: Throwable) { Log.w(TAG, "Mapbox.getInstance failed", e) }
        com.forestry.counter.domain.location.MapLibreHttpConfig.configure(context)
        true
    }

    @Suppress("NAME_SHADOWING")
    val offlineTileManager = offlineTileManager ?: remember(context) { OfflineTileManager(context) }
    val offlineProgress by offlineTileManager.downloadProgress.collectAsStateWithLifecycle()
    var hasOfflineTilesState by remember { mutableStateOf(offlineTileManager.hasOfflineTiles()) }
    LaunchedEffect(offlineProgress) {
        if (offlineProgress?.isComplete == true && offlineProgress?.error == null) {
            hasOfflineTilesState = offlineTileManager.hasOfflineTiles()
        }
    }

    val geoTiges = remember(tiges) {
        tiges.mapNotNull { t ->
            val (lon, lat, _) = WktUtils.parsePointZ(t.gpsWkt)
            if (lon != null && lat != null) Triple(t, lon, lat) else null
        }
    }
    val displayedGeoTiges = remember(geoTiges, mapOnlyReliableGps, mapReliableGpsThresholdM) {
        if (!mapOnlyReliableGps) geoTiges
        else geoTiges.filter { (t, _, _) -> (t.precisionM ?: Double.MAX_VALUE) <= mapReliableGpsThresholdM.toDouble() }
    }
    val withGps = displayedGeoTiges.size
    val total = tiges.size

    val essenceColors = remember(displayedGeoTiges) {
        val map = mutableMapOf<String, Int>()
        var idx = 0
        displayedGeoTiges.forEach { (t, _, _) -> map.getOrPut(t.essenceCode.uppercase()) { ESSENCE_COLOR_PALETTE[idx++ % ESSENCE_COLOR_PALETTE.size] } }
        map
    }
    val essenceCounts = remember(displayedGeoTiges) {
        displayedGeoTiges.groupBy { it.first.essenceCode.uppercase() }.mapValues { it.value.size }
    }
    var hiddenEssences by remember { mutableStateOf(emptySet<String>()) }
    val filteredGeoTiges = remember(displayedGeoTiges, hiddenEssences) {
        if (hiddenEssences.isEmpty()) displayedGeoTiges
        else displayedGeoTiges.filter { (t, _, _) -> t.essenceCode.uppercase() !in hiddenEssences }
    }

    var mapReady by remember { mutableStateOf(false) }
    var mapLibreMap by remember { mutableStateOf<MapboxMap?>(null) }
    val initialLayerIdx = remember(mapLastLayerKey) { MAP_LAYERS.indexOfFirst { it.key == mapLastLayerKey }.takeIf { it >= 0 } ?: 0 }
    var currentLayerIdx by remember(mapLastLayerKey) { mutableIntStateOf(initialLayerIdx) }
    var showLayerPicker by remember { mutableStateOf(false) }
    var showLegend by remember { mutableStateOf(false) }
    var tigeTapAttached by remember { mutableStateOf(false) }
    var tappedTree by remember { mutableStateOf<TappedTreeInfo?>(null) }
    var dismissedGpsBanner by remember { mutableStateOf(false) }

    // ── Outil de mesure ──
    val measureActiveState = remember { mutableStateOf(false) }
    var measureActive by measureActiveState
    val measurePointsState = remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var measurePoints by measurePointsState
    var measureMode by remember { mutableStateOf(MeasureMode.DISTANCE) }
    var measureDistUnit by remember { mutableStateOf(MeasureDistUnit.M) }
    var measureAreaUnit by remember { mutableStateOf(MeasureAreaUnit.HA) }
    var measureColor by remember { mutableStateOf(MEASURE_COLORS[0]) }
    var showSavedMeasuresPanel by remember { mutableStateOf(false) }
    var showMeasureSaveDialog by remember { mutableStateOf(false) }
    var measureSaveName by remember { mutableStateOf("") }

    val treeNavigator = remember(context) { TreeNavigator(context) }
    val navState by treeNavigator.state.collectAsStateWithLifecycle()
    DisposableEffect(Unit) { onDispose { treeNavigator.stopNavigation() } }

    // Tracé GPS de parcelle : hors périmètre v1, tracer non-actif conservé pour
    // satisfaire la signature partagée de MapRenderEffects.
    val gpsTracer = remember(context) { com.forestry.counter.domain.location.GpsParcelTracer(context) }
    val traceState by gpsTracer.state.collectAsStateWithLifecycle()

    fun switchLayer(index: Int) {
        currentLayerIdx = index
        val map = mapLibreMap ?: return
        val layer = MAP_LAYERS.getOrElse(index) { MAP_LAYERS[0] }
        scope.launch { preferencesManager.setMapLastLayerKey(layer.key) }
        val styleJson = if (layer.key == "OFFLINE_LOCAL" && offlineTileManager.hasOfflineTiles()) {
            offlineTileManager.buildOfflineStyle(offlineTileManager.downloadedLayerCount().coerceAtLeast(1))
        } else layer.styleJson
        try {
            map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                enableLocationComponent(map, style, context)
                renderTigesOnMap(style, filteredGeoTiges, essenceMap, essenceColors)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Style switch failed", e)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ── Carte MapLibre ──
        val lifecycleOwner = LocalLifecycleOwner.current
        var mapError by remember { mutableStateOf(false) }
        val mapView = remember {
            try { MapView(context) } catch (e: Throwable) { mapError = true; null }
        }

        if (mapView != null && !mapError) {
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    try {
                        when (event) {
                            Lifecycle.Event.ON_START -> mapView.onStart()
                            Lifecycle.Event.ON_RESUME -> mapView.onResume()
                            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                            Lifecycle.Event.ON_STOP -> mapView.onStop()
                            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                            else -> {}
                        }
                    } catch (e: Throwable) { Log.w(TAG, "mapView lifecycle event failed", e) }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    try { mapView.onDestroy() } catch (e: Throwable) { Log.w(TAG, "mapView.onDestroy failed", e) }
                }
            }

            AndroidView(
                factory = {
                    mapView.apply {
                        try {
                            onCreate(null)
                            getMapAsync { map ->
                                try {
                                    val selectedLayer = MAP_LAYERS.getOrElse(currentLayerIdx) { MAP_LAYERS[0] }
                                    val initStyleJson = if (selectedLayer.key == "OFFLINE_LOCAL" && offlineTileManager.hasOfflineTiles()) {
                                        offlineTileManager.buildOfflineStyle(offlineTileManager.downloadedLayerCount().coerceAtLeast(1))
                                    } else selectedLayer.styleJson
                                    map.setStyle(Style.Builder().fromJson(initStyleJson)) { style ->
                                        mapLibreMap = map
                                        mapReady = true
                                        enableLocationComponent(map, style, context)
                                        renderTigesOnMap(style, filteredGeoTiges, essenceMap, essenceColors)
                                        if (!tigeTapAttached) {
                                            map.addOnMapClickListener { latLng ->
                                                if (measureActiveState.value) {
                                                    measurePointsState.value = measurePointsState.value + latLng
                                                    true
                                                } else false
                                            }
                                            attachTigeTapInfo(map, context) { info -> tappedTree = info }
                                            tigeTapAttached = true
                                        }
                                    }
                                    map.uiSettings.apply {
                                        isCompassEnabled = true
                                        isRotateGesturesEnabled = true
                                        isZoomGesturesEnabled = true
                                        isScrollGesturesEnabled = true
                                        isTiltGesturesEnabled = selectedLayer.hasTerrain
                                        setAttributionMargins(16, 0, 0, 16)
                                    }
                                } catch (e: Throwable) {
                                    Log.w(TAG, "map style setup failed, fallback", e)
                                    map.setStyle(Style.Builder().fromJson(offlineLocalStyle("Offline fallback"))) { style ->
                                        mapLibreMap = map
                                        mapReady = true
                                        enableLocationComponent(map, style, context)
                                    }
                                }
                            }
                        } catch (e: Throwable) { Log.w(TAG, "map style setup failed", e) }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Fallback si le SDK carte échoue à s'initialiser.
            Image(
                painter = painterResource(id = R.drawable.forest_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        MapRenderEffects(
            mapLibreMap = mapLibreMap,
            mapReady = mapReady,
            hasLocationPermission = hasLocationPermission,
            displayedGeoTiges = displayedGeoTiges,
            filteredGeoTiges = filteredGeoTiges,
            geoTiges = geoTiges,
            essenceColors = essenceColors,
            essenceMap = essenceMap,
            traceState = traceState,
            gpsTracer = gpsTracer,
            measurePoints = measurePoints,
            measureMode = measureMode,
            measureColor = measureColor,
        )

        // ── En-tête compact ──
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = GsShape.field,
            shadowElevation = Elevation.overlay,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(Space.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = Space.sm)) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(Touch.field)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Column {
                    Text(stringResource(R.string.carte_mode_recherche_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (withGps > 0) {
                        Text(
                            stringResource(R.string.map_subtitle_stems_format, withGps, total),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (offlineProgress != null && offlineProgress?.isComplete != true) {
                    CircularProgressIndicator(modifier = Modifier.size(Space.lg).padding(start = Space.xs))
                }
            }
        }

        MapLayerPicker(
            visible = showLayerPicker,
            currentLayerIdx = currentLayerIdx,
            hasOfflineTilesState = hasOfflineTilesState,
            offlineTileManager = offlineTileManager,
            onLayerSelected = { index -> switchLayer(index); showLayerPicker = false },
            onDismiss = { showLayerPicker = false },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = Touch.fieldPrimary + Space.xl, start = Space.xs, end = Space.xs),
        )

        MapLegendPanel(
            visible = showLegend,
            essenceColors = essenceColors,
            essenceCounts = essenceCounts,
            hiddenEssences = hiddenEssences,
            essenceMap = essenceMap,
            onToggleEssence = { code, hide -> hiddenEssences = if (hide) hiddenEssences + code else hiddenEssences - code },
            modifier = Modifier.align(Alignment.TopEnd).padding(top = Touch.fieldPrimary + Space.xl, end = Space.sm),
        )

        MapGpsWarningBanner(
            geoTiges = geoTiges,
            dismissed = dismissedGpsBanner,
            onDismiss = { dismissedGpsBanner = true },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = Touch.fieldPrimary + Space.xl),
        )

        MapTigeInfoPanel(
            tappedTree = tappedTree,
            navActive = navState.isActive,
            hasLocationPermission = hasLocationPermission,
            onRequestPermission = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
            onStartNavigation = { info ->
                treeNavigator.startNavigation(
                    TreeNavigator.Target(
                        tigeId = "", essenceName = info.essenceName, essenceCode = info.essenceCode,
                        diamCm = info.diamCm ?: 0.0, hauteurM = info.hauteurM, lat = info.lat, lon = info.lon,
                    )
                )
                tappedTree = null
            },
            onDismiss = { tappedTree = null },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = Touch.fieldPrimary + Space.xl, start = Space.md, end = Space.md),
        )

        MapNavigationOverlay(
            navState = navState,
            onStopNavigation = { treeNavigator.stopNavigation() },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = Touch.fieldPrimary + Space.xl, start = Space.md, end = Space.md),
        )

        MapOfflineProgressBar(
            progress = offlineProgress,
            onClearProgress = { offlineTileManager.clearProgress() },
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = Space.md, vertical = Touch.fieldPrimary + Space.xl),
        )

        MapNoGpsMessage(
            total = total, withGps = withGps, geoTigesSize = geoTiges.size,
            mapOnlyReliableGps = mapOnlyReliableGps, mapReliableGpsThresholdM = mapReliableGpsThresholdM,
            onDismiss = { dismissedGpsBanner = true },
            modifier = Modifier.align(Alignment.Center).padding(Space.xl),
        )
        MapEmptyMessage(
            total = total, onDismiss = { dismissedGpsBanner = true }, isGlobalScope = parcelleId == "all",
            modifier = Modifier.align(Alignment.Center).padding(Space.xl),
        )

        // ── Panneau mesure (au-dessus de la barre d'outils, un seul endroit) ──
        AnimatedVisibility(
            visible = measureActive || measurePoints.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Touch.fieldPrimary + Space.xl),
            enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { it / 2 },
        ) {
            MapMeasurePanel(
                state = MapMeasurePanelState(
                    isActive = measureActive, points = measurePoints, mode = measureMode,
                    distUnit = measureDistUnit, areaUnit = measureAreaUnit, color = measureColor,
                    showSavedPanel = showSavedMeasuresPanel,
                ),
                traceHasContent = false,
                context = context,
                onEvent = { event ->
                    when (event) {
                        is MapMeasurePanelEvent.SetActive -> measureActive = event.active
                        is MapMeasurePanelEvent.SetPoints -> measurePoints = event.points
                        is MapMeasurePanelEvent.SetMode -> measureMode = event.mode
                        is MapMeasurePanelEvent.SetDistUnit -> measureDistUnit = event.unit
                        is MapMeasurePanelEvent.SetAreaUnit -> measureAreaUnit = event.unit
                        is MapMeasurePanelEvent.SetColor -> measureColor = event.color
                        MapMeasurePanelEvent.ToggleSavedPanel -> showSavedMeasuresPanel = !showSavedMeasuresPanel
                        MapMeasurePanelEvent.SaveRequest -> { measureSaveName = ""; showMeasureSaveDialog = true }
                        is MapMeasurePanelEvent.LoadSavedMeasure -> {
                            measureMode = event.mode; measurePoints = event.points; showSavedMeasuresPanel = false
                        }
                    }
                },
            )
        }

        // ── Barre d'outils unique, grande, centrée en bas ──
        RechercheToolbar(
            measureActive = measureActive,
            layersActive = showLayerPicker,
            legendActive = showLegend,
            hasGeoTiges = displayedGeoTiges.isNotEmpty(),
            onToggleMeasure = {
                measureActive = !measureActive
                if (!measureActive) measurePoints = emptyList()
            },
            onToggleLayers = { showLayerPicker = !showLayerPicker; if (showLayerPicker) showLegend = false },
            onToggleLegend = { showLegend = !showLegend; if (showLegend) showLayerPicker = false },
            onLocate = {
                val map = mapLibreMap ?: return@RechercheToolbar
                if (!hasLocationPermission) {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    return@RechercheToolbar
                }
                try {
                    val loc = map.locationComponent.lastKnownLocation
                    if (loc != null) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 18.0), 600)
                    }
                } catch (e: Throwable) { Log.w(TAG, "animateCamera to GPS location failed", e) }
            },
            onDownloadOffline = {
                val map = mapLibreMap ?: return@RechercheToolbar
                val bounds = map.projection.visibleRegion.latLngBounds
                val layer = MAP_LAYERS.getOrElse(currentLayerIdx) { MAP_LAYERS[0] }
                offlineTileManager.downloadRegion(
                    name = parcelleId,
                    latSouth = bounds.southWest.latitude, latNorth = bounds.northEast.latitude,
                    lonWest = bounds.southWest.longitude, lonEast = bounds.northEast.longitude,
                    tileUrlTemplates = layer.tileUrls,
                    minZoom = map.cameraPosition.zoom.toInt().coerceAtLeast(8),
                    maxZoom = (map.cameraPosition.zoom.toInt() + 4).coerceAtMost(17),
                )
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Space.lg),
        )
    }

    MapMeasureSaveDialog(
        visible = showMeasureSaveDialog,
        measureMode = measureMode,
        measurePoints = measurePoints,
        measureSaveName = measureSaveName,
        onMeasureSaveNameChange = { measureSaveName = it },
        onDismiss = { showMeasureSaveDialog = false },
        context = context,
    )
}

/**
 * Barre d'outils unique du mode Recherche : une seule rangée de grands
 * boutons (Touch.fieldPrimary), plus de FABs éparpillés ni de contrôles
 * de zoom séparés — le pincer-zoomer sur la carte suffit, comme sur
 * n'importe quelle appli de carte moderne.
 */
@Composable
private fun RechercheToolbar(
    measureActive: Boolean,
    layersActive: Boolean,
    legendActive: Boolean,
    hasGeoTiges: Boolean,
    onToggleMeasure: () -> Unit,
    onToggleLayers: () -> Unit,
    onToggleLegend: () -> Unit,
    onLocate: () -> Unit,
    onDownloadOffline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        shape = GsShape.pill,
        shadowElevation = Elevation.overlay,
    ) {
        Row(
            modifier = Modifier.padding(Space.xs),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RechercheToolbarButton(Icons.Default.Layers, stringResource(R.string.map_style), layersActive, onToggleLayers)
            RechercheToolbarButton(Icons.Default.Straighten, stringResource(R.string.measure_tool_title), measureActive, onToggleMeasure)
            RechercheToolbarButton(Icons.AutoMirrored.Filled.FormatListBulleted, stringResource(R.string.map_legend), legendActive, onToggleLegend, enabled = hasGeoTiges)
            RechercheToolbarButton(Icons.Default.GpsFixed, stringResource(R.string.map_my_location), false, onLocate)
            RechercheToolbarButton(Icons.Default.CloudDownload, stringResource(R.string.offline_download), false, onDownloadOffline)
        }
    }
}

@Composable
private fun RechercheToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(Touch.fieldPrimary),
        shape = GsShape.pill,
        color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = when {
            active -> MaterialTheme.colorScheme.onPrimary
            enabled -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}
