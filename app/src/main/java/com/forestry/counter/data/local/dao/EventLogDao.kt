package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.forestry.counter.data.local.entity.EventLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventLogEntity)

    @Query("SELECT * FROM event_log ORDER BY occurredAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 100): Flow<List<EventLogEntity>>

    @Query("SELECT * FROM event_log WHERE entityType = :entityType AND entityId = :entityId ORDER BY occurredAt DESC")
    fun getByEntity(entityType: String, entityId: String): Flow<List<EventLogEntity>>

    @Query("SELECT * FROM event_log WHERE eventType = :eventType ORDER BY occurredAt DESC LIMIT :limit")
    fun getByEventType(eventType: String, limit: Int = 100): Flow<List<EventLogEntity>>

    @Query("SELECT * FROM event_log WHERE synced = 0 ORDER BY occurredAt ASC LIMIT :limit")
    suspend fun getUnsynced(limit: Int = 100): List<EventLogEntity>

    @Query("UPDATE event_log SET synced = 1, syncedAt = :syncedAt WHERE eventId = :eventId")
    suspend fun markSynced(eventId: String, syncedAt: Long)

    @Query("SELECT COUNT(*) FROM event_log WHERE synced = 0")
    suspend fun countUnsynced(): Int

    @Query("DELETE FROM event_log")
    suspend fun hardDeleteAll()
}
