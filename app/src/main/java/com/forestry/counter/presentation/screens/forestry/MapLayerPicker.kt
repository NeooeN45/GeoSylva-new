package com.forestry.counter.presentation.screens.forestry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestry.counter.R
import com.forestry.counter.domain.location.OfflineTileManager
import com.forestry.counter.presentation.theme.Elevation
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.Space
import com.forestry.counter.presentation.theme.Touch

/**
 * Panneau flottant de sélection de couche cartographique.
 */
@Composable
internal fun MapLayerPicker(
    visible: Boolean,
    currentLayerIdx: Int,
    hasOfflineTilesState: Boolean,
    offlineTileManager: OfflineTileManager,
    onLayerSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    is3DActive: Boolean = false,
    onToggle3D: (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(250)) { -it / 4 },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(200)) { -it / 4 },
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.overlay),
            shape = GsShape.field
        ) {
            Column(modifier = Modifier.padding(Space.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = stringResource(R.string.cd_layers),
                            modifier = Modifier.size(Space.lg),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.map_layer_picker_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(Touch.field)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), modifier = Modifier.size(Space.md))
                    }
                }

                Spacer(modifier = Modifier.height(Space.xs))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    MAP_LAYERS.forEachIndexed { index, layer ->
                        if (layer.key == "OFFLINE_LOCAL") return@forEachIndexed
                        LayerChip(
                            layer = layer,
                            isSelected = index == currentLayerIdx,
                            onClick = {
                                onLayerSelected(index)
                                onDismiss()
                            }
                        )
                    }
                    val offlineIdx = MAP_LAYERS.indexOfFirst { it.key == "OFFLINE_LOCAL" }
                    if (hasOfflineTilesState && offlineIdx >= 0) {
                        val (tileCount, _) = remember(hasOfflineTilesState) {
                            offlineTileManager.cacheStats()
                        }
                        Box {
                            LayerChip(
                                layer = MAP_LAYERS[offlineIdx],
                                isSelected = offlineIdx == currentLayerIdx,
                                onClick = {
                                    onLayerSelected(offlineIdx)
                                    onDismiss()
                                }
                            )
                            if (tileCount > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = GsShape.field,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(Space.xxs)
                                ) {
                                    Text(
                                        "$tileCount",
                                        modifier = Modifier.padding(horizontal = Space.xxs, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (onToggle3D != null) {
                    Spacer(modifier = Modifier.height(Space.xs))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = Touch.field)
                            .clip(GsShape.sm)
                            .selectable(selected = is3DActive, onClick = onToggle3D)
                            .padding(horizontal = Space.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xs),
                    ) {
                        Icon(
                            Icons.Default.Landscape,
                            contentDescription = null,
                            modifier = Modifier.size(Space.md),
                            tint = if (is3DActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(R.string.map_toggle_3d),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (is3DActive) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                        )
                        androidx.compose.material3.Switch(checked = is3DActive, onCheckedChange = { onToggle3D() })
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
internal fun LayerChip(
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
            .width(Space.xxl + Space.lg)
            .heightIn(min = Touch.fieldPrimary)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = GsShape.sm
            )
            .clip(GsShape.sm)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = GsShape.sm,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) Elevation.raised else Elevation.card)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.xs, horizontal = Space.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                layer.emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(Space.xxs))
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
