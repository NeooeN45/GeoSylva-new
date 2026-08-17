package com.forestry.counter.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Table de jonction N-N entre projets et forêts.
 *
 * Spec GEOSYLVA-003 §29.11 : un projet regroupe des forêts. Une forêt
 * peut appartenir à plusieurs projets.
 */
@Entity(
    tableName = "project_forests",
    primaryKeys = ["projectId", "foretId"],
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ForetEntity::class,
            parentColumns = ["foretId"],
            childColumns = ["foretId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(name = "index_project_forests_projectId", value = ["projectId"]),
        Index(name = "index_project_forests_foretId", value = ["foretId"])
    ]
)
data class ProjectForestCrossRef(
    val projectId: String,
    val foretId: String,
    val addedAt: Long = System.currentTimeMillis()
)
