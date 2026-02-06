package com.forestry.counter.domain.model

data class Formula(
    val id: String,
    val groupId: String,
    val name: String,
    val expression: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
