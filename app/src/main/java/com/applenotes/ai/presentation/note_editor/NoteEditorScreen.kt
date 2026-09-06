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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
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
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.AiAction
import com.applenotes.ai.presentation.ai_assistant.AiChatBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val audioHelper = remember { AudioRecorderHelper(context) }
    var isShareSheetOpen by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
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
                    IconButton(onClick = { viewModel.setChatSheetVisible(true) }) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Notla Sohbet Et",
                            tint = AppleYellow
                        )
                    }
                    IconButton(onClick = viewModel::toggleLock) {
                        Icon(
                            imageVector = if (uiState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Kilitle",
                            tint = if (uiState.isLocked) AppleYellow else textSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.setDrawingDialogOpen(true) }) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = "Çizim Ekle",
                            tint = AppleYellow
                        )
                    }
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Sabitle",
                            tint = if (uiState.isPinned) AppleYellow else textSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.setPomodoroOpen(true) }) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Pomodoro Odak Zamanlayıcısı",
                            tint = AppleYellow
                        )
                    }
                    IconButton(onClick = { viewModel.setZenModeOpen(true) }) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Zen Daktilo Modu",
                            tint = AppleYellow
                        )
                    }
                    IconButton(onClick = { isShareSheetOpen = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş ve Dışa Aktar",
                            tint = AppleYellow
                        )
                    }
                    IconButton(onClick = {
                        viewModel.deleteCurrentNote(onBack)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = iOSRed
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
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

                                // Notion Slash Block command shortcut
                                IconButton(
                                    onClick = { viewModel.setSlashMenuVisible(true) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Text(
                                        text = "/",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = AppleYellow
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

                                // Camera / Document Scan OCR shortcut
                                IconButton(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Belge veya Görsel Tara",
                                        tint = textSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Drawing quick shortcut
                                IconButton(
                                    onClick = { viewModel.setDrawingDialogOpen(true) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Brush,
                                        contentDescription = "Çizim Ekle",
                                        tint = textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Audio Voice Memo shortcut
                                IconButton(
                                    onClick = {
                                        if (uiState.isRecordingAudio) {
                                            val path = audioHelper.stopRecording()
                                            viewModel.setAudioRecording(false)
                                            if (path != null) {
                                                viewModel.setAudioPath(path)
                                            }
                                        } else {
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
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isRecordingAudio) iOSRed.copy(alpha = 0.15f) else Color.Transparent)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = if (uiState.isRecordingAudio) "Kaydı Bitir" else "Ses Kaydet",
                                        tint = if (uiState.isRecordingAudio) iOSRed else textSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // AI Assistant Button
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
                                text = "Ses kaydediliyor... Bitirmek için mikrofon simgesine tekrar dokunun.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = iOSRed,
                                modifier = Modifier.weight(1f)
                            )
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
                        onValueChange = viewModel::onContentChange,
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
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = linkNote.icon ?: "📝",
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(end = 8.dp)
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
                                                text = linkNote.content.take(60),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = textSecondary,
                                                maxLines = 1
                                            )
                                        }
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

                Spacer(modifier = Modifier.height(40.dp))
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
                        Text(text = "Apple Notes formatında şık PDF oluşturur", style = MaterialTheme.typography.bodySmall, color = textSecondary)
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
}

@Composable
private fun AiActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
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
