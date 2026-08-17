package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.Project
import kotlinx.coroutines.flow.Flow

/**
 * Repository pour les projets — spec GEOSYLVA-003 §29.11.
 */
interface ProjectRepository {
    fun getAll(): Flow<List<Project>>
    suspend fun getById(id: String): Project?
    suspend fun insert(project: Project)
    suspend fun update(project: Project)
    suspend fun delete(id: String)
    suspend fun setFavorite(id: String, favorite: Boolean)
    suspend fun addForest(projectId: String, foretId: String)
    suspend fun removeForest(projectId: String, foretId: String)
    suspend fun getForestIds(projectId: String): List<String>
}
