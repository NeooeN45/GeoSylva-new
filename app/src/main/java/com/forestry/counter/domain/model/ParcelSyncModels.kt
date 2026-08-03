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
