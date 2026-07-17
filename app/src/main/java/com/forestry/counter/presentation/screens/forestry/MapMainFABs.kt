package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.model.Tige
import com.forestry.counter.presentation.theme.SemanticSuccess
import com.forestry.counter.presentation.theme.MartelageEnlever

/**
 * FABs principaux en bas à droite de la carte (mesure, localisation, trace, recentrage).
 */
@Composable
internal fun MapMainFABs(
    measureActive: Boolean,
    measurePointsNotEmpty: Boolean,
    traceRecording: Boolean,
    traceHasPoints: Boolean,
    hasLocationPermission: Boolean,
    displayedGeoTiges: List<Triple<Tige, Double, Double>>,
    onToggleMeasure: () -> Unit,
    onGpsLocate: () -> Unit,
    onStartTrace: () -> Unit,
    onRecenterOnTrees: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ) {
        SmallFloatingActionButton(
            onClick = onToggleMeasure,
            containerColor = if (measureActive) MartelageEnlever else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (measureActive) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Straighten, contentDescription = stringResource(R.string.measure_tool_title))
        }

        SmallFloatingActionButton(
            onClick = onGpsLocate,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.GpsFixed, contentDescription = stringResource(R.string.map_my_location))
        }

        if (!traceRecording && !traceHasPoints) {
            SmallFloatingActionButton(
                onClick = onStartTrace,
                containerColor = SemanticSuccess,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.trace_start))
            }
        }

        if (displayedGeoTiges.isNotEmpty()) {
            FloatingActionButton(
                onClick = onRecenterOnTrees,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Forest, contentDescription = stringResource(R.string.map_recenter))
            }
        }
    }
}
