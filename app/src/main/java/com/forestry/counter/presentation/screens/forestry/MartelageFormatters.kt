package com.forestry.counter.presentation.screens.forestry

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import java.util.Locale
import kotlin.math.abs

// Helpers de formatage simples, uniquement pour l'affichage
internal fun formatVolume(v: Double): String {
    val a = abs(v)
    val decimals = when {
        a < 10.0 -> 3
        a < 100.0 -> 2
        else -> 1
    }
    return String.format(Locale.getDefault(), "%.${decimals}f", v)
}

internal fun formatG(g: Double): String = String.format(Locale.getDefault(), "%.2f", g)

internal fun formatMoney(v: Double?, placeholder: String, euroSymbol: String): String =
    v?.let { String.format(Locale.getDefault(), "%.0f %s", it, euroSymbol) } ?: placeholder

internal fun formatPrice(p: Double?, placeholder: String): String =
    p?.let { String.format(Locale.getDefault(), "%.0f", it) } ?: placeholder

internal fun formatIntPerHa(nPerHa: Double): String = String.format(Locale.getDefault(), "%.0f", nPerHa)

internal fun formatDiameter(dm: Double?, placeholder: String): String =
    dm?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: placeholder

internal fun formatHeight(h: Double?, placeholder: String): String =
    h?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: placeholder

internal fun essenceTintColor(
    essenceCode: String,
    essences: List<com.forestry.counter.domain.model.Essence>
): Color? {
    val hex = essences.firstOrNull { it.code == essenceCode }?.colorHex
    if (hex.isNullOrBlank()) return null
    return try {
        Color(AndroidColor.parseColor(hex))
    } catch (_: Throwable) {
        null
    }
}
