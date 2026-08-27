package com.applenotes.ai.presentation.notes_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.components.*
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.Note
import com.applenotes.ai.presentation.updater.UpdateDialog

import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.applenotes.ai.core.security.BiometricAuthHelper
import com.applenotes.ai.presentation.ai_assistant.GlobalAiChatBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesListViewModel,
    onNoteClick: (Long) -> Unit,
    onNewNoteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDownloadUpdate: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight

    var isCreatingFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    val pinnedNotes = remember(uiState.notes) { uiState.notes.filter { it.isPinned } }
    val unpinnedNotes = remember(uiState.notes) { uiState.notes.filter { !it.isPinned } }

    val allTags = remember(uiState.notes) {
        uiState.notes.flatMap { it.tags }.distinct()
    }

    val currentFolderTitle = remember(uiState.selectedFolderId, uiState.folders) {
        if (uiState.selectedFolderId == null) "Notlar"
        else uiState.folders.find { it.id == uiState.selectedFolderId }?.name ?: "Klasör"
    }

    val handleNoteClick: (Note) -> Unit = { note ->
        if (note.isLocked && activity != null) {
            BiometricAuthHelper.authenticate(
                activity = activity,
                title = note.title.ifBlank { "Kilitli Not" },
                subtitle = "Notu görüntülemek için kimliğinizi doğrulayın",
                onSuccess = { onNoteClick(note.id) },
                onError = { /* Keep locked */ }
            )
        } else {
            onNoteClick(note.id)
        }
    }

    Scaffold(
        bottomBar = {
            CupertinoBottomBar(
                noteCountText = "${uiState.notes.size} Not",
                onFolderClick = { viewModel.setFolderSheetVisible(true) },
                onNewNoteClick = onNewNoteClick,
                onSettingsClick = onSettingsClick
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Spacer(modifier = Modifier.statusBarsPadding())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CupertinoLargeHeader(
                                title = currentFolderTitle,
                                subtitle = if (uiState.selectedFolderId != null) "Tüm notlara dönmek için klasörler simgesine dokunun" else null
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setGlobalAiChatVisible(true) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppleYellow.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Global AI Asistan",
                                tint = AppleYellow
                            )
                        }
                    }
                }

                item {
                    CupertinoSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onCancel = { viewModel.onSearchQueryChange("") }
                    )
                }

                if (allTags.isNotEmpty() && uiState.searchQuery.isEmpty()) {
                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allTags) { tag ->
                                val isSelected = uiState.selectedTag == tag
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onSelectTag(tag) },
                                    label = {
                                        Text(
                                            text = "#",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppleYellow,
                                        selectedLabelColor = Color.White,
                                        containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.notes.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if (isDark) iOSTextTertiaryDark else iOSTextTertiaryLight,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (uiState.searchQuery.isNotEmpty()) "Sonuç bulunamadı" else "Henüz Not Yok",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Yeni bir not eklemek için sağ alttaki simgeye dokunun.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) iOSTextTertiaryDark else iOSTextTertiaryLight
                                )
                            }
                        }
                    }
                }

                if (pinnedNotes.isNotEmpty()) {
                    item {
                        InsetGroupedSection(title = "Sabitlenenler") {
                            pinnedNotes.forEachIndexed { index, note ->
                                SwipeableNoteCard(
                                    note = note,
                                    onClick = { handleNoteClick(note) },
                                    onTogglePin = { viewModel.togglePin(note.id) },
                                    onDelete = { viewModel.moveToTrash(note.id) }
                                )
                                if (index < pinnedNotes.lastIndex) {
                                    InsetDivider(startIndent = 16.dp)
                                }
                            }
                        }
                    }
                }

                if (unpinnedNotes.isNotEmpty()) {
                    item {
                        InsetGroupedSection(title = if (pinnedNotes.isNotEmpty()) "Notlar" else null) {
                            unpinnedNotes.forEachIndexed { index, note ->
                                SwipeableNoteCard(
                                    note = note,
                                    onClick = { handleNoteClick(note) },
                                    onTogglePin = { viewModel.togglePin(note.id) },
                                    onDelete = { viewModel.moveToTrash(note.id) }
                                )
                                if (index < unpinnedNotes.lastIndex) {
                                    InsetDivider(startIndent = 16.dp)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Folder Bottom Sheet
    if (uiState.isShowingFolderSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setFolderSheetVisible(false) },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Klasörler",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { isCreatingFolder = true }) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "Yeni Klasör",
                            tint = AppleYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // All Notes item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.onSelectFolder(null) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = AppleYellow
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tüm Notlar",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (uiState.selectedFolderId == null) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(
                    color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                    thickness = 0.5.dp
                )

                uiState.folders.forEach { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.onSelectFolder(folder.id) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = AppleYellow
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (uiState.selectedFolderId == folder.id) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // New Folder Dialog
    if (isCreatingFolder) {
        AlertDialog(
            onDismissRequest = { isCreatingFolder = false },
            title = { Text("Yeni Klasör", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("Klasör Adı") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleYellow,
                        cursorColor = AppleYellow
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createFolder(newFolderName)
                        newFolderName = ""
                        isCreatingFolder = false
                    }
                ) {
                    Text("Kaydet", color = AppleYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isCreatingFolder = false }) {
                    Text("Vazgeç", color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight)
                }
            },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }

    // In-App Auto Update Dialog
    uiState.updateInfo?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            onDismiss = viewModel::dismissUpdateDialog,
            onDownload = { onDownloadUpdate(updateInfo.downloadUrl) }
        )
    }

    // Global AI Assistant Sheet
    if (uiState.isGlobalAiChatVisible) {
        GlobalAiChatBottomSheet(
            messages = uiState.globalChatMessages,
            isLoading = uiState.isGlobalAiLoading,
            onSendMessage = viewModel::sendGlobalChatMessage,
            onDismiss = { viewModel.setGlobalAiChatVisible(false) }
        )
    }
}
