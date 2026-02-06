package com.forestry.counter.presentation.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.pow

object ColorUtils {

    /**
     * Calculate contrast ratio between two colors
     * https://www.w3.org/TR/WCAG21/#contrast-minimum
     */
    fun contrastRatio(foreground: Color, background: Color): Float {
        val l1 = foreground.luminance() + 0.05f
        val l2 = background.luminance() + 0.05f
        return if (l1 > l2) l1 / l2 else l2 / l1
    }

    /**
     * Check if text color has sufficient contrast (WCAG AA standard)
     */
    fun hasSufficientContrast(
        foreground: Color,
        background: Color,
        level: ContrastLevel = ContrastLevel.AA
    ): Boolean {
        val ratio = contrastRatio(foreground, background)
        return when (level) {
            ContrastLevel.AA -> ratio >= 4.5f
            ContrastLevel.AAA -> ratio >= 7.0f
        }
    }

    /**
     * Get appropriate text color (black or white) based on background
     */
    fun getContrastingTextColor(backgroundColor: Color): Color {
        val whiteContrast = contrastRatio(Color.White, backgroundColor)
        val blackContrast = contrastRatio(Color.Black, backgroundColor)
        return if (whiteContrast > blackContrast) Color.White else Color.Black
    }

    /**
     * Lighten a color by a given factor (0.0 - 1.0)
     */
    fun lighten(color: Color, factor: Float): Color {
        val f = factor.coerceIn(0f, 1f)
        return Color(
            red = color.red + (1f - color.red) * f,
            green = color.green + (1f - color.green) * f,
            blue = color.blue + (1f - color.blue) * f,
            alpha = color.alpha
        )
    }

    /**
     * Darken a color by a given factor (0.0 - 1.0)
     */
    fun darken(color: Color, factor: Float): Color {
        val f = 1f - factor.coerceIn(0f, 1f)
        return Color(
            red = color.red * f,
            green = color.green * f,
            blue = color.blue * f,
            alpha = color.alpha
        )
    }

    /**
     * Convert hex string to Color
     */
    fun parseColor(hex: String): Color? {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert Color to hex string
     */
    fun toHex(color: Color): String {
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", red, green, blue)
    }
}

enum class ContrastLevel {
    AA,   // 4.5:1 for normal text
    AAA   // 7:1 for normal text
}
