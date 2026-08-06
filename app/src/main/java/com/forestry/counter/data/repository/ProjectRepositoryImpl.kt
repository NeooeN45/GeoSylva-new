package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.ProjectDao
import com.forestry.counter.data.local.entity.ProjectEntity
import com.forestry.counter.data.local.entity.ProjectForestCrossRef
import com.forestry.counter.data.local.entity.ProvenanceEmbed
import com.forestry.counter.domain.model.Project
import com.forestry.counter.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implémentation de [ProjectRepository] — spec GEOSYLVA-003 §29.11.
 */
class ProjectRepositoryImpl(
    private val projectDao: ProjectDao,
) : ProjectRepository {

    override fun getAll(): Flow<List<Project>> =
        projectDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: String): Project? =
        projectDao.getById(id)?.toDomain()

    override suspend fun insert(project: Project) =
        projectDao.insert(project.toEntity())

    override suspend fun update(project: Project) =
        projectDao.update(project.toEntity())

    override suspend fun delete(id: String) =
        projectDao.delete(id, System.currentTimeMillis())

    override suspend fun setFavorite(id: String, favorite: Boolean) =
        projectDao.setFavorite(id, favorite)

    override suspend fun addForest(projectId: String, foretId: String) =
        projectDao.addForest(ProjectForestCrossRef(projectId, foretId))

    override suspend fun removeForest(projectId: String, foretId: String) =
        projectDao.removeForest(projectId, foretId)

    override suspend fun getForestIds(projectId: String): List<String> =
        projectDao.getForestIds(projectId)

    private fun ProjectEntity.toDomain(): Project = Project(
        projectId = projectId,
        uuid = uuid,
        name = name,
        color = color,
        territory = territory,
        organization = organization,
        status = status,
        description = description,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        auteur = auteur,
        source = source,
        version = version,
    )

    private fun Project.toEntity(): ProjectEntity = ProjectEntity(
        projectId = projectId,
        uuid = uuid,
        name = name,
        color = color,
        territory = territory,
        organization = organization,
        status = status,
        description = description,
        isFavorite = isFavorite,
        provenance = ProvenanceEmbed(null, null, null, null, null),
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        auteur = auteur,
        source = source,
        version = version,
    )
}
