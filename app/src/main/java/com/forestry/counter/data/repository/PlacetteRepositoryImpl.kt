package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.PlacetteDao
import com.forestry.counter.data.mapper.toPlacette
import com.forestry.counter.data.mapper.toPlacetteEntity
import com.forestry.counter.domain.model.Placette
import com.forestry.counter.domain.repository.PlacetteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlacetteRepositoryImpl(
    private val placetteDao: PlacetteDao
) : PlacetteRepository {

    override fun getPlacettesByParcelle(parcelleId: String): Flow<List<Placette>> {
        return placetteDao.getPlacettesByParcelle(parcelleId).map { list -> list.map { it.toPlacette() } }
    }

    override fun getPlacetteById(placetteId: String): Flow<Placette?> {
        return placetteDao.getPlacetteByIdFlow(placetteId).map { it?.toPlacette() }
    }

    override suspend fun insertPlacette(placette: Placette) {
        placetteDao.insertPlacette(placette.toPlacetteEntity())
    }

    override suspend fun updatePlacette(placette: Placette) {
        placetteDao.updatePlacette(placette.toPlacetteEntity())
    }

    override suspend fun deletePlacette(placetteId: String) {
        placetteDao.deletePlacetteById(placetteId)
    }

    override suspend fun deletePlacettesByParcelle(parcelleId: String) {
        placetteDao.deletePlacettesByParcelle(parcelleId)
    }
}
