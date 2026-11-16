package com.luna.secretnotes.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.UUID

data class NoteRecord(
    val id: String, val title: String, val body: String, val createdAt: Long, val updatedAt: Long,
    val folderId: String? = null, val tagsJson: String = "[]", val pinned: Boolean = false,
    val archived: Boolean = false, val deletedAt: Long? = null
)
data class FolderRecord(val id: String, val name: String, val createdAt: Long, val updatedAt: Long)
data class AttachmentRecord(
    val id: String, val noteId: String, val encryptedFileName: String, val displayName: String,
    val mimeType: String, val sizeBytes: Long, val createdAt: Long
)

/** SQLCipher notebook. All user metadata, notes, folders and attachment records are encrypted. */
class EncryptedNotesDatabase(
    context: Context,
    private val keyProvider: () -> ByteArray
) : AutoCloseable {
    private val appContext = context.applicationContext
    private val databaseFile: File = appContext.getDatabasePath(DATABASE_NAME)
    private val attachmentsDirectory = File(appContext.noBackupFilesDir, "encrypted-attachments")
    private var database: SQLiteDatabase? = null
    private val closed = AtomicBoolean(false)

    @Synchronized fun open(): EncryptedNotesDatabase {
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
            db.rawQuery("PRAGMA journal_mode = DELETE", emptyArray()).use { it.moveToFirst() }
            migrate(db)
            database = db
            return this
        } catch (error: Throwable) {
            db.close()
            throw error
        }
    }

    @Synchronized fun save(note: NoteRecord) {
        val values = ContentValues().apply {
            put("id", note.id); put("title", note.title); put("body", note.body)
            put("created_at", note.createdAt); put("updated_at", note.updatedAt)
            if (note.folderId == null) putNull("folder_id") else put("folder_id", note.folderId)
            put("tags", note.tagsJson); put("is_pinned", if (note.pinned) 1 else 0)
            put("is_archived", if (note.archived) 1 else 0)
            if (note.deletedAt == null) putNull("deleted_at") else put("deleted_at", note.deletedAt)
        }
        val db = requireDatabase()
        val updated = db.update("notes", values, "id = ?", arrayOf(note.id))
        if (updated == 0) {
            db.insertOrThrow("notes", null, values)
        }
    }

    @Synchronized fun get(id: String): NoteRecord? = queryNotes(" WHERE id = ? LIMIT 1", arrayOf(id)).firstOrNull()

    @Synchronized fun list(includeTrash: Boolean = false, archived: Boolean? = false, folderId: String? = null): List<NoteRecord> {
        val clauses = mutableListOf(if (includeTrash) "deleted_at IS NOT NULL" else "deleted_at IS NULL")
        if (archived != null) clauses += "is_archived = ${if (archived) 1 else 0}"
        if (folderId != null) clauses += "folder_id = ?"
        return queryNotes(clauses.joinToString(" AND ", " WHERE "), if (folderId == null) emptyArray() else arrayOf(folderId))
    }

    @Synchronized fun search(
        query: String,
        includeTrash: Boolean = false,
        archived: Boolean? = false,
        folderId: String? = null
    ): List<NoteRecord> {
        val clauses = mutableListOf(
            "(title LIKE ? ESCAPE '!' OR body LIKE ? ESCAPE '!' OR tags LIKE ? ESCAPE '!')",
            if (includeTrash) "deleted_at IS NOT NULL" else "deleted_at IS NULL"
        )
        if (archived != null) clauses += "is_archived = ${if (archived) 1 else 0}"
        if (folderId != null) clauses += "folder_id = ?"
        val escaped = query.replace("!", "!!").replace("%", "!%").replace("_", "!_")
        val pattern = "%$escaped%"
        val args = mutableListOf(pattern, pattern, pattern)
        if (folderId != null) args += folderId
        return queryNotes(clauses.joinToString(" AND ", " WHERE "), args.toTypedArray())
    }

    @Synchronized fun setPinned(id: String, pinned: Boolean, updatedAt: Long) {
        requireDatabase().execSQL("UPDATE notes SET is_pinned = ?, updated_at = ? WHERE id = ?", arrayOf<Any>(if (pinned) 1 else 0, updatedAt, id))
    }
    @Synchronized fun setArchived(id: String, archived: Boolean, updatedAt: Long) {
        requireDatabase().execSQL("UPDATE notes SET is_archived = ?, updated_at = ? WHERE id = ?", arrayOf<Any>(if (archived) 1 else 0, updatedAt, id))
    }
    @Synchronized fun moveToTrash(id: String, deletedAt: Long) {
        requireDatabase().execSQL("UPDATE notes SET deleted_at = ?, updated_at = ? WHERE id = ?", arrayOf<Any>(deletedAt, deletedAt, id))
    }
    @Synchronized fun restoreFromTrash(id: String, updatedAt: Long) {
        requireDatabase().execSQL("UPDATE notes SET deleted_at = NULL, updated_at = ? WHERE id = ?", arrayOf<Any>(updatedAt, id))
    }
    @Synchronized fun permanentlyDelete(id: String) {
        val attached = listAttachments(id)
        requireDatabase().delete("notes", "id = ?", arrayOf(id))
        attached.forEach { newAttachmentCipher().delete(it.encryptedFileName, it.id) }
    }

    @Synchronized fun emptyTrash() {
        val attached = listTrashedAttachments()
        requireDatabase().delete("notes", "deleted_at IS NOT NULL", null)
        attached.forEach { newAttachmentCipher().delete(it.encryptedFileName, it.id) }
    }

    @Synchronized fun deleteAttachment(id: String) {
        val attachment = findAttachment(id) ?: return
        requireDatabase().delete("attachments", "id = ?", arrayOf(id))
        newAttachmentCipher().delete(attachment.encryptedFileName, attachment.id)
    }

    @Synchronized fun saveFolder(folder: FolderRecord) {
        val values = ContentValues().apply {
            put("id", folder.id); put("name", folder.name)
            put("created_at", folder.createdAt); put("updated_at", folder.updatedAt)
        }
        val db = requireDatabase()
        if (db.update("folders", values, "id = ?", arrayOf(folder.id)) == 0) db.insertOrThrow("folders", null, values)
    }
    @Synchronized fun listFolders(): List<FolderRecord> {
        val cursor = requireDatabase().rawQuery("SELECT id,name,created_at,updated_at FROM folders ORDER BY name COLLATE NOCASE", emptyArray())
        return cursor.use { buildList { while (it.moveToNext()) add(FolderRecord(it.getString(0), it.getString(1), it.getLong(2), it.getLong(3))) } }
    }
    @Synchronized fun deleteFolder(id: String) {
        val db = requireDatabase(); db.beginTransaction()
        try {
            db.execSQL("UPDATE notes SET folder_id = NULL WHERE folder_id = ?", arrayOf(id))
            db.delete("folders", "id = ?", arrayOf(id)); db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    @Synchronized fun saveAttachment(attachment: AttachmentRecord) {
        val values = ContentValues().apply {
            put("id", attachment.id); put("note_id", attachment.noteId)
            put("encrypted_file_name", attachment.encryptedFileName)
            put("display_name", attachment.displayName); put("mime_type", attachment.mimeType)
            put("size_bytes", attachment.sizeBytes); put("created_at", attachment.createdAt)
        }
        val db = requireDatabase()
        if (db.update("attachments", values, "id = ?", arrayOf(attachment.id)) == 0) {
            db.insertOrThrow("attachments", null, values)
        }
    }
    @Synchronized fun listAttachments(noteId: String): List<AttachmentRecord> {
        val cursor = requireDatabase().rawQuery(
            "SELECT id,note_id,encrypted_file_name,display_name,mime_type,size_bytes,created_at FROM attachments WHERE note_id = ? ORDER BY created_at",
            arrayOf(noteId)
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) add(AttachmentRecord(
                    it.getString(0), it.getString(1), it.getString(2), it.getString(3),
                    it.getString(4), it.getLong(5), it.getLong(6)
                ))
            }
        }
    }
    @Synchronized fun deleteAttachmentMetadata(id: String) { requireDatabase().delete("attachments", "id = ?", arrayOf(id)) }

    @Synchronized
    fun createBackupSnapshot(stagingRoot: File): BackupSnapshot {
        check(database?.isOpen == true) { "Database must be open for backup" }
        val snapshotDirectory = File(stagingRoot, "snapshot-${UUID.randomUUID()}")
        check(snapshotDirectory.mkdirs()) { "Cannot create backup snapshot directory" }
        try {
            val databaseCopy = File(snapshotDirectory, DATABASE_NAME)
            databaseFile.copyTo(databaseCopy, overwrite = true)
            val attachmentsCopy = File(snapshotDirectory, ATTACHMENTS_DIRECTORY)
            attachmentsCopy.mkdirs()
            attachmentsDirectory.listFiles().orEmpty().filter { it.isFile }.forEach { source ->
                source.copyTo(File(attachmentsCopy, source.name), overwrite = true)
            }
            return BackupSnapshot(snapshotDirectory, databaseCopy, attachmentsCopy)
        } catch (error: Throwable) {
            snapshotDirectory.deleteRecursively()
            throw error
        }
    }

    data class BackupSnapshot(
        private val directory: File,
        val databaseFile: File,
        val attachmentsDirectory: File
    ) : AutoCloseable {
        override fun close() {
            directory.deleteRecursively()
        }
    }

    fun encryptedDatabaseFileForBackup(): File {
        check(database?.isOpen == true) { "Database must be open for backup" }
        return databaseFile
    }

    fun encryptedAttachmentsDirectoryForBackup(): File {
        check(database?.isOpen == true) { "Database must be open for backup" }
        if (!attachmentsDirectory.exists()) attachmentsDirectory.mkdirs()
        return attachmentsDirectory
    }

    fun newAttachmentCipher(): ChunkedAttachmentCipher = ChunkedAttachmentCipher(attachmentsDirectory)
    fun copyVaultKeyForAttachment(): ByteArray {
        check(database?.isOpen == true) { "Vault database is not open" }
        val key = keyProvider()
        require(key.size == DATA_KEY_BYTES)
        return key
    }

    @Synchronized override fun close() {
        if (closed.compareAndSet(false, true)) { database?.close(); database = null }
    }

    private fun findAttachment(id: String): AttachmentRecord? {
        val cursor = requireDatabase().rawQuery(
            "SELECT id,note_id,encrypted_file_name,display_name,mime_type,size_bytes,created_at FROM attachments WHERE id = ? LIMIT 1",
            arrayOf(id)
        )
        return cursor.use {
            if (!it.moveToFirst()) null else AttachmentRecord(
                it.getString(0), it.getString(1), it.getString(2), it.getString(3),
                it.getString(4), it.getLong(5), it.getLong(6)
            )
        }
    }

    private fun listTrashedAttachments(): List<AttachmentRecord> {
        val cursor = requireDatabase().rawQuery(
            """SELECT a.id,a.note_id,a.encrypted_file_name,a.display_name,a.mime_type,a.size_bytes,a.created_at
               FROM attachments a INNER JOIN notes n ON n.id = a.note_id WHERE n.deleted_at IS NOT NULL""",
            emptyArray()
        )
        return cursor.use {
            buildList {
                while (it.moveToNext()) add(AttachmentRecord(
                    it.getString(0), it.getString(1), it.getString(2), it.getString(3),
                    it.getString(4), it.getLong(5), it.getLong(6)
                ))
            }
        }
    }

    private fun queryNotes(selection: String, args: Array<String>): List<NoteRecord> {
        val cursor = requireDatabase().rawQuery(
            "SELECT id,title,body,created_at,updated_at,folder_id,tags,is_pinned,is_archived,deleted_at FROM notes$selection ORDER BY is_pinned DESC, updated_at DESC",
            args
        )
        return cursor.use { buildList { while (it.moveToNext()) add(it.toNoteRecord()) } }
    }
    private fun migrate(db: SQLiteDatabase) {
        val current = db.version
        check(current <= NotesMigrations.VERSION) { "Database was created by a newer version of SECRET NOTES" }
        if (current == NotesMigrations.VERSION) return
        db.beginTransaction()
        try {
            NotesMigrations.migrationPath(current).forEach { db.execSQL(it) }
            db.version = NotesMigrations.VERSION
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    private fun requireDatabase(): SQLiteDatabase = checkNotNull(database?.takeIf { it.isOpen }) { "Encrypted database is not open" }
    private fun Cursor.toNoteRecord() = NoteRecord(
        id = getString(0), title = getString(1), body = getString(2),
        createdAt = getLong(3), updatedAt = getLong(4),
        folderId = if (isNull(5)) null else getString(5), tagsJson = getString(6),
        pinned = getInt(7) != 0, archived = getInt(8) != 0,
        deletedAt = if (isNull(9)) null else getLong(9)
    )
    private companion object {
        const val DATABASE_NAME = "secret_notes.db"
        const val SQLCIPHER_LIBRARY = "sqlcipher"
        const val DATA_KEY_BYTES = 32
    }
}
