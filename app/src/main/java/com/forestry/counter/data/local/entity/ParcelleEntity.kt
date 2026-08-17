package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parcelles",
    foreignKeys = [
        // FK uniquement sur foretId (référence vers ForetEntity).
        // forestOwnerId stocke un groupId (table groups) — pas de FK car
        // GroupEntity et ForetEntity sont des tables séparées.
        ForeignKey(
            entity = ForetEntity::class,
            parentColumns = ["foretId"],
            childColumns = ["foretId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(name = "index_parcelles_name", value = ["name"]),
        Index(name = "index_parcelles_forestOwnerId", value = ["forestOwnerId"]),
        Index(name = "index_parcelles_foretId", value = ["foretId"]),
        Index(name = "index_parcelles_codeInsee", value = ["codeInseeCommune"]),
        Index(name = "index_parcelles_uuid", value = ["uuid"], unique = true)
    ]
)
data class ParcelleEntity(
    @PrimaryKey
    val parcelleId: String,
    /** UUID normalisé (RFC 4122) pour interop GSIE serveur — backfill asynchrone. */
    val uuid: String? = null,
    val forestOwnerId: String?,
    val foretId: String?,
    val name: String,
    val surfaceHa: Double?,
    val shape: String?,
    val slopePct: Double?,
    val aspect: String?,
    val access: String?,
    val altitudeM: Double?,
    val objectifType: String?,
    val objectifVal: Double?,
    val tolerancePct: Double?,
    val samplingMode: String?,
    val sampleAreaM2: Double?,
    val targetSpeciesCsv: String?,
    val srid: Int?,
    val remarks: String?,

    // Données cadastrales (IGN reverse géocodage)
    val codeInseeCommune: String?,
    val nomCommune: String?,
    val sectionCadastrale: String?,
    val numeroCadastral: String?,
    val contenanceCadastraleHa: Double?,
    val geometrieIgnWkt: String?,
    val natureCadastraleCode: String?,
    val localisationMode: String?,

    // SylvoÉcoRégion (IFN/IGN)
    val codeSer: String?,
    val nomSer: String?,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    /** Provenance normalisée (organisme source, licence, précision, statut) — spec §29.13. */
    @Embedded
    val provenance: ProvenanceEmbed = ProvenanceEmbed(null, null, null, null, null),

    // Metadata spec GeoSylva 3.0 (GEOSYLVA-003 §3.1)
    val deletedAt: Long? = null,
    val auteur: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
