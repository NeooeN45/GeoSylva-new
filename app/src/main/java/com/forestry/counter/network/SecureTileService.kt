package com.forestry.counter.network

import android.content.Context

/**
 * Service de validation des URL de tuiles cartographiques.
 */
class SecureTileService(@Suppress("UNUSED_PARAMETER") context: Context) {

    /**
     * Vérifie qu'une URL de tuile utilise un domaine autorisé.
     */
    fun validateTileUrl(url: String): Boolean {
        return SecureHttpClient.isSecureDomain(url)
    }

    /**
     * Obtient les statistiques de sécurité pour le monitoring.
     * Les connexions reposent sur TLS système, une liste de domaines autorisés
     * et l'épinglage de clés (certificate pinning) OkHttp avec pin de secours.
     */
    fun getSecurityStats(): SecurityStats {
        return SecurityStats(
            secureDomainsCount = SecureHttpClient.SECURE_DOMAINS.size,
            certificatePinningEnabled = true,
            loggingEnabled = false
        )
    }
}

/**
 * Statistiques de sécurité pour le monitoring.
 */
data class SecurityStats(
    val secureDomainsCount: Int,
    val certificatePinningEnabled: Boolean,
    val loggingEnabled: Boolean
)
