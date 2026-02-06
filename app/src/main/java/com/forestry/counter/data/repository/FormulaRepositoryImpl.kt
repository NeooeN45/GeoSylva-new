package com.forestry.counter.data.repository

import com.forestry.counter.data.local.dao.CounterDao
import com.forestry.counter.data.local.dao.FormulaDao
import com.forestry.counter.data.local.dao.GroupVariableDao
import com.forestry.counter.data.mapper.toCounter
import com.forestry.counter.data.mapper.toFormula
import com.forestry.counter.data.mapper.toFormulaEntity
import com.forestry.counter.domain.calculator.FormulaParser
import com.forestry.counter.domain.model.Formula
import com.forestry.counter.domain.repository.FormulaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FormulaRepositoryImpl(
    private val formulaDao: FormulaDao,
    private val counterDao: CounterDao,
    private val groupVariableDao: GroupVariableDao,
    private val formulaParser: FormulaParser
) : FormulaRepository {

    override fun getFormulasByGroup(groupId: String): Flow<List<Formula>> {
        return formulaDao.getFormulasByGroup(groupId).map { entities ->
            entities.map { it.toFormula() }
        }
    }

    override fun getFormulaById(formulaId: String): Flow<Formula?> {
        return formulaDao.getFormulaByIdFlow(formulaId).map { it?.toFormula() }
    }

    override suspend fun insertFormula(formula: Formula) {
        formulaDao.insertFormula(formula.toFormulaEntity())
    }

    override suspend fun updateFormula(formula: Formula) {
        formulaDao.updateFormula(formula.toFormulaEntity())
    }

    override suspend fun deleteFormula(formulaId: String) {
        formulaDao.deleteFormulaById(formulaId)
    }

    override suspend fun evaluateFormula(formulaId: String): Double? {
        val formula = formulaDao.getFormulaById(formulaId) ?: return null
        val counters = counterDao.getCountersByGroup(formula.groupOwnerId).first()
            .filter { !it.isComputed }
            .map { it.toCounter() }
        
        val variables = groupVariableDao.getVariablesByGroup(formula.groupOwnerId).first()
            .associate { it.name to it.value }

        val result = formulaParser.evaluate(
            expression = formula.expression,
            counters = counters,
            variables = variables
        )

        return when (result) {
            is FormulaParser.ParseResult.Success -> result.value
            is FormulaParser.ParseResult.Error -> null
        }
    }
}
