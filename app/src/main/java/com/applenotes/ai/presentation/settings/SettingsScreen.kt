package com.applenotes.ai.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.applenotes.ai.presentation.settings.components.CloudSyncDialog
import com.applenotes.ai.presentation.updater.UpdateDialog

/**
 * Ayarlar ekranında bulunan alt sayfaların navigasyon tanımı.
 */
enum class SettingsSubpage(val title: String) {
    MAIN("Ayarlar"),
    APPEARANCE("Görünüm & Tema"),
    AI("Yapay Zeka (AI)"),
    BACKUP("Yedekleme & Bulut"),
    STORAGE("Depolama & Hafıza"),
    UPDATES("Uygulama Güncellemeleri")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()
    val bgColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val context = androidx.compose.ui.platform.LocalContext.current

    var currentSubpage by rememberSaveable { mutableStateOf(SettingsSubpage.MAIN) }

    // Android donanım geri tuşu desteği
    BackHandler(enabled = currentSubpage != SettingsSubpage.MAIN) {
        currentSubpage = SettingsSubpage.MAIN
    }

    LaunchedEffect(Unit) {
        viewModel.loadStorageUsage(context)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupToUri(context, uri)
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreBackupFromUri(context, uri)
        }
    }

    var isCloudSyncDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CupertinoTopAppBar(
                title = if (currentSubpage == SettingsSubpage.MAIN) "Ayarlar & Profil" else currentSubpage.title,
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentSubpage != SettingsSubpage.MAIN) {
                            currentSubpage = SettingsSubpage.MAIN
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = accentColor
                        )
                    }
                }
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentSubpage,
                transitionSpec = {
                    if (targetState != SettingsSubpage.MAIN) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width / 3 } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "SettingsSubpageTransition"
            ) { subpage ->
                when (subpage) {
                    SettingsSubpage.MAIN -> {
                        SettingsMainPage(
                            uiState = uiState,
                            accentColor = accentColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onNavigate = { currentSubpage = it }
                        )
                    }
                    SettingsSubpage.APPEARANCE -> {
                        SettingsAppearancePage(
                            uiState = uiState,
                            viewModel = viewModel,
                            accentColor = accentColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                    SettingsSubpage.AI -> {
                        SettingsAiPage(
                            uiState = uiState,
                            viewModel = viewModel,
                            accentColor = accentColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                    SettingsSubpage.BACKUP -> {
                        SettingsBackupPage(
                            uiState = uiState,
                            viewModel = viewModel,
                            accentColor = accentColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onOpenCloudSync = { isCloudSyncDialogOpen = true },
                            onExportZip = {
                                val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                                exportLauncher.launch("Notism_Backup_$timeStamp.zip")
                            },
                            onRestoreZip = {
                                restoreLauncher.launch(arrayOf("application/zip", "application/json", "application/octet-stream", "*/*"))
                            },
                            onShareZip = {
                                viewModel.exportAndShareZip(context)
                            }
                        )
                    }
                    SettingsSubpage.STORAGE -> {
                        SettingsStoragePage(
                            uiState = uiState,
                            viewModel = viewModel,
                            accentColor = accentColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            isDark = isDark
                        )
                    }
                    SettingsSubpage.UPDATES -> {
                        SettingsUpdatesPage(
                            uiState = uiState,
                            viewModel = viewModel,
                            accentColor = accentColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            isDark = isDark
                        )
                    }
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
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${uiState.downloadProgress}%", style = MaterialTheme.typography.labelLarge)
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

    // Backup in Progress Dialog
    if (uiState.isBackupInProgress) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Yedekleme İşlemi", fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Dosyalar işleniyor, lütfen bekleyin...")
                }
            },
            confirmButton = {},
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }

    // Information Dialogs
    val alertMessage = uiState.testApiMessage ?: uiState.updateMessage ?: uiState.backupMessage ?: uiState.storageCleanMessage
    if (alertMessage != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissMessageDialog()
                viewModel.dismissStorageMessage()
            },
            title = { Text("Bilgilendirme", fontWeight = FontWeight.Bold) },
            text = { Text(alertMessage) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissMessageDialog()
                    viewModel.dismissStorageMessage()
                }) {
                    Text("Tamam", color = accentColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }

    if (isCloudSyncDialogOpen) {
        CloudSyncDialog(
            prefs = viewModel.securePrefs,
            repository = viewModel.noteRepository,
            onDismissRequest = { isCloudSyncDialogOpen = false }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 1. ANA AYARLAR SAYFASI (Kategori Listesi)
// -------------------------------------------------------------------------------------------------

@Composable
private fun SettingsMainPage(
    uiState: SettingsUiState,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    onNavigate: (SettingsSubpage) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CupertinoLargeHeader(
                title = "Ayarlar",
                subtitle = "Uygulama tercihleri ve yapılandırma"
            )
        }

        // Grup 1: Genel Tercihler
        item {
            InsetGroupedSection(
                title = "Genel Tercihler"
            ) {
                // Görünüm & Tema
                val themeSummary = when (uiState.themeMode) {
                    AppThemeMode.LIGHT -> "Açık"
                    AppThemeMode.DARK -> "Koyu"
                    AppThemeMode.SYSTEM -> "Sistem"
                }
                SettingsCategoryRow(
                    icon = Icons.Default.Palette,
                    iconBgColor = Color(0xFF5856D6),
                    title = "Görünüm & Tema",
                    subtitle = "$themeSummary • ${uiState.accentColor.displayName} • ${uiState.fontFamily.displayName}",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = { onNavigate(SettingsSubpage.APPEARANCE) }
                )

                InsetDivider()

                // Yapay Zeka (AI) & BYOK
                val aiModelSummary = when (uiState.activeProvider) {
                    AiProvider.GEMINI -> uiState.geminiModel
                    AiProvider.VERTEX_AI -> uiState.vertexModel
                    AiProvider.OPENAI -> uiState.openAiModel
                    AiProvider.CLAUDE -> uiState.claudeModel
                    AiProvider.OPENROUTER -> uiState.openRouterModel
                    AiProvider.GROQ -> uiState.groqModel
                    AiProvider.GEMINI_NANO -> "Gemma 2B Cihaz İçi"
                }
                SettingsCategoryRow(
                    icon = Icons.Default.AutoAwesome,
                    iconBgColor = Color(0xFFFF9500),
                    title = "Yapay Zeka (AI) & BYOK",
                    subtitle = "${uiState.activeProvider.displayName} • $aiModelSummary",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = { onNavigate(SettingsSubpage.AI) }
                )
            }
        }

        // Grup 2: Veri & Eşitleme
        item {
            InsetGroupedSection(
                title = "Veri & Depolama"
            ) {
                // Yedekleme & Bulut
                val backupSummary = if (uiState.autoBackupEnabled) {
                    val freq = if (uiState.autoBackupFrequency == "DAILY") "Günlük" else "Haftalık"
                    "Otomatik ($freq) • WebDAV & ZIP"
                } else {
                    "WebDAV, Google Drive ZIP"
                }
                SettingsCategoryRow(
                    icon = Icons.Default.CloudSync,
                    iconBgColor = Color(0xFF007AFF),
                    title = "Yedekleme & Bulut Senkronizasyonu",
                    subtitle = backupSummary,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = { onNavigate(SettingsSubpage.BACKUP) }
                )

                InsetDivider()

                // Depolama & Hafıza
                val storageSummary = "${com.applenotes.ai.core.storage.StorageHelper.formatBytes(uiState.totalStorageBytes)} • Veritabanı ve Medya"
                SettingsCategoryRow(
                    icon = Icons.Default.PieChart,
                    iconBgColor = Color(0xFF34C759),
                    title = "Depolama & Hafıza Kullanımı",
                    subtitle = storageSummary,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = { onNavigate(SettingsSubpage.STORAGE) }
                )
            }
        }

        // Grup 3: Sistem & Hakkında
        item {
            InsetGroupedSection(
                title = "Sistem & Sürüm"
            ) {
                SettingsCategoryRow(
                    icon = Icons.Default.SystemUpdate,
                    iconBgColor = Color(0xFF0A84FF),
                    title = "Uygulama Güncellemeleri",
                    subtitle = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) • GitHub Releases",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onClick = { onNavigate(SettingsSubpage.UPDATES) }
                )
            }
        }

        // Alt Bilgi
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Notism",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sürüm: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
            }
        }
    }
}

