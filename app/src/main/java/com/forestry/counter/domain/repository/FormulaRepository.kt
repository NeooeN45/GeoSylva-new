package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.Formula
import kotlinx.coroutines.flow.Flow

interface FormulaRepository {
    fun getFormulasByGroup(groupId: String): Flow<List<Formula>>
    fun getFormulaById(formulaId: String): Flow<Formula?>
    suspend fun insertFormula(formula: Formula)
    suspend fun updateFormula(formula: Formula)
    suspend fun deleteFormula(formulaId: String)
    suspend fun evaluateFormula(formulaId: String): Double?
}
