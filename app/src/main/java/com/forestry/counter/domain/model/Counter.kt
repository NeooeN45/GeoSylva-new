package com.forestry.counter.domain.model

data class Counter(
    val id: String,
    val groupId: String,
    val name: String,
    val value: Double = 0.0,
    val step: Double = 1.0,
    val min: Double? = null,
    val max: Double? = null,
    val bgColor: String? = null,
    val fgColor: String? = null,
    val iconName: String? = null,
    val isComputed: Boolean = false,
    val formulaId: String? = null,
    val targetValue: Double? = null,
    val decimalPlaces: Int? = null,
    val initialValue: Double? = null,
    val resetValue: Double? = null,
    val soundEnabled: Boolean? = null,
    val vibrationEnabled: Boolean? = null,
    val vibrationIntensity: Int? = null, // 1..3
    val targetAction: TargetAction? = null,
    val tags: List<String> = emptyList(),
    val showInFieldView: Boolean = true,
    val tileSize: TileSize = TileSize.NORMAL,
    val sortIndex: Int = 0,
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAtTarget: Boolean
        get() = targetValue?.let { value >= it } ?: false

    val progress: Float
        get() = targetValue?.let { 
            if (it > 0) (value / it).toFloat().coerceIn(0f, 1f) else 0f 
        } ?: 0f

    fun canIncrement(): Boolean {
        return max?.let { value + step <= it } ?: true
    }

    fun canDecrement(): Boolean {
        return min?.let { value - step >= it } ?: true
    }
}

enum class TileSize {
    SMALL, NORMAL, LARGE
}

enum class TargetAction {
    NONE, SOUND, VIBRATE, BOTH
}
