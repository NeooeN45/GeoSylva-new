package com.forestry.counter.presentation.screens.forestry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.presentation.theme.Elevation
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.Space
import com.forestry.counter.presentation.theme.Touch

/**
 * Légende cliquable des essences affichées sur la carte.
 */
@Composable
internal fun MapLegendPanel(
    visible: Boolean,
    essenceColors: Map<String, Int>,
    essenceCounts: Map<String, Int>,
    hiddenEssences: Set<String>,
    essenceMap: Map<String, Essence>,
    onToggleEssence: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && essenceColors.isNotEmpty(),
        enter = fadeIn(tween(200)) + expandVertically(tween(250)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.overlay),
            shape = GsShape.field
        ) {
            Column(
                modifier = Modifier.padding(Space.sm),
                verticalArrangement = Arrangement.spacedBy(Space.xxs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    Icon(
                        Icons.Default.Forest,
                        contentDescription = null,
                        modifier = Modifier.size(Space.md),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        stringResource(R.string.map_legend),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                val totalWithGps = essenceCounts.values.sum()
                essenceColors.forEach { (code, color) ->
                    val name = essenceMap[code]?.name ?: code
                    val count = essenceCounts[code] ?: 0
                    val pct = if (totalWithGps > 0) count * 100 / totalWithGps else 0
                    val isHidden = code in hiddenEssences
                    LegendRow(
                        name = name,
                        color = color,
                        count = count,
                        pct = pct,
                        isHidden = isHidden,
                        onClick = { onToggleEssence(code, !isHidden) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendRow(
    name: String,
    color: Int,
    count: Int,
    pct: Int,
    isHidden: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
        modifier = Modifier
            .heightIn(min = Touch.field)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(Space.sm)
                .clip(CircleShape)
                .background(if (isHidden) Color.LightGray else Color(color))
                .border(1.5.dp, Color.White, CircleShape)
        )
        Text(
            name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (isHidden) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "($count · $pct%)",
            style = MaterialTheme.typography.labelSmall,
            color = if (isHidden) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
