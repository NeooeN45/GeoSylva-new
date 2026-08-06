package com.forestry.counter.presentation.screens.explorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Écran Explorer — 13 catégories de données forestières.
 *
 * Spec GEOSYLVA-003 §29.4 : grille de catégories permettant de parcourir
 * toutes les entités du contrat de données.
 *
 * Chaque catégorie est une carte qui navigue vers la liste correspondante.
 * Pendant Lot 1, les catégories non encore implémentées affichent un
 * message "À venir" au clic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    onNavigateToForets: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onCategoryClick: (ExplorerCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Explorer") },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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

@Composable
private fun ExplorerCategoryCard(
    category: ExplorerCategory,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = category.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            if (!category.isImplemented) {
                Text(
                    text = "À venir",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
