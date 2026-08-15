package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.forestry.counter.R
import com.forestry.counter.presentation.theme.Elevation
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.SemanticInfo
import com.forestry.counter.presentation.theme.SemanticSuccess
import com.forestry.counter.presentation.theme.Space
import com.forestry.counter.presentation.theme.Touch

/**
 * Les 3 usages distincts de la carte, choisis explicitement avant d'entrer
 * dans l'un d'eux — pas de carte visible tant qu'aucun mode n'est choisi.
 */
enum class CarteMode { MAPS, RECHERCHE, LIBRE }

/**
 * Écran d'accueil de l'onglet Carte : sélection du mode, rien d'autre.
 * Toujours le premier écran affiché en arrivant sur l'onglet (l'onglet ne
 * mémorise pas le dernier mode utilisé — voir MainScaffold.navigateToTab).
 * Seuls 3 choix existent : chaque bouton occupe un tiers de la hauteur
 * disponible, pas de défilement.
 */
@Composable
fun CarteModeSelectorScreen(
    onSelectMode: (CarteMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.fillMaxSize().padding(Space.screenH)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = Space.xl, bottom = Space.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                    shape = GsShape.md,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.sm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            stringResource(R.string.carte_mode_title),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            stringResource(R.string.carte_mode_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = Space.xs),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Space.md),
            ) {
                CarteModeCard(
                    icon = Icons.Default.Map,
                    title = stringResource(R.string.carte_mode_maps_title),
                    description = stringResource(R.string.carte_mode_maps_desc),
                    accent = SemanticInfo,
                    onClick = { onSelectMode(CarteMode.MAPS) },
                    modifier = Modifier.weight(1f),
                )
                CarteModeCard(
                    icon = Icons.Default.TravelExplore,
                    title = stringResource(R.string.carte_mode_recherche_title),
                    description = stringResource(R.string.carte_mode_recherche_desc),
                    accent = SemanticSuccess,
                    onClick = { onSelectMode(CarteMode.RECHERCHE) },
                    modifier = Modifier.weight(1f),
                )
                CarteModeCard(
                    icon = Icons.Default.Public,
                    title = stringResource(R.string.carte_mode_libre_title),
                    description = stringResource(R.string.carte_mode_libre_desc),
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = { onSelectMode(CarteMode.LIBRE) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CarteModeCard(
    icon: ImageVector,
    title: String,
    description: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
        shape = GsShape.xl,
        shadowElevation = Elevation.overlay,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = Space.lg, vertical = Space.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.16f),
                modifier = Modifier.size(Touch.fieldPrimary),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(Space.lg),
                    )
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Space.xs),
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Space.xxs),
            )
        }
    }
}
