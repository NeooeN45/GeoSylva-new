package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.Parcelle
import kotlinx.coroutines.flow.Flow

interface ParcelleRepository {
    fun getAllParcelles(): Flow<List<Parcelle>>
    fun getParcellesByForest(forestId: String): Flow<List<Parcelle>>
    fun getParcelleById(parcelleId: String): Flow<Parcelle?>
    suspend fun insertParcelle(parcelle: Parcelle)
    suspend fun updateParcelle(parcelle: Parcelle)
    suspend fun deleteParcelle(parcelleId: String)
    suspend fun deleteAllParcelles()
}
