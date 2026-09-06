package com.applenotes.ai.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.applenotes.ai.domain.model.AiProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurePreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "apple_notes_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for emulators/devices with broken keystore
        context.getSharedPreferences("apple_notes_prefs_fallback", Context.MODE_PRIVATE)
    }

    private val _activeProviderFlow = MutableStateFlow(getActiveAiProvider())
    val activeProviderFlow: StateFlow<AiProvider> = _activeProviderFlow.asStateFlow()

    // Gemini
    var geminiApiKey: String
        get() = sharedPreferences.getString(KEY_GEMINI_KEY, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_GEMINI_KEY, value.trim()).apply()

    var geminiModel: String
        get() {
            val saved = sharedPreferences.getString(KEY_GEMINI_MODEL, "gemini-2.5-flash") ?: "gemini-2.5-flash"
            return if (saved == "gemini-1.5-flash" || saved == "gemini-2.0-flash-exp" || saved.isBlank()) {
                "gemini-2.5-flash"
            } else {
                saved
            }
        }
        set(value) = sharedPreferences.edit().putString(KEY_GEMINI_MODEL, value.trim()).apply()

    // Vertex AI
    var vertexProjectId: String
        get() = sharedPreferences.getString(KEY_VERTEX_PROJECT_ID, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_VERTEX_PROJECT_ID, value.trim()).apply()

    var vertexRegion: String
        get() = sharedPreferences.getString(KEY_VERTEX_REGION, "us-central1") ?: "us-central1"
        set(value) = sharedPreferences.edit().putString(KEY_VERTEX_REGION, value.trim()).apply()

    var vertexApiKey: String
        get() = sharedPreferences.getString(KEY_VERTEX_KEY, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_VERTEX_KEY, value.trim()).apply()

    var vertexModel: String
        get() {
            val saved = sharedPreferences.getString(KEY_VERTEX_MODEL, "gemini-2.5-flash") ?: "gemini-2.5-flash"
            return if (saved == "gemini-1.5-flash" || saved.isBlank()) "gemini-2.5-flash" else saved
        }
        set(value) = sharedPreferences.edit().putString(KEY_VERTEX_MODEL, value.trim()).apply()

    // OpenAI
    var openAiApiKey: String
        get() = sharedPreferences.getString(KEY_OPENAI_KEY, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_OPENAI_KEY, value.trim()).apply()

    var openAiModel: String
        get() = sharedPreferences.getString(KEY_OPENAI_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"
        set(value) = sharedPreferences.edit().putString(KEY_OPENAI_MODEL, value.trim()).apply()

    // Claude
    var claudeApiKey: String
        get() = sharedPreferences.getString(KEY_CLAUDE_KEY, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_CLAUDE_KEY, value.trim()).apply()

    var claudeModel: String
        get() = sharedPreferences.getString(KEY_CLAUDE_MODEL, "claude-3-5-sonnet-20241022") ?: "claude-3-5-sonnet-20241022"
        set(value) = sharedPreferences.edit().putString(KEY_CLAUDE_MODEL, value.trim()).apply()

    // OpenRouter / Custom
    var openRouterApiKey: String
        get() = sharedPreferences.getString(KEY_OPENROUTER_KEY, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_OPENROUTER_KEY, value.trim()).apply()

    var openRouterModel: String
        get() {
            val saved = sharedPreferences.getString(KEY_OPENROUTER_MODEL, "google/gemini-2.5-flash") ?: "google/gemini-2.5-flash"
            return if (saved.contains("gemini-2.0-flash-exp") || saved.isBlank()) {
                "google/gemini-2.5-flash"
            } else {
                saved
            }
        }
        set(value) = sharedPreferences.edit().putString(KEY_OPENROUTER_MODEL, value.trim()).apply()

    // Groq
    var groqApiKey: String
        get() = sharedPreferences.getString(KEY_GROQ_KEY, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_GROQ_KEY, value.trim()).apply()

    var groqModel: String
        get() = sharedPreferences.getString(KEY_GROQ_MODEL, "llama-3.3-70b-versatile") ?: "llama-3.3-70b-versatile"
        set(value) = sharedPreferences.edit().putString(KEY_GROQ_MODEL, value.trim()).apply()

    // On-Device LLM (MediaPipe) Model Path
    var onDeviceModelPath: String
        get() = sharedPreferences.getString(KEY_ON_DEVICE_MODEL_PATH, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_ON_DEVICE_MODEL_PATH, value.trim()).apply()

    // Active AI Provider
    fun getActiveAiProvider(): AiProvider {
        val name = sharedPreferences.getString(KEY_ACTIVE_PROVIDER, AiProvider.GROQ.name) ?: AiProvider.GROQ.name
        return try {
            AiProvider.valueOf(name)
        } catch (e: Exception) {
            AiProvider.GROQ
        }
    }

    fun setActiveAiProvider(provider: AiProvider) {
        sharedPreferences.edit().putString(KEY_ACTIVE_PROVIDER, provider.name).apply()
        _activeProviderFlow.value = provider
    }

    // GitHub Updater Settings
    var githubOwner: String
        get() = sharedPreferences.getString(KEY_GITHUB_OWNER, "aturkk") ?: "aturkk"
        set(value) = sharedPreferences.edit().putString(KEY_GITHUB_OWNER, value.trim()).apply()

    var githubRepo: String
        get() = sharedPreferences.getString(KEY_GITHUB_REPO, "notlarim-app") ?: "notlarim-app"
        set(value) = sharedPreferences.edit().putString(KEY_GITHUB_REPO, value.trim()).apply()

    var githubToken: String
        get() = sharedPreferences.getString(KEY_GITHUB_TOKEN, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_GITHUB_TOKEN, value.trim()).apply()

    var autoCheckUpdates: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_CHECK_UPDATES, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_AUTO_CHECK_UPDATES, value).apply()

    // Backup & Sync Settings
    var autoBackupEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_BACKUP_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, value).apply()

    var autoBackupFrequency: String
        get() = sharedPreferences.getString(KEY_AUTO_BACKUP_FREQ, "DAILY") ?: "DAILY"
        set(value) = sharedPreferences.edit().putString(KEY_AUTO_BACKUP_FREQ, value).apply()

    var lastBackupTime: Long
        get() = sharedPreferences.getLong(KEY_LAST_BACKUP_TIME, 0L)
        set(value) = sharedPreferences.edit().putLong(KEY_LAST_BACKUP_TIME, value).apply()

    companion object {
        private const val KEY_GEMINI_KEY = "gemini_api_key"
        private const val KEY_GEMINI_MODEL = "gemini_model"
        private const val KEY_VERTEX_PROJECT_ID = "vertex_project_id"
        private const val KEY_VERTEX_REGION = "vertex_region"
        private const val KEY_VERTEX_KEY = "vertex_api_key"
        private const val KEY_VERTEX_MODEL = "vertex_model"
        private const val KEY_OPENAI_KEY = "openai_api_key"
        private const val KEY_OPENAI_MODEL = "openai_model"
        private const val KEY_CLAUDE_KEY = "claude_api_key"
        private const val KEY_CLAUDE_MODEL = "claude_model"
        private const val KEY_OPENROUTER_KEY = "openrouter_api_key"
        private const val KEY_OPENROUTER_MODEL = "openrouter_model"
        private const val KEY_GROQ_KEY = "groq_api_key"
        private const val KEY_GROQ_MODEL = "groq_model"
        private const val KEY_ON_DEVICE_MODEL_PATH = "on_device_model_path"
        private const val KEY_ACTIVE_PROVIDER = "active_ai_provider"
        private const val KEY_GITHUB_OWNER = "github_owner"
        private const val KEY_GITHUB_REPO = "github_repo"
        private const val KEY_GITHUB_TOKEN = "github_token"
        private const val KEY_AUTO_CHECK_UPDATES = "auto_check_updates"
        private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
        private const val KEY_AUTO_BACKUP_FREQ = "auto_backup_freq"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
    }
}
