package com.applenotes.ai.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.data.remote.ai.AiServiceManager
import com.applenotes.ai.data.remote.github.GitHubUpdateService
import com.applenotes.ai.domain.model.AiProvider
import com.applenotes.ai.domain.model.AppUpdateInfo
import com.applenotes.ai.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val activeProvider: AiProvider = AiProvider.GEMINI,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash",
    val vertexProjectId: String = "",
    val vertexRegion: String = "us-central1",
    val vertexApiKey: String = "",
    val vertexModel: String = "gemini-2.5-flash",
    val openAiApiKey: String = "",
    val openAiModel: String = "gpt-4o-mini",
    val claudeApiKey: String = "",
    val claudeModel: String = "claude-3-5-sonnet-20241022",
    val openRouterApiKey: String = "",
    val openRouterModel: String = "google/gemini-2.5-flash",
    val groqApiKey: String = "",
    val groqModel: String = "llama3-8b-8192",
    val onDeviceModelPath: String = "",
    val onDeviceModelStatus: String = "",
    val githubOwner: String = "aturkk",
    val githubRepo: String = "notlarim-app",
    val autoCheckUpdates: Boolean = true,
    val isCheckingUpdate: Boolean = false,
    val updateInfo: AppUpdateInfo? = null,
    val updateMessage: String? = null,
    val isTestingApi: Boolean = false,
    val testApiMessage: String? = null,
    val isDownloadInProgress: Boolean = false,
    val downloadProgress: Int = 0,
    val autoBackupEnabled: Boolean = true,
    val autoBackupFrequency: String = "DAILY",
    val lastBackupTime: Long = 0L,
    val isBackupInProgress: Boolean = false,
    val backupMessage: String? = null,
    val totalStorageBytes: Long = 0L,
    val databaseSizeBytes: Long = 0L,
    val mediaSizeBytes: Long = 0L,
    val cacheSizeBytes: Long = 0L,
    val isCalculatingStorage: Boolean = false,
    val storageCleanMessage: String? = null
)

