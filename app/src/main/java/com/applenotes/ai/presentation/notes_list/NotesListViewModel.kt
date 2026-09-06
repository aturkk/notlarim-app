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

data class NotesListUiState(
    val notes: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val selectedFolderId: Long? = null,
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val isLoading: Boolean = true,
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
    val isSynthesisLoading: Boolean = false
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
    val isSynthesisLoading: Boolean = false
)

private data class FilterParams(
    val query: String,
    val folderId: Long?,
    val tag: String?
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
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _isShowingFolderSheet = MutableStateFlow(false)
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    private val _isGlobalAiChatVisible = MutableStateFlow(false)
    private val _globalChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _isGlobalAiLoading = MutableStateFlow(false)
    private val _selectionState = MutableStateFlow(SelectionState())

    private val filterParams: Flow<FilterParams> = combine(
        _searchQuery,
        _selectedFolderId,
        _selectedTag
    ) { query, folderId, tag ->
        FilterParams(query, folderId, tag)
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
                val filtered = if (params.tag != null) {
                    list.filter { it.tags.contains(params.tag) }
                } else {
                    list
                }
                params to filtered
            }
        },
        repository.getAllFolders(),
        _isShowingFolderSheet,
        _updateInfo,
        _selectionState
    ) { (params, notes), folders, isSheet, updateInfo, sel ->
        NotesListUiState(
            notes = notes,
            folders = folders,
            selectedFolderId = params.folderId,
            searchQuery = params.query,
            selectedTag = params.tag,
            isLoading = false,
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
            isSynthesisLoading = sel.isSynthesisLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesListUiState()
    )

    init {
        if (prefs.autoCheckUpdates) {
            checkForUpdateSilently()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSelectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
        _isShowingFolderSheet.value = false
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

    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createFolder(name.trim())
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
}