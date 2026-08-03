package com.forestry.counter.data.remote.identity

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

internal data class StoredIdentityTokens(
    val accessToken: String,
    val refreshToken: String,
)

/** Stockage dédié : aucun jeton d'identité ne transite par DataStore en clair. */
internal class EncryptedIdentitySessionStore(context: Context) {
    private val preferences: Result<SharedPreferences> = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    val isAvailable: Boolean
        get() = preferences.isSuccess

    fun read(): StoredIdentityTokens? {
        val prefs = preferences.getOrNull() ?: return null
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)?.takeIf(String::isNotBlank)
            ?: return null
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)?.takeIf(String::isNotBlank)
            ?: return null
        return StoredIdentityTokens(accessToken, refreshToken)
    }

    fun save(tokens: StoredIdentityTokens): Boolean {
        val prefs = preferences.getOrNull() ?: return false
        return runCatching {
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
                .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
                .commit()
        }.getOrDefault(false)
    }

    fun clear() {
        val prefs = preferences.getOrNull() ?: return
        runCatching { prefs.edit().clear().commit() }
    }

    private companion object {
        const val FILE_NAME = "quintessences_identity_session"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
