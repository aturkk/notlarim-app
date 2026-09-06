package com.applenotes.ai.presentation.notes_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applenotes.ai.core.security.SecurePreferences
import com.applenotes.ai.data.remote.github.GitHubUpdateService
import com.applenotes.ai.domain.model.AppUpdateInfo
import com.applenotes.ai.domain.model.Folder
import com.applenotes.ai.domain.model.Note
import com.applenotes.ai.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.applenotes.ai.data.remote.ai.AiServiceManager
import com.applenotes.ai.domain.model.ChatMessage
import com.applenotes.ai.domain.model.MessageRole
import com.applenotes.ai.core.templates.NoteTemplate

enum class ViewMode {
    LIST, GALLERY, KANBAN, CALENDAR
}

enum class SmartFolder(val title: String, val icon: String) {
    REMINDERS("Hatırlatıcılar", "⏰"),
    PINNED("Sabitlenenler", "⭐"),
    URGENT("Acil Notlar", "🔴"),
    ATTACHMENTS("Ekler & Medya", "📎"),
    LOCKED("Kilitli Kasa", "🔒")
}

enum class NoteSortOrder(val displayName: String, val icon: String) {
    UPDATED_DESC("Son Güncellenen", "🕒"),
    CREATED_DESC("Oluşturulma Tarihi", "📅"),
    TITLE_ASC("Başlığa Göre (A-Z)", "🔤"),
    PRIORITY_DESC("Önceliğe Göre (Acil → Düşük)", "⚡")
}

data class NotesListUiState(
    val notes: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val selectedFolderId: Long? = null,
    val selectedSmartFolder: SmartFolder? = null,
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val isLoading: Boolean = true,
    val isSemanticSearchActive: Boolean = false,
    val isSemanticSearching: Boolean = false,
    val updateInfo: AppUpdateInfo? = null,
    val isShowingFolderSheet: Boolean = false,
    val isGlobalAiChatVisible: Boolean = false,
    val globalChatMessages: List<ChatMessage> = emptyList(),
    val isGlobalAiLoading: Boolean = false,
    val viewMode: ViewMode = ViewMode.LIST,
    val isGridView: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedNoteIds: Set<Long> = emptySet(),
    val isMoveFolderDialogOpen: Boolean = false,
    val isGraphDialogOpen: Boolean = false,
    val isTemplateSheetOpen: Boolean = false,
    val isMorningDigestVisible: Boolean = false,
    val morningDigestText: String? = null,
    val isMorningDigestLoading: Boolean = false,
    val isSynthesisVisible: Boolean = false,
    val synthesisText: String? = null,
    val isSynthesisLoading: Boolean = false,
    val isDownloadInProgress: Boolean = false,
    val downloadProgress: Int = 0,
    val updateMessage: String? = null,
    val isTrashSheetOpen: Boolean = false,
    val isCompactView: Boolean = false,
    val sortOrder: NoteSortOrder = NoteSortOrder.UPDATED_DESC
)

private data class SelectionState(
    val viewMode: ViewMode = ViewMode.LIST,
    val isSelectionMode: Boolean = false,
    val selectedNoteIds: Set<Long> = emptySet(),
    val isMoveFolderDialogOpen: Boolean = false,
    val isGraphDialogOpen: Boolean = false,
    val isTemplateSheetOpen: Boolean = false,
    val isMorningDigestVisible: Boolean = false,
    val morningDigestText: String? = null,
    val isMorningDigestLoading: Boolean = false,
    val isSynthesisVisible: Boolean = false,
    val synthesisText: String? = null,
    val isSynthesisLoading: Boolean = false,
    val isDownloadInProgress: Boolean = false,
    val downloadProgress: Int = 0,
    val updateMessage: String? = null,
    val isTrashSheetOpen: Boolean = false,
    val isSemanticSearchActive: Boolean = false,
    val isSemanticSearching: Boolean = false,
    val isCompactView: Boolean = false,
    val sortOrder: NoteSortOrder = NoteSortOrder.UPDATED_DESC
)

