package com.forestry.counter.capsule

import com.forestry.counter.capsule.json.CanonicalJson
import com.forestry.counter.capsule.json.JsonValue
import com.forestry.counter.capsule.json.StrictJsonError
import com.forestry.counter.capsule.json.StrictJsonParser
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Base64
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile

private const val MANIFEST_NAME = "manifest.json"
private const val SIGNATURE_NAME = "signature.json"
private const val PAYLOAD_PREFIX = "payload/"
private const val SIGNATURE_ALGORITHM = "Ed25519"
private const val SIGNATURE_SCHEMA_VERSION = "1.0.0"
private val SHA256_PATTERN = Regex("^[0-9a-f]{64}$")

/** Vérifie hors ligne une capsule territoriale `.gsiecap` — jamais d'extraction vers la zone active. */
interface CapsuleVerifier {
    fun verify(
        path: File,
        keyProvider: TrustedKeyProvider,
        policy: CapsulePolicy = CapsulePolicy(),
        now: Instant = Instant.now(),
    ): CapsuleVerificationResult
}

/**
 * Implémentation de référence, miroir pas à pas de
 * `gsie_execution_kit.capsule.verify_capsule` (Python) — même ordre de
 * contrôles, mêmes messages d'erreur, mêmes budgets par défaut. Voir
 * `docs/COMPATIBILITE_KOTLIN.md` pour le détail de chaque correspondance et
 * les limites connues.
 */
class ZipCapsuleVerifier : CapsuleVerifier {
    override fun verify(
        path: File,
        keyProvider: TrustedKeyProvider,
        policy: CapsulePolicy,
        now: Instant,
    ): CapsuleVerificationResult =
        try {
            CapsuleVerificationResult.Valid(verifyOrThrow(path, keyProvider, policy, now))
        } catch (error: CapsuleVerificationError) {
            CapsuleVerificationResult.Invalid(error)
        }

