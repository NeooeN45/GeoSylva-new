package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.forestry.counter.R
import com.forestry.counter.domain.location.OfflineTileManager
import com.forestry.counter.presentation.theme.Elevation
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.Radius
import com.forestry.counter.presentation.theme.Space
import com.forestry.counter.presentation.theme.Touch
import java.text.DateFormat
import java.util.Date

/**
 * Gestion des zones hors-ligne téléchargées : liste, suppression individuelle,
 * et lancement d'un nouveau téléchargement pour la zone visible — avec
 * estimation (tuiles/taille) confirmée avant de consommer du réseau/stockage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapOfflineRegionsSheet(
    regions: List<OfflineTileManager.RegionMeta>,
    estimate: Pair<Long, Long>?,
    downloadAvailable: Boolean,
    onRequestDownloadCurrentView: () -> Unit,
    onConfirmDownload: () -> Unit,
    onCancelEstimate: () -> Unit,
    onDeleteRegion: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg),
    ) {
        Column(modifier = Modifier.padding(horizontal = Space.md, vertical = Space.sm)) {
            Text(
                stringResource(R.string.offline_regions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.offline_regions_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Space.xxs, bottom = Space.sm),
            )

            Surface(
                onClick = onRequestDownloadCurrentView,
                enabled = downloadAvailable,
                shape = GsShape.md,
                color = if (downloadAvailable) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(Space.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    val actionColor = if (downloadAvailable) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = actionColor)
                    Text(
                        stringResource(R.string.offline_download_current_view),
                        style = MaterialTheme.typography.labelLarge,
                        color = actionColor,
                    )
                }
            }
            if (!downloadAvailable) {
                Text(
                    stringResource(R.string.offline_pack_unavailable_for_layer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Space.xs),
                )
            }

            if (regions.isEmpty()) {
                Text(
                    stringResource(R.string.offline_regions_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Space.lg),
                )
            } else {
                LazyColumn(modifier = Modifier.padding(top = Space.sm)) {
                    items(regions, key = { it.id }) { region ->
                        OfflineRegionRow(region = region, onDelete = { onDeleteRegion(region.id) })
                    }
                }
            }
        }
    }

    if (estimate != null) {
        val (tileCount, sizeBytes) = estimate
        AlertDialog(
            onDismissRequest = onCancelEstimate,
            title = { Text(stringResource(R.string.offline_download_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.offline_download_confirm_body,
                        tileCount,
                        String.format("%.1f", sizeBytes / 1_048_576.0),
                    )
                )
            },
            confirmButton = { TextButton(onClick = onConfirmDownload) { Text(stringResource(R.string.offline_download)) } },
            dismissButton = { TextButton(onClick = onCancelEstimate) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun OfflineRegionRow(region: OfflineTileManager.RegionMeta, onDelete: () -> Unit) {
    Surface(
        shape = GsShape.sm,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().padding(vertical = Space.xxs),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Storage,
                contentDescription = null,
                modifier = Modifier.size(Space.md),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.size(Space.xs))
            Column(modifier = Modifier.weight(1f)) {
                Text(region.name, style = MaterialTheme.typography.labelLarge)
                val sizeMb = String.format("%.1f", region.sizeBytes / 1_048_576.0)
                val dateStr = DateFormat.getDateInstance(DateFormat.SHORT).format(Date(region.downloadedAtMs))
                Text(
                    "$sizeMb Mo · zoom ${region.minZoom}-${region.maxZoom} · $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(Touch.field)) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
