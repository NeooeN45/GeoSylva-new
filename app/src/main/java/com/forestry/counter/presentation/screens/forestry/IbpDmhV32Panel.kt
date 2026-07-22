package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.model.IbpCriterionData

@Composable
internal fun IbpDmhV32Panel(
    counts: Map<String, Float>,
    onGroupCountChange: (String, Float) -> Unit
) {
    val cappedTotal = IbpCriterionData.dmhCappedTotal(counts)
    val score = IbpCriterionData.scoreFFromCounts(cappedTotal)
    val scoreColor = when (score) {
        5 -> Color(0xFF2E7D32)
        2 -> Color(0xFFF9A825)
        1 -> Color(0xFFE65100)
        else -> Color(0xFFC62828)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(R.string.ibp_dmh_v32_instruction),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = scoreColor.copy(alpha = 0.10f)),
            border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.ibp_dmh_v32_total, cappedTotal),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(color = scoreColor, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "$score / 5",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = (cappedTotal / 8f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = scoreColor
                )
                Text(
                    "< 2 = 0 · 2 à < 3 = 1 · 3 à < 8 = 2 · ≥ 8 = 5",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IbpCriterionData.dmhTypes.forEachIndexed { index, label ->
            val key = IbpCriterionData.dmhGroupKey(index)
            val value = counts[key]?.takeIf { it.isFinite() && it >= 0f } ?: 0f
            IbpDmhGroupRow(
                label = label,
                value = value,
                onValueChange = { onGroupCountChange(key, it) }
            )
        }
    }
}

@Composable
private fun IbpDmhGroupRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    var text by remember(value) {
        mutableStateOf(if (value == 0f) "" else value.toString())
    }
    val retained = value.coerceIn(0f, 2f)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { raw ->
                        if (raw.length <= 7) {
                            text = raw
                            val normalized = raw.replace(',', '.')
                            normalized.toFloatOrNull()
                                ?.takeIf { it.isFinite() && it >= 0f }
                                ?.let(onValueChange)
                            if (raw.isEmpty()) onValueChange(0f)
                        }
                    },
                    modifier = Modifier.width(126.dp),
                    label = { Text(stringResource(R.string.ibp_dmh_group_count)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Text(
                    stringResource(R.string.ibp_dmh_group_capped, retained),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (value > 2f) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