/**
 * Apple iOS Ayarlar tarzı renkli kare ikonlu kategori satırı.
 */
@Composable
private fun SettingsCategoryRow(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary,
                maxLines = 1
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 2. GÖRÜNÜM & TEMA ALT SAYFASI
// -------------------------------------------------------------------------------------------------

@Composable
private fun SettingsAppearancePage(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CupertinoLargeHeader(
                title = "Görünüm & Tema",
                subtitle = "Arayüz modu, vurgu rengi ve tipografi"
            )
        }

        // Tema Modu
        item {
            InsetGroupedSection(
                title = "Tema Modu",
                footer = "Notism temasını Açık veya Koyu olarak ayarlayabilir veya Sistem modunu seçebilirsiniz. (Varsayılan: Açık)"
            ) {
                val themeOptions = listOf(
                    Triple(AppThemeMode.LIGHT, "Açık (Varsayılan)", Icons.Default.LightMode),
                    Triple(AppThemeMode.DARK, "Koyu", Icons.Default.DarkMode),
                    Triple(AppThemeMode.SYSTEM, "Sistem", Icons.Default.SettingsBrightness)
                )

                themeOptions.forEachIndexed { index, (mode, label, icon) ->
                    val isSelected = uiState.themeMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (mode) {
                                            AppThemeMode.LIGHT -> accentColor.copy(alpha = 0.15f)
                                            AppThemeMode.DARK -> Color(0xFF5E5CE6).copy(alpha = 0.15f)
                                            AppThemeMode.SYSTEM -> Color(0xFF007AFF).copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = when (mode) {
                                        AppThemeMode.LIGHT -> accentColor
                                        AppThemeMode.DARK -> Color(0xFF5E5CE6)
                                        AppThemeMode.SYSTEM -> Color(0xFF007AFF)
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = textPrimary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seçili",
                                tint = accentColor
                            )
                        }
                    }

                    if (index < themeOptions.lastIndex) {
                        InsetDivider()
                    }
                }
            }
        }

        // Vurgu Rengi
        item {
            InsetGroupedSection(
                title = "Vurgu Rengi: ${uiState.accentColor.displayName}",
                footer = "Uygulama butonları, simgeleri ve etkileşimli öğeleri seçilen renge bürünür."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppAccentColor.entries.forEach { itemAccent ->
                        val isSelected = uiState.accentColor == itemAccent
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setAccentColor(itemAccent) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(itemAccent.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = itemAccent.displayName,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (itemAccent) {
                                    AppAccentColor.YELLOW -> "Sarı"
                                    AppAccentColor.BLUE -> "Mavi"
                                    AppAccentColor.GREEN -> "Yeşil"
                                    AppAccentColor.PURPLE -> "Mor"
                                    AppAccentColor.ORANGE -> "Turuncu"
                                    AppAccentColor.RED -> "Kırmızı"
                                },
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) itemAccent.primary else textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Yazı Tipi & Tipografi
        item {
            InsetGroupedSection(
                title = "Yazı Tipi & Tipografi",
                footer = "Uygulama başlıklarının ve not metinlerinin yazı tipi stili."
            ) {
                AppFontFamily.entries.forEachIndexed { index, fontItem ->
                    val isSelected = uiState.fontFamily == fontItem
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setFontFamily(fontItem) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = fontItem.displayName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = fontItem.fontFamily
                                ),
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = textPrimary
                            )
                            Text(
                                text = "Notism ile düşüncelerinizi özgürce not alın.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = fontItem.fontFamily
                                ),
                                color = textPrimary.copy(alpha = 0.6f)
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seçili",
                                tint = accentColor
                            )
                        }
                    }

                    if (index < AppFontFamily.entries.lastIndex) {
                        InsetDivider()
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. YAPAY ZEKA (AI) ALT SAYFASI
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsAiPage(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    var showKey by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CupertinoLargeHeader(
                title = "Yapay Zeka (AI)",
                subtitle = "BYOK sağlayıcı seçimi ve model yapılandırması"
            )
        }

        // Sağlayıcı Seçimi
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
                                text = "Varsayılan: ${provider.defaultModel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                        }

                        if (uiState.activeProvider == provider) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seçili",
                                tint = accentColor
                            )
                        }
                    }

                    if (index < AiProvider.entries.lastIndex) {
                        InsetDivider()
                    }
                }
            }
        }

        // Aktif Sağlayıcı Yapılandırması
        item {
            InsetGroupedSection(
                title = "${uiState.activeProvider.displayName} Yapılandırması"
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.activeProvider == AiProvider.GEMINI_NANO) {
                        // Status card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (uiState.onDeviceModelStatus.startsWith("✅"))
                                accentColor.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📱 Cihaz İçi LLM (MediaPipe + Gemma)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.onDeviceModelStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Gerçek cihaz içi AI için Gemma 2B model dosyasını (.bin) cihazınıza indirmeniz gerekir. Önerilen model (~1.4 GB):\nhttps://huggingface.co/google/gemma-2b-it-gpu-int4\n\nİndirdikten sonra dosya yolunu aşağıya girin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Model path input
                        Text(
                            text = "Model Dosyası Yolu (.bin)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uiState.onDeviceModelPath,
                            onValueChange = viewModel::onDeviceModelPathChange,
                            placeholder = { Text("/data/local/tmp/llm/gemma-2b-it-gpu-int4.bin") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                cursorColor = accentColor
                            ),
                            singleLine = true
                        )
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
                                    focusedBorderColor = accentColor,
                                    cursorColor = accentColor
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
                                    focusedBorderColor = accentColor,
                                    cursorColor = accentColor
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
                                focusedBorderColor = accentColor,
                                cursorColor = accentColor
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
                        AiProvider.GEMINI -> listOf("gemini-3.6-flash", "gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.5-flash-lite", "gemini-2.0-flash", "gemini-1.5-flash")
                        AiProvider.VERTEX_AI -> listOf("gemini-3.6-flash", "gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.5-flash-lite", "gemini-2.0-flash")
                        AiProvider.OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "gpt-3.5-turbo", "o1-preview", "o1-mini")
                        AiProvider.CLAUDE -> listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022", "claude-3-opus-20240229")
                        AiProvider.OPENROUTER -> listOf("google/gemini-2.5-flash", "google/gemini-2.0-flash-001", "meta-llama/llama-3.3-70b-instruct", "openai/gpt-4o-mini", "deepseek/deepseek-chat")
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
                                focusedBorderColor = accentColor,
                                cursorColor = accentColor
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
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
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
    }
}

