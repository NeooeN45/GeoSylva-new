package com.forestry.counter.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.forestry.counter.data.local.dao.ParcelleDao
import com.forestry.counter.data.local.dao.ParcelSyncDao
import com.forestry.counter.data.local.entity.ParcelSyncEntity
import com.forestry.counter.data.remote.identity.EncryptedIdentitySessionStore
import com.forestry.counter.data.work.ParcelSyncWorker
import com.forestry.counter.domain.model.ParcelSyncProcessResult
import com.forestry.counter.domain.model.ParcelSyncPullResult
import com.forestry.counter.domain.model.ParcelSyncSummary
import com.forestry.counter.domain.repository.IdentityRepository
import com.forestry.counter.domain.repository.ParcelSyncRepository
import java.io.IOException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
internal class ParcelSyncRepositoryImpl(
    context: Context,
    private val syncDao: ParcelSyncDao,
    private val parcelleDao: ParcelleDao,
    private val identityRepository: IdentityRepository,
    private val api: ParcelSyncApiService?,
    private val sessionStore: EncryptedIdentitySessionStore,
    private val activationStore: ParcelSyncActivationStore,
) : ParcelSyncRepository {
    private val appContext = context.applicationContext

    override fun observeSummary(): Flow<ParcelSyncSummary> =
        identityRepository.session.flatMapLatest { session ->
            if (session == null) {
                flowOf(ParcelSyncSummary())
            } else {
                syncDao.observeCounts(session.accountId).map { counts ->
                    ParcelSyncSummary(
                        pending = counts.pending,
                        syncing = counts.syncing,
                        synced = counts.synced,
                        conflicts = counts.conflicts,
                        errors = counts.errors,
                        lastSuccessAt = counts.lastSuccessAt,
                    )
                }
            }
        }

    override suspend fun enqueueAll(): Result<Int> {
        val accountId = identityRepository.session.value?.accountId
            ?: return Result.failure(IllegalStateException("Aucun compte connecté"))
        return runCatching {
            check(activationStore.enable(accountId)) {
                "Le stockage chiffré de l'activation est indisponible"
            }
            val parcels = parcelleDao.getAllParcellesNow()
            parcels.forEach { parcel -> enqueue(accountId, parcel.parcelleId, OPERATION_UPSERT) }
            if (parcels.isNotEmpty()) schedule()
            parcels.size
        }
    }

    override suspend fun enqueueUpsert(parcelleId: String) {
        val accountId = identityRepository.session.value?.accountId ?: return
        if (!activationStore.isEnabled(accountId)) return
        enqueue(accountId, parcelleId, OPERATION_UPSERT)
        schedule()
    }

    override suspend fun enqueueDelete(parcelleId: String) {
        val accountId = identityRepository.session.value?.accountId ?: return
        if (!activationStore.isEnabled(accountId)) return
        enqueue(accountId, parcelleId, OPERATION_DELETE)
        schedule()
    }

    private suspend fun enqueue(accountId: String, parcelleId: String, operation: String) {
        val existing = syncDao.get(accountId, parcelleId)
        val now = System.currentTimeMillis()
        syncDao.upsert(
            ParcelSyncEntity(
                accountId = accountId,
                parcelId = parcelleId,
                operation = operation,
                operationId = UUID.randomUUID().toString(),
                state = STATE_PENDING,
                serverVersion = existing?.serverVersion,
                retryCount = 0,
                queuedAt = now,
                lastAttemptAt = existing?.lastAttemptAt,
                lastSuccessAt = existing?.lastSuccessAt,
                nextAttemptAt = now,
                lastErrorCode = null,
            )
        )
    }

    override suspend fun processPending(): ParcelSyncProcessResult {
        val session = identityRepository.session.value ?: return EMPTY_RESULT
        val service = api ?: return failReadyRows(session.accountId, ERROR_API_NOT_CONFIGURED)
        val now = System.currentTimeMillis()
        val ready = syncDao.getReady(
            accountId = session.accountId,
            now = now,
            staleBefore = now - STALE_SYNC_DELAY_MS,
            limit = BATCH_SIZE,
        )
        var synchronized = 0
        var conflicts = 0
        var errors = 0
        var shouldRetry = false
        ready.forEach { queued ->
            val claimed = syncDao.claimIfCurrent(
                accountId = queued.accountId,
                parcelId = queued.parcelId,
                operationId = queued.operationId,
                expectedState = queued.state,
                expectedLastAttemptAt = queued.lastAttemptAt,
                attemptedAt = System.currentTimeMillis(),
            )
            if (claimed == 0) return@forEach
            when (synchronize(service, queued)) {
                SyncItemResult.SUCCESS -> synchronized += 1
                SyncItemResult.CONFLICT -> conflicts += 1
                SyncItemResult.RETRY -> {
                    errors += 1
                    shouldRetry = true
                }
                SyncItemResult.ERROR -> errors += 1
            }
        }
        if (ready.size == BATCH_SIZE) schedule()
        return ParcelSyncProcessResult(synchronized, conflicts, errors, shouldRetry)
    }

    override suspend fun pull(): Result<ParcelSyncPullResult> {
        val accountId = identityRepository.session.value?.accountId
            ?: return Result.failure(IllegalStateException("Aucun compte connecté"))
        if (!activationStore.isEnabled(accountId)) {
            return Result.failure(IllegalStateException("Synchronisation non activée"))
        }
        val service = api ?: return Result.failure(IllegalStateException(ERROR_API_NOT_CONFIGURED))
        return runCatching {
            var page = 1
            var pagesFetched = 0
            var inserted = 0
            var updated = 0
            var deleted = 0
            var skippedLocalDirty = 0
            while (pagesFetched < MAX_PULL_PAGES) {
                val body = fetchPullPage(service, page)
                pagesFetched += 1
                body.items.forEach { item ->
                    when (mergeFromServer(accountId, item)) {
                        MergeOutcome.INSERTED -> inserted += 1
                        MergeOutcome.UPDATED -> updated += 1
                        MergeOutcome.DELETED -> deleted += 1
                        MergeOutcome.SKIPPED_LOCAL_DIRTY -> skippedLocalDirty += 1
                        MergeOutcome.NOOP -> {}
                    }
                }
                val fetchedSoFar = page * body.size
                if (body.items.isEmpty() || fetchedSoFar >= body.total) break
                page += 1
            }
            ParcelSyncPullResult(inserted, updated, deleted, skippedLocalDirty, pagesFetched)
        }
    }

    private suspend fun fetchPullPage(service: ParcelSyncApiService, page: Int): GeoSylvaParcelPageDto {
        var response = service.list(authorizationHeader(), page, PULL_PAGE_SIZE)
        if (response.code() == 401 && identityRepository.refreshSession().isSuccess) {
            response = service.list(authorizationHeader(), page, PULL_PAGE_SIZE)
        }
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code()} lors de la recuperation des parcelles")
        }
        return response.body() ?: throw IOException("Reponse vide lors de la recuperation des parcelles")
    }

    /**
     * Fusionne une parcelle serveur en local — jamais si une modification
     * locale n'a pas encore ete synchronisee avec succes (le local gagne,
     * voir doc [com.forestry.counter.domain.repository.ParcelSyncRepository.pull]).
     */
    private suspend fun mergeFromServer(accountId: String, item: ParcelSyncResponseDto): MergeOutcome {
        val queued = syncDao.get(accountId, item.clientId)
        val existing = parcelleDao.getParcelleByIdAny(item.clientId)
        val outcome = decideMergeOutcome(
            isLocalDirty = queued != null && queued.state != STATE_SYNCED,
            isTombstone = item.status == PARCEL_STATUS_DELETED,
            existsLocally = existing != null,
            isAlreadyDeletedLocally = existing?.deletedAt != null,
        )
        when (outcome) {
            MergeOutcome.SKIPPED_LOCAL_DIRTY -> Unit
            MergeOutcome.NOOP -> recordSynced(accountId, item)
            MergeOutcome.DELETED -> {
                parcelleDao.deleteParcelleById(item.clientId, parseServerTimestamp(item.serverUpdatedAt))
                recordSynced(accountId, item)
            }
            MergeOutcome.INSERTED -> {
                val entity = item.toParcelleEntity(existing) ?: return MergeOutcome.NOOP
                parcelleDao.insertParcelle(entity)
                recordSynced(accountId, item)
            }
            MergeOutcome.UPDATED -> {
                val entity = item.toParcelleEntity(existing) ?: return MergeOutcome.NOOP
                parcelleDao.updateParcelle(entity)
                recordSynced(accountId, item)
            }
        }
        return outcome
    }

    /** Aligne la file d'attente locale sur l'etat serveur connu apres un pull reussi. */
    private suspend fun recordSynced(accountId: String, item: ParcelSyncResponseDto) {
        val now = System.currentTimeMillis()
        syncDao.upsert(
            ParcelSyncEntity(
                accountId = accountId,
                parcelId = item.clientId,
                operation = OPERATION_UPSERT,
                operationId = UUID.randomUUID().toString(),
                state = STATE_SYNCED,
                serverVersion = item.serverVersion,
                retryCount = 0,
                queuedAt = now,
                lastAttemptAt = now,
                lastSuccessAt = now,
                nextAttemptAt = now,
                lastErrorCode = null,
            )
        )
    }

    private fun parseServerTimestamp(serverUpdatedAt: String?): Long =
        serverUpdatedAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
            ?: System.currentTimeMillis()

    private suspend fun synchronize(
        service: ParcelSyncApiService,
        queued: ParcelSyncEntity,
    ): SyncItemResult {
        return try {
            var response = send(service, queued, authorizationHeader())
            if (response.code() == 401 && identityRepository.refreshSession().isSuccess) {
                response = send(service, queued, authorizationHeader())
            }
            handleResponse(queued, response)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            markRetry(queued, "NETWORK_UNAVAILABLE")
            SyncItemResult.RETRY
        } catch (_: Exception) {
            markPermanentError(queued, "INVALID_SERVER_RESPONSE")
            SyncItemResult.ERROR
        }
    }

    private suspend fun send(
        service: ParcelSyncApiService,
        queued: ParcelSyncEntity,
        authorization: String,
    ): Response<ParcelSyncResponseDto> {
        val timestamp = Instant.ofEpochMilli(queued.queuedAt).toString()
        return if (queued.operation == OPERATION_DELETE) {
            service.delete(
                authorization,
                queued.parcelId,
                ParcelDeleteRequestDto(
                    operationId = queued.operationId,
                    baseVersion = queued.serverVersion,
                    clientUpdatedAt = timestamp,
                ),
            )
        } else {
            val parcel = parcelleDao.getParcelleById(queued.parcelId)
            if (parcel == null) {
                service.delete(
                    authorization,
                    queued.parcelId,
                    ParcelDeleteRequestDto(
                        operationId = queued.operationId,
                        baseVersion = queued.serverVersion,
                        clientUpdatedAt = timestamp,
                    ),
                )
            } else {
                service.upsert(
                    authorization,
                    queued.parcelId,
                    ParcelUpsertRequestDto(
                        operationId = queued.operationId,
                        baseVersion = queued.serverVersion,
                        clientUpdatedAt = Instant.ofEpochMilli(parcel.updatedAt).toString(),
                        parcel = parcel.toSyncPayload(),
                    ),
                )
            }
        }
    }

    private suspend fun handleResponse(
        queued: ParcelSyncEntity,
        response: Response<ParcelSyncResponseDto>,
    ): SyncItemResult {
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) {
                markPermanentError(queued, "EMPTY_RESPONSE")
                return SyncItemResult.ERROR
            }
            val now = System.currentTimeMillis()
            syncDao.recordSuccess(
                accountId = queued.accountId,
                parcelId = queued.parcelId,
                operationId = queued.operationId,
                serverVersion = body.serverVersion,
                completedAt = now,
            )
            return SyncItemResult.SUCCESS
        }
        return when (classifySyncHttpFailure(response.code())) {
            SyncFailureAction.CONFLICT -> {
                recordFailure(queued, STATE_CONFLICT, queued.retryCount, Long.MAX_VALUE, "SYNC_VERSION_CONFLICT")
                SyncItemResult.CONFLICT
            }
            SyncFailureAction.RETRY -> {
                markRetry(queued, "HTTP_${response.code()}")
                SyncItemResult.RETRY
            }
            SyncFailureAction.REFRESH_SESSION,
            SyncFailureAction.PERMANENT_ERROR,
            -> {
                markPermanentError(queued, "HTTP_${response.code()}")
                SyncItemResult.ERROR
            }
        }
    }

    private suspend fun markRetry(queued: ParcelSyncEntity, code: String) {
        val retryCount = queued.retryCount + 1
        val delay = (MIN_RETRY_DELAY_MS * (1L shl retryCount.coerceAtMost(6)))
            .coerceAtMost(MAX_RETRY_DELAY_MS)
        recordFailure(
            queued,
            STATE_ERROR,
            retryCount,
            System.currentTimeMillis() + delay,
            code,
        )
    }

    private suspend fun markPermanentError(queued: ParcelSyncEntity, code: String) {
        recordFailure(
            queued,
            STATE_ERROR,
            queued.retryCount,
            Long.MAX_VALUE,
            code,
        )
    }

    private suspend fun recordFailure(
        queued: ParcelSyncEntity,
        state: String,
        retryCount: Int,
        nextAttemptAt: Long,
        code: String,
    ) {
        syncDao.recordFailureIfCurrent(
            accountId = queued.accountId,
            parcelId = queued.parcelId,
            operationId = queued.operationId,
            state = state,
            retryCount = retryCount,
            nextAttemptAt = nextAttemptAt,
            errorCode = code,
        )
    }

    private suspend fun failReadyRows(accountId: String, code: String): ParcelSyncProcessResult {
        val now = System.currentTimeMillis()
        val ready = syncDao.getReady(
            accountId = accountId,
            now = now,
            staleBefore = now - STALE_SYNC_DELAY_MS,
            limit = BATCH_SIZE,
        )
        ready.forEach { markPermanentError(it, code) }
        return ParcelSyncProcessResult(0, 0, ready.size, false)
    }

    private fun authorizationHeader(): String {
        val token = sessionStore.read()?.accessToken
            ?: throw IllegalStateException("Session chiffrée indisponible")
        return "Bearer $token"
    }

    private fun schedule() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<ParcelSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request,
        )
    }

    private enum class SyncItemResult { SUCCESS, CONFLICT, RETRY, ERROR }

    private companion object {
        const val OPERATION_UPSERT = "UPSERT"
        const val OPERATION_DELETE = "DELETE"
        const val STATE_PENDING = "PENDING"
        const val STATE_SYNCING = "SYNCING"
        const val STATE_SYNCED = "SYNCED"
        const val STATE_CONFLICT = "CONFLICT"
        const val STATE_ERROR = "ERROR"
        const val ERROR_API_NOT_CONFIGURED = "API_NOT_CONFIGURED"
        const val UNIQUE_WORK_NAME = "geosylva-parcel-sync"
        const val BATCH_SIZE = 50
        const val MIN_RETRY_DELAY_MS = 15_000L
        const val MAX_RETRY_DELAY_MS = 60 * 60 * 1_000L
        const val STALE_SYNC_DELAY_MS = 10 * 60 * 1_000L
        const val PULL_PAGE_SIZE = 200
        const val MAX_PULL_PAGES = 200
        val EMPTY_RESULT = ParcelSyncProcessResult(0, 0, 0, false)
    }
}

object ParcelSyncRepositoryFactory {
    fun create(
        context: Context,
        syncDao: ParcelSyncDao,
        parcelleDao: ParcelleDao,
        identityRepository: IdentityRepository,
    ): ParcelSyncRepository = ParcelSyncRepositoryImpl(
        context = context,
        syncDao = syncDao,
        parcelleDao = parcelleDao,
        identityRepository = identityRepository,
        api = ParcelSyncApiFactory.create(context),
        sessionStore = EncryptedIdentitySessionStore(context),
        activationStore = ParcelSyncActivationStore(context),
    )
}
