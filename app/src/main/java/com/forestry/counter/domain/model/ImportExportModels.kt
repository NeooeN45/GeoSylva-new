package com.forestry.counter.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CounterExport(
    val id: String,
    val groupId: String,
    val groupName: String,
    val name: String,
    val value: Double,
    val step: Double,
    val min: Double? = null,
    val max: Double? = null,
    val bgColor: String? = null,
    val fgColor: String? = null,
    val targetValue: Double? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class GroupExport(
    val id: String,
    val name: String,
    val color: String? = null,
    val counters: List<CounterExport>,
    val formulas: List<FormulaExport>,
    val variables: List<VariableExport>
)

@Serializable
data class FormulaExport(
    val id: String,
    val name: String,
    val expression: String,
    val description: String? = null
)

@Serializable
data class VariableExport(
    val name: String,
    val value: Double,
    val description: String? = null
)

@Serializable
data class AppExport(
    val version: String = "1.0.0",
    val exportDate: Long,
    val groups: List<GroupExport>
)

data class ImportMapping(
    val sourceColumn: String,
    val targetField: String,
    val transform: ((String) -> Any)? = null
)

enum class ImportMode {
    REPLACE,    // Delete existing and import new
    MERGE,      // Update existing, add new
    ADD         // Only add new (no updates)
}

data class ImportResult(
    val success: Boolean,
    val importedCount: Int,
    val skippedCount: Int,
    val errorCount: Int,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

enum class ExportFormat {
    CSV,
    XLSX,
    JSON,
    SQLITE,
    ZIP
}

enum class ExportLayout {
    LONG,   // One row per counter
    PIVOT   // Pivoted by species x class
}
