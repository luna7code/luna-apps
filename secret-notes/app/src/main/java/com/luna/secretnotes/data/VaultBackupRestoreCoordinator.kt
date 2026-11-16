package com.luna.secretnotes.data

import android.content.Context
import com.luna.secretnotes.security.CredentialVault
import com.luna.secretnotes.security.VaultKeyMaterial
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File

/**
 * Installs a validated portable backup while the current database connection is closed.
 * The old database and attachment directory are kept as rollback copies until the
 * Android Keystore has successfully wrapped the restored DEK.
 */
class VaultBackupRestoreCoordinator(
    context: Context,
    private val vault: CredentialVault
) {
    private val appContext = context.applicationContext

    fun install(
        restored: EncryptedBackupManager.RestoredBackup,
        currentVaultPassword: CharArray
    ): VaultKeyMaterial {
        val importedKey = restored.copyDataKey()
        val databaseFile = appContext.getDatabasePath(DATABASE_NAME)
        val parent = databaseFile.parentFile ?: error("Database directory is unavailable")
        val attachments = File(appContext.noBackupFilesDir, ATTACHMENTS_DIRECTORY)
        val stagedDatabase = File(parent, DATABASE_NAME + ".restore-new")
        val stagedAttachments = File(attachments.parentFile, ATTACHMENTS_DIRECTORY + ".restore-new")
        val previousDatabase = File(parent, DATABASE_NAME + ".restore-old")
        val previousAttachments = File(attachments.parentFile, ATTACHMENTS_DIRECTORY + ".restore-old")
        var previousDatabaseMoved = false
        var previousAttachmentsMoved = false
        var newDatabaseInstalled = false
        var newAttachmentsInstalled = false

        try {
            validateDatabase(restored.databaseFile, importedKey)
            require(restored.attachmentsDirectory.isDirectory)

            stagedDatabase.delete()
            stagedAttachments.deleteRecursively()
            restored.databaseFile.copyTo(stagedDatabase, overwrite = true)
            copyDirectory(restored.attachmentsDirectory, stagedAttachments)

            previousDatabase.delete()
            previousAttachments.deleteRecursively()

            if (databaseFile.exists()) {
                check(databaseFile.renameTo(previousDatabase)) { "Cannot stage current database for rollback" }
                previousDatabaseMoved = true
            }
            if (attachments.exists()) {
                check(attachments.renameTo(previousAttachments)) { "Cannot stage current attachments for rollback" }
                previousAttachmentsMoved = true
            }

            check(stagedDatabase.renameTo(databaseFile)) { "Cannot install restored database" }
            newDatabaseInstalled = true
            check(stagedAttachments.renameTo(attachments)) { "Cannot install restored attachments" }
            newAttachmentsInstalled = true

            // This verifies the existing app credential and updates its Keystore-wrapped DEK.
            val newSession = vault.replaceVaultKey(currentVaultPassword, importedKey.copyOf())
            previousDatabase.delete()
            previousAttachments.deleteRecursively()
            return newSession
        } catch (error: Throwable) {
            if (newDatabaseInstalled) databaseFile.delete()
            if (newAttachmentsInstalled) attachments.deleteRecursively()
            if (previousDatabaseMoved) previousDatabase.renameTo(databaseFile)
            if (previousAttachmentsMoved) previousAttachments.renameTo(attachments)
            stagedDatabase.delete()
            stagedAttachments.deleteRecursively()
            throw error
        } finally {
            importedKey.fill(0)
            currentVaultPassword.fill('\u0000')
        }
    }

    private fun validateDatabase(file: File, key: ByteArray) {
        require(file.isFile && file.length() > 0) { "Backup database is missing" }
        System.loadLibrary("sqlcipher")
        val db = SQLiteDatabase.openOrCreateDatabase(file, key, null, null, null)
        try {
            val integrity = db.rawQuery("PRAGMA integrity_check", emptyArray()).use {
                require(it.moveToFirst())
                it.getString(0)
            }
            check(integrity == "ok") { "Backup database integrity check failed" }
            val hasNotesTable = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'notes' LIMIT 1",
                emptyArray()
            ).use { it.moveToFirst() }
            check(hasNotesTable) { "Backup does not contain a notes table" }
            check(db.version in 1..NotesMigrations.VERSION) { "Unsupported backup database version" }
        } finally {
            db.close()
        }
    }

    private fun copyDirectory(source: File, target: File) {
        check(target.mkdirs() || target.isDirectory)
        source.listFiles().orEmpty().forEach { child ->
            val destination = File(target, child.name)
            if (child.isDirectory) copyDirectory(child, destination)
            else child.copyTo(destination, overwrite = true)
        }
    }

    private companion object {
        const val DATABASE_NAME = "secret_notes.db"
        const val ATTACHMENTS_DIRECTORY = "encrypted-attachments"
    }
}
