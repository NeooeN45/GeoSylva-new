package com.forestry.counter.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * Test la migration 33→34 — ajout des champs metadata spec GeoSylva 3.0
 * (GEOSYLVA-003 §3.1) sur les entités cœur métier.
 *
 * Champs ajoutés : deletedAt, auteur, source, version
 * Tables impactées : forets, parcelles, placettes, tiges, inventaire_sessions,
 * essences, observations_flore, arbres_habitat, alertes_sanitaires,
 * diagnostics_sylvicoles, ripisylve_observation, station_diagnostics
 */
class DatabaseMigration33to34Test {

    private val tablesCible = listOf(
        "forets",
        "parcelles",
        "placettes",
        "tiges",
        "inventaire_sessions",
        "essences",
        "observations_flore",
        "arbres_habitat",
        "alertes_sanitaires",
        "diagnostics_sylvicoles",
        "ripisylve_observation",
        "station_diagnostics"
    )

    @Test
    fun `la migration ajoute deletedAt auteur source version sur toutes les tables cible`() {
        val database = mockk<SupportSQLiteDatabase>(relaxed = true)

        DatabaseMigrations.MIGRATION_33_34.migrate(database)

        for (table in tablesCible) {
            verify(atLeast = 1) {
                database.execSQL(match { sql -> sql.contains("ALTER TABLE $table ADD COLUMN deletedAt INTEGER") })
            }
            verify(atLeast = 1) {
                database.execSQL(match { sql -> sql.contains("ALTER TABLE $table ADD COLUMN auteur TEXT") })
            }
            verify(atLeast = 1) {
                database.execSQL(match { sql -> sql.contains("ALTER TABLE $table ADD COLUMN source TEXT") })
            }
            verify(atLeast = 1) {
                database.execSQL(match { sql -> sql.contains("ALTER TABLE $table ADD COLUMN version INTEGER NOT NULL DEFAULT 1") })
            }
        }
    }

    @Test
    fun `la migration est declaree de version 33 a 34`() {
        val migration = DatabaseMigrations.MIGRATION_33_34
        assert(migration.startVersion == 33) { "Version source attendue : 33, actuelle : ${migration.startVersion}" }
        assert(migration.endVersion == 34) { "Version cible attendue : 34, actuelle : ${migration.endVersion}" }
    }

    @Test
    fun `la migration est presente dans ALL`() {
        assert(DatabaseMigrations.ALL.contains(DatabaseMigrations.MIGRATION_33_34)) {
            "MIGRATION_33_34 doit être dans le tableau ALL pour être appliquée"
        }
    }
}
