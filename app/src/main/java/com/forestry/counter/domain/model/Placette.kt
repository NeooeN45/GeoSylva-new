package com.forestry.counter.domain.model

data class Placette(
    val id: String,
    val parcelleId: String,
    val name: String?,
    val type: String?,
    val rayonM: Double?,
    val surfaceM2: Double?,
    val centerWkt: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
