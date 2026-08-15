package com.forestry.counter.domain.location

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.*

/**
 * Gestionnaire de téléchargement de tuiles hors-ligne.
 *
 * Télécharge directement les tuiles raster via HTTP et les stocke dans
 * le système de fichiers local. Génère un style MapLibre qui référence
 * les tuiles locales via file://.
 */
class OfflineTileManager(private val context: Context) {

    companion object {
        private const val TAG = "OfflineTileManager"
        private const val TILES_DIR = "offline_tiles"
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 15_000
        /** Limite raisonnable pour éviter des téléchargements trop longs */
        private const val MAX_TILES_PER_DOWNLOAD = 6_000
        /** Nombre maximum de téléchargements parallèles (limite serveur + batterie) */
        private const val MAX_CONCURRENT_DOWNLOADS = 6
        /** Nombre de tentatives par tuile en cas d'échec (HTTP 5xx, timeout, IO) */
        private const val MAX_RETRY_ATTEMPTS = 3
        /** Base du backoff exponentiel : 500ms, 1000ms, 2000ms */
        private const val RETRY_BACKOFF_BASE_MS = 500L
        /**
         * User-Agent conforme à la politique OSM Tile Usage :
         * https://operations.osmfoundation.org/policies/tiles/
         * Doit identifier l'application + fournir un moyen de contact.
         */
        private const val USER_AGENT =
            "GeoSylva/2.3.0 (+https://geosylva.fr; contact: contact@geosylva.fr)"
    }

    data class DownloadProgress(
        val regionName: String,
        val completedResources: Long,
        val requiredResources: Long,
        val completedSize: Long,
        val isComplete: Boolean,
        val error: String? = null
    ) {
        val progressPct: Double
            get() = if (requiredResources > 0) completedResources.toDouble() / requiredResources * 100.0 else 0.0
    }

    /** Une zone hors-ligne nommée, téléchargée par l'utilisateur. */
    @Serializable
    data class RegionMeta(
        val id: String,
        val name: String,
        val latSouth: Double,
        val latNorth: Double,
        val lonWest: Double,
        val lonEast: Double,
        val minZoom: Int,
        val maxZoom: Int,
        val layerCount: Int,
        val sizeBytes: Long,
        val downloadedAtMs: Long,
    )

    @Serializable
    private data class RegionCatalog(val regions: List<RegionMeta> = emptyList())

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentJob: Job? = null

    /** Dossier racine des tuiles hors-ligne */
    private val tilesRoot: File
        get() = File(context.filesDir, TILES_DIR)

    private val catalogFile: File
        get() = File(context.filesDir, "offline_regions_catalog.json")

    private fun readCatalog(): RegionCatalog {
        return try {
            if (!catalogFile.exists()) RegionCatalog()
            else Json.decodeFromString(catalogFile.readText())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read offline regions catalog", e)
            RegionCatalog()
        }
    }

    private fun writeCatalog(catalog: RegionCatalog) {
        try {
            catalogFile.writeText(Json.encodeToString(catalog))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write offline regions catalog", e)
        }
    }

    /** Liste des zones hors-ligne téléchargées, les plus récentes en premier. */
    fun listRegions(): List<RegionMeta> = readCatalog().regions.sortedByDescending { it.downloadedAtMs }

    /** Supprime une zone hors-ligne précise (ses tuiles + son entrée de catalogue). */
    fun deleteRegion(regionId: String) {
        File(tilesRoot, regionId).deleteRecursively()
        val catalog = readCatalog()
        writeCatalog(catalog.copy(regions = catalog.regions.filterNot { it.id == regionId }))
    }

    // ─── Conversion coordonnées → indices de tuiles ───

    private fun lonToTileX(lon: Double, zoom: Int): Int {
        return ((lon + 180.0) / 360.0 * (1 shl zoom)).toInt().coerceIn(0, (1 shl zoom) - 1)
    }

