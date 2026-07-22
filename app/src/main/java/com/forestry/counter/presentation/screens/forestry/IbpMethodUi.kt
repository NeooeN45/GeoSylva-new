package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.domain.model.IbpAnswers

@Composable
internal fun IbpMethodBanner(
    answers: IbpAnswers,
    readOnly: Boolean,
    onCreateCurrent: () -> Unit,
    onOpenReference: (() -> Unit)?
) {
    val isCurrent = answers.isCurrentMethod
    val accent = if (isCurrent) Color(0xFF2E7D32) else MaterialTheme.colorScheme.tertiary
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCurrent) Icons.Default.Verified else Icons.Default.Lock,
                    contentDescription = null,
                    tint = accent
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isCurrent) stringResource(R.string.ibp_method_current_title)
                    else stringResource(R.string.ibp_method_history_title, answers.methodLabel),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.weight(1f)
                )
                if (readOnly) {
                    Surface(color = accent.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            stringResource(R.string.ibp_read_only),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Text(
                if (isCurrent) stringResource(R.string.ibp_method_current_body)
                else stringResource(R.string.ibp_method_history_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isCurrent) {
                    Button(onClick = onCreateCurrent, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.ibp_start_v32))
                    }
                }
                if (onOpenReference != null) {
                    TextButton(onClick = onOpenReference) {
                        Icon(Icons.Default.MenuBook, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.cd_guide))
                    }
                }
            }
        }
    }
}

@Composable
internal fun IbpHistoryDetailsPanel(details: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.ibp_history_observations),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (details.isEmpty()) {
            Text(
                stringResource(R.string.ibp_history_no_details),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            details.forEach { detail ->
                Text("• $detail", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
