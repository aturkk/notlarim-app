package com.applenotes.ai.domain.model

enum class AiProvider(val displayName: String, val defaultModel: String, val availableModels: List<String>) {
    GEMINI(
        displayName = "Google Gemini",
        defaultModel = "gemini-1.5-flash",
        availableModels = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash-exp")
    ),
    OPENAI(
        displayName = "OpenAI",
        defaultModel = "gpt-4o-mini",
        availableModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo")
    ),
    CLAUDE(
        displayName = "Anthropic Claude",
        defaultModel = "claude-3-5-sonnet-20241022",
        availableModels = listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022")
    ),
    OPENROUTER(
        displayName = "OpenRouter / Diğer",
        defaultModel = "google/gemini-flash-1.5",
        availableModels = listOf("google/gemini-flash-1.5", "meta-llama/llama-3.2-3b-instruct", "mistralai/mistral-7b-instruct")
    )
}
