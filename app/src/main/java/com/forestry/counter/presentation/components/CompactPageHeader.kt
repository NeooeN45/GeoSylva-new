package com.forestry.counter.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forestry.counter.presentation.theme.Space

/**
 * En-tête de page compact — 52dp, contre les 64dp d'un `TopAppBar`
 * Material standard (152dp pour un `LargeTopAppBar`). Introduit après deux
 * retours indépendants ("la boîte de texte est encore trop haute") sur
 * Explorer et Réglages : les composants Material par défaut n'offrent pas
 * de palier plus compact, d'où ce remplacement entièrement personnalisé.
 *
 * Garde volontairement un fond (`background`, pas transparent) — la demande
 * était de réduire la boîte, pas de la supprimer.
 */
@Composable
fun CompactPageHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(52.dp)
                .padding(horizontal = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            } else {
                Spacer(modifier = Modifier.width(Space.md))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).padding(start = if (onBack != null) Space.xs else 0.dp),
            )
            actions()
            Spacer(modifier = Modifier.width(Space.xs))
        }
    }
}
