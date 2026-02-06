package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.CounterDao
import com.forestry.counter.data.local.dao.FormulaDao
import com.forestry.counter.data.local.dao.GroupDao
import com.forestry.counter.data.local.dao.GroupVariableDao
import com.forestry.counter.data.local.entity.GroupEntity
import com.forestry.counter.data.mapper.toCounter
import com.forestry.counter.data.mapper.toCounterEntity
import com.forestry.counter.data.mapper.toFormula
import com.forestry.counter.data.mapper.toFormulaEntity
import com.forestry.counter.data.mapper.toGroup
import com.forestry.counter.data.mapper.toGroupEntity
import com.forestry.counter.domain.model.Group
import com.forestry.counter.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class GroupRepositoryImpl(
    private val groupDao: GroupDao,
    private val counterDao: CounterDao,
    private val formulaDao: FormulaDao,
    private val groupVariableDao: GroupVariableDao
) : GroupRepository {

    override fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().transform { entities ->
            val result = mutableListOf<Group>()
            for (entity in entities) {
                val count = counterDao.getCounterCountByGroup(entity.groupId)
                val total = counterDao.getTotalValueByGroup(entity.groupId) ?: 0.0
                result.add(entity.toGroup(counterCount = count, totalValue = total))
            }
            emit(result)
        }
    }

    override fun getGroupById(groupId: String): Flow<Group?> {
        return groupDao.getGroupByIdFlow(groupId).transform { entity ->
            if (entity == null) {
                emit(null)
            } else {
                val count = counterDao.getCounterCountByGroup(entity.groupId)
                val total = counterDao.getTotalValueByGroup(entity.groupId) ?: 0.0
                emit(entity.toGroup(counterCount = count, totalValue = total))
            }
        }
    }

    override suspend fun insertGroup(group: Group) {
        groupDao.insertGroup(group.toGroupEntity())
    }

    override suspend fun updateGroup(group: Group) {
        groupDao.updateGroup(group.toGroupEntity())
    }

    override suspend fun deleteGroup(groupId: String) {
        groupDao.deleteGroupById(groupId)
    }

    override suspend fun deleteAllGroups() {
        groupDao.deleteAllGroups()
    }

    override suspend fun duplicateGroup(groupId: String): String {
        val group = groupDao.getGroupById(groupId) ?: return ""
        val counters = counterDao.getCountersByGroup(groupId).first()
        val formulas = formulaDao.getFormulasByGroup(groupId).first()
        val variables = groupVariableDao.getVariablesByGroup(groupId).first()

        val newGroupId = UUID.randomUUID().toString()
        val newGroup = group.copy(
            groupId = newGroupId,
            name = "${group.name} (Copy)",
            sortIndex = (groupDao.getMaxSortIndex() ?: 0) + 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        groupDao.insertGroup(newGroup)

        // Duplicate counters
        val newCounters = counters.map { counter ->
            counter.copy(
                counterId = UUID.randomUUID().toString(),
                groupOwnerId = newGroupId,
                value = 0.0,
                createdAt = System.currentTimeMillis(),
                lastUpdatedAt = System.currentTimeMillis()
            )
        }
        if (newCounters.isNotEmpty()) counterDao.insertCounters(newCounters)

        // Duplicate formulas
        val newFormulas = formulas.map { formula ->
            formula.copy(
                formulaId = UUID.randomUUID().toString(),
                groupOwnerId = newGroupId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
        if (newFormulas.isNotEmpty()) formulaDao.insertFormulas(newFormulas)

        // Duplicate variables
        val newVariables = variables.map { variable ->
            variable.copy(
                variableId = UUID.randomUUID().toString(),
                groupOwnerId = newGroupId
            )
        }
        if (newVariables.isNotEmpty()) groupVariableDao.insertVariables(newVariables)

        return newGroupId
    }
}
