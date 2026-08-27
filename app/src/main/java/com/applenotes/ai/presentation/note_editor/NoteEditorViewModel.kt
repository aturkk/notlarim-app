package com.applenotes.ai.presentation.note_editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.applenotes.ai.data.remote.ai.AiServiceManager
import com.applenotes.ai.domain.model.AiAction
import com.applenotes.ai.domain.model.ChatMessage
import com.applenotes.ai.domain.model.MessageRole
import com.applenotes.ai.domain.model.Note
import com.applenotes.ai.domain.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val noteId: Long = 0,
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isAiLoading: Boolean = false,
    val aiErrorMessage: String? = null,
    val isAiSheetVisible: Boolean = false,
    val isChatSheetVisible: Boolean = false,
    val chatMessages: List<ChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val shareText: String? = null
)

class NoteEditorViewModel(
    private val noteId: Long,
    private val repository: NoteRepository,
    private val aiServiceManager: AiServiceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState(noteId = noteId))
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        if (noteId > 0) {
            loadNote()
        }
    }

    private fun loadNote() {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                _uiState.update {
                    it.copy(
                        title = note.title,
                        content = note.content,
                        tags = note.tags,
                        isPinned = note.isPinned
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        scheduleAutoSave()
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
        scheduleAutoSave()
    }

    fun insertMarkdown(prefix: String, suffix: String = "") {
        val current = _uiState.value.content
        val updated = if (current.isEmpty()) {
            "$prefix$suffix"
        } else if (current.endsWith("\n")) {
            "$current$prefix$suffix"
        } else {
            "$current\n$prefix$suffix"
        }
        onContentChange(updated)
    }

    fun togglePin() {
        _uiState.update { it.copy(isPinned = !it.isPinned) }
        scheduleAutoSave()
    }

    fun setAiSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isAiSheetVisible = visible) }
    }

    fun setChatSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(isChatSheetVisible = visible) }
    }

    fun dismissError() {
        _uiState.update { it.copy(aiErrorMessage = null) }
    }

    fun executeAiAction(action: AiAction) {
        val content = _uiState.value.content
        if (content.isBlank()) {
            _uiState.update { it.copy(aiErrorMessage = "İşlem yapabilmek için lütfen önce bir şeyler yazın.") }
            return
        }

        when (action) {
            AiAction.FIX_GRAMMAR -> {
                fixGrammar()
                return
            }
            AiAction.TRANSLATE -> {
                translateNote("İngilizce")
                return
            }
            AiAction.CONTINUE_WRITING -> {
                continueWriting()
                return
            }
            AiAction.AUTO_TITLE_TAGS -> {
                _uiState.update { it.copy(isAiLoading = true, isAiSheetVisible = false, aiErrorMessage = null) }
                viewModelScope.launch {
                    val result = aiServiceManager.suggestTitleAndTags(content)
                    result.onSuccess { res ->
                        _uiState.update { current ->
                            current.copy(
                                title = if (current.title.isBlank()) res.title else current.title,
                                tags = (current.tags + res.tags).distinct(),
                                isAiLoading = false
                            )
                        }
                        saveNoteImmediately()
                    }.onFailure { err ->
                        _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = err.message) }
                    }
                }
            }
            else -> {
                _uiState.update { it.copy(isAiLoading = true, isAiSheetVisible = false, aiErrorMessage = null) }
                viewModelScope.launch {
                    val result = aiServiceManager.executeAction(action, content)
                    result.onSuccess { output ->
                        _uiState.update { current ->
                            val updatedContent = when (action) {
                                AiAction.SUMMARIZE -> "$output\n\n---\n$content"
                                AiAction.EXTRACT_ACTIONS -> "$content\n\n### Yapılacaklar Listesi\n$output"
                                AiAction.REWRITE_PROFESSIONAL,
                                AiAction.REWRITE_CASUAL,
                                AiAction.REWRITE_CONCISE -> output
                                else -> current.content
                            }
                            current.copy(
                                content = updatedContent,
                                isAiLoading = false
                            )
                        }
                        saveNoteImmediately()
                    }.onFailure { err ->
                        _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = err.message) }
                    }
                }
            }
        }
    }

    fun translateNote(targetLanguage: String = "İngilizce") {
        val content = _uiState.value.content
        if (content.isBlank()) {
            _uiState.update { it.copy(aiErrorMessage = "İşlem yapabilmek için lütfen önce bir şeyler yazın.") }
            return
        }

        _uiState.update { it.copy(isAiLoading = true, isAiSheetVisible = false, aiErrorMessage = null) }

        viewModelScope.launch {
            val result = aiServiceManager.translateNote(content, targetLanguage)
            result.onSuccess { output ->
                _uiState.update { current ->
                    current.copy(
                        content = "${current.content}\n\n---\n### Çeviri ($targetLanguage)\n$output",
                        isAiLoading = false
                    )
                }
                saveNoteImmediately()
            }.onFailure { err ->
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = err.message) }
            }
        }
    }

    fun fixGrammar() {
        val content = _uiState.value.content
        if (content.isBlank()) {
            _uiState.update { it.copy(aiErrorMessage = "İşlem yapabilmek için lütfen önce bir şeyler yazın.") }
            return
        }

        _uiState.update { it.copy(isAiLoading = true, isAiSheetVisible = false, aiErrorMessage = null) }

        viewModelScope.launch {
            val result = aiServiceManager.fixGrammar(content)
            result.onSuccess { output ->
                _uiState.update { current ->
                    current.copy(
                        content = output,
                        isAiLoading = false
                    )
                }
                saveNoteImmediately()
            }.onFailure { err ->
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = err.message) }
            }
        }
    }

    fun continueWriting() {
        val content = _uiState.value.content
        if (content.isBlank()) {
            _uiState.update { it.copy(aiErrorMessage = "İşlem yapabilmek için lütfen önce bir şeyler yazın.") }
            return
        }

        _uiState.update { it.copy(isAiLoading = true, isAiSheetVisible = false, aiErrorMessage = null) }

        viewModelScope.launch {
            val result = aiServiceManager.continueWriting(content)
            result.onSuccess { output ->
                _uiState.update { current ->
                    val separator = if (current.content.endsWith(" ") || current.content.endsWith("\n")) "" else " "
                    current.copy(
                        content = current.content + separator + output,
                        isAiLoading = false
                    )
                }
                saveNoteImmediately()
            }.onFailure { err ->
                _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = err.message) }
            }
        }
    }

    fun sendChatMessage(question: String) {
        if (question.isBlank()) return

        val userMsg = ChatMessage(role = MessageRole.USER, content = question.trim())
        val updatedHistory = _uiState.value.chatMessages + userMsg

        _uiState.update {
            it.copy(
                chatMessages = updatedHistory,
                isChatLoading = true
            )
        }

        viewModelScope.launch {
            val currentTitle = _uiState.value.title
            val currentContent = _uiState.value.content
            val combined = if (currentTitle.isNotBlank()) "$currentTitle\n\n$currentContent" else currentContent
            val result = aiServiceManager.chatWithNote(
                noteContent = combined,
                userQuestion = question,
                chatHistory = _uiState.value.chatMessages
            )

            result.onSuccess { answer ->
                val assistantMsg = ChatMessage(role = MessageRole.ASSISTANT, content = answer)
                _uiState.update {
                    it.copy(
                        chatMessages = it.chatMessages + assistantMsg,
                        isChatLoading = false
                    )
                }
            }.onFailure { err ->
                val errorMsg = ChatMessage(
                    role = MessageRole.ASSISTANT,
                    content = "Hata: ${err.message ?: "Bilinmeyen hata"}"
                )
                _uiState.update {
                    it.copy(
                        chatMessages = it.chatMessages + errorMsg,
                        isChatLoading = false
                    )
                }
            }
        }
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(500)
            saveNoteImmediately()
        }
    }

    private suspend fun saveNoteImmediately() {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) return

        val note = Note(
            id = state.noteId,
            title = state.title.trim(),
            content = state.content,
            tags = state.tags,
            isPinned = state.isPinned,
            updatedAt = System.currentTimeMillis()
        )
        val savedId = repository.saveNote(note)
        if (state.noteId == 0L && savedId > 0) {
            _uiState.update { it.copy(noteId = savedId) }
        }
    }

    fun deleteCurrentNote(onDeleted: () -> Unit) {
        viewModelScope.launch {
            if (_uiState.value.noteId > 0) {
                repository.moveToTrash(_uiState.value.noteId)
            }
            onDeleted()
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}
