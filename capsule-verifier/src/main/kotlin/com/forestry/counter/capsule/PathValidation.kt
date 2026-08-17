package com.forestry.counter.capsule

private const val PAYLOAD_PREFIX = "payload/"
private val multiSlash = Regex("/+")

/**
 * Miroir exact de `_validate_relative_name` (Python, basée sur `PurePosixPath`).
 *
 * Reproduit fidèlement — y compris ses cas limites vérifiés empiriquement
 * contre CPython 3.12 — plutôt qu'une réimplémentation "de bon sens" qui
 * diverge silencieusement :
 * - les segments `.` sont éliminés silencieusement de la décomposition
 *   (comme `PurePosixPath("a/./b").parts == ('a','b')`), donc un `.` isolé
 *   n'est rejeté ni comme "non sûr" ni comme "non canonique" — un nom
 *   littéral `"."` est en fait accepté par les DEUX implémentations (constaté
 *   empiriquement, pas une décision de conception de ce module) ;
 * - les segments `..` restent présents dans la décomposition et déclenchent
 *   le rejet "non sûr" ;
 * - tout écart entre la forme reconstruite et le nom d'origine (séparateurs
 *   multiples, segment `.` explicite, slash de fin) déclenche "non canonique".
 */
internal fun validateRelativeName(name: String, prefixRequired: Boolean = false) {
    if (name.isEmpty() || "\\" in name || "\u0000" in name) {
        throw CapsuleVerificationError.ForbiddenPath(name)
    }
    val isAbsolute = name.startsWith("/")
    val body = if (isAbsolute) name.substring(1) else name
    val parts = if (body.isEmpty()) emptyList() else body.split(multiSlash).filter { it.isNotEmpty() && it != "." }
    if (isAbsolute || parts.any { it == ".." }) {
        throw CapsuleVerificationError.UnsafePath(name)
    }
    // PurePosixPath(".").as_posix() == "." (jamais la chaîne vide) — cas
    // particulier du "répertoire courant" à reproduire pour un verdict
    // identique à Python sur ce nom précis.
    val reconstructed = if (parts.isEmpty()) "." else parts.joinToString("/")
    if (reconstructed != name) {
        throw CapsuleVerificationError.NonCanonicalPath(name)
    }
    if (prefixRequired && !name.startsWith(PAYLOAD_PREFIX)) {
        throw CapsuleVerificationError.InvalidManifestField("Membre hors payload interdit : $name")
    }
}
