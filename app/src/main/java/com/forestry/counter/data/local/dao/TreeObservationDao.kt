package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.TreeObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeObservationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: TreeObservationEntity)

    @Update
    suspend fun update(observation: TreeObservationEntity)

    @Query("UPDATE observations SET deletedAt = :timestamp WHERE observationId = :id")
    suspend fun delete(id: String, timestamp: Long)

    @Query("DELETE FROM observations")
    suspend fun hardDeleteAll()

    @Query("SELECT * FROM observations WHERE deletedAt IS NULL ORDER BY observedAt DESC")
    fun getAll(): Flow<List<TreeObservationEntity>>

    @Query("SELECT * FROM observations WHERE observationId = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): TreeObservationEntity?

    @Query("SELECT * FROM observations WHERE uuid = :uuid AND deletedAt IS NULL")
    suspend fun getByUuid(uuid: String): TreeObservationEntity?

    @Query("SELECT * FROM observations WHERE treeId = :treeId AND deletedAt IS NULL ORDER BY observedAt DESC")
    fun getByTree(treeId: String): Flow<List<TreeObservationEntity>>

    @Query("SELECT * FROM observations WHERE parcelleOwnerId = :parcelleId AND deletedAt IS NULL ORDER BY observedAt DESC")
    fun getByParcelle(parcelleId: String): Flow<List<TreeObservationEntity>>

    @Query("SELECT * FROM observations WHERE placetteOwnerId = :placetteId AND deletedAt IS NULL ORDER BY observedAt DESC")
    fun getByPlacette(placetteId: String): Flow<List<TreeObservationEntity>>

    @Query("SELECT * FROM observations WHERE protocol = :protocol AND deletedAt IS NULL ORDER BY observedAt DESC")
    fun getByProtocol(protocol: String): Flow<List<TreeObservationEntity>>

    @Query("SELECT * FROM observations WHERE uuid IS NULL AND deletedAt IS NULL")
    suspend fun getWithoutUuid(): List<TreeObservationEntity>

    @Query("UPDATE observations SET uuid = :uuid WHERE observationId = :id")
    suspend fun setUuid(id: String, uuid: String)
}
