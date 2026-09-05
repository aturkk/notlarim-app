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
private data class VertexRequest(
    val contents: List<VertexContent>,
    val systemInstruction: VertexContent? = null
)

@Serializable
private data class VertexContent(
    val role: String? = null,
    val parts: List<VertexPart>
)

@Serializable
private data class VertexPart(
    val text: String
)

@Serializable
private data class VertexResponse(
    val candidates: List<VertexCandidate>? = null,
    val error: VertexError? = null
)

@Serializable
private data class VertexCandidate(
    val content: VertexContent? = null
)

@Serializable
private data class VertexError(
    val code: Int? = null,
    val message: String? = null
)

class VertexAiApiClient {
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
        projectId: String,
        region: String,
        apiKeyOrToken: String,
        model: String,
        prompt: String,
        systemPrompt: String? = null,
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> {
        if (projectId.isBlank()) {
            return Result.failure(IllegalStateException("Google Cloud Project ID girilmemiş. Lütfen Ayarlar bölümünden Project ID girin."))
        }
        if (apiKeyOrToken.isBlank()) {
            return Result.failure(IllegalStateException("Vertex AI API Anahtarı / Access Token girilmemiş."))
        }

        val activeRegion = region.ifBlank { "us-central1" }
        val cleanModel = model.trim().removePrefix("models/")
        val activeModel = when {
            cleanModel.isBlank() || cleanModel == "gemini-1.5-flash" -> "gemini-2.5-flash"
            else -> cleanModel
        }

        // Support both Express API Key (?key=) and OAuth Bearer tokens
        val isExpressKey = apiKeyOrToken.startsWith("AIza")
        val url = if (isExpressKey) {
            "https://$activeRegion-aiplatform.googleapis.com/v1/projects/$projectId/locations/$activeRegion/publishers/google/models/$activeModel:generateContent?key=$apiKeyOrToken"
        } else {
            "https://$activeRegion-aiplatform.googleapis.com/v1/projects/$projectId/locations/$activeRegion/publishers/google/models/$activeModel:generateContent"
        }

        val contents = mutableListOf<VertexContent>()
        for ((role, text) in history) {
            val vertexRole = if (role.lowercase() == "user") "user" else "model"
            contents.add(VertexContent(role = vertexRole, parts = listOf(VertexPart(text = text))))
        }
        contents.add(VertexContent(role = "user", parts = listOf(VertexPart(text = prompt))))

        val sysContent = systemPrompt?.let {
            VertexContent(parts = listOf(VertexPart(text = it)))
        }

        return try {
            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                if (!isExpressKey) {
                    header("Authorization", "Bearer $apiKeyOrToken")
                }
                setBody(VertexRequest(contents = contents, systemInstruction = sysContent))
            }
            val body = response.bodyAsText()
            val parsed = json.decodeFromString<VertexResponse>(body)

            if (parsed.error != null) {
                Result.failure(Exception("Vertex AI Hatası: ${parsed.error.message ?: "Bilinmeyen hata"}"))
            } else {
                val answer = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!answer.isNullOrBlank()) {
                    Result.success(answer.trim())
                } else {
                    Result.failure(Exception("Vertex AI boş yanıt döndürdü."))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Bağlantı Hatası (Vertex AI): ${e.localizedMessage ?: e.message}"))
        }
    }
}