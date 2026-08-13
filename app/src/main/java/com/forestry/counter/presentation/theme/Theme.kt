package com.forestry.counter.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.forestry.counter.data.preferences.ThemeMode
import com.forestry.counter.data.preferences.FontSize
import androidx.compose.ui.text.TextStyle

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = OnError,
    outline = Outline,
    outlineVariant = OutlineVariant,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceTint = Primary,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryVariantDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryVariantDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceTint = PrimaryDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark
)

@Composable
@Suppress("DEPRECATION")
fun ForestryCounterTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: Color = Primary,
    // Couleur des blocs mis en avant (tuiles de statistiques, etc.) —
    // demandée séparément de `accentColor` : elle ne pilotait jusqu'ici que
    // les boutons/icônes, jamais ces blocs, restés au vert de marque fixe
    // quel que soit l'accent choisi. `null` = comportement historique.
    containerAccentColor: Color? = null,
    // Défaut à `false` : sur Android 12+, la palette Material You dérivée du
    // fond d'écran de l'utilisateur écrase entièrement l'identité GeoSylva.
    // C'est acceptable pour une application système, pas pour un outil métier.
    // Reste proposé comme option explicite dans les réglages.
    dynamicColor: Boolean = false,
    fontSize: FontSize = FontSize.MEDIUM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // L'accent personnalisé n'écrase la couleur de marque que si
        // l'utilisateur en a explicitement choisi un autre. Sinon une couleur
        // pensée pour le thème clair se retrouverait appliquée au thème sombre.
        darkTheme -> {
            var scheme = if (accentColor == Primary) DarkColorScheme else DarkColorScheme.copy(primary = accentColor)
            if (containerAccentColor != null) {
                scheme = scheme.copy(
                    primaryContainer = containerAccentColor,
                    onPrimaryContainer = com.forestry.counter.presentation.utils.ColorUtils.getContrastingTextColor(containerAccentColor),
                )
            }
            scheme
        }
        else -> {
            var scheme = if (accentColor == Primary) LightColorScheme else LightColorScheme.copy(primary = accentColor)
            if (containerAccentColor != null) {
                scheme = scheme.copy(
                    primaryContainer = containerAccentColor,
                    onPrimaryContainer = com.forestry.counter.presentation.utils.ColorUtils.getContrastingTextColor(containerAccentColor),
                )
            }
            scheme
        }
    }

    val scale = fontSize.scale
    val typography = remember(scale) { Typography.scaled(scale) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}

fun parseAccentColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Primary
    }
}

private fun TextStyle.scaled(scale: Float): TextStyle {
    if (scale == 1f) return this
    return this.copy(
        fontSize = this.fontSize * scale,
        lineHeight = this.lineHeight * scale
    )
}

private fun Typography.scaled(scale: Float): Typography {
    if (scale == 1f) return this
    return Typography(
        displayLarge = displayLarge.scaled(scale),
        displayMedium = displayMedium.scaled(scale),
        displaySmall = displaySmall.scaled(scale),
        headlineLarge = headlineLarge.scaled(scale),
        headlineMedium = headlineMedium.scaled(scale),
        headlineSmall = headlineSmall.scaled(scale),
        titleLarge = titleLarge.scaled(scale),
        titleMedium = titleMedium.scaled(scale),
        titleSmall = titleSmall.scaled(scale),
        bodyLarge = bodyLarge.scaled(scale),
        bodyMedium = bodyMedium.scaled(scale),
        bodySmall = bodySmall.scaled(scale),
        labelLarge = labelLarge.scaled(scale),
        labelMedium = labelMedium.scaled(scale),
        labelSmall = labelSmall.scaled(scale)
    )
}
