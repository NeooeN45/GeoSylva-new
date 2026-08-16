package com.forestry.counter.network

import android.content.Context
import okhttp3.CertificatePinner
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Client HTTP durci pour les flux distants configurables.
 *
 * TLS repose sur les autorités système définies dans network_security_config.xml.
 *
 * Les fournisseurs cartographiques sont des services tiers dont GeoSylva ne
 * contrôle ni les certificats ni leur rotation. Ils utilisent donc la chaîne de
 * confiance Android, sans pins statiques applicatifs. Cette stratégie évite de
 * rendre une version déjà distribuée inutilisable lors d'une rotation TLS tout
 * en conservant HTTPS obligatoire, la liste blanche et la protection DNS/SSRF.
 *
 * Un éventuel épinglage futur doit être limité à une infrastructure Quintessences
 * maîtrisée, avec au moins une clé de secours réelle et une procédure de rotation
 * testée avant activation.
 */
object SecureHttpClient {

    /**
     * Crée un client HTTPS avec validation des redirections et résolution DNS publique.
     *
     * @param context       Contexte de l'application
     * @param enableLogging Active les logs HTTP en mode DEBUG uniquement
     * @return OkHttpClient configuré pour refuser les cibles réseau non publiques
     */
    fun createSecureClient(
        @Suppress("UNUSED_PARAMETER") context: Context,
        enableLogging: Boolean = false,
        allowLocalDebug: Boolean = false,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                val target = chain.request().url.toString()
                if (
                    !isSafeRemoteHttpsUrl(target) &&
                    !(allowLocalDebug && isSafeLocalDebugUrl(target))
                ) {
                    throw IOException("Redirection vers une URL non publique ou non HTTPS refusée")
                }
                chain.proceed(chain.request())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .dns(if (allowLocalDebug) Dns.SYSTEM else PublicOnlyDns)

        if (enableLogging && isDebugBuild()) {
            builder.addInterceptor(Interceptor { chain ->
                val req = chain.request()
                android.util.Log.d("SecureHttpClient", "→ ${req.method} ${req.url.host}")
                val resp: Response = chain.proceed(req)
                android.util.Log.d("SecureHttpClient", "← ${resp.code}")
                resp
            })
        }

        return builder.build()
    }

    /** Domaines cartographiques explicitement autorisés par l'application. */
    val SECURE_DOMAINS = listOf(
        "demotiles.maplibre.org",
        "tile.opentopomap.org",
        "basemaps.cartocdn.com",
        "server.arcgisonline.com",
        "data.geopf.fr",
        "api.maptiler.com",
        "tiles.maptiler.com",
        "tile.openstreetmap.org"
    )

    /**
     * Pinner volontairement vide : compatibilité avec le diagnostic existant.
     * La confiance TLS est fournie par Android, pas par des pins tiers figés.
     */
    fun buildCertificatePinner(): CertificatePinner = CertificatePinner.Builder().build()

    /** Retourne true si l'URL cible un domaine de la liste [SECURE_DOMAINS]. */
    fun isSecureDomain(url: String): Boolean {
        if (!isSafeRemoteHttpsUrl(url)) return false
        val host = url.toHttpUrlOrNull()?.host ?: return false
        return SECURE_DOMAINS.any { domain ->
            host.equals(domain, ignoreCase = true) || host.endsWith(".$domain", ignoreCase = true)
        }
    }

    /**
     * Valide une URL fournie dynamiquement avant toute requête distante.
     *
     * La résolution DNS est contrôlée séparément par [PublicOnlyDns], ce qui protège
     * aussi contre un nom public redirigé vers une adresse privée.
     */
    fun isSafeRemoteHttpsUrl(url: String): Boolean {
        val parsed = url.toHttpUrlOrNull() ?: return false
        if (parsed.scheme != "https" || parsed.username.isNotEmpty() || parsed.password.isNotEmpty()) {
            return false
        }
        val host = parsed.host
        if (host.equals("localhost", ignoreCase = true) || host.endsWith(".localhost", ignoreCase = true)) {
            return false
        }
        return !isForbiddenIpLiteral(host)
    }

    /** Autorise uniquement les alias locaux Android dans une variante debug. */
    fun isSafeLocalDebugUrl(url: String): Boolean {
        val parsed = url.toHttpUrlOrNull() ?: return false
        if (parsed.scheme != "http" || parsed.username.isNotEmpty() || parsed.password.isNotEmpty()) {
            return false
        }
        return parsed.host.equals("localhost", ignoreCase = true) ||
            parsed.host == "127.0.0.1" ||
            parsed.host == "10.0.2.2"
    }

    private fun isForbiddenIpLiteral(host: String): Boolean {
        val isIpv4Literal = IPV4_LITERAL.matches(host)
        val isIpv6Literal = ':' in host
        if (!isIpv4Literal && !isIpv6Literal) return false
        val address = runCatching { InetAddress.getByName(host) }.getOrNull() ?: return true
        return isForbiddenAddress(address)
    }

    private fun isForbiddenAddress(address: InetAddress): Boolean {
        if (
            address.isAnyLocalAddress ||
            address.isLoopbackAddress ||
            address.isLinkLocalAddress ||
            address.isSiteLocalAddress ||
            address.isMulticastAddress
        ) {
            return true
        }

        val bytes = address.address.map(Byte::toInt).map { it and 0xff }
        if (bytes.size == 16) {
            val isUniqueLocal = bytes.first() and 0xfe == 0xfc
            val isDocumentation = bytes[0] == 0x20 &&
                bytes[1] == 0x01 &&
                bytes[2] == 0x0d &&
                bytes[3] == 0xb8
            val isBenchmark = bytes.take(6) == listOf(0x20, 0x01, 0x00, 0x02, 0x00, 0x00)
            return isUniqueLocal || isDocumentation || isBenchmark
        }
        if (bytes.size != 4) return false

        val first = bytes[0]
        val second = bytes[1]
        return first == 0 ||
            first >= 224 ||
            (first == 100 && second in 64..127) ||
            (first == 198 && second in 18..19) ||
            (first == 192 && second == 0) ||
            (first == 192 && second == 88 && bytes[2] == 99) ||
            (first == 198 && second == 51 && bytes[2] == 100) ||
            (first == 203 && second == 0 && bytes[2] == 113)
    }

    private val IPV4_LITERAL = Regex("""^(?:\d{1,3}\.){3}\d{1,3}$""")

    private object PublicOnlyDns : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            val addresses = Dns.SYSTEM.lookup(hostname)
            if (addresses.isEmpty() || addresses.any(::isForbiddenAddress)) {
                throw UnknownHostException("Adresse réseau non publique refusée")
            }
            return addresses
        }
    }

    private fun isDebugBuild(): Boolean {
        return try {
            Class.forName("com.forestry.counter.BuildConfig")
                .getField("DEBUG")
                .getBoolean(null)
        } catch (e: Exception) {
            false
        }
    }
}
