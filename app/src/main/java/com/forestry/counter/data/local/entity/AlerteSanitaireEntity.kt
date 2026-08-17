package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alertes_sanitaires",
    foreignKeys = [
        ForeignKey(
            entity = ParcelleEntity::class,
            parentColumns = ["parcelleId"],
            childColumns = ["parcelleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InventaireSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(name = "index_alerte_parcelleId", value = ["parcelleId"]),
        Index(name = "index_alerte_sessionId", value = ["sessionId"]),
        Index(name = "index_alerte_niveauRisque", value = ["niveauRisque"])
    ]
)
data class AlerteSanitaireEntity(
    @PrimaryKey
    val alerteId: String,
    val parcelleId: String,
    val sessionId: String?,
    val codePathogene: String,
    val nomPathogene: String,
    val niveauRisque: String,
    val nbTigesAtteintes: Int?,
    val pctTigesAtteintes: Double?,
    val essencesCiblesJson: String?,
    val symptomesObservesJson: String?,
    val recommandationsJson: String?,
    val isOrganismeReglemente: Boolean,
    val dateDetection: Long,
    val isAlerteDsf: Boolean,
    val remarques: String?,

    // Metadata spec GeoSylva 3.0 (GEOSYLVA-003 §3.1)
    val deletedAt: Long? = null,
    val auteur: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
