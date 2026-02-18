package com.forestry.counter.domain.usecase.export

import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.model.Tige
import org.junit.Assert.*
import org.junit.Test

class QgisExportHelperTest {

    private fun makeTige(
        id: String = "t1",
        essenceCode: String = "CHP",
        diamCm: Double = 35.0,
        hauteurM: Double? = 22.0,
        gpsWkt: String? = "POINT Z(2.35 48.85 100)",
        parcelleId: String = "p1",
        placetteId: String? = null,
        categorie: String? = null,
        qualite: Int? = null
    ) = Tige(
        id = id,
        parcelleId = parcelleId,
        placetteId = placetteId,
        essenceCode = essenceCode,
        diamCm = diamCm,
        hauteurM = hauteurM,
        gpsWkt = gpsWkt,
        precisionM = 3.0,
        altitudeM = 100.0,
        timestamp = 1700000000000L,
        note = null,
        produit = null,
        fCoef = null,
        valueEur = null,
        numero = null,
        categorie = categorie,
        qualite = qualite,
        defauts = null,
        photoUri = null,
        qualiteDetail = null
    )

    private val essences = listOf(
        Essence(code = "CHP", name = "Chêne pédonculé", categorie = "feuillu", densiteBoite = 0.65, colorHex = "#228B22")
    )

    // ── GeoJSON ──

    @Test
    fun `buildGeoJson returns valid FeatureCollection with one stem`() {
        val tiges = listOf(makeTige())
        val (json, count) = QgisExportHelper.buildGeoJson(tiges, essences)

        assertEquals(1, count)
        assertTrue(json.startsWith("{\"type\":\"FeatureCollection\""))
        assertTrue(json.contains("\"stem_count\":1"))
        assertTrue(json.contains("\"essence_code\":\"CHP\""))
        assertTrue(json.contains("\"diam_cm\":35"))
        assertTrue(json.contains("\"coordinates\":[2.35,48.85"))
    }

    @Test
    fun `buildGeoJson skips tiges without GPS`() {
        val tiges = listOf(
            makeTige(id = "t1", gpsWkt = null),
            makeTige(id = "t2", gpsWkt = "POINT Z(2.35 48.85 100)")
        )
        val (json, count) = QgisExportHelper.buildGeoJson(tiges, essences)

        assertEquals(1, count)
        assertTrue(json.contains("\"id\":\"t2\""))
        assertFalse(json.contains("\"id\":\"t1\""))
    }

    @Test
    fun `buildGeoJson returns empty collection for no GPS tiges`() {
        val tiges = listOf(makeTige(gpsWkt = null))
        val (json, count) = QgisExportHelper.buildGeoJson(tiges, essences)

        assertEquals(0, count)
        assertTrue(json.contains("\"features\":[]"))
    }

    @Test
    fun `buildGeoJson includes Lambert93 for France coordinates`() {
        val tiges = listOf(makeTige(gpsWkt = "POINT Z(2.35 48.85 100)"))
        val (json, _) = QgisExportHelper.buildGeoJson(tiges, essences)

        assertTrue(json.contains("\"lambert93_e\":"))
        assertTrue(json.contains("\"lambert93_n\":"))
    }

    @Test
    fun `buildGeoJson includes categorie and qualite fields`() {
        val tiges = listOf(makeTige(categorie = "DEPERISSANT", qualite = 2))
        val (json, _) = QgisExportHelper.buildGeoJson(tiges, essences)

        assertTrue(json.contains("\"categorie\":\"DEPERISSANT\""))
        assertTrue(json.contains("\"qualite\":2"))
    }

    // ── CSV-XY ──

    @Test
    fun `buildCsvXY returns header and one data row`() {
        val tiges = listOf(makeTige())
        val (csv, count) = QgisExportHelper.buildCsvXY(tiges, essences)

        assertEquals(1, count)
        val lines = csv.trimEnd().split("\n")
        assertEquals(2, lines.size) // header + 1 row
        assertTrue(lines[0].startsWith("id;essence_code;"))
        assertTrue(lines[1].contains("CHP"))
        assertTrue(lines[1].contains("35.0"))
    }

    @Test
    fun `buildCsvXY skips tiges without GPS`() {
        val tiges = listOf(
            makeTige(id = "t1", gpsWkt = null),
            makeTige(id = "t2")
        )
        val (csv, count) = QgisExportHelper.buildCsvXY(tiges, essences)

        assertEquals(1, count)
        val lines = csv.trimEnd().split("\n")
        assertEquals(2, lines.size)
        assertTrue(lines[1].startsWith("t2;"))
    }

    @Test
    fun `buildCsvXY escapes semicolons in notes`() {
        val tige = makeTige().copy(note = "test;note")
        val tiges = listOf(tige)
        val (csv, _) = QgisExportHelper.buildCsvXY(tiges, essences)

        // semicolons in content should be quoted
        assertTrue(csv.contains("\"test;note\""))
    }

    // ── WKT parsing ──

    @Test
    fun `parseWktPointZ parses valid WKT`() {
        val p = QgisExportHelper.parseWktPointZ("POINT Z(2.35 48.85 100)")
        assertNotNull(p)
        assertEquals(2.35, p!!.lon, 0.001)
        assertEquals(48.85, p.lat, 0.001)
        assertEquals(100.0, p.alt!!, 0.001)
    }

    @Test
    fun `parseWktPointZ returns null for null input`() {
        assertNull(QgisExportHelper.parseWktPointZ(null))
    }

    @Test
    fun `parseWktPointZ returns null for empty string`() {
        assertNull(QgisExportHelper.parseWktPointZ(""))
    }
}
