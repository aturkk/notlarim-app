package com.applenotes.ai.domain.model

enum class AiProvider(val displayName: String, val defaultModel: String, val availableModels: List<String>) {
    GEMINI(
        displayName = "Google Gemini (Ücretsiz AI Studio)",
        defaultModel = "gemini-1.5-flash",
        availableModels = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash-exp")
    ),
    VERTEX_AI(
        displayName = "Google Cloud Vertex AI",
        defaultModel = "gemini-1.5-flash",
        availableModels = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash-exp")
    ),
    OPENAI(
        displayName = "OpenAI (ChatGPT)",
        defaultModel = "gpt-4o-mini",
        availableModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo")
    ),
    CLAUDE(
        displayName = "Anthropic Claude",
        defaultModel = "claude-3-5-sonnet-20241022",
        availableModels = listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022")
    ),
    OPENROUTER(
        displayName = "OpenRouter (Ücretsiz Modeller)",
        defaultModel = "google/gemini-2.0-flash-exp:free",
        availableModels = listOf("google/gemini-2.0-flash-exp:free", "meta-llama/llama-3.2-3b-instruct:free", "deepseek/deepseek-r1:free")
    ),
    GROQ(
        displayName = "⚡ Groq (Llama 3.3 70B & DeepSeek R1 - Ultra Hızlı)",
        defaultModel = "llama-3.3-70b-versatile",
        availableModels = listOf("llama-3.3-70b-versatile", "deepseek-r1-distill-llama-70b", "gemma2-9b-it", "llama-3.1-8b-instant")
    ),
    GEMINI_NANO(
        displayName = "📱 Cihaz İçi Gemini Nano (İnternetsiz / AICore)",
        defaultModel = "gemini-nano",
        availableModels = listOf("gemini-nano", "aicore-ondevice")
    )
}
