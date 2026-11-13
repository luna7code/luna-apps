package com.lunacode.mobilesecurity.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScanDao {
    @Insert
    suspend fun insert(record: ScanRecord): Long

    @Query("SELECT * FROM scan_records ORDER BY scannedAt DESC LIMIT 1")
    suspend fun latest(): ScanRecord?

    @Query("SELECT * FROM scan_records ORDER BY scannedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<ScanRecord>

    @Query("SELECT COUNT(*) FROM scan_records")
    suspend fun count(): Int

    @Query("""
        DELETE FROM scan_records
        WHERE id NOT IN (
            SELECT id FROM scan_records
            ORDER BY scannedAt DESC
            LIMIT :maxRows
        )
    """)
    suspend fun pruneToLimit(maxRows: Int): Int
}
