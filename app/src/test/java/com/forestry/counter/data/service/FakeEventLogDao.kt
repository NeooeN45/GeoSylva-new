package com.forestry.counter.data.service

import com.forestry.counter.data.local.dao.EventLogDao
import com.forestry.counter.data.local.entity.EventLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake DAO pour tester EventLogger sans base Room.
 * Stocke les événements en mémoire.
 */
class FakeEventLogDao : EventLogDao {
    val inserted = mutableListOf<EventLogEntity>()

    override suspend fun insert(event: EventLogEntity) {
        inserted.add(event)
    }

    override fun getRecent(limit: Int): Flow<List<EventLogEntity>> = flowOf(inserted.takeLast(limit))

    override fun getByEntity(entityType: String, entityId: String): Flow<List<EventLogEntity>> =
        flowOf(inserted.filter { it.entityType == entityType && it.entityId == entityId })

    override fun getByEventType(eventType: String, limit: Int): Flow<List<EventLogEntity>> =
        flowOf(inserted.filter { it.eventType == eventType }.takeLast(limit))

    override suspend fun getUnsynced(limit: Int): List<EventLogEntity> =
        inserted.filter { !it.synced }.take(limit)

    override suspend fun markSynced(eventId: String, syncedAt: Long) {
        val idx = inserted.indexOfFirst { it.eventId == eventId }
        if (idx >= 0) inserted[idx] = inserted[idx].copy(synced = true, syncedAt = syncedAt)
    }

    override suspend fun countUnsynced(): Int = inserted.count { !it.synced }

    override suspend fun hardDeleteAll() { inserted.clear() }
}
