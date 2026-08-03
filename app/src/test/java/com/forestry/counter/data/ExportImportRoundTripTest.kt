package com.forestry.counter.data

import com.forestry.counter.domain.model.AppExport
import com.forestry.counter.domain.model.CounterExport
import com.forestry.counter.domain.model.FormulaExport
import com.forestry.counter.domain.model.GroupExport
import com.forestry.counter.domain.model.VariableExport
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests du contrat de round-trip export/import — spec GeoSylva 3.0 §11.
 *
 * Valide que le format d'échange ([AppExport]) est **sans perte** : encoder puis
 * décoder un export produit des données identiques. C'est le contrat de
 * restauration après crash ou migration appareil. Les use cases
 * [com.forestry.counter.domain.usecase.export.ExportDataUseCase] et
 * [com.forestry.counter.domain.usecase.import.ImportDataUseCase] ne sont que
 * des adaptateurs I/O (ContentResolver) au-dessus de ce contrat de
 * sérialisation ; la garantie « pas de perte de données » vit donc ici.
 *
 * Tests JVM purs (pas d'Android, pas de Robolectric) pour la vitesse.
 */
class ExportImportRoundTripTest {

    // Configuration JSON identique à celle des use cases (forward-compat).
    private val json: Json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    // ── Arrange : fabriques de fixtures ─────────────────────────────────────

    private fun counterExport(groupId: String, name: String): CounterExport =
        CounterExport(
            id = "counter-$name",
            groupId = groupId,
            groupName = "Groupe $groupId",
            name = name,
            value = 42.0,
            step = 0.5,
            min = 0.0,
            max = 100.0,
            bgColor = "#FF0000",
            fgColor = "#FFFFFF",
            targetValue = 50.0,
            tags = listOf("sapin", "hêtre")
        )

    private fun formulaExport(name: String): FormulaExport = FormulaExport(
        id = "formula-$name",
        name = name,
        expression = "a + b * 2",
        description = "Formule de test"
    )

    private fun variableExport(name: String): VariableExport = VariableExport(
        name = name,
        value = 12.5,
        description = "Variable de test"
    )

    private fun groupExport(id: String, name: String): GroupExport = GroupExport(
        id = id,
        name = name,
        color = "#00AA00",
        counters = listOf(counterExport(id, "Tige A"), counterExport(id, "Tige B")),
        formulas = listOf(formulaExport("Densité")),
        variables = listOf(variableExport("hauteur"))
    )

    private fun fullExport(): AppExport = AppExport(
        version = "3.0.0",
        exportDate = 1_700_000_000_000L,
        groups = listOf(groupExport("g1", "Forêt A"), groupExport("g2", "Forêt B"))
    )

    // ── Scénario 1 : Round-trip complet sans perte ──────────────────────────

    @Test
    fun should_preserve_all_data_when_round_tripping_full_export() {
        // Arrange
        val original = fullExport()

        // Act — export puis import (décodage)
        val encoded = json.encodeToString(original)
        val decoded: AppExport = json.decodeFromString(encoded)

        // Then — toutes les données sont identiques
        assertEquals(original, decoded)
    }

    // ── Scénario 2 : Restauration après crash (réinitialisation puis réimport) ─

    @Test
    fun should_restore_data_after_simulated_crash() {
        // Arrange — données initiales
        val original = fullExport()
        val encoded = json.encodeToString(original)

        // Act — on simule un crash : la référence « base courante » est perdue,
        // on recrée les données uniquement depuis l'export.
        val restored: AppExport = json.decodeFromString(encoded)

        // Then — les données restaurées sont identiques à l'original
        assertEquals(original, restored)
        assertEquals(2, restored.groups.size)
        assertEquals(2, restored.groups[0].counters.size)
        assertEquals(1, restored.groups[0].formulas.size)
    }

    // ── Scénario 3 : Export vide ────────────────────────────────────────────

    @Test
    fun should_round_trip_empty_export_without_error() {
        // Arrange
        val empty = AppExport(version = "3.0.0", exportDate = 0L, groups = emptyList())

        // Act
        val encoded = json.encodeToString(empty)
        val decoded: AppExport = json.decodeFromString(encoded)

        // Then — pas d'erreur, DB vide
        assertEquals(empty, decoded)
        assertTrue(decoded.groups.isEmpty())
    }

    // ── Scénario 4 : Fichier corrompu ───────────────────────────────────────

    @Test
    fun should_throw_serialization_exception_when_importing_corrupt_json() {
        // Arrange — JSON malformé (accolade non fermée, valeur invalide)
        val corrupt = """{"version":"3.0.0","exportDate":,"groups":[]"""

        // Then — l'erreur est propagée (pas de crash silencieux)
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<AppExport>(corrupt)
        }
    }

    @Test
    fun should_throw_when_importing_non_json_content() {
        // Arrange — contenu qui n'est pas du JSON du tout
        val notJson = "CECI N'EST PAS DU JSON"

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<AppExport>(notJson)
        }
    }

    // ── Scénario 5 : Préservation des metadata ──────────────────────────────

    @Test
    fun should_preserve_version_metadata_through_round_trip() {
        // Arrange
        val original = fullExport().copy(version = "3.0.0")

        // Act
        val encoded = json.encodeToString(original)
        val decoded: AppExport = json.decodeFromString(encoded)

        // Then — la metadata `version` est préservée
        assertEquals("3.0.0", decoded.version)
        assertEquals(original.version, decoded.version)
    }

    @Test
    fun should_preserve_export_date_through_round_trip() {
        val original = fullExport().copy(exportDate = 1_700_000_000_123L)

        val decoded: AppExport = json.decodeFromString(json.encodeToString(original))

        assertEquals(1_700_000_000_123L, decoded.exportDate)
    }

    @Test
    fun should_preserve_nullable_fields_through_round_trip() {
        // Arrange — champs nullables à null et liste vide
        val original = AppExport(
            version = "3.0.0",
            exportDate = 0L,
            groups = listOf(
                GroupExport(
                    id = "g-null",
                    name = "Forêt nullable",
                    color = null,
                    counters = listOf(
                        CounterExport(
                            id = "c-null",
                            groupId = "g-null",
                            groupName = "Forêt nullable",
                            name = "Tige nullable",
                            value = 0.0,
                            step = 1.0,
                            min = null,
                            max = null,
                            bgColor = null,
                            fgColor = null,
                            targetValue = null,
                            tags = emptyList()
                        )
                    ),
                    formulas = emptyList(),
                    variables = emptyList()
                )
            )
        )

        // Act
        val decoded: AppExport = json.decodeFromString(json.encodeToString(original))

        // Then — les champs nullables sont préservés (null ou valeur)
        assertEquals(original, decoded)
        assertEquals(null, decoded.groups[0].color)
        assertEquals(null, decoded.groups[0].counters[0].min)
        assertEquals(null, decoded.groups[0].counters[0].targetValue)
        assertTrue(decoded.groups[0].counters[0].tags.isEmpty())
    }

    // ── Forward-compatibilité : tolérance aux champs inconnus ────────────────

    @Test
    fun should_tolerate_unknown_keys_for_forward_compatibility() {
        // Arrange — un export futur ajoute des champs inconnus de la v3.0.0
        val futureJson = """
            {"version":"4.0.0","exportDate":1700000000000,
             "groups":[{"id":"g1","name":"Futur","color":"#000",
             "counters":[],"formulas":[],"variables":[],
             "futureField":"ignored"}],"futureTopLevel":"ignored"}
        """.trimIndent()

        // Act — l'import ne doit pas crasher (ignoreUnknownKeys = true)
        val decoded: AppExport = json.decodeFromString(futureJson)

        // Then
        assertEquals("4.0.0", decoded.version)
        assertEquals(1, decoded.groups.size)
        assertEquals("Futur", decoded.groups[0].name)
    }

    // ── Garde-fou : l'encodage est déterministe et stable ───────────────────

    @Test
    fun should_produce_stable_encoding_for_identical_export() {
        val original = fullExport()

        val first = json.encodeToString(original)
        val second = json.encodeToString(original)

        assertEquals(first, second)
    }

    @Test
    fun should_produce_distinct_encoding_for_distinct_exports() {
        val a = fullExport()
        val b = a.copy(version = "3.0.1")

        assertNotEquals(json.encodeToString(a), json.encodeToString(b))
    }
}
