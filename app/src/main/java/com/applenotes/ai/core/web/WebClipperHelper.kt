package com.applenotes.ai.core.web

import android.content.Intent
import com.applenotes.ai.data.remote.ai.AiServiceManager
import com.applenotes.ai.domain.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

object WebClipperHelper {

    private val URL_PATTERN = Pattern.compile(
        "https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?",
        Pattern.CASE_INSENSITIVE
    )

    data class ClippedContent(
        val title: String,
        val url: String?,
        val rawText: String,
        val description: String?
    )

    fun extractContentFromIntent(intent: Intent): ClippedContent {
        var sharedText: String = ""

        // 1. Check EXTRA_TEXT as CharSequence (safely handling SpannedString/SpannableStringBuilder)
        val extraText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        if (!extraText.isNullOrBlank()) {
            sharedText = extraText.trim()
        }

        // 2. Check ClipData if EXTRA_TEXT was empty
        if (sharedText.isBlank()) {
            val clipData = intent.clipData
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0)?.text?.toString()
                if (!clipText.isNullOrBlank()) {
                    sharedText = clipText.trim()
                } else {
                    val clipUri = clipData.getItemAt(0)?.uri?.toString()
                    if (!clipUri.isNullOrBlank()) {
                        sharedText = clipUri.trim()
                    }
                }
            }
        }

        // 3. Check Intent.data URI
        if (sharedText.isBlank()) {
            val dataUri = intent.data?.toString()
            if (!dataUri.isNullOrBlank()) {
                sharedText = dataUri.trim()
            }
        }

        // 4. Extract Subject / Title
        val subject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT)?.toString()
            ?: intent.getCharSequenceExtra(Intent.EXTRA_TITLE)?.toString()
            ?: ""

        // Find URL inside the text
        val matcher = URL_PATTERN.matcher(sharedText)
        val extractedUrl = if (matcher.find()) matcher.group() else null

        val initialTitle = when {
            subject.isNotBlank() -> subject.trim()
            extractedUrl != null -> {
                try {
                    val host = URL(extractedUrl).host.removePrefix("www.")
                    "Web: $host"
                } catch (e: Exception) {
                    "Web Kırpıntısı"
                }
            }
            sharedText.isNotBlank() -> sharedText.take(40).lines().firstOrNull() ?: "Web Kırpıntısı"
            else -> "Web Kırpıntısı"
        }

        return ClippedContent(
            title = initialTitle,
            url = extractedUrl,
            rawText = sharedText,
            description = null
        )
    }

    suspend fun fetchMetadataAndBuildNote(
        clipped: ClippedContent,
        aiServiceManager: AiServiceManager? = null
    ): Note = withContext(Dispatchers.IO) {
        var pageTitle = clipped.title
        var pageDescription: String? = null

        val targetUrl = clipped.url
        if (targetUrl != null) {
            try {
                val urlObj = URL(targetUrl)
                val connection = (urlObj.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4000
                    readTimeout = 4000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
                    instanceFollowRedirects = true
                }

                if (connection.responseCode in 200..299) {
                    val htmlSnippet = connection.inputStream.bufferedReader().use { reader ->
                        val buffer = CharArray(16384)
                        val read = reader.read(buffer, 0, buffer.size)
                        if (read > 0) String(buffer, 0, read) else ""
                    }

                    // Extract <title>...</title>
                    val titleMatcher = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL).matcher(htmlSnippet)
                    if (titleMatcher.find()) {
                        val extractedHtmlTitle = titleMatcher.group(1)?.trim()?.replace("\n", " ")
                        if (!extractedHtmlTitle.isNullOrBlank() && (pageTitle.startsWith("Web:") || pageTitle == "Web Kırpıntısı")) {
                            pageTitle = extractedHtmlTitle
                        }
                    }

                    // Extract meta description
                    val metaDescMatcher = Pattern.compile("<meta\\s+name=[\"']description[\"']\\s+content=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE).matcher(htmlSnippet)
                    if (metaDescMatcher.find()) {
                        pageDescription = metaDescMatcher.group(1)?.trim()
                    }
                }
            } catch (e: Exception) {
                // Fallback to existing metadata
            }
        }

        // Format Note Content
        val contentBuilder = StringBuilder()
        if (targetUrl != null) {
            contentBuilder.append("🌐 **Kaynak:** [$targetUrl]($targetUrl)\n\n")
        }

        val textExcludingUrl = if (targetUrl != null) {
            clipped.rawText.replace(targetUrl, "").trim()
        } else {
            clipped.rawText
        }

        if (textExcludingUrl.isNotBlank()) {
            contentBuilder.append("> $textExcludingUrl\n\n")
        }

        if (!pageDescription.isNullOrBlank() && pageDescription != textExcludingUrl) {
            contentBuilder.append("📄 **Açıklama:**\n$pageDescription\n\n")
        }

        // If AI is available, generate a quick 3-bullet summary
        if (aiServiceManager != null && (textExcludingUrl.isNotBlank() || !pageDescription.isNullOrBlank())) {
            val contentToSummarize = if (!pageDescription.isNullOrBlank()) {
                "$pageTitle\n$pageDescription\n$textExcludingUrl"
            } else {
                "$pageTitle\n$textExcludingUrl"
            }

            try {
                val prompt = "Aşağıdaki web sayfası/haber içeriğini analiz et ve en önemli noktaları tam 3 vurucu madde halinde Türkçe özetle:\n\n$contentToSummarize"
                val systemPrompt = "Sen web sayfalarını 3 maddelik özete dönüştüren akıllı bir asistansın. Maddelerin başına '• ' koy."
                val aiSummary = aiServiceManager.chatWithNote(contentToSummarize, prompt, emptyList()).getOrNull()
                if (!aiSummary.isNullOrBlank()) {
                    contentBuilder.append("### ⚡ 3 Maddelik Yapay Zeka Özeti\n$aiSummary\n\n")
                }
            } catch (e: Exception) {
                // Ignore AI failure, keep plain content
            }
        }

        Note(
            title = pageTitle,
            content = contentBuilder.toString().trim(),
            tags = listOf("Web"),
            icon = "🌐",
            updatedAt = System.currentTimeMillis()
        )
    }
}
