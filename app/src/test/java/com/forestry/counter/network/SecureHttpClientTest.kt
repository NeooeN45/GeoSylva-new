package com.forestry.counter.network

import android.content.Context
import com.forestry.counter.BuildConfig
import io.mockk.mockk
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import java.net.UnknownHostException
import org.junit.Before
import org.junit.Test

/**
 * Tests pour SecureHttpClient - Couverture des fonctionnalités de réseau sécurisé.
 * Vérifie HTTPS, la résolution DNS publique, le refus des cibles SSRF et
 * l'épinglage de clés (certificate pinning) des domaines autorisés.
 */
class SecureHttpClientTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
    }

    @Test
    fun `createSecureClient should gate logging on build type`() {
        // When
        val client = SecureHttpClient.createSecureClient(context, enableLogging = true)

        // Then
        assertEquals(BuildConfig.DEBUG, client.interceptors.isNotEmpty())
        assertTrue(client.networkInterceptors.isNotEmpty())
    }

    @Test
    fun `isSecureDomain should return true for allowed domains`() {
        // Given
        val secureUrls = listOf(
            "https://demotiles.maplibre.org/tiles/{z}/{x}/{y}.png",
            "https://tile.opentopomap.org/{z}/{x}/{y}.png",
            "https://basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png",
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
            "https://data.geopf.fr/wmts?"
        )

        // When/Then
        secureUrls.forEach { url ->
            assert(SecureHttpClient.isSecureDomain(url)) { "URL should be secure: $url" }
        }
    }

    @Test
    fun `isSecureDomain should return false for unallowed domains`() {
        // Given
        val unsecureUrls = listOf(
            "https://evil.com/malware",
            "http://insecure-site.org/data",
            "https://phishing.net/steal",
            "https://unknown-domain.com/api"
        )

        // When/Then
        unsecureUrls.forEach { url ->
            assert(!SecureHttpClient.isSecureDomain(url)) { "URL should not be secure: $url" }
        }
    }

    @Test
    fun `isSecureDomain should handle malformed URLs gracefully`() {
        // Given
        val malformedUrls = listOf(
            "not-a-url",
            "",
            "ftp://protocol-not-supported.com",
            "javascript:alert('xss')",
            "data:text/plain,evil"
        )

        // When/Then
        malformedUrls.forEach { url ->
            assert(!SecureHttpClient.isSecureDomain(url)) { "Malformed URL should not be secure: $url" }
        }
    }

    @Test
    fun `isSecureDomain should handle subdomains correctly`() {
        // Given
        val subdomainUrls = listOf(
            "https://api.demotiles.maplibre.org/v1/tiles",
            "https://cdn.tile.opentopomap.org/assets",
            "https://tiles.basemaps.cartocdn.com/maps"
        )

        // When/Then
        subdomainUrls.forEach { url ->
            assert(SecureHttpClient.isSecureDomain(url)) { "Subdomain URL should be secure: $url" }
        }
    }

    @Test
    fun `SECURE_DOMAINS should contain all expected domains`() {
        // Given
        val expectedDomains = listOf(
            "demotiles.maplibre.org",
            "tile.opentopomap.org",
            "basemaps.cartocdn.com",
            "server.arcgisonline.com",
            "data.geopf.fr"
        )

        // When/Then
        expectedDomains.forEach { domain ->
            assert(SecureHttpClient.SECURE_DOMAINS.contains(domain)) { 
                "Domain should be in secure domains list: $domain" 
            }
        }
    }

    @Test
    fun `createSecureClient should reject system DNS resolver`() {
        // When
        val client = SecureHttpClient.createSecureClient(context, enableLogging = false)

        // Then
        assertFalse(client.dns === Dns.SYSTEM)
    }

    @Test
    fun `createSecureClient should have reasonable timeouts`() {
        // When
        val client = SecureHttpClient.createSecureClient(context, enableLogging = false)

        // Then
        assert(client.connectTimeoutMillis == 30000) { "Connect timeout should be 30s" }
        assert(client.readTimeoutMillis == 60000) { "Read timeout should be 60s" }
        assert(client.writeTimeoutMillis == 60000) { "Write timeout should be 60s" }
    }

    @Test
    fun `createSecureClient should retry on connection failure`() {
        // When
        val client = SecureHttpClient.createSecureClient(context, enableLogging = false)

        // Then
        assert(client.retryOnConnectionFailure) { "Should retry on connection failure" }
    }

    @Test
    fun `safe remote URL rejects local credentials and non https targets`() {
        assertTrue(
            SecureHttpClient.isSafeRemoteHttpsUrl("https://prices.example.org/feed.json")
        )

        listOf(
            "http://prices.example.org/feed.json",
            "https://localhost/feed.json",
            "https://127.0.0.1/feed.json",
            "https://10.0.0.1/feed.json",
            "https://172.16.1.2/feed.json",
            "https://192.168.1.2/feed.json",
            "https://169.254.169.254/latest/meta-data",
            "https://100.64.0.1/feed.json",
            "https://192.0.2.1/feed.json",
            "https://[::1]/feed.json",
            "https://[fc00::1]/feed.json",
            "https://[2001:db8::1]/feed.json",
            "https://user:password@example.org/feed.json"
        ).forEach { url ->
            assertFalse("URL distante dangereuse acceptée : $url", SecureHttpClient.isSafeRemoteHttpsUrl(url))
        }
    }

    @Test
    fun `secure client DNS rejects non public addresses`() {
        val client = SecureHttpClient.createSecureClient(context)

        listOf(
            "127.0.0.1",
            "10.0.0.1",
            "100.64.0.1",
            "192.0.2.1",
            "fc00::1",
            "2001:db8::1"
        ).forEach { address ->
            try {
                client.dns.lookup(address)
                fail("Une adresse non publique ne doit jamais être résolue : $address")
            } catch (_: UnknownHostException) {
                // Protection SSRF attendue.
            }
        }
    }

    @Test
    fun `secure domain requires https`() {
        assertFalse(SecureHttpClient.isSecureDomain("http://data.geopf.fr/resource"))
        assertTrue(SecureHttpClient.isSecureDomain("https://data.geopf.fr/resource"))
    }

    @Test
    fun `local debug URL accepts only loopback and emulator aliases`() {
        listOf(
            "http://localhost:8000/",
            "http://127.0.0.1:8000/",
            "http://10.0.2.2:8000/"
        ).forEach { url ->
            assertTrue(url, SecureHttpClient.isSafeLocalDebugUrl(url))
        }
        listOf(
            "http://192.168.1.10:8000/",
            "http://10.0.0.5:8000/",
            "https://127.0.0.1:8000/",
            "http://user:password@127.0.0.1:8000/"
        ).forEach { url ->
            assertFalse(url, SecureHttpClient.isSafeLocalDebugUrl(url))
        }
    }

    @Test
    fun `local debug client uses system DNS only when explicitly enabled`() {
        val client = SecureHttpClient.createSecureClient(context, allowLocalDebug = true)

        assertTrue(client.dns === Dns.SYSTEM)
    }

    @Test
    fun `buildCertificatePinner should pin every secure carto domain`() {
        val pinner = SecureHttpClient.buildCertificatePinner()

        SecureHttpClient.SECURE_DOMAINS.forEach { domain ->
            val pins = pinner.findMatchingPins(domain)
            assertTrue(
                "Le domaine carto devrait avoir au moins un pin : $domain",
                pins.isNotEmpty()
            )
        }
    }

    @Test
    fun `buildCertificatePinner should provide a backup pin per domain`() {
        val pinner = SecureHttpClient.buildCertificatePinner()

        SecureHttpClient.SECURE_DOMAINS.forEach { domain ->
            val pins = pinner.findMatchingPins(domain)
            assertTrue(
                "Chaque domaine devrait avoir un pin primaire + un pin de secours : $domain",
                pins.size >= 2
            )
        }
    }

    @Test
    fun `buildCertificatePinner should pin the GSIE API host when configured`() {
        val gsieHost = BuildConfig.GSIE_API_BASE_URL.trim()
            .takeIf { it.isNotEmpty() && SecureHttpClient.isSafeRemoteHttpsUrl(it) }
            ?.toHttpUrlOrNull()?.host

        val pinner = SecureHttpClient.buildCertificatePinner()

        if (gsieHost != null) {
            val pins = pinner.findMatchingPins(gsieHost)
            assertTrue(
                "Le domaine GSIE configuré devrait être pinné : $gsieHost",
                pins.isNotEmpty()
            )
        } else {
            // Pas de domaine GSIE distant configuré : le pinning carto reste actif.
            SecureHttpClient.SECURE_DOMAINS.forEach { domain ->
                assertTrue(pinner.findMatchingPins(domain).isNotEmpty())
            }
        }
    }

    @Test
    fun `createSecureClient should enable certificate pinning`() {
        val client = SecureHttpClient.createSecureClient(context, enableLogging = false)

        // Le client doit porter un CertificatePinner non vide : au moins les
        // domaines carto doivent avoir des pins déclarés.
        val hasPinnedDomain = SecureHttpClient.SECURE_DOMAINS.any { domain ->
            client.certificatePinner.findMatchingPins(domain).isNotEmpty()
        }
        assertTrue("Le client devrait avoir le certificate pinning activé", hasPinnedDomain)
    }

    @Test
    fun `getSecurityStats should report certificate pinning enabled`() {
        val stats = SecureTileService(context).getSecurityStats()

        assertTrue("certificatePinningEnabled devrait être true", stats.certificatePinningEnabled)
    }
}
