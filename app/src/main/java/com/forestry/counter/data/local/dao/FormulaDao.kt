package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.FormulaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FormulaDao {
    @Query("SELECT * FROM formulas WHERE groupOwnerId = :groupId ORDER BY name ASC")
    fun getFormulasByGroup(groupId: String): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas WHERE formulaId = :formulaId")
    suspend fun getFormulaById(formulaId: String): FormulaEntity?

    @Query("SELECT * FROM formulas WHERE formulaId = :formulaId")
    fun getFormulaByIdFlow(formulaId: String): Flow<FormulaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: FormulaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormulas(formulas: List<FormulaEntity>)

    @Update
    suspend fun updateFormula(formula: FormulaEntity)

    @Delete
    suspend fun deleteFormula(formula: FormulaEntity)

    @Query("DELETE FROM formulas WHERE formulaId = :formulaId")
    suspend fun deleteFormulaById(formulaId: String)

    @Query("DELETE FROM formulas WHERE groupOwnerId = :groupId")
    suspend fun deleteFormulasByGroup(groupId: String)

    @Query("DELETE FROM formulas")
    suspend fun deleteAllFormulas()
}
