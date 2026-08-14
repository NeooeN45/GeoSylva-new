package com.forestry.counter.presentation.screens.forestry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestry.counter.R
import com.forestry.counter.domain.location.OfflineTileManager
import com.forestry.counter.domain.model.Tige
import com.forestry.counter.presentation.theme.GpsModere

/**
 * Overlay compact indiquant le taux de tiges géolocalisées.
 */
@Composable
internal fun MapGpsCoverageOverlay(
    total: Int,
    withGps: Int,
    showLayerPicker: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = total > 0 && !showLayerPicker,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(150)),
        modifier = modifier
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
}

/**
 * Bannière discrète avertissant de la présence de tiges au GPS peu précis.
 */
@Composable
internal fun MapGpsWarningBanner(
    geoTiges: List<Triple<Tige, Double, Double>>,
    dismissed: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val poorGpsTiges = remember(geoTiges) {
        geoTiges.count { (t, _, _) -> (t.precisionM ?: 0.0) > 20.0 }
    }
    AnimatedVisibility(
        visible = poorGpsTiges > 0 && !dismissed,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(250)),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = GpsModere.copy(alpha = 0.82f),
            modifier = Modifier.clickable(onClick = onDismiss)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.GpsFixed,
                    contentDescription = stringResource(R.string.cd_gps_locate),
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    stringResource(R.string.map_gps_poor_warning, poorGpsTiges),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Bulle temporaire affichant les coordonnées au tap / centrage GPS.
 */
@Composable
internal fun MapCoordsOverlay(
    visible: Boolean,
    coordsText: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && coordsText.isNotEmpty(),
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(150)),
        modifier = modifier
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
}

/**
 * Barre de progression du téléchargement de tuiles hors-ligne.
 */
@Composable
internal fun MapOfflineProgressBar(
    progress: OfflineTileManager.DownloadProgress?,
    onClearProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (progress == null) return

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sizeMb = String.format("%.1f", progress.completedSize / 1_048_576.0)
                Text(
                    when {
                        progress.isComplete && progress.error == null ->
                            "${stringResource(R.string.offline_download_done)} (${progress.completedResources} tuiles, $sizeMb Mo)"
                        progress.isComplete && progress.error != null ->
                            progress.error ?: stringResource(R.string.offline_download_error)
                        else -> stringResource(R.string.offline_downloading)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (progress.error != null && progress.isComplete) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (progress.isComplete) {
                    IconButton(
                        onClick = onClearProgress,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (!progress.isComplete) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (progress.progressPct / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                val sizeMbDl = String.format("%.1f", progress.completedSize / 1_048_576.0)
                Text(
                    "${progress.completedResources}/${progress.requiredResources} tuiles · $sizeMbDl Mo (${String.format("%.0f", progress.progressPct)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Message affiché quand aucune tige n'a de GPS (ou qu'elles sont filtrées).
 */
@Composable
internal fun MapNoGpsMessage(
    total: Int,
    withGps: Int,
    geoTigesSize: Int,
    mapOnlyReliableGps: Boolean,
    mapReliableGpsThresholdM: Float,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (total <= 0 || withGps != 0) return

    val isFilteredOut = geoTigesSize > 0 && mapOnlyReliableGps
    val bannerBg = if (isFilteredOut)
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
    else
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
    val bannerIcon = if (isFilteredOut)
        MaterialTheme.colorScheme.tertiary
    else
        MaterialTheme.colorScheme.error

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), modifier = Modifier.size(18.dp))
            }
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = bannerIcon
                )
                Text(
                    text = if (isFilteredOut)
                        stringResource(R.string.map_gps_filtered_out, geoTigesSize, mapReliableGpsThresholdM.toInt())
                    else
                        stringResource(R.string.map_no_gps_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isFilteredOut) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Message affiché quand la parcelle ne contient aucune tige.
 */
@Composable
internal fun MapEmptyMessage(
    total: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isGlobalScope: Boolean = false,
) {
    if (total != 0) return

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), modifier = Modifier.size(18.dp))
            }
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
                    text = stringResource(
                        if (isGlobalScope) R.string.map_empty_global else R.string.map_empty
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
