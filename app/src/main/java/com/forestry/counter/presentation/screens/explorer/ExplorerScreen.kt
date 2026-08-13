package com.forestry.counter.presentation.screens.explorer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.forestry.counter.R
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.presentation.components.CompactPageHeader
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.Motion
import com.forestry.counter.presentation.theme.Space

/**
 * Écran Explorer — 13 catégories de données forestières.
 *
 * Spec GEOSYLVA-003 §29.4 : grille de catégories permettant de parcourir
 * toutes les entités du contrat de données.
 *
 * Refondu dans le style de l'accueil Réglages (cartes arrondies sur
 * `surfaceContainerHigh`, icône en pastille de couleur, pression animée) —
 * même fond d'écran par défaut que l'Accueil pour rester le même produit
 * d'un onglet à l'autre.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    onNavigateToForets: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onCategoryClick: (ExplorerCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferencesManager = LocalContext.current.let { remember(it) { UserPreferencesManager(it) } }
    val backgroundImageEnabled by preferencesManager.backgroundImageEnabled.collectAsStateWithLifecycle(initialValue = true)
    val backgroundImageUri by preferencesManager.backgroundImageUri.collectAsStateWithLifecycle(initialValue = null)

    Box(modifier = modifier.fillMaxSize()) {
        if (backgroundImageEnabled) {
            val uriString = backgroundImageUri
            if (uriString != null) {
                AsyncImage(
                    model = uriString,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.forest_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = { CompactPageHeader(title = stringResource(R.string.explorer_title)) },
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Space.screenH,
                    end = Space.screenH,
                    top = innerPadding.calculateTopPadding() + Space.sm,
                    bottom = innerPadding.calculateBottomPadding() + Space.xxl,
                ),
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                verticalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                items(ExplorerCategory.entries) { category ->
                    ExplorerCategoryCard(
                        category = category,
                        onClick = {
                            when (category) {
                                ExplorerCategory.FORETS -> onNavigateToForets()
                                ExplorerCategory.PROJECTS -> onNavigateToProjects()
                                else -> onCategoryClick(category)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplorerCategoryCard(
    category: ExplorerCategory,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) Motion.PRESS_SCALE else 1f,
        animationSpec = Motion.springSnappy(),
        label = "explorerCardScale",
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale),
        shape = GsShape.lg,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(Space.md).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Column {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!category.isImplemented) {
                    Text(
                        text = stringResource(R.string.explorer_coming_soon),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Les 13 catégories de l'Explorer — spec GESYLVA-003 §29.4.
 */
enum class ExplorerCategory(
    val label: String,
    val icon: ImageVector,
    val isImplemented: Boolean = false,
) {
    FORETS("Forêts", Icons.Filled.Forest, isImplemented = true),
    PARCELLES("Parcelles", Icons.Filled.Park),
    PLACETTES("Placettes", Icons.Filled.Map),
    ARBRES("Arbres", Icons.Filled.Category),
    OBSERVATIONS("Observations", Icons.Filled.Biotech),
    MESURES("Mesures", Icons.Filled.Straighten),
    CALCULS("Calculs", Icons.Filled.Calculate),
    PREUVES("Preuves", Icons.Filled.PhotoLibrary),
    ESSENCES("Essences", Icons.Filled.Hub, isImplemented = true),
    STATIONS("Stations", Icons.Filled.Science),
    DIAGNOSTICS("Diagnostics", Icons.Filled.BarChart),
    PROJECTS("Projets", Icons.Filled.AccountTree, isImplemented = true),
    EVENEMENTS("Événements", Icons.Filled.Event),
}
