package com.forestry.counter.presentation.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.forestry.counter.ForestryCounterApplication
import com.forestry.counter.presentation.screens.onboarding.OnboardingScreen
import kotlinx.coroutines.launch

/**
 * Sous-graphe Onboarding.
 *
 * Routes : [Screen.Onboarding]
 */
fun NavGraphBuilder.onboardingNavGraph(
    navController: NavController,
    app: ForestryCounterApplication,
    transitions: NavTransitions,
) {
    composable(
        route = Screen.Onboarding.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val permissionsLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { _ ->
            // Le callback n'est invoqué qu'après la réponse à toutes les
            // autorisations. L'écriture atomique précède immédiatement la
            // navigation dans la même coroutine : l'écran principal ne peut
            // donc pas observer un onboarding terminé sans visite planifiée.
            coroutineScope.launch {
                app.userPreferences.completeOnboardingAndScheduleCoachMarkTour()
                navController.navigate(Screen.Forets.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
        }
        OnboardingScreen(
            onComplete = {
                val permissions = buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.CAMERA)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
                permissionsLauncher.launch(permissions.toTypedArray())
            }
        )
    }
}
