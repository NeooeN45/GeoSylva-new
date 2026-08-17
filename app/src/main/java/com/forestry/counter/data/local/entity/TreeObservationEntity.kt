package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Observation d'un arbre lors d'une campagne — événement daté et localisé.
 *
 * Spec GEOSYLVA-003 §7.6 : chaque campagne produit une nouvelle observation.
 * Une observation contient des [MeasurementEntity] et peut référencer des
 * [EvidenceEntity]. Aucune observation n'écrase une autre — tout est conservé
 * pour traçabilité temporelle.
 *
 * Une observation peut aussi être générique (non rattachée à un arbre
 * permanent) — par exemple une observation de peuplement, de station, ou
 * une observation floristique. Dans ce cas, `treeId` est null et
 * `targetType`/`targetId` désignent l'objet observé.
 */
@Entity(
    tableName = "observations",
    foreignKeys = [
        ForeignKey(
            entity = PermanentTreeEntity::class,
            parentColumns = ["treeId"],
            childColumns = ["treeId"],
            onDelete = ForeignKey.CASCADE
        ),
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
        )
    ],
    indices = [
        Index(name = "index_observations_uuid", value = ["uuid"], unique = true),
        Index(name = "index_observations_treeId", value = ["treeId"]),
        Index(name = "index_observations_parcelleOwnerId", value = ["parcelleOwnerId"]),
        Index(name = "index_observations_placetteOwnerId", value = ["placetteOwnerId"]),
        Index(name = "index_observations_observedAt", value = ["observedAt"]),
        Index(name = "index_observations_protocol", value = ["protocol"])
    ]
)
data class TreeObservationEntity(
    @PrimaryKey
    val observationId: String,
    /** UUID normalisé (RFC 4122) pour interop GSIE serveur. */
    val uuid: String?,
    /** Arbre permanent observé — null si observation générique. */
    val treeId: String?,
    /** Parcelle propriétaire (toujours renseigné pour le contexte territorial). */
    val parcelleOwnerId: String,
    /** Placette propriétaire — null si observation hors placette. */
    val placetteOwnerId: String?,
    /** Type de cible pour les observations non-arbre : peuplement, station, flore, etc. */
    val targetType: String?,
    /** ID de la cible (non FK — référence logique). */
    val targetId: String?,
    /** Protocole appliqué : inventaire, martelage, diagnostic_sanitaire, ibp, etc. */
    val protocol: String,
    /** Observateur (nom ou identifiant utilisateur). */
    val observer: String?,
    /** Date/heure de l'observation (epoch millis). */
    val observedAt: Long = System.currentTimeMillis(),
    /** Coordonnées GPS de l'observation (WKT) — null si héritées du parent. */
    val gpsWkt: String?,
    val precisionM: Double?,
    /** Notes libres de l'observateur. */
    val note: String?,
    /** Provenance normalisée (organisme source, licence, précision, statut). */
    @Embedded
    val provenance: ProvenanceEmbed,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Metadata spec GeoSylva 3.0 (GEOSYLVA-003 §3.1)
    val deletedAt: Long? = null,
    val auteur: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