    private fun latToTileY(lat: Double, zoom: Int): Int {
        val latRad = Math.toRadians(lat)
        val n = 1 shl zoom
        return ((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n).toInt().coerceIn(0, n - 1)
    }

    /**
     * Calcule le nombre total de tuiles pour les bounds et zooms donnés.
     */
    private fun countTiles(
        latSouth: Double, latNorth: Double,
        lonWest: Double, lonEast: Double,
        minZoom: Int, maxZoom: Int,
        layerCount: Int
    ): Long {
        var total = 0L
        for (z in minZoom..maxZoom) {
            val xMin = lonToTileX(lonWest, z)
            val xMax = lonToTileX(lonEast, z)
            val yMin = latToTileY(latNorth, z) // north = smaller y
            val yMax = latToTileY(latSouth, z)
            total += (xMax - xMin + 1).toLong() * (yMax - yMin + 1).toLong()
        }
        return total * layerCount
    }

    /**
     * Construit l'URL réelle d'une tuile à partir d'un template.
     */
    private fun buildTileUrl(template: String, z: Int, x: Int, y: Int): String {
        return template
            .replace("{z}", z.toString())
            .replace("{x}", x.toString())
            .replace("{y}", y.toString())
    }

    /**
     * Chemin local du fichier tuile, à l'intérieur du dossier de la région.
     */
    private fun tileFile(regionId: String, layerIndex: Int, z: Int, x: Int, y: Int): File {
        return File(tilesRoot, "$regionId/layer$layerIndex/$z/$x/$y.png")
    }

    /**
     * Télécharge une tuile unique. Retourne la taille en octets ou -1 si échec.
     */
    private fun downloadSingleTile(url: String, destFile: File): Long {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.instanceFollowRedirects = true

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                destFile.parentFile?.mkdirs()
                conn.inputStream.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                destFile.length()
            } else {
                Log.w(TAG, "HTTP ${conn.responseCode} for $url")
                -1L
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed tile: $url — ${e.message}")
            -1L
        } finally {
            try {
                (URL(url).openConnection() as? HttpURLConnection)?.disconnect()
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    /**
     * Télécharge une tuile avec retry + backoff exponentiel.
     * Retente sur échec (HTTP 5xx, timeout, IO) jusqu'à MAX_RETRY_ATTEMPTS fois.
     * Retourne la taille en octets ou -1 si toutes les tentatives échouent.
     */
    private suspend fun downloadTileWithRetry(url: String, destFile: File): Long {
        var attempt = 0
        var lastSize: Long
        while (attempt < MAX_RETRY_ATTEMPTS) {
            lastSize = downloadSingleTile(url, destFile)
            if (lastSize > 0) return lastSize
            attempt++
            if (attempt < MAX_RETRY_ATTEMPTS) {
                // Backoff exponentiel : 500ms, 1000ms, 2000ms
                delay(RETRY_BACKOFF_BASE_MS shl (attempt - 1))
            }
        }
        return -1L
    }

    /**
     * Démarre le téléchargement des tuiles pour la zone visible.
     *
     * @param name Nom de la région (pour l'affichage)
     * @param latSouth Latitude sud
     * @param latNorth Latitude nord
     * @param lonWest Longitude ouest
     * @param lonEast Longitude est
     * @param tileUrlTemplates Liste des templates URL de tuiles (ex: "https://.../{z}/{x}/{y}.png")
     * @param minZoom Zoom minimum
     * @param maxZoom Zoom maximum
     */
    fun downloadRegion(
        name: String,
        latSouth: Double,
        latNorth: Double,
        lonWest: Double,
        lonEast: Double,
        tileUrlTemplates: List<String>,
        minZoom: Int,
        maxZoom: Int
    ) {
        if (tileUrlTemplates.isEmpty()) {
            _downloadProgress.value = DownloadProgress(
                regionName = name, completedResources = 0, requiredResources = 0,
                completedSize = 0, isComplete = true,
                error = "Aucune source de tuiles pour cette couche"
            )
            return
        }

        val totalTiles = countTiles(latSouth, latNorth, lonWest, lonEast, minZoom, maxZoom, tileUrlTemplates.size)
        Log.i(TAG, "Download request: $name — $totalTiles tiles, zoom $minZoom..$maxZoom, ${tileUrlTemplates.size} layers")

        if (totalTiles > MAX_TILES_PER_DOWNLOAD) {
            _downloadProgress.value = DownloadProgress(
                regionName = name, completedResources = 0, requiredResources = totalTiles,
                completedSize = 0, isComplete = true,
                error = "Trop de tuiles ($totalTiles) — réduisez la zone ou le zoom"
            )
            return
        }

        // Annuler un téléchargement en cours
        currentJob?.cancel()

        _downloadProgress.value = DownloadProgress(
            regionName = name, completedResources = 0, requiredResources = totalTiles,
            completedSize = 0, isComplete = false
        )

        val regionId = "region_${System.currentTimeMillis()}"

        currentJob = scope.launch {
            val completedAtomic = AtomicLong(0)
            val totalSizeAtomic = AtomicLong(0)
            val errorsAtomic = AtomicInteger(0)
            val semaphore = Semaphore(MAX_CONCURRENT_DOWNLOADS)

            // 1. Compter les tuiles déjà en cache + construire la liste des téléchargements
            val pendingDownloads = mutableListOf<Triple<String, File, Int>>() // url, dest, layerIdx
            for (z in minZoom..maxZoom) {
                val xMin = lonToTileX(lonWest, z)
                val xMax = lonToTileX(lonEast, z)
                val yMin = latToTileY(latNorth, z)
                val yMax = latToTileY(latSouth, z)

                for (x in xMin..xMax) {
                    for (y in yMin..yMax) {
                        tileUrlTemplates.forEachIndexed { layerIdx, template ->
                            val dest = tileFile(regionId, layerIdx, z, x, y)
                            if (dest.exists()) {
                                completedAtomic.incrementAndGet()
                                totalSizeAtomic.addAndGet(dest.length())
                            } else {
                                val url = buildTileUrl(template, z, x, y)
                                pendingDownloads.add(Triple(url, dest, layerIdx))
                            }
                        }
                    }
                }
            }

            // 2. Lancer les téléchargements en parallèle (limite MAX_CONCURRENT_DOWNLOADS)
            coroutineScope {
                pendingDownloads.map { (url, dest, _) ->
                    async(Dispatchers.IO) {
                        semaphore.withPermit {
                            val size = downloadTileWithRetry(url, dest)
                            if (size > 0) {
                                totalSizeAtomic.addAndGet(size)
                            } else {
                                errorsAtomic.incrementAndGet()
                            }
                            val completed = completedAtomic.incrementAndGet()

                            // Mettre à jour le progrès toutes les 5 tuiles
                            if (completed % 5 == 0L || completed == totalTiles) {
                                _downloadProgress.value = DownloadProgress(
                                    regionName = name,
                                    completedResources = completed,
                                    requiredResources = totalTiles,
                                    completedSize = totalSizeAtomic.get(),
                                    isComplete = false
                                )
                            }
                        }
                    }
                }.awaitAll()
            }

            val errors = errorsAtomic.get()
            val errorMsg = if (errors > 0) "$errors tuiles en erreur" else null
            _downloadProgress.value = DownloadProgress(
                regionName = name,
                completedResources = completedAtomic.get(),
                requiredResources = totalTiles,
                completedSize = totalSizeAtomic.get(),
                isComplete = true,
                error = errorMsg
            )
            if (completedAtomic.get() > 0) {
                val catalog = readCatalog()
                writeCatalog(catalog.copy(regions = catalog.regions + RegionMeta(
                    id = regionId, name = name,
                    latSouth = latSouth, latNorth = latNorth, lonWest = lonWest, lonEast = lonEast,
                    minZoom = minZoom, maxZoom = maxZoom, layerCount = tileUrlTemplates.size,
                    sizeBytes = totalSizeAtomic.get(), downloadedAtMs = System.currentTimeMillis(),
                )))
            }
            Log.i(TAG, "Download complete: $name — ${completedAtomic.get()} tiles, ${totalSizeAtomic.get() / 1024} KB, $errors errors")
        }
    }

    /** Estimation (nombre de tuiles, taille approx.) avant de lancer un téléchargement. */
    fun estimateDownload(
        latSouth: Double, latNorth: Double, lonWest: Double, lonEast: Double,
        minZoom: Int, maxZoom: Int, layerCount: Int,
    ): Pair<Long, Long> {
        val tiles = countTiles(latSouth, latNorth, lonWest, lonEast, minZoom, maxZoom, layerCount)
        // ~18 Ko/tuile raster en moyenne (estimation grossière, affinée par le téléchargement réel).
        return tiles to tiles * 18_000L
    }

    /**
     * Génère un style MapLibre JSON qui référence toutes les zones hors-ligne
     * téléchargées (une par une, empilées — les zones sont choisies par
     * l'utilisateur et ne se chevauchent en général pas). Pas de `glyphs` :
     * les couches raster hors-ligne n'affichent pas de texte, et pointer vers
     * une police distante casserait l'usage vraiment hors-ligne.
     */
    fun buildOfflineStyle(@Suppress("UNUSED_PARAMETER") layerCount: Int = 1): String {
        val root = tilesRoot.absolutePath
        val sources = StringBuilder()
        val layers = StringBuilder()
        var first = true

        listRegions().forEach { region ->
            for (i in 0 until region.layerCount) {
                if (!first) { sources.append(","); layers.append(",") }
                first = false
                val sourceId = "${region.id}_layer$i"
                sources.append(
                    """"$sourceId":{"type":"raster","tiles":["file://$root/${region.id}/layer$i/{z}/{x}/{y}.png"],"tileSize":256,"maxzoom":${region.maxZoom},"attribution":"Tuiles mises en cache depuis leur source d'origine (IGN Géoportail / OpenStreetMap / etc.) — voir licences respectives"}"""
                )
                val opacity = if (i == 0) "" else ""","paint":{"raster-opacity":0.7}"""
                layers.append("""{"id":"$sourceId","type":"raster","source":"$sourceId"$opacity}""")
            }
        }

        return """{"version":8,"name":"Offline Local","sources":{$sources},"layers":[$layers]}"""
    }

    /**
     * Vérifie si des tuiles hors-ligne existent.
     */
    fun hasOfflineTiles(): Boolean = listRegions().isNotEmpty()

    /**
     * Compte le nombre de tuiles en cache et la taille totale, toutes zones confondues.
     */
    fun cacheStats(): Pair<Int, Long> {
        if (!tilesRoot.exists()) return 0 to 0L
        var count = 0
        var size = 0L
        tilesRoot.walkTopDown().filter { it.extension == "png" }.forEach {
            count++
            size += it.length()
        }
        return count to size
    }

    /**
     * Supprime toutes les zones hors-ligne (tuiles + catalogue).
     */
    fun clearCache() {
        tilesRoot.deleteRecursively()
        writeCatalog(RegionCatalog())
        Log.i(TAG, "Offline tile cache cleared")
    }

    /**
     * Nombre de couches (layers) de la zone hors-ligne la plus récente —
     * conservé pour compatibilité de l'appelant existant (MapScreen).
     */
    fun downloadedLayerCount(): Int = listRegions().firstOrNull()?.layerCount ?: 0

    fun clearProgress() {
        _downloadProgress.value = null
    }
}
