package com.forestry.counter.data.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Mémorise le consentement explicite à la synchronisation, séparément pour chaque compte. */
internal class ParcelSyncActivationStore(context: Context) {
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

    fun isEnabled(accountId: String): Boolean =
        preferences.getOrNull()?.getBoolean(accountKey(accountId), false) == true

    fun enable(accountId: String): Boolean {
        val prefs = preferences.getOrNull() ?: return false
        return runCatching {
            prefs.edit().putBoolean(accountKey(accountId), true).commit()
        }.getOrDefault(false)
    }

    private fun accountKey(accountId: String): String = "account_$accountId"

    private companion object {
        const val FILE_NAME = "geosylva_parcel_sync_activation"
    }
}
