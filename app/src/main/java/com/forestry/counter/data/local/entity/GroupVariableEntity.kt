package com.forestry.counter.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Custom variables for group-level calculations (e.g., PLOT_AREA=2000)
 */
@Entity(
    tableName = "group_variables",
    foreignKeys = [ForeignKey(
        entity = GroupEntity::class,
        parentColumns = ["groupId"],
        childColumns = ["groupOwnerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("groupOwnerId"), Index("name")]
)
data class GroupVariableEntity(
    @PrimaryKey
    val variableId: String,
    val groupOwnerId: String,
    val name: String,
    val value: Double,
    val description: String? = null
)
