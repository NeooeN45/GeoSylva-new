package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.ObservationFloreDao
import com.forestry.counter.data.mapper.toDomain
import com.forestry.counter.data.mapper.toEntity
import com.forestry.counter.domain.model.ObservationFlore
import com.forestry.counter.domain.repository.ObservationFloreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObservationFloreRepositoryImpl(private val dao: ObservationFloreDao) : ObservationFloreRepository {
    override fun getByParcelle(parcelleId: String): Flow<List<ObservationFlore>> =
        dao.getByParcelle(parcelleId).map { list -> list.map { it.toDomain() } }
    override fun getByPlacette(placetteId: String): Flow<List<ObservationFlore>> =
        dao.getByPlacette(placetteId).map { list -> list.map { it.toDomain() } }
    override suspend fun getBySession(sessionId: String): List<ObservationFlore> =
        dao.getBySession(sessionId).map { it.toDomain() }
    override suspend fun countSpeciesByParcelle(parcelleId: String): Int = dao.countSpeciesByParcelle(parcelleId)
    override suspend fun getProtectedSpeciesByParcelle(parcelleId: String): List<ObservationFlore> =
        dao.getProtectedSpeciesByParcelle(parcelleId).map { it.toDomain() }
    override suspend fun insert(observation: ObservationFlore) = dao.insert(observation.toEntity())
    override suspend fun insertAll(observations: List<ObservationFlore>) =
        dao.insertAll(observations.map { it.toEntity() })
    override suspend fun update(observation: ObservationFlore) = dao.update(observation.toEntity())
    override suspend fun delete(observation: ObservationFlore) = dao.delete(observation.toEntity())
    override suspend fun deleteByParcelle(parcelleId: String) = dao.deleteByParcelle(parcelleId)
}
