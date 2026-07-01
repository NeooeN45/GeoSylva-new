package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.ValeurFonciereDao
import com.forestry.counter.data.mapper.toDomain
import com.forestry.counter.data.mapper.toEntity
import com.forestry.counter.domain.model.ValeurFonciere
import com.forestry.counter.domain.repository.ValeurFonciereRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ValeurFonciereRepositoryImpl(private val dao: ValeurFonciereDao) : ValeurFonciereRepository {
    override fun getByParcelle(parcelleId: String): Flow<ValeurFonciere?> =
        dao.getByParcelle(parcelleId).map { it?.toDomain() }
    override suspend fun getByParcelleOnce(parcelleId: String): ValeurFonciere? =
        dao.getByParcelleOnce(parcelleId)?.toDomain()
    override suspend fun sumPatrimoineTotal(): Double? = dao.sumPatrimoineTotal()
    override suspend fun sumCarboneTotal(): Double? = dao.sumCarboneTotal()
    override suspend fun insert(valeur: ValeurFonciere) = dao.insert(valeur.toEntity())
    override suspend fun update(valeur: ValeurFonciere) = dao.update(valeur.toEntity())
    override suspend fun deleteByParcelle(parcelleId: String) = dao.deleteByParcelle(parcelleId)
}
