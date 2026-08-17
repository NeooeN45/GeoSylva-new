package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.StationEnvironnementaleDao
import com.forestry.counter.data.mapper.toDomain
import com.forestry.counter.data.mapper.toEntity
import com.forestry.counter.domain.model.StationEnvironnementale
import com.forestry.counter.domain.repository.StationEnvironnementaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StationEnvironnementaleRepositoryImpl(private val dao: StationEnvironnementaleDao) : StationEnvironnementaleRepository {
    override fun getByParcelle(parcelleId: String): Flow<StationEnvironnementale?> =
        dao.getByParcelle(parcelleId).map { it?.toDomain() }
    override suspend fun getByParcelleOnce(parcelleId: String): StationEnvironnementale? =
        dao.getByParcelleOnce(parcelleId)?.toDomain()
    override suspend fun insert(station: StationEnvironnementale) = dao.insert(station.toEntity())
    override suspend fun update(station: StationEnvironnementale) = dao.update(station.toEntity())
    override suspend fun updateDvf(parcelleId: String, prix: Double?, nb: Int?, fetchedAt: Long) = dao.updateDvf(parcelleId, prix, nb, fetchedAt)
    override suspend fun deleteByParcelle(parcelleId: String) = dao.deleteByParcelle(parcelleId)
}
