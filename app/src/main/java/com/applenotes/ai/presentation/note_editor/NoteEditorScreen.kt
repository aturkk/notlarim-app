package com.applenotes.ai.presentation.note_editor

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.core.content.ContextCompat
import android.Manifest
import com.applenotes.ai.core.audio.AudioRecorderHelper
import com.applenotes.ai.core.components.CupertinoTopAppBar
import com.applenotes.ai.core.components.CupertinoFormatBar
import com.applenotes.ai.core.components.SlashCommandBottomSheet
import com.applenotes.ai.core.components.IconPickerBottomSheet
import com.applenotes.ai.core.components.CoverPickerBottomSheet
import com.applenotes.ai.core.templates.TemplatePickerBottomSheet
import com.applenotes.ai.presentation.note_editor.components.VersionHistoryBottomSheet
import com.applenotes.ai.presentation.note_editor.components.ZenFocusModeDialog
import com.applenotes.ai.presentation.note_editor.components.PomodoroTimerDialog
import com.applenotes.ai.presentation.note_editor.components.AiResultPreviewDialog
import com.applenotes.ai.presentation.note_editor.components.EditorAttachmentBottomSheet
import java.io.File
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import com.applenotes.ai.core.reminder.ReminderScheduler
import com.applenotes.ai.presentation.note_editor.components.ReminderPickerDialog
import com.applenotes.ai.presentation.note_editor.components.TableEditorDialog
import com.applenotes.ai.presentation.note_editor.components.PdfViewerBottomSheet
import com.applenotes.ai.presentation.note_editor.components.MediaLightboxDialog
import com.applenotes.ai.presentation.note_editor.components.EditorTabsBar
import com.applenotes.ai.presentation.note_editor.components.EditorTabItem
import com.applenotes.ai.core.templates.CustomTemplateManager
import com.applenotes.ai.core.templates.CustomTemplate
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.AiAction
import com.applenotes.ai.presentation.ai_assistant.AiChatBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    autoRecordAudio: Boolean = false,
    onBack: () -> Unit,
    onNavigateToNote: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()
    val bgColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val audioHelper = remember { AudioRecorderHelper(context) }
    var isShareSheetOpen by remember { mutableStateOf(false) }
    var isMoreMenuOpen by remember { mutableStateOf(false) }
    var isAttachmentSheetOpen by remember { mutableStateOf(false) }
    var isWikiLinkPickerOpen by remember { mutableStateOf(false) }
    var wikiSearchQuery by remember { mutableStateOf("") }

    val reminderScheduler = remember { ReminderScheduler(context) }
    val templateManager = remember { CustomTemplateManager(context) }
    var isReminderPickerOpen by remember { mutableStateOf(false) }
    var isTableEditorOpen by remember { mutableStateOf(false) }
    var viewingPdfPath by remember { mutableStateOf<String?>(null) }
    var viewingLightboxUrl by remember { mutableStateOf<String?>(null) }
    var recentTabs by remember { mutableStateOf<List<EditorTabItem>>(emptyList()) }

    LaunchedEffect(uiState.noteId, uiState.title, uiState.icon) {
        if (uiState.noteId > 0) {
            val currentItem = EditorTabItem(
                id = uiState.noteId,
                title = uiState.title.ifBlank { "Başlıksız Not" },
                icon = uiState.icon
            )
            val filtered = recentTabs.filter { it.id != uiState.noteId }
            recentTabs = (listOf(currentItem) + filtered).take(6)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioHelper.stopPlaying()
            if (uiState.isRecordingAudio) {
                audioHelper.stopRecording()
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setAudioRecording(true)
            audioHelper.startRecording()
        }
    }

    LaunchedEffect(autoRecordAudio) {
        if (autoRecordAudio) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                viewModel.setAudioRecording(true)
                audioHelper.startRecording()
            } else {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val bytes = context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
                    if (bytes != null) {
                        viewModel.processImageOcr(bytes)
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    val multiImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                val mediaDir = File(context.filesDir, "note_media").apply { mkdirs() }
                val mdList = mutableListOf<String>()
                uris.forEach { uri ->
                    try {
                        val destFile = File(mediaDir, "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            destFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        mdList.add("![](${destFile.absolutePath})")
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                if (mdList.isNotEmpty()) {
                    viewModel.insertContent(mdList.joinToString("\n\n"))
                }
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val pdfDir = File(context.filesDir, "note_pdfs").apply { mkdirs() }
                    val destFile = File(pdfDir, "doc_${System.currentTimeMillis()}.pdf")
                    context.contentResolver.openInputStream(it)?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val pdfMd = "[📄 PDF Belgesi: ${destFile.name}](${destFile.absolutePath})"
                    viewModel.insertContent(pdfMd)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                CupertinoTopAppBar(
                    title = "",
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = AppleYellow
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = viewModel::undo,
                            enabled = uiState.canUndo
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Geri Al",
                                tint = if (uiState.canUndo) AppleYellow else textSecondary.copy(alpha = 0.35f)
                            )
                        }
                        IconButton(
                            onClick = viewModel::redo,
                            enabled = uiState.canRedo
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "İleri Al",
                                tint = if (uiState.canRedo) AppleYellow else textSecondary.copy(alpha = 0.35f)
                            )
                        }

                        // Overflow More Menu (iOS ⋯ style)
                        Box {
                            IconButton(
                                onClick = { isMoreMenuOpen = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "Daha Fazla İşlem",
                                    tint = AppleYellow,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = isMoreMenuOpen,
                                onDismissRequest = { isMoreMenuOpen = false },
                                modifier = Modifier.background(if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Paylaş ve Dışa Aktar") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = AppleYellow) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        isShareSheetOpen = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (uiState.reminderTime != null) "Hatırlatıcıyı Düzenle" else "⏰ Hatırlatıcı Kur") },
                                    leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null, tint = AppleYellow) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        isReminderPickerOpen = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (uiState.isPinned) "Sabitlemeyi Kaldır" else "Başa Sabitle") },
                                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = if (uiState.isPinned) AppleYellow else textSecondary) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        viewModel.togglePin()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (uiState.isLocked) "Notun Kilidini Aç" else "Notu Kilitle") },
                                    leadingIcon = { Icon(if (uiState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = null, tint = if (uiState.isLocked) AppleYellow else textSecondary) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        viewModel.toggleLock()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("📋 Şablon Olarak Kaydet") },
                                    leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = AppleYellow) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        if (uiState.title.isBlank() && uiState.content.isBlank()) {
                                            Toast.makeText(context, "Şablon oluşturmak için başlık veya içerik girin", Toast.LENGTH_SHORT).show()
                                        } else {
                                            coroutineScope.launch {
                                                templateManager.saveTemplate(
                                                    CustomTemplate(
                                                        title = uiState.title.ifBlank { "Özel Şablon" },
                                                        description = "Notism özel şablonu",
                                                        icon = uiState.icon ?: "📋",
                                                        content = uiState.content,
                                                        defaultTags = uiState.tags
                                                    )
                                                )
                                                Toast.makeText(context, "Şablon başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Notla Sohbet Et") },
                                    leadingIcon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = AppleYellow) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        viewModel.setChatSheetVisible(true)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("İçindekiler Tablosu") },
                                    leadingIcon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = textSecondary) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        viewModel.setTocSheetVisible(true)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Pomodoro Odak Zamanlayıcısı") },
                                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = AppleYellow) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        viewModel.setPomodoroOpen(true)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Zen Daktilo Modu") },
                                    leadingIcon = { Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = AppleYellow) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        viewModel.setZenModeOpen(true)
                                    }
                                )
                                HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight)
                                DropdownMenuItem(
                                    text = { Text("Notu Sil", color = iOSRed) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = iOSRed) },
                                    onClick = {
                                        isMoreMenuOpen = false
                                        viewModel.deleteCurrentNote(onBack)
                                    }
                                )
                            }
                        }
                    }
                )
                if (recentTabs.size > 1) {
                    EditorTabsBar(
                        tabs = recentTabs,
                        activeNoteId = uiState.noteId,
                        onSelectTab = { targetId ->
                            if (targetId != uiState.noteId) {
                                onNavigateToNote(targetId)
                            }
                        },
                        onCloseTab = { targetId ->
                            recentTabs = recentTabs.filter { it.id != targetId }
                        }
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            ) {
                AnimatedVisibility(visible = uiState.isFormatBarVisible) {
                    CupertinoFormatBar(
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        onUndo = viewModel::undo,
                        onRedo = viewModel::redo,
                        onBoldClick = { viewModel.insertMarkdown("**", "**") },
                        onItalicClick = { viewModel.insertMarkdown("*", "*") },
                        onStrikeClick = { viewModel.insertMarkdown("~~", "~~") },
                        onH1Click = { viewModel.applyHeader(1) },
                        onH2Click = { viewModel.applyHeader(2) },
                        onChecklistClick = viewModel::applyChecklist,
                        onBulletClick = viewModel::applyBulletList,
                        onNumberedClick = viewModel::applyNumberedList,
                        onQuoteClick = viewModel::applyQuote,
                        onCodeClick = viewModel::applyCodeBlock,
                        onLinkClick = { viewModel.insertMarkdown("[[", "]]") }
                    )
                }

                Surface(
                    color = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
                    tonalElevation = 0.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(
                            color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                            thickness = 0.5.dp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Quick Action Icons (Apple Notes Style)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // "＋" Insert / Attachment Picker
                                IconButton(
                                    onClick = { isAttachmentSheetOpen = true },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Ekle",
                                        tint = AppleYellow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // "Aa" Format bar toggle
                                IconButton(
                                    onClick = viewModel::toggleFormatBar,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Text(
                                        text = "Aa",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = if (uiState.isFormatBarVisible) AppleYellow else textSecondary
                                    )
                                }

                                // Checklist quick shortcut
                                IconButton(
                                    onClick = viewModel::applyChecklist,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckBox,
                                        contentDescription = "Kontrol Listesi",
                                        tint = textSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Markdown visual preview toggle
                                IconButton(
                                    onClick = { viewModel.setMarkdownPreviewVisible(true) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Görsel Önizleme",
                                        tint = textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Right Side: Recording status or AI Assistant button
                            if (uiState.isRecordingAudio) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = iOSRed.copy(alpha = 0.15f),
                                    modifier = Modifier.clickable {
                                        val path = audioHelper.stopRecording()
                                        viewModel.setAudioRecording(false)
                                        if (path != null) {
                                            viewModel.setAudioPath(path)
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Durdur",
                                            tint = iOSRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Kaydı Bitir",
                                            color = iOSRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.setAiSheetVisible(true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppleYellow,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Yapay Zeka",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = bgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Page Cover Banner
                uiState.coverUrl?.let { url ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.setCoverPickerVisible(true) }
                    ) {
                        coil.compose.AsyncImage(
                            model = url,
                            contentDescription = "Kapak Görseli",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Kapağı Değiştir",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action Row: Add Icon / Add Cover / Choose Template
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.icon != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) iOSCardBackgroundDark else Color(0xFFF2F2F7),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { viewModel.setIconPickerVisible(true) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = uiState.icon!!, fontSize = 22.sp)
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { viewModel.setIconPickerVisible(true) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("😀 + Simge Ekle", color = textSecondary, fontSize = 12.sp)
                        }
                    }

                    if (uiState.coverUrl == null) {
                        TextButton(
                            onClick = { viewModel.setCoverPickerVisible(true) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("🖼️ + Kapak Ekle", color = textSecondary, fontSize = 12.sp)
                        }
                    }

                    if (uiState.content.isBlank()) {
                        TextButton(
                            onClick = { viewModel.setTemplatePickerVisible(true) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("✨ Şablon Seç", color = AppleYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title Field
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (uiState.title.isEmpty()) {
                        Text(
                            text = "Başlık",
                            style = MaterialTheme.typography.headlineLarge,
                            color = textSecondary
                        )
                    }
                    BasicTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            color = textPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(AppleYellow),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Reminder Badge Pill
                uiState.reminderTime?.let { timeMillis ->
                    val reminderDateStr = remember(timeMillis) {
                        SimpleDateFormat("d MMMM EEEE, HH:mm", Locale("tr")).format(Date(timeMillis))
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppleYellow.copy(alpha = 0.12f),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { isReminderPickerOpen = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AppleYellowDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hatırlatıcı: $reminderDateStr",
                                color = AppleYellowDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (uiState.noteId > 0) {
                                        reminderScheduler.cancelReminder(uiState.noteId)
                                    }
                                    viewModel.updateReminder(null)
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kaldır",
                                    tint = AppleYellowDark,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AppleYellow.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppleYellowDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Notion-style Page Properties Bar (Priority, Status, Progress)
                Spacer(modifier = Modifier.height(10.dp))
                com.applenotes.ai.presentation.note_editor.components.PagePropertiesBar(
                    priority = uiState.priority,
                    status = uiState.status,
                    progress = uiState.progress,
                    noteContent = uiState.content,
                    onPriorityChange = viewModel::setPriority,
                    onStatusChange = viewModel::setStatus,
                    onProgressChange = viewModel::setProgress
                )

                // Drawing Preview Card
                uiState.drawingPath?.let { path ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎨 Çizim Eki",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleYellow
                                )
                                IconButton(
                                    onClick = viewModel::deleteDrawing,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Çizimi Sil",
                                        tint = iOSRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            coil.compose.AsyncImage(
                                model = java.io.File(path),
                                contentDescription = "Not Çizimi",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                    }
                }

                // Audio Attachment Card
                uiState.audioPath?.let { audioPath ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (uiState.isPlayingAudio) {
                                            audioHelper.stopPlaying()
                                            viewModel.setAudioPlaying(false)
                                        } else {
                                            viewModel.setAudioPlaying(true)
                                            audioHelper.playAudio(audioPath) {
                                                viewModel.setAudioPlaying(false)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(AppleYellow)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (uiState.isPlayingAudio) "Duraklat" else "Oynat",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🎙️ Ses Kaydı",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = if (uiState.isPlayingAudio) "Oynatılıyor..." else "Dokun ve dinle",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary
                                    )
                                }
                                // Transcribe Button
                                FilledTonalButton(
                                    onClick = { viewModel.transcribeAudioFile(audioPath) },
                                    enabled = !uiState.isTranscribingAudio,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    if (uiState.isTranscribingAudio) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = AppleYellow
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = AppleYellow
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Yazıya Dök",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                // Meeting / Lecture Minutes Button
                                FilledTonalButton(
                                    onClick = { viewModel.generateMeetingMinutesFromAudio(audioPath) },
                                    enabled = !uiState.isAiLoading,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "🎓 Tutanak",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleYellow
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        audioHelper.stopPlaying()
                                        viewModel.setAudioPlaying(false)
                                        viewModel.setAudioPath(null)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Ses Kaydını Sil",
                                        tint = iOSRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Timestamp Seek Chips
                            val timestampRegex = remember { Regex("""\[⏱️\s*(\d{1,2}):(\d{2})\]""") }
                            val timestamps = remember(uiState.content) {
                                timestampRegex.findAll(uiState.content).mapNotNull { match ->
                                    val full = match.value
                                    val mins = match.groupValues[1].toIntOrNull() ?: 0
                                    val secs = match.groupValues[2].toIntOrNull() ?: 0
                                    val totalMs = (mins * 60 + secs) * 1000
                                    Pair(full, totalMs)
                                }.distinctBy { it.first }.toList()
                            }
                            if (timestamps.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .padding(bottom = 10.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    timestamps.forEach { (label, ms) ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AppleYellow.copy(alpha = 0.15f),
                                            modifier = Modifier.clickable {
                                                if (!uiState.isPlayingAudio) {
                                                    viewModel.setAudioPlaying(true)
                                                    audioHelper.playAudio(audioPath) {
                                                        viewModel.setAudioPlaying(false)
                                                    }
                                                }
                                                audioHelper.seekTo(ms)
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                color = AppleYellowDark,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Recording Banner
                if (uiState.isRecordingAudio) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = iOSRed.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(iOSRed)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ses kaydediliyor...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = iOSRed,
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalButton(
                                onClick = {
                                    val sec = audioHelper.getRecordingElapsedSeconds()
                                    val m = sec / 60
                                    val s = sec % 60
                                    val stamp = String.format(Locale.ROOT, "[⏱️ %02d:%02d]", m, s)
                                    viewModel.insertContent(stamp)
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("⏱️ Damga", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = iOSRed)
                            }
                        }
                    }
                }

                // OCR Processing Banner
                if (uiState.isOcrLoading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppleYellow.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AppleYellow,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Belge taranıyor ve yapay zeka ile metne dönüştürülüyor...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = AppleYellowDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (uiState.content.isEmpty()) {
                        Text(
                            text = "Notunuzu yazmaya başlayın...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textSecondary
                        )
                    }
                    BasicTextField(
                        value = uiState.content,
                        onValueChange = { newText ->
                            val oldLength = uiState.content.length
                            viewModel.onContentChange(newText)
                            if (newText.length > oldLength && scrollState.value >= scrollState.maxValue - 450) {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        },
                        visualTransformation = com.applenotes.ai.core.components.MarkdownVisualTransformation(isDark),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = textPrimary,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(AppleYellow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 300.dp)
                    )
                }

                if (uiState.content.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        val wordCount = remember(uiState.content) {
                            uiState.content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                        }
                        Text(
                            text = "${uiState.content.length} karakter · $wordCount kelime",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = textSecondary
                        )
                    }
                }

                // Attached PDFs (Click to open native PDF viewer)
                val pdfRegex = remember { Regex("""\[📄\s*PDF Belgesi:\s*([^\]]+)\]\(([^)]+)\)""") }
                val pdfMatches = remember(uiState.content) {
                    pdfRegex.findAll(uiState.content).map { it.groupValues[1] to it.groupValues[2] }.toList()
                }
                if (pdfMatches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("📄 Ekli PDF Belgeleri (${pdfMatches.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    pdfMatches.forEach { (pdfName, pdfPath) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { viewingPdfPath = pdfPath }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = iOSRed, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = pdfName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textPrimary, maxLines = 1)
                                    Text(text = "PDF Okuyucu ile Aç", fontSize = 11.sp, color = textSecondary)
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = textSecondary)
                            }
                        }
                    }
                }

                // Attached Images Gallery (Click to open Pinch-to-zoom Lightbox)
                val imageRegex = remember { Regex("""!\[.*?\]\((file:///[^)]+|/[^)]+|[A-Za-z]:\\[^)]+)\)""") }
                val imageMatches = remember(uiState.content) {
                    imageRegex.findAll(uiState.content).map { it.groupValues[1] }.distinct().toList()
                }
                if (imageMatches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("🖼️ Not Görselleri (${imageMatches.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        imageMatches.forEach { imgPath ->
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewingLightboxUrl = imgPath }
                            ) {
                                coil.compose.AsyncImage(
                                    model = File(imgPath),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // Backlinks Section (Bi-directional links)
                if (uiState.backlinks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = AppleYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bu Nota Bağlanan Notlar (${uiState.backlinks.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            uiState.backlinks.forEach { linkNote ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToNote(linkNote.id) }
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = linkNote.icon ?: "📝",
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = linkNote.title.ifBlank { "Başlıksız Not" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary
                                        )
                                        if (linkNote.content.isNotBlank()) {
                                            Text(
                                                text = linkNote.content.take(60).replace("\n", " "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = textSecondary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                HorizontalDivider(
                                    color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(320.dp))
            }

            // AI Loading Overlay
            if (uiState.isAiLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
                            shadowElevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = AppleYellow)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Yapay Zeka İşliyor...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Drawing Canvas Dialog
    if (uiState.isDrawingDialogOpen) {
        com.applenotes.ai.core.components.AppleDrawingDialog(
            onDismiss = { viewModel.setDrawingDialogOpen(false) },
            onSaveDrawing = viewModel::saveDrawing
        )
    }

    // Flashcards & Mindmap Dialog
    uiState.flashcardsResult?.let { result ->
        com.applenotes.ai.presentation.ai_assistant.AiFlashcardsDialog(
            rawText = result,
            onDismiss = viewModel::dismissFlashcardsDialog
        )
    }

    // AI Action Sheet (Apple Style)
    if (uiState.isAiSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setAiSheetVisible(false) },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Yapay Zeka Sihirbazı",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bu not için uygulamak istediğiniz işlemi seçin:",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chat with Note
                AiActionItem(
                    title = "Notla Sohbet Et",
                    subtitle = "Notun içeriği hakkında soru sor ve yanıt al",
                    icon = Icons.Default.Chat,
                    onClick = {
                        coroutineScope.launch {
                            viewModel.setAiSheetVisible(false)
                            kotlinx.coroutines.delay(120)
                            viewModel.setChatSheetVisible(true)
                        }
                    }
                )

                HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight, thickness = 0.5.dp)

                AiAction.entries.forEach { action ->
                    AiActionItem(
                        title = action.title,
                        subtitle = action.description,
                        icon = when (action) {
                            AiAction.SUMMARIZE -> Icons.Default.Summarize
                            AiAction.REWRITE_PROFESSIONAL -> Icons.Default.Work
                            AiAction.REWRITE_CASUAL -> Icons.Default.Mood
                            AiAction.REWRITE_CONCISE -> Icons.Default.Compress
                            AiAction.EXTRACT_ACTIONS -> Icons.Default.Checklist
                            AiAction.AUTO_TITLE_TAGS -> Icons.Default.Label
                            AiAction.FIX_GRAMMAR -> Icons.Default.Spellcheck
                            AiAction.TRANSLATE -> Icons.Default.Translate
                            AiAction.CONTINUE_WRITING -> Icons.Default.EditNote
                            AiAction.FLASHCARDS -> Icons.Default.School
                            AiAction.MINDMAP -> Icons.Default.AccountTree
                            AiAction.EXTRACT_REMINDERS -> Icons.Default.Alarm
                        },
                        onClick = { viewModel.executeAiAction(action) }
                    )
                    HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight, thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Chat Bottom Sheet
    if (uiState.isChatSheetVisible) {
        AiChatBottomSheet(
            messages = uiState.chatMessages,
            isLoading = uiState.isChatLoading,
            onSendMessage = viewModel::sendChatMessage,
            onDismiss = { viewModel.setChatSheetVisible(false) }
        )
    }

    // AI Result Preview & Tone Regeneration Dialog
    uiState.aiPreviewResult?.let { preview ->
        AiResultPreviewDialog(
            title = preview.title,
            generatedText = preview.generatedText,
            isRegenerating = preview.isRegenerating,
            activeTone = preview.activeTone,
            onApplyAppend = viewModel::applyAiPreviewAppend,
            onApplyReplace = viewModel::applyAiPreviewReplace,
            onRegenerate = viewModel::regenerateAiPreview,
            onDismiss = viewModel::dismissAiPreview
        )
    }

    // Error Snackbar / Alert
    uiState.aiErrorMessage?.let { err ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Yapay Zeka Bildirimi", fontWeight = FontWeight.Bold) },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text("Tamam", color = AppleYellow, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }

    // Editor Attachment Bottom Sheet
    if (isAttachmentSheetOpen) {
        EditorAttachmentBottomSheet(
            onDismiss = { isAttachmentSheetOpen = false },
            onScanDocumentClick = { photoPickerLauncher.launch("image/*") },
            onAddImagesClick = {
                isAttachmentSheetOpen = false
                multiImagePickerLauncher.launch("image/*")
            },
            onAddPdfClick = {
                isAttachmentSheetOpen = false
                pdfPickerLauncher.launch("application/pdf")
            },
            onDrawingClick = { viewModel.setDrawingDialogOpen(true) },
            onVoiceRecordClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    viewModel.setAudioRecording(true)
                    audioHelper.startRecording()
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onInsertTableClick = {
                isAttachmentSheetOpen = false
                isTableEditorOpen = true
            },
            onInsertFormulaClick = {
                viewModel.insertMarkdown("$$ ", " $$")
            },
            onSlashMenuClick = {
                viewModel.setSlashMenuVisible(true)
            },
            onAddWikiLinkClick = {
                isWikiLinkPickerOpen = true
            }
        )
    }

    // Share & Export Bottom Sheet
    if (isShareSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isShareSheetOpen = false },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Dışa Aktar ve Paylaş",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // PDF Export
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            isShareSheetOpen = false
                            viewModel.exportToPdf(context)
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = AppleYellow)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = "PDF Belgesi Olarak Dışa Aktar", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(text = "Notism formatında şık PDF oluşturur", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                    }
                }

                HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight, thickness = 0.5.dp)

                // Image Card Export
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            isShareSheetOpen = false
                            viewModel.exportToImageCard(context)
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = AppleYellow)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = "Sosyal Paylaşım Kartı (Görsel)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(text = "Sosyal medyada paylaşmak için estetik PNG kartı", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                    }
                }

                HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight, thickness = 0.5.dp)

                // Plain Text Share
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            isShareSheetOpen = false
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, uiState.title)
                                putExtra(Intent.EXTRA_TEXT, "${uiState.title}\n\n${uiState.content}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Notu Paylaş"))
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = AppleYellow)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = "Düz Metin Olarak Paylaş", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(text = "Mesaj veya e-posta olarak gönder", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                    }
                }

                HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight, thickness = 0.5.dp)

                // Version History (Time Machine)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            isShareSheetOpen = false
                            viewModel.setVersionHistoryVisible(true)
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = AppleYellow)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = "Zaman Makinesi (Sürüm Geçmişi)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(text = "Notun önceki düzenlemelerine göz at ve geri yükle", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Version History Bottom Sheet
    if (uiState.isVersionHistoryVisible) {
        VersionHistoryBottomSheet(
            historyList = uiState.historyList,
            onDismiss = { viewModel.setVersionHistoryVisible(false) },
            onRestoreVersion = viewModel::restoreVersion
        )
    }

    // Zen Focus Mode Dialog
    if (uiState.isZenModeOpen) {
        ZenFocusModeDialog(
            title = uiState.title,
            content = uiState.content,
            onTitleChange = viewModel::onTitleChange,
            onContentChange = viewModel::onContentChange,
            onDismiss = { viewModel.setZenModeOpen(false) }
        )
    }

    // Pomodoro Timer Dialog
    if (uiState.isPomodoroOpen) {
        PomodoroTimerDialog(
            onDismiss = { viewModel.setPomodoroOpen(false) }
        )
    }

    // Slash Command Bottom Sheet
    if (uiState.isSlashMenuVisible) {
        SlashCommandBottomSheet(
            onDismiss = { viewModel.setSlashMenuVisible(false) },
            onSelectCommand = viewModel::insertSlashCommand
        )
    }

    // Icon Picker Bottom Sheet
    if (uiState.isIconPickerVisible) {
        IconPickerBottomSheet(
            currentIcon = uiState.icon,
            onDismiss = { viewModel.setIconPickerVisible(false) },
            onSelectIcon = viewModel::setIcon
        )
    }

    // Cover Picker Bottom Sheet
    if (uiState.isCoverPickerVisible) {
        CoverPickerBottomSheet(
            currentCoverUrl = uiState.coverUrl,
            onDismiss = { viewModel.setCoverPickerVisible(false) },
            onSelectCover = viewModel::setCoverUrl
        )
    }

    // Template Picker Bottom Sheet
    if (uiState.isTemplatePickerVisible) {
        TemplatePickerBottomSheet(
            onDismiss = { viewModel.setTemplatePickerVisible(false) },
            onSelectTemplate = viewModel::applyTemplate
        )
    }

    // AI Preview Result Dialog (Allows reviewing, tone change, regenerating, and inserting)
    uiState.aiPreviewResult?.let { preview ->
        AiResultPreviewDialog(
            title = preview.title,
            generatedText = preview.generatedText,
            isRegenerating = preview.isRegenerating,
            activeTone = preview.activeTone,
            onApplyAppend = viewModel::applyAiPreviewAppend,
            onApplyReplace = viewModel::applyAiPreviewReplace,
            onRegenerate = viewModel::regenerateAiPreview,
            onDismiss = viewModel::dismissAiPreview
        )
    }

    // Table of Contents Bottom Sheet
    if (uiState.isTocSheetVisible) {
        com.applenotes.ai.presentation.note_editor.components.TableOfContentsBottomSheet(
            noteContent = uiState.content,
            onDismiss = { viewModel.setTocSheetVisible(false) },
            onSelectHeading = { _ ->
                viewModel.setTocSheetVisible(false)
            },
            onInsertTocToNote = viewModel::insertTableOfContents
        )
    }

    // Markdown Visual Preview Bottom Sheet (KaTeX Math & Mermaid Diagrams)
    if (uiState.isMarkdownPreviewVisible) {
        com.applenotes.ai.presentation.note_editor.components.MarkdownPreviewBottomSheet(
            title = uiState.title,
            content = uiState.content,
            onDismiss = { viewModel.setMarkdownPreviewVisible(false) }
        )
    }

    // Wiki-Link Picker Dialog
    if (isWikiLinkPickerOpen) {
        val selectableNotes = allNotes.filter {
            it.id != uiState.noteId && (wikiSearchQuery.isBlank() || it.title.contains(wikiSearchQuery, ignoreCase = true) || it.content.contains(wikiSearchQuery, ignoreCase = true))
        }
        AlertDialog(
            onDismissRequest = {
                isWikiLinkPickerOpen = false
                wikiSearchQuery = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = accentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Not Bağlantısı Ekle", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = wikiSearchQuery,
                        onValueChange = { wikiSearchQuery = it },
                        placeholder = { Text("Not ara...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            cursorColor = accentColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (selectableNotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (wikiSearchQuery.isBlank()) "Bağlanacak başka not bulunamadı" else "Aramayla eşleşen not yok",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondary
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                            items(selectableNotes, key = { it.id }) { targetNote ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.insertWikiLink(targetNote.title.ifBlank { "Başlıksız Not" })
                                            isWikiLinkPickerOpen = false
                                            wikiSearchQuery = ""
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(targetNote.icon ?: "📝", fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = targetNote.title.ifBlank { "Başlıksız Not" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = targetNote.content.take(50).replace("\n", " "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    isWikiLinkPickerOpen = false
                    wikiSearchQuery = ""
                }) {
                    Text("Vazgeç", color = textSecondary)
                }
            },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }

    // Reminder Picker Dialog
    if (isReminderPickerOpen) {
        ReminderPickerDialog(
            currentReminderMillis = uiState.reminderTime,
            onDismissRequest = { isReminderPickerOpen = false },
            onSetReminder = { timeMillis ->
                viewModel.updateReminder(timeMillis)
                if (uiState.noteId > 0) {
                    reminderScheduler.scheduleReminder(
                        noteId = uiState.noteId,
                        title = uiState.title.ifBlank { "Not Hatırlatıcısı" },
                        snippet = uiState.content.take(80),
                        triggerTimeMillis = timeMillis
                    )
                }
                isReminderPickerOpen = false
                Toast.makeText(context, "Hatırlatıcı kuruldu!", Toast.LENGTH_SHORT).show()
            },
            onClearReminder = {
                if (uiState.noteId > 0) {
                    reminderScheduler.cancelReminder(uiState.noteId)
                }
                viewModel.updateReminder(null)
                isReminderPickerOpen = false
            }
        )
    }

    // Table Editor Dialog
    if (isTableEditorOpen) {
        TableEditorDialog(
            onDismissRequest = { isTableEditorOpen = false },
            onInsertTable = { mdTable ->
                viewModel.insertContent(mdTable)
                isTableEditorOpen = false
            }
        )
    }

    // Native PDF Viewer Bottom Sheet
    viewingPdfPath?.let { path ->
        PdfViewerBottomSheet(
            pdfFilePath = path,
            onDismiss = { viewingPdfPath = null }
        )
    }

    // Media Lightbox Dialog (Pinch-to-zoom)
    viewingLightboxUrl?.let { url ->
        MediaLightboxDialog(
            imageUrl = url,
            onDismissRequest = { viewingLightboxUrl = null }
        )
    }
}

@Composable
private fun AiActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val isDark = isAppDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppleYellow,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
            )
        }
    }
}
