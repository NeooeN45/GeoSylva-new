package com.forestry.counter.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.forestry.counter.ForestryCounterApplication
import com.forestry.counter.presentation.screens.projects.ProjectDetailScreen
import com.forestry.counter.presentation.screens.projects.ProjectsScreen
import com.forestry.counter.presentation.screens.projects.ProjectsViewModel

/**
 * Sous-graphe Projets — spec GEOSYLVA-003 §29.11.
 *
 * Routes : [Screen.Projects], [Screen.ProjectDetail]
 */
fun NavGraphBuilder.projectsNavGraph(
    navController: NavController,
    app: ForestryCounterApplication,
    transitions: NavTransitions,
) {
    composable(
        route = Screen.Projects.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        val viewModel = ProjectsViewModel(app.projectRepository)
        ProjectsScreen(
            viewModel = viewModel,
            onNavigateToProjectDetail = { projectId ->
                navController.navigate(Screen.ProjectDetail.createRoute(projectId))
            },
            onNavigateBack = { navController.popBackStack() },
        )
    }

    composable(
        route = Screen.ProjectDetail.route,
        arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) { backStackEntry ->
        val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
        // Le nom du projet est récupéré via le ViewModel ; pour simplifier Lot 1,
        // on passe un nom générique et le détail l'affiche.
        ProjectDetailScreen(
            projectId = projectId,
            projectName = "Projet",
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
