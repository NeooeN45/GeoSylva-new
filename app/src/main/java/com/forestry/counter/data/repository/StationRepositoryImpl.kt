package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.StationDao
import com.forestry.counter.data.local.entity.StationEntity
import com.forestry.counter.data.service.MetadataService
import com.forestry.counter.domain.model.station.StationObservation
import com.forestry.counter.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StationRepositoryImpl(
    private val dao: StationDao,
    private val metadataService: MetadataService,
) : StationRepository {
    override fun getByParcelle(parcelleId: String): Flow<List<StationObservation>> {
        return dao.getByParcelle(parcelleId).map { list -> list.map { it.toDomain() } }
    }

    override fun getById(id: String): Flow<StationObservation?> {
        return dao.getById(id).map { it?.toDomain() }
    }

    override fun getAll(): Flow<List<StationObservation>> {
        return dao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun save(obs: StationObservation) {
        // Upsert : distingue création (auteur null) d'une modification.
        val enriched = if (obs.auteur == null) {
            metadataService.enrichForCreate(obs)
        } else {
            metadataService.enrichForUpdate(obs, baseVersion = obs.version)
        }
        dao.insert(StationEntity.fromDomain(enriched))
    }

    override suspend fun delete(obs: StationObservation) {
        dao.deleteById(obs.id, System.currentTimeMillis())
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id, System.currentTimeMillis())
    }
}
