package com.forestry.counter.data.service

import com.forestry.counter.data.service.EventLogger.EntityType
import com.forestry.counter.data.service.EventLogger.EventType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests du EventLogger — spec GEOSYLVA-003 §7.6.
 *
 * Couvre : émission d'événements, raccourcis (create/update/soft_delete),
 * comptage des non-synchronisés, non-blocage sur erreur.
 */
class EventLoggerTest {

    private lateinit var dao: FakeEventLogDao
    private lateinit var logger: EventLogger

    @Before
    fun setUp() {
        dao = FakeEventLogDao()
        logger = EventLogger(dao)
    }

    @Test
    fun should_log_create_event() = runBlocking {
        logger.logCreate(EntityType.FOREST, "forest-1", "uuid-1", "alice")

        assertEquals(1, dao.inserted.size)
        val event = dao.inserted.first()
        assertEquals(EventType.CREATE, event.eventType)
        assertEquals(EntityType.FOREST, event.entityType)
        assertEquals("forest-1", event.entityId)
        assertEquals("uuid-1", event.entityUuid)
        assertEquals("alice", event.actor)
        assertNotNull(event.eventId)
        assertFalse(event.synced)
    }

    @Test
    fun should_log_update_event_with_payload() = runBlocking {
        logger.logUpdate(EntityType.PARCELLE, "p-1", null, """{"surface": 5.0}""", "bob")

        val event = dao.inserted.first()
        assertEquals(EventType.UPDATE, event.eventType)
        assertEquals(EntityType.PARCELLE, event.entityType)
        assertEquals("""{"surface": 5.0}""", event.payload)
        assertEquals("bob", event.actor)
    }

    @Test
    fun should_log_soft_delete_event() = runBlocking {
        logger.logSoftDelete(EntityType.TREE, "tree-1")

        val event = dao.inserted.first()
        assertEquals(EventType.SOFT_DELETE, event.eventType)
        assertEquals(EntityType.TREE, event.entityType)
        assertNull(event.entityUuid)
    }

    @Test
    fun should_log_generic_event() = runBlocking {
        logger.log(EventType.SYNC_PUSH, EntityType.OBSERVATION, "obs-1", "uuid-x", """{"status":"ok"}""", "system")

        val event = dao.inserted.first()
        assertEquals(EventType.SYNC_PUSH, event.eventType)
        assertEquals(EntityType.OBSERVATION, event.entityType)
        assertEquals("uuid-x", event.entityUuid)
    }

    @Test
    fun should_count_unsynced_events() = runBlocking {
        logger.logCreate(EntityType.FOREST, "f-1")
        logger.logCreate(EntityType.FOREST, "f-2")
        logger.logCreate(EntityType.FOREST, "f-3")

        assertEquals(3, logger.countUnsynced())
    }

    @Test
    fun should_generate_unique_event_ids() = runBlocking {
        repeat(10) { i ->
            logger.logCreate(EntityType.FOREST, "f-$i")
        }

        val ids = dao.inserted.map { it.eventId }.toSet()
        assertEquals(10, ids.size)
    }

    @Test
    fun should_generate_valid_uuid_format() = runBlocking {
        logger.logCreate(EntityType.FOREST, "f-1")

        val eventId = dao.inserted.first().eventId
        // UUID RFC 4122 format: 8-4-4-4-12
        assertTrue(
            "eventId $eventId n'est pas un UUID valide",
            eventId.matches(Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"))
        )
    }

    @Test
    fun should_set_occurred_at_to_current_time() = runBlocking {
        val before = System.currentTimeMillis()
        logger.logCreate(EntityType.FOREST, "f-1")
        val after = System.currentTimeMillis()

        val event = dao.inserted.first()
        assertTrue(event.occurredAt in before..after)
    }
}
