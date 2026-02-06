package com.forestry.counter

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.forestry.counter.data.local.ForestryDatabase
import com.forestry.counter.data.preferences.UserPreferencesManager
import com.forestry.counter.data.repository.CounterRepositoryImpl
import com.forestry.counter.data.repository.FormulaRepositoryImpl
import com.forestry.counter.data.repository.GroupRepositoryImpl
import com.forestry.counter.data.repository.ParcelleRepositoryImpl
import com.forestry.counter.data.repository.PlacetteRepositoryImpl
import com.forestry.counter.data.repository.EssenceRepositoryImpl
import com.forestry.counter.data.repository.TigeRepositoryImpl
import com.forestry.counter.data.repository.ParameterRepositoryImpl
import com.forestry.counter.domain.calculator.FormulaParser
import com.forestry.counter.domain.calculation.ForestryCalculator
import com.forestry.counter.domain.calculation.PeuplementAvantCoupeCalculator
import com.forestry.counter.domain.repository.CounterRepository
import com.forestry.counter.domain.repository.FormulaRepository
import com.forestry.counter.domain.repository.GroupRepository
import com.forestry.counter.domain.repository.ParcelleRepository
import com.forestry.counter.domain.repository.PlacetteRepository
import com.forestry.counter.domain.repository.EssenceRepository
import com.forestry.counter.domain.repository.TigeRepository
import com.forestry.counter.domain.repository.ParameterRepository
import com.forestry.counter.domain.usecase.export.ExportDataUseCase
import com.forestry.counter.domain.usecase.import.ImportDataUseCase
import com.forestry.counter.data.logging.CrashLogger
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.forestry.counter.domain.parameters.ParameterKeys
import com.forestry.counter.data.parameters.ParameterDefaults
import com.forestry.counter.domain.model.ParameterItem
import com.forestry.counter.domain.model.Essence

class ForestryCounterApplication : Application() {

    // Database
    lateinit var database: ForestryDatabase
        private set

    // Repositories
    lateinit var groupRepository: GroupRepository
        private set
    lateinit var counterRepository: CounterRepository
        private set
    lateinit var formulaRepository: FormulaRepository
        private set
    lateinit var parcelleRepository: ParcelleRepository
        private set
    lateinit var placetteRepository: PlacetteRepository
        private set
    lateinit var essenceRepository: EssenceRepository
        private set
    lateinit var tigeRepository: TigeRepository
        private set
    lateinit var parameterRepository: ParameterRepository
        private set

    // Preferences
    lateinit var userPreferences: UserPreferencesManager
        private set

    // Use cases
    lateinit var exportDataUseCase: ExportDataUseCase
        private set
    lateinit var importDataUseCase: ImportDataUseCase
        private set

