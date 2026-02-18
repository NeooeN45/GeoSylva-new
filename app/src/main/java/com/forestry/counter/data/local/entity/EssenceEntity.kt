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
    val colorHex: String?, // nullable for backward compatibility
    val densiteBois: Double? = null,
    val qualiteTypique: String? = null,
    val typeCoupePreferee: String? = null,
    val usageBois: String? = null,
    val vitesseCroissance: String? = null,
    val hauteurMaxM: Double? = null,
    val diametreMaxCm: Double? = null,
    val toleranceOmbre: String? = null,
    val remarques: String? = null
)
