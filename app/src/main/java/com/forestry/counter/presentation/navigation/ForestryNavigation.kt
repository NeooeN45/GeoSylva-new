package com.forestry.counter.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.forestry.counter.ForestryCounterApplication
import com.forestry.counter.presentation.screens.group.GroupScreen
import com.forestry.counter.presentation.screens.groups.GroupsScreen
import com.forestry.counter.presentation.screens.settings.SettingsScreen
import com.forestry.counter.presentation.screens.formulas.FormulasScreen
import com.forestry.counter.presentation.screens.calculator.CalculatorScreen
import com.forestry.counter.presentation.screens.forestry.ParcellesScreen
import com.forestry.counter.presentation.screens.forestry.PlacettesScreen
import com.forestry.counter.presentation.screens.forestry.MartelageScreen
import com.forestry.counter.presentation.screens.forestry.MapScreen
import com.forestry.counter.presentation.screens.forestry.PlacetteDetailScreen
import com.forestry.counter.presentation.screens.forestry.EssenceDiamScreen
import com.forestry.counter.presentation.screens.forestry.DashboardScreen
import com.forestry.counter.presentation.screens.settings.PriceTablesEditorScreen
import com.forestry.counter.presentation.screens.onboarding.OnboardingScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Forets : Screen("forets")
    object Groups : Screen("groups")
    object GroupDetail : Screen("group/{groupId}") {
        fun createRoute(groupId: String) = "group/$groupId"
    }
    object Formulas : Screen("group/{groupId}/formulas") {
        fun createRoute(groupId: String) = "group/$groupId/formulas"
    }
    object Calculator : Screen("group/{groupId}/calculator") {
        fun createRoute(groupId: String) = "group/$groupId/calculator"
    }
    object Settings : Screen("settings")
    object PriceTablesEditor : Screen("settings/price_tables")
    object Parcelles : Screen("parcelles/{forestId}") {
        fun createRoute(forestId: String?) = "parcelles/${forestId ?: "none"}"
    }
    object Placettes : Screen("placettes/{parcelleId}") {
        fun createRoute(parcelleId: String) = "placettes/$parcelleId"
    }
    object PlacetteDetail : Screen("placette/{parcelleId}/{placetteId}") {
        fun createRoute(parcelleId: String, placetteId: String) = "placette/$parcelleId/$placetteId"
    }
    object Martelage : Screen("martelage/{scope}/{forestId}/{parcelleId}/{placetteId}") {
        fun forGlobal(): String = "martelage/GLOBAL/none/none/none"
        fun forForest(forestId: String): String = "martelage/FOREST/$forestId/none/none"
        fun forParcelle(parcelleId: String): String = "martelage/PARCELLE/none/$parcelleId/none"
        fun forPlacette(parcelleId: String, placetteId: String): String = "martelage/PLACETTE/none/$parcelleId/$placetteId"
    }
    object Map : Screen("map/{parcelleId}?navLat={navLat}&navLon={navLon}&navEssence={navEssence}&navDiam={navDiam}") {
        fun createRoute(parcelleId: String) = "map/$parcelleId"
        fun createRouteWithNav(
            parcelleId: String,
            lat: Double,
            lon: Double,
            essenceName: String,
            diamCm: Double
        ) = "map/$parcelleId?navLat=$lat&navLon=$lon&navEssence=$essenceName&navDiam=$diamCm"
    }
    object EssenceDiam : Screen("placette/{parcelleId}/{placetteId}/essence/{essenceCode}") {
        fun createRoute(parcelleId: String, placetteId: String, essenceCode: String) = "placette/$parcelleId/$placetteId/essence/$essenceCode"
    }
    object Dashboard : Screen("dashboard/{parcelleId}") {
        fun createRoute(parcelleId: String) = "dashboard/$parcelleId"
    }
    object Onboarding : Screen("onboarding")
}

