package com.luna.secretnotes.data

object NotesMigrations {
    const val VERSION = 4
    private val from1To2 = listOf(
        "ALTER TABLE notes ADD COLUMN folder_id TEXT",
        "ALTER TABLE notes ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'"
    )
    private val from2To3 = listOf(
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
    private val from3To4 = listOf(
        """CREATE TABLE attachments (
            id TEXT PRIMARY KEY NOT NULL,
            note_id TEXT NOT NULL,
            encrypted_file_name TEXT NOT NULL UNIQUE,
            display_name TEXT NOT NULL,
            mime_type TEXT NOT NULL,
            size_bytes INTEGER NOT NULL,
            created_at INTEGER NOT NULL,
            FOREIGN KEY(note_id) REFERENCES notes(id) ON DELETE CASCADE
        )""",
        "CREATE INDEX index_attachments_note_id ON attachments(note_id)"
    )
    fun migrationPath(fromVersion: Int, toVersion: Int = VERSION): List<String> {
        require(fromVersion in 0..toVersion) { "Unsupported database version: $fromVersion" }
        require(toVersion <= VERSION) { "Database version is newer than this app" }
        val result = mutableListOf<String>()
        var version = fromVersion
        while (version < toVersion) {
            result += when (version) {
                0 -> listOf("""CREATE TABLE notes (
                    id TEXT PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL,
                    body TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )""")
                1 -> from1To2
                2 -> from2To3
                3 -> from3To4
                else -> error("Missing migration from version $version")
            }
            version++
        }
        return result
    }
}