    // Calculator
    lateinit var formulaParser: FormulaParser
        private set
    lateinit var forestryCalculator: ForestryCalculator
        private set
    lateinit var peuplementAvantCoupeCalculator: PeuplementAvantCoupeCalculator
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize database
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE counters ADD COLUMN decimalPlaces INTEGER")
                db.execSQL("ALTER TABLE counters ADD COLUMN initialValue REAL")
                db.execSQL("ALTER TABLE counters ADD COLUMN resetValue REAL")
                db.execSQL("ALTER TABLE counters ADD COLUMN soundEnabled INTEGER")
                db.execSQL("ALTER TABLE counters ADD COLUMN vibrationEnabled INTEGER")
                db.execSQL("ALTER TABLE counters ADD COLUMN vibrationIntensity INTEGER")
                db.execSQL("ALTER TABLE counters ADD COLUMN targetAction TEXT")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE placettes ADD COLUMN name TEXT") } catch (_: Throwable) {}
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Extend tiges with additional forestry fields
                try { db.execSQL("ALTER TABLE tiges ADD COLUMN numero INTEGER") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE tiges ADD COLUMN categorie TEXT") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE tiges ADD COLUMN qualite INTEGER") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE tiges ADD COLUMN defauts TEXT") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE tiges ADD COLUMN photoUri TEXT") } catch (_: Throwable) {}
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE essences ADD COLUMN colorHex TEXT") } catch (_: Throwable) {}
            }
        }
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Enrich parcelles with descriptive fields (nullable for safe migration)
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN shape TEXT") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN slopePct REAL") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN aspect TEXT") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN access TEXT") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN altitudeM REAL") } catch (_: Throwable) {}
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to parcelles (nullable for safe migration)
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN forestOwnerId TEXT") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN tolerancePct REAL") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN samplingMode TEXT") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN sampleAreaM2 REAL") } catch (_: Throwable) {}
                try { db.execSQL("ALTER TABLE parcelles ADD COLUMN targetSpeciesCsv TEXT") } catch (_: Throwable) {}
                try { db.execSQL("CREATE INDEX IF NOT EXISTS index_parcelles_forestOwnerId ON parcelles(forestOwnerId)") } catch (_: Throwable) {}
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_counters_groupOwnerId ON counters(groupOwnerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_counters_name ON counters(name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_counters_sortIndex ON counters(sortIndex)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_counters_groupOwnerId_sortIndex ON counters(groupOwnerId, sortIndex)")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS parcelles (parcelleId TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, surfaceHa REAL, objectifType TEXT, objectifVal REAL, srid INTEGER, remarks TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_parcelles_name ON parcelles(name)")

                db.execSQL("CREATE TABLE IF NOT EXISTS placettes (placetteId TEXT NOT NULL PRIMARY KEY, parcelleOwnerId TEXT NOT NULL, type TEXT, rayonM REAL, surfaceM2 REAL, centerWkt TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, FOREIGN KEY(parcelleOwnerId) REFERENCES parcelles(parcelleId) ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_placettes_parcelleOwnerId ON placettes(parcelleOwnerId)")

                db.execSQL("CREATE TABLE IF NOT EXISTS essences (code TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, categorie TEXT, densiteBoite REAL)")

                db.execSQL("CREATE TABLE IF NOT EXISTS parameters (key TEXT NOT NULL PRIMARY KEY, valueJson TEXT NOT NULL, updatedAt INTEGER NOT NULL)")

                db.execSQL("CREATE TABLE IF NOT EXISTS tiges (tigeId TEXT NOT NULL PRIMARY KEY, parcelleOwnerId TEXT NOT NULL, placetteOwnerId TEXT, essenceCode TEXT NOT NULL, diamCm REAL NOT NULL, hauteurM REAL, gpsWkt TEXT, precisionM REAL, altitudeM REAL, timestamp INTEGER NOT NULL, note TEXT, produit TEXT, fCoef REAL, valueEur REAL, FOREIGN KEY(parcelleOwnerId) REFERENCES parcelles(parcelleId) ON DELETE CASCADE, FOREIGN KEY(placetteOwnerId) REFERENCES placettes(placetteId) ON DELETE SET NULL, FOREIGN KEY(essenceCode) REFERENCES essences(code) ON DELETE RESTRICT)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tiges_parcelleOwnerId ON tiges(parcelleOwnerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tiges_placetteOwnerId ON tiges(placetteOwnerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tiges_essenceCode ON tiges(essenceCode)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tiges_diamCm ON tiges(diamCm)")
            }
        }

        database = Room.databaseBuilder(
            applicationContext,
            ForestryDatabase::class.java,
            ForestryDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
            .build()

        // Install crash logger (controlled via settings)
        CrashLogger.install(this)

        // Initialize calculator
        formulaParser = FormulaParser()

        // Initialize repositories
        groupRepository = GroupRepositoryImpl(
            database.groupDao(),
            database.counterDao(),
            database.formulaDao(),
            database.groupVariableDao()
        )

        counterRepository = CounterRepositoryImpl(
            database.counterDao(),
            database.formulaDao(),
            database.groupVariableDao(),
            formulaParser
        )

        formulaRepository = FormulaRepositoryImpl(
            database.formulaDao(),
            database.counterDao(),
            database.groupVariableDao(),
            formulaParser
        )

        // Forestry repositories
        parcelleRepository = ParcelleRepositoryImpl(database.parcelleDao())
        placetteRepository = PlacetteRepositoryImpl(database.placetteDao())
        essenceRepository = EssenceRepositoryImpl(database.essenceDao())
        tigeRepository = TigeRepositoryImpl(database.tigeDao())
        parameterRepository = ParameterRepositoryImpl(database.parameterDao())

        // Initialize forestry calculator
        forestryCalculator = ForestryCalculator(parameterRepository)
        // Initialize pre-harvest stand calculator
        peuplementAvantCoupeCalculator = PeuplementAvantCoupeCalculator()

        // Initialize preferences
        userPreferences = UserPreferencesManager(applicationContext)

        // Apply saved app language at startup
        try {
            val lang = runBlocking { userPreferences.appLanguage.first() }
            if (lang == "system") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            } else {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
            }
        } catch (_: Throwable) {}

        // Initialize use cases
        exportDataUseCase = ExportDataUseCase(
            applicationContext,
            groupRepository,
            counterRepository,
            formulaRepository
        )

        importDataUseCase = ImportDataUseCase(
            applicationContext,
            groupRepository,
            counterRepository,
            formulaRepository
        )

        // Seed default forestry parameters and essences if missing
        CoroutineScope(Dispatchers.IO).launch {
            val classes = parameterRepository.getParameter(ParameterKeys.CLASSES_DIAM).first()
            if (classes == null) {
                parameterRepository.setParameter(
                    ParameterItem(
                        key = ParameterKeys.CLASSES_DIAM,
                        valueJson = ParameterDefaults.classesDiametreJson
                    )
                )
            }
            val coefs = parameterRepository.getParameter(ParameterKeys.COEFS_VOLUME).first()
            if (coefs == null) {
                parameterRepository.setParameter(
                    ParameterItem(
                        key = ParameterKeys.COEFS_VOLUME,
                        valueJson = ParameterDefaults.coefsVolumeJson
                    )
                )
            }
            val heights = parameterRepository.getParameter(ParameterKeys.HAUTEURS_DEFAUT).first()
            if (heights == null) {
                parameterRepository.setParameter(
                    ParameterItem(
                        key = ParameterKeys.HAUTEURS_DEFAUT,
                        valueJson = ParameterDefaults.hauteursDefautJson
                    )
                )
            }

            val rules = parameterRepository.getParameter(ParameterKeys.RULES_PRODUITS).first()
            if (rules == null) {
                parameterRepository.setParameter(
                    ParameterItem(
                        key = ParameterKeys.RULES_PRODUITS,
                        valueJson = ParameterDefaults.reglesProduitsJson
                    )
                )
            }
            val prices = parameterRepository.getParameter(ParameterKeys.PRIX_MARCHE).first()
            if (prices == null) {
                parameterRepository.setParameter(
                    ParameterItem(
                        key = ParameterKeys.PRIX_MARCHE,
                        valueJson = ParameterDefaults.prixMarcheJson
                    )
                )
            }
            val heightModes = parameterRepository.getParameter(ParameterKeys.HEIGHT_MODES).first()
            if (heightModes == null) {
                parameterRepository.setParameter(
                    ParameterItem(
                        key = ParameterKeys.HEIGHT_MODES,
                        valueJson = "[]"
                    )
                )
            }

            // Tarif de cubage par défaut : Algan (le plus polyvalent)
            val tarifSel = parameterRepository.getParameter(ParameterKeys.TARIF_SELECTION).first()
            if (tarifSel == null) {
                parameterRepository.setParameter(
                    ParameterItem(
                        key = ParameterKeys.TARIF_SELECTION,
                        valueJson = """{"method":"ALGAN"}"""
                    )
                )
            }

            val ess = essenceRepository.getAllEssences().first()
            val canonicalEssences = listOf(
                        Essence(code = "CH_SESSILE",     name = "Chêne sessile",                 categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "CH_PEDONCULE",   name = "Chêne pédonculé",               categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "HETRE_COMMUN",   name = "Hêtre commun",                  categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "PIN_SYLVESTRE",  name = "Pin sylvestre",                 categorie = "Résineux", densiteBoite = null),
                        Essence(code = "EPICEA_COMMUN",  name = "Épicéa commun",                 categorie = "Résineux", densiteBoite = null),
                        Essence(code = "SAPIN_PECTINE",  name = "Sapin pectiné",                 categorie = "Résineux", densiteBoite = null),
                        Essence(code = "CHATAIGNIER",    name = "Châtaignier",                   categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "FRENE_ELEVE",    name = "Frêne élevé",                   categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "BOUL_VERRUQ",    name = "Bouleau verruqueux",            categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "BOUL_PUBESC",    name = "Bouleau pubescent",             categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "ERABLE_SYC",     name = "Érable sycomore",               categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "ERABLE_PLANE",   name = "Érable plane",                  categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "ERABLE_CHAMP",   name = "Érable champêtre",              categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "NOYER_COMMUN",   name = "Noyer commun",                  categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "PEUPLIER_NOIR",  name = "Peuplier noir",                 categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "PEUPLIER_TREMB", name = "Peuplier tremble",              categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "PEUPLIER_HYBR",  name = "Peuplier hybride",              categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "DOUGLAS_VERT",   name = "Douglas vert",                  categorie = "Résineux", densiteBoite = null),
                        Essence(code = "PIN_MARITIME",   name = "Pin maritime",                  categorie = "Résineux", densiteBoite = null),
                        Essence(code = "ROBINIER",       name = "Robinier faux-acacia",          categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "AULNE_GLUT",     name = "Aulne glutineux",               categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "AULNE_BLANC",    name = "Aulne blanc",                   categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "TIL_PET_FEUIL",  name = "Tilleul à petites feuilles",    categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "TIL_GR_FEUIL",   name = "Tilleul à grandes feuilles",    categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "ORME_CHAMP",     name = "Orme champêtre",                categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "ORME_LISSE",     name = "Orme lisse",                    categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "ORME_MONT",      name = "Orme montagne",                 categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "SAULE_BLANC",    name = "Saule blanc",                   categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "SAULE_FRAGILE",  name = "Saule fragile",                 categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "SAULE_MARSAULT", name = "Saule marsault",                categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "CHARME",         name = "Charme commun",                 categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "NOISETIER",      name = "Noisetier commun",              categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "SORB_OISEL",     name = "Sorbier des oiseleurs",         categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "ALISIER_BLANC",  name = "Alisier blanc",                 categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "ALISIER_TORM",   name = "Alisier torminal",              categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "MEL_EUROPE",     name = "Mélèze d’Europe",               categorie = "Résineux", densiteBoite = null),
                        Essence(code = "MEL_HYBRIDE",    name = "Mélèze hybride",                categorie = "Résineux", densiteBoite = null),
                        Essence(code = "PIN_NOIR_AUTR",  name = "Pin noir d’Autriche",           categorie = "Résineux", densiteBoite = null),
                        Essence(code = "PIN_LARICIO",    name = "Pin laricio de Corse",          categorie = "Résineux", densiteBoite = null),
                        Essence(code = "PIN_WEYMOUTH",   name = "Pin de Weymouth",               categorie = "Résineux", densiteBoite = null),
                        Essence(code = "POMMIER_SAUV",   name = "Pommier sauvage",               categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "POIRIER_SAUV",   name = "Poirier sauvage",               categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "TREMBLE",        name = "Tremble",                       categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "FUSAIN",         name = "Fusain d’Europe",               categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "HOUX",           name = "Houx",                          categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "GENEVRIER",      name = "Genévrier commun",              categorie = "Conifère", densiteBoite = null),
                        Essence(code = "IF",             name = "If commun",                     categorie = "Conifère", densiteBoite = null),
                        Essence(code = "NOYER_NOIR",     name = "Noyer noir",                    categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "CORMIER",        name = "Cormier",                       categorie = "Feuillu",  densiteBoite = null),
                        Essence(code = "CERISIER_MERIS", name = "Cerisier merisier",             categorie = "Feuillu",  densiteBoite = null)
            )

            if (ess.isEmpty()) {
                // Première installation : insère toute la liste canonique
                essenceRepository.insertEssences(canonicalEssences)
            } else {
                // Mise à jour d'une base existante : ajoute uniquement les essences manquantes
                val existingCodes = ess.map { it.code }.toSet()
                val missing = canonicalEssences.filter { it.code !in existingCodes }
                if (missing.isNotEmpty()) {
                    essenceRepository.insertEssences(missing)
                }
            }
        }
    }
}
