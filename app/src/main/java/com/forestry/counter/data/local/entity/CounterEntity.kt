package com.forestry.counter.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counters",
    foreignKeys = [ForeignKey(
        entity = GroupEntity::class,
        parentColumns = ["groupId"],
        childColumns = ["groupOwnerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(name = "index_counters_groupOwnerId", value = ["groupOwnerId"]),
        Index(name = "index_counters_name", value = ["name"]),
        Index(name = "index_counters_sortIndex", value = ["sortIndex"]),
        Index(name = "index_counters_groupOwnerId_sortIndex", value = ["groupOwnerId", "sortIndex"])
    ]
)
data class CounterEntity(
    @PrimaryKey
    val counterId: String,
    val groupOwnerId: String,
    val name: String,
    val value: Double = 0.0,      // allows non-integer calculations
    val step: Double = 1.0,
    val min: Double? = null,
    val max: Double? = null,
    val bgColor: String? = null,   // UI customization
    val fgColor: String? = null,
    val iconName: String? = null,
    val isComputed: Boolean = false, // computed counter (read-only)
    val formulaId: String? = null,   // if computed
    val targetValue: Double? = null,
    val decimalPlaces: Int? = null,  // number formatting
    val initialValue: Double? = null,
    val resetValue: Double? = null,
    val soundEnabled: Boolean? = null,     // null=inherit, true/false override
    val vibrationEnabled: Boolean? = null, // null=inherit
    val vibrationIntensity: Int? = null,   // 1..3, null=inherit
    val targetAction: String? = null,      // NONE, SOUND, VIBRATE, BOTH
    val tags: String? = null,        // comma-separated tags for filtering
    val showInFieldView: Boolean = true,
    val tileSize: String = "NORMAL", // SMALL, NORMAL, LARGE
    val sortIndex: Int = 0,
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
