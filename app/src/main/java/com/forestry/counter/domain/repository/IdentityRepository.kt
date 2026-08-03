package com.forestry.counter.domain.repository

import com.forestry.counter.domain.model.AccountSession
import com.forestry.counter.domain.model.AccountProfile
import com.forestry.counter.domain.model.ApiDiagnostic
import com.forestry.counter.domain.model.GoogleNonce
import com.forestry.counter.domain.model.ProviderCapability
import kotlinx.coroutines.flow.StateFlow

interface IdentityRepository {
    val session: StateFlow<AccountSession?>
    val profile: StateFlow<AccountProfile?>
    val apiBaseUrl: String
    val isApiConfigured: Boolean
    val isGoogleClientConfigured: Boolean

    suspend fun getProviders(): Result<List<ProviderCapability>>

    suspend fun loginWithPassword(email: String, password: String): Result<AccountSession>

    suspend fun register(
        email: String,
        password: String,
        displayName: String?,
    ): Result<AccountSession>

    suspend fun requestGoogleNonce(): Result<GoogleNonce>

    suspend fun loginWithGoogle(idToken: String, nonce: String): Result<AccountSession>

    suspend fun refreshSession(): Result<AccountSession>

    suspend fun logout(): Result<Unit>

    suspend fun loadProfile(): Result<AccountProfile>

    suspend fun updateDisplayName(displayName: String?): Result<AccountProfile>

    suspend fun requestEmailVerification(): Result<Unit>

    suspend fun confirmEmailVerification(code: String): Result<AccountProfile>

    suspend fun requestPasswordReset(email: String): Result<Unit>

    suspend fun confirmPasswordReset(
        email: String,
        code: String,
        newPassword: String,
    ): Result<Unit>

    suspend fun diagnoseApi(): ApiDiagnostic
}
