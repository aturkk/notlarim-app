package com.applenotes.ai.presentation.notes_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.applenotes.ai.presentation.notes_list.components.KanbanBoardView
import com.applenotes.ai.presentation.notes_list.components.GraphViewDialog
import com.applenotes.ai.presentation.notes_list.components.CalendarView
import com.applenotes.ai.presentation.notes_list.components.MorningDigestDialog
import com.applenotes.ai.presentation.notes_list.components.SynthesisDialog
import com.applenotes.ai.presentation.notes_list.components.AiHubBottomSheet
import com.applenotes.ai.presentation.notes_list.components.TrashBottomSheet
import com.applenotes.ai.core.haptic.rememberHapticFeedbackHelper
import com.applenotes.ai.core.templates.TemplatePickerBottomSheet

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.AnimatedVisibility
import com.applenotes.ai.presentation.notes_list.components.NoteContextMenuBottomSheet
import com.applenotes.ai.core.components.SonnerFloatingToast
import com.applenotes.ai.core.export.NoteExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesListViewModel,
    onNoteClick: (Long) -> Unit,
    onNewNoteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDownloadUpdate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val trashNotes by viewModel.trashNotes.collectAsState()
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()
    val bgColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val haptic = rememberHapticFeedbackHelper()

    val lazyListState = rememberLazyListState()
    val isScrolled by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 40 } }

    var isCreatingFolder by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<com.applenotes.ai.domain.model.Folder?>(null) }
    var noteForContextMenu by remember { mutableStateOf<Note?>(null) }
    var newFolderName by remember { mutableStateOf("") }
    var isMoreMenuExpanded by remember { mutableStateOf(false) }
    var isSortDialogOpen by remember { mutableStateOf(false) }
    var floatingToastMessage by remember { mutableStateOf<String?>(null) }
    var isAiHubSheetVisible by remember { mutableStateOf(false) }
    var isCommandPaletteVisible by remember { mutableStateOf(false) }

    val pinnedNotes = remember(uiState.notes) { uiState.notes.filter { it.isPinned } }
    val unpinnedNotes = remember(uiState.notes) { uiState.notes.filter { !it.isPinned } }

    val allTags = remember(uiState.notes) {
        uiState.notes.flatMap { it.tags }.distinct()
    }

    val currentFolderTitle = remember(uiState.selectedFolderId, uiState.selectedSmartFolder, uiState.folders) {
        val sf = uiState.selectedSmartFolder
        if (sf != null) "${sf.icon} ${sf.title}"
        else if (uiState.selectedFolderId == null) "Notism"
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
            if (uiState.isSelectionMode) {
                Surface(
                    color = if (isDark) iOSBlurOverlayDark else iOSBlurOverlayLight,
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
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
                                .height(54.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = viewModel::clearSelection) {
                                Text(
                                    text = "Vazgeç",
                                    color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                IconButton(
                                    onClick = viewModel::openSynthesis,
                                    enabled = uiState.selectedNoteIds.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Sentezle",
                                        tint = if (uiState.selectedNoteIds.isNotEmpty()) AppleYellow else Color.Gray.copy(alpha = 0.4f)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.setMoveFolderDialogOpen(true) },
                                    enabled = uiState.selectedNoteIds.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DriveFileMove,
                                        contentDescription = "Klasöre Taşı",
                                        tint = if (uiState.selectedNoteIds.isNotEmpty()) AppleYellow else Color.Gray.copy(alpha = 0.4f)
                                    )
                                }

                                IconButton(
                                    onClick = viewModel::togglePinSelectedNotes,
                                    enabled = uiState.selectedNoteIds.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Sabitle",
                                        tint = if (uiState.selectedNoteIds.isNotEmpty()) AppleYellow else Color.Gray.copy(alpha = 0.4f)
                                    )
                                }

                                IconButton(
                                    onClick = viewModel::deleteSelectedNotes,
                                    enabled = uiState.selectedNoteIds.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Sil",
                                        tint = if (uiState.selectedNoteIds.isNotEmpty()) iOSRed else Color.Gray.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                CupertinoBottomBar(
                    noteCountText = "${uiState.notes.size} Not",
                    onFolderClick = { viewModel.setFolderSheetVisible(true) },
                    onNewNoteClick = onNewNoteClick,
                    onSettingsClick = onSettingsClick
                )
            }
        },
        containerColor = bgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Spacer(modifier = Modifier.statusBarsPadding())

                    // Top Action Bar (Folder back button & Quick action icons)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.selectedFolderId != null || uiState.selectedSmartFolder != null) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.onSelectFolder(null)
                                        viewModel.onSelectSmartFolder(null)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.selectedSmartFolder != null) "Tüm Notlar" else "Klasörler",
                                    color = accentColor,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            AnimatedVisibility(visible = isScrolled) {
                                Text(
                                    text = currentFolderTitle,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            if (!isScrolled) {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.isSelectionMode) {
                                TextButton(onClick = {
                                    if (uiState.selectedNoteIds.size == uiState.notes.size) {
                                        viewModel.clearSelection()
                                    } else {
                                        viewModel.selectAllNotes()
                                    }
                                }) {
                                    Text(
                                        text = if (uiState.selectedNoteIds.size == uiState.notes.size) "Kaldır" else "Tümü",
                                        color = AppleYellow,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                TextButton(onClick = viewModel::clearSelection) {
                                    Text(
                                        text = "Bitti",
                                        color = AppleYellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Select button
                                if (uiState.notes.isNotEmpty()) {
                                    TextButton(
                                        onClick = { viewModel.setSelectionMode(true) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Seç",
                                            color = AppleYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                // Spotlight Command Palette
                                IconButton(
                                    onClick = {
                                        haptic.tick()
                                        isCommandPaletteVisible = true
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Spotlight Komut Merkezi",
                                        tint = AppleYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // More Menu (Dropdown for Graph, Templates, New Folder, Settings)
                                Box {
                                    IconButton(
                                        onClick = { isMoreMenuExpanded = true },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreHoriz,
                                            contentDescription = "Daha Fazla",
                                            tint = AppleYellow,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = isMoreMenuExpanded,
                                        onDismissRequest = { isMoreMenuExpanded = false },
                                        modifier = Modifier.background(if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("📅 Günün Notu") },
                                            leadingIcon = { Icon(Icons.Default.Today, contentDescription = null, tint = accentColor) },
                                            onClick = {
                                                isMoreMenuExpanded = false
                                                viewModel.openOrCreateDailyNote(onNoteClick)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Sırala: ${uiState.sortOrder.displayName}") },
                                            leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null, tint = AppleYellow) },
                                            onClick = {
                                                isMoreMenuExpanded = false
                                                isSortDialogOpen = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Ağ Görünümü (Graph)") },
                                            leadingIcon = { Icon(Icons.Default.AccountTree, contentDescription = null, tint = accentColor) },
                                            onClick = {
                                                isMoreMenuExpanded = false
                                                viewModel.setGraphDialogOpen(true)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Şablonlar") },
                                            leadingIcon = { Icon(Icons.Default.PostAdd, contentDescription = null, tint = AppleYellow) },
                                            onClick = {
                                                isMoreMenuExpanded = false
                                                viewModel.setTemplateSheetOpen(true)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (uiState.isCompactView) "Detaylı Liste Görünümü" else "Kompakt Liste Görünümü") },
                                            leadingIcon = { Icon(if (uiState.isCompactView) Icons.Default.ViewAgenda else Icons.Default.TableRows, contentDescription = null, tint = AppleYellow) },
                                            onClick = {
                                                isMoreMenuExpanded = false
                                                viewModel.toggleCompactView()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Yeni Klasör") },
                                            leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = accentColor) },
                                            onClick = {
                                                isMoreMenuExpanded = false
                                                isCreatingFolder = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Son Silinenler") },
                                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = iOSRed) },
                                            onClick = {
                                                isMoreMenuExpanded = false
                                                viewModel.setTrashSheetVisible(true)
                                            }
                                        )
                                        HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight)
                                        DropdownMenuItem(
                                            text = { Text("Ayarlar") },
                                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = accentColor) },
                                            onClick = {
                                                isMoreMenuExpanded = false
                                                onSettingsClick()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Full-width Cupertino Large Title Header (Never wraps vertically)
                    AnimatedVisibility(visible = !isScrolled) {
                        CupertinoLargeHeader(
                            title = if (uiState.isSelectionMode) "${uiState.selectedNoteIds.size} Not Seçildi" else currentFolderTitle,
                            subtitle = if (uiState.isSelectionMode) "İşlem yapmak için notları seçin" else if (uiState.selectedFolderId != null) "Tüm notlara dönmek için klasörler simgesine dokunun" else null
                        )
                    }
                }

                // Notism AI Smart Pill Header (Digest, Global AI, Synthesis Hub)
                if (!uiState.isSelectionMode && uiState.selectedFolderId == null) {
                    item {
                        AnimatedVisibility(visible = !isScrolled) {
                            AiSmartPillHeader(
                                onClick = { isAiHubSheetVisible = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                item {
                    CupertinoSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onCancel = { viewModel.onSearchQueryChange("") },
                        isAiSearchActive = uiState.isSemanticSearchActive,
                        onToggleAiSearch = viewModel::toggleSemanticSearch,
                        isAiSearching = uiState.isSemanticSearching,
                        onCommandPaletteClick = {
                            haptic.tick()
                            isCommandPaletteVisible = true
                        }
                    )
                }

                // Emil Kowalski Segmented Control for View Modes
                item {
                    val segmentItems = remember {
                        listOf(
                            SegmentItem("Liste", Icons.Default.ViewAgenda),
                            SegmentItem("Galeri", Icons.Default.GridView),
                            SegmentItem("Pano", Icons.Default.ViewWeek),
                            SegmentItem("Takvim", Icons.Default.CalendarMonth)
                        )
                    }
                    val currentTab = when (uiState.viewMode) {
                        ViewMode.LIST -> 0
                        ViewMode.GALLERY -> 1
                        ViewMode.KANBAN -> 2
                        ViewMode.CALENDAR -> 3
                    }
                    AppleSegmentedControl(
                        items = segmentItems,
                        selectedIndex = currentTab,
                        onIndexSelected = { index ->
                            val targetMode = when (index) {
                                0 -> ViewMode.LIST
                                1 -> ViewMode.GALLERY
                                2 -> ViewMode.KANBAN
                                else -> ViewMode.CALENDAR
                            }
                            viewModel.setViewMode(targetMode)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                if (uiState.searchQuery.isEmpty()) {
                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "Tümü" (All) Chip
                            val isAllSelected = uiState.selectedSmartFolder == null && uiState.selectedTag == null && uiState.selectedFolderId == null
                            item {
                                FilterChip(
                                    selected = isAllSelected,
                                    onClick = {
                                        haptic.tick()
                                        if (uiState.selectedSmartFolder != null) viewModel.onSelectSmartFolder(null)
                                        if (uiState.selectedTag != null) viewModel.onSelectTag(null)
                                        if (uiState.selectedFolderId != null) viewModel.onSelectFolder(null)
                                    },
                                    label = {
                                        Text(
                                            text = "Tümü",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                                            )
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

                            // Smart Folder Quick Filters
                            val quickSmartFolders = listOf(
                                SmartFolder.PINNED,
                                SmartFolder.REMINDERS,
                                SmartFolder.URGENT,
                                SmartFolder.ATTACHMENTS,
                                SmartFolder.LOCKED
                            )
                            items(quickSmartFolders) { sf ->
                                val isSelected = uiState.selectedSmartFolder == sf
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.tick()
                                        if (isSelected) {
                                            viewModel.onSelectSmartFolder(null)
                                        } else {
                                            viewModel.onSelectSmartFolder(sf)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = "${sf.icon} ${sf.title}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
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

                            // Tags Quick Filters
                            items(allTags) { tag ->
                                val isSelected = uiState.selectedTag == tag
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.tick()
                                        viewModel.onSelectTag(tag)
                                    },
                                    label = {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
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

                // ─── CALENDAR VIEW ──────────────────────────────────────────
                if (uiState.viewMode == ViewMode.CALENDAR) {
                    item {
                        CalendarView(
                            notes = uiState.notes,
                            onNoteClick = handleNoteClick,
                            onAddNoteForDate = { dateMillis ->
                                viewModel.createNoteWithReminder(dateMillis, onNoteClick)
                            }
                        )
                    }
                }

                // ─── KANBAN BOARD VIEW ────────────────────────────────────────
                if (uiState.viewMode == ViewMode.KANBAN && uiState.notes.isNotEmpty()) {
                    item {
                        KanbanBoardView(
                            notes = uiState.notes,
                            onNoteClick = handleNoteClick,
                            onMoveNote = viewModel::updateKanbanColumn,
                            onAddCard = { col ->
                                viewModel.createKanbanCard(col, onNoteClick)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(580.dp)
                        )
                    }
                }

                // ─── GALLERY (GRID) VIEW ──────────────────────────────────────
                if (uiState.viewMode == ViewMode.GALLERY && uiState.notes.isNotEmpty()) {
                    if (pinnedNotes.isNotEmpty()) {
                        item {
                            Text(
                                text = "SABİTLENENLER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = textSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        pinnedNotes.chunked(2).forEach { pair ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { note ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            NoteGalleryCard(
                                                note = note,
                                                isSelected = uiState.selectedNoteIds.contains(note.id),
                                                isSelectionMode = uiState.isSelectionMode,
                                                onClick = {
                                                    if (uiState.isSelectionMode) {
                                                        viewModel.toggleSelectNote(note.id)
                                                    } else {
                                                        handleNoteClick(note)
                                                    }
                                                },
                                                onLongClick = {
                                                    if (uiState.isSelectionMode) {
                                                        viewModel.toggleSelectNote(note.id)
                                                    } else {
                                                        noteForContextMenu = note
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    if (unpinnedNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "NOTLAR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = textSecondary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        unpinnedNotes.chunked(2).forEach { pair ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { note ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            NoteGalleryCard(
                                                note = note,
                                                isSelected = uiState.selectedNoteIds.contains(note.id),
                                                isSelectionMode = uiState.isSelectionMode,
                                                onClick = {
                                                    if (uiState.isSelectionMode) {
                                                        viewModel.toggleSelectNote(note.id)
                                                    } else {
                                                        handleNoteClick(note)
                                                    }
                                                },
                                                onLongClick = {
                                                    if (uiState.isSelectionMode) {
                                                        viewModel.toggleSelectNote(note.id)
                                                    } else {
                                                        noteForContextMenu = note
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // ─── CLASSIC LIST VIEW ────────────────────────────────────────
                if (uiState.viewMode == ViewMode.LIST && uiState.notes.isNotEmpty()) {
                    if (pinnedNotes.isNotEmpty()) {
                        item {
                            InsetGroupedSection(title = "Sabitlenenler") {
                                pinnedNotes.forEachIndexed { index, note ->
                                    key(note.id) {
                                        SwipeableNoteCard(
                                            note = note,
                                            isSelected = uiState.selectedNoteIds.contains(note.id),
                                            isSelectionMode = uiState.isSelectionMode,
                                            isCompact = uiState.isCompactView,
                                            onClick = {
                                                if (uiState.isSelectionMode) {
                                                    viewModel.toggleSelectNote(note.id)
                                                } else {
                                                    handleNoteClick(note)
                                                }
                                            },
                                            onLongClick = {
                                                if (uiState.isSelectionMode) {
                                                    viewModel.toggleSelectNote(note.id)
                                                } else {
                                                    noteForContextMenu = note
                                                }
                                            },
                                            onTogglePin = { viewModel.togglePin(note.id) },
                                            onDelete = { viewModel.moveToTrash(note.id) },
                                            onToggleChecklistItem = viewModel::toggleChecklistItem
                                        )
                                        if (index < pinnedNotes.lastIndex) {
                                            InsetDivider(startIndent = 16.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (unpinnedNotes.isNotEmpty()) {
                        item {
                            InsetGroupedSection(title = if (pinnedNotes.isNotEmpty()) "Notlar" else null) {
                                unpinnedNotes.forEachIndexed { index, note ->
                                    key(note.id) {
                                        SwipeableNoteCard(
                                            note = note,
                                            isSelected = uiState.selectedNoteIds.contains(note.id),
                                            isSelectionMode = uiState.isSelectionMode,
                                            isCompact = uiState.isCompactView,
                                            onClick = {
                                                if (uiState.isSelectionMode) {
                                                    viewModel.toggleSelectNote(note.id)
                                                } else {
                                                    handleNoteClick(note)
                                                }
                                            },
                                            onLongClick = {
                                                if (uiState.isSelectionMode) {
                                                    viewModel.toggleSelectNote(note.id)
                                                } else {
                                                    noteForContextMenu = note
                                                }
                                            },
                                            onTogglePin = { viewModel.togglePin(note.id) },
                                            onDelete = { viewModel.moveToTrash(note.id) },
                                            onToggleChecklistItem = viewModel::toggleChecklistItem
                                        )
                                        if (index < unpinnedNotes.lastIndex) {
                                            InsetDivider(startIndent = 16.dp)
                                        }
                                    }
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

                // Smart Folders Section
                Text(
                    text = "AKILLI KLASÖRLER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 8.dp)
                )

                SmartFolder.entries.forEach { sf ->
                    val isSelected = uiState.selectedSmartFolder == sf
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.onSelectSmartFolder(sf) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = sf.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = sf.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) accentColor else textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                HorizontalDivider(
                    color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Text(
                    text = "KLASÖRLERİM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 8.dp)
                )

                uiState.folders.forEach { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.onSelectFolder(folder.id) }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
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
                        if (folder.noteCount > 0) {
                            Text(
                                text = "${folder.noteCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        IconButton(
                            onClick = { folderToDelete = folder },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Klasörü Sil",
                                tint = iOSRed.copy(alpha = 0.75f),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Son Silinenler (Çöp Kutusu)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.setFolderSheetVisible(false)
                            viewModel.setTrashSheetVisible(true)
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = iOSRed
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Son Silinenler",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (trashNotes.isNotEmpty()) {
                        Text(
                            text = "${trashNotes.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
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

    // Delete Folder Confirmation Dialog
    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Klasörü Sil", fontWeight = FontWeight.Bold) },
            text = {
                Text("\"${folder.name}\" klasörünü silmek istediğinize emin misiniz? Bu klasördeki notlar silinmez, klasörsüz alana taşınır.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFolder(folder.id)
                        folderToDelete = null
                    }
                ) {
                    Text("Sil", color = iOSRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Vazgeç", color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight)
                }
            },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }

    // Peek & Pop Note Long-Press Context Menu
    noteForContextMenu?.let { note ->
        NoteContextMenuBottomSheet(
            note = note,
            onDismiss = { noteForContextMenu = null },
            onTogglePin = { viewModel.togglePin(note.id) },
            onDuplicate = {
                viewModel.duplicateNote(note)
                floatingToastMessage = "Not başarıyla çoğaltıldı"
            },
            onToggleLock = { viewModel.toggleLock(note.id) },
            onSharePdf = {
                coroutineScope.launch {
                    floatingToastMessage = "📄 PDF hazırlanıyor..."
                    try {
                        val file = NoteExporter.exportToPdf(context, note)
                        NoteExporter.shareFile(context, file, "application/pdf")
                    } catch (e: Exception) {
                        floatingToastMessage = "Hata: ${e.localizedMessage}"
                    }
                }
            },
            onShareImageCard = {
                coroutineScope.launch {
                    floatingToastMessage = "🖼️ Paylaşım kartı hazırlanıyor..."
                    try {
                        val file = NoteExporter.exportToImageCard(context, note)
                        NoteExporter.shareFile(context, file, "image/png")
                    } catch (e: Exception) {
                        floatingToastMessage = "Hata: ${e.localizedMessage}"
                    }
                }
            },
            onMoveToFolder = {
                viewModel.enterSelectionMode(note.id)
                viewModel.setMoveFolderDialogOpen(true)
            },
            onEnterSelectMode = {
                viewModel.enterSelectionMode(note.id)
            },
            onDelete = {
                viewModel.moveToTrash(note.id)
                floatingToastMessage = "Not çöp kutusuna taşındı"
            }
        )
    }

    // In-App Auto Update Dialog
    uiState.updateInfo?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            onDismiss = viewModel::dismissUpdateDialog,
            onDownload = { viewModel.downloadAndInstallUpdate(updateInfo.downloadUrl) }
        )
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
                    Text("${uiState.downloadProgress}%", style = MaterialTheme.typography.labelLarge)
                }
            },
            confirmButton = {},
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }

    // Update Message / Error Alert
    uiState.updateMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::dismissUpdateMessage,
            title = { Text("Güncelleme", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissUpdateMessage) {
                    Text("Tamam", color = AppleYellow, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
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

    // Move to Folder Dialog
    if (uiState.isMoveFolderDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setMoveFolderDialogOpen(false) },
            title = {
                Text(
                    text = "Klasöre Taşı",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "${uiState.selectedNoteIds.size} not taşınacak:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Root / No folder option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.moveSelectedNotesToFolder(null) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = AppleYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tüm Notlar (Klasörsüz)",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Folder list
                    if (uiState.folders.isEmpty()) {
                        Text(
                            text = "Henüz özel bir klasör oluşturulmadı.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        uiState.folders.forEach { folder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.moveSelectedNotesToFolder(folder.id) }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = AppleYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.setMoveFolderDialogOpen(false) }) {
                    Text(
                        text = "Vazgeç",
                        color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                    )
                }
            },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }

    // Knowledge Graph 2D Dialog
    if (uiState.isGraphDialogOpen) {
        GraphViewDialog(
            notes = uiState.notes,
            onDismiss = { viewModel.setGraphDialogOpen(false) },
            onNoteClick = { noteId ->
                val targetNote = uiState.notes.find { it.id == noteId }
                if (targetNote != null) handleNoteClick(targetNote)
                else onNoteClick(noteId)
            }
        )
    }

    // Template Picker Bottom Sheet
    if (uiState.isTemplateSheetOpen) {
        TemplatePickerBottomSheet(
            onDismiss = { viewModel.setTemplateSheetOpen(false) },
            onSelectTemplate = { template ->
                viewModel.createNoteFromTemplate(template, onNoteClick)
            }
        )
    }

    // Morning Digest Dialog
    if (uiState.isMorningDigestVisible) {
        MorningDigestDialog(
            isLoading = uiState.isMorningDigestLoading,
            digestContent = uiState.morningDigestText,
            onDismiss = viewModel::closeMorningDigest,
            onSaveAsNote = { title, content ->
                viewModel.saveReportAsNote(title, content, onNoteClick)
            }
        )
    }

    // Multi-Note Synthesis Dialog
    if (uiState.isSynthesisVisible) {
        SynthesisDialog(
            isLoading = uiState.isSynthesisLoading,
            synthesisContent = uiState.synthesisText,
            selectedCount = uiState.selectedNoteIds.size,
            onDismiss = viewModel::closeSynthesis,
            onSaveAsNote = { title, content ->
                viewModel.saveReportAsNote(title, content, onNoteClick)
            }
        )
    }

    // Notism AI Hub Bottom Sheet
    if (isAiHubSheetVisible) {
        AiHubBottomSheet(
            onDismiss = { isAiHubSheetVisible = false },
            onMorningDigestClick = { viewModel.openMorningDigest() },
            onGlobalAiChatClick = { viewModel.setGlobalAiChatVisible(true) },
            onSynthesisClick = {
                if (uiState.selectedNoteIds.isNotEmpty()) {
                    viewModel.openSynthesis()
                } else {
                    viewModel.setSelectionMode(true)
                }
            }
        )
    }

    // Son Silinenler (Çöp Kutusu) Bottom Sheet
    if (uiState.isTrashSheetOpen) {
        TrashBottomSheet(
            deletedNotes = trashNotes,
            onDismiss = { viewModel.setTrashSheetVisible(false) },
            onRestoreNote = viewModel::restoreFromTrash,
            onDeletePermanently = viewModel::deletePermanently,
            onEmptyTrash = viewModel::emptyTrash
        )
    }

    // Spotlight Command Palette
    if (isCommandPaletteVisible) {
        CommandPaletteBottomSheet(
            allNotes = uiState.notes,
            onDismiss = { isCommandPaletteVisible = false },
            onSelectNote = { noteId ->
                val targetNote = uiState.notes.find { it.id == noteId }
                if (targetNote != null) handleNoteClick(targetNote)
                else onNoteClick(noteId)
            },
            onNewNote = onNewNoteClick,
            onOpenDailyNote = { viewModel.openOrCreateDailyNote(onNoteClick) },
            onOpenCloudSync = onSettingsClick,
            onOpenTrash = { viewModel.setTrashSheetVisible(true) },
            onOpenSettings = onSettingsClick,
            onOpenAiHub = { isAiHubSheetVisible = true },
            onSelectSmartFolder = { type ->
                when (type) {
                    "REMINDERS" -> viewModel.onSelectSmartFolder(SmartFolder.REMINDERS)
                    "PINNED" -> viewModel.onSelectSmartFolder(SmartFolder.PINNED)
                    "URGENT" -> viewModel.onSelectSmartFolder(SmartFolder.URGENT)
                }
            }
        )
    }

    // Sort Order Dialog
    if (isSortDialogOpen) {
        AlertDialog(
            onDismissRequest = { isSortDialogOpen = false },
            title = {
                Text(
                    text = "Notları Sırala",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column {
                    NoteSortOrder.entries.forEach { order ->
                        val isSelected = uiState.sortOrder == order
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.selection()
                                    viewModel.setSortOrder(order)
                                    isSortDialogOpen = false
                                    floatingToastMessage = "Sıralama: ${order.displayName}"
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = order.icon, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = order.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isSortDialogOpen = false }) {
                    Text("Tamam", color = accentColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
        )
    }


    // Sonner-Style Floating Feedback Pill
    SonnerFloatingToast(
        message = floatingToastMessage,
        onDismiss = { floatingToastMessage = null }
    )
}

