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
private data class GeminiPart(
    val text: String
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
                val answer = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
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
}
