package com.forestry.counter.presentation.screens.forestry

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.forestry.counter.domain.location.WktUtils
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.model.Tige
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.repository.ParcelleRepository
import com.forestry.counter.domain.repository.TigeRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlin.math.roundToInt

private const val TAG = "MapScreen"

// Palette de couleurs pour les pickers de couleur (fill / border)
private val SHP_COLOR_PALETTE = listOf(
    0xFF2E7D32.toInt(), // vert forêt
    0xFF1B5E20.toInt(), // vert foncé
    0xFF4CAF50.toInt(), // vert clair
    0xFF81C784.toInt(), // vert pastel
    0xFF1565C0.toInt(), // bleu
    0xFF42A5F5.toInt(), // bleu clair
    0xFFEF6C00.toInt(), // orange
    0xFFF44336.toInt(), // rouge
    0xFF9C27B0.toInt(), // violet
    0xFF795548.toInt(), // marron
    0xFF607D8B.toInt(), // gris-bleu
    0xFF000000.toInt(), // noir
)

private const val SHP_SOURCE_ID = "shp-parcelles"
private const val SHP_FILL_ID = "shp-fill"
private const val SHP_LINE_ID = "shp-line"
private const val SHP_LABEL_ID = "shp-labels"

/**
 * Applique (ou met à jour) un overlay shapefile GeoJSON sur le style MapLibre courant.
 */
private fun applyShapefileOverlay(
    style: Style,
    geoJson: String,
    overlay: ShapefileOverlay
) {
    try {
        // Supprimer les couches/source existantes
        try { style.removeLayer(SHP_LABEL_ID) } catch (_: Throwable) {}
        try { style.removeLayer(SHP_LINE_ID) } catch (_: Throwable) {}
        try { style.removeLayer(SHP_FILL_ID) } catch (_: Throwable) {}
        try { style.removeSource(SHP_SOURCE_ID) } catch (_: Throwable) {}

        if (!overlay.visible) return

        // Ajouter la source GeoJSON
        val source = GeoJsonSource(SHP_SOURCE_ID)
        source.setGeoJson(geoJson)
        style.addSource(source)

        // Fill layer (polygones colorés) — couleur RGB sans alpha + opacité séparée
        val fillRgb = overlay.fillColor or 0xFF000000.toInt() // forcer alpha 0xFF pour la couleur
        val fill = FillLayer(SHP_FILL_ID, SHP_SOURCE_ID).withProperties(
            PropertyFactory.fillColor(fillRgb),
            PropertyFactory.fillOpacity(overlay.fillOpacity)
        )
        style.addLayer(fill)

        // Line layer (contours)
        val borderRgb = overlay.borderColor or 0xFF000000.toInt()
        val line = LineLayer(SHP_LINE_ID, SHP_SOURCE_ID).withProperties(
            PropertyFactory.lineColor(borderRgb),
            PropertyFactory.lineWidth(overlay.borderWidth),
            PropertyFactory.lineOpacity(overlay.borderOpacity)
        )
        style.addLayer(line)

        // Symbol layer (étiquettes) — seulement si des champs sont sélectionnés
        if (overlay.labelFields.isNotEmpty()) {
            val textExpr = if (overlay.combineLabels) {
                // Combiner : "val1 · val2 · val3"
                overlay.labelFields.joinToString(" · ") { "{${it.key}}" }
            } else {
                // Multi-ligne : "val1\nval2\nval3"
                overlay.labelFields.joinToString("\n") { "{${it.key}}" }
            }
            val labels = SymbolLayer(SHP_LABEL_ID, SHP_SOURCE_ID).withProperties(
                PropertyFactory.textField(textExpr),
                PropertyFactory.textSize(overlay.labelSize),
                PropertyFactory.textColor(android.graphics.Color.BLACK),
                PropertyFactory.textHaloColor(android.graphics.Color.WHITE),
                PropertyFactory.textHaloWidth(1.5f),
                PropertyFactory.textFont(arrayOf("Open Sans Bold")),
                PropertyFactory.textAllowOverlap(false),
                PropertyFactory.textIgnorePlacement(false),
                PropertyFactory.textMaxWidth(10f)
            )
            style.addLayer(labels)
        }

        Log.d(TAG, "Shapefile overlay applied: ${overlay.featureCount} features")

    } catch (e: Throwable) {
        Log.e(TAG, "applyShapefileOverlay failed", e)
    }
}

