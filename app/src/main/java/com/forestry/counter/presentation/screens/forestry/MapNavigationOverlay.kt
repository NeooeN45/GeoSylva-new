package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.location.TreeNavigator
import com.forestry.counter.presentation.theme.SemanticInfo
import com.forestry.counter.presentation.theme.SemanticSuccess
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Overlay de navigation boussole affiché en haut de la carte.
 */
@Composable
internal fun MapNavigationOverlay(
    navState: TreeNavigator.NavigationState,
    onStopNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!navState.isActive) return

    Card(
        modifier = modifier.widthIn(max = 300.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (navState.arrived) SemanticSuccess.copy(alpha = 0.95f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.nav_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (navState.arrived) Color.White else MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onStopNavigation,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.nav_stop),
                        modifier = Modifier.size(16.dp),
                        tint = if (navState.arrived) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            navState.target?.let { target ->
                Text(
                    "${target.essenceName} · \u2300 ${target.diamCm.roundToInt()} cm",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (navState.arrived) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (navState.arrived) {
                Text(
                    stringResource(R.string.nav_arrived),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                NavigationCompass(navState = navState)
            }
        }
    }
}

@Composable
private fun NavigationCompass(navState: TreeNavigator.NavigationState) {
    val relativeBearing = navState.relativeBearingDeg ?: 0f
    val compassColor = if (navState.arrived) Color.White else SemanticInfo

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            drawCircle(
                color = compassColor.copy(alpha = 0.15f),
                radius = size.minDimension / 2f
            )
            drawCircle(
                color = compassColor.copy(alpha = 0.3f),
                radius = size.minDimension / 2f,
                style = Stroke(width = 2f)
            )

            val angleRad = Math.toRadians(relativeBearing.toDouble()).toFloat()
            val cx = center.x
            val cy = center.y
            val arrowLen = size.minDimension / 2f - 10f

            val tipX = cx + arrowLen * kotlin.math.sin(angleRad)
            val tipY = cy - arrowLen * kotlin.math.cos(angleRad)

            val baseLen = 12f
            val leftX = cx + baseLen * kotlin.math.sin(angleRad - Math.PI.toFloat() * 0.85f)
            val leftY = cy - baseLen * kotlin.math.cos(angleRad - Math.PI.toFloat() * 0.85f)
            val rightX = cx + baseLen * kotlin.math.sin(angleRad + Math.PI.toFloat() * 0.85f)
            val rightY = cy - baseLen * kotlin.math.cos(angleRad + Math.PI.toFloat() * 0.85f)

            val path = Path().apply {
                moveTo(tipX, tipY)
                lineTo(leftX, leftY)
                lineTo(cx, cy)
                lineTo(rightX, rightY)
                close()
            }
            drawPath(path, color = compassColor)
        }
    }

    navState.distanceM?.let { dist ->
        val distText = if (dist >= 1000f) {
            String.format(Locale.getDefault(), "%.1f km", dist / 1000f)
        } else {
            String.format(Locale.getDefault(), "%.0f m", dist)
        }
        Text(
            distText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SemanticInfo
        )
    }

    navState.userAccuracyM?.let { acc ->
        Text(
            stringResource(R.string.nav_accuracy, String.format(Locale.getDefault(), "%.1f", acc)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
