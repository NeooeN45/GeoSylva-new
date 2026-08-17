package com.forestry.counter.data.remote.identity

import android.content.Context
import com.forestry.counter.BuildConfig
import com.forestry.counter.network.SecureHttpClient
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalSerializationApi::class)
internal object IdentityApiFactory {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun create(context: Context, configuredBaseUrl: String): IdentityApiService? {
        val baseUrl = configuredBaseUrl.trim().trimEnd('/').let { value ->
            if (value.isEmpty()) "" else "$value/"
        }
        val localDebug = BuildConfig.DEBUG && SecureHttpClient.isSafeLocalDebugUrl(baseUrl)
        if (
            baseUrl.isEmpty() ||
            (!SecureHttpClient.isSafeRemoteHttpsUrl(baseUrl) && !localDebug)
        ) {
            return null
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(SecureHttpClient.createSecureClient(context, allowLocalDebug = localDebug))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(IdentityApiService::class.java)
    }
}
