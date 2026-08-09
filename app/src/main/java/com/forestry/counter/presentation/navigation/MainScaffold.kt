package com.forestry.counter.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.forestry.counter.ForestryCounterApplication
import com.forestry.counter.presentation.screens.account.AccountScreen
import com.forestry.counter.presentation.screens.common.ComingSoonScreen
import com.forestry.counter.presentation.screens.explorer.ExplorerCategory
import com.forestry.counter.presentation.screens.explorer.ExplorerScreen
import com.forestry.counter.presentation.screens.home.HomeScreen
import com.forestry.counter.presentation.screens.home.HomeViewModel

/**
 * Scaffold principal avec bottom navigation 5 entrées — spec GEOSYLVA-003 §29.3.
 *
 * Affiche la bottom bar sur les 5 destinations de premier niveau
 * (Accueil, Explorer, Missions, Carte, Compte) et la masque sur les
 * sous-routes (parcelles, placettes, etc.).
 *
 * Pendant Lot 1 :
 *   - Accueil et Explorer sont des placeholders (implémentés en 3.2/3.3)
 *   - Missions, Carte, Compte affichent [ComingSoonScreen]
 *   - Les anciennes routes Forets/Settings ne sont plus accessibles depuis
 *     la bottom bar (décision Fondateur — redirect en Sprint 3.4)
 */
@Composable
fun MainScaffold(
    navController: NavHostController,
    app: ForestryCounterApplication,
    content: @Composable (Modifier) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = BottomNavDestination.fromRoute(currentRoute)
    val showBottomBar = currentDestination != null

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(destination.route) {
                                        // Évite la pile de destinations de premier niveau
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = if (currentRoute == destination.route)
                                        destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}

/**
 * Contenu des 5 onglets de premier niveau.
 *
 * Accueil affiche [HomeScreen] (tableau de bord).
 * Explorer est un placeholder en attendant Sprint 3.3.
 * Missions, Carte, Compte affichent [ComingSoonScreen].
 */
@Composable
fun TopLevelTabContent(
    route: String,
    app: ForestryCounterApplication,
    onNavigateToExplorer: () -> Unit,
    onNavigateToForet: (String) -> Unit,
    onNavigateToForets: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onCreateForest: () -> Unit,
    onCategoryClick: (ExplorerCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (route) {
        BottomNavDestination.ACCUEIL.route -> {
            val viewModel = HomeViewModel(app.foretRepository, app.parcelleRepository)
            HomeScreen(
                viewModel = viewModel,
                onNavigateToExplorer = onNavigateToExplorer,
                onNavigateToForet = onNavigateToForet,
                onCreateForest = onCreateForest,
                modifier = modifier,
            )
        }
        BottomNavDestination.EXPLORER.route -> {
            ExplorerScreen(
                onNavigateToForets = onNavigateToForets,
                onNavigateToProjects = onNavigateToProjects,
                onCategoryClick = onCategoryClick,
                modifier = modifier,
            )
        }
        BottomNavDestination.MISSIONS.route -> {
            ComingSoonScreen("Missions", modifier)
        }
        BottomNavDestination.CARTE.route -> {
            ComingSoonScreen("Carte", modifier)
        }
        BottomNavDestination.COMPTE.route -> {
            // L'écran existait déjà mais n'était atteignable que par les
            // réglages — l'onglet affichait « À venir ». ID-F-013 impose un
            // espace compte distinct de la page de connexion.
            AccountScreen(
                repository = app.identityRepository,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateBack = null,
                modifier = modifier,
            )
        }
    }
}
