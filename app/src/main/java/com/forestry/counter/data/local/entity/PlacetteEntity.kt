package com.forestry.counter.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "placettes",
    foreignKeys = [ForeignKey(
        entity = ParcelleEntity::class,
        parentColumns = ["parcelleId"],
        childColumns = ["parcelleOwnerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(name = "index_placettes_parcelleOwnerId", value = ["parcelleOwnerId"])
    ]
)
data class PlacetteEntity(
    @PrimaryKey
    val placetteId: String,
    val parcelleOwnerId: String,
    val name: String?,
    val type: String?,
    val rayonM: Double?,
    val surfaceM2: Double?,
    val centerWkt: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
