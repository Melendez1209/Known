package com.melendez.known.util

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Utility class for diagnosing database-related issues
 * Helps debug cold start database initialization problems
 */
object DatabaseDiagnostics {
    private const val TAG = "DatabaseDiagnostics"

    /**
     * Perform comprehensive database environment check
     */
    fun performDatabaseEnvironmentCheck(context: Context) {
        Log.d(TAG, "=== Database Environment Check ===")

        try {
            // Check application files directory
            val filesDir = context.filesDir
            Log.d(TAG, "App files directory: ${filesDir.absolutePath}")
            Log.d(TAG, "Files directory exists: ${filesDir.exists()}")
            Log.d(TAG, "Files directory readable: ${filesDir.canRead()}")
            Log.d(TAG, "Files directory writable: ${filesDir.canWrite()}")

            // Check databases directory
            val dbDir = File(filesDir, "databases")
            Log.d(TAG, "Database directory: ${dbDir.absolutePath}")
            Log.d(TAG, "Database directory exists: ${dbDir.exists()}")
            if (dbDir.exists()) {
                Log.d(TAG, "Database directory readable: ${dbDir.canRead()}")
                Log.d(TAG, "Database directory writable: ${dbDir.canWrite()}")

                // List database files
                val dbFiles = dbDir.listFiles()
                if (dbFiles != null) {
                    Log.d(TAG, "Database files count: ${dbFiles.size}")
                    dbFiles.forEach { file ->
                        Log.d(TAG, "DB file: ${file.name}, size: ${file.length()} bytes")
                    }
                } else {
                    Log.d(TAG, "Cannot list database files (null)")
                }
            }

            // Check specific database file
            val appDbFile = File(dbDir, "app_database")
            Log.d(TAG, "App database file exists: ${appDbFile.exists()}")
            if (appDbFile.exists()) {
                Log.d(TAG, "App database file size: ${appDbFile.length()} bytes")
                Log.d(TAG, "App database file readable: ${appDbFile.canRead()}")
                Log.d(TAG, "App database file writable: ${appDbFile.canWrite()}")
            }

            // Check WAL and SHM files
            val walFile = File(dbDir, "app_database-wal")
            val shmFile = File(dbDir, "app_database-shm")
            Log.d(TAG, "WAL file exists: ${walFile.exists()}")
            Log.d(TAG, "SHM file exists: ${shmFile.exists()}")

            // Check available space
            val freeSpace = filesDir.freeSpace
            val totalSpace = filesDir.totalSpace
            Log.d(TAG, "Free space: ${freeSpace / 1024 / 1024} MB")
            Log.d(TAG, "Total space: ${totalSpace / 1024 / 1024} MB")

        } catch (e: Exception) {
            Log.e(TAG, "Error during database environment check: ${e.message}", e)
        }

        Log.d(TAG, "=== End Database Environment Check ===")
    }

    /**
     * Check if database files are corrupted or inaccessible
     */
    fun checkDatabaseIntegrity(context: Context): Boolean {
        return try {
            val dbDir = File(context.filesDir, "databases")
            val appDbFile = File(dbDir, "app_database")

            if (!appDbFile.exists()) {
                Log.d(TAG, "Database file does not exist - this is normal for first run")
                return true
            }

            // Basic file accessibility check
            val canRead = appDbFile.canRead()
            val canWrite = appDbFile.canWrite()
            val hasSize = appDbFile.length() > 0

            Log.d(
                TAG,
                "Database integrity check - readable: $canRead, writable: $canWrite, has size: $hasSize"
            )

            canRead && canWrite
        } catch (e: Exception) {
            Log.e(TAG, "Database integrity check failed: ${e.message}", e)
            false
        }
    }

    /**
     * Clean up potentially corrupted database files
     */
    fun cleanupCorruptedDatabase(context: Context): Boolean {
        return try {
            Log.w(TAG, "Attempting to cleanup potentially corrupted database")

            val dbDir = File(context.filesDir, "databases")
            val filesToDelete = arrayOf(
                File(dbDir, "app_database"),
                File(dbDir, "app_database-wal"),
                File(dbDir, "app_database-shm")
            )

            var deletedCount = 0
            filesToDelete.forEach { file ->
                if (file.exists() && file.delete()) {
                    Log.d(TAG, "Deleted corrupted file: ${file.name}")
                    deletedCount++
                }
            }

            Log.d(TAG, "Cleanup completed, deleted $deletedCount files")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Database cleanup failed: ${e.message}", e)
            false
        }
    }
} 