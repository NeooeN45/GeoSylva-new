package com.forestry.counter.domain.usecase.export

import com.forestry.counter.domain.model.Essence
import com.forestry.counter.domain.model.Tige
import com.forestry.counter.domain.location.Lambert93Converter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitaire pour exporter les tiges en GeoJSON ou CSV-XY compatibles QGIS.
 *
 * Le GeoJSON respecte la RFC 7946 (CRS implicite EPSG:4326).
 * Les attributs forestiers sont exhaustifs pour usage professionnel.
 */
object QgisExportHelper {

    // ──────────────────────────────────────────────
    // GeoJSON (QGIS / MapLibre / Leaflet)
    // ──────────────────────────────────────────────

    /**
     * Génère un GeoJSON FeatureCollection complet pour QGIS.
     * @return Pair(geojsonString, stemCount)
     */
    fun buildGeoJson(
        tiges: List<Tige>,
        essences: List<Essence> = emptyList(),
        parcelleName: String? = null,
        placetteName: String? = null
    ): Pair<String, Int> {
        val essenceMap = essences.associateBy { it.code.uppercase() }
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date())

        val features = tiges.mapNotNull { t ->
            val (lon, lat, alt) = parseWktPointZ(t.gpsWkt) ?: return@mapNotNull null
            val ess = essenceMap[t.essenceCode.uppercase()]
            buildFeature(t, ess, lon, lat, alt, parcelleName, placetteName)
        }

