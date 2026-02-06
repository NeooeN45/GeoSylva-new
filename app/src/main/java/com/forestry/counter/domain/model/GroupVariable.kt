package com.forestry.counter.domain.model

data class GroupVariable(
    val id: String,
    val groupId: String,
    val name: String,
    val value: Double,
    val description: String? = null
)
