package com.forestry.counter.capsule

import java.security.MessageDigest
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer

/**
 * Vérification Ed25519 via Bouncy Castle plutôt que `java.security` natif :
 * le support Ed25519 de la plateforme Android n'est garanti qu'à partir de
 * l'API 33, incompatible avec le `minSdk 26` de l'application — Bouncy Castle
 * donne un comportement identique sur toutes les API cibles (voir décision
 * documentée dans le rapport final de cette mission).
 */
internal object Ed25519Support {
    /**
     * Calcule l'identifiant de clé, miroir exact de `_key_id` (Python) :
     * `sha256:` + SHA-256 hexadécimal des octets DER SubjectPublicKeyInfo.
     */
    fun keyId(publicKeyDer: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(publicKeyDer)
        return "sha256:" + digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Extrait les 32 octets bruts de clé publique Ed25519 depuis leur
     * encodage DER SubjectPublicKeyInfo (format produit par
     * `serialization.PublicFormat.SubjectPublicKeyInfo` côté Python).
     */
    fun parsePublicKeyDer(der: ByteArray): Ed25519PublicKeyParameters =
        try {
            val keyInfo = org.bouncycastle.asn1.x509.SubjectPublicKeyInfo.getInstance(der)
            Ed25519PublicKeyParameters(keyInfo.publicKeyData.bytes, 0)
        } catch (exc: Exception) {
            throw IllegalArgumentException("Clé publique Ed25519 (DER) illisible : ${exc.message}", exc)
        }

    fun verify(publicKey: Ed25519PublicKeyParameters, message: ByteArray, signature: ByteArray): Boolean {
        if (signature.size != 64) return false
        val verifier = Ed25519Signer()
        verifier.init(false, publicKey)
        verifier.update(message, 0, message.size)
        return verifier.verifySignature(signature)
    }
}
