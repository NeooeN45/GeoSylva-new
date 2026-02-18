package com.forestry.counter.domain.usecase.export

import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.model.Tige
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Exporte les tiges géolocalisées au format ESRI Shapefile (Point).
 *
 * Produit un ZIP contenant .shp, .shx, .dbf et .prj (WGS 84).
 * Aucune dépendance tierce — écriture binaire directe conformément à la
 * spécification ESRI Shapefile Technical Description (1998).
 */
object ShapefileExporter {

    private const val SHP_MAGIC = 9994
    private const val SHP_VERSION = 1000
    private const val SHAPE_POINT = 1

    private const val WGS84_PRJ =
        "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\"," +
        "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]]," +
        "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]]"

    data class ExportResult(val stemCount: Int, val byteSize: Int)

    private data class ShpRow(
        val tige: Tige,
        val lon: Double,
        val lat: Double,
        val alt: Double?,
        val essenceName: String
    )

    /**
     * Écrit un ZIP Shapefile complet dans [outputStream].
     * @param baseName nom de base des fichiers dans le ZIP (sans extension)
     * @return nombre de tiges exportées et taille totale du ZIP
     */
    fun exportToZip(
        tiges: List<Tige>,
        essences: List<Essence> = emptyList(),
        outputStream: OutputStream,
        baseName: String = "geosylva_tiges"
    ): ExportResult {
        val essenceMap = essences.associateBy { it.code.uppercase() }

        val rows = tiges.mapNotNull { t ->
            val pt = QgisExportHelper.parseWktPointZ(t.gpsWkt) ?: return@mapNotNull null
            val essName = essenceMap[t.essenceCode.uppercase()]?.name ?: t.essenceCode
            ShpRow(t, pt.lon, pt.lat, pt.alt, essName)
        }

        if (rows.isEmpty()) {
            ZipOutputStream(outputStream).use { it.finish() }
            return ExportResult(0, 0)
        }

        val shpBytes = buildShp(rows)
        val shxBytes = buildShx(rows.size)
        val dbfBytes = buildDbf(rows)

        val zipBaos = ByteArrayOutputStream(shpBytes.size + shxBytes.size + dbfBytes.size + 1024)
        ZipOutputStream(zipBaos).use { zip ->
            zip.putNextEntry(ZipEntry("$baseName.shp")); zip.write(shpBytes); zip.closeEntry()
            zip.putNextEntry(ZipEntry("$baseName.shx")); zip.write(shxBytes); zip.closeEntry()
            zip.putNextEntry(ZipEntry("$baseName.dbf")); zip.write(dbfBytes); zip.closeEntry()
            zip.putNextEntry(ZipEntry("$baseName.prj")); zip.write(WGS84_PRJ.toByteArray(Charsets.US_ASCII)); zip.closeEntry()
        }

        val zipData = zipBaos.toByteArray()
        outputStream.write(zipData)
        return ExportResult(rows.size, zipData.size)
    }

    // ── SHP (main shape file) ──────────────────────

    private fun buildShp(rows: List<ShpRow>): ByteArray {
        val numRecords = rows.size
        val recordContentLen = 20 // shapeType(4) + X(8) + Y(8)
        val recordTotalLen = 8 + recordContentLen
        val fileLen = 100 + numRecords * recordTotalLen

        val buf = ByteBuffer.allocate(fileLen)

        // File header
        buf.order(ByteOrder.BIG_ENDIAN)
        buf.putInt(SHP_MAGIC)
        buf.position(24)
        buf.putInt(fileLen / 2)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(SHP_VERSION)
        buf.putInt(SHAPE_POINT)

        // Bounding box
        var xMin = Double.MAX_VALUE; var yMin = Double.MAX_VALUE
        var xMax = -Double.MAX_VALUE; var yMax = -Double.MAX_VALUE
        for (r in rows) {
            if (r.lon < xMin) xMin = r.lon
            if (r.lon > xMax) xMax = r.lon
            if (r.lat < yMin) yMin = r.lat
            if (r.lat > yMax) yMax = r.lat
        }
        buf.putDouble(xMin); buf.putDouble(yMin)
        buf.putDouble(xMax); buf.putDouble(yMax)
        buf.putDouble(0.0); buf.putDouble(0.0)
        buf.putDouble(0.0); buf.putDouble(0.0)

        // Records
        for ((idx, r) in rows.withIndex()) {
            buf.order(ByteOrder.BIG_ENDIAN)
            buf.putInt(idx + 1)
            buf.putInt(recordContentLen / 2)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            buf.putInt(SHAPE_POINT)
            buf.putDouble(r.lon)
            buf.putDouble(r.lat)
        }

        return buf.array()
    }

