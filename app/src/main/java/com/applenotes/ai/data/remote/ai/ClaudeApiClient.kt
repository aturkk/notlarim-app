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
private data class ClaudeRequest(
    val model: String,
    val max_tokens: Int = 2048,
    val system: String? = null,
    val messages: List<ClaudeMessage>
)

@Serializable
private data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
private data class ClaudeResponse(
    val content: List<ClaudeContentBlock>? = null,
    val error: ClaudeError? = null
)

@Serializable
private data class ClaudeContentBlock(
    val type: String? = null,
    val text: String? = null
)

@Serializable
private data class ClaudeError(
    val type: String? = null,
    val message: String? = null
)

class ClaudeApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun generateMessage(
        apiKey: String,
        model: String,
        prompt: String,
        systemPrompt: String? = null,
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("Anthropic Claude API anahtarı girilmemiş. Lütfen Ayarlar > Profil bölümünden API anahtarınızı girin."))
        }

        val messages = mutableListOf<ClaudeMessage>()
        for ((role, text) in history) {
            val claudeRole = if (role.lowercase() == "user") "user" else "assistant"
            messages.add(ClaudeMessage(role = claudeRole, content = text))
        }
        messages.add(ClaudeMessage(role = "user", content = prompt))

        return try {
            val response = httpClient.post("https://api.anthropic.com/v1/messages") {
                contentType(ContentType.Application.Json)
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                setBody(ClaudeRequest(model = model, system = systemPrompt, messages = messages))
            }
            val body = response.bodyAsText()
            val parsed = json.decodeFromString<ClaudeResponse>(body)

            if (parsed.error != null) {
                Result.failure(Exception("Claude Hatası: ${parsed.error.message ?: "Bilinmeyen hata"}"))
            } else {
                val answer = parsed.content?.firstOrNull()?.text
                if (!answer.isNullOrBlank()) {
                    Result.success(answer.trim())
                } else {
                    Result.failure(Exception("Claude boş yanıt döndürdü."))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Bağlantı Hatası (Claude): ${e.localizedMessage ?: e.message}"))
        }
    }
}
