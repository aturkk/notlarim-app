package com.applenotes.ai.core.backup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AutoBackupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val prefs = SecurePreferences(context)

        if (!prefs.autoBackupEnabled) {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val notes = db.noteDao.getAllNotes().first().map { it.toDomain() }

                if (notes.isNotEmpty()) {
                    val backupDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "Backups").apply {
                        if (!exists()) mkdirs()
                    }

                    // Rotate current to previous if exists
                    val currentBackup = File(backupDir, "AppleNotes_AutoBackup.zip")
                    if (currentBackup.exists()) {
                        val prevBackup = File(backupDir, "AppleNotes_AutoBackup_prev.zip")
                        if (prevBackup.exists()) prevBackup.delete()
                        currentBackup.renameTo(prevBackup)
                    }

                    val tempZip = BackupRestoreHelper.createBackupZip(context, notes)
                    tempZip.copyTo(File(backupDir, "AppleNotes_AutoBackup.zip"), overwrite = true)
                    tempZip.delete()

                    prefs.lastBackupTime = System.currentTimeMillis()
                }

                // Reschedule for next cycle
                AutoBackupScheduler.schedule(context)
            } catch (_: Exception) {
                // Ignore background errors
            } finally {
                pendingResult.finish()
            }
        }
    }
}