    // ── SHX (index file) ──────────────────────────

    private fun buildShx(numRecords: Int): ByteArray {
        val recordContentLen = 20
        val shxFileLen = 100 + numRecords * 8

        val buf = ByteBuffer.allocate(shxFileLen)

        buf.order(ByteOrder.BIG_ENDIAN)
        buf.putInt(SHP_MAGIC)
        buf.position(24)
        buf.putInt(shxFileLen / 2)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(SHP_VERSION)
        buf.putInt(SHAPE_POINT)
        repeat(8) { buf.putDouble(0.0) }

        buf.order(ByteOrder.BIG_ENDIAN)
        val recordTotalLen = 8 + recordContentLen
        for (i in 0 until numRecords) {
            buf.putInt((100 + i * recordTotalLen) / 2)
            buf.putInt(recordContentLen / 2)
        }

        return buf.array()
    }

    // ── DBF (attribute table) ──────────────────────

    private data class DbfField(val name: String, val type: Char, val size: Int, val decimal: Int = 0)

    private val DBF_FIELDS = listOf(
        DbfField("ESSENCE", 'C', 20),
        DbfField("ESS_NAME", 'C', 40),
        DbfField("DIAM_CM", 'N', 10, 1),
        DbfField("HAUTEUR_M", 'N', 10, 1),
        DbfField("QUALITE", 'N', 5),
        DbfField("CATEGORIE", 'C', 20),
        DbfField("PRODUIT", 'C', 30),
        DbfField("NOTE", 'C', 60),
        DbfField("PRECISION", 'N', 8, 1),
        DbfField("ALTITUDE", 'N', 10, 1)
    )

    private fun buildDbf(rows: List<ShpRow>): ByteArray {
        val recordLen = 1 + DBF_FIELDS.sumOf { it.size }
        val headerLen = 32 + DBF_FIELDS.size * 32 + 1

        val baos = ByteArrayOutputStream(headerLen + rows.size * recordLen)
        val dos = DataOutputStream(baos)

        // Header
        dos.writeByte(0x03) // dBASE III
        dos.writeByte(26); dos.writeByte(2); dos.writeByte(17) // date
        dos.writeIntLE(rows.size)
        dos.writeShortLE(headerLen)
        dos.writeShortLE(recordLen)
        repeat(20) { dos.writeByte(0) }

        // Field descriptors
        for (f in DBF_FIELDS) {
            dos.write(f.name.toByteArray(Charsets.US_ASCII).copyOf(11))
            dos.writeByte(f.type.code)
            repeat(4) { dos.writeByte(0) }
            dos.writeByte(f.size)
            dos.writeByte(f.decimal)
            repeat(14) { dos.writeByte(0) }
        }
        dos.writeByte(0x0D) // terminator

        // Records
        for (r in rows) {
            dos.writeByte(0x20) // not deleted
            writeDbfText(dos, r.tige.essenceCode, 20)
            writeDbfText(dos, r.essenceName, 40)
            writeDbfNum(dos, r.tige.diamCm, 10, 1)
            writeDbfNum(dos, r.tige.hauteurM, 10, 1)
            writeDbfNum(dos, r.tige.qualite?.toDouble(), 5, 0)
            writeDbfText(dos, r.tige.categorie ?: "", 20)
            writeDbfText(dos, r.tige.produit ?: "", 30)
            writeDbfText(dos, r.tige.note ?: "", 60)
            writeDbfNum(dos, r.tige.precisionM, 8, 1)
            writeDbfNum(dos, r.tige.altitudeM ?: r.alt, 10, 1)
        }
        dos.writeByte(0x1A) // EOF

        dos.flush()
        return baos.toByteArray()
    }

    // ── Write helpers ──

    private fun writeDbfText(dos: DataOutputStream, value: String, size: Int) {
        val bytes = value.take(size).toByteArray(Charsets.ISO_8859_1)
        dos.write(bytes)
        repeat(size - bytes.size) { dos.writeByte(0x20) }
    }

    private fun writeDbfNum(dos: DataOutputStream, value: Double?, size: Int, decimal: Int) {
        val str = if (value != null) {
            if (decimal > 0) String.format("%.${decimal}f", value)
            else String.format("%.0f", value)
        } else ""
        writeDbfText(dos, str.takeLast(size), size)
    }

    private fun DataOutputStream.writeIntLE(v: Int) {
        writeByte(v and 0xFF)
        writeByte((v shr 8) and 0xFF)
        writeByte((v shr 16) and 0xFF)
        writeByte((v shr 24) and 0xFF)
    }

    private fun DataOutputStream.writeShortLE(v: Int) {
        writeByte(v and 0xFF)
        writeByte((v shr 8) and 0xFF)
    }
}
