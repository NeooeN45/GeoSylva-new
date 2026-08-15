package com.forestry.counter.network

import android.content.Context
import com.forestry.counter.BuildConfig
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
 * L'épinglage de clés (certificate pinning) OkHttp est **activé** pour tous les
 * domaines de la liste [SECURE_DOMAINS] (7 domaines cartographiques) ainsi que
 * pour le domaine de l'API GSIE extrait dynamiquement de
 * `BuildConfig.GSIE_API_BASE_URL`. Chaque domaine possède un pin primaire et un
 * pin de secours (backup) afin de permettre une rotation TLS coordonnée sans
 * interruption de service : si l'autorité ou la clé publique primaire est
 * remplacée, le pin de secours maintient la connectivité le temps que le pin
 * primaire soit mis à jour dans une nouvelle version de l'application.
 *
 * Les pins actuels sont des **placeholders** ([PIN_PRIMARY_PLACEHOLDER] et
 * [PIN_BACKUP_PLACEHOLDER]) à remplacer par les hashes SHA-256 des clés
 * publiques de production (voir les `TODO` dans [buildCertificatePinner]).
 *
 * ATTENTION : OkHttp rejette toute connexion vers un domaine pinné dont le
 * certificat ne correspond à aucun pin déclaré. Tant que les placeholders sont
 * en place, les flux vers les domaines pinnés échoueront en production.
 * Remplacer impérativement les placeholders par les hashes réels avant toute
 * mise en production, sinon tous les flux distants (tuiles carto + API GSIE)
 * seront bloqués.
 */
object SecureHttpClient {

    /**
     * Pin primaire placeholder — hash SHA-256 d'une clé publique inexistante.
     *
     * TODO: remplacer par hash production (clé publique actuelle de chaque
     * domaine). À générer avec `openssl s_client` ou l'utilitaire
     * `CertificatePinner.pin()` d'OkHttp sur le certificat de production.
     */
    private const val PIN_PRIMARY_PLACEHOLDER =
        "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="

    /**
     * Pin de secours placeholder — hash SHA-256 d'une clé de rotation future.
     *
     * TODO: remplacer par hash production (clé publique de la prochaine
     * rotation TLS de chaque domaine). Permet de changer de certificat sans
     * casser l'application déjà déployée.
     */
    private const val PIN_BACKUP_PLACEHOLDER =
        "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="

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
            .certificatePinner(buildCertificatePinner())
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
     * Construit le [CertificatePinner] OkHttp pour tous les domaines pinnés.
     *
     * Couvre les 7 domaines cartographiques de [SECURE_DOMAINS] ainsi que le
     * domaine de l'API GSIE extrait dynamiquement de
     * `BuildConfig.GSIE_API_BASE_URL`. Chaque domaine reçoit un pin primaire
     * ([PIN_PRIMARY_PLACEHOLDER]) et un pin de secours
     * ([PIN_BACKUP_PLACEHOLDER]) pour permettre la rotation TLS.
     *
     * Le domaine GSIE n'est pinné que si son URL de base est une URL HTTPS
     * distante valide ([isSafeRemoteHttpsUrl]) : les URLs de debug local
     * (ex. `http://127.0.0.1:8000/`) sont exclues pour ne pas bloquer les
     * essais en émulateur.
     *
     * @return CertificatePinner configuré pour tous les domaines éligibles
     */
    fun buildCertificatePinner(): CertificatePinner {
        val builder = CertificatePinner.Builder()
        SECURE_DOMAINS.forEach { domain ->
            builder.add(domain, PIN_PRIMARY_PLACEHOLDER, PIN_BACKUP_PLACEHOLDER)
        }
        gsieApiHost()?.let { host ->
            builder.add(host, PIN_PRIMARY_PLACEHOLDER, PIN_BACKUP_PLACEHOLDER)
        }
        return builder.build()
    }

    /**
     * Extrait le nom d'hôte de l'URL de base de l'API GSIE.
     *
     * @return le host pinnable, ou null si l'URL est vide, invalide ou locale
     */
    private fun gsieApiHost(): String? {
        val baseUrl = BuildConfig.GSIE_API_BASE_URL.trim()
        if (baseUrl.isEmpty() || !isSafeRemoteHttpsUrl(baseUrl)) return null
        return baseUrl.toHttpUrlOrNull()?.host
    }

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
