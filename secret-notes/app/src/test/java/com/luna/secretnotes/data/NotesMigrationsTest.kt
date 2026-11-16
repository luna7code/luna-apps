package com.luna.secretnotes.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesMigrationsTest {
    @Test fun freshDatabaseBuildsAllSchemaVersionsInOrder() {
        val statements = NotesMigrations.migrationPath(0)
        assertEquals(12, statements.size)
        assertTrue(statements.first().contains("CREATE TABLE notes"))
        assertTrue(statements.any { it.contains("ADD COLUMN tags") })
        assertTrue(statements.any { it.contains("ADD COLUMN deleted_at") })
        assertTrue(statements.any { it.contains("CREATE TABLE folders") })
        assertTrue(statements.any { it.contains("CREATE TABLE attachments") })
        assertTrue(statements.last().contains("index_attachments_note_id"))
    }
    @Test fun versionOneMigrationAddsPrivateMetadataAndAttachments() {
        val statements = NotesMigrations.migrationPath(1)
        assertEquals(11, statements.size)
        assertTrue(statements.any { it.contains("folder_id") })
        assertTrue(statements.any { it.contains("tags") })
        assertTrue(statements.any { it.contains("CREATE TABLE attachments") })
    }
    @Test fun currentVersionNeedsNoMigration() {
        assertTrue(NotesMigrations.migrationPath(NotesMigrations.VERSION).isEmpty())
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsDowngrade() {
        NotesMigrations.migrationPath(4, 3)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsFutureSchema() {
        NotesMigrations.migrationPath(5)
    }
}
