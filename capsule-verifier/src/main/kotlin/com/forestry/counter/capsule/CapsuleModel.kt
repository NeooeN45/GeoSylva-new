package com.forestry.counter.capsule

/** Budgets défensifs, miroir de `gsie_execution_kit.capsule.CapsuleLimits` (Python). */
data class CapsulePolicy(
    val maxFiles: Int = 512,
    val maxTotalUncompressedBytes: Long = 512L * 1024 * 1024,
    val maxMemberUncompressedBytes: Long = 256L * 1024 * 1024,
    val maxMetadataBytes: Long = 2L * 1024 * 1024,
    val maxCompressionRatio: Double = 200.0,
)

/** Clé publique de confiance, identifiée par son empreinte (`sha256:<hex>`). */
class TrustedPublicKey(val keyId: String, val publicKeyDer: ByteArray)

/**
 * Source de confiance externe pour les clés publiques — jamais dérivée du
 * contenu de la capsule elle-même (voir ADR-008 : "aucune auto-approbation
 * d'une clé contenue dans l'archive").
 */
interface TrustedKeyProvider {
    fun find(keyId: String): TrustedPublicKey?
}

/** Implémentation triviale à une seule clé — équivalent direct du chemin de clé publique unique côté Python. */
class SingleKeyTrustedKeyProvider(private val key: TrustedPublicKey) : TrustedKeyProvider {
    override fun find(keyId: String): TrustedPublicKey? = if (keyId == key.keyId) key else null
}

data class VerifiedFileEntry(val path: String, val size: Long, val sha256: String)

data class VerifiedCapsuleManifest(
    val capsuleId: String,
    val schemaVersion: String,
    val createdAt: String,
    val validUntil: String?,
    val keyId: String,
    val territoryId: String,
    val files: List<VerifiedFileEntry>,
)

sealed interface CapsuleVerificationResult {
    data class Valid(val manifest: VerifiedCapsuleManifest) : CapsuleVerificationResult

    data class Invalid(val error: CapsuleVerificationError) : CapsuleVerificationResult
}

/**
 * Erreurs typées de vérification — miroir de `CapsuleError` (Python), mais
 * décomposé en sous-types actionnables plutôt qu'une seule classe générique.
 * Aucune de ces erreurs n'est jamais convertie silencieusement en succès ou
 * en résultat vide (RFC-0014 / ADR-008).
 */
sealed class CapsuleVerificationError(message: String) : Exception(message) {
    class MalformedArchive(message: String) : CapsuleVerificationError(message)

    class MissingMember(name: String) : CapsuleVerificationError("Membre obligatoire absent : $name")

    class UnexpectedMembers(names: List<String>) :
        CapsuleVerificationError("Membres non déclarés : ${names.joinToString(", ")}")

    class MissingDeclaredMembers(names: List<String>) :
        CapsuleVerificationError("Membres manquants : ${names.joinToString(", ")}")

    class DuplicateMembers(message: String) : CapsuleVerificationError(message)

    class UnsafePath(name: String) : CapsuleVerificationError("Chemin d'archive non sûr : $name")

    class ForbiddenPath(name: String) : CapsuleVerificationError("Chemin d'archive interdit : $name")

    class NonCanonicalPath(name: String) : CapsuleVerificationError("Chemin d'archive non canonique : $name")

    class EncryptedMember(name: String) :
        CapsuleVerificationError("Membre ZIP chiffré non supporté : $name")

    class BudgetExceeded(message: String) : CapsuleVerificationError(message)

    class UnsupportedSchemaVersion(version: String?) :
        CapsuleVerificationError("Version majeure de manifeste non supportée : $version")

    class InvalidManifestField(message: String) : CapsuleVerificationError(message)

    class CapsuleIdMismatch :
        CapsuleVerificationError("capsule_id ne correspond pas au contenu du manifeste")

    class UnsupportedSignatureVersion(message: String) : CapsuleVerificationError(message)

    class KeyIdMismatch(message: String) : CapsuleVerificationError(message)

    class UntrustedKey :
        CapsuleVerificationError("La capsule n'est pas signée par la clé publique approuvée")

    class InvalidSignature :
        CapsuleVerificationError("Signature cryptographique invalide")

    class SizeMismatch(path: String) : CapsuleVerificationError("Taille divergente : $path")

    class TamperedPayload(path: String) : CapsuleVerificationError("Empreinte divergente : $path")

    class Expired(message: String) : CapsuleVerificationError(message)

    class MalformedJson(message: String) : CapsuleVerificationError(message)

    class NotCanonicalJson(message: String) : CapsuleVerificationError(message)

    class UnsupportedNumberFormat(message: String) : CapsuleVerificationError(message)
}
