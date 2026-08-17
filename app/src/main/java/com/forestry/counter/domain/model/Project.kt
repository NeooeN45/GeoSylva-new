package com.forestry.counter.domain.model

import androidx.compose.runtime.Stable

/**
 * Domain model pour un projet — spec GEOSYLVA-003 §29.11.
 *
 * Conteneur organisationnel pour les forêts, missions, documents
 * d'un même objectif.
 */
@Stable
data class Project(
    val projectId: String,
    val uuid: String? = null,
    val name: String,
    val color: String? = null,
    val territory: String? = null,
    val organization: String? = null,
    val status: String = "active",
    val description: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val auteur: String? = null,
    val source: String? = null,
    val version: Int = 1,
)
