package com.forestry.counter.data.local.entity

import androidx.room.Embedded

/**
 * Provenance normalisée — embedded dans les entités qui portent une donnée
 * sourcée (forêt, parcelle, placette, observation, etc.).
 *
 * Spec GEOSYLVA-003 §29.13 (création forêt guidée) + §7.6 : organisme source,
 * date, licence, précision, statut.
 *
 * Embedded (pas de table séparée) — les colonnes sont préfixées `provenance_`
 * dans la table hôte.
 */
data class ProvenanceEmbed(
    /** Organisme source : IGN, ONF, CRPF, saisie manuelle, import, etc. */
    @androidx.room.ColumnInfo(name = "provenance_organism")
    val organism: String?,
    /** Date d'acquisition de la source (epoch millis). */
    @androidx.room.ColumnInfo(name = "provenance_acquiredAt")
    val acquiredAt: Long?,
    /** Licence de la donnée : ODbL, CC-BY, proprietary, etc. */
    @androidx.room.ColumnInfo(name = "provenance_license")
    val license: String?,
    /** Précision déclarée de la source (en mètres pour données géo). */
    @androidx.room.ColumnInfo(name = "provenance_precisionM")
    val precisionM: Double?,
    /** Statut : official, provisional, draft, imported, manual. */
    @androidx.room.ColumnInfo(name = "provenance_status")
    val status: String?
)
