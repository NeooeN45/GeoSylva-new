package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mesure atomique rattachée à une observation.
 *
 * Spec GEOSYLVA-003 §7.6 : chaque observation contient des mesures. Une
 * mesure = un type (diamètre, hauteur, etc.), une valeur, une unité, une
 * incertitude et une méthode. Aucune mesure n'écrase une autre.
 *
 * Les mesures sont immuables une fois enregistrées — une correction se
 * fait par une nouvelle mesure (avec `replacesMeasurementId` pointant vers
 * la mesure remplacée) pour préserver la traçabilité.
 */
@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = TreeObservationEntity::class,
            parentColumns = ["observationId"],
            childColumns = ["observationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(name = "index_measurements_uuid", value = ["uuid"], unique = true),
        Index(name = "index_measurements_observationId", value = ["observationId"]),
        Index(name = "index_measurements_type", value = ["type"]),
        Index(name = "index_measurements_replaces", value = ["replacesMeasurementId"])
    ]
)
data class MeasurementEntity(
    @PrimaryKey
    val measurementId: String,
    /** UUID normalisé (RFC 4122) pour interop GSIE serveur. */
    val uuid: String?,
    val observationId: String,
    /** Type de mesure : diameter, height, circumference, volume, basal_area, etc. */
    val type: String,
    /** Valeur mesurée (dans l'unité déclarée). */
    val value: Double,
    /** Code unité (référence vers [UnitEntity] — ex. "cm", "m", "m2", "m3"). */
    val unitCode: String,
    /** Incertitude de mesure (± dans la même unité). Null si non mesurée. */
    val uncertainty: Double?,
    /** Méthode de mesure : tape, compas, clinometer, lidar, visual, etc. */
    val method: String?,
    /** Hauteur de mesure pour les diamètres (ex. 1.30m) — null si non applicable. */
    val heightM: Double?,
    /** Mesure qui remplace celle-ci (traçabilité des corrections). */
    val replacesMeasurementId: String?,
    /** Instrument utilisé (identifiant ou modèle). */
    val instrument: String?,
    val measuredAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),

    // Metadata spec GeoSylva 3.0 (GEOSYLVA-003 §3.1)
    val deletedAt: Long? = null,
    val auteur: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
