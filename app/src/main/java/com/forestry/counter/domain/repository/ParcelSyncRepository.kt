package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.ParcelSyncProcessResult
import com.forestry.counter.domain.model.ParcelSyncPullResult
import com.forestry.counter.domain.model.ParcelSyncSummary
import kotlinx.coroutines.flow.Flow

interface ParcelSyncRepository {
    fun observeSummary(): Flow<ParcelSyncSummary>

    suspend fun enqueueAll(): Result<Int>

    suspend fun enqueueUpsert(parcelleId: String)

    suspend fun enqueueDelete(parcelleId: String)

    suspend fun processPending(): ParcelSyncProcessResult

    /**
     * Récupère les parcelles depuis le serveur GSIE et les fusionne en local
     * (GEOSYLVA P0-3, 2e moitié — pull et résolution).
     *
     * Une parcelle avec une modification locale non synchronisée n'est
     * jamais écrasée : le local gagne jusqu'à ce que sa modification soit
     * poussée avec succès (voir [ParcelSyncPullResult.skippedLocalDirty]).
     */
    suspend fun pull(): Result<ParcelSyncPullResult>
}
