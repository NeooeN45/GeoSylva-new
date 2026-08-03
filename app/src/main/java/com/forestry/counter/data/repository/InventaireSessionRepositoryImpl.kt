package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.InventaireSessionDao
import com.forestry.counter.data.mapper.toDomain
import com.forestry.counter.data.mapper.toEntity
import com.forestry.counter.data.service.MetadataService
import com.forestry.counter.domain.model.InventaireSession
import com.forestry.counter.domain.repository.InventaireSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventaireSessionRepositoryImpl(
    private val dao: InventaireSessionDao,
    private val metadataService: MetadataService,
) : InventaireSessionRepository {
    override fun getByParcelle(parcelleId: String): Flow<List<InventaireSession>> =
        dao.getByParcelle(parcelleId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: String): InventaireSession? =
        dao.getById(id)?.toDomain()
    override suspend fun getLatestByType(parcelleId: String, type: String): InventaireSession? =
        dao.getLatestByType(parcelleId, type)?.toDomain()
    override suspend fun insert(session: InventaireSession) =
        dao.insert(metadataService.enrichForCreate(session).toEntity())
    override suspend fun update(session: InventaireSession) =
        dao.update(metadataService.enrichForUpdate(session, baseVersion = session.version).toEntity())
    override suspend fun deleteById(id: String) = dao.deleteById(id, System.currentTimeMillis())
}
