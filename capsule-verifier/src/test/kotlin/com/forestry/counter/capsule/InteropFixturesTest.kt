package com.forestry.counter.capsule

import com.forestry.counter.capsule.json.JsonValue
import com.forestry.counter.capsule.json.StrictJsonParser
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Consomme le corpus de fixtures partagé produit par
 * `Quintessences/21_EXPERIMENTS/EXP-0001_CAPSULE_TERRITORIALE/EXPERIENCE/scripts/generate_interop_fixtures.py`
 * (stratégie retenue : sous-ensemble copié avec SHA-256 contrôlé — voir
 * `docs/COMPATIBILITE_KOTLIN.md`). Doit produire exactement les mêmes
 * verdicts que `tests/test_interop_fixtures.py` côté Python sur les mêmes
 * fichiers.
 */
class InteropFixturesTest {
    private val corpusDir =
        File(
            (System.getProperty("capsuleVerifier.corpusDir"))
                ?: "src/test/resources/contract-interop"
        )

    private fun readExpectedManifest(): JsonValue.Obj {
        val bytes = File(corpusDir, "expected.json").readBytes()
        return StrictJsonParser.parse(bytes) as JsonValue.Obj
    }

    private fun keyProviderFor(manifest: JsonValue.Obj): TrustedKeyProvider {
        val trustedKeyId = (manifest.get("trusted_key_id") as JsonValue.Str).value
        val publicKeyDer = pemToDer(File(corpusDir, "trusted-public.pem").readText())
        return SingleKeyTrustedKeyProvider(TrustedPublicKey(trustedKeyId, publicKeyDer))
    }

    @Test
    fun `SHA-256 du corpus copie correspond a SHA256SUMS-txt`() {
        val sumsFile = File(corpusDir, "SHA256SUMS.txt")
        assertTrue(sumsFile.isFile, "SHA256SUMS.txt absent de $corpusDir")
        val recorded =
            sumsFile.readLines().filter { it.isNotBlank() }.associate { line ->
                val (digest, name) = line.split("  ", limit = 2)
                name to digest
            }
        for (entry in corpusDir.listFiles()!!) {
            if (entry.name == "SHA256SUMS.txt") continue
            val actual = MessageDigest.getInstance("SHA-256").digest(entry.readBytes())
                .joinToString("") { "%02x".format(it) }
            assertEquals(recorded[entry.name], actual, "SHA-256 divergent pour ${entry.name}")
        }
    }

    /**
     * Cas où Python et Kotlin rejettent tous deux la capsule (même verdict)
     * mais avec un message distinct, par construction et non par bug :
     * Python rejette `NaN` via le hook d'extension `parse_constant` de sa
     * bibliothèque `json` ("Constante JSON non finie interdite"), alors que
     * le parseur strict Kotlin de ce module (grammaire RFC 8259 pure) rejette
     * la même entrée en amont, simplement parce que `NaN` n'est pas un jeton
     * JSON valide ("Jeton JSON inattendu"). Le verdict (invalid) est
     * identique ; seul le texte diffère — ne pas exiger la même sous-chaîne.
     */
    private val knownMessageDivergence = setOf("nan-in-manifest")

    @Test
    fun `le corpus partage produit les memes verdicts que Python`() {
        val manifest = readExpectedManifest()
        val keyProvider = keyProviderFor(manifest)
        val verifier: CapsuleVerifier = ZipCapsuleVerifier()
        val cases = (manifest.get("cases") as JsonValue.Arr).items.map { it as JsonValue.Obj }

        val failures = mutableListOf<String>()
        for (case in cases) {
            val id = (case.get("id") as JsonValue.Str).value
            val fileName = (case.get("file") as JsonValue.Str).value
            val expected = (case.get("expected") as JsonValue.Str).value
            val expectedSubstring =
                (case.get("error_contains") as? JsonValue.Str)?.value.takeIf { id !in knownMessageDivergence }

            val result = verifier.verify(File(corpusDir, fileName), keyProvider)
            when (result) {
                is CapsuleVerificationResult.Valid -> {
                    if (expected != "valid") {
                        failures.add("$id: attendu '$expected', obtenu 'valid'")
                    }
                }
                is CapsuleVerificationResult.Invalid -> {
                    if (expected != "invalid") {
                        failures.add(
                            "$id: attendu '$expected', obtenu 'invalid' (${result.error.message})"
                        )
                    } else if (expectedSubstring != null &&
                        result.error.message?.contains(expectedSubstring) != true
                    ) {
                        failures.add(
                            "$id: message '${result.error.message}' ne contient pas '$expectedSubstring'"
                        )
                    }
                }
            }
        }

        assertEquals(emptyList(), failures, failures.joinToString("\n"))
    }
}

/** Extrait les octets DER d'une clé publique PEM (SubjectPublicKeyInfo). */
internal fun pemToDer(pem: String): ByteArray {
    val base64 =
        pem.lineSequence()
            .filterNot { it.startsWith("-----") }
            .joinToString("")
    return java.util.Base64.getDecoder().decode(base64)
}
