package com.forestry.counter.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "formulas",
    foreignKeys = [ForeignKey(
        entity = GroupEntity::class,
        parentColumns = ["groupId"],
        childColumns = ["groupOwnerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("groupOwnerId")]
)
data class FormulaEntity(
    @PrimaryKey
    val formulaId: String,
    val groupOwnerId: String,
    val name: String,
    val expression: String,  // e.g., "sum(name:startsWith('Hêtre'))"
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
