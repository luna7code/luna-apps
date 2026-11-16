package com.luna.secretnotes.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

data class NoteRecord(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
    val folderId: String? = null,
    val tagsJson: String = "[]",
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val deletedAt: Long? = null
)

/**
 * SQLCipher-backed notes store. The random 256-bit DEK is supplied by the unlocked
 * vault and cleared from this class's temporary byte array immediately after open.
 * SQLCipher encrypts database pages, schema, indexes, journaling and note metadata.
 */
class EncryptedNotesDatabase(
    context: Context,
    private val keyProvider: () -> ByteArray
) : AutoCloseable {
    private val appContext = context.applicationContext
    private val databaseFile: File = appContext.getDatabasePath(DATABASE_NAME)
    private var database: SQLiteDatabase? = null
    private val closed = AtomicBoolean(false)

    @Synchronized
    fun open(): EncryptedNotesDatabase {
        check(!closed.get()) { "Database wrapper is closed" }
        if (database?.isOpen == true) return this
        databaseFile.parentFile?.mkdirs()
        System.loadLibrary(SQLCIPHER_LIBRARY)

        val key = keyProvider()
        require(key.size == DATA_KEY_BYTES) { "Database key must be 256 bits" }
        val db = try {
            SQLiteDatabase.openOrCreateDatabase(databaseFile, key, null, null, null)
        } finally {
            key.fill(0)
        }

        try {
            db.execSQL("PRAGMA cipher_memory_security = ON")
            db.execSQL("PRAGMA secure_delete = ON")
            db.execSQL("PRAGMA foreign_keys = ON")
            db.rawQuery("PRAGMA journal_mode = DELETE", emptyArray()).use { }
            migrate(db)
            database = db
            return this
        } catch (error: Throwable) {
            db.close()
            throw error
        }
    }

    @Synchronized
    fun save(note: NoteRecord) {
        val values = ContentValues().apply {
            put("id", note.id)
            put("title", note.title)
            put("body", note.body)
            put("created_at", note.createdAt)
            put("updated_at", note.updatedAt)
            put("folder_id", note.folderId)
            put("tags", note.tagsJson)
            put("is_pinned", if (note.pinned) 1 else 0)
            put("is_archived", if (note.archived) 1 else 0)
            if (note.deletedAt == null) putNull("deleted_at") else put("deleted_at", note.deletedAt)
        }
        requireDatabase().insertWithOnConflict(
            "notes", null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    @Synchronized
    fun get(id: String): NoteRecord? {
        val cursor = requireDatabase().rawQuery(
            "SELECT id,title,body,created_at,updated_at,folder_id,tags,is_pinned,is_archived,deleted_at FROM notes WHERE id = ? LIMIT 1",
            arrayOf(id)
        )
        return cursor.use { if (it.moveToFirst()) it.toNoteRecord() else null }
    }

    @Synchronized
    fun list(includeTrash: Boolean = false): List<NoteRecord> {
        val where = if (includeTrash) "" else " WHERE deleted_at IS NULL"
        val cursor = requireDatabase().rawQuery(
            "SELECT id,title,body,created_at,updated_at,folder_id,tags,is_pinned,is_archived,deleted_at FROM notes$where ORDER BY is_pinned DESC, updated_at DESC",
            emptyArray()
        )
        return cursor.use {
            buildList { while (it.moveToNext()) add(it.toNoteRecord()) }
        }
    }

    @Synchronized
    fun moveToTrash(id: String, deletedAt: Long) {
        requireDatabase().execSQL(
            "UPDATE notes SET deleted_at = ?, updated_at = ? WHERE id = ?",
            arrayOf<Any>(deletedAt, deletedAt, id)
        )
    }

    @Synchronized
    fun permanentlyDelete(id: String) {
        requireDatabase().delete("notes", "id = ?", arrayOf(id))
    }

    @Synchronized
    override fun close() {
        if (closed.compareAndSet(false, true)) {
            database?.close()
            database = null
        }
    }

    private fun migrate(db: SQLiteDatabase) {
        val current = db.version
        check(current <= NotesMigrations.VERSION) {
            "Database was created by a newer version of SECRET NOTES"
        }
        if (current == NotesMigrations.VERSION) return
        db.beginTransaction()
        try {
            NotesMigrations.migrationPath(current).forEach { statement -> db.execSQL(statement) }
            db.version = NotesMigrations.VERSION
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun requireDatabase(): SQLiteDatabase =
        checkNotNull(database?.takeIf { it.isOpen }) { "Encrypted database is not open" }

    private fun Cursor.toNoteRecord() = NoteRecord(
        id = getString(0),
        title = getString(1),
        body = getString(2),
        createdAt = getLong(3),
        updatedAt = getLong(4),
        folderId = if (isNull(5)) null else getString(5),
        tagsJson = getString(6),
        pinned = getInt(7) != 0,
        archived = getInt(8) != 0,
        deletedAt = if (isNull(9)) null else getLong(9)
    )

    private companion object {
        const val DATABASE_NAME = "secret_notes.db"
        const val SQLCIPHER_LIBRARY = "sqlcipher"
        const val DATA_KEY_BYTES = 32
    }
}
