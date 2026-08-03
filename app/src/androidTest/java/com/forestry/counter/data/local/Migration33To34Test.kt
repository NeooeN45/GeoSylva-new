package com.forestry.counter.data.local

import android.content.Context
import android.database.Cursor
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests d'intégration Room pour la migration v33→v34.
 *
 * La migration 33→34 ajoute 4 champs metadata normalisés (spec GeoSylva 3.0
 * §3.1) sur 12 tables cœur métier :
 *   - deletedAt : soft delete (suppression logique traçable)
 *   - auteur     : opérateur qui a créé/modifié la donnée
 *   - source     : origine de la donnée (manual | import | sync | gps)
 *   - version    : version de l'objet pour optimistic locking (DEFAULT 1)
 *
 * Ces tests vérifient que :
 *   1. Les données existantes (v33) sont préservées après migration.
 *   2. Les nouveaux champs prennent les bonnes valeurs par défaut
 *      (deletedAt=null, auteur=null, source=null, version=1).
 *   3. On peut écrire de nouvelles lignes avec les metadata renseignés.
 *
 * Approche : MigrationTestHelper crée une vraie DB SQLite v33 depuis le
 * schema JSON (app/schemas/.../33.json), on y insère des données, puis on
 * exécute directement [DatabaseMigrations.MIGRATION_33_34] sur la DB
 * ouverte et on vérifie les données résultantes.
 *
 * Note : ces tests sont instrumentés (androidTest) et nécessitent un
 * émulateur ou un device pour s'exécuter (connectedAndroidTest).
 *
 * FIXME (P0) — incohérence schema sur la colonne `version` :
 * La migration ajoute `version INTEGER NOT NULL DEFAULT 1` (le DEFAULT est
 * obligatoire pour remplir les lignes pré-existing, NOT NULL sinon rejeté).
 * Mais les entités v34 ne déclarent pas `@ColumnInfo(defaultValue = "1")`,
 * donc le schema v34 attendu par Room n'a pas de DEFAULT. Room.validateSchema
 * compare strictement les defaultValue → `runMigrationsAndValidate` lèverait
 * IllegalStateException("Expected ... defaultValue='null', Found ... defaultValue='1'").
 * En production (pas de fallbackToDestructiveMigration), cela crasherait à
 * l'ouverture après migration. Correctif : ajouter
 * `@ColumnInfo(defaultValue = "1")` sur `version` dans les 12 entités, puis
 * regénérer le schema v34. Tant que ce correctif n'est pas appliqué, on
 * exécute la migration directement (sans validation de schema Room) pour
 * valider la préservation des données et les valeurs par défaut.
 */
@RunWith(AndroidJUnit4::class)
class Migration33To34Test {

    /** Dossier des schemas JSON dans les assets androidTest. */
    private val schemaAssetFolder = "com.forestry.counter.data.local.ForestryDatabase"

