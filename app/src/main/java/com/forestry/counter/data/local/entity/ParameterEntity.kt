package com.forestry.counter.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parameters")
data class ParameterEntity(
    @PrimaryKey
    val key: String,
    val valueJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