/**
 * Builds a raster-only MapLibre style JSON from a single tile URL template.
 * This is more reliable than inline multi-line JSON strings.
 */
private fun rasterStyle(name: String, tileUrl: String, tileSize: Int = 256, maxZoom: Int = 19): String {
    return """{
  "version": 8,
  "name": "$name",
  "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
  "sources": {
    "tiles": {
      "type": "raster",
      "tiles": ["$tileUrl"],
      "tileSize": $tileSize,
      "maxzoom": $maxZoom
    }
  },
  "layers": [
    { "id": "tiles", "type": "raster", "source": "tiles" }
  ]
}"""
}

/**
 * Construit un style MapLibre avec couche de base + couches overlay optionnelles.
 */
private fun rasterStyleMulti(
    name: String,
    baseTileUrl: String,
    overlayTileUrls: List<String> = emptyList(),
    tileSize: Int = 256,
    maxZoom: Int = 19
): String {
    val sources = mutableListOf<String>()
    val layers = mutableListOf<String>()
    sources += """"base":{"type":"raster","tiles":["$baseTileUrl"],"tileSize":$tileSize,"maxzoom":$maxZoom}"""
    layers += """{ "id": "base", "type": "raster", "source": "base" }"""
    overlayTileUrls.forEachIndexed { i, url ->
        sources += """"overlay$i":{"type":"raster","tiles":["$url"],"tileSize":256,"maxzoom":$maxZoom}"""
        layers += """{ "id": "overlay$i", "type": "raster", "source": "overlay$i", "paint": { "raster-opacity": 0.7 } }"""
    }
    return """{"version":8,"name":"$name","glyphs":"https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf","sources":{${sources.joinToString(",")}},"layers":[${layers.joinToString(",")}]}"""
}

// ── URL WMTS GéoPortail (data.geopf.fr) ──
private const val GEOPF_WMTS = "https://data.geopf.fr/wmts?" +
    "SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&FORMAT=image/png" +
    "&TILEMATRIXSET=PM&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}"

private fun geopfLayer(layer: String) = "$GEOPF_WMTS&LAYER=$layer"

/**
 * Catégorie de couche (pour organiser le sélecteur).
 */
enum class LayerCategory(val labelResId: Int) {
    GENERAL(R.string.map_category_general)
}

/**
 * Description d'une couche de fond de carte.
 */
data class MapLayerDef(
    val key: String,
    val labelResId: Int,
    val emoji: String,
    val styleJson: String,
    val isDark: Boolean = false,
    val category: LayerCategory = LayerCategory.GENERAL
)

