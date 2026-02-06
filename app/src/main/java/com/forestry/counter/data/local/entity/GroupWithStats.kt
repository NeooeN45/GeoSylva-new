package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo

/**
 * Projection for groups with aggregated stats.
 */
data class GroupWithStats(
    val groupId: String,
    val name: String,
    val color: String?,
    val sortIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(name = "counterCount") val counterCount: Int,
    @ColumnInfo(name = "totalValue") val totalValue: Double?
)
