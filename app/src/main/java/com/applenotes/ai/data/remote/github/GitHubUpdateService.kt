package com.applenotes.ai.data.remote.github

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.applenotes.ai.BuildConfig
import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.domain.model.AppUpdateInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class GitHubRelease(
    val tag_name: String? = null,
    val name: String? = null,
    val body: String? = null,
    val published_at: String? = null,
    val message: String? = null,
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
private data class GitHubAsset(
    val name: String = "",
    val browser_download_url: String = "",
    val size: Long = 0
)

class GitHubUpdateService(
    private val context: Context,
    private val prefs: SecurePreferences
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun checkForUpdate(): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val owner = prefs.githubOwner.ifBlank { "aturkk" }
            val repo = prefs.githubRepo.ifBlank { "notlarim-app" }
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"

            val response = httpClient.get(url) {
                header("Accept", "application/vnd.github+json")
                header("User-Agent", "AppleNotesAI-Updater")
            }

            val currentVersion = BuildConfig.VERSION_NAME
            val status = response.status.value

            if (status == 404) {
                return@withContext Result.success(
                    AppUpdateInfo(
                        currentVersion = currentVersion,
                        latestVersion = currentVersion,
                        releaseTitle = "Güncel",
                        changelog = "Henüz yeni bir sürüm yayınlanmamış.",
                        downloadUrl = "",
                        isUpdateAvailable = false
                    )
                )
            }

            if (status !in 200..299) {
                return@withContext Result.failure(Exception("GitHub API hatası (HTTP $status)"))
            }

            val body = response.bodyAsText()
            val release = json.decodeFromString<GitHubRelease>(body)

            val tagName = release.tag_name
            if (tagName.isNullOrBlank()) {
                return@withContext Result.success(
                    AppUpdateInfo(
                        currentVersion = currentVersion,
                        latestVersion = currentVersion,
                        releaseTitle = "Güncel",
                        changelog = "Uygulamanız en güncel sürümde.",
                        downloadUrl = "",
                        isUpdateAvailable = false
                    )
                )
            }

            val latestVersion = tagName.removePrefix("v").trim()

            // Find APK asset
            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                ?: release.assets.firstOrNull()

            val downloadUrl = apkAsset?.browser_download_url ?: ""
            val isNewer = isVersionNewer(latest = latestVersion, current = currentVersion)

            Result.success(
                AppUpdateInfo(
                    currentVersion = currentVersion,
                    latestVersion = tagName,
                    releaseTitle = release.name ?: tagName,
                    changelog = release.body ?: "Hata düzeltmeleri ve performans iyileştirmeleri.",
                    downloadUrl = downloadUrl,
                    isUpdateAvailable = isNewer && downloadUrl.isNotBlank(),
                    publishedAt = release.published_at ?: ""
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Güncelleme kontrolü başarısız: ${e.localizedMessage ?: e.message}"))
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            return false
        } catch (e: Exception) {
            return latest != current
        }
    }

    /**
     * Downloads APK with progress percentage stream (0..100)
     */
    fun downloadApk(downloadUrl: String, fileName: String = "update.apk"): Flow<Int> = flow {
        val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val url = URL(downloadUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("İndirme sunucusu hata verdi: ${connection.responseCode}")
        }

        val fileLength = connection.contentLength
        val input = connection.inputStream
        val output = FileOutputStream(destinationFile)

        val data = ByteArray(4096)
        var total: Long = 0
        var count: Int
        var lastEmittedProgress = 0

        while (input.read(data).also { count = it } != -1) {
            total += count
            output.write(data, 0, count)
            if (fileLength > 0) {
                val progress = ((total * 100) / fileLength).toInt()
                if (progress > lastEmittedProgress) {
                    lastEmittedProgress = progress
                    emit(progress)
                }
            }
        }

        output.flush()
        output.close()
        input.close()
        emit(100)
    }.flowOn(Dispatchers.IO)

    fun installDownloadedApk(fileName: String = "update.apk") {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (!file.exists()) return

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}