class SettingsViewModel(
    private val prefs: SecurePreferences,
    private val updateService: GitHubUpdateService,
    private val aiServiceManager: AiServiceManager,
    private val repository: NoteRepository
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
            groqApiKey = prefs.groqApiKey,
            groqModel = prefs.groqModel,
            onDeviceModelPath = prefs.onDeviceModelPath,
            onDeviceModelStatus = if (prefs.onDeviceModelPath.isNotBlank()) "✅ Model yüklü: ${prefs.onDeviceModelPath}" else "⚠️ Model dosyası seçilmedi",
            githubOwner = prefs.githubOwner,
            githubRepo = prefs.githubRepo,
            autoCheckUpdates = prefs.autoCheckUpdates,
            autoBackupEnabled = prefs.autoBackupEnabled,
            autoBackupFrequency = prefs.autoBackupFrequency,
            lastBackupTime = prefs.lastBackupTime
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

    fun onGroqKeyChange(key: String) {
        prefs.groqApiKey = key
        _uiState.update { it.copy(groqApiKey = key) }
    }

    fun onGroqModelChange(model: String) {
        prefs.groqModel = model
        _uiState.update { it.copy(groqModel = model) }
    }

    fun onDeviceModelPathChange(path: String) {
        val success = aiServiceManager.initializeOnDeviceModel(path)
        val status = if (success) {
            "✅ Model başarıyla yüklendi!"
        } else if (path.isBlank()) {
            "⚠️ Model dosyası seçilmedi"
        } else {
            "❌ Model yüklenemedi. Dosya yolu doğru mu? (.bin dosyası olmalı)"
        }
        _uiState.update { it.copy(onDeviceModelPath = path, onDeviceModelStatus = status) }
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
                        val installResult = updateService.installDownloadedApk()
                        installResult.onFailure { installErr ->
                            _uiState.update { it.copy(updateMessage = installErr.message) }
                        }
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

    fun createBackup(context: android.content.Context) {
        viewModelScope.launch {
            try {
                repository.getAllNotes().first().let { allNotes ->
                    val zipFile = com.applenotes.ai.core.export.NoteExporter.createBackupZip(context, allNotes)
                    com.applenotes.ai.core.export.NoteExporter.shareFile(context, zipFile, "application/zip")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(updateMessage = "Yedekleme hatası: ${e.message}") }
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(updateInfo = null) }
    }

    fun dismissMessageDialog() {
        _uiState.update { it.copy(updateMessage = null, testApiMessage = null, backupMessage = null) }
    }

    fun onAutoBackupToggle(context: android.content.Context, enabled: Boolean) {
        prefs.autoBackupEnabled = enabled
        _uiState.update { it.copy(autoBackupEnabled = enabled) }
        if (enabled) {
            com.applenotes.ai.core.backup.AutoBackupScheduler.schedule(context)
        } else {
            com.applenotes.ai.core.backup.AutoBackupScheduler.cancel(context)
        }
    }

    fun onAutoBackupFrequencyChange(context: android.content.Context, frequency: String) {
        prefs.autoBackupFrequency = frequency
        _uiState.update { it.copy(autoBackupFrequency = frequency) }
        com.applenotes.ai.core.backup.AutoBackupScheduler.schedule(context)
    }

    fun exportBackupToUri(context: android.content.Context, targetUri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupInProgress = true) }
            val notes = repository.getAllNotes().first()
            val result = com.applenotes.ai.core.backup.BackupRestoreHelper.exportBackupToUri(context, targetUri, notes)
            result.onSuccess { count ->
                prefs.lastBackupTime = System.currentTimeMillis()
                _uiState.update {
                    it.copy(
                        isBackupInProgress = false,
                        lastBackupTime = prefs.lastBackupTime,
                        backupMessage = "✅ $count adet not başarıyla Google Drive'a / Dosyalara yedeklendi!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isBackupInProgress = false,
                        backupMessage = "❌ Yedekleme başarısız: ${err.message}"
                    )
                }
            }
        }
    }

    fun restoreBackupFromUri(context: android.content.Context, sourceUri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupInProgress = true) }
            val result = com.applenotes.ai.core.backup.BackupRestoreHelper.restoreBackupFromUri(context, sourceUri, repository)
            result.onSuccess { count ->
                _uiState.update {
                    it.copy(
                        isBackupInProgress = false,
                        backupMessage = "✅ $count adet not başarıyla geri yüklendi!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isBackupInProgress = false,
                        backupMessage = "❌ Geri yükleme başarısız: ${err.message}"
                    )
                }
            }
        }
    }

    fun exportAndShareZip(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val notes = repository.getAllNotes().first()
                val zipFile = com.applenotes.ai.core.backup.BackupRestoreHelper.createBackupZip(context, notes)
                com.applenotes.ai.core.export.NoteExporter.shareFile(context, zipFile, "application/zip")
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Hata: ${e.message}") }
            }
        }
    }

    fun loadStorageUsage(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingStorage = true) }
            val breakdown = com.applenotes.ai.core.storage.StorageHelper.getStorageBreakdown(context)
            _uiState.update {
                it.copy(
                    totalStorageBytes = breakdown.totalBytes,
                    databaseSizeBytes = breakdown.databaseBytes,
                    mediaSizeBytes = breakdown.mediaBytes,
                    cacheSizeBytes = breakdown.cacheBytes,
                    isCalculatingStorage = false
                )
            }
        }
    }

    fun clearCache(context: android.content.Context) {
        viewModelScope.launch {
            val success = com.applenotes.ai.core.storage.StorageHelper.clearCache(context)
            val msg = if (success) "✅ Geçici önbellek başarıyla temizlendi!" else "❌ Önbellek temizlenirken bir sorun oluştu."
            _uiState.update { it.copy(storageCleanMessage = msg) }
            loadStorageUsage(context)
        }
    }

    fun dismissStorageMessage() {
        _uiState.update { it.copy(storageCleanMessage = null) }
    }
}