    private fun verifyOrThrow(
        path: File,
        keyProvider: TrustedKeyProvider,
        policy: CapsulePolicy,
        now: Instant,
    ): VerifiedCapsuleManifest {
        if (!path.isFile) {
            throw CapsuleVerificationError.MalformedArchive("Capsule introuvable : $path")
        }

        val zip =
            try {
                ZipFile.builder().setFile(path).get()
            } catch (exc: Exception) {
                throw CapsuleVerificationError.MalformedArchive("Conteneur ZIP invalide : ${exc.message}")
            }

        zip.use { archive ->
            val entries = archive.entries.toList()
            val names = entries.map { it.name }
            if (names.size != names.toSet().size) {
                throw CapsuleVerificationError.DuplicateMembers("L'archive contient des membres dupliqués")
            }
            validateArchiveBudgets(entries, policy)

            val manifestBytes = readMetadata(archive, entries, MANIFEST_NAME, policy)
            val signatureBytes = readMetadata(archive, entries, SIGNATURE_NAME, policy)

            val manifestValue = parseStrictOrThrow(manifestBytes)
            val signatureValue = parseStrictOrThrow(signatureBytes)
            val manifest =
                manifestValue as? JsonValue.Obj
                    ?: throw CapsuleVerificationError.MalformedJson(
                        "Le manifeste et la signature doivent être des objets JSON"
                    )
            val signatureDocument =
                signatureValue as? JsonValue.Obj
                    ?: throw CapsuleVerificationError.MalformedJson(
                        "Le manifeste et la signature doivent être des objets JSON"
                    )

            if (!CanonicalJson.encode(manifest).contentEquals(manifestBytes)) {
                throw CapsuleVerificationError.NotCanonicalJson(
                    "Le manifeste n'utilise pas la représentation canonique"
                )
            }
            if (!CanonicalJson.encode(signatureDocument).contentEquals(signatureBytes)) {
                throw CapsuleVerificationError.NotCanonicalJson(
                    "Le document de signature n'est pas canonique"
                )
            }

            val files = validateManifestStructure(manifest)

            val manifestWithoutId = JsonValue.Obj(manifest.entries.filter { it.first != "capsule_id" })
            val observedCapsuleId = (manifest.get("capsule_id") as? JsonValue.Str)?.value
            if (observedCapsuleId != manifestId(manifestWithoutId)) {
                throw CapsuleVerificationError.CapsuleIdMismatch()
            }

            val manifestSignature =
                manifest.get("signature") as? JsonValue.Obj
                    ?: throw CapsuleVerificationError.InvalidManifestField("Métadonnée de signature absente du manifeste")
            if ((signatureDocument.get("schema_version") as? JsonValue.Str)?.value != SIGNATURE_SCHEMA_VERSION) {
                throw CapsuleVerificationError.UnsupportedSignatureVersion(
                    "Version du document de signature non supportée"
                )
            }
            if ((signatureDocument.get("algorithm") as? JsonValue.Str)?.value != SIGNATURE_ALGORITHM) {
                throw CapsuleVerificationError.UnsupportedSignatureVersion(
                    "Algorithme du document de signature non supporté"
                )
            }
            val signedKeyId = (signatureDocument.get("key_id") as? JsonValue.Str)?.value
            val manifestKeyId = (manifestSignature.get("key_id") as? JsonValue.Str)?.value
            if (signedKeyId != manifestKeyId) {
                throw CapsuleVerificationError.KeyIdMismatch(
                    "Les identifiants de clé du manifeste divergent"
                )
            }
            val trustedKey = signedKeyId?.let(keyProvider::find) ?: throw CapsuleVerificationError.UntrustedKey()

            val encodedSignature =
                (signatureDocument.get("signature_base64") as? JsonValue.Str)?.value
                    ?: throw CapsuleVerificationError.InvalidManifestField("Signature Base64 absente")
            val decodedSignature =
                try {
                    Base64.getDecoder().decode(encodedSignature)
                } catch (exc: IllegalArgumentException) {
                    throw CapsuleVerificationError.InvalidManifestField("Signature Base64 invalide")
                }
            val publicKey = Ed25519Support.parsePublicKeyDer(trustedKey.publicKeyDer)
            if (!Ed25519Support.verify(publicKey, manifestBytes, decodedSignature)) {
                throw CapsuleVerificationError.InvalidSignature()
            }

            val expectedNames =
                (setOf(MANIFEST_NAME, SIGNATURE_NAME) + files.map { "$PAYLOAD_PREFIX${it.path}" }).toSet()
            val observedNames = names.toSet()
            val unexpected = (observedNames - expectedNames).sorted()
            val missing = (expectedNames - observedNames).sorted()
            if (unexpected.isNotEmpty()) {
                throw CapsuleVerificationError.UnexpectedMembers(unexpected)
            }
            if (missing.isNotEmpty()) {
                throw CapsuleVerificationError.MissingDeclaredMembers(missing)
            }

            val entriesByName = entries.associateBy { it.name }
            val verifiedFiles = mutableListOf<VerifiedFileEntry>()
            for (entry in files) {
                val archiveName = "$PAYLOAD_PREFIX${entry.path}"
                validateRelativeName(archiveName, prefixRequired = true)
                val zipEntry = entriesByName.getValue(archiveName)
                if (zipEntry.size != entry.size) {
                    throw CapsuleVerificationError.SizeMismatch(entry.path)
                }
                val digest = MessageDigest.getInstance("SHA-256")
                var observedSize = 0L
                archive.getInputStream(zipEntry).use { stream ->
                    val buffer = ByteArray(1024 * 1024)
                    while (true) {
                        val read = stream.read(buffer)
                        if (read < 0) break
                        digest.update(buffer, 0, read)
                        observedSize += read
                        // Garde-fou streaming : un ZIP avec un en-tête
                        // central mensonger pourrait faire décompresser bien
                        // plus que la taille déclarée. On impose le plafond
                        // pendant la lecture, pas seulement a posteriori.
                        if (observedSize > policy.maxMemberUncompressedBytes) {
                            throw CapsuleVerificationError.BudgetExceeded(
                                "Membre ${entry.path} dépasse le plafond en streaming : $observedSize octets"
                            )
                        }
                    }
                }
                val observedDigest = digest.digest().joinToString("") { "%02x".format(it) }
                if (observedSize != entry.size || observedDigest != entry.sha256) {
                    throw CapsuleVerificationError.TamperedPayload(entry.path)
                }
                verifiedFiles.add(VerifiedFileEntry(entry.path, observedSize, observedDigest))
            }

            val validUntil = (manifest.get("valid_until") as? JsonValue.Str)?.value
            if (validUntil != null) {
                val expiration =
                    try {
                        Instant.parse(validUntil)
                    } catch (exc: DateTimeParseException) {
                        throw CapsuleVerificationError.InvalidManifestField(
                            "valid_until n'est pas une date ISO 8601 valide"
                        )
                    }
                if (now.isAfter(expiration)) {
                    throw CapsuleVerificationError.Expired("Capsule expirée depuis $validUntil")
                }
            }

            val territory =
                manifest.get("territory") as? JsonValue.Obj
                    ?: throw CapsuleVerificationError.InvalidManifestField("Résumé territorial absent du manifeste")
            val territoryId =
                (territory.get("territory_id") as? JsonValue.Str)?.value
                    ?: throw CapsuleVerificationError.InvalidManifestField("territory_id absent du manifeste")
            val capsuleId =
                (manifest.get("capsule_id") as? JsonValue.Str)?.value
                    ?: throw CapsuleVerificationError.InvalidManifestField("capsule_id absent du manifeste")
            val schemaVersion =
                (manifest.get("schema_version") as? JsonValue.Str)?.value
                    ?: throw CapsuleVerificationError.InvalidManifestField("schema_version absent du manifeste")
            val createdAt =
                (manifest.get("created_at") as? JsonValue.Str)?.value
                    ?: throw CapsuleVerificationError.InvalidManifestField("created_at absent du manifeste")

            return VerifiedCapsuleManifest(
                capsuleId = capsuleId,
                schemaVersion = schemaVersion,
                createdAt = createdAt,
                validUntil = validUntil,
                keyId = trustedKey.keyId,
                territoryId = territoryId,
                files = verifiedFiles,
            )
        }
    }

