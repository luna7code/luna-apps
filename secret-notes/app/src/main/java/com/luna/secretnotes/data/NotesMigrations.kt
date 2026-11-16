package com.luna.secretnotes.data

/**
 * Ordered SQL migrations for the encrypted SQLCipher database.
 * These statements execute only after SQLCipher has authenticated and opened the DB.
 */
object NotesMigrations {
    const val VERSION = 3

    val from1To2 = listOf(
        "ALTER TABLE notes ADD COLUMN folder_id TEXT",
        "ALTER TABLE notes ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'"
    )

    val from2To3 = listOf(
        "ALTER TABLE notes ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE notes ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0",
        "ALTER TABLE notes ADD COLUMN deleted_at INTEGER",
        """CREATE TABLE folders (
            id TEXT PRIMARY KEY NOT NULL,
            name TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL
        )""",
        "CREATE INDEX index_notes_updated_at ON notes(updated_at)",
        "CREATE INDEX index_notes_folder_id ON notes(folder_id)",
        "CREATE INDEX index_notes_deleted_at ON notes(deleted_at)"
    )

    fun migrationPath(fromVersion: Int, toVersion: Int = VERSION): List<String> {
        require(fromVersion in 0..toVersion) { "Unsupported database version: $fromVersion" }
        require(toVersion <= VERSION) { "Database version is newer than this app" }
        val statements = mutableListOf<String>()
        var version = fromVersion
        while (version < toVersion) {
            statements += when (version) {
                0 -> listOf(
                    """CREATE TABLE notes (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )"""
                )
                1 -> from1To2
                2 -> from2To3
                else -> error("Missing migration from version $version")
            }
            version++
        }
        return statements
    }
}
