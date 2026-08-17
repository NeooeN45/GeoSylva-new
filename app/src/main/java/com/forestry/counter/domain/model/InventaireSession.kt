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
    val createdAt: Long = System.currentTimeMillis(),

    // Metadata spec GeoSylva 3.0 §3.1 — provenance et traçabilité
    override val deletedAt: Long? = null,
    override val auteur: String? = null,
    override val source: String? = null,
    override val version: Int = 1
) : Metadatable<InventaireSession> {
    override fun withMetadata(auteur: String, source: String, version: Int): InventaireSession =
        copy(auteur = auteur, source = source, version = version)
}
