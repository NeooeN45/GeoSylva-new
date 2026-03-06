package com.forestry.counter.presentation.screens.forestry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestry.counter.domain.model.IbpCriterionData
import com.forestry.counter.domain.model.IbpCriterionId
import com.forestry.counter.domain.model.IbpMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IbpReferenceScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guide IBP Terrain", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Header intro ─────────────────────────────────────────
            item { IbpReferenceHeader() }

            // ── Scoring summary ──────────────────────────────────────
            item { IbpScoringSummaryCard() }

            // ── Mode quick guide ─────────────────────────────────────
            item { IbpModesCard() }

            // ── Criteria fiches A–J ──────────────────────────────────
            item {
                Text(
                    "Fiches critères A–J",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
                )
            }
            itemsIndexed(IbpCriterionId.ALL) { _, cid ->
                IbpCriterieFiche(cid)
            }

            // ── Footer credit ────────────────────────────────────────
            item { IbpReferenceFooter() }
        }
    }
}

/* ─────────────── Header ─────────────────────────────────────────── */
@Composable
private fun IbpReferenceHeader() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.EmojiNature, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                Column {
                    Text("IBP – Indice de Biodiversité Potentielle",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Méthode CNPF / IDF – Version 3.2",
                        style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .8f))
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = .3f))
            Text(
                "L'IBP évalue la biodiversité potentielle d'un peuplement forestier en notant 10 critères " +
                "répartis en deux groupes (A–G = Peuplement & Gestion ; H–J = Contexte). " +
                "Chaque critère vaut 0, 2 ou 5 points. Score total sur 50 points.",
                style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .9f)
            )
        }
    }
}

/* ─────────────── Scoring summary ───────────────────────────────── */
@Composable
private fun IbpScoringSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Niveaux IBP", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            val levels = listOf(
                "Très faible" to (0 to 9) to Color(0xFFC62828),
                "Faible" to (10 to 19) to Color(0xFFE65100),
                "Moyen" to (20 to 29) to Color(0xFFF9A825),
                "Bon" to (30 to 39) to Color(0xFF2E7D32),
                "Très bon" to (40 to 50) to Color(0xFF1565C0)
            )
            levels.forEach { (labelRange, color) ->
                val (label, range) = labelRange
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(14.dp).clip(CircleShape).background(color))
                    Text("${range.first}–${range.second} pts", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold, color = color, modifier = Modifier.width(80.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall)
                }
            }
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ScoreGroupChip(label = "Groupe A (Peuplement)", max = 35, color = Color(0xFF2E7D32))
                ScoreGroupChip(label = "Groupe B (Contexte)", max = 15, color = Color(0xFF1565C0))
            }
        }
    }
}

