package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.ParcelleDao
import com.forestry.counter.data.mapper.toParcelle
import com.forestry.counter.data.mapper.toParcelleEntity
import com.forestry.counter.domain.model.Parcelle
import com.forestry.counter.domain.repository.ParcelleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ParcelleRepositoryImpl(
    private val parcelleDao: ParcelleDao,
    private val onUpsert: suspend (String) -> Unit = {},
    private val onDelete: suspend (String) -> Unit = {},
) : ParcelleRepository {

    override fun getAllParcelles(): Flow<List<Parcelle>> {
        return parcelleDao.getAllParcelles().map { list -> list.map { it.toParcelle() } }
    }

    override fun getParcellesByForest(forestId: String): Flow<List<Parcelle>> {
        return parcelleDao.getParcellesByForest(forestId).map { list -> list.map { it.toParcelle() } }
    }

    override fun getParcelleById(parcelleId: String): Flow<Parcelle?> {
        return parcelleDao.getParcelleByIdFlow(parcelleId).map { it?.toParcelle() }
    }

    override suspend fun insertParcelle(parcelle: Parcelle) {
        parcelleDao.insertParcelle(parcelle.toParcelleEntity())
        runCatching { onUpsert(parcelle.id) }
    }

    override suspend fun updateParcelle(parcelle: Parcelle) {
        parcelleDao.updateParcelle(parcelle.toParcelleEntity())
        runCatching { onUpsert(parcelle.id) }
    }

    override suspend fun deleteParcelle(parcelleId: String) {
        parcelleDao.deleteParcelleById(parcelleId)
        runCatching { onDelete(parcelleId) }
    }

    override suspend fun deleteAllParcelles() {
        val ids = parcelleDao.getAllParcellesNow().map { it.parcelleId }
        parcelleDao.deleteAllParcelles()
        ids.forEach { id -> runCatching { onDelete(id) } }
    }
}
