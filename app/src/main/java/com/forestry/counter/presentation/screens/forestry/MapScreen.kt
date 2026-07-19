package com.forestry.counter.presentation.screens.forestry

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.forestry.counter.presentation.theme.MartelageEnlever
import com.forestry.counter.presentation.theme.SemanticSuccess
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.forestry.counter.R
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.domain.geo.ShapefileOverlay
import com.forestry.counter.domain.geo.ShapefileOverlayManager
import com.forestry.counter.domain.location.GpsParcelTracer
import com.forestry.counter.domain.location.TreeNavigator
import com.forestry.counter.domain.location.WktUtils
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.model.Tige
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.repository.ParcelleRepository
import com.forestry.counter.domain.repository.TigeRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.forestry.counter.domain.location.OfflineTileManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlin.math.*
import java.io.File
import java.util.Locale

private const val TAG = "MapScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    parcelleId: String,
    tigeRepository: TigeRepository,
    essenceRepository: EssenceRepository? = null,
    parcelleRepository: ParcelleRepository? = null,
    preferencesManager: UserPreferencesManager,
    offlineTileManager: OfflineTileManager? = null,
    onNavigateBack: () -> Unit,
    initialNavLat: Double? = null,
    initialNavLon: Double? = null,
    initialNavEssence: String? = null,
    initialNavDiam: Double? = null
) {
    val context = LocalContext.current

    // Déterminer le flux de tiges selon le scope
    val tigesFlow = remember(parcelleId) {
        when {
            parcelleId == "none" -> flowOf(emptyList<Tige>())
            parcelleId == "all" -> tigeRepository.getAllTiges()
            parcelleId.startsWith("forest_") -> {
                val forestId = parcelleId.removePrefix("forest_")
                if (parcelleRepository != null) {
                    parcelleRepository.getParcellesByForest(forestId).flatMapLatest { parcelles ->
                        if (parcelles.isEmpty()) flowOf(emptyList())
                        else {
                            val flows = parcelles.map { tigeRepository.getTigesByParcelle(it.id) }
                            combine(flows) { arrays -> arrays.flatMap { it } }
                        }
                    }
                } else flowOf(emptyList())
            }
            else -> tigeRepository.getTigesByParcelle(parcelleId)
        }
    }
    val tiges by tigesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val essences by (essenceRepository?.getAllEssences()
        ?: kotlinx.coroutines.flow.flowOf(emptyList<Essence>())).collectAsStateWithLifecycle(initialValue = emptyList())
    val animationsEnabled by preferencesManager.animationsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val mapLastLayerKey by preferencesManager.mapLastLayerKey.collectAsStateWithLifecycle(initialValue = "PLAN_IGN")
    val mapShowLegendPref by preferencesManager.mapShowLegend.collectAsStateWithLifecycle(initialValue = false)
    val mapOnlyReliableGps by preferencesManager.mapOnlyReliableGps.collectAsStateWithLifecycle(initialValue = false)
    val mapReliableGpsThresholdM by preferencesManager.mapReliableGpsThresholdM.collectAsStateWithLifecycle(initialValue = 8f)

    // Permission localisation
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) hasLocationPermission = true }

    // Demander la permission au lancement si pas encore accordée
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Initialiser MapLibre de façon synchrone AVANT toute création de MapView
    remember(context) {
        try { Mapbox.getInstance(context) } catch (e: Throwable) { Log.w(TAG, "Mapbox.getInstance failed", e) }
        // Configurer le client HTTP MapLibre : cache 50MB + retry backoff + User-Agent conforme
        com.forestry.counter.domain.location.MapLibreHttpConfig.configure(context)
        true
    }

    @Suppress("NAME_SHADOWING")
    val offlineTileManager = offlineTileManager ?: remember(context) { OfflineTileManager(context) }
    val offlineProgress by offlineTileManager.downloadProgress.collectAsStateWithLifecycle()
    var showOfflineSnackbar by remember { mutableStateOf(false) }

    // Présence de tuiles hors-ligne (se met à jour après un téléchargement)
    var hasOfflineTilesState by remember { mutableStateOf(offlineTileManager.hasOfflineTiles()) }
    LaunchedEffect(offlineProgress) {
        if (offlineProgress?.isComplete == true && offlineProgress?.error == null) {
            hasOfflineTilesState = offlineTileManager.hasOfflineTiles()
        }
    }

    val essenceMap = remember(essences) { essences.associateBy { it.code.uppercase() } }

    var dismissedGpsBanner by remember { mutableStateOf(false) }

    val geoTiges = remember(tiges) {
        tiges.mapNotNull { t ->
            val (lon, lat, _) = WktUtils.parsePointZ(t.gpsWkt)
            if (lon != null && lat != null) Triple(t, lon, lat) else null
        }
    }
    val reliableGeoTiges = remember(geoTiges, mapReliableGpsThresholdM) {
        geoTiges.filter { (t, _, _) ->
            val precision = t.precisionM ?: Double.MAX_VALUE
            precision <= mapReliableGpsThresholdM.toDouble()
        }
    }
    val displayedGeoTiges = remember(geoTiges, reliableGeoTiges, mapOnlyReliableGps) {
        if (mapOnlyReliableGps) reliableGeoTiges else geoTiges
    }
    val withGps = displayedGeoTiges.size
    val total = tiges.size

    // Couleurs par essence (stables)
    val essenceColors = remember(displayedGeoTiges) {
        val map = mutableMapOf<String, Int>()
        var idx = 0
        displayedGeoTiges.forEach { (t, _, _) ->
            map.getOrPut(t.essenceCode.uppercase()) {
                ESSENCE_COLOR_PALETTE[idx++ % ESSENCE_COLOR_PALETTE.size]
            }
        }
        map
    }

    // Compteurs par essence
    val essenceCounts = remember(displayedGeoTiges) {
        displayedGeoTiges.groupBy { it.first.essenceCode.uppercase() }.mapValues { it.value.size }
    }

    // Filtre d'essences (légende cliquable)
    var hiddenEssences by remember { mutableStateOf(emptySet<String>()) }
    val filteredGeoTiges = remember(displayedGeoTiges, hiddenEssences) {
        if (hiddenEssences.isEmpty()) displayedGeoTiges
        else displayedGeoTiges.filter { (t, _, _) -> t.essenceCode.uppercase() !in hiddenEssences }
    }

    var mapReady by remember { mutableStateOf(false) }
    var mapLibreMap by remember { mutableStateOf<MapboxMap?>(null) }
    val initialLayerIdx = remember(mapLastLayerKey) {
        MAP_LAYERS.indexOfFirst { it.key == mapLastLayerKey }.takeIf { it >= 0 } ?: 0
    }
    var currentLayerIdx by remember(mapLastLayerKey) { mutableIntStateOf(initialLayerIdx) }
    var showLegend by remember(mapShowLegendPref) { mutableStateOf(mapShowLegendPref) }
    var showLayerPicker by remember { mutableStateOf(false) }
    var showCoords by remember { mutableStateOf(false) }
    var coordsText by remember { mutableStateOf("") }
    var tigeTapAttached by remember { mutableStateOf(false) }

    // ── Shapefile overlay state ──
    val scope = rememberCoroutineScope()
    val shpManager = remember { ShapefileOverlayManager(context) }
    var shpOverlay by remember { mutableStateOf<ShapefileOverlay?>(shpManager.listOverlays().firstOrNull()) }
    var shpGeoJsonFile by remember { mutableStateOf<java.io.File?>(null) }
    var showShpPanel by remember { mutableStateOf(false) }
    var shpImporting by remember { mutableStateOf(false) }
    var shpErrorMessage by remember { mutableStateOf<String?>(null) }

    // Appliquer l'overlay shapefile quand le fichier ou les paramètres changent
    fun applyCurrentShpOverlay(style: Style): String {
        val file = shpGeoJsonFile ?: return "no file"
        val ov = shpOverlay ?: return "no overlay"
        return applyShapefileOverlay(style, file, ov)
    }

    val onApplyShpOverlay = {
        val map = mapLibreMap
        if (map != null && mapReady) {
            map.getStyle { style -> applyCurrentShpOverlay(style) }
        }
    }

    // ── GPS Parcel Trace state ──
    val gpsTracer = remember(context) { GpsParcelTracer(context) }
    val traceState by gpsTracer.state.collectAsStateWithLifecycle()
    var showTraceSaveDialog by remember { mutableStateOf(false) }
    var traceName by remember { mutableStateOf("") }

    // ── Measure tool state ──
    val measureActiveState: androidx.compose.runtime.MutableState<Boolean> = remember { mutableStateOf(false) }
    var measureActive by measureActiveState
    val measurePointsState: androidx.compose.runtime.MutableState<List<LatLng>> = remember { mutableStateOf(emptyList()) }
    var measurePoints by measurePointsState
    var measureMode by remember { mutableStateOf(MeasureMode.DISTANCE) }
    var showMeasureSaveDialog by remember { mutableStateOf(false) }
    var measureSaveName by remember { mutableStateOf("") }
    var measureDistUnit by remember { mutableStateOf(MeasureDistUnit.M) }
    var measureAreaUnit by remember { mutableStateOf(MeasureAreaUnit.HA) }
    var measureColor by remember { mutableStateOf(MEASURE_COLORS[0]) }
    var showSavedMeasuresPanel by remember { mutableStateOf(false) }

    // ── Tree navigation state ──
    val treeNavigator = remember(context) { TreeNavigator(context) }
    val navState by treeNavigator.state.collectAsStateWithLifecycle()
    var tappedTree by remember { mutableStateOf<TappedTreeInfo?>(null) }

    // Cleanup navigator on dispose
    DisposableEffect(Unit) {
        onDispose {
            treeNavigator.stopNavigation()
            gpsTracer.clearTrace()
        }
    }

    // ── Auto-start navigation if initial nav params are provided ──
    LaunchedEffect(initialNavLat, initialNavLon, hasLocationPermission) {
        if (initialNavLat != null && initialNavLon != null && hasLocationPermission) {
            val target = TreeNavigator.Target(
                tigeId = "",
                essenceName = initialNavEssence ?: "?",
                essenceCode = initialNavEssence ?: "?",
                diamCm = initialNavDiam ?: 0.0,
                hauteurM = null,
                lat = initialNavLat,
                lon = initialNavLon
            )
            treeNavigator.startNavigation(target)
        }
    }

    // Résoudre le fichier GeoJSON au démarrage si un overlay existe
    LaunchedEffect(shpOverlay?.id) {
        val overlay = shpOverlay ?: run { shpGeoJsonFile = null; return@LaunchedEffect }
        shpGeoJsonFile = shpManager.getGeoJsonFile(overlay)
        Log.d(TAG, "Overlay ${overlay.id}: geojson file=${shpGeoJsonFile?.absolutePath}, exists=${shpGeoJsonFile?.exists()}")
        if (mapReady) onApplyShpOverlay()
    }

    // File picker pour importer un .zip shapefile
    val shpPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        shpImporting = true
        shpErrorMessage = null
        scope.launch {
            val overlay = shpManager.importFromUri(uri)
            shpImporting = false
            if (overlay != null) {
                shpOverlay = overlay
                shpErrorMessage = null
                Toast.makeText(
                    context,
                    context.getString(R.string.shp_import_success, overlay.featureCount),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                shpErrorMessage = context.getString(R.string.shp_import_error)
                Toast.makeText(context, context.getString(R.string.shp_import_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun switchLayer(index: Int) {
        currentLayerIdx = index
        val map = mapLibreMap ?: return
        val layer = MAP_LAYERS.getOrElse(index) { MAP_LAYERS[0] }
        scope.launch { preferencesManager.setMapLastLayerKey(layer.key) }

        // Pour la couche offline, utiliser le style avec tuiles locales si disponible
        val styleJson = if (layer.key == "OFFLINE_LOCAL" && offlineTileManager.hasOfflineTiles()) {
            offlineTileManager.buildOfflineStyle(offlineTileManager.downloadedLayerCount().coerceAtLeast(1))
        } else {
            layer.styleJson
        }

        try {
            map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                enableLocationComponent(map, style, context)
                applyCurrentShpOverlay(style)
                renderTigesOnMap(style, filteredGeoTiges, essenceMap, essenceColors)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Style switch failed", e)
            map.setStyle(Style.Builder().fromJson(offlineLocalStyle("Offline fallback"))) { style ->
                enableLocationComponent(map, style, context)
                applyCurrentShpOverlay(style)
                renderTigesOnMap(style, filteredGeoTiges, essenceMap, essenceColors)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.map_parcelle_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (withGps > 0) {
                            Text(
                                stringResource(R.string.map_subtitle_stems_format, withGps, total),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    // Télécharger tuiles hors-ligne
                    IconButton(onClick = {
                        val map = mapLibreMap
                        if (map != null) {
                            val bounds = map.projection.visibleRegion.latLngBounds
                            val layer = MAP_LAYERS.getOrElse(currentLayerIdx) { MAP_LAYERS[0] }
                            offlineTileManager.downloadRegion(
                                name = parcelleId,
                                latSouth = bounds.southWest.latitude,
                                latNorth = bounds.northEast.latitude,
                                lonWest = bounds.southWest.longitude,
                                lonEast = bounds.northEast.longitude,
                                tileUrlTemplates = layer.tileUrls,
                                minZoom = map.cameraPosition.zoom.toInt().coerceAtLeast(8),
                                maxZoom = (map.cameraPosition.zoom.toInt() + 4).coerceAtMost(17)
                            )
                        }
                    }) {
                        val op = offlineProgress
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = stringResource(R.string.offline_download),
                            tint = if (op != null && !op.isComplete)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Shapefile overlay
                    IconButton(onClick = { showShpPanel = !showShpPanel }) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = stringResource(R.string.shp_overlay),
                            tint = if (shpOverlay != null && shpOverlay?.visible == true) SemanticSuccess
                                   else if (showShpPanel) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Toggle légende
                    IconButton(onClick = {
                        val next = !showLegend
                        showLegend = next
                        scope.launch { preferencesManager.setMapShowLegend(next) }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = stringResource(R.string.map_legend),
                            tint = if (showLegend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Ouvrir sélecteur de couches
                    IconButton(onClick = { showLayerPicker = !showLayerPicker }) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = stringResource(R.string.map_style),
                            tint = if (showLayerPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
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
            var mapError by remember { mutableStateOf(false) }
            val mapView = remember {
                try {
                    MapView(context)
                } catch (e: Throwable) {
                    mapError = true
                    null
                }
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
                                        } else {
                                            selectedLayer.styleJson
                                        }
                                        map.setStyle(Style.Builder().fromJson(initStyleJson)) { style ->
                                            mapLibreMap = map
                                            mapReady = true
                                            enableLocationComponent(map, style, context)
                                            applyCurrentShpOverlay(style)
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
                                        Log.w(TAG, "Error setting selected map style, fallback to offline local", e)
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
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ── Side-effects de rendu carte ──
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
                measureColor = measureColor
            )

            // ── Panneau sélecteur de couches (par catégorie) ──
            MapLayerPicker(
                visible = showLayerPicker,
                currentLayerIdx = currentLayerIdx,
                hasOfflineTilesState = hasOfflineTilesState,
                offlineTileManager = offlineTileManager,
                onLayerSelected = { index ->
                    switchLayer(index)
                    showLayerPicker = false
                },
                onDismiss = { showLayerPicker = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp, start = 6.dp, end = 6.dp)
            )

            // ── Panneau shapefile overlay ──
            MapShapefilePanel(
                state = MapShapefilePanelState(
                    overlay = shpOverlay,
                    isVisible = showShpPanel
                ),
                onEvent = { event ->
                    when (event) {
                        is MapShapefilePanelEvent.SetOverlay -> shpOverlay = event.overlay
                        is MapShapefilePanelEvent.DeleteOverlay -> {
                            shpOverlay = null
                            shpGeoJsonFile = null
                        }
                    }
                },
                shpManager = shpManager,
                context = context,
                shpImporting = shpImporting,
                shpErrorMessage = shpErrorMessage,
                onImportRequest = {
                    shpPickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
                },
                onApplyOverlay = onApplyShpOverlay,
                onDismissPanel = { showShpPanel = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp, start = 6.dp, end = 6.dp)
                    .fillMaxWidth()
            )

            // ── Overlay : couverture GPS ──
            MapGpsCoverageOverlay(
                total = total,
                withGps = withGps,
                showLayerPicker = showLayerPicker,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )

            // ── Légende par essence ──
            MapLegendPanel(
                visible = showLegend,
                essenceColors = essenceColors,
                essenceCounts = essenceCounts,
                hiddenEssences = hiddenEssences,
                essenceMap = essenceMap,
                onToggleEssence = { code, hide ->
                    hiddenEssences = if (hide) hiddenEssences + code else hiddenEssences - code
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 88.dp)
            )

            // ── Avertissement GPS mauvais ──
            MapGpsWarningBanner(
                geoTiges = geoTiges,
                dismissed = dismissedGpsBanner,
                onDismiss = { dismissedGpsBanner = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 6.dp)
            )

            // ── Coordonnées au tap ──
            MapCoordsOverlay(
                visible = showCoords,
                coordsText = coordsText,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )

            // ── Outils droite : Zoom +/- et Nord ──
            MapZoomControls(
                mapLibreMap = mapLibreMap,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
            )

            // ── Tapped tree info card (top center) ──
            MapTigeInfoPanel(
                tappedTree = tappedTree,
                navActive = navState.isActive,
                hasLocationPermission = hasLocationPermission,
                onRequestPermission = {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onStartNavigation = { info ->
                    val target = TreeNavigator.Target(
                        tigeId = "",
                        essenceName = info.essenceName,
                        essenceCode = info.essenceCode,
                        diamCm = info.diamCm ?: 0.0,
                        hauteurM = info.hauteurM,
                        lat = info.lat,
                        lon = info.lon
                    )
                    treeNavigator.startNavigation(target)
                    tappedTree = null
                },
                onDismiss = { tappedTree = null },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp, start = 16.dp, end = 16.dp)
            )

            // ── Navigation compass overlay (top center) ──
            MapNavigationOverlay(
                navState = navState,
                onStopNavigation = { treeNavigator.stopNavigation() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp, start = 16.dp, end = 16.dp)
            )

            // ── GPS Trace control panel (bas gauche) ──
            MapTraceControlPanel(
                traceState = traceState,
                onAddManualPoint = {
                    val map = mapLibreMap ?: return@MapTraceControlPanel
                    try {
                        val loc = map.locationComponent.lastKnownLocation
                        if (loc != null) {
                            gpsTracer.addManualPoint(
                                lat = loc.latitude,
                                lon = loc.longitude,
                                alt = if (loc.hasAltitude()) loc.altitude else null,
                                precisionM = loc.accuracy
                            )
                        }
                    } catch (e: Throwable) { Log.w(TAG, "addManualPoint from lastKnownLocation failed", e) }
                },
                onUndoLastPoint = { gpsTracer.undoLastPoint() },
                onStopRecording = { gpsTracer.stopRecording() },
                onSaveTrace = {
                    traceName = ""
                    showTraceSaveDialog = true
                },
                onClearTrace = { gpsTracer.clearTrace() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 16.dp)
            )

            // ── Panneau outil de mesure (bas gauche) ──
            MapMeasurePanel(
                state = MapMeasurePanelState(
                    isActive = measureActive,
                    points = measurePoints,
                    mode = measureMode,
                    distUnit = measureDistUnit,
                    areaUnit = measureAreaUnit,
                    color = measureColor,
                    showSavedPanel = showSavedMeasuresPanel
                ),
                traceHasContent = traceState.isRecording || traceState.points.isNotEmpty(),
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
                            measureMode = event.mode
                            measurePoints = event.points
                            showSavedMeasuresPanel = false
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 12.dp,
                        bottom = if (traceState.isRecording || traceState.points.isNotEmpty()) 170.dp else 16.dp
                    )
            )

            // ── FABs principaux (bas droite) ──
            MapMainFABs(
                measureActive = measureActive,
                measurePointsNotEmpty = measurePoints.isNotEmpty(),
                traceRecording = traceState.isRecording,
                traceHasPoints = traceState.points.isNotEmpty(),
                hasLocationPermission = hasLocationPermission,
                displayedGeoTiges = displayedGeoTiges,
                onToggleMeasure = {
                    measureActive = !measureActive
                    if (!measureActive) measurePoints = emptyList()
                },
                onGpsLocate = {
                    val map = mapLibreMap ?: return@MapMainFABs
                    if (!hasLocationPermission) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        return@MapMainFABs
                    }
                    try {
                        val loc = map.locationComponent.lastKnownLocation
                        if (loc != null) {
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(loc.latitude, loc.longitude), 18.0
                                ), 600
                            )
                            coordsText = String.format(
                                java.util.Locale.US,
                                "%.6f, %.6f",
                                loc.latitude, loc.longitude
                            )
                            showCoords = true
                        }
                    } catch (e: Throwable) { Log.w(TAG, "animateCamera to GPS location failed", e) }
                },
                onStartTrace = {
                    if (!hasLocationPermission) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        return@MapMainFABs
                    }
                    gpsTracer.startRecording()
                },
                onRecenterOnTrees = {
                    val map = mapLibreMap ?: return@MapMainFABs
                    try {
                        val builder = LatLngBounds.Builder()
                        displayedGeoTiges.forEach { (_, lon, lat) -> builder.include(LatLng(lat, lon)) }
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100), 600)
                    } catch (e: Throwable) { Log.w(TAG, "recenter on trees failed", e) }
                    showCoords = false
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 16.dp)
            )

            // ── Barre de progression téléchargement hors-ligne ──
            MapOfflineProgressBar(
                progress = offlineProgress,
                onClearProgress = { offlineTileManager.clearProgress() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 64.dp)
            )

            // ── Message si aucun GPS (fermable) ──
            MapNoGpsMessage(
                total = total,
                withGps = withGps,
                geoTigesSize = geoTiges.size,
                mapOnlyReliableGps = mapOnlyReliableGps,
                mapReliableGpsThresholdM = mapReliableGpsThresholdM,
                onDismiss = { dismissedGpsBanner = true },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )

            // ── Message si aucune tige (fermable) ──
            MapEmptyMessage(
                total = total,
                onDismiss = { dismissedGpsBanner = true },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        }
    }

    // ── Save measure dialog ──
    MapMeasureSaveDialog(
        visible = showMeasureSaveDialog,
        measureMode = measureMode,
        measurePoints = measurePoints,
        measureSaveName = measureSaveName,
        onMeasureSaveNameChange = { measureSaveName = it },
        onDismiss = { showMeasureSaveDialog = false },
        context = context
    )

    // ── Save trace dialog ──
    MapTraceSaveDialog(
        visible = showTraceSaveDialog,
        traceState = traceState,
        traceName = traceName,
        onTraceNameChange = { traceName = it },
        onDismiss = { showTraceSaveDialog = false },
        onConfirm = {
            val wkt = gpsTracer.toWktPolygon()
            val surfHa = traceState.surfaceHa
            if (wkt != null && parcelleRepository != null) {
                scope.launch {
                    if (parcelleId != "none" && parcelleId != "all" && !parcelleId.startsWith("forest_")) {
                        try {
                            val parcelle = parcelleRepository.getParcelleById(parcelleId).first()
                            if (parcelle != null) {
                                parcelleRepository.updateParcelle(
                                    parcelle.copy(
                                        shape = wkt,
                                        surfaceHa = surfHa ?: parcelle.surfaceHa,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        } catch (e: Throwable) { Log.w(TAG, "updateParcelle with trace shape failed", e) }
                    }
                    gpsTracer.clearTrace()
                    showTraceSaveDialog = false
                }
            } else {
                gpsTracer.clearTrace()
                showTraceSaveDialog = false
            }
        }
    )
}
