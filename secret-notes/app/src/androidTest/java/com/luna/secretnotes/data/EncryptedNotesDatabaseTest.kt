package com.luna.secretnotes.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom

@RunWith(AndroidJUnit4::class)
class EncryptedNotesDatabaseTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun noteRoundTripAndTrashAreEncryptedAtRest() {
        context.deleteDatabase("secret_notes.db")
        val key = ByteArray(32).also(SecureRandom()::nextBytes)
        val note = NoteRecord(
            id = "note-1",
            title = "Black hole observation",
            body = "This private note must never appear in plaintext on disk.",
            createdAt = 100L,
            updatedAt = 200L,
            folderId = "space",
            tagsJson = """["astronomy","private"]""",
            pinned = true
        )

        EncryptedNotesDatabase(context) { key.copyOf() }.use { database ->
            database.open()
            database.save(note)
            assertEquals(note, database.get(note.id))
            database.moveToTrash(note.id, 300L)
            assertFalse(database.list().any { it.id == note.id })
            assertEquals(1, database.list(includeTrash = true).size)
        }

        val file = context.getDatabasePath("secret_notes.db")
        val diskText = file.readBytes().toString(Charsets.ISO_8859_1)
        assertFalse(diskText.contains(note.title))
        assertFalse(diskText.contains(note.body))

        EncryptedNotesDatabase(context) { key.copyOf() }.use { reopened ->
            reopened.open()
            assertEquals(note.copy(deletedAt = 300L, updatedAt = 300L), reopened.get(note.id))
        }
        key.fill(0)
        context.deleteDatabase("secret_notes.db")
    }

    @Test
    fun wrongKeyCannotOpenExistingDatabase() {
        context.deleteDatabase("secret_notes.db")
        val key = ByteArray(32) { 0x31 }
        EncryptedNotesDatabase(context) { key.copyOf() }.use { it.open() }

        val wrongKey = ByteArray(32) { 0x72 }
        var rejected = false
        try {
            EncryptedNotesDatabase(context) { wrongKey.copyOf() }.use { it.open() }
        } catch (_: Exception) {
            rejected = true
        }
        assertTrue(rejected)
        context.deleteDatabase("secret_notes.db")
    }
}
