package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.ForetDao
import com.forestry.counter.data.mapper.toDomain
import com.forestry.counter.data.mapper.toEntity
import com.forestry.counter.data.service.MetadataService
import com.forestry.counter.domain.model.Foret
import com.forestry.counter.domain.repository.ForetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ForetRepositoryImpl(
    private val dao: ForetDao,
    private val metadataService: MetadataService,
) : ForetRepository {
    override fun getAll(): Flow<List<Foret>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: String): Foret? =
        dao.getById(id)?.toDomain()
    override suspend fun insert(foret: Foret) =
        dao.insert(metadataService.enrichForCreate(foret).toEntity())
    override suspend fun update(foret: Foret) =
        dao.update(metadataService.enrichForUpdate(foret, baseVersion = foret.version).toEntity())
    override suspend fun deleteById(id: String) = dao.deleteById(id)
}