// -------------------------------------------------------------------------------------------------
// 4. YEDEKLEME & BULUT ALT SAYFASI
// -------------------------------------------------------------------------------------------------

@Composable
private fun SettingsBackupPage(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    onOpenCloudSync: () -> Unit,
    onExportZip: () -> Unit,
    onRestoreZip: () -> Unit,
    onShareZip: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CupertinoLargeHeader(
                title = "Yedekleme & Bulut",
                subtitle = "WebDAV, Google Drive ve yerel arşivler"
            )
        }

        // Bulut & Yerel Yedekleme
        item {
            InsetGroupedSection(
                title = "Yedekleme & Bulut Senkronizasyonu",
                footer = "Notlarınız varsayılan olarak cihazınızda saklanır. Kişisel WebDAV bulutunuzla senkronize edebilir veya Google Drive ve yerel depolamaya tek dokunuşla ZIP yedeği alabilirsiniz."
            ) {
                // 0. Personal Cloud Sync (WebDAV / Nextcloud)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCloudSync() }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kişisel Bulut (WebDAV / Nextcloud)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Nextcloud veya WebDAV sunucunuz ile güvenli eşitleme",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                InsetDivider()

                // 1. Google Drive / SAF Export
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExportZip() }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cihaza / Google Drive'a Yedekle (ZIP)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = "Drive veya cihazda konum seçerek tam ZIP yedeği kaydeder",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                }

                InsetDivider()

                // 2. Restore from Backup
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRestoreZip() }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Yedekten Geri Yükle",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = "Google Drive veya cihazınızdaki yedek dosyasından notları geri yükler",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                }

                InsetDivider()

                // 3. Markdown (.zip) Share
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShareZip() }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Markdown (.zip) Paylaş / Dışa Aktar",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = "Obsidian ve Notion uyumlu .md dosyaları olarak dışa aktarır",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                }
            }
        }

        // Otomatik Yedekleme
        item {
            InsetGroupedSection(
                title = "Otomatik Yedekleme",
                footer = "Cihaz boştayken notlarınızın güvenliği için arka planda düzenli yedekler oluşturulur."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Otomatik Arka Plan Yedekleme",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = "Notlarınızı düzenli aralıklarla yerel depolamaya otomatik yedekler",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = uiState.autoBackupEnabled,
                        onCheckedChange = { viewModel.onAutoBackupToggle(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor
                        )
                    )
                }

                if (uiState.autoBackupEnabled) {
                    InsetDivider()

                    // Sıklık Seçimi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Yedekleme Sıklığı",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.autoBackupFrequency == "DAILY",
                                onClick = { viewModel.onAutoBackupFrequencyChange(context, "DAILY") },
                                label = { Text("Günlük") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = uiState.autoBackupFrequency == "WEEKLY",
                                onClick = { viewModel.onAutoBackupFrequencyChange(context, "WEEKLY") },
                                label = { Text("Haftalık") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    InsetDivider()

                    // Son Yedekleme Zamanı
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Son Yedekleme",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                        val lastBackupStr = if (uiState.lastBackupTime > 0) {
                            java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(uiState.lastBackupTime))
                        } else {
                            "Henüz yapılmadı"
                        }
                        Text(
                            text = lastBackupStr,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 5. DEPOLAMA & HAFIZA ALT SAYFASI
// -------------------------------------------------------------------------------------------------

@Composable
private fun SettingsStoragePage(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CupertinoLargeHeader(
                title = "Depolama & Hafıza",
                subtitle = "Veritabanı, medya ve önbellek yönetimi"
            )
        }

        item {
            InsetGroupedSection(
                title = "Depolama Dağılımı",
                footer = "Uygulamanın yerel veritabanı, ses/çizim medyaları ve geçici önbellek boyutunu gösterir."
            ) {
                // Storage Header with Total and Apple-style Multi-color Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Uygulamanın Kapladığı Yer",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = com.applenotes.ai.core.storage.StorageHelper.formatBytes(uiState.totalStorageBytes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented bar
                    val total = maxOf(uiState.totalStorageBytes, 1L).toFloat()
                    val dbRatio = (uiState.databaseSizeBytes.toFloat() / total).coerceIn(0.05f, 1f)
                    val mediaRatio = (uiState.mediaSizeBytes.toFloat() / total).coerceIn(0.05f, 1f)
                    val cacheRatio = (uiState.cacheSizeBytes.toFloat() / total).coerceIn(0.05f, 1f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA))
                    ) {
                        Box(modifier = Modifier.weight(dbRatio).fillMaxHeight().background(accentColor))
                        Box(modifier = Modifier.weight(mediaRatio).fillMaxHeight().background(Color(0xFF007AFF)))
                        Box(modifier = Modifier.weight(cacheRatio).fillMaxHeight().background(Color(0xFF8E8E93)))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Veritabanı", fontSize = 11.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF007AFF)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ses & Çizim", fontSize = 11.sp, color = textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF8E8E93)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Önbellek", fontSize = 11.sp, color = textSecondary)
                        }
                    }
                }

                InsetDivider()

                // Database Size row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🗄️ Notlar ve Geçmiş Veritabanı", style = MaterialTheme.typography.bodyMedium, color = textPrimary)
                    Text(com.applenotes.ai.core.storage.StorageHelper.formatBytes(uiState.databaseSizeBytes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textSecondary)
                }

                InsetDivider()

                // Media Size row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🎙️ Ses Kayıtları & Çizimler", style = MaterialTheme.typography.bodyMedium, color = textPrimary)
                    Text(com.applenotes.ai.core.storage.StorageHelper.formatBytes(uiState.mediaSizeBytes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textSecondary)
                }

                InsetDivider()

                // Cache Size row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🗑️ Geçici Önbellek (Cache)", style = MaterialTheme.typography.bodyMedium, color = textPrimary)
                    Text(com.applenotes.ai.core.storage.StorageHelper.formatBytes(uiState.cacheSizeBytes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textSecondary)
                }

                InsetDivider()

                // Clear Cache Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearCache(context) }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Önbelleği Temizle",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                        Text(
                            text = "Geçici dosyaları silerek telefon hafızasını rahatlatır",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 6. UYGULAMA GÜNCELLEMELERİ ALT SAYFASI
// -------------------------------------------------------------------------------------------------

@Composable
private fun SettingsUpdatesPage(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            CupertinoLargeHeader(
                title = "Güncellemeler",
                subtitle = "GitHub Releases sürüm denetimi ve indirme"
            )
        }

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
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
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
                                color = accentColor,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kontrol Ediliyor...")
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Güncellemeleri Şimdi Denetle", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            InsetGroupedSection(
                title = "Uygulama Bilgileri"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Uygulama Adı", style = MaterialTheme.typography.bodyMedium, color = textPrimary)
                    Text("Notism (Apple Notes AI)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textSecondary)
                }

                InsetDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sürüm", style = MaterialTheme.typography.bodyMedium, color = textPrimary)
                    Text("${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = textSecondary)
                }

                InsetDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paket Adı", style = MaterialTheme.typography.bodyMedium, color = textPrimary)
                    Text(BuildConfig.APPLICATION_ID, style = MaterialTheme.typography.bodyMedium, color = textSecondary)
                }
            }
        }
    }
}

