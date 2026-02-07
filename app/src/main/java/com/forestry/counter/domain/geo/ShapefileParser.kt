package com.forestry.counter.domain.geo

import android.util.Log
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.zip.ZipInputStream

/**
 * Attributs d'une parcelle forestière extraits du .dbf.
 */
data class ParcelAttributes(
    val nom: String,            // NOM — numéro de parcelle
    val surface: Double,        // SURFACE_HA
    val forestName: String,     // llib_frt — nom de la forêt
    val parcelCode: String,     // ccod_prf — code parcelle
    val district: String,       // qdis_prf
    val allAttributes: Map<String, String>
)

/**
 * Feature = géométrie + attributs.
 */
data class ShapeFeature(
    val rings: List<List<Pair<Double, Double>>>,  // anneaux du polygone (lon, lat WGS84)
    val attributes: ParcelAttributes
)

/**
 * Résultat complet du parsing d'un shapefile.
 */
data class ShapefileResult(
    val features: List<ShapeFeature>,
    val forestNames: Set<String>,
    val isLambert93: Boolean
)

/**
 * Parser léger de Shapefile (.shp + .dbf) depuis un flux ZIP.
 * Gère la reprojection Lambert 93 → WGS84.
 */
object ShapefileParser {

    private const val TAG = "ShapefileParser"

    // ── Types de forme Shapefile ──
    private const val SHAPE_POLYGON = 5
    private const val SHAPE_POLYGON_M = 25
    private const val SHAPE_POLYGON_Z = 15
    private val POLYGON_TYPES = setOf(SHAPE_POLYGON, SHAPE_POLYGON_M, SHAPE_POLYGON_Z)

