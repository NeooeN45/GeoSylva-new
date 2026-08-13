package com.forestry.counter.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.forestry.counter.ForestryCounterApplication
import com.forestry.counter.presentation.screens.settings.SettingsHomeScreen
import com.forestry.counter.presentation.screens.common.ComingSoonScreen
import com.forestry.counter.presentation.screens.explorer.ExplorerCategory
import com.forestry.counter.presentation.screens.explorer.ExplorerScreen
import com.forestry.counter.presentation.screens.home.HomeScreen
import com.forestry.counter.presentation.screens.home.HomeViewModel
import com.forestry.counter.presentation.theme.Space

/**
 * Scaffold principal avec bottom navigation 5 entrées — spec GEOSYLVA-003 §29.3.
 *
 * Sur les 5 destinations de premier niveau (Accueil, Explorer, Missions,
 * Carte, Réglages), la barre reste affichée en permanence. Sur une
 * sous-page (Forêts, Projets, fiche forêt…), elle se replie automatiquement
 * avec une animation — plus d'espace pour le contenu — et une petite
 * poignée en bas de l'écran permet de la faire réapparaître d'un geste.
 * Elle se replie de nouveau dès qu'on change de route.
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
    val isTopLevel = currentDestination != null

    var manuallyRevealed by remember { mutableStateOf(false) }
    LaunchedEffect(currentRoute) { manuallyRevealed = false }
    val barVisible = isTopLevel || manuallyRevealed

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = barVisible,
                    enter = slideInVertically(animationSpec = tween(220)) { it } + fadeIn(tween(220)),
                    exit = slideOutVertically(animationSpec = tween(180)) { it } + fadeOut(tween(180)),
                ) {
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
                                    Icon(
                                        imageVector = if (currentRoute == destination.route)
                                            destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.label,
                                    )
                                },
                                label = {
                                    Text(
                                        destination.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }

        // Poignée de réouverture — visible uniquement sur une sous-page dont
        // la barre est repliée. Le tap suffit ; à la différence d'un simple
        // "plus d'espace, plus de navigation", elle reste accessible d'un
        // geste, jamais à plus d'un tap.
        AnimatedVisibility(
            visible = !isTopLevel && !manuallyRevealed,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Surface(
                onClick = { manuallyRevealed = true },
                modifier = Modifier.padding(bottom = Space.sm),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                tonalElevation = 3.dp,
            ) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Afficher la navigation",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
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
    onNavigateToSettings: () -> Unit,
    onNavigateToSettingsCategory: (String) -> Unit,
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
        BottomNavDestination.PARAMETRES.route -> {
            // L'onglet portait Compte jusqu'ici — un simple bouton engrenage
            // minuscule dans sa barre du haut était le seul accès aux
            // réglages. L'onglet héberge maintenant directement l'accueil
            // Réglages (recherche + catégories) ; Compte reste à une carte
            // de distance via la catégorie « Compte ».
            SettingsHomeScreen(
                onNavigateToSection = onNavigateToSettingsCategory,
                modifier = modifier,
            )
        }
    }
}
