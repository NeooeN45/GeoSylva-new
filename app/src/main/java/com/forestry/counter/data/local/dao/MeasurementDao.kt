package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: MeasurementEntity)

    @Update
    suspend fun update(measurement: MeasurementEntity)

    @Query("UPDATE measurements SET deletedAt = :timestamp WHERE measurementId = :id")
    suspend fun delete(id: String, timestamp: Long)

    @Query("DELETE FROM measurements")
    suspend fun hardDeleteAll()

    @Query("SELECT * FROM measurements WHERE deletedAt IS NULL ORDER BY measuredAt DESC")
    fun getAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE measurementId = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): MeasurementEntity?

    @Query("SELECT * FROM measurements WHERE observationId = :observationId AND deletedAt IS NULL ORDER BY type ASC")
    fun getByObservation(observationId: String): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE type = :type AND deletedAt IS NULL ORDER BY measuredAt DESC")
    fun getByType(type: String): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE uuid IS NULL AND deletedAt IS NULL")
    suspend fun getWithoutUuid(): List<MeasurementEntity>

    @Query("UPDATE measurements SET uuid = :uuid WHERE measurementId = :id")
    suspend fun setUuid(id: String, uuid: String)
}
