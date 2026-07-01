package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.ObservationFlore
import kotlinx.coroutines.flow.Flow

interface ObservationFloreRepository {
    fun getByParcelle(parcelleId: String): Flow<List<ObservationFlore>>
    fun getByPlacette(placetteId: String): Flow<List<ObservationFlore>>
    suspend fun getBySession(sessionId: String): List<ObservationFlore>
    suspend fun countSpeciesByParcelle(parcelleId: String): Int
    suspend fun getProtectedSpeciesByParcelle(parcelleId: String): List<ObservationFlore>
    suspend fun insert(observation: ObservationFlore)
    suspend fun insertAll(observations: List<ObservationFlore>)
    suspend fun update(observation: ObservationFlore)
    suspend fun delete(observation: ObservationFlore)
    suspend fun deleteByParcelle(parcelleId: String)
}
