package com.forestry.counter.domain.calculation

import kotlinx.serialization.Serializable

@Serializable
data class CoefVolumeRange(
    val essence: String,
    val min: Int,
    val max: Int,
    val f: Double,
    val method: String? = null // ex: "LENT" ou "RAPIDE" (optionnel pour compatibilité)
)

@Serializable
data class HeightDefaultRange(
    val essence: String,
    val min: Int,
    val max: Int,
    val h: Double
)

@Serializable
data class HeightModeEntry(
    val essence: String,
    val diamClass: Int,
    val mode: String,          // DEFAULT | FIXED | SAMPLES
    val fixed: Double? = null  // required if mode == FIXED
)

data class ClassSynthesis(
    val diamClass: Int,
    val count: Int,
    val hMean: Double?,
    val vSum: Double?,
    val valueSumEur: Double? = null
)

data class SynthesisTotals(
    val nTotal: Int,
    val dmWeighted: Double?,
    val hMean: Double?,
    val vTotal: Double?
)

@Serializable
data class ProductRule(
    val essence: String? = null, // null or "*" = wildcard
    val min: Int? = null,
    val max: Int? = null,
    val product: String // BO, BI, BCh, PATE
)

@Serializable
data class PriceEntry(
    val essence: String,
    val product: String,
    val min: Int,
    val max: Int,
    val eurPerM3: Double
)
