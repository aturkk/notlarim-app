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
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import io.ktor.client.plugins.HttpTimeout
import java.util.concurrent.TimeUnit

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

@Serializable
private data class OpenAiTranscriptionResponse(
    val text: String? = null,
    val error: OpenAiError? = null
)

class OpenAiApiClient {
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
                    header("HTTP-Referer", "https://github.com/notism/ai")
                    header("X-Title", "Notism")
                }
                setBody(OpenAiChatRequest(model = model, messages = messages))
            }
            val body = response.bodyAsText()
            val parsed = json.decodeFromString<OpenAiChatResponse>(body)

            if (parsed.error != null) {
                Result.failure(Exception("API/Servis Hatası: ${parsed.error.message ?: "Bilinmeyen hata"}"))
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

    suspend fun transcribeAudio(
        apiKey: String,
        audioFile: File,
        baseUrl: String = "https://api.openai.com/v1/audio/transcriptions",
        model: String = "whisper-1"
    ): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("API anahtarı girilmemiş."))
        }

        return try {
            val response = httpClient.submitFormWithBinaryData(
                url = baseUrl,
                formData = formData {
                    append("model", model)
                    append("file", audioFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "audio/m4a")
                        append(HttpHeaders.ContentDisposition, "filename=\"${audioFile.name}\"")
                    })
                }
            ) {
                header("Authorization", "Bearer $apiKey")
            }
            val body = response.bodyAsText()
            val parsed = try {
                json.decodeFromString<OpenAiTranscriptionResponse>(body)
            } catch (e: Exception) {
                null
            }

            if (parsed?.error != null) {
                Result.failure(Exception("Whisper Hatası: ${parsed.error.message}"))
            } else if (!parsed?.text.isNullOrBlank()) {
                Result.success(parsed!!.text!!.trim())
            } else {
                Result.failure(Exception("Transkript yanıtı alınamadı."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Transkript Bağlantı Hatası: ${e.localizedMessage ?: e.message}"))
        }
    }
}
