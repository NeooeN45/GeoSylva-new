package com.forestry.counter.domain.usecase.export

import com.forestry.counter.domain.model.IbpCriterionId
import com.forestry.counter.domain.model.IbpEvaluation
import com.forestry.counter.domain.model.IbpLevel
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object IbpCsvExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun export(
        evaluations: List<IbpEvaluation>,
        placetteNames: Map<String, String> = emptyMap(),
        out: OutputStream
    ) {
        val sb = StringBuilder()

        // Header
        val criteriaHeaders = IbpCriterionId.ALL.joinToString(",") { it.code }
        sb.appendLine("\"Placette\",\"Date\",\"Evaluateur\",\"Score_A\",\"Score_B\",\"Score_Total\",\"Niveau\",$criteriaHeaders,\"Complet\",\"Notes\"")

        // Rows
        for (eval in evaluations.sortedByDescending { it.observationDate }) {
            val name = placetteNames[eval.placetteId]?.let { csvEscape(it) } ?: csvEscape(eval.placetteId.take(12))
            val date = dateFormat.format(Date(eval.observationDate))
            val evaluator = csvEscape(eval.evaluatorName.ifBlank { "" })
            val scoreA = if (eval.scoreA >= 0) eval.scoreA.toString() else ""
            val scoreB = if (eval.scoreB >= 0) eval.scoreB.toString() else ""
            val scoreTotal = if (eval.scoreTotal >= 0) eval.scoreTotal.toString() else ""
            val level = if (eval.isComplete) IbpLevel.fromScore(eval.scoreTotal).name else ""
            val criteriaValues = IbpCriterionId.ALL.joinToString(",") { id ->
                val v = eval.answers.get(id)
                if (v >= 0) v.toString() else ""
            }
            val complete = if (eval.isComplete) "1" else "0"
            val notes = csvEscape(eval.globalNote)
            sb.appendLine("$name,\"$date\",$evaluator,$scoreA,$scoreB,$scoreTotal,\"$level\",$criteriaValues,$complete,$notes")
        }

        out.write(sb.toString().toByteArray(Charsets.UTF_8))
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else "\"$value\""
    }
}
