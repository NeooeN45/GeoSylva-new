package com.forestry.counter.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Journal d'événements — base du Lot 5 (synchronisation GSIE).
 *
 * Spec GEOSYLVA-003 §7.6 + Lot 5 : toute mutation (création, modification,
 * suppression, sync, conflit) est tracée. Le journal est append-only.
 *
 * L'eventType identifie l'action, l'entityType+entityId désignent la cible,
 * le payload (JSON) contient le détail (diff, conflit, etc.).
 */
@Entity(
    tableName = "event_log",
    indices = [
        Index(name = "index_event_log_entityType_entityId", value = ["entityType", "entityId"]),
        Index(name = "index_event_log_eventType", value = ["eventType"]),
        Index(name = "index_event_log_occurredAt", value = ["occurredAt"]),
        Index(name = "index_event_log_synced", value = ["synced"])
    ]
)
data class EventLogEntity(
    @PrimaryKey
    val eventId: String,
    /** Type d'événement : create, update, delete, soft_delete, sync_push, sync_pull, conflict, merge. */
    val eventType: String,
    /** Type d'entité cible : forest, parcelle, placette, tree, observation, measurement, calculation, evidence. */
    val entityType: String,
    /** ID de l'entité cible (PK existante — pas une FK pour rester générique). */
    val entityId: String,
    /** UUID de l'entité cible si disponible (interop GSIE). */
    val entityUuid: String?,
    /** Payload JSON (diff avant/après, détail conflit, etc.). */
    val payload: String?,
    /** Auteur de l'action (utilisateur ou système). */
    val actor: String?,
    /** Horodatage de l'événement (epoch millis). */
    val occurredAt: Long = System.currentTimeMillis(),
    /** Indique si l'événement a été synchronisé vers GSIE serveur (Lot 5). */
    val synced: Boolean = false,
    /** Horodatage de la synchronisation — null si non synchronisé. */
    val syncedAt: Long? = null,
    @ColumnInfo(name = "version", defaultValue = "1")
    val version: Int = 1
)
