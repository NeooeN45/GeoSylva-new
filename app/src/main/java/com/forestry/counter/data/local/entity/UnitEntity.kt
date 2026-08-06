package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Catalogue d'unités de mesure — table de référence.
 *
 * Spec GEOSYLVA-003 §7.6 : catalogue d'unités (m, cm, m², m³/ha, kg, t) +
 * conversion automatique aux frontières.
 *
 * Les unités sont groupées par dimension (length, area, volume, mass, etc.)
 * pour permettre la conversion. Chaque unité a un facteur vers l'unité de
 * référence de sa dimension.
 */
@Entity(
    tableName = "units",
    indices = [
        Index(name = "index_units_code", value = ["code"], unique = true),
        Index(name = "index_units_dimension", value = ["dimension"])
    ]
)
data class UnitEntity(
    @PrimaryKey
    val code: String,
    /** Symbole affiché : m, cm, m², m³, m³/ha, kg, t. */
    val symbol: String,
    /** Nom complet en français : mètre, centimètre, mètre carré. */
    val nameFr: String,
    /** Dimension : length, area, volume, mass, angle, count, dimensionless. */
    val dimension: String,
    /** Facteur vers l'unité de référence de la dimension (1.0 pour la réf). */
    val toBaseFactor: Double,
    /** Unité de référence de la dimension (ex. length → m, mass → kg). */
    val baseUnitCode: String,
    /** Description / contexte d'usage. */
    val description: String?,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
