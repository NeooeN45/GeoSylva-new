package com.forestry.counter.presentation.utils

/**
 * Parse une saisie de hauteur flexible :
 * - Valeur unique : "18.5" ou "18,5"
 * - Plusieurs virgules (>=2) : "25,20,27" → moyenne
 * - Séparateur point-virgule : "25;20;27" → moyenne
 *
 * @return Pair(moyenne, nombre de valeurs) ou (null, 0) si invalide
 */
fun parseHeightInputMean(raw: String): Pair<Double?, Int> {
    val s = raw.trim()
    if (s.isBlank()) return null to 0
    val compact = s.replace(" ", "")
    val commaCount = compact.count { it == ',' }
    if (commaCount >= 2) {
        val values = compact.split(',')
            .mapNotNull { it.toDoubleOrNull() }
            .filter { it > 0.0 }
        if (values.isEmpty()) return null to 0
        return values.average() to values.size
    }
    if (compact.contains(';')) {
        val values = compact.split(';')
            .mapNotNull { it.replace(',', '.').toDoubleOrNull() }
            .filter { it > 0.0 }
        if (values.isEmpty()) return null to 0
        return values.average() to values.size
    }
    val v = compact.replace(',', '.').toDoubleOrNull()
    return if (v != null && v > 0.0) v to 1 else null to 0
}
