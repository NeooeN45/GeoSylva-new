package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Résultat calculé à partir de mesures.
 *
 * Spec GEOSYLVA-003 §7.6 + §7.10 : un CalculationRun référence les mesures
 * d'entrée, la méthode appliquée (Method Registry — Lot 2), les sorties,
 * l'incertitude et un statut (validé / rejeté / supersédé).
 *
 * Aucun CalculationRun n'écrase un autre — un recalcul crée un nouveau run
 * avec `supersedesRunId` pointant vers le précédent.
 */
@Entity(
    tableName = "calculation_runs",
    foreignKeys = [
        ForeignKey(
            entity = TreeObservationEntity::class,
            parentColumns = ["observationId"],
            childColumns = ["observationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(name = "index_calculation_runs_uuid", value = ["uuid"], unique = true),
        Index(name = "index_calculation_runs_observationId", value = ["observationId"]),
        Index(name = "index_calculation_runs_method", value = ["method"]),
        Index(name = "index_calculation_runs_status", value = ["status"]),
        Index(name = "index_calculation_runs_supersedes", value = ["supersedesRunId"])
    ]
)
data class CalculationRunEntity(
    @PrimaryKey
    val runId: String,
    /** UUID normalisé (RFC 4122) pour interop GSIE serveur. */
    val uuid: String?,
    val observationId: String,
    /** Méthode appliquée (identifiant Method Registry — ex. "cubage.tarif", "volume.schumacher_hall"). */
    val method: String,
    /** Version de la méthode (semver ou hash). */
    val methodVersion: String,
    /** IDs des mesures d'entrée (sérialisé JSON — ex. ["m1","m2"]). */
    val inputMeasurementIds: String,
    /** Sorties calculées (sérialisé JSON — ex. {"volume_m3": 0.45, "uncertainty": 0.03}). */
    val outputs: String,
    /** Incertitude globale estimée (± dans l'unité principale du résultat). */
    val uncertainty: Double?,
    /** Statut du calcul : validated, rejected, superseded, pending. */
    val status: String,
    /** Run précédent remplacé par celui-ci (traçabilité des recalculs). */
    val supersedesRunId: String?,
    /** Message d'erreur si status = rejected. */
    val errorMessage: String?,
    val calculatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),

    // Metadata spec GeoSylva 3.0 (GEOSYLVA-003 §3.1)
    val deletedAt: Long? = null,
    val auteur: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
