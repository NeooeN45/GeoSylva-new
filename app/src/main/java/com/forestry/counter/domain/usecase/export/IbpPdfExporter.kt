package com.forestry.counter.domain.usecase.export

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.forestry.counter.domain.model.*
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object IbpPdfExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun export(context: Context, eval: IbpEvaluation, out: OutputStream, placetteName: String = "") {
        val doc = PdfDocument()
        val pageWidth = 595   // A4 width in points (72 dpi)
        val pageHeight = 842  // A4 height in points

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        drawPage(canvas, eval, pageWidth, pageHeight, placetteName)
        doc.finishPage(page)

        val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = doc.startPage(pageInfo2)
        drawDetailPage(page2.canvas, eval, pageWidth, pageHeight)
        doc.finishPage(page2)

        doc.writeTo(out)
        doc.close()
    }

    private fun drawPage(canvas: Canvas, eval: IbpEvaluation, w: Int, h: Int, placetteName: String = "") {
        val margin = 40f
        var y = margin

        // ── Header band ──────────────────────────────────────────
        val headerPaint = Paint().apply { color = Color.parseColor("#1B5E20"); isAntiAlias = true }
        canvas.drawRect(0f, 0f, w.toFloat(), 90f, headerPaint)

        // Title
        val titlePaint = Paint().apply {
            color = Color.WHITE; textSize = 22f; isFakeBoldText = true
            isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("IBP — Indice de Biodiversité Potentielle", margin, 38f, titlePaint)

        val subTitlePaint = Paint().apply { color = Color.WHITE; textSize = 11f; isAntiAlias = true; alpha = 200 }
        canvas.drawText("Peuplement forestier — Évaluation terrain", margin, 58f, subTitlePaint)

        // GeoSylva brand
        val brandPaint = Paint().apply { color = Color.WHITE; textSize = 10f; isAntiAlias = true; alpha = 180 }
        canvas.drawText("GeoSylva", (w - margin - 70).coerceAtLeast(0f), 38f, brandPaint)
        canvas.drawText("Rapport généré le ${dateFormat.format(Date())}", (w - margin - 130).coerceAtLeast(0f), 58f, brandPaint)

        y = 110f

        // ── Meta section ─────────────────────────────────────────
        val metaBg = Paint().apply { color = Color.parseColor("#F1F8E9"); isAntiAlias = true }
        canvas.drawRoundRect(margin, y, w - margin, y + 60f, 6f, 6f, metaBg)
        val metaBorder = Paint().apply { color = Color.parseColor("#A5D6A7"); style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRoundRect(margin, y, w - margin, y + 60f, 6f, 6f, metaBorder)

        val labelPaint = Paint().apply { color = Color.parseColor("#558B2F"); textSize = 9f; isAntiAlias = true }
        val valuePaint = Paint().apply { color = Color.parseColor("#212121"); textSize = 11f; isFakeBoldText = true; isAntiAlias = true }

        canvas.drawText("ÉVALUATEUR", margin + 12f, y + 18f, labelPaint)
        canvas.drawText(eval.evaluatorName.ifBlank { "—" }, margin + 12f, y + 34f, valuePaint)
        canvas.drawText("DATE D'OBSERVATION", margin + 200f, y + 18f, labelPaint)
        canvas.drawText(dateFormat.format(Date(eval.observationDate)), margin + 200f, y + 34f, valuePaint)
        canvas.drawText("PLACETTE", margin + 360f, y + 18f, labelPaint)
        val displayName = placetteName.ifBlank { eval.placetteId.take(12) }
        canvas.drawText(displayName, margin + 360f, y + 34f, valuePaint)
        canvas.drawText("Page 1/2", margin + 360f, y + 50f, labelPaint)

        y += 80f

        // ── Score summary ────────────────────────────────────────
        val scoreTotal = eval.scoreTotal
        val level = eval.levelColor()
        val levelColor = ibpLevelColorInt(level)

        val scoreBg = Paint().apply { color = levelColor; isAntiAlias = true; alpha = 30 }
        canvas.drawRoundRect(margin, y, w - margin, y + 90f, 8f, 8f, scoreBg)
        val scoreBorder = Paint().apply { color = levelColor; style = Paint.Style.STROKE; strokeWidth = 2f }
        canvas.drawRoundRect(margin, y, w - margin, y + 90f, 8f, 8f, scoreBorder)

        val bigScorePaint = Paint().apply {
            color = levelColor; textSize = 48f; isFakeBoldText = true; isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        val scoreText = if (scoreTotal >= 0) "$scoreTotal" else "—"
        canvas.drawText(scoreText, margin + 24f, y + 62f, bigScorePaint)

        val outOf20 = Paint().apply { color = Color.parseColor("#616161"); textSize = 18f; isAntiAlias = true }
        canvas.drawText("/ 50", margin + 80f, y + 62f, outOf20)

        // Level badge
        val levelBgPaint = Paint().apply { color = levelColor; isAntiAlias = true }
        canvas.drawRoundRect(margin + 140f, y + 22f, margin + 300f, y + 50f, 4f, 4f, levelBgPaint)
        val levelTextPaint = Paint().apply { color = Color.WHITE; textSize = 13f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText(ibpLevelLabel(level), margin + 148f, y + 41f, levelTextPaint)

        // Sub-scores
        val subLabelPaint = Paint().apply { color = Color.parseColor("#757575"); textSize = 9f; isAntiAlias = true }
        val subValPaint = Paint().apply { color = Color.parseColor("#212121"); textSize = 14f; isFakeBoldText = true; isAntiAlias = true }
        val aScore = eval.scoreA; val bScore = eval.scoreB
        canvas.drawText("Groupe A (peuplement)", margin + 320f, y + 28f, subLabelPaint)
        canvas.drawText("${if (aScore >= 0) aScore else "—"} / 35", margin + 320f, y + 46f, subValPaint)
        canvas.drawText("Groupe B (contexte)", margin + 440f, y + 28f, subLabelPaint)
        canvas.drawText("${if (bScore >= 0) bScore else "—"} / 15", margin + 440f, y + 46f, subValPaint)

        // Interpretation
        val commentPaint = Paint().apply { color = Color.parseColor("#424242"); textSize = 10f; isAntiAlias = true }
        canvas.drawText(ibpLevelCommentShort(level), margin + 24f, y + 82f, commentPaint)

        y += 110f

        // ── Progress bar ─────────────────────────────────────────
        val barBg = Paint().apply { color = Color.parseColor("#E0E0E0") }
        canvas.drawRoundRect(margin, y, w - margin, y + 10f, 5f, 5f, barBg)
        if (scoreTotal >= 0) {
            val barLen = (w - 2 * margin) * (scoreTotal / 50f)
            val barFill = Paint().apply { color = levelColor; isAntiAlias = true }
            canvas.drawRoundRect(margin, y, margin + barLen, y + 10f, 5f, 5f, barFill)
        }

        y += 24f

        // ── Recommendations ───────────────────────────────────────
        val recTitlePaint = Paint().apply { color = Color.parseColor("#2E7D32"); textSize = 12f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("Recommandations de gestion", margin, y, recTitlePaint)
        y += 14f

        val recPaint = Paint().apply { color = Color.parseColor("#424242"); textSize = 10f; isAntiAlias = true }
        ibpLevelRecommendationsStatic(level).forEach { rec ->
            canvas.drawText("• $rec", margin + 8f, y, recPaint)
            y += 15f
        }

        y += 14f

        // ── Score radar / table ───────────────────────────────────
        val sectionTitle = Paint().apply { color = Color.parseColor("#1B5E20"); textSize = 12f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("Détail des 10 critères — Groupe A (Peuplement)", margin, y, sectionTitle)
        y += 10f

        drawCriteriaTable(canvas, eval, IbpCriterionId.GROUP_A, margin, y, w, Color.parseColor("#2E7D32"))
        y += IbpCriterionId.GROUP_A.size * 28f + 10f

        // ── Notes ─────────────────────────────────────────────────
        if (eval.globalNote.isNotBlank()) {
            val noteLabelPaint = Paint().apply { color = Color.parseColor("#2E7D32"); textSize = 11f; isFakeBoldText = true; isAntiAlias = true }
            canvas.drawText("Notes de terrain :", margin, y, noteLabelPaint)
            y += 15f
            val notePaint = Paint().apply { color = Color.parseColor("#424242"); textSize = 10f; isAntiAlias = true }
            val maxWidth = (w - 2 * margin).toInt()
            wrapText(canvas, eval.globalNote, notePaint, margin, y, maxWidth.toFloat(), 14f)
        }

        // Footer
        drawFooter(canvas, "Rapport IBP — GeoSylva — Page 1/2", w, h)
    }

    private fun drawDetailPage(canvas: Canvas, eval: IbpEvaluation, w: Int, h: Int) {
        val margin = 40f
        var y = margin

        // Header band
        val headerPaint = Paint().apply { color = Color.parseColor("#1565C0"); isAntiAlias = true }
        canvas.drawRect(0f, 0f, w.toFloat(), 60f, headerPaint)
        val titlePaint = Paint().apply { color = Color.WHITE; textSize = 16f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("IBP — Groupe B (Contexte) & Synthèse complète", margin, 38f, titlePaint)
        y = 80f

        // Group B table
        val sectionTitle = Paint().apply { color = Color.parseColor("#1565C0"); textSize = 12f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("Groupe B — Critères contextuels", margin, y, sectionTitle)
        y += 10f
        drawCriteriaTable(canvas, eval, IbpCriterionId.GROUP_B, margin, y, w, Color.parseColor("#1565C0"))
        y += IbpCriterionId.GROUP_B.size * 28f + 20f

        // Radar chart (simple)
        canvas.drawText("Synthèse radar des 10 critères", margin, y, sectionTitle)
        y += 14f
        drawRadarChart(canvas, eval, margin, y, (w - 2 * margin).toInt(), 220)
        y += 240f

        // Complete criteria detail
        val detailTitle = Paint().apply { color = Color.parseColor("#424242"); textSize = 12f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("Référentiel des critères (seuils officiels IBP)", margin, y, detailTitle)
        y += 14f

        val smallPaint = Paint().apply { color = Color.parseColor("#616161"); textSize = 9f; isAntiAlias = true }
        val criteria = listOf(
            "A – Essences autochtones" to "0: 0-1 genre | 2: 2 genres | 5: ≥3 genres (subalpin) / ≥5 genres",
            "B – Structure verticale" to "0: 1 strate | 2: 2 strates | 5: 5 strates (0=1, 2=2, 2=3-4, 5=5)",
            "C – Bois morts sur pied" to "0: aucun | 2: BMg≥1/ha ou BMm≥1/ha | 5: BMg≥3/ha",
            "D – Bois morts au sol" to "0: aucun | 2: BMg≥1/ha ou BMm≥1/ha | 5: BMg≥3/ha",
            "E – Très gros bois vivants" to "0: TGB<1/ha et GB<1/ha | 2: TGB≥1/ha | 5: TGB≥5/ha",
            "F – Dendromicrohabitats" to "0: <2 arbres/ha | 2: 2-3 arbres/ha | 5: ≥5 arbres/ha (8+ types)",
            "G – Milieux ouverts florifers" to "0: 0% | 2: <1% ou lisières | 5: ≥1% surface",
            "H – Continuité temporelle" to "0: forêt récente (<30 ans) | 2: partielle | 5: forêt ancienne",
            "I – Milieux aquatiques" to "0: aucun type | 2: 1 type | 5: 2 types et plus",
            "J – Milieux rocheux" to "0: aucun type | 2: 1 type | 5: 2 types et plus"
        )
        criteria.forEach { (name, desc) ->
            val labelPaint = Paint().apply { color = Color.parseColor("#424242"); textSize = 9f; isFakeBoldText = true; isAntiAlias = true }
            canvas.drawText(name, margin, y, labelPaint)
            canvas.drawText(desc, margin + 150f, y, smallPaint)
            y += 13f
        }

        y += 10f

        // IBP source reference
        val refPaint = Paint().apply { color = Color.parseColor("#9E9E9E"); textSize = 8f; isAntiAlias = true; textSkewX = -0.25f }
        canvas.drawText("Source : IBP — Larrieu & Cabanettes (INRAE/ONF). Méthode de diagnostic de la biodiversité forestière.", margin, y, refPaint)
        y += 12f
        canvas.drawText("Référence : Guide IBP — méthode de diagnostic rapide de la biodiversité potentielle forestière.", margin, y, refPaint)

        drawFooter(canvas, "Rapport IBP — GeoSylva — Page 2/2", w, h)
    }

    private fun drawCriteriaTable(canvas: Canvas, eval: IbpEvaluation, criteria: List<IbpCriterionId>, x: Float, startY: Float, w: Int, accentColor: Int) {
        var y = startY
        val rowH = 28f
        val colWidths = floatArrayOf(60f, 200f, 50f, w - x * 2 - 320f)

        // Header
        val headerBg = Paint().apply { color = accentColor; isAntiAlias = true; alpha = 220 }
        canvas.drawRect(x, y, w - x, y + rowH, headerBg)
        val headPaint = Paint().apply { color = Color.WHITE; textSize = 9f; isFakeBoldText = true; isAntiAlias = true }
        canvas.drawText("Code", x + 6f, y + 18f, headPaint)
        canvas.drawText("Critère", x + colWidths[0] + 4f, y + 18f, headPaint)
        canvas.drawText("Score", x + colWidths[0] + colWidths[1] + 4f, y + 18f, headPaint)
        canvas.drawText("Option choisie", x + colWidths[0] + colWidths[1] + colWidths[2] + 4f, y + 18f, headPaint)
        y += rowH

        criteria.forEachIndexed { i, cid ->
            val rowBg = Paint().apply { color = if (i % 2 == 0) Color.parseColor("#FAFAFA") else Color.parseColor("#F3F3F3") }
            canvas.drawRect(x, y, w - x, y + rowH, rowBg)

            val score = eval.answers.get(cid)
            val isAnswered = score >= 0

            // Score cell color
            if (isAnswered) {
                val scoreCellColor = when (score) {
                    0 -> Color.parseColor("#FFCDD2")
                    2 -> Color.parseColor("#FFF9C4")
                    else -> Color.parseColor("#C8E6C9")
                }
                canvas.drawRect(x + colWidths[0] + colWidths[1], y + 2f, x + colWidths[0] + colWidths[1] + colWidths[2] - 2f, y + rowH - 2f, Paint().apply { color = scoreCellColor })
            }

            val cellPaint = Paint().apply { color = Color.parseColor("#424242"); textSize = 9f; isAntiAlias = true }
            val codePaint = Paint().apply { color = accentColor; textSize = 9f; isFakeBoldText = true; isAntiAlias = true }
            canvas.drawText(cid.displayCode, x + 6f, y + 18f, codePaint)
            canvas.drawText(ibpCriterionTitleStatic(cid), x + colWidths[0] + 4f, y + 18f, cellPaint)
            canvas.drawText(if (isAnswered) "$score / 5" else "—", x + colWidths[0] + colWidths[1] + 8f, y + 18f, cellPaint)
            if (isAnswered) {
                val optionShort = ibpOptionShort(cid, score)
                canvas.drawText(optionShort.take(42), x + colWidths[0] + colWidths[1] + colWidths[2] + 4f, y + 18f, cellPaint)
            }

            // Border
            val border = Paint().apply { color = Color.parseColor("#E0E0E0"); style = Paint.Style.STROKE; strokeWidth = 0.5f }
            canvas.drawRect(x, y, w - x, y + rowH, border)
            y += rowH
        }
    }

    private fun drawRadarChart(canvas: Canvas, eval: IbpEvaluation, x: Float, startY: Float, w: Int, h: Int) {
        val cx = x + w / 2f
        val cy = startY + h / 2f
        val outerR = minOf(w, h) / 2f * 0.82f
        val criteria = IbpCriterionId.ALL
        val n = criteria.size
        val angleStep = (2 * Math.PI / n).toFloat()

        // Background rings
        val ringPaint = Paint().apply { color = Color.parseColor("#E0E0E0"); style = Paint.Style.STROKE; strokeWidth = 1f; isAntiAlias = true }
        for (ring in 1..2) {
            val r = outerR * ring / 2f
            canvas.drawCircle(cx, cy, r, ringPaint)
        }
        canvas.drawCircle(cx, cy, outerR, ringPaint)

        // Axis lines
        val axisPaint = Paint().apply { color = Color.parseColor("#BDBDBD"); strokeWidth = 0.5f; isAntiAlias = true }
        criteria.forEachIndexed { i, _ ->
            val angle = angleStep * i - Math.PI.toFloat() / 2f
            canvas.drawLine(cx, cy, cx + outerR * kotlin.math.cos(angle), cy + outerR * kotlin.math.sin(angle), axisPaint)
        }

        // Data polygon
        val dataPath = Path()
        val dataPaint = Paint().apply {
            color = Color.parseColor("#2E7D32"); alpha = 100
            style = Paint.Style.FILL; isAntiAlias = true
        }
        val dataStroke = Paint().apply {
            color = Color.parseColor("#2E7D32"); style = Paint.Style.STROKE
            strokeWidth = 2f; isAntiAlias = true
        }
        criteria.forEachIndexed { i, cid ->
            val score = eval.answers.get(cid).coerceAtLeast(0)
            val r = outerR * score / 5f
            val angle = angleStep * i - Math.PI.toFloat() / 2f
            val px = cx + r * kotlin.math.cos(angle)
            val py = cy + r * kotlin.math.sin(angle)
            if (i == 0) dataPath.moveTo(px, py) else dataPath.lineTo(px, py)
        }
        dataPath.close()
        canvas.drawPath(dataPath, dataPaint)
        canvas.drawPath(dataPath, dataStroke)

        // Labels
        val labelPaint = Paint().apply { color = Color.parseColor("#424242"); textSize = 8f; isAntiAlias = true; textAlign = Paint.Align.CENTER }
        criteria.forEachIndexed { i, cid ->
            val angle = angleStep * i - Math.PI.toFloat() / 2f
            val r = outerR + 14f
            val lx = cx + r * kotlin.math.cos(angle)
            val ly = cy + r * kotlin.math.sin(angle)
            canvas.drawText(cid.displayCode, lx, ly + 3f, labelPaint)
        }
    }

    private fun drawFooter(canvas: Canvas, text: String, w: Int, h: Int) {
        val footerPaint = Paint().apply { color = Color.parseColor("#BDBDBD"); style = Paint.Style.STROKE; strokeWidth = 0.5f }
        canvas.drawLine(40f, h - 30f, w - 40f, h - 30f, footerPaint)
        val footTextPaint = Paint().apply { color = Color.parseColor("#9E9E9E"); textSize = 8f; isAntiAlias = true }
        canvas.drawText(text, 40f, h - 18f, footTextPaint)
    }

    private fun wrapText(canvas: Canvas, text: String, paint: Paint, x: Float, startY: Float, maxWidth: Float, lineH: Float) {
        var y = startY
        val words = text.split(" ")
        var line = ""
        for (word in words) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(test) > maxWidth) {
                if (line.isNotEmpty()) { canvas.drawText(line, x, y, paint); y += lineH }
                line = word
            } else { line = test }
        }
        if (line.isNotEmpty()) canvas.drawText(line, x, y, paint)
    }

    private fun ibpLevelColorInt(level: IbpLevel): Int = when (level) {
        IbpLevel.VERY_LOW  -> Color.parseColor("#C62828")
        IbpLevel.LOW       -> Color.parseColor("#E65100")
        IbpLevel.MEDIUM    -> Color.parseColor("#F9A825")
        IbpLevel.GOOD      -> Color.parseColor("#2E7D32")
        IbpLevel.VERY_GOOD -> Color.parseColor("#1565C0")
    }

    private fun ibpLevelLabel(level: IbpLevel): String = when (level) {
        IbpLevel.VERY_LOW  -> "Très faible potentiel"
        IbpLevel.LOW       -> "Faible potentiel"
        IbpLevel.MEDIUM    -> "Moyen potentiel"
        IbpLevel.GOOD      -> "Bon potentiel"
        IbpLevel.VERY_GOOD -> "Très bon potentiel"
    }

    private fun ibpLevelCommentShort(level: IbpLevel): String = when (level) {
        IbpLevel.VERY_LOW  -> "Peuplement peu favorable à la biodiversité — mesures urgentes nécessaires."
        IbpLevel.LOW       -> "Potentiel limité — quelques éléments peuvent être améliorés."
        IbpLevel.MEDIUM    -> "Potentiel moyen — maintien et enrichissement progressif recommandés."
        IbpLevel.GOOD      -> "Bon potentiel biodiversité — conserver et renforcer les éléments favorables."
        IbpLevel.VERY_GOOD -> "Excellent potentiel — gestion douce préservant tous les éléments habitats."
    }

    private fun ibpLevelRecommendationsStatic(level: IbpLevel): List<String> = when (level) {
        IbpLevel.VERY_LOW -> listOf(
            "Conserver et favoriser la diversification des essences indigènes.",
            "Laisser en place quelques arbres morts sur pied et au sol.",
            "Réduire la pression de coupe, favoriser les îlots de sénescence."
        )
        IbpLevel.LOW -> listOf(
            "Identifier et protéger les arbres à très forte valeur biologique.",
            "Maintenir le bois mort existant et en laisser se former.",
            "Favoriser le développement de la végétation du sous-bois."
        )
        IbpLevel.MEDIUM -> listOf(
            "Conserver les gros bois et les arbres porteurs de dendromicrohabitats.",
            "Renforcer les connexions avec les habitats voisins.",
            "Enrichir la lisière avec des espèces arbustives indigènes."
        )
        IbpLevel.GOOD -> listOf(
            "Maintenir la gestion actuelle favorable à la biodiversité.",
            "Renforcer la continuité forestière en preservant les vieux arbres."
        )
        IbpLevel.VERY_GOOD -> listOf(
            "Gestion très douce recommandée — préserver tous les éléments habitats.",
            "Documenter et valoriser ce peuplement comme référence territoriale."
        )
    }

    private fun ibpCriterionTitleStatic(id: IbpCriterionId): String = when (id) {
        IbpCriterionId.E1  -> "A – Essences autochtones"
        IbpCriterionId.E2  -> "B – Structure verticale"
        IbpCriterionId.GB  -> "E – Très gros bois vivants"
        IbpCriterionId.BMS -> "C – Bois morts sur pied"
        IbpCriterionId.BMC -> "D – Bois morts au sol"
        IbpCriterionId.DMH -> "F – Dendromicrohabitats (dmh)"
        IbpCriterionId.VS  -> "G – Milieux ouverts florifers"
        IbpCriterionId.CF  -> "H – Continuité temporelle"
        IbpCriterionId.CO  -> "I – Milieux aquatiques"
        IbpCriterionId.HC  -> "J – Milieux rocheux"
    }

    private fun ibpOptionShort(id: IbpCriterionId, score: Int): String {
        val idx = when (score) { 0 -> 0; 2 -> 1; 5 -> 2; else -> 0 }
        return when (id) {
            IbpCriterionId.E1  -> listOf("0-1 genre", "2 genres", "≥3 genres (subalp.) / ≥5 genres")[idx]
            IbpCriterionId.E2  -> listOf("1 strate", "2 strates", "5 strates")[idx]
            IbpCriterionId.GB  -> listOf("TGB<1/ha, GB<1/ha", "TGB≥1/ha", "TGB≥5/ha")[idx]
            IbpCriterionId.BMS -> listOf("Aucun", "BMg≥1/ha ou BMm≥1/ha", "BMg≥3/ha")[idx]
            IbpCriterionId.BMC -> listOf("Aucun", "BMg≥1/ha ou BMm≥1/ha", "BMg≥3/ha")[idx]
            IbpCriterionId.DMH -> listOf("<2 arbres/ha", "2–3 arbres/ha", "≥5 arbres/ha")[idx]
            IbpCriterionId.VS  -> listOf("0% surface", "<1% ou lisières", "≥1% surface")[idx]
            IbpCriterionId.CF  -> listOf("Forêt récente", "État boisé partiel", "Forêt ancienne")[idx]
            IbpCriterionId.CO  -> listOf("Aucun type", "1 type", "2 types et plus")[idx]
            IbpCriterionId.HC  -> listOf("Aucun type", "1 type", "2 types et plus")[idx]
        }
    }
}
