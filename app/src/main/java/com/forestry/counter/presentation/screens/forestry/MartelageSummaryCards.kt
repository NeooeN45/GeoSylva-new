package com.forestry.counter.presentation.screens.forestry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.forestry.counter.domain.calculation.quality.WoodQualityGrade
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.presentation.utils.ColorUtils
import java.util.Locale

/**
 * Carte Volume & Prix (affichée uniquement si volumeAvailable).
 */
@Composable
internal fun VolumeCard(
    vTotalText: String,
    vPerHaText: String,
    revenueTotalText: String,
    revenuePerHaText: String,
    animationsEnabled: Boolean,
    volumeAvailable: Boolean
) {
    AnimatedVisibility(
        visible = volumeAvailable,
        enter = fadeIn(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
            expandVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 180 else 0, easing = FastOutSlowInEasing)) +
            shrinkVertically(animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0, easing = FastOutSlowInEasing))
    ) {
        val volumeCardBg = MaterialTheme.colorScheme.primaryContainer
        val volumeCardContent = ColorUtils.getContrastingTextColor(volumeCardBg)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(
                containerColor = volumeCardBg,
                contentColor = volumeCardContent
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(stringResource(R.string.martelage_volume_price_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem(label = stringResource(R.string.martelage_label_v_total), value = "$vTotalText m³")
                    StatItem(label = stringResource(R.string.martelage_label_v_per_ha), value = "$vPerHaText m³/ha")
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = volumeCardContent.copy(alpha = 0.15f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem(label = stringResource(R.string.martelage_label_revenue), value = revenueTotalText)
                    StatItem(label = stringResource(R.string.martelage_label_revenue_per_ha), value = revenuePerHaText)
                }
            }
        }
    }
}

/**
 * Carte Surface terrière (G prélevé / G/ha) avec surface en ha.
 */
