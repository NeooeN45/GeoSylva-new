package com.forestry.counter.data.service

import android.util.Log
import com.forestry.counter.data.local.dao.EventLogDao
import com.forestry.counter.data.local.entity.EventLogEntity
import java.util.UUID

/**
 * Service d'émission d'événements — spec GEOSYLVA-003 §7.6 + Lot 5.
 *
 * Journalise toute mutation (création, modification, suppression, sync,
 * conflit) dans la table event_log. Append-only.
 *
 * Le journal est la base de la synchronisation GSIE (Lot 5) : les
 * événements non synchronisés (`synced = 0`) sont poussés vers le serveur.
 *
 * Initialisé manuellement dans ForestryCounterApplication (pas de Hilt).
 */
class EventLogger(
    private val eventLogDao: EventLogDao
) {
    object EventType {
        const val CREATE = "create"
        const val UPDATE = "update"
        const val DELETE = "delete"
        const val SOFT_DELETE = "soft_delete"
        const val SYNC_PUSH = "sync_push"
        const val SYNC_PULL = "sync_pull"
        const val CONFLICT = "conflict"
        const val MERGE = "merge"
    }

    object EntityType {
        const val FOREST = "forest"
        const val PARCELLE = "parcelle"
        const val PLACETTE = "placette"
        const val TREE = "tree"
        const val OBSERVATION = "observation"
        const val MEASUREMENT = "measurement"
        const val CALCULATION = "calculation"
        const val EVIDENCE = "evidence"
        const val PROJECT = "project"
    }

    companion object {
        private const val TAG = "EventLogger"
    }

    /**
     * Émet un événement dans le journal.
     *
     * @param eventType Type d'événement (cf. EventType).
     * @param entityType Type d'entité cible (cf. EntityType).
     * @param entityId ID de l'entité cible (PK existante).
     * @param entityUuid UUID de l'entité si disponible (interop GSIE).
     * @param payload Payload JSON (diff, conflit, etc.) — null si aucun.
     * @param actor Auteur de l'action (utilisateur ou système) — null si inconnu.
     */
    suspend fun log(
        eventType: String,
        entityType: String,
        entityId: String,
        entityUuid: String? = null,
        payload: String? = null,
        actor: String? = null
    ) {
        val event = EventLogEntity(
            eventId = UUID.randomUUID().toString(),
            eventType = eventType,
            entityType = entityType,
            entityId = entityId,
            entityUuid = entityUuid,
            payload = payload,
            actor = actor
        )
        try {
            eventLogDao.insert(event)
        } catch (e: Exception) {
            // Le journal ne doit jamais bloquer l'opération métier
            Log.w(TAG, "Émission événement échouée (non bloquant): ${e.message}")
        }
    }

    /** Raccourci pour un événement de création. */
    suspend fun logCreate(entityType: String, entityId: String, entityUuid: String? = null, actor: String? = null) =
        log(EventType.CREATE, entityType, entityId, entityUuid, actor = actor)

    /** Raccourci pour un événement de mise à jour. */
    suspend fun logUpdate(entityType: String, entityId: String, entityUuid: String? = null, payload: String? = null, actor: String? = null) =
        log(EventType.UPDATE, entityType, entityId, entityUuid, payload, actor)

    /** Raccourci pour un soft delete. */
    suspend fun logSoftDelete(entityType: String, entityId: String, entityUuid: String? = null, actor: String? = null) =
        log(EventType.SOFT_DELETE, entityType, entityId, entityUuid, actor = actor)

    /** Nombre d'événements en attente de synchronisation. */
    suspend fun countUnsynced(): Int = eventLogDao.countUnsynced()
}
