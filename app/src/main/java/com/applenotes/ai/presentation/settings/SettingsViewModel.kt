package com.applenotes.ai.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.data.remote.ai.AiServiceManager
import com.applenotes.ai.data.remote.github.GitHubUpdateService
import com.applenotes.ai.domain.model.AiProvider
import com.applenotes.ai.domain.model.AppUpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val activeProvider: AiProvider = AiProvider.GEMINI,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-1.5-flash",
    val vertexProjectId: String = "",
    val vertexRegion: String = "us-central1",
    val vertexApiKey: String = "",
    val vertexModel: String = "gemini-1.5-flash",
    val openAiApiKey: String = "",
    val openAiModel: String = "gpt-4o-mini",
    val claudeApiKey: String = "",
    val claudeModel: String = "claude-3-5-sonnet-20241022",
    val openRouterApiKey: String = "",
    val openRouterModel: String = "google/gemini-2.0-flash-exp:free",
    val githubOwner: String = "developer",
    val githubRepo: String = "AppleNotesAI",
    val autoCheckUpdates: Boolean = true,
    val isCheckingUpdate: Boolean = false,
    val updateInfo: AppUpdateInfo? = null,
    val updateMessage: String? = null,
    val isTestingApi: Boolean = false,
    val testApiMessage: String? = null,
    val isDownloadInProgress: Boolean = false,
    val downloadProgress: Int = 0
)

class SettingsViewModel(
    private val prefs: SecurePreferences,
    private val updateService: GitHubUpdateService,
    private val aiServiceManager: AiServiceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            activeProvider = prefs.getActiveAiProvider(),
            geminiApiKey = prefs.geminiApiKey,
            geminiModel = prefs.geminiModel,
            vertexProjectId = prefs.vertexProjectId,
            vertexRegion = prefs.vertexRegion,
            vertexApiKey = prefs.vertexApiKey,
            vertexModel = prefs.vertexModel,
            openAiApiKey = prefs.openAiApiKey,
            openAiModel = prefs.openAiModel,
            claudeApiKey = prefs.claudeApiKey,
            claudeModel = prefs.claudeModel,
            openRouterApiKey = prefs.openRouterApiKey,
            openRouterModel = prefs.openRouterModel,
            githubOwner = prefs.githubOwner,
            githubRepo = prefs.githubRepo,
            autoCheckUpdates = prefs.autoCheckUpdates
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setActiveProvider(provider: AiProvider) {
        prefs.setActiveAiProvider(provider)
        _uiState.update { it.copy(activeProvider = provider) }
    }

    fun onGeminiKeyChange(key: String) {
        prefs.geminiApiKey = key
        _uiState.update { it.copy(geminiApiKey = key) }
    }

    fun onGeminiModelChange(model: String) {
        prefs.geminiModel = model
        _uiState.update { it.copy(geminiModel = model) }
    }

    fun onVertexProjectIdChange(projectId: String) {
        prefs.vertexProjectId = projectId
        _uiState.update { it.copy(vertexProjectId = projectId) }
    }

    fun onVertexRegionChange(region: String) {
        prefs.vertexRegion = region
        _uiState.update { it.copy(vertexRegion = region) }
    }

    fun onVertexApiKeyChange(key: String) {
        prefs.vertexApiKey = key
        _uiState.update { it.copy(vertexApiKey = key) }
    }

    fun onVertexModelChange(model: String) {
        prefs.vertexModel = model
        _uiState.update { it.copy(vertexModel = model) }
    }

    fun onOpenAiKeyChange(key: String) {
        prefs.openAiApiKey = key
        _uiState.update { it.copy(openAiApiKey = key) }
    }

    fun onOpenAiModelChange(model: String) {
        prefs.openAiModel = model
        _uiState.update { it.copy(openAiModel = model) }
    }

    fun onClaudeKeyChange(key: String) {
        prefs.claudeApiKey = key
        _uiState.update { it.copy(claudeApiKey = key) }
    }

    fun onClaudeModelChange(model: String) {
        prefs.claudeModel = model
        _uiState.update { it.copy(claudeModel = model) }
    }

    fun onOpenRouterKeyChange(key: String) {
        prefs.openRouterApiKey = key
        _uiState.update { it.copy(openRouterApiKey = key) }
    }

    fun onOpenRouterModelChange(model: String) {
        prefs.openRouterModel = model
        _uiState.update { it.copy(openRouterModel = model) }
    }

    fun onGithubOwnerChange(owner: String) {
        prefs.githubOwner = owner
        _uiState.update { it.copy(githubOwner = owner) }
    }

    fun onGithubRepoChange(repo: String) {
        prefs.githubRepo = repo
        _uiState.update { it.copy(githubRepo = repo) }
    }

    fun onAutoCheckUpdatesToggle(enabled: Boolean) {
        prefs.autoCheckUpdates = enabled
        _uiState.update { it.copy(autoCheckUpdates = enabled) }
    }

    fun testAiConnection() {
        _uiState.update { it.copy(isTestingApi = true, testApiMessage = null) }
        viewModelScope.launch {
            val result = aiServiceManager.chatWithNote("Deneme", "Merhaba! Bağlantı başarılı mı?", emptyList())
            result.onSuccess {
                _uiState.update { it.copy(isTestingApi = false, testApiMessage = "✅ Bağlantı Başarılı! Yapay zeka kullanıma hazır.") }
            }.onFailure { err ->
                _uiState.update { it.copy(isTestingApi = false, testApiMessage = "❌ Bağlantı Başarısız: ${err.message}") }
            }
        }
    }

    fun checkForUpdateManually() {
        _uiState.update { it.copy(isCheckingUpdate = true, updateMessage = null, updateInfo = null) }
        viewModelScope.launch {
            val result = updateService.checkForUpdate()
            result.onSuccess { info ->
                if (info.isUpdateAvailable) {
                    _uiState.update { it.copy(isCheckingUpdate = false, updateInfo = info) }
                } else {
                    _uiState.update { it.copy(isCheckingUpdate = false, updateMessage = "Uygulamanız en güncel sürümde (${info.currentVersion}).") }
                }
            }.onFailure { err ->
                _uiState.update { it.copy(isCheckingUpdate = false, updateMessage = "Hata: ${err.message}") }
            }
        }
    }

    fun downloadAndInstallUpdate(downloadUrl: String) {
        _uiState.update { it.copy(isDownloadInProgress = true, downloadProgress = 0, updateInfo = null) }
        viewModelScope.launch {
            try {
                updateService.downloadApk(downloadUrl).collect { progress ->
                    _uiState.update { it.copy(downloadProgress = progress) }
                    if (progress >= 100) {
                        _uiState.update { it.copy(isDownloadInProgress = false) }
                        updateService.installDownloadedApk()
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDownloadInProgress = false,
                        updateMessage = "İndirme başarısız: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(updateInfo = null) }
    }

    fun dismissMessageDialog() {
        _uiState.update { it.copy(updateMessage = null, testApiMessage = null) }
    }
}
