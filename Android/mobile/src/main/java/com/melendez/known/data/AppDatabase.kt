package com.melendez.known.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.melendez.known.data.dao.SettingsDao
import com.melendez.known.data.entity.Settings
import java.io.File

@Database(entities = [Settings::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao

    companion object {
        private const val TAG = "AppDatabase"
        private const val DATABASE_NAME = "app_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return try {
                Log.d(TAG, "Building database instance")

                // Ensure database directory exists
                val dbDir = File(context.filesDir, "databases")
                if (!dbDir.exists()) {
                    val created = dbDir.mkdirs()
                    Log.d(TAG, "Database directory created: $created")
                }

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "Database created successfully")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d(TAG, "Database opened successfully")
                        }
                    })
                    .fallbackToDestructiveMigration(false) // Add fallback for development
                    .build().also {
                        Log.d(TAG, "Database instance built successfully")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to build database: ${e.message}", e)
                throw e
            }
        }
    }
} 