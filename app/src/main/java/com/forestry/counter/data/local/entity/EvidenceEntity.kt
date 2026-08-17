package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Pièce jointe (preuve) rattachée à une observation ou une mesure.
 *
 * Spec GEOSYLVA-003 §7.6 : photo, audio, document, coordonnée GPS.
 * Une preuve peut être rattachée à une observation (observationId renseigné,
 * measurementId null) ou à une mesure spécifique (les deux renseignés).
 */
@Entity(
    tableName = "evidence",
    foreignKeys = [
        ForeignKey(
            entity = TreeObservationEntity::class,
            parentColumns = ["observationId"],
            childColumns = ["observationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MeasurementEntity::class,
            parentColumns = ["measurementId"],
            childColumns = ["measurementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(name = "index_evidence_uuid", value = ["uuid"], unique = true),
        Index(name = "index_evidence_observationId", value = ["observationId"]),
        Index(name = "index_evidence_measurementId", value = ["measurementId"])
    ]
)
data class EvidenceEntity(
    @PrimaryKey
    val evidenceId: String,
    /** UUID normalisé (RFC 4122) pour interop GSIE serveur. */
    val uuid: String?,
    val observationId: String,
    /** Mesure spécifique rattachée — null si la preuve est au niveau observation. */
    val measurementId: String?,
    /** Type de preuve : photo, audio, document, gps_track, gps_point. */
    val type: String,
    /** URI locale du fichier (content:// ou file://). */
    val uri: String,
    /** Hash SHA-256 du contenu pour dédoublonnage et intégrité. */
    val sha256: String?,
    /** Taille en octets. */
    val sizeBytes: Long?,
    /** MIME type (image/jpeg, audio/mp4, application/pdf, etc.). */
    val mimeType: String?,
    /** Légende ou description libre. */
    val caption: String?,
    /** Coordonnées GPS au moment de la capture (WKT POINT) — pour photos. */
    val gpsWkt: String?,
    val capturedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),

    // Metadata spec GeoSylva 3.0 (GEOSYLVA-003 §3.1)
    val deletedAt: Long? = null,
    val auteur: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
