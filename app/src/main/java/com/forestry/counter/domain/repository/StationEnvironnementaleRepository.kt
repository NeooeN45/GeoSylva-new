package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.StationEnvironnementale
import kotlinx.coroutines.flow.Flow

interface StationEnvironnementaleRepository {
    fun getByParcelle(parcelleId: String): Flow<StationEnvironnementale?>
    suspend fun getByParcelleOnce(parcelleId: String): StationEnvironnementale?
    suspend fun insert(station: StationEnvironnementale)
    suspend fun update(station: StationEnvironnementale)
    suspend fun updateDvf(parcelleId: String, prix: Double?, nb: Int?, fetchedAt: Long)
    suspend fun deleteByParcelle(parcelleId: String)
}
