package com.forestry.counter.domain.model

/**
 * Domain model pour une valeur foncière.
 * Mappé depuis/vers ValeurFonciereEntity dans data/mapper.
 */
data class ValeurFonciere(
    val valeurId: String,
    val parcelleId: String,
    val dateEstimation: Long,

    // Valeur foncière nue (DVF Cerema)
    val valeurFonciereNuEurHa: Double?,
    val sourceValeurFonciere: String?,
    val prixMarcheRegionalEurHa: Double?,

    // Valeur du bois sur pied
    val volumeCommercialisableM3: Double?,
    val valeurBoisSurPiedEur: Double?,

    // Carbone — Label Bas Carbone
    val carboneTotalTonnes: Double?,
    val valeurCarboneLabelBcEur: Double?,

    // Valeur patrimoniale totale
    val valeurTotalePatrimoineEur: Double?,

    // Coûts prévisionnels
    val coutEclaircieEstimeEur: Double?,
    val coutRenouvellementEstimeEur: Double?,
    val revenuBrutAnnuelMoyenEur: Double?,

    // Régime fiscal
    val eligiblePsg: Boolean,
    val eligibleDefiForet: Boolean,
    val eligibleIfiExoneration: Boolean,
    val eligibleDpa: Boolean,
    val alertesFiscalesJson: String?,

    val remarques: String?,
    val updatedAt: Long = System.currentTimeMillis()
)