private val MAP_LAYERS = listOf(
    // ── Couches générales ──
    MapLayerDef(
        key = "OSM",
        labelResId = R.string.map_layer_osm,
        emoji = "\uD83C\uDF0D",
        styleJson = rasterStyle(
            "OpenStreetMap",
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
        )
    ),
    MapLayerDef(
        key = "SATELLITE",
        labelResId = R.string.map_layer_satellite,
        emoji = "\uD83D\uDEF0\uFE0F",
        styleJson = rasterStyle(
            "ESRI Satellite",
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
        ),
        isDark = true
    ),
    MapLayerDef(
        key = "SAT_LABELS",
        labelResId = R.string.map_layer_satellite_labels,
        emoji = "\uD83C\uDF10",
        styleJson = rasterStyleMulti(
            "Satellite + Labels",
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
            overlayTileUrls = listOf(
                "https://stamen-tiles.a.ssl.fastly.net/toner-labels/{z}/{x}/{y}@2x.png"
            )
        ),
        isDark = true
    ),
    MapLayerDef(
        key = "TOPO",
        labelResId = R.string.map_layer_topo,
        emoji = "\u26F0\uFE0F",
        styleJson = rasterStyle(
            "OpenTopoMap",
            "https://tile.opentopomap.org/{z}/{x}/{y}.png",
            maxZoom = 17
        )
    ),
    MapLayerDef(
        key = "CARTO_VOYAGER",
        labelResId = R.string.map_layer_carto,
        emoji = "\uD83D\uDDFA\uFE0F",
        styleJson = rasterStyle(
            "Carto Voyager",
            "https://basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}@2x.png"
        )
    ),
    MapLayerDef(
        key = "CARTO_DARK",
        labelResId = R.string.map_layer_dark,
        emoji = "\uD83C\uDF19",
        styleJson = rasterStyle(
            "Carto Dark",
            "https://basemaps.cartocdn.com/dark_all/{z}/{x}/{y}@2x.png"
        ),
        isDark = true
    ),
    MapLayerDef(
        key = "ESRI_TOPO",
        labelResId = R.string.map_layer_esri_topo,
        emoji = "\uD83C\uDFDE\uFE0F",
        styleJson = rasterStyle(
            "ESRI Topographic",
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}"
        )
    ),
    // ── Couches forestières / ONF ──
    MapLayerDef(
        key = "PLAN_IGN",
        labelResId = R.string.map_layer_plan_ign,
        emoji = "\uD83C\uDDEB\uD83C\uDDF7",
        styleJson = rasterStyle(
            "Plan IGN v2",
            geopfLayer("GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2")
        ),
        category = LayerCategory.GENERAL
    ),
    MapLayerDef(
        key = "CADASTRE",
        labelResId = R.string.map_layer_cadastre,
        emoji = "\uD83D\uDCCF",
        styleJson = rasterStyleMulti(
            "Cadastre",
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            overlayTileUrls = listOf(
                geopfLayer("CADASTRALPARCELS.PARCELLAIRE_EXPRESS")
            )
        ),
        category = LayerCategory.GENERAL
    ),
    MapLayerDef(
        key = "FORETS_PUBLIQUES",
        labelResId = R.string.map_layer_forets_publiques,
        emoji = "\uD83C\uDF32",
        styleJson = rasterStyleMulti(
            "Forêts publiques",
            geopfLayer("GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2"),
            overlayTileUrls = listOf(
                geopfLayer("FORETS.PUBLIQUES")
            )
        ),
        category = LayerCategory.GENERAL
    ),
    MapLayerDef(
        key = "SAT_FORETS",
        labelResId = R.string.map_layer_sat_forets,
        emoji = "\uD83D\uDEF0\uD83C\uDF32",
        styleJson = rasterStyleMulti(
            "Satellite + Forêts publiques",
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
            overlayTileUrls = listOf(
                geopfLayer("FORETS.PUBLIQUES")
            )
        ),
        isDark = true,
        category = LayerCategory.GENERAL
    ),
    MapLayerDef(
        key = "BD_FORET",
        labelResId = R.string.map_layer_bd_foret,
        emoji = "\uD83C\uDF33",
        styleJson = rasterStyleMulti(
            "BD Forêt (IFN)",
            geopfLayer("GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2"),
            overlayTileUrls = listOf(
                geopfLayer("LANDUSE.FORESTINVENTORY.V2")
            )
        ),
        category = LayerCategory.GENERAL
    ),
    MapLayerDef(
        key = "FORET_CADASTRE",
        labelResId = R.string.map_layer_foret_cadastre,
        emoji = "\uD83C\uDF32\uD83D\uDCCF",
        styleJson = rasterStyleMulti(
            "Forêts + Cadastre",
            geopfLayer("GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2"),
            overlayTileUrls = listOf(
                geopfLayer("FORETS.PUBLIQUES"),
                geopfLayer("CADASTRALPARCELS.PARCELLAIRE_EXPRESS")
            )
        ),
        category = LayerCategory.GENERAL
    )
)

