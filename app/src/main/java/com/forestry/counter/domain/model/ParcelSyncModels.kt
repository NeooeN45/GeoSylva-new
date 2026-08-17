package com.forestry.counter.domain.model

data class ParcelSyncSummary(
    val pending: Int = 0,
    val syncing: Int = 0,
    val synced: Int = 0,
    val conflicts: Int = 0,
    val errors: Int = 0,
    val lastSuccessAt: Long? = null,
) {
    val hasWork: Boolean
        get() = pending > 0 || syncing > 0
}

data class ParcelSyncProcessResult(
    val synchronized: Int,
    val conflicts: Int,
    val errors: Int,
    val shouldRetry: Boolean,
)

/**
 * Résultat d'un pull (serveur → local) — GEOSYLVA P0-3, 2e moitié.
 *
 * [skippedLocalDirty] compte les parcelles ignorées par le pull car elles ont
 * une modification locale non encore synchronisée (PENDING/SYNCING/CONFLICT/
 * ERROR dans la file d'attente) — le local gagne tant qu'il n'a pas été
 * poussé avec succès, jamais écrasé silencieusement par le serveur.
 */
data class ParcelSyncPullResult(
    val inserted: Int = 0,
    val updated: Int = 0,
    val deleted: Int = 0,
    val skippedLocalDirty: Int = 0,
    val pagesFetched: Int = 0,
)
