package com.forestry.counter.domain.model

/**
 * Domain model pour une forêt.
 * Mappé depuis/vers ForetEntity dans data/mapper.
 */
data class Foret(
    val foretId: String,
    val nom: String,
    val proprietaireNom: String,
    /**
     * Email du propriétaire forestier.
     *
     * Finalité RGPD (Art. 5§1.b) : utilisé uniquement pour contacter le
     * propriétaire pour les rapports de gestion. Optionnel — non transmis
     * à aucun tiers. Voir PRIVACY_POLICY.md §1.1.
     */
    val proprietaireEmail: String?,
    val gestionnaireNom: String?,
    val typeForet: String?,
    val objectifGestion: String?,
    val psgNumero: String?,
    val psgDateExpiration: Long?,
    val departement: String?,
    val remarques: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Metadata spec GeoSylva 3.0 §3.1 — provenance et traçabilité
    override val deletedAt: Long? = null,
    override val auteur: String? = null,
    override val source: String? = null,
    override val version: Int = 1
) : Metadatable<Foret> {
    override fun withMetadata(auteur: String, source: String, version: Int): Foret =
        copy(auteur = auteur, source = source, version = version)
}
