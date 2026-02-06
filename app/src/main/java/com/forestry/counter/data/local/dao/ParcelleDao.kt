package com.forestry.counter.data.local.dao

import androidx.room.*
import com.forestry.counter.data.local.entity.ParcelleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelleDao {
    @Query("SELECT * FROM parcelles ORDER BY name ASC")
    fun getAllParcelles(): Flow<List<ParcelleEntity>>

    @Query("SELECT * FROM parcelles WHERE forestOwnerId = :forestId ORDER BY name ASC")
    fun getParcellesByForest(forestId: String): Flow<List<ParcelleEntity>>

    @Query("SELECT * FROM parcelles WHERE parcelleId = :id")
    fun getParcelleByIdFlow(id: String): Flow<ParcelleEntity?>

    @Query("SELECT * FROM parcelles WHERE parcelleId = :id")
    suspend fun getParcelleById(id: String): ParcelleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcelle(entity: ParcelleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcelles(entities: List<ParcelleEntity>)

    @Update
    suspend fun updateParcelle(entity: ParcelleEntity)

    @Delete
    suspend fun deleteParcelle(entity: ParcelleEntity)

    @Query("DELETE FROM parcelles WHERE parcelleId = :id")
    suspend fun deleteParcelleById(id: String)

    @Query("DELETE FROM parcelles")
    suspend fun deleteAllParcelles()
}
