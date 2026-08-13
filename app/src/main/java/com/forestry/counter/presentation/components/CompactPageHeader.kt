package com.forestry.counter.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
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
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.Space

/**
 * En-tête de page compact — le titre seul porte un fondu (pastille arrondie
 * translucide), sans barre pleine largeur derrière : c'est le traitement
 * "Bonjour" / "Accès rapide" de l'Accueil, jugé très réussi, étendu ici.
 * Une première version posait le titre sur une barre `background` pleine
 * largeur — jugée toujours trop massive malgré ses 52dp (contre 64-152dp
 * pour les composants Material par défaut).
 */
@Composable
fun CompactPageHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Space.sm, vertical = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = Space.xs))
        }
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
            shape = GsShape.md,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xxs),
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        actions()
    }
}