    private fun parseStrictOrThrow(bytes: ByteArray): JsonValue =
        try {
            StrictJsonParser.parse(bytes)
        } catch (exc: StrictJsonError) {
            throw CapsuleVerificationError.MalformedJson(exc.message ?: "JSON invalide")
        }

    private fun validateArchiveBudgets(
        entries: List<ZipArchiveEntry>,
        policy: CapsulePolicy,
    ) {
        if (entries.size > policy.maxFiles + 2) {
            throw CapsuleVerificationError.BudgetExceeded("Archive trop riche en membres : ${entries.size}")
        }
        var totalSize = 0L
        for (entry in entries) {
            validateRelativeName(entry.name)
            if (entry.generalPurposeBit.usesEncryption()) {
                throw CapsuleVerificationError.EncryptedMember(entry.name)
            }
            if (entry.size > policy.maxMemberUncompressedBytes) {
                throw CapsuleVerificationError.BudgetExceeded("Membre trop volumineux : ${entry.name}")
            }
            totalSize += entry.size
            if (totalSize > policy.maxTotalUncompressedBytes + 2 * policy.maxMetadataBytes) {
                throw CapsuleVerificationError.BudgetExceeded("Archive trop volumineuse après décompression")
            }
            val compressedSize = entry.compressedSize.coerceAtLeast(1)
            val ratio = entry.size.toDouble() / compressedSize.toDouble()
            if (ratio > policy.maxCompressionRatio) {
                throw CapsuleVerificationError.BudgetExceeded(
                    "Ratio de compression suspect pour ${entry.name} : " +
                        String.format(java.util.Locale.ROOT, "%.1f", ratio)
                )
            }
        }
    }

    private fun readMetadata(
        archive: ZipFile,
        entries: List<ZipArchiveEntry>,
        name: String,
        policy: CapsulePolicy,
    ): ByteArray {
        val entry =
            entries.firstOrNull { it.name == name }
                ?: throw CapsuleVerificationError.MissingMember(name)
        if (entry.size > policy.maxMetadataBytes) {
            throw CapsuleVerificationError.BudgetExceeded("Métadonnée trop volumineuse : $name")
        }
        // Lecture bornée en streaming : entry.size vient de l'en-tête ZIP
        // (potentiellement mensonger). On lit avec un plafond dur pour
        // éviter qu'un membre malicieux ne remplisse la mémoire avant la
        // vérification a posteriori.
        return archive.getInputStream(entry).use { stream ->
            readBoundedBytes(stream, policy.maxMetadataBytes, name)
        }
    }

