package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.forestry.counter.R
import com.forestry.counter.presentation.components.CompactPageHeader
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.Space

/**
 * Contenu provisoire d'un mode Carte pas encore construit (Maps, Libre —
 * voir plan de refonte Carte, Phase B). Header avec retour vers le
 * sélecteur de mode ; pas de "onNavigateBack" nul ici, ce n'est jamais
 * l'écran d'entrée de l'onglet.
 */
@Composable
fun CarteModeStubScreen(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Construction,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.fillMaxSize()) {
            CompactPageHeader(title = title, onBack = onBack)
            Box(modifier = Modifier.fillMaxSize().padding(Space.xl), contentAlignment = Alignment.Center) {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                    shape = GsShape.md,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space.md),
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(Space.xxl),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
