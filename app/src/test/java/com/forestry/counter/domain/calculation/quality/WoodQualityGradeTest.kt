package com.forestry.counter.domain.calculation.quality

import org.junit.Assert.*
import org.junit.Test

class WoodQualityGradeTest {

    // ═══════════════════════════════════════════════════════════
    // QualityAssessment — score et grade
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `score max 12 gives grade A`() {
        val a = QualityAssessment("t1", rectitude = 3, branchage = 3, etatSanitaire = 3, defautsFut = 3)
        assertEquals(12, a.score)
        assertEquals(WoodQualityGrade.A, a.grade)
    }

    @Test
    fun `score 10 gives grade A`() {
        val a = QualityAssessment("t1", rectitude = 3, branchage = 3, etatSanitaire = 2, defautsFut = 2)
        assertEquals(10, a.score)
        assertEquals(WoodQualityGrade.A, a.grade)
    }

    @Test
    fun `score 9 gives grade B`() {
        val a = QualityAssessment("t1", rectitude = 3, branchage = 2, etatSanitaire = 2, defautsFut = 2)
        assertEquals(9, a.score)
        assertEquals(WoodQualityGrade.B, a.grade)
    }

    @Test
    fun `score 7 gives grade B`() {
        val a = QualityAssessment("t1", rectitude = 2, branchage = 2, etatSanitaire = 2, defautsFut = 1)
        assertEquals(7, a.score)
        assertEquals(WoodQualityGrade.B, a.grade)
    }

    @Test
    fun `score 6 gives grade C`() {
        val a = QualityAssessment("t1", rectitude = 2, branchage = 2, etatSanitaire = 1, defautsFut = 1)
        assertEquals(6, a.score)
        assertEquals(WoodQualityGrade.C, a.grade)
    }

    @Test
    fun `score 4 gives grade C`() {
        val a = QualityAssessment("t1", rectitude = 1, branchage = 1, etatSanitaire = 1, defautsFut = 1)
        assertEquals(4, a.score)
        assertEquals(WoodQualityGrade.C, a.grade)
    }

    @Test
    fun `score 3 gives grade D`() {
        val a = QualityAssessment("t1", rectitude = 1, branchage = 1, etatSanitaire = 1, defautsFut = 0)
        assertEquals(3, a.score)
        assertEquals(WoodQualityGrade.D, a.grade)
    }

    @Test
    fun `score 0 gives grade D`() {
        val a = QualityAssessment("t1", rectitude = 0, branchage = 0, etatSanitaire = 0, defautsFut = 0)
        assertEquals(0, a.score)
        assertEquals(WoodQualityGrade.D, a.grade)
    }

    @Test
    fun `default assessment has score 8 and grade B`() {
        val a = QualityAssessment("t1")
        assertEquals(8, a.score)
        assertEquals(WoodQualityGrade.B, a.grade)
    }

    // ═══════════════════════════════════════════════════════════
    // WoodQualityGrade — multiplicateurs
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `grade multipliers are ordered A gt B gt C gt D`() {
        assertTrue(WoodQualityGrade.A.multiplier > WoodQualityGrade.B.multiplier)
        assertTrue(WoodQualityGrade.B.multiplier > WoodQualityGrade.C.multiplier)
        assertTrue(WoodQualityGrade.C.multiplier > WoodQualityGrade.D.multiplier)
    }

    @Test
    fun `grade C multiplier is 1_0 (reference)`() {
        assertEquals(1.0, WoodQualityGrade.C.multiplier, 0.001)
    }

    @Test
    fun `grade D multiplier is positive`() {
        assertTrue(WoodQualityGrade.D.multiplier > 0.0)
    }

    // ═══════════════════════════════════════════════════════════
    // ForestProduct — codes
    // ═══════════════════════════════════════════════════════════

    @Test
    fun `all products have unique codes`() {
        val codes = ForestProduct.entries.map { it.code }
        assertEquals(codes.size, codes.distinct().size)
    }

    @Test
    fun `feuillu products exist`() {
        val feu = ForestProduct.entries.filter { it.isFeuillus }
        assertTrue("Should have feuillu products", feu.isNotEmpty())
        assertTrue(feu.any { it == ForestProduct.MERAIN })
        assertTrue(feu.any { it == ForestProduct.TRANCHAGE })
    }

    @Test
    fun `resineux products exist`() {
        val res = ForestProduct.entries.filter { it.isResineux }
        assertTrue("Should have resineux products", res.isNotEmpty())
        assertTrue(res.any { it == ForestProduct.GRUME_LONGUE })
        assertTrue(res.any { it == ForestProduct.POTEAU_LIGNE })
    }
}
