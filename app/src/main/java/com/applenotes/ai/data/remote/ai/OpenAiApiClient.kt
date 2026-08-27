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
private data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Float = 0.7f
)

@Serializable
private data class OpenAiMessage(
    val role: String,
    val content: String
)

@Serializable
private data class OpenAiChatResponse(
    val choices: List<OpenAiChoice>? = null,
    val error: OpenAiError? = null
)

@Serializable
private data class OpenAiChoice(
    val message: OpenAiMessage
)

@Serializable
private data class OpenAiError(
    val message: String? = null,
    val type: String? = null
)

class OpenAiApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun generateChatCompletion(
        apiKey: String,
        model: String,
        prompt: String,
        systemPrompt: String? = null,
        history: List<Pair<String, String>> = emptyList(),
        baseUrl: String = "https://api.openai.com/v1/chat/completions"
    ): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("API anahtarı girilmemiş. Lütfen Ayarlar > Profil bölümünden API anahtarınızı girin."))
        }

        val messages = mutableListOf<OpenAiMessage>()
        if (systemPrompt != null) {
            messages.add(OpenAiMessage(role = "system", content = systemPrompt))
        }
        for ((role, text) in history) {
            messages.add(OpenAiMessage(role = role, content = text))
        }
        messages.add(OpenAiMessage(role = "user", content = prompt))

        return try {
            val response = httpClient.post(baseUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                if (baseUrl.contains("openrouter")) {
                    header("HTTP-Referer", "https://github.com/applenotes/ai")
                    header("X-Title", "Apple Notes AI")
                }
                setBody(OpenAiChatRequest(model = model, messages = messages))
            }
            val body = response.bodyAsText()
            val parsed = json.decodeFromString<OpenAiChatResponse>(body)

            if (parsed.error != null) {
                Result.failure(Exception("OpenAI/Servis Hatası: ${parsed.error.message ?: "Bilinmeyen hata"}"))
            } else {
                val answer = parsed.choices?.firstOrNull()?.message?.content
                if (!answer.isNullOrBlank()) {
                    Result.success(answer.trim())
                } else {
                    Result.failure(Exception("Model boş yanıt döndürdü."))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Bağlantı Hatası: ${e.localizedMessage ?: e.message}"))
        }
    }
}
