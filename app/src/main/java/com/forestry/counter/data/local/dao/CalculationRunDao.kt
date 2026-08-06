package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.CalculationRunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationRunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: CalculationRunEntity)

    @Update
    suspend fun update(run: CalculationRunEntity)

    @Query("UPDATE calculation_runs SET deletedAt = :timestamp WHERE runId = :id")
    suspend fun delete(id: String, timestamp: Long)

    @Query("DELETE FROM calculation_runs")
    suspend fun hardDeleteAll()

    @Query("SELECT * FROM calculation_runs WHERE deletedAt IS NULL ORDER BY calculatedAt DESC")
    fun getAll(): Flow<List<CalculationRunEntity>>

    @Query("SELECT * FROM calculation_runs WHERE runId = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): CalculationRunEntity?

    @Query("SELECT * FROM calculation_runs WHERE observationId = :observationId AND deletedAt IS NULL ORDER BY calculatedAt DESC")
    fun getByObservation(observationId: String): Flow<List<CalculationRunEntity>>

    @Query("SELECT * FROM calculation_runs WHERE method = :method AND deletedAt IS NULL ORDER BY calculatedAt DESC")
    fun getByMethod(method: String): Flow<List<CalculationRunEntity>>

    @Query("SELECT * FROM calculation_runs WHERE status = :status AND deletedAt IS NULL ORDER BY calculatedAt DESC")
    fun getByStatus(status: String): Flow<List<CalculationRunEntity>>

    @Query("UPDATE calculation_runs SET status = 'superseded' WHERE runId = :id")
    suspend fun supersede(id: String)
}
