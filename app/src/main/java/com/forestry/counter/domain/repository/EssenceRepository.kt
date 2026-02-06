package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.Essence
import kotlinx.coroutines.flow.Flow

interface EssenceRepository {
    fun getAllEssences(): Flow<List<Essence>>
    fun getEssenceByCode(code: String): Flow<Essence?>
    suspend fun insertEssence(essence: Essence)
    suspend fun insertEssences(essences: List<Essence>)
    suspend fun updateEssence(essence: Essence)
    suspend fun deleteEssence(code: String)
    suspend fun deleteAllEssences()
}
