package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestry.counter.R
import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.model.Tige
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.repository.TigeRepository
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Couleurs palette dashboard ──
private val CHART_COLORS = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFF44336), Color(0xFFFF9800),
    Color(0xFF9C27B0), Color(0xFF009688), Color(0xFF795548), Color(0xFF607D8B),
    Color(0xFFE91E63), Color(0xFF3F51B5), Color(0xFFCDDC39), Color(0xFF00BCD4),
    Color(0xFFFF5722), Color(0xFF8BC34A), Color(0xFF673AB7), Color(0xFFFFC107)
)

private fun essenceDisplayColor(essence: Essence?, index: Int): Color {
    essence?.colorHex?.let { hex ->
        return try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { CHART_COLORS[index % CHART_COLORS.size] }
    }
    return CHART_COLORS[index % CHART_COLORS.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    parcelleId: String,
    tigeRepository: TigeRepository,
    essenceRepository: EssenceRepository,
    onNavigateBack: () -> Unit
) {
    val tiges by tigeRepository.getTigesByParcelle(parcelleId).collectAsState(initial = emptyList())
    val essences by essenceRepository.getAllEssences().collectAsState(initial = emptyList())
    val essenceMap = remember(essences) { essences.associateBy { it.code.uppercase() } }

    // ── Données agrégées ──
    val tigesByEssence = remember(tiges) {
        tiges.groupBy { it.essenceCode.uppercase() }
            .entries.sortedByDescending { it.value.size }
    }

    val diamClasses = remember(tiges) {
        tiges.groupBy { ((it.diamCm / 5).toInt() * 5) }
            .toSortedMap()
            .mapValues { it.value.size }
    }

    // Surface terrière par essence (G = π/4 * d² * N, en m²)
    val gByEssence = remember(tiges) {
        tiges.groupBy { it.essenceCode.uppercase() }
            .mapValues { (_, list) -> list.sumOf { PI / 4.0 * (it.diamCm / 100.0) * (it.diamCm / 100.0) } }
            .entries.sortedByDescending { it.value }
    }

    val totalG = remember(gByEssence) { gByEssence.sumOf { it.value } }
    val totalTiges = tiges.size
    val avgDiam = remember(tiges) { if (tiges.isEmpty()) 0.0 else tiges.sumOf { it.diamCm } / tiges.size }
    val tigesWithHeight = remember(tiges) { tiges.filter { (it.hauteurM ?: 0.0) > 0.0 } }
    val avgHeight = remember(tigesWithHeight) { if (tigesWithHeight.isEmpty()) 0.0 else tigesWithHeight.sumOf { it.hauteurM ?: 0.0 } / tigesWithHeight.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.dashboard_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Résumé chiffres clés (2×2 grid) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard(
                    label = stringResource(R.string.dashboard_total_stems),
                    value = "$totalTiges",
                    icon = { Icon(Icons.Default.Forest, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f)
                )
                SummaryMiniCard(
                    label = stringResource(R.string.dashboard_avg_diam),
                    value = String.format("%.1f cm", avgDiam),
                    icon = { Icon(Icons.Default.Straighten, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard(
                    label = stringResource(R.string.dashboard_basal_area),
                    value = String.format("%.3f m²", totalG),
                    icon = { Icon(Icons.Default.PieChart, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f)
                )
                if (avgHeight > 0.0) {
                    SummaryMiniCard(
                        label = stringResource(R.string.dashboard_avg_height),
                        value = String.format("%.1f m", avgHeight),
                        icon = { Icon(Icons.Default.BarChart, null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // ── Donut : répartition par essence ──
            if (tigesByEssence.isNotEmpty()) {
                DashboardCard(title = stringResource(R.string.dashboard_species_distribution)) {
                    EssenceDonutChart(
                        data = tigesByEssence.map { (code, list) ->
                            Triple(
                                essenceMap[code]?.name ?: code,
                                list.size,
                                essenceDisplayColor(essenceMap[code], tigesByEssence.indexOfFirst { it.key == code })
                            )
                        },
                        total = totalTiges
                    )
                }
            }

            // ── Barres : distribution des diamètres ──
            if (diamClasses.isNotEmpty()) {
                DashboardCard(title = stringResource(R.string.dashboard_diameter_distribution)) {
                    DiameterBarChart(diamClasses)
                }
            }

            // ── Barres horizontales : surface terrière par essence ──
            if (gByEssence.isNotEmpty()) {
                DashboardCard(title = stringResource(R.string.dashboard_basal_area_by_species)) {
                    BasalAreaBars(
                        data = gByEssence.map { (code, g) ->
                            Triple(
                                essenceMap[code]?.name ?: code,
                                g,
                                essenceDisplayColor(essenceMap[code], gByEssence.indexOfFirst { it.key == code })
                            )
                        },
                        maxG = gByEssence.maxOfOrNull { it.value } ?: 1.0
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ════════════════════════════════════════════════
// Composables internes
// ════════════════════════════════════════════════

@Composable
private fun SummaryMiniCard(
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
private fun DashboardCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

// ── Donut chart ──
@Composable
private fun EssenceDonutChart(
    data: List<Triple<String, Int, Color>>, // name, count, color
    total: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut
        Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val strokeWidth = 28.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val topLeft = Offset(
                    (size.width - 2 * radius - strokeWidth) / 2f,
                    (size.height - 2 * radius - strokeWidth) / 2f
                )
                val arcSize = Size(2 * radius + strokeWidth, 2 * radius + strokeWidth)
                var startAngle = -90f
                data.forEach { (_, count, color) ->
                    val sweep = 360f * count / total
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep - 1.5f, // gap
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweep
                }
            }
            Text("$total", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Légende
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            data.take(10).forEach { (name, count, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = CircleShape,
                        color = color
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    val pct = if (total > 0) count * 100 / total else 0
                    Text(
                        "$name ($count • $pct%)",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (data.size > 10) {
                Text(
                    "+${data.size - 10} ${stringResource(R.string.dashboard_others)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Bar chart : classes de diamètre ──
@Composable
private fun DiameterBarChart(classes: Map<Int, Int>) {
    val maxCount = classes.values.maxOrNull() ?: 1
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        classes.forEach { (cls, count) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.width(38.dp)
            ) {
                Text(
                    "$count",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontSize = 10.sp
                )
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height((140.dp * count / maxCount).coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(color = barColor)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "$cls",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// ── Horizontal bars : G par essence ──
@Composable
private fun BasalAreaBars(
    data: List<Triple<String, Double, Color>>,
    maxG: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        data.take(12).forEach { (name, g, color) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(90.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f).height(16.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = (size.width * g / maxG).toFloat().coerceAtLeast(4f)
                        drawRoundRect(
                            color = color,
                            size = Size(barWidth, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    String.format("%.3f", g),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(52.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
