package com.forestry.counter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.forestry.counter.data.local.entity.ParcelSyncCounts
import com.forestry.counter.data.local.entity.ParcelSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelSyncDao {
    @Query(
        """
        SELECT
            CAST(COALESCE(SUM(CASE WHEN state = 'PENDING' THEN 1 ELSE 0 END), 0) AS INTEGER) AS pending,
            CAST(COALESCE(SUM(CASE WHEN state = 'SYNCING' THEN 1 ELSE 0 END), 0) AS INTEGER) AS syncing,
            CAST(COALESCE(SUM(CASE WHEN state = 'SYNCED' THEN 1 ELSE 0 END), 0) AS INTEGER) AS synced,
            CAST(COALESCE(SUM(CASE WHEN state = 'CONFLICT' THEN 1 ELSE 0 END), 0) AS INTEGER) AS conflicts,
            CAST(COALESCE(SUM(CASE WHEN state = 'ERROR' THEN 1 ELSE 0 END), 0) AS INTEGER) AS errors,
            MAX(lastSuccessAt) AS lastSuccessAt
        FROM parcel_sync_queue
        WHERE accountId = :accountId
        """,
    )
    fun observeCounts(accountId: String): Flow<ParcelSyncCounts>

    @Query(
        """
        SELECT * FROM parcel_sync_queue
        WHERE accountId = :accountId
          AND (
            (state IN ('PENDING', 'ERROR') AND nextAttemptAt <= :now)
            OR (state = 'SYNCING' AND lastAttemptAt <= :staleBefore)
          )
        ORDER BY queuedAt ASC
        LIMIT :limit
        """,
    )
    suspend fun getReady(
        accountId: String,
        now: Long,
        staleBefore: Long,
        limit: Int,
    ): List<ParcelSyncEntity>

    @Query("SELECT * FROM parcel_sync_queue WHERE accountId = :accountId AND parcelId = :parcelId")
    suspend fun get(accountId: String, parcelId: String): ParcelSyncEntity?

    @Query(
        """
        UPDATE parcel_sync_queue
        SET state = 'SYNCING', lastAttemptAt = :attemptedAt
        WHERE accountId = :accountId
          AND parcelId = :parcelId
          AND operationId = :operationId
          AND state = :expectedState
          AND (
            (lastAttemptAt IS NULL AND :expectedLastAttemptAt IS NULL)
            OR lastAttemptAt = :expectedLastAttemptAt
          )
        """,
    )
    suspend fun claimIfCurrent(
        accountId: String,
        parcelId: String,
        operationId: String,
        expectedState: String,
        expectedLastAttemptAt: Long?,
        attemptedAt: Long,
    ): Int

    @Query(
        """
        UPDATE parcel_sync_queue
        SET serverVersion = :serverVersion,
            lastSuccessAt = :completedAt,
            state = CASE WHEN operationId = :operationId THEN 'SYNCED' ELSE state END,
            retryCount = CASE WHEN operationId = :operationId THEN 0 ELSE retryCount END,
            nextAttemptAt = CASE WHEN operationId = :operationId THEN :completedAt ELSE nextAttemptAt END,
            lastErrorCode = CASE WHEN operationId = :operationId THEN NULL ELSE lastErrorCode END
        WHERE accountId = :accountId AND parcelId = :parcelId
        """,
    )
    suspend fun recordSuccess(
        accountId: String,
        parcelId: String,
        operationId: String,
        serverVersion: Int,
        completedAt: Long,
    ): Int

    @Query(
        """
        UPDATE parcel_sync_queue
        SET state = :state,
            retryCount = :retryCount,
            nextAttemptAt = :nextAttemptAt,
            lastErrorCode = :errorCode
        WHERE accountId = :accountId
          AND parcelId = :parcelId
          AND operationId = :operationId
        """,
    )
    suspend fun recordFailureIfCurrent(
        accountId: String,
        parcelId: String,
        operationId: String,
        state: String,
        retryCount: Int,
        nextAttemptAt: Long,
        errorCode: String,
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ParcelSyncEntity)
}
