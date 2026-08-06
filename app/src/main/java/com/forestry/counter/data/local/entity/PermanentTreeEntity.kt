package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Arbre permanent — identité stable d'un arbre suivi sur plusieurs campagnes.
 *
 * Spec GEOSYLVA-003 §7.6 : un arbre permanent est identifié une fois (UUID),
 * chaque campagne produit une nouvelle [TreeObservationEntity], chaque
 * observation contient des [MeasurementEntity], chaque mesure peut déclencher
 * des [CalculationRunEntity]. Aucun niveau n'écrase un autre.
 *
 * Distinction fondamentale :
 *   Arbre permanent ≠ Observation ≠ Mesure ≠ Résultat calculé
 */
@Entity(
    tableName = "permanent_trees",
    foreignKeys = [
        ForeignKey(
            entity = ParcelleEntity::class,
            parentColumns = ["parcelleId"],
            childColumns = ["parcelleOwnerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlacetteEntity::class,
            parentColumns = ["placetteId"],
            childColumns = ["placetteOwnerId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = EssenceEntity::class,
            parentColumns = ["code"],
            childColumns = ["essenceCode"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(name = "index_permanent_trees_uuid", value = ["uuid"], unique = true),
        Index(name = "index_permanent_trees_parcelleOwnerId", value = ["parcelleOwnerId"]),
        Index(name = "index_permanent_trees_placetteOwnerId", value = ["placetteOwnerId"]),
        Index(name = "index_permanent_trees_essenceCode", value = ["essenceCode"])
    ]
)
data class PermanentTreeEntity(
    @PrimaryKey
    val treeId: String,
    /** UUID normalisé (RFC 4122) pour interop GSIE serveur. */
    val uuid: String?,
    val parcelleOwnerId: String,
    val placetteOwnerId: String?,
    val essenceCode: String,
    /** Numéro d'arbre sur le terrain (marquage physique). */
    val numeroArbre: Int?,
    /** Coordonnées GPS permanentes (WKT POINT) — emplacement de l'arbre. */
    val gpsWkt: String?,
    val precisionM: Double?,
    val altitudeM: Double?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Metadata spec GeoSylva 3.0 (GEOSYLVA-003 §3.1)
    val deletedAt: Long? = null,
    val auteur: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
