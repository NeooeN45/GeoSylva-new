package com.forestry.counter.domain.model

/**
 * Domain model pour une forêt.
 * Mappé depuis/vers ForetEntity dans data/mapper.
 */
data class Foret(
    val foretId: String,
    val nom: String,
    val proprietaireNom: String,
    val proprietaireEmail: String?,
    val gestionnaireNom: String?,
    val typeForet: String?,
    val objectifGestion: String?,
    val psgNumero: String?,
    val psgDateExpiration: Long?,
    val departement: String?,
    val remarques: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
