package com.applenotes.ai.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.BuildConfig
import com.applenotes.ai.core.components.*
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.AiProvider
import com.applenotes.ai.presentation.updater.UpdateDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val context = androidx.compose.ui.platform.LocalContext.current

    var showKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CupertinoTopAppBar(
                title = "Ayarlar & Profil",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = AppleYellow
                        )
                    }
                }
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                CupertinoLargeHeader(
                    title = "Ayarlar",
                    subtitle = "Yapay zeka anahtarları ve uygulama tercihleri"
                )
            }

            // AI Provider Selection Section
            item {
                InsetGroupedSection(
                    title = "Yapay Zeka Sağlayıcısı (BYOK)",
                    footer = "API anahtarlarınız doğrudan cihazınızın donanım şifreleme çipinde (Android Keystore) saklanır ve hiçbir sunucuya gönderilmez."
                ) {
                    AiProvider.entries.forEachIndexed { index, provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setActiveProvider(provider) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = provider.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (uiState.activeProvider == provider) FontWeight.SemiBold else FontWeight.Normal,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Varsayılan: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }

                            if (uiState.activeProvider == provider) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçili",
                                    tint = AppleYellow
                                )
                            }
                        }

                        if (index < AiProvider.entries.lastIndex) {
                            InsetDivider()
                        }
                    }
                }
            }

            // Active Provider API Key & Model Configuration
            item {
                InsetGroupedSection(
                    title = "${uiState.activeProvider.displayName} Yapılandırması"
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (uiState.activeProvider == AiProvider.GEMINI_NANO) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AppleYellow.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚡ %100 Çevrimdışı & Donanımsal Yapay Zeka",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleYellow
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Bu modda notlarınız internete veya bulut sunucularına gönderilmez. Android 14+ (Pixel, Galaxy S24 vb.) cihazların NPU çipinde yerel olarak işlenir. API anahtarı veya kredi kartı gerekmez.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        } else {
                            if (uiState.activeProvider == AiProvider.VERTEX_AI) {
                                Text(
                                    text = "Google Cloud Project ID",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = uiState.vertexProjectId,
                                    onValueChange = viewModel::onVertexProjectIdChange,
                                    placeholder = { Text("Örn: my-gcp-project-12345") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleYellow,
                                        cursorColor = AppleYellow
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Vertex AI Bölgesi (Location)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = uiState.vertexRegion,
                                    onValueChange = viewModel::onVertexRegionChange,
                                    placeholder = { Text("Örn: us-central1 veya europe-west1") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleYellow,
                                        cursorColor = AppleYellow
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // API Key Field
                            Text(
                                text = if (uiState.activeProvider == AiProvider.VERTEX_AI) "Vertex AI API Anahtarı / Access Token" else "API Anahtarı",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val apiKey = when (uiState.activeProvider) {
                                AiProvider.GEMINI -> uiState.geminiApiKey
                                AiProvider.VERTEX_AI -> uiState.vertexApiKey
                                AiProvider.OPENAI -> uiState.openAiApiKey
                                AiProvider.CLAUDE -> uiState.claudeApiKey
                                AiProvider.OPENROUTER -> uiState.openRouterApiKey
                                AiProvider.GROQ -> uiState.groqApiKey
                                AiProvider.GEMINI_NANO -> ""
                            }

                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = { newVal ->
                                    when (uiState.activeProvider) {
                                        AiProvider.GEMINI -> viewModel.onGeminiKeyChange(newVal)
                                        AiProvider.VERTEX_AI -> viewModel.onVertexApiKeyChange(newVal)
                                        AiProvider.OPENAI -> viewModel.onOpenAiKeyChange(newVal)
                                        AiProvider.CLAUDE -> viewModel.onClaudeKeyChange(newVal)
                                        AiProvider.OPENROUTER -> viewModel.onOpenRouterKeyChange(newVal)
                                        AiProvider.GROQ -> viewModel.onGroqKeyChange(newVal)
                                        AiProvider.GEMINI_NANO -> Unit
                                    }
                                },
                                placeholder = { Text(if (uiState.activeProvider == AiProvider.VERTEX_AI) "Vertex API Key veya OAuth Token..." else if (uiState.activeProvider == AiProvider.GROQ) "Groq API Key (gsk_...)" else "API anahtarınızı yapıştırın...") },
                                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { showKey = !showKey }) {
                                        Icon(
                                            imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = textSecondary
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppleYellow,
                                    cursorColor = AppleYellow
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Model selection field
                        Text(
                            text = "Kullanılacak Model",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val modelName = when (uiState.activeProvider) {
                            AiProvider.GEMINI -> uiState.geminiModel
                            AiProvider.VERTEX_AI -> uiState.vertexModel
                            AiProvider.OPENAI -> uiState.openAiModel
                            AiProvider.CLAUDE -> uiState.claudeModel
                            AiProvider.OPENROUTER -> uiState.openRouterModel
                            AiProvider.GROQ -> uiState.groqModel
                            AiProvider.GEMINI_NANO -> "gemini-nano (Yerleşik NPU)"
                        }

                        var expanded by remember { mutableStateOf(false) }

                        val availableModels = when (uiState.activeProvider) {
                            AiProvider.GEMINI -> listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash-exp")
                            AiProvider.VERTEX_AI -> listOf("gemini-1.5-flash", "gemini-1.5-pro")
                            AiProvider.OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo", "o1-preview", "o1-mini")
                            AiProvider.CLAUDE -> listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022", "claude-3-opus-20240229")
                            AiProvider.OPENROUTER -> listOf("google/gemini-2.0-flash-exp:free", "anthropic/claude-3.5-sonnet", "meta-llama/llama-3.3-70b-instruct", "openai/gpt-4o-mini", "deepseek/deepseek-chat")
                            AiProvider.GROQ -> listOf("llama3-8b-8192", "llama3-70b-8192", "gemma2-9b-it", "llama-4-maverick-17b-128e-instruct", "llama-4-scout-17b-16e-instruct")
                            AiProvider.GEMINI_NANO -> listOf("gemini-nano (Yerleşik NPU)")
                        }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { if (uiState.activeProvider != AiProvider.GEMINI_NANO) expanded = it }
                        ) {
                            OutlinedTextField(
                                value = modelName,
                                onValueChange = { newModel ->
                                    when (uiState.activeProvider) {
                                        AiProvider.GEMINI -> viewModel.onGeminiModelChange(newModel)
                                        AiProvider.VERTEX_AI -> viewModel.onVertexModelChange(newModel)
                                        AiProvider.OPENAI -> viewModel.onOpenAiModelChange(newModel)
                                        AiProvider.CLAUDE -> viewModel.onClaudeModelChange(newModel)
                                        AiProvider.OPENROUTER -> viewModel.onOpenRouterModelChange(newModel)
                                        AiProvider.GROQ -> viewModel.onGroqModelChange(newModel)
                                        AiProvider.GEMINI_NANO -> Unit
                                    }
                                },
                                readOnly = uiState.activeProvider == AiProvider.GEMINI_NANO,
                                placeholder = { Text("Örn: ${uiState.activeProvider.defaultModel}") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppleYellow,
                                    cursorColor = AppleYellow
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    if (uiState.activeProvider != AiProvider.GEMINI_NANO) {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    }
                                }
                            )

                            if (uiState.activeProvider != AiProvider.GEMINI_NANO) {
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    availableModels.forEach { model ->
                                        DropdownMenuItem(
                                            text = { Text(model) },
                                            onClick = {
                                                when (uiState.activeProvider) {
                                                    AiProvider.GEMINI -> viewModel.onGeminiModelChange(model)
                                                    AiProvider.VERTEX_AI -> viewModel.onVertexModelChange(model)
                                                    AiProvider.OPENAI -> viewModel.onOpenAiModelChange(model)
                                                    AiProvider.CLAUDE -> viewModel.onClaudeModelChange(model)
                                                    AiProvider.OPENROUTER -> viewModel.onOpenRouterModelChange(model)
                                                    AiProvider.GROQ -> viewModel.onGroqModelChange(model)
                                                    AiProvider.GEMINI_NANO -> Unit
                                                }
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val isKeyProvided = when (uiState.activeProvider) {
                            AiProvider.GEMINI -> uiState.geminiApiKey.isNotBlank()
                            AiProvider.VERTEX_AI -> uiState.vertexApiKey.isNotBlank()
                            AiProvider.OPENAI -> uiState.openAiApiKey.isNotBlank()
                            AiProvider.CLAUDE -> uiState.claudeApiKey.isNotBlank()
                            AiProvider.OPENROUTER -> uiState.openRouterApiKey.isNotBlank()
                            AiProvider.GROQ -> uiState.groqApiKey.isNotBlank()
                            AiProvider.GEMINI_NANO -> true
                        }

                        Button(
                            onClick = viewModel::testAiConnection,
                            colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isTestingApi && isKeyProvided
                        ) {
                            if (uiState.isTestingApi) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bağlantı Test Ediliyor...")
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (uiState.activeProvider == AiProvider.GEMINI_NANO) "Yerel AI Motorunu Test Et" else "Bağlantıyı Test Et", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // GitHub Auto-Updater Section
            item {
                InsetGroupedSection(
                    title = "GitHub Otomatik Güncelleme Motoru",
                    footer = "Uygulama, GitHub Releases üzerinden doğrudan APK indirerek kendini güncelleyebilir."
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Otomatik Güncelleme Denetimi",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Uygulama açılışında yeni sürüm kontrolü yap",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                            Switch(
                                checked = uiState.autoCheckUpdates,
                                onCheckedChange = viewModel::onAutoCheckUpdatesToggle,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AppleYellow)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        InsetDivider(startIndent = 0.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "GitHub Repo Bilgileri",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.githubOwner,
                                onValueChange = viewModel::onGithubOwnerChange,
                                label = { Text("Kullanıcı / Organizasyon") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.githubRepo,
                                onValueChange = viewModel::onGithubRepoChange,
                                label = { Text("Repo Adı") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = viewModel::checkForUpdateManually,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight,
                                contentColor = textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isCheckingUpdate
                        ) {
                            if (uiState.isCheckingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = AppleYellow,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kontrol Ediliyor...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = AppleYellow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Güncellemeleri Şimdi Denetle", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Data & Backup Section
            item {
                InsetGroupedSection(
                    title = "Veri Güvenliği ve Yedekleme"
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tüm notlarınızı, etiketlerinizi ve medya dosyalarınızı şifreli bir ZIP arşivi olarak dışa aktarabilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.createBackup(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight,
                                contentColor = textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, tint = AppleYellow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tüm Notları ZIP Olarak Yedekle", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // App Info Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Apple Notes AI",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary
                    )
                    Text(
                        text = "Sürüm: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }
        }
    }

    // Download Progress Dialog
    if (uiState.isDownloadInProgress) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Güncelleme İndiriliyor", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { uiState.downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = AppleYellow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("%", style = MaterialTheme.typography.labelLarge)
                }
            },
            confirmButton = {},
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }

    // Update Available Dialog
    uiState.updateInfo?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            onDismiss = viewModel::dismissUpdateDialog,
            onDownload = { viewModel.downloadAndInstallUpdate(updateInfo.downloadUrl) }
        )
    }

    // Information Dialogs
    val alertMessage = uiState.testApiMessage ?: uiState.updateMessage
    if (alertMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissMessageDialog,
            title = { Text("Bilgilendirme", fontWeight = FontWeight.Bold) },
            text = { Text(alertMessage) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissMessageDialog) {
                    Text("Tamam", color = AppleYellow, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }
}
