package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.Foret
import kotlinx.coroutines.flow.Flow

interface ForetRepository {
    fun getAll(): Flow<List<Foret>>
    suspend fun getById(id: String): Foret?
    suspend fun insert(foret: Foret)
    suspend fun update(foret: Foret)
    suspend fun deleteById(id: String)
}