@Composable
internal fun BasalAreaCard(
    gTotal: Double,
    gPerHa: Double,
    surfaceHa: Double? = null,
    ratioVG: Double? = null
) {
    val surfaceCardBg = MaterialTheme.colorScheme.secondaryContainer
    val surfaceCardContent = ColorUtils.getContrastingTextColor(surfaceCardBg)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = surfaceCardBg,
            contentColor = surfaceCardContent
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(stringResource(R.string.martelage_basal_area_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(label = stringResource(R.string.martelage_label_g_total), value = "${formatG(gTotal)} m²")
                StatItem(label = stringResource(R.string.martelage_label_g_per_ha), value = "${formatG(gPerHa)} m²/ha")
            }
            if (surfaceHa != null || ratioVG != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = surfaceCardContent.copy(alpha = 0.15f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (surfaceHa != null) {
                        StatItem(
                            label = stringResource(R.string.martelage_label_surface),
                            value = String.format(Locale.getDefault(), "%.4f ha", surfaceHa)
                        )
                    }
                    if (ratioVG != null) {
                        StatItem(
                            label = stringResource(R.string.martelage_label_ratio_vg),
                            value = String.format(Locale.getDefault(), "%.1f", ratioVG)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Carte Densité & structure dendrométrique complète.
 */
@Composable
internal fun DensityCard(
    nTotal: Int,
    nPerHa: Double,
    dm: Double?,
    meanH: Double?,
    dg: Double?,
    hLorey: Double?,
    dMin: Double?,
    dMax: Double?,
    cvDiam: Double?,
    placeholderDash: String
) {
    val densityCardBg = MaterialTheme.colorScheme.tertiaryContainer
    val densityCardContent = ColorUtils.getContrastingTextColor(densityCardBg)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = densityCardBg,
            contentColor = densityCardContent
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(stringResource(R.string.martelage_density_structure_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(label = stringResource(R.string.martelage_label_n_total), value = "$nTotal")
                StatItem(label = stringResource(R.string.martelage_label_n_per_ha), value = "${formatIntPerHa(nPerHa)}/ha")
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = densityCardContent.copy(alpha = 0.15f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(label = stringResource(R.string.martelage_label_dm), value = "${formatDiameter(dm, placeholderDash)} cm")
                StatItem(label = stringResource(R.string.martelage_label_dg), value = "${formatDiameter(dg, placeholderDash)} cm")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(label = stringResource(R.string.martelage_label_hm), value = "${formatHeight(meanH, placeholderDash)} m")
                StatItem(label = stringResource(R.string.martelage_label_hlorey), value = "${formatHeight(hLorey, placeholderDash)} m")
            }
            if (dMin != null || dMax != null || cvDiam != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = densityCardContent.copy(alpha = 0.15f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (dMin != null && dMax != null) {
                        StatItem(
                            label = stringResource(R.string.martelage_label_d_range),
                            value = "${formatDiameter(dMin, placeholderDash)} – ${formatDiameter(dMax, placeholderDash)} cm"
                        )
                    }
                    if (cvDiam != null) {
                        StatItem(
                            label = stringResource(R.string.martelage_label_cv_diam),
                            value = String.format(Locale.getDefault(), "%.0f %%", cvDiam)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Carte distribution par classe de diamètre.
 */
@Composable
internal fun ClassDistributionCard(
    classDistribution: List<ClassDistEntry>,
    volumeAvailable: Boolean,
    animationsEnabled: Boolean
) {
    if (classDistribution.isEmpty()) return
    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val cardContent = ColorUtils.getContrastingTextColor(cardBg)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = cardBg,
            contentColor = cardContent
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(stringResource(R.string.martelage_class_distribution_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val maxN = classDistribution.maxOfOrNull { it.n } ?: 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                classDistribution.filter { it.n > 0 }.forEach { entry ->
                    val fraction = entry.n.toFloat() / maxN.coerceAtLeast(1).toFloat()
                    val barH = (fraction * 80).coerceAtLeast(6f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        Text(
                            "${entry.n}",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barH.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        )
                        Text(
                            "${entry.diamClass}",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // Légende
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.martelage_class_dist_legend_n),
                    style = MaterialTheme.typography.labelSmall,
                    color = cardContent.copy(alpha = 0.6f)
                )
                Text(
                    stringResource(R.string.martelage_class_dist_legend_diam),
                    style = MaterialTheme.typography.labelSmall,
                    color = cardContent.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Carte distribution qualité du bois (A/B/C/D).
 */
@Composable
internal fun QualityDistributionCard(
    qualityDistribution: List<QualityDistEntry>,
    assessedCount: Int,
    totalCount: Int
) {
    if (assessedCount == 0) return
    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val cardContent = ColorUtils.getContrastingTextColor(cardBg)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = cardBg,
            contentColor = cardContent
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.martelage_quality_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.martelage_quality_assessed_format, assessedCount, totalCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = cardContent.copy(alpha = 0.6f)
                )
            }

            qualityDistribution.forEach { entry ->
                val gradeColor = when (entry.grade) {
                    WoodQualityGrade.A -> Color(0xFF4CAF50)
                    WoodQualityGrade.B -> Color(0xFF2196F3)
                    WoodQualityGrade.C -> Color(0xFFFF9800)
                    WoodQualityGrade.D -> Color(0xFFF44336)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge grade
                    Surface(
                        color = gradeColor,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.width(32.dp)
                    ) {
                        Text(
                            entry.grade.shortLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    // Barre proportionnelle
                    val fraction = (entry.pct / 100.0).coerceIn(0.0, 1.0).toFloat()
                    Box(modifier = Modifier.weight(1f).height(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .clip(RoundedCornerShape(4.dp))
                                .background(gradeColor.copy(alpha = 0.6f))
                        )
                    }
                    // Count + pct
                    Text(
                        "${entry.count} (${String.format(Locale.getDefault(), "%.0f", entry.pct)}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = cardContent.copy(alpha = 0.8f),
                        modifier = Modifier.width(72.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Coverage indicator
            val coveragePct = if (totalCount > 0) assessedCount.toDouble() / totalCount * 100.0 else 0.0
            if (coveragePct < 100.0) {
                Text(
                    stringResource(R.string.martelage_quality_coverage_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = cardContent.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Tableau par essence enrichi (N, N%, V, V%, G, G%, Dm, Dg, €/m³, recette).
 */
@Composable
internal fun PerEssenceTable(
    perEssence: List<PerEssenceStats>,
    essences: List<Essence>,
    placeholderDash: String,
    euroSymbol: String
) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(stringResource(R.string.martelage_per_species_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

    val perEssenceCardBg = MaterialTheme.colorScheme.surfaceVariant
    val perEssenceCardContent = ColorUtils.getContrastingTextColor(perEssenceCardBg)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = perEssenceCardBg,
            contentColor = perEssenceCardContent
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in perEssence) {
                val tint = essenceTintColor(row.essenceCode, essences)
                val bg = tint?.copy(alpha = 0.12f)
                    ?: MaterialTheme.colorScheme.surface.copy(alpha = 0.04f)
                val rowTextColor = ColorUtils.getContrastingTextColor(bg)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = bg, contentColor = rowTextColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    row.essenceName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (row.dominantQuality != null) {
                                    val qColor = when (row.dominantQuality) {
                                        WoodQualityGrade.A -> Color(0xFF4CAF50)
                                        WoodQualityGrade.B -> Color(0xFF2196F3)
                                        WoodQualityGrade.C -> Color(0xFFFF9800)
                                        WoodQualityGrade.D -> Color(0xFFF44336)
                                    }
                                    Surface(
                                        color = qColor,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            row.dominantQuality.shortLabel,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                "N=${row.n} (${String.format(Locale.getDefault(), "%.0f", row.nPct)}%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = rowTextColor.copy(alpha = 0.7f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem(
                                label = "V",
                                value = "${formatVolume(row.vTotal)} m³ (${String.format(Locale.getDefault(), "%.0f", row.vPct)}%)",
                                color = rowTextColor
                            )
                            StatItem(
                                label = "G",
                                value = "${formatG(row.gTotal)} m² (${String.format(Locale.getDefault(), "%.0f", row.gPct)}%)",
                                color = rowTextColor
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem(
                                label = "Dm",
                                value = "${formatDiameter(row.dm, placeholderDash)} cm",
                                color = rowTextColor
                            )
                            StatItem(
                                label = "Dg",
                                value = "${formatDiameter(row.dg, placeholderDash)} cm",
                                color = rowTextColor
                            )
                            if (row.meanPricePerM3 != null) {
                                StatItem(
                                    label = "€/m³",
                                    value = formatPrice(row.meanPricePerM3, placeholderDash),
                                    color = rowTextColor
                                )
                            }
                        }
                        if (row.revenueTotal != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatItem(
                                    label = stringResource(R.string.martelage_label_revenue),
                                    value = formatMoney(row.revenueTotal, placeholderDash, euroSymbol),
                                    color = rowTextColor
                                )
                                StatItem(
                                    label = stringResource(R.string.martelage_label_revenue_per_ha),
                                    value = formatMoney(row.revenuePerHa, placeholderDash, euroSymbol),
                                    color = rowTextColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = LocalContentColor.current
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}
