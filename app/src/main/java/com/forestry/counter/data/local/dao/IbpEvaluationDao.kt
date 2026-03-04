package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.IbpEvaluationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IbpEvaluationDao {

    @Query("SELECT * FROM ibp_evaluations WHERE placetteId = :placetteId ORDER BY observationDate DESC")
    fun getByPlacette(placetteId: String): Flow<List<IbpEvaluationEntity>>

    @Query("SELECT * FROM ibp_evaluations WHERE parcelleId = :parcelleId ORDER BY observationDate DESC")
    fun getByParcelle(parcelleId: String): Flow<List<IbpEvaluationEntity>>

    @Query("SELECT * FROM ibp_evaluations WHERE id = :id")
    fun getById(id: String): Flow<IbpEvaluationEntity?>

    @Query("SELECT * FROM ibp_evaluations ORDER BY observationDate DESC")
    fun getAll(): Flow<List<IbpEvaluationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: IbpEvaluationEntity)

    @Delete
    suspend fun delete(entity: IbpEvaluationEntity)

    @Query("DELETE FROM ibp_evaluations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM ibp_evaluations WHERE placetteId = :placetteId")
    suspend fun deleteByPlacette(placetteId: String)
}