        val sb = StringBuilder(features.size * 512)
        sb.append("{\"type\":\"FeatureCollection\",")
        sb.append("\"name\":\"GeoSylva_tiges\",")
        sb.append("\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"urn:ogc:def:crs:OGC:1.3:CRS84\"}},")
        sb.append("\"metadata\":{\"exported_at\":\"$now\",\"source\":\"GeoSylva\",\"stem_count\":${features.size}},")
        sb.append("\"features\":[")
        features.forEachIndexed { i, f ->
            if (i > 0) sb.append(',')
            sb.append(f)
        }
        sb.append("]}")
        return sb.toString() to features.size
    }

    private fun buildFeature(
        t: Tige, ess: Essence?,
        lon: Double, lat: Double, alt: Double?,
        parcelleName: String?, placetteName: String?
    ): String = buildString {
        append("{\"type\":\"Feature\",")
        append("\"geometry\":{\"type\":\"Point\",\"coordinates\":[")
        append(lon); append(','); append(lat)
        if (alt != null) { append(','); append(alt) }
        append("]},")
        append("\"properties\":{")
        appendProp("id", t.id)
        appendProp("parcelle_id", t.parcelleId)
        appendPropNullable("parcelle_name", parcelleName)
        appendPropNullable("placette_id", t.placetteId)
        appendPropNullable("placette_name", placetteName)
        appendProp("essence_code", t.essenceCode)
        appendPropNullable("essence_name", ess?.name)
        appendPropNullable("categorie_essence", ess?.categorie)
        appendNum("diam_cm", t.diamCm)
        appendNum("diam_class_cm", (t.diamCm / 5.0).toInt() * 5)
        appendNumNullable("hauteur_m", t.hauteurM)
        appendNumNullable("precision_m", t.precisionM)
        appendNumNullable("altitude_m", t.altitudeM ?: alt)
        appendPropNullable("categorie", t.categorie)
        appendNumNullable("qualite", t.qualite)
        appendNumNullable("numero", t.numero)
        appendPropNullable("note", t.note)
        appendPropNullable("produit", t.produit)
        appendNumNullable("f_coef", t.fCoef)
        appendNumNullable("value_eur", t.valueEur)
        appendPropNullable("defauts", t.defauts?.joinToString(","))
        appendPropNullable("photo_uri", t.photoUri)
        appendNum("timestamp", t.timestamp)
        // Lambert 93 (EPSG:2154) pour la France
        if (Lambert93Converter.isInFranceMetro(lon, lat)) {
            val (e93, n93) = Lambert93Converter.toL93(lon, lat)
            appendNum("lambert93_e", e93.toLong())
            appendNum("lambert93_n", n93.toLong(), last = true)
        } else {
            appendNumNullable("lambert93_e", null)
            appendNumNullable("lambert93_n", null, last = true)
        }
        append("}}")
    }

    // ──────────────────────────────────────────────
    // CSV-XY (QGIS — Texte délimité)
    // ──────────────────────────────────────────────

    /**
     * Génère un CSV séparateur point-virgule avec colonnes lon/lat pour import QGIS.
     * @return Pair(csvString, stemCount)
     */
    fun buildCsvXY(
        tiges: List<Tige>,
        essences: List<Essence> = emptyList()
    ): Pair<String, Int> {
        val essenceMap = essences.associateBy { it.code.uppercase() }
        val sb = StringBuilder(tiges.size * 256)

        // Header
        sb.appendLine("id;essence_code;essence_name;categorie;diam_cm;diam_class_cm;hauteur_m;qualite;produit;note;precision_m;altitude_m;lon;lat;lambert93_e;lambert93_n;timestamp")

        var count = 0
        for (t in tiges) {
            val parsed = parseWktPointZ(t.gpsWkt) ?: continue
            val (lon, lat, alt) = parsed
            val ess = essenceMap[t.essenceCode.uppercase()]
            val diamClass = (t.diamCm / 5.0).toInt() * 5

            sb.append(csvEscape(t.id)); sb.append(';')
            sb.append(csvEscape(t.essenceCode)); sb.append(';')
            sb.append(csvEscape(ess?.name ?: "")); sb.append(';')
            sb.append(csvEscape(ess?.categorie ?: "")); sb.append(';')
            sb.append(t.diamCm); sb.append(';')
            sb.append(diamClass); sb.append(';')
            sb.append(t.hauteurM?.toString() ?: ""); sb.append(';')
            sb.append(t.qualite?.toString() ?: ""); sb.append(';')
            sb.append(csvEscape(t.produit ?: "")); sb.append(';')
            sb.append(csvEscape(t.note ?: "")); sb.append(';')
            sb.append(t.precisionM?.toString() ?: ""); sb.append(';')
            sb.append((t.altitudeM ?: alt)?.toString() ?: ""); sb.append(';')
            sb.append(lon); sb.append(';')
            sb.append(lat); sb.append(';')
            if (Lambert93Converter.isInFranceMetro(lon, lat)) {
                val (e93, n93) = Lambert93Converter.toL93(lon, lat)
                sb.append("%.0f".format(e93)); sb.append(';')
                sb.append("%.0f".format(n93)); sb.append(';')
            } else {
                sb.append(';'); sb.append(';')
            }
            sb.appendLine(t.timestamp.toString())
            count++
        }

        return sb.toString() to count
    }

    // ──────────────────────────────────────────────
    // Utilitaires
    // ──────────────────────────────────────────────

    data class WktPoint(val lon: Double, val lat: Double, val alt: Double?)

    fun parseWktPointZ(wkt: String?): WktPoint? {
        val (lon, lat, alt) = com.forestry.counter.domain.location.WktUtils.parsePointZ(wkt)
        return if (lon != null && lat != null) WktPoint(lon, lat, alt) else null
    }

    private fun csvEscape(s: String): String {
        val clean = s.replace("\r\n", " ").replace("\n", " ")
        return if (clean.contains(';') || clean.contains('"')) {
            "\"" + clean.replace("\"", "\"\"") + "\""
        } else clean
    }

    // JSON property helpers — avoid pulling in kotlinx.serialization for a simple builder
    private fun StringBuilder.appendProp(key: String, value: String, last: Boolean = false) {
        append('"'); append(key); append("\":\""); append(jsonEscape(value)); append('"')
        if (!last) append(',')
    }

    private fun StringBuilder.appendPropNullable(key: String, value: String?, last: Boolean = false) {
        append('"'); append(key); append("\":")
        if (value != null) { append('"'); append(jsonEscape(value)); append('"') } else append("null")
        if (!last) append(',')
    }

    private fun StringBuilder.appendNum(key: String, value: Number, last: Boolean = false) {
        append('"'); append(key); append("\":"); append(value)
        if (!last) append(',')
    }

    private fun StringBuilder.appendNumNullable(key: String, value: Number?, last: Boolean = false) {
        append('"'); append(key); append("\":"); append(value?.toString() ?: "null")
        if (!last) append(',')
    }

    private fun jsonEscape(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}
