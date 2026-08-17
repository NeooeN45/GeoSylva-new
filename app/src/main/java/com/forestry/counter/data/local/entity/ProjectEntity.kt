package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Projet / dossier — conteneur organisationnel pour les forêts, missions,
 * documents d'un même objectif.
 *
 * Spec GEOSYLVA-003 §29.11 : liste projets + page détail (onglets Vue
 * générale, Forêts, Missions, Documents, Carte, Équipe, Historique).
 *
 * Un projet regroupe des forêts (relation N-N via la table de jonction
 * `project_forests`). Il porte une couleur, un territoire, un statut et
 * une provenance.
 */
@Entity(
    tableName = "projects",
    indices = [
        Index(name = "index_projects_uuid", value = ["uuid"], unique = true),
        Index(name = "index_projects_name", value = ["name"]),
        Index(name = "index_projects_territory", value = ["territory"]),
        Index(name = "index_projects_status", value = ["status"])
    ]
)
data class ProjectEntity(
    @PrimaryKey
    val projectId: String,
    /** UUID normalisé (RFC 4122) pour interop GSIE serveur. */
    val uuid: String?,
    val name: String,
    /** Couleur d'affichage (hex ARGB — ex. "#FF2E7D32"). */
    val color: String?,
    /** Territoire / région / département de rattachement. */
    val territory: String?,
    /** Organisation propriétaire du projet. */
    val organization: String?,
    /** Statut : active, archived, draft. */
    val status: String = "active",
    val description: String?,
    /** Favori (épinglé en haut de liste). */
    val isFavorite: Boolean = false,
    @Embedded
    val provenance: ProvenanceEmbed,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Metadata spec GeoSylva 3.0 (GEOSYLVA-003 §3.1)
    val deletedAt: Long? = null,
    val auteur: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
