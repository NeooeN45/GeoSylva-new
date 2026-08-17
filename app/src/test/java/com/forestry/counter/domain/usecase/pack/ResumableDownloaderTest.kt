package com.forestry.counter.domain.usecase.pack

import java.io.File
import java.net.URL
import java.security.MessageDigest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Couvre les critères de test exigés par PROMPT_03/PR-G2 pour le
 * téléchargement de pack : coupure et reprise, mauvais ETag, checksum
 * invalide, espace disque insuffisant.
 */
class ResumableDownloaderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun sha256(data: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(data).joinToString("") { "%02x".format(it) }

    @Test
    fun `telechargement complet reussit et le checksum correspond`() {
        val payload = "contenu-de-test-pack-geosylva".repeat(100).toByteArray()
        server.enqueue(MockResponse().setResponseCode(200).setBody(okio.Buffer().write(payload)))

        val destination = File(tempFolder.root, "pack.part")
        var lastProgress = 0f
        downloadWithChecksum(URL(server.url("/pack").toString()), destination, sha256(payload)) {
            lastProgress = it
        }

        assertEquals(payload.size.toLong(), destination.length())
        assertTrue(destination.readBytes().contentEquals(payload))
        assertEquals(1f, lastProgress)
    }

    @Test
    fun `checksum invalide rejette le fichier et ne laisse aucun fragment`() {
        val payload = "donnees-corrompues".toByteArray()
        server.enqueue(MockResponse().setResponseCode(200).setBody(okio.Buffer().write(payload)))

        val destination = File(tempFolder.root, "pack.part")
        val wrongSha = sha256("autre-contenu".toByteArray())

        val error = runCatching {
            downloadWithChecksum(URL(server.url("/pack").toString()), destination, wrongSha)
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertFalse("le fragment corrompu ne doit pas rester sur disque", destination.exists())
    }

    @Test
    fun `coupure reseau puis reprise avec ETag assemble le meme contenu`() {
        val fullPayload = ("segment-avant-coupure-" + "x".repeat(500) + "-segment-apres-coupure").toByteArray()
        val firstHalf = fullPayload.copyOfRange(0, fullPayload.size / 2)
        val secondHalf = fullPayload.copyOfRange(fullPayload.size / 2, fullPayload.size)
        val etag = "\"etag-v1\""

        // Première tentative : coupure réseau après le premier octet.
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("ETag", etag)
                .setBody(okio.Buffer().write(firstHalf))
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        val destination = File(tempFolder.root, "pack.part")
        val firstAttempt = runCatching {
            downloadWithChecksum(URL(server.url("/pack").toString()), destination, sha256(fullPayload))
        }
        assertTrue("la premiere tentative doit echouer (coupure simulee)", firstAttempt.isFailure)

        // On simule manuellement ce que le serveur a réellement transmis avant
        // la coupure (MockWebServer NO_RESPONSE ne permet pas d'envoyer un
        // corps partiel réaliste) : le fragment + son ETag restent sur disque,
        // exactement comme `installPack` les préserverait après une IOException.
        destination.writeBytes(firstHalf)
        File(destination.parentFile, "${destination.name}.etag").writeText(etag)

        // Deuxième tentative : le serveur accepte la reprise (206 + ETag identique).
        server.enqueue(
            MockResponse()
                .setResponseCode(206)
                .setHeader("ETag", etag)
                .setBody(okio.Buffer().write(secondHalf))
        )

        downloadWithChecksum(URL(server.url("/pack").toString()), destination, sha256(fullPayload))

        assertTrue(destination.readBytes().contentEquals(fullPayload))

        val rangeRequest = server.takeRequest() // requête de la 1ère tentative
        assertTrue(rangeRequest.path == "/pack")
        val resumeRequest = server.takeRequest()
        assertEquals("bytes=${firstHalf.size}-", resumeRequest.getHeader("Range"))
        assertEquals(etag, resumeRequest.getHeader("If-Range"))
    }

    @Test
    fun `ETag invalide au moment de la reprise force un redemarrage complet`() {
        val fullPayload = "contenu-complet-apres-changement-serveur".toByteArray()
        val staleFragment = "ancien-fragment-perime".toByteArray()

        val destination = File(tempFolder.root, "pack.part")
        destination.writeBytes(staleFragment)
        File(destination.parentFile, "${destination.name}.etag").writeText("\"etag-perime\"")

        // Le contenu a changé côté serveur : il renvoie 200 (pas 206) malgré
        // la demande de reprise, invalidant l'ETag connu.
        server.enqueue(MockResponse().setResponseCode(200).setBody(okio.Buffer().write(fullPayload)))

        downloadWithChecksum(URL(server.url("/pack").toString()), destination, sha256(fullPayload))

        assertTrue(destination.readBytes().contentEquals(fullPayload))
    }

    @Test
    fun `espace disque insuffisant est rejete explicitement avant ecriture`() {
        // MockWebServer écrase le Content-Length déclaré par la taille réelle
        // du body, ce qui rend impossible le déclenchement de la branche
        // d'espace disque via un téléchargement mocké. On teste donc
        // directement la fonction pure [checkDiskSpace] extraite de
        // [downloadWithChecksum] pour garantir le contrat : refus explicite
        // avec un message clair quand l'espace disponible est insuffisant.
        val error = runCatching {
            checkDiskSpace(available = 1_000L, required = 10_000L, path = "/pack/region.pack")
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(error?.message?.contains("Espace disque insuffisant") == true)
        assertTrue(error?.message?.contains("/pack/region.pack") == true)
        assertTrue(error?.message?.contains("10000") == true)
    }

    @Test
    fun `espace disque suffisant ne leve pas d erreur`() {
        // Garde-fou symétrique : la fonction ne doit pas lever quand l'espace
        // disponible couvre exactement le besoin (cas limite égalité).
        checkDiskSpace(available = 10_000L, required = 10_000L, path = "/pack/ok.pack")
        checkDiskSpace(available = Long.MAX_VALUE, required = 1L, path = "/pack/ok.pack")
    }

    @Test
    fun `schema non HTTPS est rejete explicitement`() {
        // Le SHA-256 protège l'intégrité mais pas la confidentialité de la
        // source — HTTP est interdit pour éviter un MITM qui observerait le
        // flux. Validé par checkHttpsScheme (fonction pure) car MockWebServer
        // tourne en HTTP, ce qui rendrait impossible le déclenchement de
        // cette branche via un téléchargement mocké.
        val httpUrl = URL("http://example.com/pack/region.pack")
        val error = runCatching { checkHttpsScheme(httpUrl) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        assertTrue(error?.message?.contains("HTTPS obligatoire") == true)
        assertTrue(error?.message?.contains("http") == true)
    }

    @Test
    fun `schema HTTPS est accepte`() {
        // Garde-fou symétrique : la fonction ne doit pas lever pour HTTPS.
        checkHttpsScheme(URL("https://example.com/pack/region.pack"))
        checkHttpsScheme(URL("HTTPS://example.com/pack/region.pack"))
    }

    @Test
    fun `pack depassant la taille maximale est rejete`() {
        // Garde-fou streaming : un serveur compromis peut mentir sur
        // Content-Length ou l'omettre et streamer indéfiniment. checkPackSize
        // impose un plafond dur (MAX_PACK_BYTES = 512 Mo) indépendant de
        // l'en-tête serveur.
        val oversized = 600L * 1024 * 1024 // 600 Mo > 512 Mo
        val error = runCatching { checkPackSize(oversized, "/pack/region.pack") }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(error?.message?.contains("trop volumineux") == true)
    }

    @Test
    fun `pack sous la taille maximale est accepte`() {
        // Garde-fou symétrique : la fonction ne doit pas lever sous le plafond.
        checkPackSize(0L, "/pack/empty.pack")
        checkPackSize(512L * 1024 * 1024, "/pack/limit.pack") // exactement 512 Mo
    }

    @Test
    fun `code HTTP d'erreur serveur est signale explicitement`() {
        server.enqueue(MockResponse().setResponseCode(500))

        val destination = File(tempFolder.root, "pack.part")
        val error = runCatching {
            downloadWithChecksum(URL(server.url("/pack").toString()), destination, "0".repeat(64))
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(error?.message?.contains("HTTP 500") == true)
    }
}
