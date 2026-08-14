package com.forestry.counter.presentation.screens.forestry

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.location.GpsParcelTracer
import com.forestry.counter.presentation.theme.MartelageEnlever
import com.forestry.counter.presentation.theme.Space
import com.mapbox.mapboxsdk.geometry.LatLng
import java.io.File
import java.util.Locale

/**
 * Dialogue de sauvegarde d'une mesure distance/surface.
 */
@Composable
internal fun MapMeasureSaveDialog(
    visible: Boolean,
    measureMode: MeasureMode,
    measurePoints: List<LatLng>,
    measureSaveName: String,
    onMeasureSaveNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.measure_save_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                if (measureMode == MeasureMode.DISTANCE && measurePoints.size >= 2) {
                    val dist = measurePolylineM(measurePoints)
                    val t = if (dist >= 1000.0) String.format(Locale.getDefault(), "%.3f km", dist / 1000.0)
                    else String.format(Locale.getDefault(), "%.1f m", dist)
                    Text(
                        stringResource(R.string.measure_panel_distance, t),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MartelageEnlever
                    )
                }
                if (measureMode == MeasureMode.AREA && measurePoints.size >= 3) {
                    val areaM2 = measureAreaM2(measurePoints)
                    val ha = areaM2 / 10_000.0
                    val t = if (ha >= 0.01) String.format(Locale.getDefault(), "%.4f ha", ha)
                    else String.format(Locale.getDefault(), "%.0f m²", areaM2)
                    Text(
                        stringResource(R.string.measure_panel_area, t),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MartelageEnlever
                    )
                }
                Text(
                    stringResource(R.string.measure_points_count, measurePoints.size),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = measureSaveName,
                    onValueChange = onMeasureSaveNameChange,
                    label = { Text(stringResource(R.string.measure_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    saveMeasurement(context, measureMode, measurePoints, measureSaveName)
                    onDismiss()
                }
            ) { Text(stringResource(R.string.measure_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

private fun saveMeasurement(
    context: Context,
    measureMode: MeasureMode,
    measurePoints: List<LatLng>,
    measureSaveName: String
) {
    val mode = measureMode
    val pts = measurePoints
    val dist = if (mode == MeasureMode.DISTANCE) measurePolylineM(pts) else 0.0
    val areaM2 = if (mode == MeasureMode.AREA) measureAreaM2(pts) else 0.0
    val name = measureSaveName.trim().ifBlank {
        "${if (mode == MeasureMode.DISTANCE) "distance" else "surface"}_${System.currentTimeMillis()}"
    }
    val ptsJson = pts.joinToString(",") {
        "[${String.format(Locale.US, "%.7f", it.latitude)},${String.format(Locale.US, "%.7f", it.longitude)}]"
    }
    val json = """{"name":"${name.replace("\"", "\\\"")}","mode":"$mode","points":[$ptsJson],"distanceM":$dist,"areaHa":${areaM2 / 10_000.0},"timestamp":${System.currentTimeMillis()}}"""
    try {
        val dir = File(context.getExternalFilesDir(null), "measurements")
        dir.mkdirs()
        File(dir, "${name}_${System.currentTimeMillis()}.json").writeText(json)
        Toast.makeText(context, context.getString(R.string.measure_saved), Toast.LENGTH_SHORT).show()
    } catch (e: Throwable) { Log.w("MapDialogs", "save measurement file failed", e) }
}

/**
 * Dialogue de sauvegarde d'un tracé GPS (polygone parcelle).
 */
@Composable
internal fun MapTraceSaveDialog(
    visible: Boolean,
    traceState: GpsParcelTracer.TraceState,
    traceName: String,
    onTraceNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.trace_save_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                traceState.surfaceHa?.let { ha ->
                    Text(
                        stringResource(R.string.trace_surface_ha, String.format(Locale.getDefault(), "%.4f", ha)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                traceState.perimeterM?.let { p ->
                    Text(
                        stringResource(R.string.trace_perimeter_m, String.format(Locale.getDefault(), "%.0f", p)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    stringResource(R.string.trace_points_count, traceState.points.size),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = traceName,
                    onValueChange = onTraceNameChange,
                    label = { Text(stringResource(R.string.trace_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.trace_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
