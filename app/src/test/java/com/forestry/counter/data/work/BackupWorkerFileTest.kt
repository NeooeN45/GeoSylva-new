package com.forestry.counter.data.work

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BackupWorkerFileTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `complete temporary backup is published without duplication`() {
        val directory = folder.newFolder("backups")
        val temporary = directory.resolve(".backup.tmp")
        val target = directory.resolve("backup.zip")
        temporary.writeText("zip-content")

        assertTrue(publishBackup(temporary, target))
        assertFalse(temporary.exists())
        assertEquals("zip-content", target.readText())
    }

    @Test
    fun `empty backup is never published`() {
        val directory = folder.newFolder("empty")
        val temporary = directory.resolve(".backup.tmp")
        val target = directory.resolve("backup.zip")
        temporary.createNewFile()

        assertFalse(publishBackup(temporary, target))
        assertTrue(temporary.exists())
        assertFalse(target.exists())
    }

    @Test
    fun `existing archive is never overwritten`() {
        val directory = folder.newFolder("collision")
        val temporary = directory.resolve(".backup.tmp")
        val target = directory.resolve("backup.zip")
        temporary.writeText("new")
        target.writeText("existing")

        assertFalse(publishBackup(temporary, target))
        assertEquals("new", temporary.readText())
        assertEquals("existing", target.readText())
    }

    @Test
    fun `stale temporary backup is removed before export`() {
        val temporary = folder.newFile("stale.tmp")
        temporary.writeText("stale")

        assertTrue(resetTemporaryBackup(temporary))
        assertFalse(temporary.exists())
    }
}
