package com.forestry.counter.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.forestry.counter.ForestryCounterApplication
import com.forestry.counter.presentation.screens.calculator.CalculatorScreen
import com.forestry.counter.presentation.screens.forestry.TarifDocumentationScreen
import com.forestry.counter.presentation.screens.formulas.FormulasScreen
import com.forestry.counter.presentation.screens.group.GroupScreen
import com.forestry.counter.presentation.screens.groups.GroupsScreen
import com.forestry.counter.presentation.screens.packs.PackManagerScreen
import com.forestry.counter.presentation.screens.settings.PriceTablesEditorScreen
import com.forestry.counter.presentation.screens.settings.SettingsScreen
import com.forestry.counter.presentation.screens.settings.PrivacyPolicyScreen
import com.forestry.counter.presentation.screens.account.AccountScreen
import com.forestry.counter.presentation.screens.account.DeveloperOptionsScreen
import com.forestry.counter.presentation.screens.account.LoginScreen
import com.forestry.counter.presentation.screens.account.PasswordRecoveryScreen

/**
 * Sous-graphe Settings, outils et groupes (comptage).
 *
 * Routes : [Screen.Groups], [Screen.GroupDetail], [Screen.Formulas],
 * [Screen.Calculator], [Screen.Settings], [Screen.PriceTablesEditor],
 * [Screen.TarifDocs], [Screen.PackManager]
 */
fun NavGraphBuilder.settingsNavGraph(
    navController: NavController,
    app: ForestryCounterApplication,
    transitions: NavTransitions,
) {
    composable(
        route = Screen.Groups.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        GroupsScreen(
            groupRepository = app.groupRepository,
            onNavigateToGroup = { groupId ->
                navController.navigate(Screen.GroupDetail.createRoute(groupId))
            },
            onNavigateToSettings = {
                navController.navigate(Screen.Settings.route)
            },
            preferencesManager = app.userPreferences,
            onNavigateToIbp = { navController.navigate(Screen.IbpProjects.route) }
        )
    }

    composable(
        route = Screen.GroupDetail.route,
        arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) { backStackEntry ->
        val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
        GroupScreen(
            groupId = groupId,
            groupRepository = app.groupRepository,
            counterRepository = app.counterRepository,
            formulaRepository = app.formulaRepository,
            exportDataUseCase = app.exportDataUseCase,
            importDataUseCase = app.importDataUseCase,
            preferencesManager = app.userPreferences,
            onNavigateToFormulas = { navController.navigate(Screen.Formulas.createRoute(groupId)) },
            onNavigateToCalculator = { navController.navigate(Screen.Calculator.createRoute(groupId)) },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.Formulas.route,
        arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) { backStackEntry ->
        val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
        FormulasScreen(
            groupId = groupId,
            formulaRepository = app.formulaRepository,
            preferencesManager = app.userPreferences,
            onNavigateToCalculator = { navController.navigate(Screen.Calculator.createRoute(groupId)) },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.Calculator.route,
        arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) { backStackEntry ->
        val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
        CalculatorScreen(
            groupId = groupId,
            counterRepository = app.counterRepository,
            formulaRepository = app.formulaRepository,
            formulaParser = app.formulaParser,
            preferencesManager = app.userPreferences,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.Settings.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        SettingsScreen(
            preferencesManager = app.userPreferences,
            exportDataUseCase = app.exportDataUseCase,
            parameterRepository = app.parameterRepository,
            tigeRepository = app.tigeRepository,
            essenceRepository = app.essenceRepository,
            forestryCalculator = app.forestryCalculator,
            parcelleRepository = app.parcelleRepository,
            placetteRepository = app.placetteRepository,
            offlineTileManager = app.offlineTileManager,
            identityRepository = app.identityRepository,
            deleteAllUserDataUseCase = app.deleteAllUserDataUseCase,
            onNavigateToPriceTablesEditor = { navController.navigate(Screen.PriceTablesEditor.route) },
            onNavigateToAccount = { navController.navigate(Screen.Account.route) },
            onNavigateToDeveloperOptions = {
                navController.navigate(Screen.DeveloperOptions.route)
            },
            onNavigateToPrivacyPolicy = {
                navController.navigate(Screen.PrivacyPolicy.route)
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.Account.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        AccountScreen(
            repository = app.identityRepository,
            onNavigateToLogin = { navController.navigate(Screen.Login.route) },
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            onNavigateBack = { navController.popBackStack() },
        )
    }

    composable(
        route = Screen.Login.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        LoginScreen(
            repository = app.identityRepository,
            onAuthenticated = { navController.popBackStack() },
            onContinueOffline = { navController.popBackStack() },
            onForgotPassword = { navController.navigate(Screen.PasswordRecovery.route) },
        )
    }

    composable(
        route = Screen.PasswordRecovery.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        PasswordRecoveryScreen(
            repository = app.identityRepository,
            onCompleted = { navController.popBackStack() },
            onNavigateBack = { navController.popBackStack() },
        )
    }

    composable(
        route = Screen.DeveloperOptions.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        DeveloperOptionsScreen(
            repository = app.identityRepository,
            parcelSyncRepository = app.parcelleSyncRepository,
            preferences = app.userPreferences,
            onNavigateBack = { navController.popBackStack() },
        )
    }

    composable(
        route = Screen.PriceTablesEditor.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        PriceTablesEditorScreen(
            parameterRepository = app.parameterRepository,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.TarifDocs.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        TarifDocumentationScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.PrivacyPolicy.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        PrivacyPolicyScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.PackManager.route,
        enterTransition = transitions.enter,
        exitTransition = transitions.exit,
        popEnterTransition = transitions.popEnter,
        popExitTransition = transitions.popExit,
    ) {
        PackManagerScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
