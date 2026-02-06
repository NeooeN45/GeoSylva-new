package com.forestry.counter.domain.calculation

import com.forestry.counter.domain.model.Tige
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Calcul reproduisant le tableau Excel "Peuplement avant coupe" pour les résineux (Douglas en priorité).
 *
 * Les classes de diamètre sont fixées à : 20,25,30,35,40,45,50,55,60,65,70,75 cm.
 */
class PeuplementAvantCoupeCalculator {

    data class ClassRow(
        val diamClassCm: Int,
        val nByEssence: Map<String, Int>,
        val nTotal: Int,
        val nHa: Double,
        val pctNCategory: Double?,
        val gUnit: Double,
        val gParcelle: Double,
        val gHa: Double,
        val pctGCategory: Double?,
        val dmContribution: Double, // (D_m * N_classe) terme pour la moyenne pondérée globale
        val vPerTree: Double,
        val vTotal: Double,
        val vTrituPerTree: Double,
        val vTrituClass: Double,
        val vBoisOeuvreClass: Double,
        val pctBoisNonTritu: Double,
        val hMoyenne: Double
    )

    data class GlobalTotals(
        val nTotal: Int,
        val nHaTotal: Double,
        val gParcelleTotal: Double,
        val gHaTotal: Double,
        val dmGlobalM: Double,
        val surfaceTerriereMoyenne: Double,
        val sPct: Double,
        val vTotal: Double,
        val vTritu: Double,
        val vBoisOeuvre: Double,
        val vTotalHa: Double,
        val vTrituHa: Double,
        val vBoisOeuvreHa: Double,
        val pctBoisNonTrituGlobal: Double,
        val pctEssenceByTiges: Map<String, Double>
    )

    data class ResultatPeuplementAvantCoupe(
        val classes: List<ClassRow>,
        val totals: GlobalTotals
    )

