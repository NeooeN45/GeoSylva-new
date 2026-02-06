package com.forestry.counter.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "essences")
data class EssenceEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val categorie: String?,
    val densiteBoite: Double?,
    val colorHex: String? // nullable for backward compatibility
)