    /**
     * Lit un flux en imposant un plafond dur sur le cumul d'octets lus.
     * Garde-fou streaming contre les ZIP à en-tête central mensonger : un
     * membre dont `ZipArchiveEntry.size` est sous le plafond mais dont le
     * contenu réel le dépasse est rejeté pendant la lecture, pas a posteriori.
     *
     * @throws CapsuleVerificationError.BudgetExceeded si le flux dépasse [maxBytes]
     */
    private fun readBoundedBytes(
        stream: java.io.InputStream,
        maxBytes: Long,
        name: String,
    ): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            val read = stream.read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) {
                throw CapsuleVerificationError.BudgetExceeded(
                    "Métadonnée $name dépasse le plafond en streaming : $total octets (max $maxBytes)"
                )
            }
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }

    private fun validateManifestStructure(manifest: JsonValue.Obj): List<VerifiedFileEntry> {
        val schemaVersion = (manifest.get("schema_version") as? JsonValue.Str)?.value
        if (schemaVersion == null || schemaVersion.substringBefore('.') != "1") {
            throw CapsuleVerificationError.UnsupportedSchemaVersion(schemaVersion)
        }
        val capsuleId = (manifest.get("capsule_id") as? JsonValue.Str)?.value
        if (capsuleId == null || !capsuleId.startsWith("cap_")) {
            throw CapsuleVerificationError.InvalidManifestField("capsule_id absent ou invalide")
        }
        if (manifest.get("created_at") !is JsonValue.Str) {
            throw CapsuleVerificationError.InvalidManifestField("created_at doit être une date ISO 8601")
        }

        val territory = manifest.get("territory") as? JsonValue.Obj
        if (territory == null || territory.get("territory_id") !is JsonValue.Str) {
            throw CapsuleVerificationError.InvalidManifestField("Résumé territorial absent ou invalide")
        }
        val signature = manifest.get("signature") as? JsonValue.Obj
        if (signature == null) {
            throw CapsuleVerificationError.InvalidManifestField("Métadonnée de signature absente du manifeste")
        }
        if ((signature.get("algorithm") as? JsonValue.Str)?.value != SIGNATURE_ALGORITHM) {
            throw CapsuleVerificationError.InvalidManifestField("Algorithme de signature non supporté")
        }
        if (signature.get("key_id") !is JsonValue.Str) {
            throw CapsuleVerificationError.InvalidManifestField("Identifiant de clé absent du manifeste")
        }

        val filesValue = manifest.get("files") as? JsonValue.Arr
        if (filesValue == null || filesValue.items.isEmpty()) {
            throw CapsuleVerificationError.InvalidManifestField("Liste de fichiers absente ou vide")
        }
        val seenPaths = HashSet<String>()
        val normalized = mutableListOf<VerifiedFileEntry>()
        for (rawEntry in filesValue.items) {
            val entryObj =
                rawEntry as? JsonValue.Obj
                    ?: throw CapsuleVerificationError.InvalidManifestField(
                        "Entrée de fichier invalide dans le manifeste"
                    )
            val entryPath =
                (entryObj.get("path") as? JsonValue.Str)?.value
                    ?: throw CapsuleVerificationError.InvalidManifestField("Chemin de fichier absent du manifeste")
            validateRelativeName(entryPath)
            if (!seenPaths.add(entryPath)) {
                throw CapsuleVerificationError.InvalidManifestField("Chemin dupliqué dans le manifeste : $entryPath")
            }
            val sizeValue = entryObj.get("size")
            val size =
                (sizeValue as? JsonValue.JsonInteger)?.value?.takeIf { it.signum() >= 0 }
                    ?: throw CapsuleVerificationError.InvalidManifestField("Taille invalide dans le manifeste : $entryPath")
            val sha256 =
                (entryObj.get("sha256") as? JsonValue.Str)?.value?.takeIf { SHA256_PATTERN.matches(it) }
                    ?: throw CapsuleVerificationError.InvalidManifestField(
                        "SHA-256 invalide dans le manifeste : $entryPath"
                    )
            normalized.add(VerifiedFileEntry(entryPath, size.toLongExactOrThrow(entryPath), sha256))
        }
        if (normalized.none { it.path == "territory.json" }) {
            throw CapsuleVerificationError.InvalidManifestField("territory.json n'est pas déclaré dans le manifeste")
        }
        return normalized
    }

    private fun manifestId(manifestWithoutId: JsonValue.Obj): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(CanonicalJson.encode(manifestWithoutId))
        return "cap_" + digest.joinToString("") { "%02x".format(it) }.substring(0, 32)
    }

    private fun BigInteger.toLongExactOrThrow(path: String): Long =
        try {
            this.longValueExact()
        } catch (exc: ArithmeticException) {
            throw CapsuleVerificationError.InvalidManifestField("Taille invalide dans le manifeste : $path")
        }
}