    /**
     * Calcul principal.
     * @param tiges toutes les tiges de la placette (ou de la parcelle si pas de placette), déjà filtrées si besoin.
     * @param surfacePlacetteM2 surface en m². Si <= 0, le résultat sera tout à 0.
     * @param hoM hauteur dominante Ho (m).
     * @param hauteurMoyenneParClasse dictionnaire diam_cm -> hauteur moyenne (m). Si absent, on retombe sur Ho.
     */
    fun compute(
        tiges: List<Tige>,
        surfacePlacetteM2: Double?,
        hoM: Double?,
        hauteurMoyenneParClasse: Map<Int, Double>,
        allowedClasses: List<Int> = listOf(20,25,30,35,40,45,50,55,60,65,70,75),
        pctBoisNonTrituTable: Map<Int, Double> = defaultPctBoisNonTritu
    ): ResultatPeuplementAvantCoupe {
        val surfM2 = surfacePlacetteM2 ?: 0.0
        if (surfM2 <= 0.0 || tiges.isEmpty()) {
            val emptyRows = allowedClasses.map { d ->
                ClassRow(
                    diamClassCm = d,
                    nByEssence = emptyMap(),
                    nTotal = 0,
                    nHa = 0.0,
                    pctNCategory = null,
                    gUnit = gUnitForD(d.toDouble()),
                    gParcelle = 0.0,
                    gHa = 0.0,
                    pctGCategory = null,
                    dmContribution = 0.0,
                    vPerTree = 0.0,
                    vTotal = 0.0,
                    vTrituPerTree = 0.0,
                    vTrituClass = 0.0,
                    vBoisOeuvreClass = 0.0,
                    pctBoisNonTritu = pctBoisNonTrituTable[d] ?: 0.0,
                    hMoyenne = hauteurMoyenneParClasse[d] ?: (hoM ?: 0.0)
                )
            }
            val totals = GlobalTotals(
                nTotal = 0,
                nHaTotal = 0.0,
                gParcelleTotal = 0.0,
                gHaTotal = 0.0,
                dmGlobalM = 0.0,
                surfaceTerriereMoyenne = 0.0,
                sPct = 0.0,
                vTotal = 0.0,
                vTritu = 0.0,
                vBoisOeuvre = 0.0,
                vTotalHa = 0.0,
                vTrituHa = 0.0,
                vBoisOeuvreHa = 0.0,
                pctBoisNonTrituGlobal = 0.0,
                pctEssenceByTiges = emptyMap()
            )
            return ResultatPeuplementAvantCoupe(emptyRows, totals)
        }

        val surfaceHa = surfM2 / 10000.0
        val ho = hoM ?: 0.0

        // Regrouper les tiges par classe de diamètre arrondie à la classe fixée.
        val byClassAndEssence: Map<Int, Map<String, List<Tige>>> = tiges.groupBy { diamToClassFixed(it.diamCm, allowedClasses) }
            .mapValues { (_, list) -> list.groupBy { it.essenceCode } }

        val nByClass: MutableMap<Int, Int> = mutableMapOf()
        val gParcelleByClass: MutableMap<Int, Double> = mutableMapOf()
        val vTotalByClass: MutableMap<Int, Double> = mutableMapOf()
        val vTrituByClass: MutableMap<Int, Double> = mutableMapOf()
        val vBoisOeuvreByClass: MutableMap<Int, Double> = mutableMapOf()

        val classRows = mutableListOf<ClassRow>()

        // Pré-calcul pour catégories de grosseur
        fun isCat1(d: Int) = d == 20 || d == 25
        fun isCat2(d: Int) = d == 30 || d == 35 || d == 40 || d == 45
        fun isCat3(d: Int) = d == 50 || d == 55 || d == 60 || d == 65 || d == 70 || d == 75

        // Remplir les lignes par classe
        for (d in allowedClasses) {
            val essMap = byClassAndEssence[d] ?: emptyMap()
            val nByEss = essMap.mapValues { (_, list) -> list.size }
            val nTotalClass = nByEss.values.sum()
            val nHa = if (surfaceHa > 0.0) nTotalClass / surfaceHa else 0.0

            val gUnit = gUnitForD(d.toDouble())
            val gParcelle = gUnit * nTotalClass
            val gHa = if (surfaceHa > 0.0) gParcelle / surfaceHa else 0.0

            val hClasse = hauteurMoyenneParClasse[d] ?: ho

            val dM = d / 100.0
            val vTotalClasse = if (nTotalClass > 0 && hClasse > 0.0) {
                volumeTotalClasse(dM, hClasse, nTotalClass)
            } else 0.0

            val vParTige = if (nTotalClass > 0) vTotalClasse / nTotalClass else 0.0

            val pctNonTritu = pctBoisNonTrituTable[d] ?: 0.0
            val pctTritu = (100.0 - pctNonTritu).coerceAtLeast(0.0)
            val vTrituPerTree = vParTige * (pctTritu / 100.0)
            val vTrituClasse = vTrituPerTree * nTotalClass
            val vBoisOeuvreClasse = vTotalClasse - vTrituClasse

            nByClass[d] = nTotalClass
            gParcelleByClass[d] = gParcelle
            vTotalByClass[d] = vTotalClasse
            vTrituByClass[d] = vTrituClasse
            vBoisOeuvreByClass[d] = vBoisOeuvreClasse

            val dmContribution = dM * nTotalClass

            classRows += ClassRow(
                diamClassCm = d,
                nByEssence = nByEss,
                nTotal = nTotalClass,
                nHa = nHa,
                pctNCategory = null, // rempli après
                gUnit = gUnit,
                gParcelle = gParcelle,
                gHa = gHa,
                pctGCategory = null, // rempli après
                dmContribution = dmContribution,
                vPerTree = vParTige,
                vTotal = vTotalClasse,
                vTrituPerTree = vTrituPerTree,
                vTrituClass = vTrituClasse,
                vBoisOeuvreClass = vBoisOeuvreClasse,
                pctBoisNonTritu = pctNonTritu,
                hMoyenne = hClasse
            )
        }

        val nTotal = nByClass.values.sum()
        val nHaTotal = if (surfaceHa > 0.0) nTotal / surfaceHa else 0.0
        val gParcelleTotal = gParcelleByClass.values.sum()
        val gHaTotal = if (surfaceHa > 0.0) gParcelleTotal / surfaceHa else 0.0

        val sommeDxN = classRows.sumOf { it.dmContribution }
        val dmGlobalM = if (nTotal > 0) sommeDxN / nTotal else 0.0

        val surfaceTerriereMoyenne = if (nTotal > 0) gParcelleTotal / nTotal else 0.0

        val sPct = if (nHaTotal > 0.0 && ho > 0.0) {
            10746.0 / (ho * sqrt(nHaTotal))
        } else 0.0

        val vTotalGlobal = vTotalByClass.values.sum()
        val vTrituGlobal = vTrituByClass.values.sum()
        val vBoisOeuvreGlobal = vBoisOeuvreByClass.values.sum()

        val facteurHa = if (surfaceHa > 0.0) 1.0 / surfaceHa else 0.0
        val vTotalHa = vTotalGlobal * facteurHa
        val vTrituHa = vTrituGlobal * facteurHa
        val vBoisOeuvreHa = vBoisOeuvreGlobal * facteurHa

        val pctBoisNonTrituGlobal = if (vTotalGlobal > 0.0) {
            (vBoisOeuvreGlobal / vTotalGlobal) * 100.0
        } else 0.0

        val nParEssenceTotal: Map<String, Int> = byClassAndEssence
            .flatMap { (_, emap) -> emap.mapValues { it.value.size }.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, list) -> list.sum() }

