package com.forestry.counter.domain.model

/** Fournisseurs d'identité compris par l'écosystème Quintessences. */
enum class IdentityProvider {
    LOCAL,
    GOOGLE,
    ENTERPRISE,
    UNKNOWN,
}

enum class ProviderAvailability {
    AVAILABLE,
    NOT_CONFIGURED,
    DEVELOPMENT,
}

data class ProviderCapability(
    val provider: IdentityProvider,
    val availability: ProviderAvailability,
    val label: String,
)

/** Métadonnées non sensibles extraites d'un jeton GSIE déjà reçu. */
data class AccountSession(
    val accountId: String,
    val provider: IdentityProvider,
    val roles: List<String>,
    val expiresAtEpochSeconds: Long,
) {
    fun isExpired(nowEpochSeconds: Long = System.currentTimeMillis() / 1_000L): Boolean =
        expiresAtEpochSeconds <= nowEpochSeconds
}

data class AccountProfile(
    val accountId: String,
    val displayName: String?,
    val email: String?,
    val emailVerified: Boolean,
    val providers: List<IdentityProvider>,
    val roles: List<String>,
)

data class GoogleNonce(
    val value: String,
    val expiresInSeconds: Int,
)

enum class ApiConnectionState {
    CONNECTED,
    DEGRADED,
    UNREACHABLE,
    NOT_CONFIGURED,
}

data class ApiDiagnostic(
    val state: ApiConnectionState,
    val latencyMillis: Long? = null,
    val httpCode: Int? = null,
    val version: String? = null,
    val environment: String? = null,
    val dependencies: Map<String, String> = emptyMap(),
    val checkedAtEpochMillis: Long = System.currentTimeMillis(),
)

enum class IdentityError {
    API_NOT_CONFIGURED,
    GOOGLE_NOT_CONFIGURED,
    SECURE_STORAGE_UNAVAILABLE,
    INVALID_INPUT,
    INVALID_CREDENTIALS,
    ACCOUNT_ALREADY_EXISTS,
    ACCOUNT_LINK_REQUIRED,
    ACTION_CODE_INVALID,
    EMAIL_DELIVERY_UNAVAILABLE,
    NETWORK_UNAVAILABLE,
    SERVER_UNAVAILABLE,
    INVALID_SERVER_RESPONSE,
    CANCELLED,
    UNKNOWN,
}

class IdentityClientException(
    val error: IdentityError,
    cause: Throwable? = null,
) : Exception(error.name, cause)
