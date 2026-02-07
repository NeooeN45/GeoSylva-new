package com.forestry.counter.domain.calculation.quality

import org.junit.Assert.*
import org.junit.Test

class ProductClassifierTest {

    // ═══════════════════════════════════════════════════════════
    // Feuillus premium (qualité A)
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `chene sessile A D=60 gives MERAIN`() {
        val r = ProductClassifier.classify("CH_SESSILE", "Feuillu", 60.0, WoodQualityGrade.A)
        assertEquals(ForestProduct.MERAIN, r.primary)
    }

    @Test
    fun `chene sessile A D=50 gives TRANCHAGE`() {
        val r = ProductClassifier.classify("CH_SESSILE", "Feuillu", 50.0, WoodQualityGrade.A)
        assertEquals(ForestProduct.TRANCHAGE, r.primary)
    }

    @Test
    fun `hetre A D=50 gives TRANCHAGE`() {
        val r = ProductClassifier.classify("HETRE_COMMUN", "Feuillu", 50.0, WoodQualityGrade.A)
        assertEquals(ForestProduct.TRANCHAGE, r.primary)
    }

    @Test
    fun `noyer A D=45 gives TRANCHAGE`() {
        val r = ProductClassifier.classify("NOYER_COMMUN", "Feuillu", 45.0, WoodQualityGrade.A)
        assertEquals(ForestProduct.TRANCHAGE, r.primary)
    }

    @Test
    fun `feuillu A D=40 gives SCIAGE_QUAL`() {
        val r = ProductClassifier.classify("FRENE_ELEVE", "Feuillu", 40.0, WoodQualityGrade.A)
        assertEquals(ForestProduct.SCIAGE_QUAL, r.primary)
    }

    // ═══════════════════════════════════════════════════════════
    // Résineux premium (qualité A)
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `douglas A D=40 gives GRUME_LONGUE`() {
        val r = ProductClassifier.classify("DOUGLAS_VERT", "Résineux", 40.0, WoodQualityGrade.A)
        assertEquals(ForestProduct.GRUME_LONGUE, r.primary)
    }

    @Test
    fun `resineux A D=25 gives POTEAU_LIGNE`() {
        val r = ProductClassifier.classify("PIN_SYLVESTRE", "Résineux", 25.0, WoodQualityGrade.A)
        assertEquals(ForestProduct.POTEAU_LIGNE, r.primary)
    }

    // ═══════════════════════════════════════════════════════════
    // Qualité B
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `qualite B D=40 gives SCIAGE_QUAL`() {
        val r = ProductClassifier.classify("CH_PEDONCULE", "Feuillu", 40.0, WoodQualityGrade.B)
        assertEquals(ForestProduct.SCIAGE_QUAL, r.primary)
    }

    @Test
    fun `qualite B D=30 gives SCIAGE_STD`() {
        val r = ProductClassifier.classify("HETRE_COMMUN", "Feuillu", 30.0, WoodQualityGrade.B)
        assertEquals(ForestProduct.SCIAGE_STD, r.primary)
    }

    // ═══════════════════════════════════════════════════════════
    // Qualité C
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `qualite C D=30 gives SCIAGE_STD`() {
        val r = ProductClassifier.classify("EPICEA_COMMUN", "Résineux", 30.0, WoodQualityGrade.C)
        assertEquals(ForestProduct.SCIAGE_STD, r.primary)
    }

    @Test
    fun `qualite C D=22 gives PALETTE`() {
        val r = ProductClassifier.classify("PIN_MARITIME", "Résineux", 22.0, WoodQualityGrade.C)
        assertEquals(ForestProduct.PALETTE, r.primary)
    }

    // ═══════════════════════════════════════════════════════════
    // Qualité D / petits diamètres
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `qualite D D=15 gives BOIS_INDUSTRIE`() {
        val r = ProductClassifier.classify("CHARME", "Feuillu", 15.0, WoodQualityGrade.D)
        assertEquals(ForestProduct.BOIS_INDUSTRIE, r.primary)
    }

    @Test
    fun `tres petit diametre feuillu gives BOIS_CHAUFFAGE`() {
        val r = ProductClassifier.classify("CHARME", "Feuillu", 8.0, WoodQualityGrade.D)
        assertEquals(ForestProduct.BOIS_CHAUFFAGE, r.primary)
    }

    @Test
    fun `tres petit diametre resineux gives BOIS_ENERGIE`() {
        val r = ProductClassifier.classify("EPICEA_COMMUN", "Résineux", 8.0, WoodQualityGrade.D)
        assertEquals(ForestProduct.BOIS_ENERGIE, r.primary)
    }

    // ═══════════════════════════════════════════════════════════
    // Résultats cohérents
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `classification always has primary product`() {
        val essences = listOf("CH_SESSILE", "HETRE_COMMUN", "DOUGLAS_VERT", "PIN_SYLVESTRE")
        val grades = WoodQualityGrade.entries
        val diameters = listOf(10.0, 20.0, 30.0, 40.0, 50.0, 60.0)
        for (ess in essences) {
            for (g in grades) {
                for (d in diameters) {
                    val r = ProductClassifier.classify(ess, "Feuillu", d, g)
                    assertNotNull("Primary should not be null for $ess/$g/D=$d", r.primary)
                    assertTrue("Note should not be empty", r.qualityNote.isNotBlank())
                }
            }
        }
    }
}
