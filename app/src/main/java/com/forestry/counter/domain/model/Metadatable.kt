package com.forestry.counter.domain.model

/**
 * Modèle de domaine traçable selon le contrat GeoSylva 3.0 §3.1.
 *
 * Les implémentations exposent les quatre champs metadata (deletedAt, auteur,
 * source, version) et propagent les mutations de traçabilité via [withMetadata].
 *
 * @param T type concret auto-référencé (F-bounded polymorphism) : garantit que
 *          [withMetadata] retourne le type exact sans cast ni réflexion.
 */
interface Metadatable<T : Metadatable<T>> {
    /** Horodatage de suppression logique (null si actif). Géré par le soft-delete. */
    val deletedAt: Long?

    /** Identifiant du compte Quintessences auteur de la dernière écriture. */
    val auteur: String?

    /** Point d'entrée à l'origine de la donnée (manual, import, sync, gps). */
    val source: String?

    /** Numéro de version incrémenté à chaque modification. */
    val version: Int

    /**
     * Retourne une copie avec les metadata de traçabilité renseignés.
     * [deletedAt] est préservé (géré séparément par le soft-delete).
     */
    fun withMetadata(auteur: String, source: String, version: Int): T
}
