package com.forestry.counter.domain.usecase.export

import com.forestry.counter.domain.model.IbpCriterionId
import com.forestry.counter.domain.model.IbpEvaluation
import com.forestry.counter.domain.model.IbpLevel
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Exports a list of IBP evaluations to a ZIP containing:
 * - ibp_points.geojson  (all evaluations with GPS coords)
 * - ibp_all.csv         (full table – all 10 criteria)
 * - ibp_style.qml       (QGIS rule-based style coloured by IBP level)
 * - ibp_metadata.json
 */
object IbpQgisExporter {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun export(
        evaluations: List<IbpEvaluation>,
        projectName: String,
        out: OutputStream
    ) {
        ZipOutputStream(out).use { zip ->
            writeGeoJson(zip, evaluations)
            writeCsv(zip, evaluations)
            writeQmlStyle(zip)
            writeMetadata(zip, projectName, evaluations.size)
        }
    }

    /* ─── GeoJSON ───────────────────────────────────────────────── */
    private fun writeGeoJson(zip: ZipOutputStream, evals: List<IbpEvaluation>) {
        zip.putNextEntry(ZipEntry("ibp_points.geojson"))
        val sb = StringBuilder()
        sb.append("""{"type":"FeatureCollection","features":[""")
        val withGps = evals.filter { it.latitude != null && it.longitude != null }
        withGps.forEachIndexed { idx, ev ->
            val score = ev.scoreTotal
            val level = IbpLevel.fromScore(score)
            sb.append("""
{"type":"Feature","geometry":{"type":"Point","coordinates":[${ev.longitude},${ev.latitude}]},
"properties":{
  "id":"${ev.id}",
  "date":"${sdf.format(Date(ev.observationDate))}",
  "placette_id":"${ev.placetteId}",
  "parcelle_id":"${ev.parcelleId}",
  "evaluateur":"${ev.evaluatorName.replace("\"", "\\\"")}",
  "score_total":$score,
  "score_A":${ev.scoreA},
  "score_B":${ev.scoreB},
  "niveau":"${level.name}",
  "mode":"${ev.ibpMode.name}",
${criteriaProperties(ev)}
}}""".trimIndent())
            if (idx < withGps.lastIndex) sb.append(",")
        }
        sb.append("]}")
        zip.write(sb.toString().toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun criteriaProperties(ev: IbpEvaluation): String {
        return IbpCriterionId.ALL.joinToString(",\n") { cid ->
            val v = ev.answers.get(cid)
            "  \"${cid.displayCode}\":${if (v >= 0) v else "null"}"
        }
    }

    /* ─── CSV ────────────────────────────────────────────────────── */
    private fun writeCsv(zip: ZipOutputStream, evals: List<IbpEvaluation>) {
        zip.putNextEntry(ZipEntry("ibp_all.csv"))
        val header = buildString {
            append("id,date,placette_id,parcelle_id,evaluateur,latitude,longitude,")
            append("score_total,score_A,score_B,niveau,mode,")
            append(IbpCriterionId.ALL.joinToString(",") { it.displayCode })
        }
        val rows = evals.map { ev ->
            val score = ev.scoreTotal
            val level = IbpLevel.fromScore(score)
            buildString {
                append("${ev.id},")
                append("${sdf.format(Date(ev.observationDate))},")
                append("${ev.placetteId},")
                append("${ev.parcelleId},")
                append("\"${ev.evaluatorName.replace("\"", "\"\"")}\",")
                append("${ev.latitude ?: ""},")
                append("${ev.longitude ?: ""},")
                append("$score,")
                append("${ev.scoreA},")
                append("${ev.scoreB},")
                append("${level.name},")
                append("${ev.ibpMode.name},")
                append(IbpCriterionId.ALL.joinToString(",") { cid ->
                    val v = ev.answers.get(cid); if (v >= 0) "$v" else ""
                })
            }
        }
        val csv = (listOf(header) + rows).joinToString("\n")
        zip.write(csv.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    /* ─── QGIS QML style ────────────────────────────────────────── */
    private fun writeQmlStyle(zip: ZipOutputStream) {
        zip.putNextEntry(ZipEntry("ibp_style.qml"))
        val qml = buildString {
            appendLine("""<!DOCTYPE qgis PUBLIC 'http://mrcc.com/qgis.dtd' 'SYSTEM'>""")
            appendLine("""<qgis version="3.28" styleCategories="Symbology">""")
            appendLine(""" <renderer-v2 type="RuleRenderer" forcerasterrender="0">""")
            appendLine("""  <rules key="{IBP_RULES}">""")
            val rules = listOf(
                "VERY_GOOD"  to ("#1565C0" to "Score 40–50 (Très bon)"),
                "GOOD"       to ("#2E7D32" to "Score 30–39 (Bon)"),
                "MEDIUM"     to ("#F9A825" to "Score 20–29 (Moyen)"),
                "LOW"        to ("#E65100" to "Score 10–19 (Faible)"),
                "VERY_LOW"   to ("#C62828" to "Score 0–9 (Très faible)")
            )
            rules.forEach { (level, pair) ->
                val (color, label) = pair
                val filter = when (level) {
                    "VERY_GOOD" -> """"niveau" = 'VERY_GOOD'"""
                    "GOOD"      -> """"niveau" = 'GOOD'"""
                    "MEDIUM"    -> """"niveau" = 'MEDIUM'"""
                    "LOW"       -> """"niveau" = 'LOW'"""
                    else        -> """"niveau" = 'VERY_LOW'"""
                }
                appendLine("""   <rule filter="$filter" label="$label">""")
                appendLine("""    <symbol type="marker" name=""><layer class="SimpleMarker">""")
                appendLine("""     <Option type="Map"><Option name="color" value="$color"/><Option name="size" value="4"/></Option>""")
                appendLine("""    </layer></symbol>""")
                appendLine("""   </rule>""")
            }
            appendLine("""  </rules>""")
            appendLine(""" </renderer-v2>""")
            appendLine("""</qgis>""")
        }
        zip.write(qml.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    /* ─── Metadata JSON ─────────────────────────────────────────── */
    private fun writeMetadata(zip: ZipOutputStream, projectName: String, count: Int) {
        zip.putNextEntry(ZipEntry("ibp_metadata.json"))
        val meta = """
{
  "project": "${projectName.replace("\"", "\\\"")}",
  "protocol": "IBP v3.2 CNPF/IDF",
  "export_date": "${sdf.format(Date())}",
  "evaluation_count": $count,
  "generator": "GeoSylva",
  "criteria_group_A": ["A","B","C","D","E","F","G"],
  "criteria_group_B": ["H","I","J"],
  "score_max": 50,
  "levels": {
    "VERY_LOW":  [0,9],
    "LOW":       [10,19],
    "MEDIUM":    [20,29],
    "GOOD":      [30,39],
    "VERY_GOOD": [40,50]
  }
}
""".trimIndent()
        zip.write(meta.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}
