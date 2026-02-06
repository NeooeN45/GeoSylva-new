package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.ParameterDao
import com.forestry.counter.data.mapper.toParameterEntity
import com.forestry.counter.data.mapper.toParameterItem
import com.forestry.counter.domain.model.ParameterItem
import com.forestry.counter.domain.repository.ParameterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ParameterRepositoryImpl(
    private val parameterDao: ParameterDao
) : ParameterRepository {

    override fun getAllParameters(): Flow<List<ParameterItem>> {
        return parameterDao.getAllParameters().map { list -> list.map { it.toParameterItem() } }
    }

    override fun getParameter(key: String): Flow<ParameterItem?> {
        return parameterDao.getParameterFlow(key).map { it?.toParameterItem() }
    }

    override suspend fun setParameter(item: ParameterItem) {
        parameterDao.insertParameter(item.toParameterEntity())
    }

    override suspend fun setParameters(items: List<ParameterItem>) {
        parameterDao.insertParameters(items.map { it.toParameterEntity() })
    }

    override suspend fun deleteParameter(key: String) {
        parameterDao.deleteParameterByKey(key)
    }
}
