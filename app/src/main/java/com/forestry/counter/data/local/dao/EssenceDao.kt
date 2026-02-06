package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.EssenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EssenceDao {
    @Query("SELECT * FROM essences ORDER BY name ASC")
    fun getAllEssences(): Flow<List<EssenceEntity>>

    @Query("SELECT * FROM essences WHERE code = :code")
    fun getEssenceByCodeFlow(code: String): Flow<EssenceEntity?>

    @Query("SELECT * FROM essences WHERE code = :code")
    suspend fun getEssenceByCode(code: String): EssenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssence(entity: EssenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEssences(entities: List<EssenceEntity>)

    @Update
    suspend fun updateEssence(entity: EssenceEntity)

    @Delete
    suspend fun deleteEssence(entity: EssenceEntity)

    @Query("DELETE FROM essences WHERE code = :code")
    suspend fun deleteEssenceByCode(code: String)

    @Query("DELETE FROM essences")
    suspend fun deleteAllEssences()
}
