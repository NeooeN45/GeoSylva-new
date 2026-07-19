package com.forestry.counter.domain.usecase.pack

import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Télécharge avec reprise (`Range`/`If-Range`/`ETag`) et vérifie le SHA-256 du
 * fichier complet une fois assemblé.
 *
 * Fonction pure (aucune dépendance Android/Context) — extraite de
 * [PackManager] pour rester testable directement sur JVM, sans Robolectric ni
 * émulateur (voir `PackManagerDownloadTest`, basé sur MockWebServer).
 *
 * Le fragment déjà présent (`destination`, laissé par une tentative
 * interrompue) n'est réutilisé que si son `ETag` d'origine est connu et que
 * le serveur répond `206 Partial Content` avec ce même `ETag` en `If-Range` —
 * sinon on repart de zéro : mélanger deux versions d'un fichier changé côté
 * serveur produirait un pack corrompu sans que le checksum final ne le
 * détecte forcément avant la toute fin.
 */
internal fun downloadWithChecksum(
    url: URL,
    destination: File,
    expectedSha256: String,
    onProgress: (Float) -> Unit = {}
) {
    val etagFile = File(destination.parentFile, "${destination.name}.etag")
    var resumeFrom = if (destination.isFile) destination.length() else 0L
    var storedEtag = etagFile.takeIf { it.isFile }?.readText()?.trim()?.takeIf { it.isNotEmpty() }
    if (resumeFrom > 0 && storedEtag == null) {
        destination.delete()
        resumeFrom = 0L
    }

    var attempt = 0
    while (true) {
        attempt++
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 120_000
            requestMethod = "GET"
            instanceFollowRedirects = true
            if (resumeFrom > 0) {
                setRequestProperty("Range", "bytes=$resumeFrom-")
                storedEtag?.let { setRequestProperty("If-Range", it) }
            }
        }
        try {
            val code = connection.responseCode
            val resuming = when (code) {
                HttpURLConnection.HTTP_PARTIAL -> resumeFrom > 0
                HttpURLConnection.HTTP_OK -> false
                else -> error("Serveur packs HTTP $code")
            }
            if (!resuming && resumeFrom > 0) {
                // Reprise refusée ou invalidée (contenu changé) : repart de zéro.
                destination.delete()
                resumeFrom = 0L
            }

            val remaining = connection.contentLengthLong
            val total = if (resuming && remaining >= 0) remaining + resumeFrom else remaining
            if (total > 0) {
                checkPackSize(total, url.path)
                val available = destination.parentFile?.usableSpace ?: Long.MAX_VALUE
                checkDiskSpace(available, total - resumeFrom, url.path)
            }

            val digest = MessageDigest.getInstance("SHA-256")
            if (resuming) {
                destination.inputStream().use { existing ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var read: Int
                    while (existing.read(buffer).also { read = it } >= 0) digest.update(buffer, 0, read)
                }
            }
            connection.inputStream.use { input ->
                FileOutputStream(destination, resuming).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var readTotal = resumeFrom
                    var read: Int
                    while (input.read(buffer).also { read = it } >= 0) {
                        if (read == 0) continue
                        output.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                        readTotal += read
                        // Garde-fou streaming : un serveur compromis peut
                        // mentir sur Content-Length (ou l'omettre) et streamer
                        // indéfiniment. On vérifie la taille réelle lue à
                        // chaque itération, indépendamment de l'en-tête.
                        checkPackSize(readTotal, url.path)
                        if (total > 0) onProgress((readTotal.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }

            val actualSha256 = digest.digest().joinToString("") { "%02x".format(it) }
            if (actualSha256 != expectedSha256) {
                if (resuming && attempt == 1) {
                    // Ne retente qu'une fois en repartant de zéro — évite de
                    // boucler indéfiniment si le serveur renvoie toujours un
                    // contenu incohérent avec son propre ETag.
                    destination.delete()
                    etagFile.delete()
                    resumeFrom = 0L
                    storedEtag = null
                    continue
                }
                // Fichier assemblé corrompu et non rejouable : ne jamais le
                // laisser en place comme base de reprise pour une prochaine
                // tentative (installPack ne le supprimera pas lui-même
                // puisque cette erreur n'est pas une IOException réseau).
                destination.delete()
                etagFile.delete()
                error("Checksum SHA-256 invalide pour ${url.path}")
            }
            connection.getHeaderField("ETag")?.let { etagFile.writeText(it) } ?: etagFile.delete()
            return
        } finally {
            connection.disconnect()
        }
    }
}

private const val DEFAULT_BUFFER_SIZE = 8 * 1024

/**
 * Taille maximale d'un pack GeoSylva (512 Mo). Garde-fou contre les
 * serveurs compromis qui mentent sur `Content-Length` ou l'omettent et
 * streament indéfiniment pour remplir le disque avant l'échec du checksum.
 * Un pack régional réel fait quelques dizaines de Mo — cette limite est
 * volontairement généreuse tout en restant un plafond dur.
 */
private const val MAX_PACK_BYTES = 512L * 1024 * 1024

/**
 * Vérifie que la taille du pack reste sous le plafond dur
 * [MAX_PACK_BYTES]. Appelée à la fois sur le `Content-Length` déclaré
 * (avant écriture) et sur le cumul des octets réellement lus (en
 * streaming) pour ne pas dépendre de l'en-tête serveur.
 *
 * @throws IllegalStateException si [bytes] > [MAX_PACK_BYTES]
 */
internal fun checkPackSize(bytes: Long, path: String) {
    check(bytes <= MAX_PACK_BYTES) {
        "Pack $path trop volumineux : $bytes octets (max $MAX_PACK_BYTES)"
    }
}

/**
 * Vérifie l'espace disque disponible avant écriture. Extraite de
 * [downloadWithChecksum] pour rester testable unitairement sans dépendre
 * d'un vrai système de fichiers (MockWebServer écrase le Content-Length
 * déclaré par la taille réelle du body, rendant la branche impossible à
 * déclencher via un serveur mock).
 *
 * @throws IllegalStateException si [available] < [required]
 */
internal fun checkDiskSpace(available: Long, required: Long, path: String) {
    check(available >= required) {
        "Espace disque insuffisant pour $path ($required octets requis)"
    }
}

/**
 * Vérifie que l'URL utilise le schéma HTTPS. Extraite de
 * [downloadWithChecksum] pour rester testable unitairement (MockWebServer
 * tourne en HTTP, ce qui rendrait impossible le déclenchement de la
 * branche HTTPS via un téléchargement mocké). Appelée par
 * [PackManager.installPack] avant de déléguer à [downloadWithChecksum].
 *
 * Sécurité : le SHA-256 protège l'intégrité mais pas la
 * confidentialité/authenticité de la source — un MITM sur HTTP pourrait
 * observer le flux et tenter une attaque par prégénération si le contenu
 * est prévisible. HTTPS est obligatoire.
 *
 * @throws IllegalArgumentException si le schéma n'est pas HTTPS
 */
internal fun checkHttpsScheme(url: URL) {
    require(url.protocol.equals("https", ignoreCase = true)) {
        "Schéma ${url.protocol} interdit — HTTPS obligatoire pour ${url.path}"
    }
}
