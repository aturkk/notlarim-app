package com.applenotes.ai.core.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat

object StorageHelper {

    data class StorageBreakdown(
        val databaseBytes: Long,
        val mediaBytes: Long,
        val cacheBytes: Long,
        val totalBytes: Long
    )

    suspend fun getStorageBreakdown(context: Context): StorageBreakdown = withContext(Dispatchers.IO) {
        val dbSize = getDatabaseSize(context)
        val mediaSize = getMediaSize(context)
        val cacheSize = getCacheSize(context)
        val totalSize = dbSize + mediaSize + cacheSize

        StorageBreakdown(
            databaseBytes = dbSize,
            mediaBytes = mediaSize,
            cacheBytes = cacheSize,
            totalBytes = totalSize
        )
    }

    private fun getDatabaseSize(context: Context): Long {
        var size = 0L
        val dbFile = context.getDatabasePath("apple_notes_db")
        if (dbFile.exists()) size += dbFile.length()

        val walFile = File(dbFile.path + "-wal")
        if (walFile.exists()) size += walFile.length()

        val shmFile = File(dbFile.path + "-shm")
        if (shmFile.exists()) size += shmFile.length()

        return size
    }

    private fun getMediaSize(context: Context): Long {
        var size = 0L
        // Scan filesDir recursively for media files (drawings, audio, exported files)
        context.filesDir?.let { dir ->
            size += getDirectorySize(dir)
        }
        return size
    }

    private fun getCacheSize(context: Context): Long {
        var size = 0L
        context.cacheDir?.let { dir ->
            size += getDirectorySize(dir)
        }
        context.externalCacheDir?.let { dir ->
            size += getDirectorySize(dir)
        }
        return size
    }

    private fun getDirectorySize(dir: File): Long {
        var size = 0L
        if (!dir.exists()) return 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isDirectory) {
                getDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    suspend fun clearCache(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            deleteDirContents(context.cacheDir)
            context.externalCacheDir?.let { deleteDirContents(it) }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun deleteDirContents(dir: File?): Boolean {
        if (dir == null || !dir.exists()) return true
        var success = true
        val files = dir.listFiles() ?: return true
        for (file in files) {
            if (file.isDirectory) {
                deleteDirContents(file)
            }
            if (!file.delete()) {
                success = false
            }
        }
        return success
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val formatted = DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble()))
        return "$formatted ${units[digitGroups.coerceIn(0, units.size - 1)]}"
    }
}