private val ESSENCE_COLOR_PALETTE = intArrayOf(
    0xFF2E7D32.toInt(), // vert forêt
    0xFF1565C0.toInt(), // bleu
    0xFFEF6C00.toInt(), // orange
    0xFF7B1FA2.toInt(), // violet
    0xFFC62828.toInt(), // rouge
    0xFF00838F.toInt(), // cyan
    0xFF4E342E.toInt(), // brun
    0xFF9E9D24.toInt(), // olive
    0xFFAD1457.toInt(), // rose
    0xFF37474F.toInt(), // gris-bleu
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    parcelleId: String,
    tigeRepository: TigeRepository,
    essenceRepository: EssenceRepository? = null,
    parcelleRepository: ParcelleRepository? = null,
    preferencesManager: UserPreferencesManager,
    onNavigateBack: () -> Unit
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
    val tiges by tigesFlow.collectAsState(initial = emptyList())
    val essences by (essenceRepository?.getAllEssences()
        ?: kotlinx.coroutines.flow.flowOf(emptyList<Essence>())).collectAsState(initial = emptyList())
    val animationsEnabled by preferencesManager.animationsEnabled.collectAsState(initial = true)

    // Permission localisation
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val hasLocationPermission = locationPermission.status.isGranted ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    // Demander la permission au lancement si pas encore accordée
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermission.launchPermissionRequest()
        }
    }

    // Initialiser MapLibre de façon synchrone AVANT toute création de MapView
    remember(context) {
        try { Mapbox.getInstance(context) } catch (_: Throwable) {}
        true
    }

    val essenceMap = remember(essences) { essences.associateBy { it.code.uppercase() } }

    val geoTiges = remember(tiges) {
        tiges.mapNotNull { t ->
            val (lon, lat, _) = WktUtils.parsePointZ(t.gpsWkt)
            if (lon != null && lat != null) Triple(t, lon, lat) else null
        }
    }
    val withGps = geoTiges.size
    val total = tiges.size

    // Couleurs par essence (stables)
    val essenceColors = remember(geoTiges) {
        val map = mutableMapOf<String, Int>()
        var idx = 0
        geoTiges.forEach { (t, _, _) ->
            map.getOrPut(t.essenceCode.uppercase()) {
                ESSENCE_COLOR_PALETTE[idx++ % ESSENCE_COLOR_PALETTE.size]
            }
        }
        map
    }

    // Compteurs par essence
    val essenceCounts = remember(geoTiges) {
        geoTiges.groupBy { it.first.essenceCode.uppercase() }.mapValues { it.value.size }
    }

    var mapReady by remember { mutableStateOf(false) }
    var mapLibreMap by remember { mutableStateOf<MapboxMap?>(null) }
    var currentLayerIdx by remember { mutableIntStateOf(0) }
    var showLegend by remember { mutableStateOf(false) }
    var showLayerPicker by remember { mutableStateOf(false) }
    var showCoords by remember { mutableStateOf(false) }
    var coordsText by remember { mutableStateOf("") }

    // ── Shapefile overlay state ──
    val scope = rememberCoroutineScope()
    val shpManager = remember { ShapefileOverlayManager(context) }
    var shpOverlay by remember { mutableStateOf<ShapefileOverlay?>(shpManager.listOverlays().firstOrNull()) }
    var shpGeoJson by remember { mutableStateOf<String?>(null) }
    var showShpPanel by remember { mutableStateOf(false) }
    var shpImporting by remember { mutableStateOf(false) }

    // Charger le GeoJSON au démarrage si un overlay existe
    LaunchedEffect(shpOverlay?.id) {
        val overlay = shpOverlay ?: run { shpGeoJson = null; return@LaunchedEffect }
        shpGeoJson = shpManager.loadGeoJson(overlay)
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

    val currentLayer = MAP_LAYERS.getOrElse(currentLayerIdx) { MAP_LAYERS[0] }

    // Appliquer l'overlay shapefile quand le GeoJSON ou les paramètres changent
    fun applyCurrentShpOverlay(style: Style) {
        val json = shpGeoJson ?: return
        val ov = shpOverlay ?: return
        applyShapefileOverlay(style, json, ov)
    }

    fun switchLayer(index: Int) {
        currentLayerIdx = index
        val map = mapLibreMap ?: return
        val layer = MAP_LAYERS.getOrElse(index) { MAP_LAYERS[0] }
        try {
            map.setStyle(Style.Builder().fromJson(layer.styleJson)) { style ->
                enableLocationComponent(map, style, context)
                applyCurrentShpOverlay(style)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Style switch failed", e)
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
                    // Shapefile overlay
                    IconButton(onClick = { showShpPanel = !showShpPanel }) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = stringResource(R.string.shp_overlay),
                            tint = if (shpOverlay != null && shpOverlay?.visible == true) Color(0xFF2E7D32)
                                   else if (showShpPanel) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Toggle légende
                    IconButton(onClick = { showLegend = !showLegend }) {
                        Icon(
                            Icons.Default.FormatListBulleted,
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
                        } catch (_: Throwable) {}
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        try { mapView.onDestroy() } catch (_: Throwable) {}
                    }
                }

                AndroidView(
                    factory = {
                        mapView.apply {
                            try {
                                onCreate(null)
                                getMapAsync { map ->
                                    try {
                                        map.setStyle(Style.Builder().fromJson(MAP_LAYERS[currentLayerIdx].styleJson)) { style ->
                                            mapLibreMap = map
                                            mapReady = true
                                            enableLocationComponent(map, style, context)
                                            applyCurrentShpOverlay(style)
                                        }
                                        map.uiSettings.apply {
                                            isCompassEnabled = true
                                            isRotateGesturesEnabled = true
                                            isZoomGesturesEnabled = true
                                            isScrollGesturesEnabled = true
                                            isTiltGesturesEnabled = false
                                            setAttributionMargins(16, 0, 0, 16)
                                        }
                                    } catch (e: Throwable) {
                                        Log.e(TAG, "Error setting map style", e)
                                        mapError = true
                                    }
                                }
                            } catch (e: Throwable) {
                                Log.e(TAG, "Error creating map", e)
                                mapError = true
                            }
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
                if (geoTiges.isNotEmpty()) return@LaunchedEffect
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

            // Ajouter les marqueurs quand la carte et les données sont prêtes
            LaunchedEffect(mapReady, geoTiges) {
                val map = mapLibreMap ?: return@LaunchedEffect
                if (!mapReady || geoTiges.isEmpty()) return@LaunchedEffect

                map.removeAnnotations()

                val iconFactory = IconFactory.getInstance(context)
                val boundsBuilder = LatLngBounds.Builder()

                for ((tige, lon, lat) in geoTiges) {
                    val pos = LatLng(lat, lon)
                    boundsBuilder.include(pos)

                    val color = essenceColors[tige.essenceCode.uppercase()] ?: ESSENCE_COLOR_PALETTE[0]
                    val radiusPx = ((tige.diamCm / 3.0).coerceIn(6.0, 24.0)).toInt()
                    val bmp = createCircleMarker(radiusPx * 2, color)
                    val icon = iconFactory.fromBitmap(bmp)

                    val essName = essenceMap[tige.essenceCode.uppercase()]?.name ?: tige.essenceCode
                    val snippet = buildString {
                        append("⌀ ${tige.diamCm.roundToInt()} cm")
                        tige.hauteurM?.let { append("  ·  H ${it.roundToInt()} m") }
                        tige.precisionM?.let { append("  ·  ±${"%.1f".format(it)} m") }
                    }

                    map.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(essName)
                            .snippet(snippet)
                            .icon(icon)
                    )
                }

                try {
                    val bounds = boundsBuilder.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 800)
                } catch (_: Throwable) {
                    val first = geoTiges.first()
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(first.third, first.second), 17.0),
                        800
                    )
                }
            }

            // ── Appliquer/mettre à jour overlay shapefile quand les données changent ──
            LaunchedEffect(shpGeoJson, shpOverlay) {
                val map = mapLibreMap ?: return@LaunchedEffect
                if (!mapReady) return@LaunchedEffect
                map.getStyle { style -> applyCurrentShpOverlay(style) }
            }

            // ── Panneau sélecteur de couches (par catégorie) ──
            AnimatedVisibility(
                visible = showLayerPicker,
                enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { -it / 4 },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { -it / 4 },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp, start = 6.dp, end = 6.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                Icon(
                                    Icons.Default.Layers,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    stringResource(R.string.map_layer_picker_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { showLayerPicker = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MAP_LAYERS.forEachIndexed { index, layer ->
                                LayerChip(
                                    layer = layer,
                                    isSelected = index == currentLayerIdx,
                                    onClick = {
                                        switchLayer(index)
                                        showLayerPicker = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

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
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF2E7D32))
                                Text(stringResource(R.string.shp_overlay), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showShpPanel = false }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
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
                                color = Color(0xFF2E7D32),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text(stringResource(R.string.shp_import), color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        } else {
                            val ov = shpOverlay!!

                            // ── Info + Visibilité ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ov.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
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
                                        shpGeoJson = null
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

            // ── Overlay : couverture GPS (compact, en bas à gauche) ──
            AnimatedVisibility(
                visible = total > 0 && !showLayerPicker,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp).widthIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.GpsFixed,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "$withGps / $total",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        val progressVal = if (total > 0) withGps.toFloat() / total.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progressVal },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }

            // ── Légende par essence (améliorée) ──
            AnimatedVisibility(
                visible = showLegend && essenceColors.isNotEmpty(),
                enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 88.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Forest,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                stringResource(R.string.map_legend),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        essenceColors.forEach { (code, color) ->
                            val name = essenceMap[code]?.name ?: code
                            val count = essenceCounts[code] ?: 0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(color))
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                                Text(
                                    name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "($count)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Coordonnées au tap ──
            AnimatedVisibility(
                visible = showCoords && coordsText.isNotEmpty(),
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.88f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        coordsText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Outils droite : Zoom +/- et Nord ──
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom +
                MapToolButton(
                    onClick = {
                        val map = mapLibreMap ?: return@MapToolButton
                        map.animateCamera(CameraUpdateFactory.zoomIn(), 200)
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.map_zoom_in), modifier = Modifier.size(20.dp)) }
                )
                // Zoom -
                MapToolButton(
                    onClick = {
                        val map = mapLibreMap ?: return@MapToolButton
                        map.animateCamera(CameraUpdateFactory.zoomOut(), 200)
                    },
                    icon = { Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.map_zoom_out), modifier = Modifier.size(20.dp)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Nord / Boussole
                MapToolButton(
                    onClick = {
                        val map = mapLibreMap ?: return@MapToolButton
                        map.animateCamera(CameraUpdateFactory.bearingTo(0.0), 400)
                    },
                    icon = { Icon(Icons.Default.Explore, contentDescription = stringResource(R.string.map_north), modifier = Modifier.size(20.dp)) }
                )
            }

            // ── FABs principaux (bas droite) ──
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Ma position
                SmallFloatingActionButton(
                    onClick = {
                        val map = mapLibreMap ?: return@SmallFloatingActionButton
                        if (!hasLocationPermission) {
                            locationPermission.launchPermissionRequest()
                            return@SmallFloatingActionButton
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
                        } catch (_: Throwable) {}
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, contentDescription = stringResource(R.string.map_my_location))
                }
                // Recentrer sur les arbres
                if (geoTiges.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = {
                            val map = mapLibreMap ?: return@FloatingActionButton
                            try {
                                val builder = LatLngBounds.Builder()
                                geoTiges.forEach { (_, lon, lat) -> builder.include(LatLng(lat, lon)) }
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100), 600)
                            } catch (_: Throwable) {}
                            showCoords = false
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Forest, contentDescription = stringResource(R.string.map_recenter))
                    }
                }
            }

            // ── Message si aucun GPS ──
            if (total > 0 && withGps == 0) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.map_no_gps_data),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Message si aucune tige ──
            if (total == 0) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.map_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tuile individuelle de couche dans le sélecteur.
 */
@Composable
private fun LayerChip(
    layer: MapLayerDef,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val bgColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Card(
        modifier = Modifier
            .width(72.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                layer.emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                stringResource(layer.labelResId),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Bouton outil carte (petit, rond, semi-transparent).
 */
@Composable
private fun MapToolButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shadowElevation = 3.dp,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}

/**
 * Active le composant de localisation MapLibre pour afficher le point bleu de l'utilisateur.
 */
private fun enableLocationComponent(map: MapboxMap, style: Style, context: android.content.Context) {
    try {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return

        val locationComponent = map.locationComponent
        val activationOptions = LocationComponentActivationOptions
            .builder(context, style)
            .useDefaultLocationEngine(true)
            .build()
        locationComponent.activateLocationComponent(activationOptions)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.NONE
        locationComponent.renderMode = RenderMode.COMPASS
    } catch (e: Throwable) {
        Log.w(TAG, "Could not enable location component", e)
    }
}

private fun createCircleMarker(sizePx: Int, color: Int): Bitmap {
    val size = sizePx.coerceAtLeast(8)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val r = size / 2f
    // Ombre légère
    val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = 0x40000000
        style = Paint.Style.FILL
    }
    canvas.drawCircle(r + 1f, r + 1f, r - 1f, shadow)
    // Remplissage
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(r, r, r - 2f, fill)
    // Bordure blanche
    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }
    canvas.drawCircle(r, r, r - 2f, border)
    return bmp
}
