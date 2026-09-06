package com.applenotes.ai.data.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

import io.ktor.client.plugins.HttpTimeout
import java.util.concurrent.TimeUnit

@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@Serializable
private data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
private data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@Serializable
private data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null,
    val thought: Boolean? = null
)

@Serializable
private data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@Serializable
private data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
private data class GeminiError(
    val code: Int? = null,
    val message: String? = null
)

class GeminiApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(90, TimeUnit.SECONDS)
                writeTimeout(90, TimeUnit.SECONDS)
                callTimeout(90, TimeUnit.SECONDS)
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 90_000L
            connectTimeoutMillis = 30_000L
            socketTimeoutMillis = 90_000L
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun generateContent(
        apiKey: String,
        model: String,
        prompt: String,
        systemPrompt: String? = null,
        history: List<Pair<String, String>> = emptyList() // role to message
    ): Result<String> {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            return Result.failure(IllegalStateException("Google Gemini API anahtarı girilmemiş. Lütfen Ayarlar > Profil bölümünden API anahtarınızı girin."))
        }

        // Clean model name and map deprecated models automatically
        val rawModel = model.trim().removePrefix("models/")
        val effectiveModel = when {
            rawModel.isBlank() || rawModel == "gemini-1.5-flash" || rawModel == "gemini-2.0-flash-exp" -> "gemini-2.5-flash"
            else -> rawModel
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$effectiveModel:generateContent?key=$trimmedKey"

        val contents = mutableListOf<GeminiContent>()
        for ((role, text) in history) {
            val geminiRole = if (role.lowercase() == "user") "user" else "model"
            contents.add(GeminiContent(role = geminiRole, parts = listOf(GeminiPart(text = text))))
        }
        contents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))

        val sysContent = systemPrompt?.let {
            GeminiContent(parts = listOf(GeminiPart(text = it)))
        }

        return try {
            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(contents = contents, systemInstruction = sysContent))
            }
            val body = response.bodyAsText()
            val parsed = try {
                json.decodeFromString<GeminiResponse>(body)
            } catch (e: Exception) {
                null
            }

            if (parsed?.error != null) {
                val codeStr = parsed.error.code?.let { "[$it] " } ?: ""
                Result.failure(Exception("Gemini Hatası: $codeStr${parsed.error.message ?: "Bilinmeyen hata"}"))
            } else if (response.status.value !in 200..299) {
                Result.failure(Exception("Gemini Servis Hatası (${response.status.value}): ${body.take(250)}"))
            } else {
                val parts = parsed?.candidates?.firstOrNull()?.content?.parts
                val answer = parts?.filter { it.thought != true }?.mapNotNull { it.text }?.joinToString("")?.ifBlank {
                    parts?.mapNotNull { it.text }?.joinToString("")
                }
                if (!answer.isNullOrBlank()) {
                    Result.success(answer.trim())
                } else {
                    Result.failure(Exception("Gemini boş yanıt döndürdü."))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Bağlantı Hatası (Gemini): ${e.localizedMessage ?: e.message}"))
        }
    }

    suspend fun transcribeAudio(
        apiKey: String,
        audioBytes: ByteArray,
        mimeType: String = "audio/mp4"
    ): Result<String> {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            return Result.failure(IllegalStateException("Google Gemini API anahtarı girilmemiş."))
        }

        val base64Data = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$trimmedKey"

        val contents = listOf(
            GeminiContent(
                role = "user",
                parts = listOf(
                    GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64Data)),
                    GeminiPart(text = "Lütfen bu ses kaydını eksiksiz, pürüzsüz ve temiz bir şekilde Türkçeye transkribe et (yazıya dök). Konuşulanları aynen aktar, ek yorum yapma.")
                )
            )
        )

        return try {
            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(contents = contents))
            }
            val body = response.bodyAsText()
            val parsed = try { json.decodeFromString<GeminiResponse>(body) } catch (e: Exception) { null }

            if (parsed?.error != null) {
                Result.failure(Exception("Gemini Transkript Hatası: ${parsed.error.message}"))
            } else {
                val answer = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!answer.isNullOrBlank()) {
                    Result.success(answer.trim())
                } else {
                    Result.failure(Exception("Transkript oluşturulamadı veya boş yanıt döndü."))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Transkript Bağlantı Hatası: ${e.message}"))
        }
    }

    suspend fun extractTextFromImage(
        apiKey: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Result<String> {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            return Result.failure(IllegalStateException("Google Gemini API anahtarı girilmemiş."))
        }

        val base64Data = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$trimmedKey"

        val contents = listOf(
            GeminiContent(
                role = "user",
                parts = listOf(
                    GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64Data)),
                    GeminiPart(text = "Lütfen bu belgedeki veya görseldeki tüm yazıları, notları ve metinleri eksiksiz bir şekilde oku ve Markdown formatında düzenli olarak çıkar (OCR). Ek açıklama yapma, yalnızca belgedeki metni ver.")
                )
            )
        )

        return try {
            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(contents = contents))
            }
            val body = response.bodyAsText()
            val parsed = try { json.decodeFromString<GeminiResponse>(body) } catch (e: Exception) { null }

            if (parsed?.error != null) {
                Result.failure(Exception("Gemini OCR Hatası: ${parsed.error.message}"))
            } else {
                val answer = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!answer.isNullOrBlank()) {
                    Result.success(answer.trim())
                } else {
                    Result.failure(Exception("Görselden metin çıkarılamadı."))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("OCR Bağlantı Hatası: ${e.message}"))
        }
    }
}
