package com.applenotes.ai.domain.model

enum class AiProvider(val displayName: String, val defaultModel: String, val availableModels: List<String>) {
    GEMINI(
        displayName = "Google Gemini (Ücretsiz AI Studio)",
        defaultModel = "gemini-2.5-flash",
        availableModels = listOf("gemini-3.6-flash", "gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.5-flash-lite", "gemini-2.0-flash", "gemini-1.5-flash")
    ),
    VERTEX_AI(
        displayName = "Google Cloud Vertex AI",
        defaultModel = "gemini-2.5-flash",
        availableModels = listOf("gemini-3.6-flash", "gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.5-flash-lite", "gemini-2.0-flash")
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
        displayName = "OpenRouter (Ücretsiz & Bulut Modeller)",
        defaultModel = "google/gemini-2.5-flash",
        availableModels = listOf("google/gemini-2.5-flash", "google/gemini-2.0-flash-001", "meta-llama/llama-3.3-70b-instruct", "deepseek/deepseek-chat")
    ),
    GROQ(
        displayName = "⚡ Groq (Llama & Gemma - Ultra Hızlı)",
        defaultModel = "llama3-8b-8192",
        availableModels = listOf("llama3-8b-8192", "llama3-70b-8192", "gemma2-9b-it", "llama-4-maverick-17b-128e-instruct", "llama-4-scout-17b-16e-instruct")
    ),
    GEMINI_NANO(
        displayName = "📱 Cihaz İçi Gemini Nano (İnternetsiz / AICore)",
        defaultModel = "gemini-nano",
        availableModels = listOf("gemini-nano", "aicore-ondevice")
    )
}
