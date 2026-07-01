package com.forestry.counter.domain.model

/**
 * Domain model pour une session d'inventaire.
 * Mappé depuis/vers InventaireSessionEntity dans data/mapper.
 */
data class InventaireSession(
    val sessionId: String,
    val parcelleId: String,
    val typeSession: String,
    val dateDebut: Long,
    val dateFin: Long?,
    val operateurNom: String?,
    val methode: String?,
    val intensiteEchantillonnagePct: Double?,
    val objectifSession: String?,
    val remarques: String?,
    val createdAt: Long = System.currentTimeMillis()
)
