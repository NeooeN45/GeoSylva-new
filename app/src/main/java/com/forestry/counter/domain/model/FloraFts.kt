package com.forestry.counter.domain.model

/**
 * Domain models pour les tables FTS et cache GPS.
 * Mappés depuis/vers FloraFtsEntity et GpsContextCacheEntity dans data/mapper.
 */

data class FloraFts(
    val speciesId: String = "",
    val nomFrancais: String = "",
    val nomScientifique: String = "",
    val vernaculaires: String = "",
    val synonymes: String = "",
    val typeMilieu: String = "",
    val strate: String = ""
)

data class GpsContextCache(
    val latKey: Double,
    val lonKey: Double,
    val regionCode: String = "",
    val deptCode: String = "",
    val altitudeApproxM: Double = 0.0,
    val topoHint: String = "",
    val zoneHumideProb: Double = 0.0,
    val packIdActive: String = "",
    val computedAt: Long = System.currentTimeMillis()
)
