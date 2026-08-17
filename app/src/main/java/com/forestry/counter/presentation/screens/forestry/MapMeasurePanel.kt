package com.forestry.counter.presentation.screens.forestry

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.MartelageEnlever
import com.forestry.counter.presentation.theme.Radius
import com.forestry.counter.presentation.theme.Space
import com.forestry.counter.presentation.theme.Touch
import com.mapbox.mapboxsdk.geometry.LatLng
import java.io.File
import java.util.Locale

/**
 * État UI du panneau de mesure.
 */
internal data class MapMeasurePanelState(
    val isActive: Boolean = false,
    val points: List<LatLng> = emptyList(),
    val mode: MeasureMode = MeasureMode.DISTANCE,
    val distUnit: MeasureDistUnit = MeasureDistUnit.M,
    val areaUnit: MeasureAreaUnit = MeasureAreaUnit.M2,
    val color: Color = MartelageEnlever,
    val showSavedPanel: Boolean = false
)

/**
 * Événements produits par le panneau de mesure.
 */
internal sealed class MapMeasurePanelEvent {
    data class SetActive(val active: Boolean) : MapMeasurePanelEvent()
    data class SetPoints(val points: List<LatLng>) : MapMeasurePanelEvent()
    data class SetMode(val mode: MeasureMode) : MapMeasurePanelEvent()
    data class SetDistUnit(val unit: MeasureDistUnit) : MapMeasurePanelEvent()
    data class SetAreaUnit(val unit: MeasureAreaUnit) : MapMeasurePanelEvent()
    data class SetColor(val color: Color) : MapMeasurePanelEvent()
    data object ToggleSavedPanel : MapMeasurePanelEvent()
    data object SaveRequest : MapMeasurePanelEvent()
    data class LoadSavedMeasure(val mode: MeasureMode, val points: List<LatLng>) : MapMeasurePanelEvent()
}

