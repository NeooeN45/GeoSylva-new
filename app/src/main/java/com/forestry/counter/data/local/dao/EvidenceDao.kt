package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.forestry.counter.data.local.entity.EvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(evidence: EvidenceEntity)

    @Update
    suspend fun update(evidence: EvidenceEntity)

    @Query("UPDATE evidence SET deletedAt = :timestamp WHERE evidenceId = :id")
    suspend fun delete(id: String, timestamp: Long)

    @Query("DELETE FROM evidence")
    suspend fun hardDeleteAll()

    @Query("SELECT * FROM evidence WHERE deletedAt IS NULL ORDER BY capturedAt DESC")
    fun getAll(): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence WHERE evidenceId = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): EvidenceEntity?

    @Query("SELECT * FROM evidence WHERE observationId = :observationId AND deletedAt IS NULL ORDER BY capturedAt ASC")
    fun getByObservation(observationId: String): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence WHERE measurementId = :measurementId AND deletedAt IS NULL")
    fun getByMeasurement(measurementId: String): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence WHERE sha256 = :sha256 AND deletedAt IS NULL LIMIT 1")
    suspend fun findBySha256(sha256: String): EvidenceEntity?
}
