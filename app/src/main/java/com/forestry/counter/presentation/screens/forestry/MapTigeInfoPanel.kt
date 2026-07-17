package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.forestry.counter.presentation.theme.SemanticInfo
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Carte d'information affichée quand l'utilisateur tape sur une tige.
 */
@Composable
internal fun MapTigeInfoPanel(
    tappedTree: TappedTreeInfo?,
    navActive: Boolean,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartNavigation: (TappedTreeInfo) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTapped = tappedTree ?: return
    if (navActive) return

    Card(
        modifier = modifier
            .widthIn(max = 340.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val catLabel = when (currentTapped.categorie) {
                    "DEPERISSANT" -> "\u26A0 Dépérissant"
                    "ARBRE_BIO" -> "\uD83C\uDF3F Arbre bio"
                    "MORT" -> "\uD83D\uDC80 Mort"
                    "PARASITE" -> "\uD83D\uDC1B Parasité"
                    else -> null
                }
                Column {
                    if (catLabel != null) {
                        Text(catLabel, style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF6C00))
                    }
                    Text(
                        currentTapped.essenceName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), modifier = Modifier.size(16.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                currentTapped.diamCm?.let {
                    Text(stringResource(R.string.map_diam_format, it.roundToInt()), style = MaterialTheme.typography.bodySmall)
                }
                currentTapped.hauteurM?.let {
                    Text(stringResource(R.string.map_height_label, it.roundToInt()), style = MaterialTheme.typography.bodySmall)
                }
                currentTapped.precisionM?.let {
                    Text(stringResource(R.string.map_precision_format, String.format(Locale.US, "%.1f", it)), style = MaterialTheme.typography.bodySmall)
                }
            }
            SmallFloatingActionButton(
                onClick = {
                    if (!hasLocationPermission) {
                        onRequestPermission()
                    } else {
                        onStartNavigation(currentTapped)
                        onDismiss()
                    }
                },
                containerColor = SemanticInfo,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = stringResource(R.string.cd_navigate), modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.nav_navigate_to), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
