package com.forestry.counter.data.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.forestry.counter.ForestryCounterApplication
import java.util.UUID

/**
 * Backfill asynchrone des UUID sur les entités cœur existantes.
 *
 * Spec GEOSYLVA-003 §7.6 + Lot 1 Sprint 2.3 : après la migration 34→35
 * qui ajoute la colonne `uuid` (nullable) sur forets, parcelles, placettes
 * et tiges, ce worker génère un UUID RFC 4122 pour chaque ligne qui n'en
 * a pas encore.
 *
 * Stratégie : `legacy_id` + backfill asynchrone (décision Fondateur).
 * L'ID existant est conservé comme PK ; l'UUID est ajouté pour l'interop
 * GSIE serveur (Lot 5).
 *
 * Le worker est idempotent : il ne génère un UUID que pour les lignes
 * où `uuid IS NULL`. Il peut être relancé sans risque.
 */
class UuidBackfillWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? ForestryCounterApplication
            ?: return Result.failure(workDataOf(KEY_ERROR to "Application container unavailable"))

        val database = app.database
            ?: return Result.failure(workDataOf(KEY_ERROR to "Database unavailable"))

        return try {
            var totalBackfilled = 0

            // Forets
            val forets = database.foretDao().getWithoutUuid()
            forets.forEach { entity ->
                database.foretDao().setUuid(entity.foretId, UUID.randomUUID().toString())
                totalBackfilled++
            }

            // Parcelles
            val parcelles = database.parcelleDao().getWithoutUuid()
            parcelles.forEach { entity ->
                database.parcelleDao().setUuid(entity.parcelleId, UUID.randomUUID().toString())
                totalBackfilled++
            }

            // Placettes
            val placettes = database.placetteDao().getWithoutUuid()
            placettes.forEach { entity ->
                database.placetteDao().setUuid(entity.placetteId, UUID.randomUUID().toString())
                totalBackfilled++
            }

            // Tiges
            val tiges = database.tigeDao().getWithoutUuid()
            tiges.forEach { entity ->
                database.tigeDao().setUuid(entity.tigeId, UUID.randomUUID().toString())
                totalBackfilled++
            }

            Log.i(TAG, "Backfill UUID terminé : $totalBackfilled entités traitées")
            Result.success(workDataOf(KEY_BACKFILLED_COUNT to totalBackfilled))
        } catch (error: Exception) {
            Log.e(TAG, "Backfill UUID échoué", error)
            if (runAttemptCount + 1 >= MAX_ATTEMPTS) {
                Result.failure(workDataOf(KEY_ERROR to (error.message ?: "Unexpected error")))
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        private const val TAG = "UuidBackfillWorker"
        private const val MAX_ATTEMPTS = 3

        const val KEY_BACKFILLED_COUNT = "backfilled_count"
        const val KEY_ERROR = "error"

        /** Nom unique du worker pour le WorkManager (enqueue/cancel). */
        const val UNIQUE_WORK_NAME = "geosylva_uuid_backfill"
    }
}
