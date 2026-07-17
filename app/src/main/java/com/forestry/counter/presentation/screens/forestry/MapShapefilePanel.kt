package com.forestry.counter.presentation.screens.forestry

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.geo.LabelField
import com.forestry.counter.domain.geo.ShapefileOverlay
import com.forestry.counter.domain.geo.ShapefileOverlayManager
import com.forestry.counter.presentation.theme.SemanticError
import com.forestry.counter.presentation.theme.SemanticSuccess
import java.util.Locale

/**
 * État UI du panneau shapefile overlay.
 */
internal data class MapShapefilePanelState(
    val overlay: ShapefileOverlay? = null,
    val isVisible: Boolean = false
)

/**
 * Événements produits par le panneau shapefile overlay.
 */
internal sealed class MapShapefilePanelEvent {
    data class SetOverlay(val overlay: ShapefileOverlay?) : MapShapefilePanelEvent()
    data class DeleteOverlay(val id: String) : MapShapefilePanelEvent()
}

/**
 * Panneau de gestion du shapefile overlay (import, style, visibilité, étiquettes).
 */
@Composable
internal fun MapShapefilePanel(
    state: MapShapefilePanelState,
    onEvent: (MapShapefilePanelEvent) -> Unit,
    shpManager: ShapefileOverlayManager,
    context: Context,
    shpImporting: Boolean,
    shpErrorMessage: String?,
    onImportRequest: () -> Unit,
    onApplyOverlay: () -> Unit,
    onDismissPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { -it / 4 },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { -it / 4 },
        modifier = modifier
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
                    IconButton(onClick = onDismissPanel, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (state.overlay == null) {
                    Text(
                        stringResource(R.string.shp_no_overlay),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        onClick = onImportRequest,
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
                    val ov = state.overlay ?: return@Column

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
                                onEvent(MapShapefilePanelEvent.SetOverlay(updated))
                                shpManager.updateOverlay(updated)
                                onApplyOverlay()
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
                                        onEvent(MapShapefilePanelEvent.SetOverlay(updated))
                                        shpManager.updateOverlay(updated)
                                        onApplyOverlay()
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.shp_fill_opacity, (ov.fillOpacity * 100).toInt()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = ov.fillOpacity,
                        onValueChange = { v -> onEvent(MapShapefilePanelEvent.SetOverlay(ov.copy(fillOpacity = v))) },
                        onValueChangeFinished = {
                            state.overlay?.let {
                                shpManager.updateOverlay(it)
                                onApplyOverlay()
                            }
                        },
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
                                        onEvent(MapShapefilePanelEvent.SetOverlay(updated))
                                        shpManager.updateOverlay(updated)
                                        onApplyOverlay()
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.shp_border_opacity, (ov.borderOpacity * 100).toInt()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = ov.borderOpacity,
                        onValueChange = { v -> onEvent(MapShapefilePanelEvent.SetOverlay(ov.copy(borderOpacity = v))) },
                        onValueChangeFinished = {
                            state.overlay?.let {
                                shpManager.updateOverlay(it)
                                onApplyOverlay()
                            }
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    )
                    Text(
                        stringResource(R.string.shp_border_width, "%.1f".format(Locale.US, ov.borderWidth)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = ov.borderWidth,
                        onValueChange = { v -> onEvent(MapShapefilePanelEvent.SetOverlay(ov.copy(borderWidth = v))) },
                        onValueChangeFinished = {
                            state.overlay?.let {
                                shpManager.updateOverlay(it)
                                onApplyOverlay()
                            }
                        },
                        valueRange = 0.5f..5f,
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    )

                    // ── ÉTIQUETTES ──
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth().height(1.dp)) {}
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.shp_labels_title), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    val isFr = Locale.getDefault().language == "fr"
                    LabelField.entries.forEach { field ->
                        val checked = field in ov.labelFields
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newFields = if (checked) ov.labelFields - field else ov.labelFields + field
                                    val updated = ov.copy(labelFields = newFields)
                                    onEvent(MapShapefilePanelEvent.SetOverlay(updated))
                                    shpManager.updateOverlay(updated)
                                    onApplyOverlay()
                                }
                                .padding(vertical = 1.dp)
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    val newFields = if (isChecked) ov.labelFields + field else ov.labelFields - field
                                    val updated = ov.copy(labelFields = newFields)
                                    onEvent(MapShapefilePanelEvent.SetOverlay(updated))
                                    shpManager.updateOverlay(updated)
                                    onApplyOverlay()
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
                                    onEvent(MapShapefilePanelEvent.SetOverlay(updated))
                                    shpManager.updateOverlay(updated)
                                    onApplyOverlay()
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Switch(
                                checked = ov.combineLabels,
                                onCheckedChange = { combine ->
                                    val updated = ov.copy(combineLabels = combine)
                                    onEvent(MapShapefilePanelEvent.SetOverlay(updated))
                                    shpManager.updateOverlay(updated)
                                    onApplyOverlay()
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
                            onValueChange = { v -> onEvent(MapShapefilePanelEvent.SetOverlay(ov.copy(labelSize = v))) },
                            onValueChangeFinished = {
                                state.overlay?.let {
                                    shpManager.updateOverlay(it)
                                    onApplyOverlay()
                                }
                            },
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
                            onClick = onImportRequest,
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
                                onEvent(MapShapefilePanelEvent.DeleteOverlay(ov.id))
                                onApplyOverlay()
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

                // Message d'erreur d'import
                if (!shpErrorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = shpErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = SemanticError
                    )
                }
            }
        }
    }
}