@Composable
fun ForestryNavigation(app: ForestryCounterApplication) {
    val navController = rememberNavController()

    val animationsEnabled by app.userPreferences.animationsEnabled.collectAsState(initial = true)
    val onboardingCompleted by app.userPreferences.onboardingCompleted.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    // Courbes Material 3 Emphasized (M3 spec)
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    // Durées M3 : entrée 400ms, sortie 200ms pour un enchaînement fluide
    val enterMs = 400
    val exitMs = 250
    val navScale = 0.94f

    val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (!animationsEnabled) {
            EnterTransition.None
        } else {
            fadeIn(animationSpec = tween(durationMillis = enterMs, delayMillis = 60, easing = emphasizedDecelerate)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = enterMs, easing = emphasizedDecelerate),
                    initialOffsetX = { it / 4 }
                ) +
                scaleIn(
                    animationSpec = tween(durationMillis = enterMs, easing = emphasizedDecelerate),
                    initialScale = navScale
                )
        }
    }
    val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (!animationsEnabled) {
            ExitTransition.None
        } else {
            fadeOut(animationSpec = tween(durationMillis = exitMs, easing = emphasizedAccelerate)) +
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = exitMs, easing = emphasizedAccelerate),
                    targetOffsetX = { -it / 5 }
                ) +
                scaleOut(
                    animationSpec = tween(durationMillis = exitMs, easing = emphasizedAccelerate),
                    targetScale = navScale
                )
        }
    }
    val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (!animationsEnabled) {
            EnterTransition.None
        } else {
            fadeIn(animationSpec = tween(durationMillis = enterMs, delayMillis = 60, easing = emphasizedDecelerate)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = enterMs, easing = emphasizedDecelerate),
                    initialOffsetX = { -it / 4 }
                ) +
                scaleIn(
                    animationSpec = tween(durationMillis = enterMs, easing = emphasizedDecelerate),
                    initialScale = navScale
                )
        }
    }
    val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (!animationsEnabled) {
            ExitTransition.None
        } else {
            fadeOut(animationSpec = tween(durationMillis = exitMs, easing = emphasizedAccelerate)) +
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = exitMs, easing = emphasizedAccelerate),
                    targetOffsetX = { it / 5 }
                ) +
                scaleOut(
                    animationSpec = tween(durationMillis = exitMs, easing = emphasizedAccelerate),
                    targetScale = navScale
                )
        }
    }

    val startDest = if (onboardingCompleted) Screen.Forets.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(
            route = Screen.Onboarding.route,
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) {
            OnboardingScreen(
                onComplete = {
                    coroutineScope.launch {
                        app.userPreferences.setOnboardingCompleted(true)
                    }
                    navController.navigate(Screen.Forets.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Forests selection using existing Groups screen
        composable(
            route = Screen.Forets.route,
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) {
            GroupsScreen(
                groupRepository = app.groupRepository,
                onNavigateToGroup = { groupId ->
                    navController.navigate(Screen.Parcelles.createRoute(groupId))
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                preferencesManager = app.userPreferences,
                onNavigateToMartelage = { groupIdOrNull ->
                    if (groupIdOrNull == null) {
                        navController.navigate(Screen.Martelage.forGlobal())
                    } else {
                        navController.navigate(Screen.Martelage.forForest(groupIdOrNull))
                    }
                },
                onNavigateToMap = { scope ->
                    navController.navigate(Screen.Map.createRoute(scope))
                }
            )
        }

        composable(
            route = Screen.EssenceDiam.route,
            arguments = listOf(
                navArgument("parcelleId") { type = NavType.StringType },
                navArgument("placetteId") { type = NavType.StringType },
                navArgument("essenceCode") { type = NavType.StringType }
            ),
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) { backStackEntry ->
            val parcelleId = backStackEntry.arguments?.getString("parcelleId") ?: return@composable
            val placetteId = backStackEntry.arguments?.getString("placetteId") ?: return@composable
            val essenceCode = backStackEntry.arguments?.getString("essenceCode") ?: return@composable
            EssenceDiamScreen(
                parcelleId = parcelleId,
                placetteId = placetteId,
                essenceCode = essenceCode,
                tigeRepository = app.tigeRepository,
                calculator = app.forestryCalculator,
                userPreferences = app.userPreferences,
                essenceRepository = app.essenceRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // Forestry flow
        composable(
            route = Screen.Parcelles.route,
            arguments = listOf(navArgument("forestId") { type = NavType.StringType }),
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) { backStackEntry ->
            val forestArg = backStackEntry.arguments?.getString("forestId")
            val forestId = forestArg?.takeUnless { it == "none" }
            ParcellesScreen(
                forestId = forestId,
                groupRepository = app.groupRepository,
                parcelleRepository = app.parcelleRepository,
                placetteRepository = app.placetteRepository,
                tigeRepository = app.tigeRepository,
                userPreferences = app.userPreferences,
                onNavigateToPlacettes = { parcelleId ->
                    navController.navigate(Screen.Placettes.createRoute(parcelleId))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMartelage = { fid ->
                    navController.navigate(Screen.Martelage.forForest(fid))
                },
                onNavigateToMap = {
                    val scope = if (forestId != null) "forest_$forestId" else "all"
                    navController.navigate(Screen.Map.createRoute(scope))
                }
            )
        }

        composable(
            route = Screen.Placettes.route,
            arguments = listOf(navArgument("parcelleId") { type = NavType.StringType }),
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) { backStackEntry ->
            val parcelleId = backStackEntry.arguments?.getString("parcelleId") ?: return@composable
            PlacettesScreen(
                parcelleId = parcelleId,
                placetteRepository = app.placetteRepository,
                parcelleRepository = app.parcelleRepository,
                tigeRepository = app.tigeRepository,
                userPreferences = app.userPreferences,
                onNavigateToMartelage = { pid, plid ->
                    navController.navigate(Screen.PlacetteDetail.createRoute(pid, plid))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMartelageForParcelle = { pid ->
                    navController.navigate(Screen.Martelage.forParcelle(pid))
                },
                onNavigateToMap = { pid ->
                    navController.navigate(Screen.Map.createRoute(pid))
                },
                onNavigateToDashboard = { pid ->
                    navController.navigate(Screen.Dashboard.createRoute(pid))
                }
            )
        }

        composable(
            route = Screen.PlacetteDetail.route,
            arguments = listOf(
                navArgument("parcelleId") { type = NavType.StringType },
                navArgument("placetteId") { type = NavType.StringType }
            ),
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) { backStackEntry ->
            val parcelleId = backStackEntry.arguments?.getString("parcelleId") ?: return@composable
            val placetteId = backStackEntry.arguments?.getString("placetteId") ?: return@composable
            PlacetteDetailScreen(
                parcelleId = parcelleId,
                placetteId = placetteId,
                essenceRepository = app.essenceRepository,
                tigeRepository = app.tigeRepository,
                placetteRepository = app.placetteRepository,
                userPreferences = app.userPreferences,
                onNavigateToDiametres = { pid, plid, essence ->
                    navController.navigate(Screen.EssenceDiam.createRoute(pid, plid, essence))
                },
                onNavigateToMartelage = { pid, plid ->
                    navController.navigate(Screen.Martelage.forPlacette(pid, plid))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Martelage.route,
            arguments = listOf(
                navArgument("scope") { type = NavType.StringType },
                navArgument("forestId") { type = NavType.StringType },
                navArgument("parcelleId") { type = NavType.StringType },
                navArgument("placetteId") { type = NavType.StringType }
            ),
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) { backStackEntry ->
            val scope = backStackEntry.arguments?.getString("scope") ?: "GLOBAL"
            val forestId = backStackEntry.arguments?.getString("forestId")?.takeUnless { it == "none" }
            val parcelleId = backStackEntry.arguments?.getString("parcelleId")?.takeUnless { it == "none" }
            val placetteId = backStackEntry.arguments?.getString("placetteId")?.takeUnless { it == "none" }
            MartelageScreen(
                scope = scope,
                forestId = forestId,
                parcelleId = parcelleId,
                placetteId = placetteId,
                essenceRepository = app.essenceRepository,
                tigeRepository = app.tigeRepository,
                parcelleRepository = app.parcelleRepository,
                forestryCalculator = app.forestryCalculator,
                userPreferences = app.userPreferences,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPriceTablesEditor = { navController.navigate(Screen.PriceTablesEditor.route) },
                onNavigateToMap = { pid -> navController.navigate(Screen.Map.createRoute(pid)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Map.route,
            arguments = listOf(
                navArgument("parcelleId") { type = NavType.StringType },
                navArgument("navLat") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("navLon") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("navEssence") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("navDiam") { type = NavType.StringType; nullable = true; defaultValue = null }
            ),
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) { backStackEntry ->
            val parcelleId = backStackEntry.arguments?.getString("parcelleId") ?: return@composable
            val navLat = backStackEntry.arguments?.getString("navLat")?.toDoubleOrNull()
            val navLon = backStackEntry.arguments?.getString("navLon")?.toDoubleOrNull()
            val navEssence = backStackEntry.arguments?.getString("navEssence")
            val navDiam = backStackEntry.arguments?.getString("navDiam")?.toDoubleOrNull()
            MapScreen(
                parcelleId = parcelleId,
                tigeRepository = app.tigeRepository,
                essenceRepository = app.essenceRepository,
                parcelleRepository = app.parcelleRepository,
                preferencesManager = app.userPreferences,
                offlineTileManager = app.offlineTileManager,
                onNavigateBack = { navController.popBackStack() },
                initialNavLat = navLat,
                initialNavLon = navLon,
                initialNavEssence = navEssence,
                initialNavDiam = navDiam
            )
        }

        composable(
            route = Screen.Dashboard.route,
            arguments = listOf(navArgument("parcelleId") { type = NavType.StringType }),
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) { backStackEntry ->
            val parcelleId = backStackEntry.arguments?.getString("parcelleId") ?: return@composable
            DashboardScreen(
                parcelleId = parcelleId,
                tigeRepository = app.tigeRepository,
                essenceRepository = app.essenceRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Groups.route,
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) {
            GroupsScreen(
                groupRepository = app.groupRepository,
                onNavigateToGroup = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                preferencesManager = app.userPreferences
            )
        }

        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
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
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
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
            route = Screen.Settings.route,
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
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
                onNavigateToPriceTablesEditor = { navController.navigate(Screen.PriceTablesEditor.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PriceTablesEditor.route,
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
        ) {
            PriceTablesEditorScreen(
                parameterRepository = app.parameterRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Calculator.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
            enterTransition = navEnterTransition,
            exitTransition = navExitTransition,
            popEnterTransition = navPopEnterTransition,
            popExitTransition = navPopExitTransition
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
    }
}