/**
 * Tiroir de mesure distance / surface et mesures sauvegardées.
 *
 * La visibilité du tiroir (`sheetVisible`) est distincte de l'état "mesure
 * en cours" (`state.isActive`) : le bouton "Mesurer sur la carte" ferme le
 * tiroir sans arrêter la mesure, pour laisser l'utilisateur taper des
 * points sur la carte (un `ModalBottomSheet` ouvert intercepte les taps
 * derrière lui). Un bouton flottant dans `MapRechercheScreen` rouvre le
 * tiroir ensuite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapMeasurePanel(
    state: MapMeasurePanelState,
    sheetVisible: Boolean,
    context: Context,
    onEvent: (MapMeasurePanelEvent) -> Unit,
    onRequestHideSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!sheetVisible) return
    ModalBottomSheet(
        onDismissRequest = onRequestHideSheet,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Space.md, vertical = Space.xs),
            verticalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            MeasurePanelHeader()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MeasureModeSelector(state = state, onEvent = onEvent)
                MeasureUnitSelector(state = state, onEvent = onEvent)
            }
            if (state.isActive && state.points.isEmpty()) {
                Text(
                    stringResource(R.string.measure_tap_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MeasureResults(state = state)
            if (state.mode == MeasureMode.DISTANCE && state.points.size >= 2) {
                MeasureSegmentBreakdown(state = state)
            }
            MeasureColorPalette(state = state, onEvent = onEvent)
            Surface(
                onClick = onRequestHideSheet,
                color = state.color,
                shape = GsShape.field,
                modifier = Modifier.fillMaxWidth().height(Touch.field),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        stringResource(R.string.measure_go_to_map),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
            MeasureActionButtons(state = state, context = context, onEvent = onEvent)
            if (state.showSavedPanel) {
                SavedMeasuresPanel(context = context, onEvent = onEvent)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(Space.xs))
        }
    }
}

@Composable
private fun MeasurePanelHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs)
    ) {
        Icon(
            Icons.Default.Straighten,
            contentDescription = stringResource(R.string.cd_straighten),
            tint = MartelageEnlever,
            modifier = Modifier.size(Space.lg)
        )
        Text(
            stringResource(R.string.measure_tool_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MartelageEnlever
        )
    }
}

@Composable
private fun MeasureModeSelector(
    state: MapMeasurePanelState,
    onEvent: (MapMeasurePanelEvent) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
        listOf(MeasureMode.DISTANCE to R.string.measure_mode_distance,
            MeasureMode.AREA to R.string.measure_mode_area).forEach { (mode, resId) ->
            val sel = state.mode == mode
            Surface(
                onClick = {
                    if (state.mode != mode) {
                        onEvent(MapMeasurePanelEvent.SetMode(mode))
                        onEvent(MapMeasurePanelEvent.SetPoints(emptyList()))
                    }
                },
                color = if (sel) MartelageEnlever else MaterialTheme.colorScheme.surfaceVariant,
                shape = GsShape.sm,
                modifier = Modifier.height(Touch.min)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.wrapContentSize()) {
                    Text(
                        stringResource(resId),
                        modifier = Modifier.padding(horizontal = Space.sm),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun MeasureUnitSelector(
    state: MapMeasurePanelState,
    onEvent: (MapMeasurePanelEvent) -> Unit
) {
    if (state.mode == MeasureMode.DISTANCE) {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
            listOf(MeasureDistUnit.M to "m", MeasureDistUnit.KM to "km").forEach { (unit, label) ->
                val sel = state.distUnit == unit
                Surface(
                    onClick = { onEvent(MapMeasurePanelEvent.SetDistUnit(unit)) },
                    color = if (sel) state.color else MaterialTheme.colorScheme.surfaceVariant,
                    shape = GsShape.sm,
                    modifier = Modifier.height(Touch.min)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.wrapContentSize()) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = Space.sm),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(Space.xxs)) {
            listOf(MeasureAreaUnit.M2 to "m²", MeasureAreaUnit.ARES to "ares", MeasureAreaUnit.HA to "ha").forEach { (unit, label) ->
                val sel = state.areaUnit == unit
                Surface(
                    onClick = { onEvent(MapMeasurePanelEvent.SetAreaUnit(unit)) },
                    color = if (sel) state.color else MaterialTheme.colorScheme.surfaceVariant,
                    shape = GsShape.sm,
                    modifier = Modifier.height(Touch.min)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.wrapContentSize()) {
                        Text(
                            label,
                            modifier = Modifier.padding(horizontal = Space.sm),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasureResults(state: MapMeasurePanelState) {
    if (state.points.size >= 2 && state.mode == MeasureMode.DISTANCE) {
        val dist = measurePolylineM(state.points)
        val t = when (state.distUnit) {
            MeasureDistUnit.M  -> String.format(Locale.getDefault(), "%.1f m", dist)
            MeasureDistUnit.KM -> String.format(Locale.getDefault(), "%.4f km", dist / 1000.0)
        }
        Text(
            stringResource(R.string.measure_panel_distance, t),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = state.color
        )
    }
    if (state.mode == MeasureMode.AREA && state.points.size >= 3) {
        val areaM2 = measureAreaM2(state.points)
        val t = when (state.areaUnit) {
            MeasureAreaUnit.M2   -> String.format(Locale.getDefault(), "%.1f m²", areaM2)
            MeasureAreaUnit.ARES -> String.format(Locale.getDefault(), "%.2f ares", areaM2 / 100.0)
            MeasureAreaUnit.HA   -> String.format(Locale.getDefault(), "%.4f ha", areaM2 / 10_000.0)
        }
        Text(
            stringResource(R.string.measure_panel_area, t),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = state.color
        )
    }
    if (state.points.isNotEmpty()) {
        Text(
            stringResource(R.string.measure_points_count, state.points.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Détail segment par segment (mode Distance, ≥2 points) — chips défilables
 * horizontalement, une par tronçon entre deux points consécutifs.
 */
