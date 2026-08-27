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
    val isGlobalAiLoading: Boolean = false
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
        _updateInfo
    ) { (params, notes), folders, isSheet, updateInfo ->
        NotesListUiState(
            notes = notes,
            folders = folders,
            selectedFolderId = params.folderId,
            searchQuery = params.query,
            selectedTag = params.tag,
            isLoading = false,
            updateInfo = updateInfo,
            isShowingFolderSheet = isSheet
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
}