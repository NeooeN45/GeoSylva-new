package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.ParameterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParameterDao {
    @Query("SELECT * FROM parameters")
    fun getAllParameters(): Flow<List<ParameterEntity>>

    @Query("SELECT * FROM parameters WHERE key = :key")
    fun getParameterFlow(key: String): Flow<ParameterEntity?>

    @Query("SELECT * FROM parameters WHERE key = :key")
    suspend fun getParameter(key: String): ParameterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameter(entity: ParameterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameters(entities: List<ParameterEntity>)

    @Update
    suspend fun updateParameter(entity: ParameterEntity)

    @Delete
    suspend fun deleteParameter(entity: ParameterEntity)

    @Query("DELETE FROM parameters WHERE key = :key")
    suspend fun deleteParameterByKey(key: String)
}
