package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.forestry.counter.R
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.screenH, vertical = Space.xl),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                shape = GsShape.lg,
            ) {
                Column(modifier = Modifier.padding(Space.md)) {
                    Text(
                        stringResource(R.string.carte_mode_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.carte_mode_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Space.xxs),
                    )
                }
            }
            Column(
                modifier = Modifier.padding(top = Space.lg),
                verticalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                CarteModeCard(
                    icon = Icons.Default.Map,
                    title = stringResource(R.string.carte_mode_maps_title),
                    description = stringResource(R.string.carte_mode_maps_desc),
                    accent = SemanticInfo,
                    onClick = { onSelectMode(CarteMode.MAPS) },
                )
                CarteModeCard(
                    icon = Icons.Default.TravelExplore,
                    title = stringResource(R.string.carte_mode_recherche_title),
                    description = stringResource(R.string.carte_mode_recherche_desc),
                    accent = SemanticSuccess,
                    onClick = { onSelectMode(CarteMode.RECHERCHE) },
                )
                CarteModeCard(
                    icon = Icons.Default.Public,
                    title = stringResource(R.string.carte_mode_libre_title),
                    description = stringResource(R.string.carte_mode_libre_desc),
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = { onSelectMode(CarteMode.LIBRE) },
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
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        shape = GsShape.lg,
    ) {
        Row(
            modifier = Modifier.padding(Space.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.16f),
                modifier = Modifier.size(Touch.fieldPrimary),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Space.xxs),
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
