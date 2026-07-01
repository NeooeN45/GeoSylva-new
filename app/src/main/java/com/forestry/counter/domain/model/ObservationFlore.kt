package com.forestry.counter.domain.model

/**
 * Domain model pour une observation flore.
 * Mappé depuis/vers ObservationFloreEntity dans data/mapper.
 */
data class ObservationFlore(
    val observationId: String,
    val parcelleId: String,
    val placetteId: String?,
    val sessionId: String?,
    val codeEspece: String,
    val nomScientifique: String,
    val nomCommun: String?,
    val abundanceDominance: String,
    val strate: String,
    val sociabilite: Int?,
    val indicateurEllenbergL: Int?,
    val indicateurEllenbergT: Int?,
    val indicateurEllenbergR: Int?,
    val indicateurEllenbergF: Int?,
    val indicateurEllenbergN: Int?,
    val isEspeceProtegee: Boolean,
    val isEspeceIndicatrice: Boolean,
    val dateSaisie: Long,
    val createdAt: Long = System.currentTimeMillis()
)
