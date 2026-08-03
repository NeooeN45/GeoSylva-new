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
