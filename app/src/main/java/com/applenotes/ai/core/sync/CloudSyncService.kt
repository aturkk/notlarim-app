package com.applenotes.ai.core.sync

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.applenotes.ai.core.backup.BackupRestoreHelper
import com.applenotes.ai.domain.model.Note
import com.applenotes.ai.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class CloudSyncService(private val context: Context) {

    /**
     * Uploads all notes as a zip package to a WebDAV server
     */
    suspend fun uploadToWebDav(
        notes: List<Note>,
        serverUrl: String,
        username: String,
        password: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = serverUrl.trimEnd('/')
            val fullUrl = if (cleanUrl.endsWith(".zip")) cleanUrl else "$cleanUrl/Notism_Sync_Backup.zip"
            val zipFile = BackupRestoreHelper.createBackupZip(context, notes)

            val url = URL(fullUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 30000
                readTimeout = 30000
                val auth = "$username:$password"
                val encodedAuth = Base64.encodeToString(auth.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                setRequestProperty("Authorization", "Basic $encodedAuth")
                setRequestProperty("Content-Type", "application/zip")
                setFixedLengthStreamingMode(zipFile.length())
            }

            FileInputStream(zipFile).use { fis ->
                connection.outputStream.use { os ->
                    fis.copyTo(os)
                    os.flush()
                }
            }

            val responseCode = connection.responseCode
            connection.disconnect()
            zipFile.delete()

            if (responseCode in 200..299 || responseCode == 201 || responseCode == 204) {
                Result.success(true)
            } else {
                Result.failure(Exception("WebDAV Sunucu Hatası (HTTP $responseCode)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads the backup package from WebDAV and restores it
     */
    suspend fun restoreFromWebDav(
        serverUrl: String,
        username: String,
        password: String,
        repository: NoteRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = serverUrl.trimEnd('/')
            val fullUrl = if (cleanUrl.endsWith(".zip")) cleanUrl else "$cleanUrl/Notism_Sync_Backup.zip"

            val url = URL(fullUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 30000
                readTimeout = 30000
                val auth = "$username:$password"
                val encodedAuth = Base64.encodeToString(auth.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                setRequestProperty("Authorization", "Basic $encodedAuth")
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                connection.disconnect()
                return@withContext Result.failure(Exception("Dosya indirilemedi (HTTP $responseCode)"))
            }

            val bytes = connection.inputStream.use { it.readBytes() }
            connection.disconnect()

            BackupRestoreHelper.restoreBackupFromBytes(bytes, repository)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exports backup directly to user's selected SAF Uri (Google Drive or internal)
     */
    suspend fun syncToSaf(
        targetUri: Uri,
        notes: List<Note>
    ): Result<Int> = withContext(Dispatchers.IO) {
        BackupRestoreHelper.exportBackupToUri(context, targetUri, notes)
    }

    /**
     * Restores backup from user's selected SAF Uri
     */
    suspend fun restoreFromSaf(
        sourceUri: Uri,
        repository: NoteRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        BackupRestoreHelper.restoreBackupFromUri(context, sourceUri, repository)
    }
}
