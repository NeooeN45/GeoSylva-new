package com.forestry.counter.presentation.screens.forestry

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.domain.repository.TigeRepository
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    parcelleId: String,
    tigeRepository: TigeRepository,
    preferencesManager: UserPreferencesManager,
    onNavigateBack: () -> Unit
) {
    val tiges by tigeRepository.getTigesByParcelle(parcelleId).collectAsState(initial = emptyList())
    val animationsEnabled by preferencesManager.animationsEnabled.collectAsState(initial = true)

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val withGps = tiges.count { it.gpsWkt != null }
            val total = tiges.size
            val progressTarget = if (total > 0) withGps.toFloat() / total.toFloat() else 0f
            val progress by animateFloatAsState(
                targetValue = progressTarget,
                animationSpec = tween(durationMillis = if (animationsEnabled) 320 else 0, easing = FastOutSlowInEasing),
                label = "mapGpsProgress"
            )
            val withGpsAnim by animateFloatAsState(
                targetValue = withGps.toFloat(),
                animationSpec = tween(durationMillis = if (animationsEnabled) 320 else 0, easing = FastOutSlowInEasing),
                label = "mapGpsCount"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Column {
                            Text(stringResource(R.string.gps_coverage), style = MaterialTheme.typography.titleMedium)

                            Crossfade(
                                targetState = total,
                                animationSpec = tween(durationMillis = if (animationsEnabled) 200 else 0, easing = FastOutSlowInEasing),
                                label = "mapGpsTextCrossfade"
                            ) { totalState ->
                                val withGpsDisplay = withGpsAnim.roundToInt().coerceIn(0, totalState)
                                Text(
                                    stringResource(R.string.gps_points_format, withGpsDisplay, totalState),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                    )

                    Text(
                        stringResource(R.string.map_coming_soon),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun parseWktPointZ(wkt: String?): Triple<Double?, Double?, Double?> {
    if (wkt.isNullOrBlank()) return Triple(null, null, null)
    val cleaned = wkt.trim().replace(Regex("\\s+"), " ")
    val regex = Regex("POINT( Z)? \\(([-0-9.]+) ([-0-9.]+)( [-0-9.]+)?\\)")
    val m = regex.find(cleaned) ?: return Triple(null, null, null)
    val lon = m.groupValues[2].toDoubleOrNull()
    val lat = m.groupValues[3].toDoubleOrNull()
    val alt = m.groupValues.getOrNull(4)?.trim()?.toDoubleOrNull()
    return Triple(lon, lat, alt)
}
