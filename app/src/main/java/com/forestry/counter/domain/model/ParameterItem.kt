package com.forestry.counter.domain.model

data class ParameterItem(
    val key: String,
    val valueJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
