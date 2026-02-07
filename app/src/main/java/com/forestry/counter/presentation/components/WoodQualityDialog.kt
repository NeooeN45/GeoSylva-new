package com.forestry.counter.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.calculation.quality.ClassificationResult
import com.forestry.counter.domain.calculation.quality.ProductClassifier
import com.forestry.counter.domain.calculation.quality.QualityAssessment
import com.forestry.counter.domain.calculation.quality.WoodQualityGrade

/**
 * Dialogue d'évaluation rapide de la qualité du bois.
 * 4 critères visuels avec sliders → score → grade A/B/C/D → produit + prix estimé.
 */
@Composable
fun WoodQualityDialog(
    essenceCode: String,
    categorie: String?,
    diamCm: Double,
    hauteurM: Double?,
    initialDetail: String? = null,
    animationsEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (grade: WoodQualityGrade, product: ClassificationResult, detailJson: String) -> Unit
) {
    // Restaurer les critères depuis le JSON sauvegardé
    val initial = remember(initialDetail) {
        try {
            if (initialDetail != null) {
                val obj = org.json.JSONObject(initialDetail)
                listOf(obj.optInt("r", 2), obj.optInt("b", 2), obj.optInt("s", 2), obj.optInt("d", 2))
            } else listOf(2, 2, 2, 2)
        } catch (_: Throwable) { listOf(2, 2, 2, 2) }
    }
    var rectitude by remember { mutableIntStateOf(initial[0]) }
    var branchage by remember { mutableIntStateOf(initial[1]) }
    var etatSanitaire by remember { mutableIntStateOf(initial[2]) }
    var defautsFut by remember { mutableIntStateOf(initial[3]) }

    val assessment = remember(rectitude, branchage, etatSanitaire, defautsFut) {
        QualityAssessment(
            tigeId = "",
            rectitude = rectitude,
            branchage = branchage,
            etatSanitaire = etatSanitaire,
            defautsFut = defautsFut
        )
    }

    val classification = remember(assessment.grade, essenceCode, categorie, diamCm, hauteurM) {
        ProductClassifier.classify(
            essenceCode = essenceCode,
            categorie = categorie,
            diamCm = diamCm,
            quality = assessment.grade,
            hauteurM = hauteurM
        )
    }

    val gradeColor = when (assessment.grade) {
        WoodQualityGrade.A -> Color(0xFF4CAF50)
        WoodQualityGrade.B -> Color(0xFF2196F3)
        WoodQualityGrade.C -> Color(0xFFFF9800)
        WoodQualityGrade.D -> Color(0xFFF44336)
    }

    val animatedGradeColor by animateColorAsState(
        targetValue = gradeColor,
        animationSpec = tween(
            durationMillis = if (animationsEnabled) 300 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "gradeColor"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Star, contentDescription = null, tint = animatedGradeColor) },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.quality_dialog_title))
                Surface(
                    color = animatedGradeColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = " ${assessment.grade.shortLabel} ",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Critère 1 : Rectitude
                QualityCriterion(
                    label = stringResource(R.string.quality_rectitude),
                    value = rectitude,
                    onValueChange = { rectitude = it },
                    labels = listOf(
                        stringResource(R.string.quality_rect_0),
                        stringResource(R.string.quality_rect_1),
                        stringResource(R.string.quality_rect_2),
                        stringResource(R.string.quality_rect_3)
                    )
                )

                // Critère 2 : Branchage
                QualityCriterion(
                    label = stringResource(R.string.quality_branching),
                    value = branchage,
                    onValueChange = { branchage = it },
                    labels = listOf(
                        stringResource(R.string.quality_branch_0),
                        stringResource(R.string.quality_branch_1),
                        stringResource(R.string.quality_branch_2),
                        stringResource(R.string.quality_branch_3)
                    )
                )

                // Critère 3 : État sanitaire
                QualityCriterion(
                    label = stringResource(R.string.quality_health),
                    value = etatSanitaire,
                    onValueChange = { etatSanitaire = it },
                    labels = listOf(
                        stringResource(R.string.quality_health_0),
                        stringResource(R.string.quality_health_1),
                        stringResource(R.string.quality_health_2),
                        stringResource(R.string.quality_health_3)
                    )
                )

                // Critère 4 : Défauts du fût
                QualityCriterion(
                    label = stringResource(R.string.quality_defects),
                    value = defautsFut,
                    onValueChange = { defautsFut = it },
                    labels = listOf(
                        stringResource(R.string.quality_defect_0),
                        stringResource(R.string.quality_defect_1),
                        stringResource(R.string.quality_defect_2),
                        stringResource(R.string.quality_defect_3)
                    )
                )

                HorizontalDivider()

                // Résultat
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.quality_result_grade, assessment.grade.label),
                        style = MaterialTheme.typography.titleSmall,
                        color = animatedGradeColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.quality_result_product, classification.primary.label),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (classification.secondary != null) {
                        Text(
                            text = stringResource(R.string.quality_result_secondary, classification.secondary.label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = classification.qualityNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val detailJson = """{"r":$rectitude,"b":$branchage,"s":$etatSanitaire,"d":$defautsFut}"""
                onConfirm(assessment.grade, classification, detailJson)
            }) {
                Text(stringResource(R.string.validate))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun QualityCriterion(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    labels: List<String>
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            labels.forEachIndexed { index, lbl ->
                val selected = value == index
                val bgColor = if (selected) {
                    when (index) {
                        0 -> Color(0xFFF44336).copy(alpha = 0.2f)
                        1 -> Color(0xFFFF9800).copy(alpha = 0.2f)
                        2 -> Color(0xFF2196F3).copy(alpha = 0.2f)
                        3 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }
                val borderColor = if (selected) {
                    when (index) {
                        0 -> Color(0xFFF44336)
                        1 -> Color(0xFFFF9800)
                        2 -> Color(0xFF2196F3)
                        3 -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.primary
                    }
                } else Color.Transparent

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onValueChange(index) },
                    color = bgColor,
                    shape = RoundedCornerShape(8.dp),
                    border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, borderColor) else null
                ) {
                    Text(
                        text = lbl,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        maxLines = 2,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
