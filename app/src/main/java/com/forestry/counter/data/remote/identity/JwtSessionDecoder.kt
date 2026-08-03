package com.forestry.counter.data.remote.identity

import com.forestry.counter.domain.model.AccountSession
import com.forestry.counter.domain.model.IdentityProvider
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Lit uniquement les métadonnées d'affichage d'un jeton déjà accepté via HTTPS.
 * L'autorisation reste exclusivement vérifiée par GSIE côté serveur.
 */
internal object JwtSessionDecoder {
    private val json = Json { ignoreUnknownKeys = true }

    fun decode(accessToken: String): AccountSession? = runCatching {
        val payloadPart = accessToken.split('.').getOrNull(1) ?: return null
        val payloadBytes = Base64.getUrlDecoder().decode(padded(payloadPart))
        val payload = json.parseToJsonElement(
            String(payloadBytes, StandardCharsets.UTF_8)
        ).jsonObject
        val subject = payload["sub"]?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() && it.length <= 200 }
            ?: return null
        val expiresAt = payload["exp"]?.jsonPrimitive?.longOrNull ?: return null
        val provider = when (payload["auth_provider"]?.jsonPrimitive?.contentOrNull) {
            "local" -> IdentityProvider.LOCAL
            "google" -> IdentityProvider.GOOGLE
            "enterprise" -> IdentityProvider.ENTERPRISE
            else -> IdentityProvider.UNKNOWN
        }
        val roles = payload["roles"]?.jsonArray
            ?.mapNotNull { it.jsonPrimitive.contentOrNull }
            ?.filter { it.length <= 100 }
            .orEmpty()
        AccountSession(subject, provider, roles, expiresAt)
    }.getOrNull()

    private fun padded(value: String): String = value + "=".repeat((4 - value.length % 4) % 4)
}
