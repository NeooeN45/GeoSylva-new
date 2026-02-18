package com.forestry.counter.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Toutes les migrations Room de l'application, extraites de ForestryCounterApplication
 * pour faciliter la maintenance et la testabilité.
 */
object DatabaseMigrations {

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

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN forestOwnerId TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN tolerancePct REAL") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN samplingMode TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN sampleAreaM2 REAL") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN targetSpeciesCsv TEXT") } catch (_: Throwable) {}
            try { db.execSQL("CREATE INDEX IF NOT EXISTS index_parcelles_forestOwnerId ON parcelles(forestOwnerId)") } catch (_: Throwable) {}
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try { db.execSQL("ALTER TABLE placettes ADD COLUMN name TEXT") } catch (_: Throwable) {}
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
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
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN shape TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN slopePct REAL") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN aspect TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN access TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE parcelles ADD COLUMN altitudeM REAL") } catch (_: Throwable) {}
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try { db.execSQL("ALTER TABLE tiges ADD COLUMN qualiteDetail TEXT") } catch (_: Throwable) {}
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            try { db.execSQL("ALTER TABLE essences ADD COLUMN densiteBois REAL") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE essences ADD COLUMN qualiteTypique TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE essences ADD COLUMN typeCoupePreferee TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE essences ADD COLUMN usageBois TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE essences ADD COLUMN vitesseCroissance TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE essences ADD COLUMN hauteurMaxM REAL") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE essences ADD COLUMN diametreMaxCm REAL") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE essences ADD COLUMN toleranceOmbre TEXT") } catch (_: Throwable) {}
            try { db.execSQL("ALTER TABLE essences ADD COLUMN remarques TEXT") } catch (_: Throwable) {}
        }
    }

    /** Liste ordonnée de toutes les migrations pour Room.databaseBuilder */
    val ALL = arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
        MIGRATION_9_10, MIGRATION_10_11
    )
}
