package com.applenotes.ai.data.remote.github

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import okhttp3.OkHttpClient
import okhttp3.Request
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
        val owner = prefs.githubOwner.ifBlank { "aturkk" }
        val repo = prefs.githubRepo.ifBlank { "notlarim-app" }
        val currentVersion = BuildConfig.VERSION_NAME

        try {
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"

            val response = httpClient.get(url) {
                header("Accept", "application/vnd.github+json")
                header("User-Agent", "Mozilla/5.0 (Linux; Android)")
                if (prefs.githubToken.isNotBlank()) {
                    header("Authorization", "Bearer ${prefs.githubToken}")
                }
            }

            val status = response.status.value

            if (status == 404) {
                // If no formal Release was published yet, check Git Tags endpoint
                val tagsUrl = "https://api.github.com/repos/$owner/$repo/tags"
                val tagsResponse = httpClient.get(tagsUrl) {
                    header("Accept", "application/vnd.github+json")
                    header("User-Agent", "Mozilla/5.0 (Linux; Android)")
                    if (prefs.githubToken.isNotBlank()) {
                        header("Authorization", "Bearer ${prefs.githubToken}")
                    }
                }
                if (tagsResponse.status.value in 200..299) {
                    val tagsBody = tagsResponse.bodyAsText()
                    val tagRegex = Regex(""""name"\s*:\s*"([^"]+)"""")
                    val match = tagRegex.find(tagsBody)
                    val firstTag = match?.groupValues?.getOrNull(1)
                    if (firstTag != null) {
                        val latestVersion = firstTag.removePrefix("v").trim()
                        val isNewer = isVersionNewer(latest = latestVersion, current = currentVersion)
                        val downloadUrl = "https://github.com/$owner/$repo/releases/download/$firstTag/app-release.apk"
                        return@withContext Result.success(
                            AppUpdateInfo(
                                currentVersion = currentVersion,
                                latestVersion = firstTag,
                                releaseTitle = "Yeni Sürüm $firstTag",
                                changelog = "GitHub üzerinden yeni sürüm yayınlandı.",
                                downloadUrl = downloadUrl,
                                isUpdateAvailable = isNewer
                            )
                        )
                    }
                }
            }

            if (status in 200..299) {
                val body = response.bodyAsText()
                val release = json.decodeFromString<GitHubRelease>(body)

                val tagName = release.tag_name
                if (!tagName.isNullOrBlank()) {
                    val latestVersion = tagName.removePrefix("v").trim()
                    val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                        ?: release.assets.firstOrNull()
                    val downloadUrl = apkAsset?.browser_download_url
                        ?: "https://github.com/$owner/$repo/releases/download/$tagName/app-release.apk"
                    val isNewer = isVersionNewer(latest = latestVersion, current = currentVersion)

                    return@withContext Result.success(
                        AppUpdateInfo(
                            currentVersion = currentVersion,
                            latestVersion = tagName,
                            releaseTitle = release.name ?: tagName,
                            changelog = release.body ?: "Hata düzeltmeleri ve yeni özellikler.",
                            downloadUrl = downloadUrl,
                            isUpdateAvailable = isNewer,
                            publishedAt = release.published_at ?: ""
                        )
                    )
                }
            }

            // If API returned 403 (Rate Limit) or non-200, fallback to direct Web URL resolution
            return@withContext checkUpdateViaWebFallback(owner, repo, currentVersion)
        } catch (e: Exception) {
            // Fallback to web check if network/api throws
            return@withContext checkUpdateViaWebFallback(owner, repo, currentVersion)
        }
    }

    private suspend fun checkUpdateViaWebFallback(owner: String, repo: String, currentVersion: String): Result<AppUpdateInfo> {
        return try {
            withContext(Dispatchers.IO) {
                // Use OkHttp directly so we can read the final URL after redirects.
                // Ktor's response.call.request.url returns the ORIGINAL url, not the redirected one.
                val okClient = okhttp3.OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()

                val webUrl = "https://github.com/$owner/$repo/releases/latest"
                val request = okhttp3.Request.Builder()
                    .url(webUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android)")
                    .build()

                val response = okClient.newCall(request).execute()
                val finalUrl = response.request.url.toString()
                response.close()

                // After redirect, finalUrl should look like .../releases/tag/v1.5.0
                val tagMatch = Regex(".*/releases/tag/([^/?#]+)").find(finalUrl)
                val tagName = tagMatch?.groupValues?.getOrNull(1)

                if (tagName != null) {
                    val latestVersion = tagName.removePrefix("v").trim()
                    val isNewer = isVersionNewer(latest = latestVersion, current = currentVersion)
                    val downloadUrl = "https://github.com/$owner/$repo/releases/download/$tagName/app-release.apk"
                    return@withContext Result.success(
                        AppUpdateInfo(
                            currentVersion = currentVersion,
                            latestVersion = tagName,
                            releaseTitle = "Yeni Sürüm $tagName",
                            changelog = "GitHub üzerinden yeni sürüm mevcut.",
                            downloadUrl = downloadUrl,
                            isUpdateAvailable = isNewer
                        )
                    )
                }

                // Fallback: parse raw build.gradle.kts from main branch
                val rawGradleUrl = "https://raw.githubusercontent.com/$owner/$repo/main/app/build.gradle.kts"
                val gradleRequest = okhttp3.Request.Builder()
                    .url(rawGradleUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android)")
                    .build()
                val gradleResponse = okClient.newCall(gradleRequest).execute()
                val gradleContent = if (gradleResponse.isSuccessful) gradleResponse.body?.string() else null
                gradleResponse.close()

                val rawVersion = gradleContent?.let {
                    Regex("""versionName\s*=\s*"([^"]+)"""").find(it)?.groupValues?.getOrNull(1)
                }

                if (rawVersion != null) {
                    val isNewer = isVersionNewer(latest = rawVersion, current = currentVersion)
                    val downloadUrl = "https://github.com/$owner/$repo/releases/download/v$rawVersion/app-release.apk"
                    return@withContext Result.success(
                        AppUpdateInfo(
                            currentVersion = currentVersion,
                            latestVersion = "v$rawVersion",
                            releaseTitle = "Yeni Sürüm v$rawVersion",
                            changelog = "GitHub üzerinden yeni sürüm mevcut.",
                            downloadUrl = downloadUrl,
                            isUpdateAvailable = isNewer
                        )
                    )
                }

                Result.success(
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
        } catch (e: Exception) {
            Result.failure(Exception("Güncelleme denetimi yapılamadı: ${e.localizedMessage ?: e.message}"))
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
     * Downloads APK with progress percentage stream (0..100) and full HTTP 302 redirect handling
     */
    fun downloadApk(downloadUrl: String, fileName: String = "update.apk"): Flow<Int> = flow {
        val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        var currentUrl = downloadUrl
        var connection: HttpURLConnection
        var redirectCount = 0

        while (true) {
            val url = URL(currentUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android)")
            }
            connection.connect()

            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                status == HttpURLConnection.HTTP_MOVED_PERM ||
                status == HttpURLConnection.HTTP_SEE_OTHER ||
                status == 307 || status == 308
            ) {
                val newUrl = connection.getHeaderField("Location")
                if (!newUrl.isNullOrBlank() && redirectCount < 5) {
                    currentUrl = newUrl
                    redirectCount++
                    continue
                }
            }
            break
        }

        if (connection.responseCode !in 200..299) {
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
