package com.forestry.counter.data.sync

import android.content.Context
import com.forestry.counter.BuildConfig
import com.forestry.counter.network.SecureHttpClient
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalSerializationApi::class)
internal object ParcelSyncApiFactory {
    fun create(context: Context): ParcelSyncApiService? {
        val baseUrl = BuildConfig.GSIE_API_BASE_URL.trim().trimEnd('/').let { value ->
            if (value.isEmpty()) "" else "$value/"
        }
        val localDebug = BuildConfig.DEBUG && SecureHttpClient.isSafeLocalDebugUrl(baseUrl)
        if (baseUrl.isEmpty() || (!SecureHttpClient.isSafeRemoteHttpsUrl(baseUrl) && !localDebug)) {
            return null
        }
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(SecureHttpClient.createSecureClient(context, allowLocalDebug = localDebug))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ParcelSyncApiService::class.java)
    }
}
