package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.IbpEvaluation
import kotlinx.coroutines.flow.Flow

interface IbpRepository {
    fun getByPlacette(placetteId: String): Flow<List<IbpEvaluation>>
    fun getByParcelle(parcelleId: String): Flow<List<IbpEvaluation>>
    fun getById(id: String): Flow<IbpEvaluation?>
    fun getAll(): Flow<List<IbpEvaluation>>
    suspend fun save(evaluation: IbpEvaluation)
    suspend fun delete(id: String)
    suspend fun deleteByPlacette(placetteId: String)
}