@Composable
private fun ScoreGroupChip(label: String, max: Int, color: Color) {
    Surface(color = color.copy(alpha = .1f), shape = RoundedCornerShape(10.dp)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Text("$label / $max", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ─────────────── Modes guide ────────────────────────────────────── */
@Composable
private fun IbpModesCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Modes d'évaluation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            val modes = listOf(
                IbpMode.COMPLET    to ("Complet (A–J)" to "Évaluation complète des 10 critères. Recommandé pour une première visite ou bilan biodiversité."),
                IbpMode.RAPIDE     to ("Rapide (A,C,E,F,H)" to "5 critères essentiels pour une évaluation terrain rapide en moins de 30 min."),
                IbpMode.BOIS_MORT  to ("Bois mort (C,D,E)" to "Focus sur les critères de bois mort et gros bois. Idéal après coupe ou inventaire."),
                IbpMode.CONTEXTE   to ("Contexte (H,I,J)" to "Critères de contexte écologique uniquement. Sur carte/Géoportail possible."),
                IbpMode.PEUPLEMENT to ("Peuplement (A–G)" to "Groupe A uniquement. Pour évaluation intra-peuplement sans contexte paysager.")
            )
            modes.forEach { (mode, info) ->
                val (label, desc) = info
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(color = Color(0xFF1B5E20).copy(alpha = .1f), shape = RoundedCornerShape(6.dp)) {
                        Text(mode.name, style = MaterialTheme.typography.labelSmall, color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/* ─────────────── Per-criterion fiche ───────────────────────────── */
@Composable
private fun IbpCriterieFiche(cid: IbpCriterionId) {
    var expanded by remember { mutableStateOf(false) }
    val groupColor = if (cid.group == com.forestry.counter.domain.model.IbpGroup.A) Color(0xFF2E7D32) else Color(0xFF1565C0)
    val thresholds = IbpCriterionData.thresholds(cid)
    val protocol  = IbpCriterionData.protocol(cid)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(if (expanded) 4.dp else 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, groupColor.copy(alpha = .2f))
    ) {
        Column {
            // ── Header (always visible) ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = groupColor, shape = RoundedCornerShape(8.dp)) {
                    Text(cid.displayCode, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold, color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(ibpCriterionFullTitle(cid), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(ibpCriterionShortDesc(cid), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Threshold chips inline
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    thresholds.forEach { (_, pts) ->
                        val c = when (pts) { 5 -> Color(0xFF2E7D32); 2 -> Color(0xFFF9A825); 1 -> Color(0xFFE65100); else -> Color(0xFFC62828) }
                        Surface(color = c, shape = CircleShape) {
                            Text("$pts", style = MaterialTheme.typography.labelSmall, color = Color.White,
                                fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp))
            }

            // ── Expanded content ────────────────────────────────────
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = groupColor.copy(alpha = .2f))

                    // Threshold table
                    Text("Seuils de notation", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold, color = groupColor)
                    thresholds.forEach { (label, pts) ->
                        val rowColor = when (pts) { 5 -> Color(0xFF2E7D32); 2 -> Color(0xFFF9A825); 1 -> Color(0xFFE65100); else -> Color(0xFFC62828) }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(color = rowColor, shape = RoundedCornerShape(6.dp)) {
                                Text("$pts pts", style = MaterialTheme.typography.labelMedium, color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }

                    HorizontalDivider(color = groupColor.copy(alpha = .1f))

                    // Protocol steps
                    Text("Protocole terrain", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold, color = groupColor)
                    protocol.forEachIndexed { idx, step ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                            Surface(color = groupColor, shape = CircleShape) {
                                Text("${idx + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp).defaultMinSize(minWidth = 18.dp),
                                    textAlign = TextAlign.Center)
                            }
                            Text(step, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/* ─────────────── Footer ─────────────────────────────────────────── */
@Composable
private fun IbpReferenceFooter() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Text("Source : CNPF / IDF – IBP v3.2 (2020)",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic)
        Text("Gurnell et al. (2009) & Larrieu et al. (2012)",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic)
    }
}

/* ─────────────── Criterion display helpers ─────────────────────── */
private fun ibpCriterionFullTitle(cid: IbpCriterionId): String = when (cid) {
    IbpCriterionId.E1  -> "A – Essences autochtones"
    IbpCriterionId.E2  -> "B – Strates de végétation"
    IbpCriterionId.BMS -> "C – Bois mort sur pied"
    IbpCriterionId.BMC -> "D – Bois mort au sol"
    IbpCriterionId.GB  -> "E – Très gros bois vivants"
    IbpCriterionId.DMH -> "F – Dendromicrohabitats"
    IbpCriterionId.VS  -> "G – Milieux ouverts florifères"
    IbpCriterionId.CF  -> "H – Ancienneté & continuité"
    IbpCriterionId.CO  -> "I – Milieux aquatiques"
    IbpCriterionId.HC  -> "J – Milieux rocheux"
}

private fun ibpCriterionShortDesc(cid: IbpCriterionId): String = when (cid) {
    IbpCriterionId.E1  -> "Nb de genres d'essences autochtones ≥7.5 cm ∅"
    IbpCriterionId.E2  -> "Nb de strates de végétation présentes (5 max)"
    IbpCriterionId.BMS -> "Nb de chicots/bois morts debout ≥17.5 cm /ha"
    IbpCriterionId.BMC -> "Nb de troncs/bois morts au sol ≥17.5 cm /ha"
    IbpCriterionId.GB  -> "Nb d'arbres vivants >47.5–67.5 cm ∅ /ha"
    IbpCriterionId.DMH -> "Nb d'arbres porteurs d'au moins un dmh /ha"
    IbpCriterionId.VS  -> "Surface de milieux ouverts florifères (% placette)"
    IbpCriterionId.CF  -> "Présence/absence de forêt ancienne (XIXe siècle)"
    IbpCriterionId.CO  -> "Nb de types de milieux aquatiques à proximité"
    IbpCriterionId.HC  -> "Nb de types de milieux rocheux à proximité"
}