        val pctEssenceByTiges = nParEssenceTotal.mapValues { (_, n) ->
            if (nTotal > 0) n.toDouble() / nTotal.toDouble() else 0.0
        }

        // Pourcentages par catégories (N et G), appliqués uniquement sur les lignes de début de catégorie.
        val nCat1 = allowedClasses.filter { isCat1(it) }.sumOf { nByClass[it] ?: 0 }
        val nCat2 = allowedClasses.filter { isCat2(it) }.sumOf { nByClass[it] ?: 0 }
        val nCat3 = allowedClasses.filter { isCat3(it) }.sumOf { nByClass[it] ?: 0 }

        val gCat1 = allowedClasses.filter { isCat1(it) }.sumOf { gParcelleByClass[it] ?: 0.0 }
        val gCat2 = allowedClasses.filter { isCat2(it) }.sumOf { gParcelleByClass[it] ?: 0.0 }
        val gCat3 = allowedClasses.filter { isCat3(it) }.sumOf { gParcelleByClass[it] ?: 0.0 }

        val classesWithPct = classRows.map { row ->
            val pctN = when {
                isCat1(row.diamClassCm) && row.diamClassCm == 20 -> if (nTotal > 0) nCat1.toDouble() / nTotal else 0.0
                isCat2(row.diamClassCm) && row.diamClassCm == 30 -> if (nTotal > 0) nCat2.toDouble() / nTotal else 0.0
                isCat3(row.diamClassCm) && row.diamClassCm == 50 -> if (nTotal > 0) nCat3.toDouble() / nTotal else 0.0
                else -> null
            }
            val pctG = when {
                isCat1(row.diamClassCm) && row.diamClassCm == 20 -> if (gParcelleTotal > 0.0) gCat1 / gParcelleTotal else 0.0
                isCat2(row.diamClassCm) && row.diamClassCm == 30 -> if (gParcelleTotal > 0.0) gCat2 / gParcelleTotal else 0.0
                isCat3(row.diamClassCm) && row.diamClassCm == 50 -> if (gParcelleTotal > 0.0) gCat3 / gParcelleTotal else 0.0
                else -> null
            }
            row.copy(pctNCategory = pctN, pctGCategory = pctG)
        }

        val totals = GlobalTotals(
            nTotal = nTotal,
            nHaTotal = nHaTotal,
            gParcelleTotal = gParcelleTotal,
            gHaTotal = gHaTotal,
            dmGlobalM = dmGlobalM,
            surfaceTerriereMoyenne = surfaceTerriereMoyenne,
            sPct = sPct,
            vTotal = vTotalGlobal,
            vTritu = vTrituGlobal,
            vBoisOeuvre = vBoisOeuvreGlobal,
            vTotalHa = vTotalHa,
            vTrituHa = vTrituHa,
            vBoisOeuvreHa = vBoisOeuvreHa,
            pctBoisNonTrituGlobal = pctBoisNonTrituGlobal,
            pctEssenceByTiges = pctEssenceByTiges
        )

        return ResultatPeuplementAvantCoupe(classesWithPct, totals)
    }

    private fun gUnitForD(diamCm: Double): Double {
        val dM = diamCm / 100.0
        return (PI / 4.0) * dM.pow(2.0)
    }

    private fun volumeTotalClasse(dM: Double, hM: Double, nClasse: Int): Double {
        return ((0.24868 * dM.pow(2.0) * hM) + 0.03179 * (dM * hM - 0.02473)) * nClasse.toDouble()
    }

    private fun diamToClassFixed(diamCm: Double, allowed: List<Int>): Int {
        // Approximer au plus proche des classes autorisées (20..75 step 5)
        val dInt = diamCm.toInt()
        return allowed.minByOrNull { kotlin.math.abs(it - dInt) } ?: dInt
    }

    companion object {
        val defaultPctBoisNonTritu: Map<Int, Double> = mapOf(
            20 to 79.56,
            25 to 90.24,
            30 to 91.03,
            35 to 94.66,
            40 to 94.69,
            45 to 96.44,
            50 to 96.34,
            55 to 97.38,
            60 to 0.0,
            65 to 0.0,
            70 to 0.0,
            75 to 0.0
        )
    }
}
