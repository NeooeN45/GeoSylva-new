package com.forestry.counter.domain.calculation

import com.forestry.counter.domain.model.Tige
import com.forestry.counter.domain.parameters.ParameterKeys
import com.forestry.counter.domain.repository.ParameterRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt

class ForestryCalculator(
    private val parameterRepository: ParameterRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun diameterClasses(): List<Int> {
        val item = parameterRepository.getParameter(ParameterKeys.CLASSES_DIAM).first()
        return runCatching {
            if (item?.valueJson.isNullOrBlank()) emptyList() else json.decodeFromString<List<Int>>(item!!.valueJson)
        }.getOrElse { emptyList() }
    }

    suspend fun lookupF(essenceCode: String, diamCm: Double): Double? {
        val item = parameterRepository.getParameter(ParameterKeys.COEFS_VOLUME).first() ?: return null
        val list = runCatching { json.decodeFromString<List<CoefVolumeRange>>(item.valueJson) }.getOrNull() ?: return null
        val d = diamCm.toInt()
        return list.firstOrNull { it.essence.equals(essenceCode, true) && d >= it.min && d <= it.max }?.f
    }

    suspend fun lookupH(essenceCode: String, diamCm: Double): Double? {
        val item = parameterRepository.getParameter(ParameterKeys.HAUTEURS_DEFAUT).first() ?: return null
        val list = runCatching { json.decodeFromString<List<HeightDefaultRange>>(item.valueJson) }.getOrNull() ?: return null
        val d = diamCm.toInt()
        return list.firstOrNull { it.essence.equals(essenceCode, true) && d >= it.min && d <= it.max }?.h
    }

    private suspend fun heightModes(): List<HeightModeEntry> {
        val item = parameterRepository.getParameter(ParameterKeys.HEIGHT_MODES).first()
        val str = item?.valueJson ?: return emptyList()
        if (str.isBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<HeightModeEntry>>(str) }.getOrElse { emptyList() }
    }

    private suspend fun heightModeFor(essenceCode: String, diamClass: Int): HeightModeEntry? {
        val modes = heightModes()
        return modes.firstOrNull { it.essence.equals(essenceCode, true) && it.diamClass == diamClass }
    }

    suspend fun getHeightMode(essenceCode: String, diamClass: Int): HeightModeEntry? = heightModeFor(essenceCode, diamClass)

    suspend fun setHeightMode(entry: HeightModeEntry?) {
        val current = heightModes().toMutableList()
        // remove existing
        if (entry != null) {
            current.removeAll { it.essence.equals(entry.essence, true) && it.diamClass == entry.diamClass }
            // if DEFAULT with no fixed, we just remove any override
            if (entry.mode.equals("DEFAULT", true)) {
                // no-op after remove
            } else {
                current.add(entry)
            }
        }
        val jsonStr = json.encodeToString(current)
        // store back
        parameterRepository.setParameter(
            com.forestry.counter.domain.model.ParameterItem(
                key = ParameterKeys.HEIGHT_MODES,
                valueJson = jsonStr
            )
        )
    }

    fun computeG(diamCm: Double): Double {
        // G = pi * (DBH/200)^2 (m^2), DBH in cm
        val radiusM = diamCm / 200.0
        return PI * radiusM.pow(2)
    }

    suspend fun computeV(essenceCode: String, diamCm: Double, heightM: Double?): Double? {
        val h = heightM ?: lookupH(essenceCode, diamCm) ?: return null
        val f = lookupF(essenceCode, diamCm) ?: return null
        val g = computeG(diamCm)
        return f * g * h
    }

    suspend fun volumeForTige(tige: Tige): Double? {
        return computeV(
            essenceCode = tige.essenceCode,
            diamCm = tige.diamCm,
            heightM = tige.hauteurM
        )
    }

    private fun diamToClass(diamCm: Double): Int = diamCm.roundToInt()

    private fun mean(values: List<Double>): Double? = values.takeIf { it.isNotEmpty() }?.average()

    private fun weightedMean(values: List<Double>, weights: List<Double>): Double? {
        if (values.isEmpty() || values.size != weights.size) return null
        val sumW = weights.sum()
        if (sumW == 0.0) return null
        return values.zip(weights).sumOf { it.first * it.second } / sumW
    }

    private fun sampleHeightMeanInClass(tiges: List<Tige>, essenceCode: String, diamClass: Int): Double? {
        val hs = tiges.asSequence()
            .filter { it.essenceCode.equals(essenceCode, true) && diamToClass(it.diamCm) == diamClass }
            .mapNotNull { it.hauteurM }
            .toList()
        return mean(hs)
    }

    suspend fun resolveHeightForClass(essenceCode: String, diamClass: Int, tiges: List<Tige>): Double? {
        val mode = heightModeFor(essenceCode, diamClass)
        return when (mode?.mode?.uppercase()) {
            "FIXED" -> mode.fixed
            "SAMPLES" -> sampleHeightMeanInClass(tiges, essenceCode, diamClass) ?: lookupH(essenceCode, diamClass.toDouble())
            else -> lookupH(essenceCode, diamClass.toDouble())
        }
    }

    suspend fun synthesisForEssence(
        essenceCode: String,
        classes: List<Int>,
        tiges: List<Tige>
    ): Pair<List<ClassSynthesis>, SynthesisTotals> {
        val perClass = mutableListOf<ClassSynthesis>()
        val tigesEss = tiges.filter { it.essenceCode.equals(essenceCode, true) }
        var vTotal = 0.0
        var valueTotal = 0.0
        var nTotal = 0
        val diamAll = mutableListOf<Double>()
        val hAll = mutableListOf<Double>()
        val rules = productRules()
        val prices = priceEntries()

        for (d in classes) {
            val list = tigesEss.filter { diamToClass(it.diamCm) == d }
            val count = list.size
            nTotal += count
            val resolvedH = resolveHeightForClass(essenceCode, d, tigesEss)
            val heights = list.mapNotNull { it.hauteurM } + if (resolvedH != null && list.any { it.hauteurM == null }) List(list.count { it.hauteurM == null }) { resolvedH } else emptyList()
            val hMean = mean(heights)

            var vSum: Double? = null
            var valueSum: Double? = null
            if (count > 0) {
                var vs = 0.0
                var valSum = 0.0
                list.forEach { t ->
                    val h = t.hauteurM ?: resolvedH
                    val v = computeV(essenceCode, t.diamCm, h)
                    if (v != null) vs += v
                    diamAll += t.diamCm
                    if (h != null) hAll += h
                    if (v != null) {
                        val dClass = diamToClass(t.diamCm)
                        val prod = classifyProduct(essenceCode, dClass, rules)
                        val eurPerM3 = priceFor(essenceCode, prod, dClass, prices)
                        if (eurPerM3 != null) valSum += v * eurPerM3
                    }
                }
                vSum = vs
                vTotal += vs
                if (valSum > 0.0) {
                    valueSum = valSum
                    valueTotal += valSum
                }
            }
            perClass += ClassSynthesis(diamClass = d, count = count, hMean = hMean, vSum = vSum, valueSumEur = valueSum)
        }

        val dm = mean(diamAll)
        val hm = mean(hAll)
        val totals = SynthesisTotals(
            nTotal = nTotal,
            dmWeighted = dm,
            hMean = hm,
            vTotal = if (vTotal > 0.0) vTotal else null
        )
        return perClass to totals
    }

    private suspend fun productRules(): List<ProductRule> {
        val item = parameterRepository.getParameter(ParameterKeys.RULES_PRODUITS).first()
        val str = item?.valueJson ?: return emptyList()
        if (str.isBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<ProductRule>>(str) }.getOrElse { emptyList() }
    }

    private suspend fun priceEntries(): List<PriceEntry> {
        val item = parameterRepository.getParameter(ParameterKeys.PRIX_MARCHE).first()
        val str = item?.valueJson ?: return emptyList()
        if (str.isBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<PriceEntry>>(str) }.getOrElse { emptyList() }
    }

    private fun classifyProduct(essence: String, diamClass: Int, rules: List<ProductRule>): String {
        // First matching rule wins; fallback basic thresholds if none
        rules.forEach { r ->
            val essMatch = r.essence == null || r.essence == "*" || r.essence.equals(essence, true)
            val minOk = r.min?.let { diamClass >= it } ?: true
            val maxOk = r.max?.let { diamClass <= it } ?: true
            if (essMatch && minOk && maxOk) return r.product
        }
        // fallback heuristic
        return when {
            diamClass >= 35 -> "BO"
            diamClass >= 20 -> "BI"
            diamClass >= 7 -> "BCh"
            else -> "PATE"
        }
    }

    private fun priceFor(essence: String, product: String, diamClass: Int, prices: List<PriceEntry>): Double? {
        return prices.firstOrNull { it.essence.equals(essence, true) && it.product.equals(product, true) && diamClass >= it.min && diamClass <= it.max }?.eurPerM3
    }
}