    /**
     * Parse un fichier ZIP contenant un shapefile (.shp + .dbf + .prj).
     *
     * @param inputStream Le flux du fichier ZIP
     * @return ShapefileResult ou null si le parsing échoue
     */
    fun parseZip(inputStream: InputStream): ShapefileResult? {
        var shpData: ByteArray? = null
        var dbfData: ByteArray? = null
        var prjData: String? = null

        try {
            val zis = ZipInputStream(inputStream)
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name.lowercase()
                when {
                    name.endsWith(".shp") -> shpData = zis.readBytes()
                    name.endsWith(".dbf") -> dbfData = zis.readBytes()
                    name.endsWith(".prj") -> prjData = String(zis.readBytes(), Charsets.UTF_8)
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            zis.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read ZIP", e)
            return null
        }

        if (shpData == null || dbfData == null) {
            Log.e(TAG, "ZIP missing .shp or .dbf")
            return null
        }

        val isLambert93 = prjData?.contains("Lambert_93", ignoreCase = true) == true
                || prjData?.contains("2154", ignoreCase = true) == true

        val attributes = parseDbf(dbfData) ?: return null
        val geometries = parseShp(shpData, isLambert93) ?: return null

        if (geometries.size != attributes.size) {
            Log.w(TAG, "Geometry count (${geometries.size}) != attribute count (${attributes.size})")
        }

        val count = minOf(geometries.size, attributes.size)
        val features = (0 until count).map { i ->
            ShapeFeature(rings = geometries[i], attributes = attributes[i])
        }

        val forestNames = features.map { it.attributes.forestName }.filter { it.isNotBlank() }.toSet()

        return ShapefileResult(
            features = features,
            forestNames = forestNames,
            isLambert93 = isLambert93
        )
    }

    // ── .shp parser ──

    private fun parseShp(data: ByteArray, reproject: Boolean): List<List<List<Pair<Double, Double>>>>? {
        try {
            val buf = ByteBuffer.wrap(data)

            // Vérifier le file code (big-endian)
            buf.order(ByteOrder.BIG_ENDIAN)
            val fileCode = buf.getInt(0)
            if (fileCode != 9994) {
                Log.e(TAG, "Invalid .shp file code: $fileCode")
                return null
            }

            // Shape type (little-endian)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            val shapeType = buf.getInt(32)
            if (shapeType !in POLYGON_TYPES) {
                Log.e(TAG, "Unsupported shape type: $shapeType (expected Polygon=5/15/25)")
                return null
            }

            val fileLenBytes = run {
                buf.order(ByteOrder.BIG_ENDIAN)
                buf.getInt(24) * 2  // file length in 16-bit words → bytes
            }

            val geometries = mutableListOf<List<List<Pair<Double, Double>>>>()
            var offset = 100 // start of records

            while (offset + 8 < fileLenBytes && offset + 8 < data.size) {
                buf.order(ByteOrder.BIG_ENDIAN)
                val contentLength = buf.getInt(offset + 4) * 2 // 16-bit words → bytes
                val recStart = offset + 8

                if (recStart + contentLength > data.size) break

                buf.order(ByteOrder.LITTLE_ENDIAN)
                val recShapeType = buf.getInt(recStart)

                if (recShapeType in POLYGON_TYPES) {
                    val poly = parsePolygonRecord(buf, recStart, reproject)
                    if (poly != null) geometries.add(poly)
                } else if (recShapeType == 0) {
                    // Null shape — ajouter un polygone vide
                    geometries.add(emptyList())
                }

                offset = recStart + contentLength
            }

            return geometries
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse .shp", e)
            return null
        }
    }

    private fun parsePolygonRecord(
        buf: ByteBuffer,
        recStart: Int,
        reproject: Boolean
    ): List<List<Pair<Double, Double>>>? {
        // Skip shape type (4) + bounding box (32) = 36
        val numParts = buf.getInt(recStart + 36)
        val numPoints = buf.getInt(recStart + 40)

        if (numParts <= 0 || numPoints <= 0) return null

        // Parts indices
        val partsOffset = recStart + 44
        val parts = IntArray(numParts) { buf.getInt(partsOffset + it * 4) }

        // Points
        val pointsOffset = partsOffset + numParts * 4
        val rings = mutableListOf<List<Pair<Double, Double>>>()

        for (p in 0 until numParts) {
            val startIdx = parts[p]
            val endIdx = if (p + 1 < numParts) parts[p + 1] else numPoints
            val ring = mutableListOf<Pair<Double, Double>>()

            for (i in startIdx until endIdx) {
                val x = buf.getDouble(pointsOffset + i * 16)
                val y = buf.getDouble(pointsOffset + i * 16 + 8)

                val (lon, lat) = if (reproject) {
                    Lambert93.toWgs84(x, y)
                } else {
                    Pair(x, y)
                }
                ring.add(Pair(lon, lat))
            }

            rings.add(ring)
        }

        return rings
    }

    // ── .dbf parser ──

    private data class DbfField(val name: String, val type: Char, val size: Int)

    private fun parseDbf(data: ByteArray): List<ParcelAttributes>? {
        try {
            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val numRecords = buf.getInt(4)
            val headerLen = buf.getShort(8).toInt() and 0xFFFF
            val recordLen = buf.getShort(10).toInt() and 0xFFFF
            val numFields = (headerLen - 33) / 32

            // Parse field descriptors
            val fields = mutableListOf<DbfField>()
            for (i in 0 until numFields) {
                val fOffset = 32 + i * 32
                val nameBytes = data.sliceArray(fOffset until fOffset + 11)
                val name = String(nameBytes, Charsets.US_ASCII).trimEnd('\u0000').trim()
                val type = data[fOffset + 11].toInt().toChar()
                val size = data[fOffset + 16].toInt() and 0xFF
                fields.add(DbfField(name, type, size))
            }

            // Parse records
            val records = mutableListOf<ParcelAttributes>()
            for (r in 0 until numRecords) {
                val recOffset = headerLen + r * recordLen + 1 // +1 for deletion flag
                val attrs = mutableMapOf<String, String>()
                var fieldPos = 0

                for (field in fields) {
                    val rawBytes = data.sliceArray(
                        recOffset + fieldPos until
                                minOf(recOffset + fieldPos + field.size, data.size)
                    )
                    val value = try {
                        String(rawBytes, Charsets.ISO_8859_1).trim()
                    } catch (_: Exception) {
                        String(rawBytes, Charsets.US_ASCII).trim()
                    }
                    attrs[field.name] = value
                    fieldPos += field.size
                }

                records.add(
                    ParcelAttributes(
                        nom = attrs["NOM"] ?: attrs["ccod_prf"] ?: "",
                        surface = attrs["SURFACE_HA"]?.toDoubleOrNull() ?: 0.0,
                        forestName = attrs["llib_frt"] ?: "",
                        parcelCode = attrs["ccod_prf"] ?: "",
                        district = attrs["qdis_prf"] ?: "",
                        allAttributes = attrs
                    )
                )
            }

            return records
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse .dbf", e)
            return null
        }
    }

    /**
     * Convertit un ShapefileResult en chaîne GeoJSON.
     */
    fun toGeoJson(result: ShapefileResult): String {
        val sb = StringBuilder()
        sb.append("""{"type":"FeatureCollection","features":[""")

        result.features.forEachIndexed { idx, feature ->
            if (idx > 0) sb.append(",")
            sb.append("""{"type":"Feature","properties":{""")

            // Properties
            sb.append(""""nom":${jsonString(feature.attributes.nom)},""")
            sb.append("\"surface_ha\":${String.format(Locale.US, "%.4f", feature.attributes.surface)},")
            sb.append(""""foret":${jsonString(feature.attributes.forestName)},""")
            sb.append(""""parcelle":${jsonString(feature.attributes.parcelCode)},""")
            sb.append(""""district":"${feature.attributes.district}"""")

            sb.append("""},"geometry":""")

            if (feature.rings.isEmpty()) {
                sb.append("null")
            } else {
                sb.append("""{"type":"Polygon","coordinates":[""")
                feature.rings.forEachIndexed { ri, ring ->
                    if (ri > 0) sb.append(",")
                    sb.append("[")
                    ring.forEachIndexed { pi, (lon, lat) ->
                        if (pi > 0) sb.append(",")
                        sb.append(String.format(Locale.US, "[%.7f,%.7f]", lon, lat))
                    }
                    sb.append("]")
                }
                sb.append("]}")
            }

            sb.append("}")
        }

        sb.append("]}")
        return sb.toString()
    }

    private fun jsonString(s: String): String {
        val escaped = s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }
}