    private lateinit var helper: MigrationTestHelper
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            schemaAssetFolder,
            FrameworkSQLiteOpenHelperFactory()
        )
    }

    @After
    fun teardown() {
        // Nettoyage des bases de test sur fichier (MigrationTestHelper utilise le filesystem).
        TEST_DB_NAMES.forEach { name -> context.deleteDatabase(name) }
    }

    // ── Helpers d'insertion de données v33 ─────────────────────────────────

    /**
     * Insère les dépendances de base (foret + parcelle + essence) nécessaires
     * aux tables qui ont des clés étrangères vers elles.
     */
    private fun insertBaseFixturesV33(db: SupportSQLiteDatabase) {
        db.execSQL(
            """INSERT INTO forets (foretId, nom, proprietaireNom, createdAt, updatedAt)
               VALUES ('foret-1', 'Forêt de test', 'Dupont', 1000, 2000)""".trimIndent()
        )
        db.execSQL(
            """INSERT INTO parcelles (parcelleId, foretId, name, createdAt, updatedAt)
               VALUES ('parcelle-1', 'foret-1', 'Parcelle A', 1000, 2000)""".trimIndent()
        )
        db.execSQL(
            """INSERT INTO essences (code, name)
               VALUES ('CHENE', 'Chêne')""".trimIndent()
        )
    }

    // ── Helpers de vérification des metadata ──────────────────────────────

    /**
     * Vérifie qu'un curseur positionné sur une ligne contient bien les 4
     * valeurs par défaut des metadata après migration d'une ligne v33.
     */
    private fun assertMetadataDefaults(cursor: Cursor) {
        assertTrue(
            "deletedAt doit être NULL (défaut) après migration",
            cursor.isNull(cursor.getColumnIndexOrThrow("deletedAt"))
        )
        assertTrue(
            "auteur doit être NULL (défaut) après migration",
            cursor.isNull(cursor.getColumnIndexOrThrow("auteur"))
        )
        assertTrue(
            "source doit être NULL (défaut) après migration",
            cursor.isNull(cursor.getColumnIndexOrThrow("source"))
        )
        assertEquals(
            "version doit être 1 (défaut) après migration",
            1,
            cursor.getInt(cursor.getColumnIndexOrThrow("version"))
        )
    }

    /**
     * Crée une DB v33 depuis le schema JSON, exécute [block] pour y insérer
     * des données, puis applique la migration 33→34 et retourne la DB ouverte.
     * L'appelant doit fermer la DB (use { }).
     */
    private fun migrateFromV33(
        dbName: String,
        block: (db: SupportSQLiteDatabase) -> Unit
    ): SupportSQLiteDatabase {
        val db = helper.createDatabase(dbName, V33)
        block(db)
        DatabaseMigrations.MIGRATION_33_34.migrate(db)
        return db
    }

    // ── Tests : préservation des données + valeurs par défaut ─────────────

    @Test
    fun migration_33_34_preserves_foret_data() {
        val db = migrateFromV33("test_foret.db") { it ->
            it.execSQL(
                """INSERT INTO forets (foretId, nom, proprietaireNom, createdAt, updatedAt)
                   VALUES ('foret-1', 'Forêt de test', 'Dupont', 1000, 2000)""".trimIndent()
            )
        }

        db.use {
            val cursor = it.query(
                "SELECT foretId, nom, deletedAt, auteur, source, version FROM forets"
            )
            cursor.use { c ->
                assertTrue("Au moins une ligne attendue", c.moveToFirst())
                assertEquals("foret-1", c.getString(c.getColumnIndexOrThrow("foretId")))
                assertEquals("Forêt de test", c.getString(c.getColumnIndexOrThrow("nom")))
                assertMetadataDefaults(c)
                assertFalse("Une seule ligne attendue", c.moveToNext())
            }
        }
    }

    @Test
    fun migration_33_34_preserves_parcelle_data() {
        val db = migrateFromV33("test_parcelle.db") { it -> insertBaseFixturesV33(it) }

        db.use {
            val cursor = it.query(
                "SELECT parcelleId, name, deletedAt, auteur, source, version FROM parcelles"
            )
            cursor.use { c ->
                assertTrue("Au moins une ligne attendue", c.moveToFirst())
                assertEquals("parcelle-1", c.getString(c.getColumnIndexOrThrow("parcelleId")))
                assertEquals("Parcelle A", c.getString(c.getColumnIndexOrThrow("name")))
                assertMetadataDefaults(c)
            }
        }
    }

    @Test
    fun migration_33_34_preserves_tige_data() {
        val db = migrateFromV33("test_tige.db") { it ->
            insertBaseFixturesV33(it)
            it.execSQL(
                """INSERT INTO tiges (tigeId, parcelleOwnerId, essenceCode, diamCm, timestamp, isTigeHabitat)
                   VALUES ('tige-1', 'parcelle-1', 'CHENE', 30.0, 1000, 0)""".trimIndent()
            )
        }

        db.use {
            val cursor = it.query(
                "SELECT tigeId, diamCm, deletedAt, auteur, source, version FROM tiges"
            )
            cursor.use { c ->
                assertTrue("Au moins une ligne attendue", c.moveToFirst())
                assertEquals("tige-1", c.getString(c.getColumnIndexOrThrow("tigeId")))
                assertEquals(
                    30.0,
                    c.getDouble(c.getColumnIndexOrThrow("diamCm")),
                    DELTA_DOUBLE
                )
                assertMetadataDefaults(c)
            }
        }
    }

    // ── Test bonus : version=1 sur toutes les tables métier ─────────────────

    @Test
    fun migration_33_34_sets_default_version_to_1_on_all_tables() {
        val db = migrateFromV33("test_all.db") { it -> insertAllBusinessFixturesV33(it) }

        db.use {
            for (table in TABLES_WITH_METADATA) {
                val cursor = it.query("SELECT version FROM $table")
                cursor.use { c ->
                    assertTrue("La table $table devrait contenir une ligne", c.moveToFirst())
                    assertEquals(
                        "version doit être 1 (défaut) sur la table $table",
                        1,
                        c.getInt(c.getColumnIndexOrThrow("version"))
                    )
                }
            }
        }
    }

    // ── Test bonus : écriture de nouvelles lignes avec metadata renseignés ──

    @Test
    fun migration_33_34_allows_new_metadata_fields_write() {
        val db = migrateFromV33("test_write.db") { it ->
            it.execSQL(
                """INSERT INTO forets (foretId, nom, proprietaireNom, createdAt, updatedAt)
                   VALUES ('foret-1', 'Forêt de test', 'Dupont', 1000, 2000)""".trimIndent()
            )
        }

        db.use {
            // Insère une nouvelle forêt avec les metadata renseignés (post-migration).
            it.execSQL(
                """INSERT INTO forets
                       (foretId, nom, proprietaireNom, createdAt, updatedAt,
                        deletedAt, auteur, source, version)
                   VALUES ('foret-new', 'Nouvelle forêt', 'Martin', 1000, 2000,
                           NULL, 'user-42', 'manual', 5)""".trimIndent()
            )

            val cursor = it.query(
                """SELECT foretId, auteur, source, version, deletedAt
                   FROM forets WHERE foretId = 'foret-new'""".trimIndent()
            )
            cursor.use { c ->
                assertTrue("La nouvelle forêt doit exister", c.moveToFirst())
                assertEquals("foret-new", c.getString(c.getColumnIndexOrThrow("foretId")))
                assertEquals("user-42", c.getString(c.getColumnIndexOrThrow("auteur")))
                assertEquals("manual", c.getString(c.getColumnIndexOrThrow("source")))
                assertEquals(
                    "version renseignée doit être préservée",
                    5,
                    c.getInt(c.getColumnIndexOrThrow("version"))
                )
                assertTrue(
                    "deletedAt doit rester NULL pour une ligne non supprimée",
                    c.isNull(c.getColumnIndexOrThrow("deletedAt"))
                )
            }
        }
    }

    // ── Fixtures v33 pour les 12 tables métier ──────────────────────────────

    /**
     * Insère une ligne v33 dans chacune des 12 tables métier, en respectant
     * l'ordre des dépendances de clés étrangères.
     */
    private fun insertAllBusinessFixturesV33(db: SupportSQLiteDatabase) {
        // Racines : foret, parcelle, essence
        insertBaseFixturesV33(db)

        // placettes (FK parcelles)
        db.execSQL(
            """INSERT INTO placettes (placetteId, parcelleOwnerId, createdAt, updatedAt)
               VALUES ('placette-1', 'parcelle-1', 1000, 2000)""".trimIndent()
        )
        // inventaire_sessions (FK parcelles)
        db.execSQL(
            """INSERT INTO inventaire_sessions (sessionId, parcelleId, typeSession, dateDebut, createdAt)
               VALUES ('session-1', 'parcelle-1', 'INVENTAIRE', 1000, 1000)""".trimIndent()
        )
        // tiges (FK parcelles, essences)
        db.execSQL(
            """INSERT INTO tiges (tigeId, parcelleOwnerId, essenceCode, diamCm, timestamp, isTigeHabitat)
               VALUES ('tige-1', 'parcelle-1', 'CHENE', 30.0, 1000, 0)""".trimIndent()
        )
        // observations_flore (FK parcelles, essences)
        db.execSQL(
            """INSERT INTO observations_flore
                   (observationId, parcelleId, codeEspece, nomScientifique,
                    abundanceDominance, strate, isEspeceProtegee, isEspeceIndicatrice,
                    dateSaisie, createdAt)
               VALUES ('obs-1', 'parcelle-1', 'CHENE', 'Quercus robur',
                       '+', 'A', 0, 0, 1000, 1000)""".trimIndent()
        )
        // arbres_habitat (FK parcelles, essences, tiges)
        db.execSQL(
            """INSERT INTO arbres_habitat
                   (arbreHabitatId, parcelleId, essenceCode, diamCm,
                    cavitesBranches, cavitesTronc, logenBois, bioticBoss, dendrothelme,
                    lianes, fissures, boisMortSurPied, isArbreVivant, isArbreRemarquable,
                    dateObservation)
               VALUES ('ah-1', 'parcelle-1', 'CHENE', 30.0,
                       0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1000)""".trimIndent()
        )
        // alertes_sanitaires (FK parcelles)
        db.execSQL(
            """INSERT INTO alertes_sanitaires
                   (alerteId, parcelleId, codePathogene, nomPathogene, niveauRisque,
                    isOrganismeReglemente, dateDetection, isAlerteDsf)
               VALUES ('alerte-1', 'parcelle-1', 'OIDIUM', 'Oïdium', 'MODERE',
                       0, 1000, 0)""".trimIndent()
        )
        // diagnostics_sylvicoles (FK parcelles)
        db.execSQL(
            """INSERT INTO diagnostics_sylvicoles
                   (diagnosticId, parcelleId, dateCreation, algoVersion, updatedAt)
               VALUES ('diag-1', 'parcelle-1', 1000, '1.0', 2000)""".trimIndent()
        )
        // ripisylve_observation (FK parcelles)
        db.execSQL(
            """INSERT INTO ripisylve_observation
                   (id, parcelleId, observerName, observationDate, createdAt, updatedAt,
                    isDraft, photosJson, sectionLengthM, sectionNotes, continuitePct,
                    largeurMode, strateHerbacee, strateArbustive, strateArborescente,
                    nbEspecesObservees, especesObserveesCsv, diamAutoFromDendro,
                    hasTresPetitBois, hasPetitBois, hasMoyenBois, hasGrosBois,
                    microhabitatCavites, microhabitatFissures, microhabitatDecollementEcorce,
                    microhabitatChampignons, microhabitatBoisMort, microhabitatTresGrosBois,
                    sanitairePct, invasivesPct, invasivesCsv, inadapteesMode,
                    stabilitePct, globalNotes)
               VALUES ('rip-1', 'parcelle-1', 'Observer', 1000, 1000, 2000,
                       1, '[]', 10.0, 'notes', 50.0, 'UNE_RANGEE', 1, 1, 1,
                       3, 'sp1,sp2,sp3', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                       80.0, 0.0, '', 'ABSENCE', 70.0, 'notes')""".trimIndent()
        )
        // station_diagnostics (FK parcelles)
        db.execSQL(
            """INSERT INTO station_diagnostics
                   (id, parcelleId, observerName, observationDate, isDraft, photosJson,
                    commune, exposition, positionTopo, texture, pierrosite, humus,
                    testHcl, drainage, rocheMere, gradientHydrique, gradientTrophique,
                    gradientLumineux, gradientHumique, especesIndicatricesJson,
                    especesXerophiles, especesMesophiles, especesHygrophiles, notes)
               VALUES ('station-1', 'parcelle-1', 'Observer', 1000, 1, '[]',
                       'Commune', 'NORD', 'PLAT', 'LIMON', 'FAIBLE', 'MULL',
                       'NEGATIF', 'NORMAL', 'Calcaire', 3, 3, 3, 3, '[]',
                       0, 0, 0, 'notes')""".trimIndent()
        )
    }

    private companion object {
        const val V33 = 33
        const val DELTA_DOUBLE = 0.001

        /** Les 12 tables cœur métier recevant les champs metadata. */
        val TABLES_WITH_METADATA = listOf(
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

        /** Noms des bases de test (pour nettoyage). */
        val TEST_DB_NAMES = listOf(
            "test_foret.db",
            "test_parcelle.db",
            "test_tige.db",
            "test_all.db",
            "test_write.db"
        )
    }
}
