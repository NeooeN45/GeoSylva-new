package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.ParameterItem
import kotlinx.coroutines.flow.Flow

interface ParameterRepository {
    fun getAllParameters(): Flow<List<ParameterItem>>
    fun getParameter(key: String): Flow<ParameterItem?>
    suspend fun setParameter(item: ParameterItem)
    suspend fun setParameters(items: List<ParameterItem>)
    suspend fun deleteParameter(key: String)
}
