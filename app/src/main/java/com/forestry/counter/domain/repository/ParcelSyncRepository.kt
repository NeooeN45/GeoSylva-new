package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.ParcelSyncProcessResult
import com.forestry.counter.domain.model.ParcelSyncSummary
import kotlinx.coroutines.flow.Flow

interface ParcelSyncRepository {
    fun observeSummary(): Flow<ParcelSyncSummary>

    suspend fun enqueueAll(): Result<Int>

    suspend fun enqueueUpsert(parcelleId: String)

    suspend fun enqueueDelete(parcelleId: String)

    suspend fun processPending(): ParcelSyncProcessResult
}
