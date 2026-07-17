package com.forestry.counter.presentation.screens.forestry

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.forestry.counter.presentation.theme.AccentGreen
import com.forestry.counter.presentation.theme.MartelageEnlever
import com.forestry.counter.presentation.theme.SemanticError
import com.forestry.counter.presentation.theme.SemanticInfo
import com.forestry.counter.presentation.theme.SemanticSuccess
import com.forestry.counter.presentation.theme.GpsModere
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
import com.forestry.counter.domain.geo.LabelField
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
    }

    // File picker pour importer un .zip shapefile
    val shpPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        shpImporting = true
        scope.launch {
            val overlay = shpManager.importFromUri(uri)
            shpImporting = false
            if (overlay != null) {
                shpOverlay = overlay
                Toast.makeText(
                    context,
                    context.getString(R.string.shp_import_success, overlay.featureCount),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(context, context.getString(R.string.shp_import_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    // Appliquer l'overlay shapefile quand le fichier ou les paramètres changent
    fun applyCurrentShpOverlay(style: Style): String {
        val file = shpGeoJsonFile ?: return "no file"
        val ov = shpOverlay ?: return "no overlay"
        return applyShapefileOverlay(style, file, ov)
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

            // Zoom initial sur la position GPS à altitude modérée quand pas de données d'arbres
            LaunchedEffect(mapReady, hasLocationPermission) {
                val map = mapLibreMap ?: return@LaunchedEffect
                if (!mapReady) return@LaunchedEffect
                // Attendre un peu pour laisser le LocationComponent s'initialiser
                kotlinx.coroutines.delay(600)
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
                    }
                } catch (_: Throwable) { /* permission pas encore accordée */ }
            }

            // Ajouter/mettre à jour les tiges (source GeoJSON + clusters) quand la carte et les données sont prêtes
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

            // ── Mettre à jour le tracé GPS sur la carte ──
            LaunchedEffect(mapReady, traceState) {
                val map = mapLibreMap ?: return@LaunchedEffect
                if (!mapReady) return@LaunchedEffect
                map.getStyle { style -> renderTraceOnMap(style, gpsTracer) }
            }

            // ── Mettre à jour la couche de mesure ──
            LaunchedEffect(mapReady, measurePoints, measureMode, measureColor) {
                val map = mapLibreMap ?: return@LaunchedEffect
                if (!mapReady) return@LaunchedEffect
                map.getStyle { style ->
                    try { renderMeasureOnMap(style, measurePoints, measureMode, measureColor.toArgb()) }
                    catch (e: Throwable) { Log.w(TAG, "renderMeasureOnMap failed", e) }
                }
            }

            // ── Appliquer/mettre à jour overlay shapefile quand les données changent ──
            LaunchedEffect(shpGeoJsonFile, shpOverlay, mapReady) {
                val map = mapLibreMap ?: return@LaunchedEffect
                if (!mapReady) return@LaunchedEffect
                Log.d(TAG, "Applying shapefile overlay: file=${shpGeoJsonFile?.absolutePath}, overlay=${shpOverlay?.id}")
                map.getStyle { style ->
                    val result = applyCurrentShpOverlay(style)
                    Log.d(TAG, "SHP apply result: $result")
                }
            }

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
            @OptIn(ExperimentalLayoutApi::class)
            AnimatedVisibility(
                visible = showShpPanel,
                enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { -it / 4 },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { -it / 4 },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp, start = 6.dp, end = 6.dp)
                    .fillMaxWidth()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // En-tête
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = stringResource(R.string.cd_map), modifier = Modifier.size(20.dp), tint = SemanticSuccess)
                                Text(stringResource(R.string.shp_overlay), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showShpPanel = false }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (shpOverlay == null) {
                            Text(
                                stringResource(R.string.shp_no_overlay),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                onClick = {
                                    shpPickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
                                },
                                color = SemanticSuccess,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add), tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text(stringResource(R.string.shp_import), color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        } else {
                            val ov = shpOverlay ?: return@Column

                            // ── Info + Visibilité ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ov.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = SemanticSuccess)
                                    Text(stringResource(R.string.shp_info_format, ov.featureCount, ov.forestNames.size), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = ov.visible,
                                    onCheckedChange = { vis ->
                                        val updated = ov.copy(visible = vis)
                                        shpOverlay = updated
                                        shpManager.updateOverlay(updated)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth().height(1.dp)) {}

                            // ── REMPLISSAGE ──
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.shp_fill_color), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SHP_COLOR_PALETTE.forEach { c ->
                                    val isSelected = (ov.fillColor and 0x00FFFFFF) == (c and 0x00FFFFFF)
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(c))
                                            .then(
                                                if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                                else Modifier
                                            )
                                            .clickable {
                                                val updated = ov.copy(fillColor = c)
                                                shpOverlay = updated
                                                shpManager.updateOverlay(updated)
                                            }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stringResource(R.string.shp_fill_opacity, (ov.fillOpacity * 100).toInt()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Slider(
                                value = ov.fillOpacity,
                                onValueChange = { v -> shpOverlay = ov.copy(fillOpacity = v) },
                                onValueChangeFinished = { shpOverlay?.let { shpManager.updateOverlay(it) } },
                                valueRange = 0f..1f,
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            )

                            // ── CONTOUR ──
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth().height(1.dp)) {}
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.shp_border_color), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SHP_COLOR_PALETTE.forEach { c ->
                                    val isSelected = (ov.borderColor and 0x00FFFFFF) == (c and 0x00FFFFFF)
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(c))
                                            .then(
                                                if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                                else Modifier
                                            )
                                            .clickable {
                                                val updated = ov.copy(borderColor = c)
                                                shpOverlay = updated
                                                shpManager.updateOverlay(updated)
                                            }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stringResource(R.string.shp_border_opacity, (ov.borderOpacity * 100).toInt()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Slider(
                                value = ov.borderOpacity,
                                onValueChange = { v -> shpOverlay = ov.copy(borderOpacity = v) },
                                onValueChangeFinished = { shpOverlay?.let { shpManager.updateOverlay(it) } },
                                valueRange = 0f..1f,
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            )
                            Text(
                                stringResource(R.string.shp_border_width, "%.1f".format(java.util.Locale.US, ov.borderWidth)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = ov.borderWidth,
                                onValueChange = { v -> shpOverlay = ov.copy(borderWidth = v) },
                                onValueChangeFinished = { shpOverlay?.let { shpManager.updateOverlay(it) } },
                                valueRange = 0.5f..5f,
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            )

                            // ── ÉTIQUETTES ──
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth().height(1.dp)) {}
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.shp_labels_title), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))

                            val isFr = java.util.Locale.getDefault().language == "fr"
                            LabelField.entries.forEach { field ->
                                val checked = field in ov.labelFields
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val newFields = if (checked) ov.labelFields - field else ov.labelFields + field
                                            val updated = ov.copy(labelFields = newFields)
                                            shpOverlay = updated
                                            shpManager.updateOverlay(updated)
                                        }
                                        .padding(vertical = 1.dp)
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { isChecked ->
                                            val newFields = if (isChecked) ov.labelFields + field else ov.labelFields - field
                                            val updated = ov.copy(labelFields = newFields)
                                            shpOverlay = updated
                                            shpManager.updateOverlay(updated)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (isFr) field.frLabel else field.enLabel,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            // Option combiner
                            if (ov.labelFields.size > 1) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val updated = ov.copy(combineLabels = !ov.combineLabels)
                                            shpOverlay = updated
                                            shpManager.updateOverlay(updated)
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Switch(
                                        checked = ov.combineLabels,
                                        onCheckedChange = { combine ->
                                            val updated = ov.copy(combineLabels = combine)
                                            shpOverlay = updated
                                            shpManager.updateOverlay(updated)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.shp_combine_labels), style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            // Taille des étiquettes
                            if (ov.labelFields.isNotEmpty()) {
                                Text(
                                    stringResource(R.string.shp_label_size, ov.labelSize.toInt()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = ov.labelSize,
                                    onValueChange = { v -> shpOverlay = ov.copy(labelSize = v) },
                                    onValueChangeFinished = { shpOverlay?.let { shpManager.updateOverlay(it) } },
                                    valueRange = 6f..24f,
                                    modifier = Modifier.fillMaxWidth().height(32.dp)
                                )
                            }

                            // ── ACTIONS ──
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth().height(1.dp)) {}
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        shpPickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
                                    },
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        stringResource(R.string.shp_replace),
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Surface(
                                    onClick = {
                                        shpManager.deleteOverlay(ov.id)
                                        shpOverlay = null
                                        shpGeoJsonFile = null
                                    },
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        stringResource(R.string.shp_delete),
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        // Indicateur d'import en cours
                        if (shpImporting) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text(stringResource(R.string.shp_importing), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

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
            AnimatedVisibility(
                visible = measurePoints.isNotEmpty() || measureActive,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 12.dp,
                        bottom = if (traceState.isRecording || traceState.points.isNotEmpty()) 170.dp else 16.dp
                    ),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).widthIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Titre + bascule mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Straighten,
                                contentDescription = stringResource(R.string.cd_straighten),
                                tint = MartelageEnlever,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                stringResource(R.string.measure_tool_title),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MartelageEnlever
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(MeasureMode.DISTANCE to R.string.measure_mode_distance,
                                   MeasureMode.AREA to R.string.measure_mode_area).forEach { (mode, resId) ->
                                val sel = measureMode == mode
                                Surface(
                                    onClick = { if (measureMode != mode) { measureMode = mode; measurePoints = emptyList() } },
                                    color = if (sel) MartelageEnlever else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(
                                        stringResource(resId),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Indice si aucun point
                        if (measureActive && measurePoints.isEmpty()) {
                            Text(
                                stringResource(R.string.measure_tap_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Sélecteur d'unité (affiché selon le mode)
                        if (measureMode == MeasureMode.DISTANCE) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(MeasureDistUnit.M to "m", MeasureDistUnit.KM to "km").forEach { (unit, label) ->
                                    val sel = measureDistUnit == unit
                                    Surface(
                                        onClick = { measureDistUnit = unit },
                                        color = if (sel) measureColor else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(22.dp)
                                    ) {
                                        Text(
                                            label,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(MeasureAreaUnit.M2 to "m²", MeasureAreaUnit.ARES to "ares", MeasureAreaUnit.HA to "ha").forEach { (unit, label) ->
                                    val sel = measureAreaUnit == unit
                                    Surface(
                                        onClick = { measureAreaUnit = unit },
                                        color = if (sel) measureColor else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(22.dp)
                                    ) {
                                        Text(
                                            label,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Résultats
                        if (measurePoints.size >= 2 && measureMode == MeasureMode.DISTANCE) {
                            val dist = measurePolylineM(measurePoints)
                            val t = when (measureDistUnit) {
                                MeasureDistUnit.M  -> String.format(Locale.getDefault(), "%.1f m", dist)
                                MeasureDistUnit.KM -> String.format(Locale.getDefault(), "%.4f km", dist / 1000.0)
                            }
                            Text(
                                stringResource(R.string.measure_panel_distance, t),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = measureColor
                            )
                        }
                        if (measureMode == MeasureMode.AREA && measurePoints.size >= 3) {
                            val areaM2 = measureAreaM2(measurePoints)
                            val t = when (measureAreaUnit) {
                                MeasureAreaUnit.M2   -> String.format(Locale.getDefault(), "%.1f m²", areaM2)
                                MeasureAreaUnit.ARES -> String.format(Locale.getDefault(), "%.2f ares", areaM2 / 100.0)
                                MeasureAreaUnit.HA   -> String.format(Locale.getDefault(), "%.4f ha", areaM2 / 10_000.0)
                            }
                            Text(
                                stringResource(R.string.measure_panel_area, t),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = measureColor
                            )
                        }
                        if (measurePoints.isNotEmpty()) {
                            Text(
                                stringResource(R.string.measure_points_count, measurePoints.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Palette de couleurs
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            MEASURE_COLORS.forEach { c ->
                                val isSelected = c == measureColor
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 20.dp else 16.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .border(if (isSelected) 2.dp else 0.dp, Color.White, CircleShape)
                                        .clickable { measureColor = c }
                                )
                            }
                        }

                        // Boutons action
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (measurePoints.isNotEmpty()) {
                                SmallFloatingActionButton(
                                    onClick = { if (measurePoints.isNotEmpty()) measurePoints = measurePoints.dropLast(1) },
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.measure_undo), modifier = Modifier.size(16.dp))
                                }
                                SmallFloatingActionButton(
                                    onClick = { measurePoints = emptyList() },
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.measure_clear), modifier = Modifier.size(16.dp))
                                }
                                if (measurePoints.size >= 2) {
                                    SmallFloatingActionButton(
                                        onClick = { measureSaveName = ""; showMeasureSaveDialog = true },
                                        containerColor = MartelageEnlever,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.measure_save), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            // Bouton mesures sauvegardées
                            SmallFloatingActionButton(
                                onClick = { showSavedMeasuresPanel = !showSavedMeasuresPanel },
                                containerColor = if (showSavedMeasuresPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (showSavedMeasuresPanel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Layers, contentDescription = stringResource(R.string.map_mesures_sauvegardees), modifier = Modifier.size(16.dp))
                            }
                        }

                        // Panneau mesures sauvegardées
                        if (showSavedMeasuresPanel) {
                            val measureDir = remember { File(context.getExternalFilesDir(null), "measurements") }
                            val savedFiles = remember(showSavedMeasuresPanel) {
                                if (measureDir.exists()) measureDir.listFiles { f -> f.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
                                else emptyList()
                            }
                            if (savedFiles.isEmpty()) {
                                Text(stringResource(R.string.map_no_saved_measures), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                                    savedFiles.forEach { file ->
                                        val raw = remember(file) { try { file.readText() } catch (_: Throwable) { "" } }
                                        val name  = remember(raw) { Regex("\"name\":\"([^\"]*)\"").find(raw)?.groupValues?.get(1) ?: file.nameWithoutExtension }
                                        val mode  = remember(raw) { if (raw.contains("\"mode\":\"AREA\"")) MeasureMode.AREA else MeasureMode.DISTANCE }
                                        val value = remember(raw) {
                                            if (mode == MeasureMode.AREA) {
                                                val ha = Regex("\"areaHa\":([\\d.E-]+)").find(raw)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                                                String.format(Locale.getDefault(), "%.4f ha", ha)
                                            } else {
                                                val m = Regex("\"distanceM\":([\\d.E-]+)").find(raw)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                                                if (m >= 1000.0) String.format(Locale.getDefault(), "%.3f km", m / 1000.0)
                                                else String.format(Locale.getDefault(), "%.1f m", m)
                                            }
                                        }
                                        Surface(
                                            onClick = {
                                                try {
                                                    val ptsStr = Regex("\"points\":\\[([^\\]]+(?:\\][^\\]]*)*?)\\],\"distanceM\"").find(raw)?.groupValues?.get(1) ?: ""
                                                    val coordPattern = Regex("\\[([\\d.E+-]+),([\\d.E+-]+)\\]")
                                                    val parsed = coordPattern.findAll(ptsStr).map { m ->
                                                        LatLng(m.groupValues[1].toDouble(), m.groupValues[2].toDouble())
                                                    }.toList()
                                                    if (parsed.isNotEmpty()) {
                                                        measureMode = mode
                                                        measurePoints = parsed
                                                        showSavedMeasuresPanel = false
                                                    }
                                                } catch (e: Throwable) { Log.w(TAG, "parse saved measure points failed", e) }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    if (mode == MeasureMode.AREA) Icons.Default.Map else Icons.Default.Straighten,
                                                    contentDescription = if (mode == MeasureMode.AREA) stringResource(R.string.cd_map) else stringResource(R.string.cd_straighten),
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MartelageEnlever
                                                )
                                                Column(Modifier.weight(1f)) {
                                                    Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                                    Text(value, style = MaterialTheme.typography.labelSmall, color = MartelageEnlever)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
