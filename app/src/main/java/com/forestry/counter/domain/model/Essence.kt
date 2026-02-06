package com.forestry.counter.domain.model

data class Essence(
    val code: String,
    val name: String,
    val categorie: String?,
    val densiteBoite: Double?,
    val colorHex: String? = null
)
