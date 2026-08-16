package com.forestry.counter.domain.location

import android.content.Context
import android.util.Log
import com.forestry.counter.BuildConfig
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Configuration du client HTTP MapLibre (OkHttp) avec :
 *  - Cache disque 50 MB (tuiles + sources GeoJSON + sprites)
 *  - Retry automatique sur HTTP 5xx et erreurs réseau (3 tentatives, backoff exponentiel)
 *  - User-Agent conforme à la politique OSM Tile Usage
 *
 * Doit être appelé une seule fois, après [com.mapbox.mapboxsdk.Mapbox.getInstance],
 * avant toute création de MapView.
 */
object MapLibreHttpConfig {

    private const val TAG = "MapLibreHttpConfig"
    private const val CACHE_DIR = "maplibre_http_cache"
    private const val CACHE_SIZE_BYTES = 50L * 1024 * 1024 // 50 MB
    private const val MAX_RETRIES = 3
    private const val BACKOFF_BASE_MS = 500L

    /** User-Agent conforme OSM — identique à OfflineTileManager pour cohérence */
    private val USER_AGENT =
        "GeoSylva/${BuildConfig.VERSION_NAME} (+https://geosylva.fr; contact: contact@geosylva.fr)"

    @Volatile
    private var configured = false

    /**
     * Configure le client OkHttp de MapLibre avec cache + retry.
     * Idempotent : ne reconfigure pas si déjà fait.
     */
    fun configure(context: Context) {
        if (configured) return
        try {
            val cacheDir = File(context.cacheDir, CACHE_DIR)
            val cache = Cache(cacheDir, CACHE_SIZE_BYTES)

            // NOTE : une hausse de maxRequestsPerHost (5→16) et un
            // intercepteur forçant Cache-Control sur les réponses de
            // tuiles ont été essayés ici pour accélérer le chargement,
            // puis retirés — ils coïncidaient avec l'apparition de tuiles
            // rendues en noir à côté de tuiles correctement chargées, sur
            // une même couche (ex. OSM), reproduit à la fois sur
            // l'émulateur ET sur un appareil réel. Reproductible sur les
            // deux, ce qui pointe vers l'un de ces deux changements
            // applicatifs plutôt qu'une particularité GPU d'émulateur.
            // Config OkHttp minimale, alignée sur ce qui fonctionnait
            // avant ces deux tentatives : cache disque simple, retry,
            // User-Agent — rien qui réécrive les réponses ni ne pousse la
            // concurrence au-delà des valeurs par défaut d'OkHttp.
            val client = OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(UserAgentInterceptor(USER_AGENT))
                .addInterceptor(RetryInterceptor(MAX_RETRIES, BACKOFF_BASE_MS))
                .build()

            HttpRequestUtil.setOkHttpClient(client)
            configured = true
            Log.i(TAG, "OkHttp configured: cache=${CACHE_SIZE_BYTES / 1024 / 1024}MB at ${cacheDir.absolutePath}")
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to configure OkHttp for MapLibre", e)
        }
    }
}

/** Ajoute le header User-Agent à toutes les requêtes sortantes. */
private class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(request)
    }
}

/**
 * Retry interceptor avec backoff exponentiel.
 * Retente sur :
 *  - HTTP 5xx (erreurs serveur transitoires)
 *  - IOException (timeout, perte réseau)
 * Ne retente PAS sur 4xx (erreurs client définitives).
 */
private class RetryInterceptor(
    private val maxRetries: Int,
    private val backoffBaseMs: Long
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastError: IOException? = null
        var lastResponse: Response? = null

        while (attempt <= maxRetries) {
            try {
                lastResponse?.close()
                val response = chain.proceed(chain.request())
                if (response.isSuccessful || response.code < 500) {
                    return response
                }
                // 5xx → retry
                lastResponse = response
                if (attempt == maxRetries) return response
            } catch (e: IOException) {
                lastError = e
                if (attempt == maxRetries) throw e
            }

            attempt++
            // Backoff exponentiel : 500ms, 1000ms, 2000ms
            val delayMs = backoffBaseMs shl (attempt - 1)
            try {
                Thread.sleep(delayMs)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                throw lastError ?: IOException("Retry interrupted")
            }
        }

        // Ne devrait jamais arriver (le loop retourne ou throw ci-dessus)
        return lastResponse ?: throw (lastError ?: IOException("Retry exhausted"))
    }
}
