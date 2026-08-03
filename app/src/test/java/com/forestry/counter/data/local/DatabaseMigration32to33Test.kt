package com.forestry.counter.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class DatabaseMigration32to33Test {
    @Test
    fun `la migration cree la file et son index de relance`() {
        val database = mockk<SupportSQLiteDatabase>(relaxed = true)

        DatabaseMigrations.MIGRATION_32_33.migrate(database)

        verify {
            database.execSQL(match { sql -> sql.contains("CREATE TABLE IF NOT EXISTS parcel_sync_queue") })
        }
        verify {
            database.execSQL(match { sql -> sql.contains("index_parcel_sync_state_next") })
        }
    }
}
