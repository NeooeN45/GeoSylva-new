package com.forestry.counter.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "parcel_sync_queue",
    primaryKeys = ["accountId", "parcelId"],
    indices = [
        Index(name = "index_parcel_sync_state_next", value = ["accountId", "state", "nextAttemptAt"]),
    ],
)
data class ParcelSyncEntity(
    val accountId: String,
    val parcelId: String,
    val operation: String,
    val operationId: String,
    val state: String,
    val serverVersion: Int?,
    val retryCount: Int,
    val queuedAt: Long,
    val lastAttemptAt: Long?,
    val lastSuccessAt: Long?,
    val nextAttemptAt: Long,
    val lastErrorCode: String?,
)

data class ParcelSyncCounts(
    val pending: Int,
    val syncing: Int,
    val synced: Int,
    val conflicts: Int,
    val errors: Int,
    val lastSuccessAt: Long?,
)
