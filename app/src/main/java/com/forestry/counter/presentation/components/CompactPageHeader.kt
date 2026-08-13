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
 * Centré sur toute la largeur (indépendamment de la présence d'une flèche
 * retour ou d'actions) et un cran plus grand — la première version, alignée
 * à gauche en `titleLarge`, restait perçue comme trop petite et décentrée.
 */
@Composable
fun CompactPageHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Space.sm, vertical = Space.sm),
    ) {
        if (onBack != null) {
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                shape = CircleShape,
                modifier = Modifier.size(44.dp).align(Alignment.CenterStart),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
            shape = GsShape.md,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = Space.md, vertical = Space.xs),
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            actions()
        }
    }
}