private data class FilterParams(
    val query: String,
    val folderId: Long?,
    val tag: String?,
    val smartFolder: SmartFolder?,
    val semanticMatches: List<Long>?
)

@OptIn(ExperimentalCoroutinesApi::class)
class NotesListViewModel(
    private val repository: NoteRepository,
    private val updateService: GitHubUpdateService,
    private val prefs: SecurePreferences,
    private val aiServiceManager: AiServiceManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    private val _selectedSmartFolder = MutableStateFlow<SmartFolder?>(null)
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _semanticMatches = MutableStateFlow<List<Long>?>(null)
    private val _isShowingFolderSheet = MutableStateFlow(false)
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    private val _isGlobalAiChatVisible = MutableStateFlow(false)
    private val _globalChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _isGlobalAiLoading = MutableStateFlow(false)
    private val _selectionState = MutableStateFlow(SelectionState())

    private val filterParams: Flow<FilterParams> = combine(
        _searchQuery,
        _selectedFolderId,
        _selectedTag,
        _selectedSmartFolder,
        _semanticMatches
    ) { query, folderId, tag, smartFolder, semanticMatches ->
        FilterParams(query, folderId, tag, smartFolder, semanticMatches)
    }

    val uiState: StateFlow<NotesListUiState> = combine(
        filterParams.flatMapLatest { params ->
            val flow = if (params.query.isNotBlank()) {
                repository.searchNotes(params.query)
            } else if (params.folderId != null) {
                repository.getNotesByFolder(params.folderId)
            } else {
                repository.getAllNotes()
            }
            flow.map { list ->
                var filtered = if (params.tag != null) {
                    list.filter { it.tags.contains(params.tag) }
                } else {
                    list
                }
                if (params.smartFolder != null) {
                    filtered = when (params.smartFolder) {
                        SmartFolder.REMINDERS -> filtered.filter { it.reminderTime != null && it.reminderTime > 0 }
                        SmartFolder.PINNED -> filtered.filter { it.isPinned }
                        SmartFolder.URGENT -> filtered.filter { it.priority?.equals("urgent", true) == true || it.priority?.equals("acil", true) == true }
                        SmartFolder.ATTACHMENTS -> filtered.filter { !it.audioPath.isNullOrBlank() || !it.drawingPath.isNullOrBlank() || it.content.contains("![") || it.content.contains(".pdf") }
                        SmartFolder.LOCKED -> filtered.filter { it.isLocked }
                    }
                }
                if (params.semanticMatches != null && params.semanticMatches.isNotEmpty()) {
                    val orderMap = params.semanticMatches.mapIndexed { idx, id -> id to idx }.toMap()
                    filtered = filtered.filter { orderMap.containsKey(it.id) }
                        .sortedBy { orderMap[it.id] }
                }
                params to filtered
            }
        },
        repository.getAllFolders(),
        _isShowingFolderSheet,
        _updateInfo,
        _selectionState
    ) { (params, notes), folders, isSheet, updateInfo, sel ->
        val sortedNotes = if (params.semanticMatches != null && params.semanticMatches.isNotEmpty()) {
            notes
        } else {
            when (sel.sortOrder) {
                NoteSortOrder.UPDATED_DESC -> notes.sortedWith(
                    compareByDescending<Note> { it.isPinned }
                        .thenByDescending { it.updatedAt }
                )
                NoteSortOrder.CREATED_DESC -> notes.sortedWith(
                    compareByDescending<Note> { it.isPinned }
                        .thenByDescending { it.createdAt }
                )
                NoteSortOrder.TITLE_ASC -> notes.sortedWith(
                    compareByDescending<Note> { it.isPinned }
                        .thenBy { it.title.lowercase() }
                )
                NoteSortOrder.PRIORITY_DESC -> notes.sortedWith(
                    compareByDescending<Note> { it.isPinned }
                        .thenBy {
                            when (it.priority?.lowercase()) {
                                "urgent", "acil" -> 0
                                "high", "yüksek" -> 1
                                "medium", "orta" -> 2
                                "low", "düşük" -> 3
                                else -> 4
                            }
                        }
                        .thenByDescending { it.updatedAt }
                )
            }
        }
        NotesListUiState(
            notes = sortedNotes,
            folders = folders,
            selectedFolderId = params.folderId,
            selectedSmartFolder = params.smartFolder,
            searchQuery = params.query,
            selectedTag = params.tag,
            isLoading = false,
            isSemanticSearchActive = sel.isSemanticSearchActive,
            isSemanticSearching = sel.isSemanticSearching,
            updateInfo = updateInfo,
            isShowingFolderSheet = isSheet,
            viewMode = sel.viewMode,
            isGridView = sel.viewMode == ViewMode.GALLERY,
            isSelectionMode = sel.isSelectionMode,
            selectedNoteIds = sel.selectedNoteIds,
            isMoveFolderDialogOpen = sel.isMoveFolderDialogOpen,
            isGraphDialogOpen = sel.isGraphDialogOpen,
            isTemplateSheetOpen = sel.isTemplateSheetOpen,
            isMorningDigestVisible = sel.isMorningDigestVisible,
            morningDigestText = sel.morningDigestText,
            isMorningDigestLoading = sel.isMorningDigestLoading,
            isSynthesisVisible = sel.isSynthesisVisible,
            synthesisText = sel.synthesisText,
            isSynthesisLoading = sel.isSynthesisLoading,
            isDownloadInProgress = sel.isDownloadInProgress,
            downloadProgress = sel.downloadProgress,
            updateMessage = sel.updateMessage,
            isTrashSheetOpen = sel.isTrashSheetOpen,
            isCompactView = sel.isCompactView,
            sortOrder = sel.sortOrder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesListUiState()
    )

    val trashNotes: StateFlow<List<Note>> = repository.getDeletedNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        if (prefs.autoCheckUpdates) {
            checkForUpdateSilently()
        }
        // 30-Day Auto Trash Cleanup
        viewModelScope.launch {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
            repository.cleanupOldTrash(thirtyDaysAgo)
        }

        // First Run Welcome Note Seeding
        viewModelScope.launch {
            if (!prefs.hasSeededWelcomeNote) {
                val currentNotes = repository.getAllNotes().firstOrNull() ?: emptyList()
                if (currentNotes.isEmpty()) {
                    val welcomeNote = Note(
                        title = "Notism'e Hoş Geldiniz! 🚀",
                        icon = "👋",
                        content = """
# Notism'e Hoş Geldiniz! ✨

Notism, Apple Notes estetiğini modern üretkenlik araçlarıyla birleştiren güçlü ve gizlilik odaklı bir not alma uygulamasıdır.

### ⚡ Hızlı İpuçları
- **Slash Komutları:** Boş bir satırda `/` yazarak başlık, tablo, kontrol listesi veya kod bloğu ekleyin.
- **Biçimlendirme:** Klavyenin hemen üzerindeki **Aa** butonuna dokunarak zengin metin araçlarına ulaşın.
- **İki Yönlü Bağlantılar:** Başka bir nota bağlanmak için `[[Not Başlığı]]` yazın.
- **Görünüm Modları:** Üstteki sekmeden **Liste, Galeri, Kanban Panosu** veya **Takvim** görünümüne geçebilirsiniz.

### ☑️ İlk Görevleriniz
- [x] Notism'i keşfet
- [ ] İlk kendi notunu oluştur
- [ ] Komut paletini dene (Arama çubuğundaki ⚡ simgesi)
- [ ] Ayarlardan favori vurgu rengini seç

---
*Keyifli ve verimli not almalar dileriz! 📝*
                        """.trimIndent(),
                        tags = listOf("Başlangıç", "İpuçları"),
                        isPinned = true
                    )
                    repository.saveNote(welcomeNote)
                }
                prefs.hasSeededWelcomeNote = true
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (_selectionState.value.isSemanticSearchActive && query.isNotBlank()) {
            performSemanticSearch(query)
        } else if (query.isBlank()) {
            _semanticMatches.value = null
        }
    }

    fun onSelectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
        _selectedSmartFolder.value = null
        _isShowingFolderSheet.value = false
    }

    fun onSelectSmartFolder(smartFolder: SmartFolder?) {
        _selectedSmartFolder.value = smartFolder
        _selectedFolderId.value = null
        _isShowingFolderSheet.value = false
    }

    fun openOrCreateDailyNote(onNoteReady: (Long) -> Unit) {
        viewModelScope.launch {
            val sdf = java.text.SimpleDateFormat("d MMMM yyyy, EEEE", java.util.Locale("tr", "TR"))
            val todayStr = sdf.format(java.util.Date())
            val todayTitle = "📅 $todayStr"

            val all = repository.getAllNotes().first()
            val existing = all.firstOrNull { it.title.equals(todayTitle, ignoreCase = true) || it.title.contains(todayStr) }
            if (existing != null) {
                onNoteReady(existing.id)
            } else {
                val templateContent = """
# $todayTitle
> 💡 *"Her gün yeni bir başlangıçtır."*

## 🎯 Bugünün Öncelikli Hedefleri
- [ ] 1. 
- [ ] 2. 
- [ ] 3. 

---

## 📝 Notlar & Gelişmeler
- 

---

## ✨ Gün Sonu Değerlendirmesi
- **Bugün ne iyi gitti?**: 
- **Günün Puanı (1-10)**: 
                """.trimIndent()
                val newNote = Note(
                    title = todayTitle,
                    content = templateContent,
                    tags = listOf("gunluk", "ajanda"),
                    icon = "📅"
                )
                val newId = repository.saveNote(newNote)
                onNoteReady(newId)
            }
        }
    }

    fun toggleSemanticSearch() {
        val current = _selectionState.value.isSemanticSearchActive
        _selectionState.update { it.copy(isSemanticSearchActive = !current) }
        if (current) {
            _semanticMatches.value = null
        } else if (_searchQuery.value.isNotBlank()) {
            performSemanticSearch(_searchQuery.value)
        }
    }

    fun performSemanticSearch(query: String) {
        if (query.isBlank()) {
            _semanticMatches.value = null
            return
        }
        viewModelScope.launch {
            _selectionState.update { it.copy(isSemanticSearching = true) }
            try {
                val all = repository.getAllNotes().first()
                if (all.isEmpty()) {
                    _selectionState.update { it.copy(isSemanticSearching = false) }
                    return@launch
                }
                val promptBuilder = StringBuilder()
                promptBuilder.append("Aşağıdaki notlar arasından kullanıcının aradığı kavrama en uygun olanların ID'lerini alaka sırasına göre virgülle ayırarak yaz (Sadece ID'leri yaz, örn: 3, 15, 2):\n")
                promptBuilder.append("Kullanıcı Araması: $query\n\n")
                all.take(40).forEach { n ->
                    val snippet = n.content.take(120).replace("\n", " ")
                    promptBuilder.append("[ID: ${n.id}] Başlık: ${n.title} | İçerik: $snippet\n")
                }
                val result = aiServiceManager.generateText(promptBuilder.toString()).getOrNull() ?: ""
                val ids = Regex("\\d+").findAll(result).mapNotNull { it.value.toLongOrNull() }.toList()
                _semanticMatches.value = if (ids.isNotEmpty()) ids else null
            } catch (e: Exception) {
                _semanticMatches.value = null
            } finally {
                _selectionState.update { it.copy(isSemanticSearching = false) }
            }
        }
    }

    fun onSelectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun setFolderSheetVisible(visible: Boolean) {
        _isShowingFolderSheet.value = visible
    }

    fun togglePin(noteId: Long) {
        viewModelScope.launch {
            repository.togglePin(noteId)
        }
    }

    fun toggleLock(noteId: Long) {
        viewModelScope.launch {
            repository.toggleLock(noteId)
        }
    }

    fun setGlobalAiChatVisible(visible: Boolean) {
        _isGlobalAiChatVisible.value = visible
    }

    fun sendGlobalChatMessage(userQuestion: String) {
        if (userQuestion.isBlank()) return
        val currentNotes = uiState.value.notes
        val userMsg = ChatMessage(role = MessageRole.USER, content = userQuestion)
        val updatedList = _globalChatMessages.value + userMsg
        _globalChatMessages.value = updatedList
        _isGlobalAiLoading.value = true

        viewModelScope.launch {
            val result = aiServiceManager.chatWithAllNotes(currentNotes, userQuestion, updatedList)
            result.onSuccess { reply ->
                val assistantMsg = ChatMessage(role = MessageRole.ASSISTANT, content = reply)
                _globalChatMessages.value = _globalChatMessages.value + assistantMsg
                _isGlobalAiLoading.value = false
            }.onFailure { err ->
                val errorMsg = ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = "Hata: ${err.message ?: "İstek işlenirken bir sorun oluştu."}"
                )
                _globalChatMessages.value = _globalChatMessages.value + errorMsg
                _isGlobalAiLoading.value = false
            }
        }
    }

    fun setViewMode(mode: ViewMode) {
        _selectionState.value = _selectionState.value.copy(viewMode = mode)
    }

    fun setSortOrder(order: NoteSortOrder) {
        _selectionState.value = _selectionState.value.copy(sortOrder = order)
    }

    fun toggleGridView() {
        val nextMode = when (_selectionState.value.viewMode) {
            ViewMode.LIST -> ViewMode.GALLERY
            ViewMode.GALLERY -> ViewMode.KANBAN
            ViewMode.KANBAN -> ViewMode.CALENDAR
            ViewMode.CALENDAR -> ViewMode.LIST
        }
        setViewMode(nextMode)
    }

    fun createNoteWithReminder(reminderMillis: Long, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newNote = Note(
                title = "",
                content = "",
                reminderTime = reminderMillis,
                updatedAt = System.currentTimeMillis()
            )
            val id = repository.saveNote(newNote)
            onCreated(id)
        }
    }

    fun setGraphDialogOpen(open: Boolean) {
        _selectionState.value = _selectionState.value.copy(isGraphDialogOpen = open)
    }

    fun setTemplateSheetOpen(open: Boolean) {
        _selectionState.value = _selectionState.value.copy(isTemplateSheetOpen = open)
    }

    fun updateKanbanColumn(noteId: Long, column: String) {
        viewModelScope.launch {
            repository.updateKanbanColumn(noteId, column)
        }
    }

    fun createNoteFromTemplate(template: NoteTemplate, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newNote = Note(
                title = template.title,
                content = template.content,
                tags = template.defaultTags,
                icon = template.icon,
                coverUrl = template.coverUrl,
                updatedAt = System.currentTimeMillis()
            )
            val id = repository.saveNote(newNote)
            setTemplateSheetOpen(false)
            onCreated(id)
        }
    }

    fun createKanbanCard(column: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newNote = Note(
                title = "",
                content = "",
                kanbanColumn = column,
                updatedAt = System.currentTimeMillis()
            )
            val id = repository.saveNote(newNote)
            onCreated(id)
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        _selectionState.value = _selectionState.value.copy(
            isSelectionMode = enabled,
            selectedNoteIds = if (enabled) _selectionState.value.selectedNoteIds else emptySet()
        )
    }

    fun toggleSelectionMode() {
        val current = _selectionState.value.isSelectionMode
        setSelectionMode(!current)
    }

    fun toggleSelectNote(noteId: Long) {
        val current = _selectionState.value.selectedNoteIds.toMutableSet()
        if (current.contains(noteId)) {
            current.remove(noteId)
        } else {
            current.add(noteId)
        }
        _selectionState.value = _selectionState.value.copy(
            isSelectionMode = true,
            selectedNoteIds = current
        )
    }

    fun selectAllNotes() {
        val allIds = uiState.value.notes.map { it.id }.toSet()
        _selectionState.value = _selectionState.value.copy(
            isSelectionMode = true,
            selectedNoteIds = allIds
        )
    }

    fun clearSelection() {
        _selectionState.value = _selectionState.value.copy(
            isSelectionMode = false,
            selectedNoteIds = emptySet(),
            isMoveFolderDialogOpen = false
        )
    }

    fun deleteSelectedNotes() {
        val ids = _selectionState.value.selectedNoteIds.toList()
        viewModelScope.launch {
            ids.forEach { id ->
                repository.moveToTrash(id)
            }
            clearSelection()
        }
    }

    fun togglePinSelectedNotes() {
        val ids = _selectionState.value.selectedNoteIds.toList()
        viewModelScope.launch {
            ids.forEach { id ->
                repository.togglePin(id)
            }
            clearSelection()
        }
    }

    fun enterSelectionMode(noteId: Long) {
        _selectionState.value = _selectionState.value.copy(
            isSelectionMode = true,
            selectedNoteIds = setOf(noteId)
        )
    }

    fun setMoveFolderDialogOpen(open: Boolean) {
        _selectionState.value = _selectionState.value.copy(isMoveFolderDialogOpen = open)
    }

    fun moveSelectedNotesToFolder(folderId: Long?) {
        val ids = _selectionState.value.selectedNoteIds.toList()
        viewModelScope.launch {
            ids.forEach { id ->
                repository.moveToFolder(id, folderId)
            }
            clearSelection()
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            repository.moveToTrash(noteId)
        }
    }

    fun setTrashSheetVisible(visible: Boolean) {
        _selectionState.update { it.copy(isTrashSheetOpen = visible) }
    }

    fun restoreFromTrash(noteId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(noteId)
        }
    }

    fun deletePermanently(noteId: Long) {
        viewModelScope.launch {
            repository.deletePermanently(noteId)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createFolder(name.trim())
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            if (_selectedFolderId.value == folderId) {
                _selectedFolderId.value = null
            }
        }
    }

    fun duplicateNote(note: Note) {
        viewModelScope.launch {
            val copyTitle = if (note.title.isNotBlank()) "${note.title} (Kopya)" else "Başlıksız Not (Kopya)"
            val duplicated = note.copy(
                id = 0,
                title = copyTitle,
                isPinned = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(duplicated)
        }
    }

    fun toggleCompactView() {
        _selectionState.update { it.copy(isCompactView = !it.isCompactView) }
    }

    fun toggleChecklistItem(noteId: Long, rawLine: String, currentChecked: Boolean) {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId) ?: return@launch
            val newLine = if (currentChecked) {
                rawLine.replaceFirst("- [x]", "- [ ]").replaceFirst("- [X]", "- [ ]")
            } else {
                rawLine.replaceFirst("- [ ]", "- [x]")
            }
            val newContent = note.content.replaceFirst(rawLine, newLine)
            repository.saveNote(note.copy(content = newContent, updatedAt = System.currentTimeMillis()))
        }
    }

    private fun checkForUpdateSilently() {
        viewModelScope.launch {
            val result = updateService.checkForUpdate()
            result.onSuccess { info ->
                if (info.isUpdateAvailable) {
                    _updateInfo.value = info
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

    fun downloadAndInstallUpdate(downloadUrl: String) {
        _updateInfo.value = null
        _selectionState.update {
            it.copy(
                isDownloadInProgress = true,
                downloadProgress = 0,
                updateMessage = null
            )
        }
        viewModelScope.launch {
            try {
                updateService.downloadApk(downloadUrl).collect { progress ->
                    _selectionState.update { it.copy(downloadProgress = progress) }
                    if (progress >= 100) {
                        _selectionState.update { it.copy(isDownloadInProgress = false) }
                        val installResult = updateService.installDownloadedApk()
                        installResult.onFailure { installErr ->
                            _selectionState.update { it.copy(updateMessage = installErr.message) }
                        }
                    }
                }
            } catch (e: Exception) {
                _selectionState.update {
                    it.copy(
                        isDownloadInProgress = false,
                        updateMessage = "İndirme başarısız: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissUpdateMessage() {
        _selectionState.update { it.copy(updateMessage = null) }
    }

    fun openMorningDigest() {
        _selectionState.value = _selectionState.value.copy(
            isMorningDigestVisible = true,
            isMorningDigestLoading = true,
            morningDigestText = null
        )
        viewModelScope.launch {
            val allNotes = uiState.value.notes
            val result = aiServiceManager.generateMorningDigest(allNotes)
            result.onSuccess { digest ->
                _selectionState.value = _selectionState.value.copy(
                    isMorningDigestLoading = false,
                    morningDigestText = digest
                )
            }.onFailure { err ->
                _selectionState.value = _selectionState.value.copy(
                    isMorningDigestLoading = false,
                    morningDigestText = "Hata: ${err.message ?: "Brifing oluşturulamadı."}"
                )
            }
        }
    }

    fun closeMorningDigest() {
        _selectionState.value = _selectionState.value.copy(
            isMorningDigestVisible = false,
            morningDigestText = null,
            isMorningDigestLoading = false
        )
    }

    fun openSynthesis() {
        val selectedNotes = uiState.value.notes.filter { it.id in _selectionState.value.selectedNoteIds }
        if (selectedNotes.isEmpty()) return
        _selectionState.value = _selectionState.value.copy(
            isSynthesisVisible = true,
            isSynthesisLoading = true,
            synthesisText = null
        )
        viewModelScope.launch {
            val result = aiServiceManager.synthesizeNotes(selectedNotes)
            result.onSuccess { text ->
                _selectionState.value = _selectionState.value.copy(
                    isSynthesisLoading = false,
                    synthesisText = text
                )
            }.onFailure { err ->
                _selectionState.value = _selectionState.value.copy(
                    isSynthesisLoading = false,
                    synthesisText = "Hata: ${err.message ?: "Sentez oluşturulamadı."}"
                )
            }
        }
    }

    fun closeSynthesis() {
        _selectionState.value = _selectionState.value.copy(
            isSynthesisVisible = false,
            synthesisText = null,
            isSynthesisLoading = false
        )
    }

    fun saveReportAsNote(title: String, content: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newNote = Note(
                title = title,
                content = content,
                updatedAt = System.currentTimeMillis()
            )
            val id = repository.saveNote(newNote)
            closeMorningDigest()
            closeSynthesis()
            clearSelection()
            onCreated(id)
        }
    }

    fun getNoteHistory(noteId: Long): Flow<List<com.applenotes.ai.data.local.model.NoteHistoryEntity>> {
        return repository.getNoteHistory(noteId)
    }

    fun restoreVersion(noteId: Long, version: com.applenotes.ai.data.local.model.NoteHistoryEntity) {
        viewModelScope.launch {
            val existing = repository.getNoteById(noteId) ?: return@launch
            // Save current state before restoring
            repository.saveNoteHistory(noteId, existing.title, existing.content)
            repository.saveNote(
                existing.copy(
                    title = version.title,
                    content = version.content,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}