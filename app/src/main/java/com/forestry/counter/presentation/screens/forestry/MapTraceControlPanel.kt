package com.forestry.counter.presentation.screens.forestry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.location.GpsParcelTracer
import com.forestry.counter.presentation.theme.AccentGreen
import com.forestry.counter.presentation.theme.Elevation
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.SemanticError
import com.forestry.counter.presentation.theme.SemanticSuccess
import com.forestry.counter.presentation.theme.Space
import com.forestry.counter.presentation.theme.Touch
import java.util.Locale

/**
 * Panneau de contrôle du tracé GPS (parcelle).
 */
@Composable
internal fun MapTraceControlPanel(
    traceState: GpsParcelTracer.TraceState,
    onAddManualPoint: () -> Unit,
    onUndoLastPoint: () -> Unit,
    onStopRecording: () -> Unit,
    onSaveTrace: () -> Unit,
    onClearTrace: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = traceState.isRecording || traceState.points.isNotEmpty(),
        modifier = modifier,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            shape = GsShape.md,
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.overlay)
        ) {
            Column(
                modifier = Modifier.padding(Space.sm),
                verticalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                Text(
                    stringResource(R.string.trace_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (traceState.isRecording) SemanticSuccess
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.trace_points_count, traceState.points.size),
                    style = MaterialTheme.typography.bodySmall
                )
                traceState.surfaceHa?.let { ha ->
                    Text(
                        stringResource(R.string.trace_surface_ha, String.format(Locale.getDefault(), "%.4f", ha)),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SemanticSuccess
                    )
                }
                traceState.perimeterM?.let { p ->
                    Text(
                        stringResource(R.string.trace_perimeter_m, String.format(Locale.getDefault(), "%.0f", p)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TraceActionButtons(
                    traceState = traceState,
                    onAddManualPoint = onAddManualPoint,
                    onUndoLastPoint = onUndoLastPoint,
                    onStopRecording = onStopRecording,
                    onSaveTrace = onSaveTrace,
                    onClearTrace = onClearTrace
                )
            }
        }
    }
}

@Composable
private fun TraceActionButtons(
    traceState: GpsParcelTracer.TraceState,
    onAddManualPoint: () -> Unit,
    onUndoLastPoint: () -> Unit,
    onStopRecording: () -> Unit,
    onSaveTrace: () -> Unit,
    onClearTrace: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
        if (traceState.isRecording) {
            SmallFloatingActionButton(
                onClick = onAddManualPoint,
                containerColor = AccentGreen,
                contentColor = Color.White,
                shape = GsShape.sm,
                modifier = Modifier.size(Touch.field)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.trace_add_point), modifier = Modifier.size(Space.md))
            }
            SmallFloatingActionButton(
                onClick = onUndoLastPoint,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = GsShape.sm,
                modifier = Modifier.size(Touch.field)
            ) {
                Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.trace_undo), modifier = Modifier.size(Space.md))
            }
            SmallFloatingActionButton(
                onClick = onStopRecording,
                containerColor = SemanticError,
                contentColor = Color.White,
                shape = GsShape.sm,
                modifier = Modifier.size(Touch.field)
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.trace_stop), modifier = Modifier.size(Space.md))
            }
        } else if (traceState.points.isNotEmpty()) {
            if (traceState.points.size >= 3) {
                SmallFloatingActionButton(
                    onClick = onSaveTrace,
                    containerColor = SemanticSuccess,
                    contentColor = Color.White,
                    shape = GsShape.sm,
                    modifier = Modifier.size(Touch.field)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.trace_save), modifier = Modifier.size(Space.md))
                }
            }
            SmallFloatingActionButton(
                onClick = onClearTrace,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = GsShape.sm,
                modifier = Modifier.size(Touch.field)
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.trace_clear), modifier = Modifier.size(Space.md))
            }
        }
    }
}