@Composable
private fun MeasureSegmentBreakdown(state: MapMeasurePanelState) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Space.xxs),
    ) {
        state.points.zipWithNext().forEachIndexed { index, (a, b) ->
            val segM = haversineM(a.latitude, a.longitude, b.latitude, b.longitude)
            val t = if (segM >= 1000.0) String.format(Locale.getDefault(), "%.2f km", segM / 1000.0)
            else String.format(Locale.getDefault(), "%.0f m", segM)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = GsShape.xs,
            ) {
                Text(
                    "${index + 1}: $t",
                    modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xxs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MeasureColorPalette(
    state: MapMeasurePanelState,
    onEvent: (MapMeasurePanelEvent) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
        MEASURE_COLORS.forEach { c ->
            val isSelected = c == state.color
            Box(
                modifier = Modifier
                    .size(if (isSelected) Space.lg else Space.md)
                    .clip(CircleShape)
                    .background(c)
                    .border(if (isSelected) 2.dp else 0.dp, Color.White, CircleShape)
                    .clickable { onEvent(MapMeasurePanelEvent.SetColor(c)) }
            )
        }
    }
}

@Composable
private fun MeasureActionButtons(
    state: MapMeasurePanelState,
    context: Context,
    onEvent: (MapMeasurePanelEvent) -> Unit
) {
    val shareText = measureShareText(state)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
        if (state.points.isNotEmpty()) {
            MeasureActionButton(
                icon = Icons.Default.Remove,
                contentDescription = stringResource(R.string.measure_undo),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { onEvent(MapMeasurePanelEvent.SetPoints(state.points.dropLast(1))) },
            )
            MeasureActionButton(
                icon = Icons.Default.Close,
                contentDescription = stringResource(R.string.measure_clear),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = { onEvent(MapMeasurePanelEvent.SetPoints(emptyList())) },
            )
            if (shareText != null) {
                MeasureActionButton(
                    icon = Icons.Default.Share,
                    contentDescription = stringResource(R.string.measure_share),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        try {
                            context.startActivity(android.content.Intent.createChooser(intent, null))
                        } catch (e: Throwable) { Log.w("MapMeasurePanel", "share intent failed", e) }
                    },
                )
            }
            if (state.points.size >= 2) {
                MeasureActionButton(
                    icon = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.measure_save),
                    containerColor = MartelageEnlever,
                    contentColor = Color.White,
                    onClick = { onEvent(MapMeasurePanelEvent.SaveRequest) },
                )
            }
        }
        MeasureActionButton(
            icon = Icons.Default.Layers,
            contentDescription = stringResource(R.string.map_mesures_sauvegardees),
            containerColor = if (state.showSavedPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (state.showSavedPanel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = { onEvent(MapMeasurePanelEvent.ToggleSavedPanel) },
        )
    }
}

/** Texte de partage ("Distance : 152 m" / "Surface : 0,42 ha"), null si rien à partager. */
private fun measureShareText(state: MapMeasurePanelState): String? {
    return when {
        state.mode == MeasureMode.DISTANCE && state.points.size >= 2 -> {
            val dist = measurePolylineM(state.points)
            val t = if (dist >= 1000.0) String.format(Locale.getDefault(), "%.3f km", dist / 1000.0)
            else String.format(Locale.getDefault(), "%.1f m", dist)
            "Distance mesurée (GeoSylva) : $t"
        }
        state.mode == MeasureMode.AREA && state.points.size >= 3 -> {
            val ha = measureAreaM2(state.points) / 10_000.0
            "Surface mesurée (GeoSylva) : ${String.format(Locale.getDefault(), "%.4f ha", ha)}"
        }
        else -> null
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MeasureActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = containerColor,
        contentColor = contentColor,
        shape = GsShape.md,
        modifier = Modifier.weight(1f).height(Touch.field),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(Space.md))
        }
    }
}

@Composable
private fun SavedMeasuresPanel(
    context: Context,
    onEvent: (MapMeasurePanelEvent) -> Unit
) {
    val measureDir = remember { File(context.getExternalFilesDir(null), "measurements") }
    val savedFiles = remember(Unit) {
        if (measureDir.exists()) measureDir.listFiles { f -> f.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        else emptyList()
    }
    if (savedFiles.isEmpty()) {
        Text(stringResource(R.string.map_no_saved_measures), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(Space.xxs), modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
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
                                onEvent(MapMeasurePanelEvent.LoadSavedMeasure(mode, parsed))
                            }
                        } catch (e: Throwable) { Log.w("MapMeasurePanel", "parse saved measure points failed", e) }
                    },
                    shape = GsShape.xs,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().heightIn(min = Touch.field)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        Icon(
                            if (mode == MeasureMode.AREA) Icons.Default.Map else Icons.Default.Straighten,
                            contentDescription = if (mode == MeasureMode.AREA) stringResource(R.string.cd_map) else stringResource(R.string.cd_straighten),
                            modifier = Modifier.size(Space.md),
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
