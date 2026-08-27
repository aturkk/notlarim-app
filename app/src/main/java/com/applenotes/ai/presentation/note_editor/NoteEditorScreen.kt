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
import com.applenotes.ai.core.components.CupertinoTopAppBar
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
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = if (uiState.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                            contentDescription = "Sabitle",
                            tint = if (uiState.isPinned) AppleYellow else textSecondary
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, uiState.title)
                            putExtra(Intent.EXTRA_TEXT, "${uiState.title}\n\n${uiState.content}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Notu Paylaş"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş",
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
            Surface(
                color = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Markdown shortcuts
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(onClick = { viewModel.insertMarkdown("### ") }) {
                                Icon(Icons.Default.Title, contentDescription = "Başlık", tint = textSecondary)
                            }
                            IconButton(onClick = { viewModel.insertMarkdown("**", "**") }) {
                                Icon(Icons.Default.FormatBold, contentDescription = "Kalın", tint = textSecondary)
                            }
                            IconButton(onClick = { viewModel.insertMarkdown("*", "*") }) {
                                Icon(Icons.Default.FormatItalic, contentDescription = "İtalik", tint = textSecondary)
                            }
                            IconButton(onClick = { viewModel.insertMarkdown("- [ ] ") }) {
                                Icon(Icons.Default.CheckBox, contentDescription = "Yapılacak", tint = textSecondary)
                            }
                            IconButton(onClick = { viewModel.insertMarkdown("- ") }) {
                                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Liste", tint = textSecondary)
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
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Yapay Zeka",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.SemiBold
                            )
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
                        viewModel.setAiSheetVisible(false)
                        viewModel.setChatSheetVisible(true)
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
