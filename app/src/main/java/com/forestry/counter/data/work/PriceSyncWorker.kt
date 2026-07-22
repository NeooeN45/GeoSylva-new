package com.forestry.counter.data.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.forestry.counter.ForestryCounterApplication
import com.forestry.counter.data.repository.ParameterRepositoryImpl
import com.forestry.counter.domain.calculation.PriceEntry
import com.forestry.counter.domain.model.ParameterItem
import com.forestry.counter.domain.parameters.ParameterKeys
import com.forestry.counter.network.SecureHttpClient
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.ResponseBody

private const val MAX_PRICE_FEED_RESPONSE_BYTES = 2 * 1024 * 1024

/**
 * Synchronise une grille tarifaire distante sans jamais rouvrir la base SQLCipher en clair.
 *
 * Le flux est limité, décodé strictement et validé avant sa publication atomique dans
 * la table des paramètres. Une réponse invalide ne remplace donc jamais les prix actifs.
 */
class PriceSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as? ForestryCounterApplication
            ?: return@withContext failure("Application container unavailable")
        val repository = ParameterRepositoryImpl(app.database.parameterDao())
        val url = inputData.getString(KEY_URL)
            ?: repository.getParameter(ParameterKeys.PRICE_FEED_URL)
                .firstOrNull()
                ?.valueJson
                ?.trim('"')

        if (url.isNullOrBlank()) return@withContext failure("Price feed URL is missing")
        if (!SecureHttpClient.isSafeRemoteHttpsUrl(url)) {
            return@withContext failure("Price feed URL must target a public HTTPS endpoint")
        }

        try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .get()
                .build()

            SecureHttpClient.createSecureClient(applicationContext)
                .newCall(request)
                .execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        val message = "Price feed returned HTTP ${response.code}"
                        return@withContext if (response.code in TRANSIENT_HTTP_CODES || response.code >= 500) {
                            retryOrFail(message)
                        } else {
                            failure(message)
                        }
                    }

                    val responseBody = response.body
                        ?: return@withContext failure("Price feed response body is empty")
                    val payload = responseBody.readLimitedUtf8()
                    val entries = JSON.decodeFromString<List<PriceEntry>>(payload)
                    if (!validatePriceEntries(entries)) {
                        return@withContext failure("Price feed schema or values are invalid")
                    }

                    repository.setParameter(
                        ParameterItem(ParameterKeys.PRIX_MARCHE, JSON.encodeToString(entries))
                    )
                    Result.success(workDataOf(KEY_ENTRY_COUNT to entries.size))
                }
        } catch (error: SerializationException) {
            Log.w(TAG, "Price feed rejected: malformed JSON", error)
            failure("Price feed JSON is malformed")
        } catch (error: IOException) {
            Log.w(TAG, "Transient price feed failure", error)
            retryOrFail(error.message ?: "Price feed network failure")
        } catch (error: Exception) {
            Log.e(TAG, "Unexpected price synchronisation failure", error)
            retryOrFail(error.message ?: "Unexpected price synchronisation failure")
        }
    }

    private fun retryOrFail(message: String): Result =
        if (runAttemptCount + 1 >= MAX_ATTEMPTS) failure(message) else Result.retry()

    private fun failure(message: String): Result = Result.failure(workDataOf(KEY_ERROR to message))

    companion object {
        private const val TAG = "PriceSyncWorker"
        private const val USER_AGENT = "GeoSylva-price-sync/1"
        private const val MAX_ATTEMPTS = 3
        private val TRANSIENT_HTTP_CODES = setOf(408, 425, 429)

        const val KEY_URL = "url"
        const val KEY_ENTRY_COUNT = "entry_count"
        const val KEY_ERROR = "error"

        private val JSON = Json {
            ignoreUnknownKeys = false
        }
    }
}

/**
 * Lit un corps HTTP sans jamais allouer au-delà de la limite configurée.
 *
 * La longueur déclarée est contrôlée d'abord, puis la limite est de nouveau
 * appliquée pendant la lecture pour les réponses segmentées ou sans Content-Length.
 */
internal fun ResponseBody.readLimitedUtf8(
    maxBytes: Int = MAX_PRICE_FEED_RESPONSE_BYTES
): String {
    require(maxBytes > 0) { "maxBytes must be positive" }
    if (contentLength() > maxBytes) {
        throw IOException("Price feed exceeds the $maxBytes byte limit")
    }

    val output = ByteArrayOutputStream()
    byteStream().use { reader ->
        val buffer = ByteArray(8192)
        while (true) {
            val count = reader.read(buffer)
            if (count < 0) break
            if (count > maxBytes - output.size()) {
                throw IOException("Price feed exceeds the $maxBytes byte limit")
            }
            output.write(buffer, 0, count)
        }
    }
    return output.toString(Charsets.UTF_8.name())
}

/** Invariants métier minimales imposées à une grille tarifaire distante. */
internal fun validatePriceEntries(entries: List<PriceEntry>): Boolean {
    if (entries.isEmpty() || entries.size > 10_000) return false

    val identities = HashSet<String>(entries.size)
    return entries.all { entry ->
        val identity = listOf(
            entry.essence.trim().uppercase(),
            entry.product.trim().uppercase(),
            entry.min.toString(),
            entry.max.toString(),
            entry.quality.orEmpty().trim().uppercase(),
            entry.region.trim().uppercase()
        ).joinToString("|")

        entry.essence.isNotBlank() &&
            entry.product.isNotBlank() &&
            entry.source.isNotBlank() &&
            entry.region.isNotBlank() &&
            entry.min >= 0 &&
            entry.max >= entry.min &&
            entry.max <= 10_000 &&
            entry.eurPerM3.isFinite() &&
            entry.eurPerM3 >= 0.0 &&
            entry.year in 1900..2100 &&
            (entry.quality == null || entry.quality.uppercase() in setOf("A", "B", "C", "D")) &&
            identities.add(identity)
    }
}
