package com.forestry.counter.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.forestry.counter.ForestryCounterApplication

class ParcelSyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? ForestryCounterApplication
            ?: return Result.failure(workDataOf(KEY_ERROR to "APPLICATION_UNAVAILABLE"))
        val result = app.parcelleSyncRepository.processPending()
        return when {
            result.shouldRetry -> Result.retry()
            else -> Result.success(
                workDataOf(
                    KEY_SYNCED_COUNT to result.synchronized,
                    KEY_CONFLICT_COUNT to result.conflicts,
                    KEY_ERROR_COUNT to result.errors,
                )
            )
        }
    }

    companion object {
        const val KEY_ERROR = "error"
        const val KEY_SYNCED_COUNT = "synced_count"
        const val KEY_CONFLICT_COUNT = "conflict_count"
        const val KEY_ERROR_COUNT = "error_count"
    }
}
