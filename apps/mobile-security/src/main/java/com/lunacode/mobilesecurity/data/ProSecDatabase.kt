package com.lunacode.mobilesecurity.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanRecord::class], version = 1, exportSchema = false)
abstract class ProSecDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        @Volatile private var INSTANCE: ProSecDatabase? = null

        fun get(context: Context): ProSecDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ProSecDatabase::class.java,
                    "pro-sec-local.db"
                ).build().also { INSTANCE = it }
            }
    }
}
