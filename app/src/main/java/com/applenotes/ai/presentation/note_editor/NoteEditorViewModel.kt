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

import com.applenotes.ai.core.components.SlashCommand
import com.applenotes.ai.core.templates.NoteTemplate

data class NoteEditorUiState(
    val noteId: Long = 0,
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val drawingPath: String? = null,
    val audioPath: String? = null,
    val reminderTime: Long? = null,
    val isAiLoading: Boolean = false,
    val aiErrorMessage: String? = null,
    val isAiSheetVisible: Boolean = false,
    val isChatSheetVisible: Boolean = false,
    val isDrawingDialogOpen: Boolean = false,
    val flashcardsResult: String? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val shareText: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isFormatBarVisible: Boolean = false,
    val isRecordingAudio: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val isTranscribingAudio: Boolean = false,
    val isOcrLoading: Boolean = false,
    val icon: String? = null,
    val coverUrl: String? = null,
    val kanbanColumn: String? = null,
    val backlinks: List<Note> = emptyList(),
    val isSlashMenuVisible: Boolean = false,
    val isIconPickerVisible: Boolean = false,
    val isCoverPickerVisible: Boolean = false,
    val isTemplatePickerVisible: Boolean = false,
    val historyList: List<com.applenotes.ai.data.local.model.NoteHistoryEntity> = emptyList(),
    val isVersionHistoryVisible: Boolean = false,
    val isZenModeOpen: Boolean = false,
    val isPomodoroOpen: Boolean = false
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
                        isPinned = note.isPinned,
                        isLocked = note.isLocked,
                        drawingPath = note.drawingPath,
                        audioPath = note.audioPath,
                        reminderTime = note.reminderTime,
                        icon = note.icon,
                        coverUrl = note.coverUrl,
                        kanbanColumn = note.kanbanColumn
                    )
                }
                loadBacklinks(note.title)
                loadVersionHistory(note.id)
            }
        }
    }

    private fun loadVersionHistory(noteId: Long) {
        if (noteId <= 0) return
        viewModelScope.launch {
            repository.getNoteHistory(noteId).collect { history ->
                _uiState.update { it.copy(historyList = history) }
            }
        }
    }

    private fun loadBacklinks(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.getAllNotes().collect { allNotes ->
                val target = "[[${title.trim()}]]"
                val links = allNotes.filter {
                    it.id != _uiState.value.noteId &&
                    it.content.contains(target, ignoreCase = true)
                }
                _uiState.update { it.copy(backlinks = links) }
            }
        }
    }

    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    private var lastSnapshotTime: Long = 0L

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        loadBacklinks(newTitle)
        scheduleAutoSave()
    }

    fun onContentChange(newContent: String, saveToHistory: Boolean = true) {
        val current = _uiState.value.content
        if (saveToHistory && newContent != current) {
            val now = System.currentTimeMillis()
            // Group typing changes within 1 second or push distinct snapshots
            if (now - lastSnapshotTime > 800L || undoStack.isEmpty()) {
                if (undoStack.size >= 60) {
                    undoStack.removeFirst()
                }
                undoStack.addLast(current)
                lastSnapshotTime = now
            }
            redoStack.clear()
        }

        val shouldTriggerSlash = newContent.endsWith("/") && (newContent.length == 1 || newContent.endsWith("\n/") || newContent.endsWith(" /"))

        _uiState.update {
            it.copy(
                content = newContent,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                isSlashMenuVisible = if (shouldTriggerSlash) true else it.isSlashMenuVisible
            )
        }
        scheduleAutoSave()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val current = _uiState.value.content
            val previous = undoStack.removeLast()
            redoStack.addLast(current)
            _uiState.update {
                it.copy(
                    content = previous,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
            scheduleAutoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val current = _uiState.value.content
            val next = redoStack.removeLast()
            undoStack.addLast(current)
            _uiState.update {
                it.copy(
                    content = next,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
            scheduleAutoSave()
        }
    }

    fun toggleFormatBar() {
        _uiState.update { it.copy(isFormatBarVisible = !it.isFormatBarVisible) }
    }

    fun toggleChecklistLine(lineIndex: Int) {
        val lines = _uiState.value.content.lines().toMutableList()
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            val updatedLine = when {
                line.trimStart().startsWith("- [ ]") -> line.replaceFirst("- [ ]", "- [x]")
                line.trimStart().startsWith("- [x]") -> line.replaceFirst("- [x]", "- [ ]")
                line.trimStart().startsWith("- [X]") -> line.replaceFirst("- [X]", "- [ ]")
                else -> line
            }
            if (updatedLine != line) {
                lines[lineIndex] = updatedLine
                onContentChange(lines.joinToString("\n"))
            }
        }
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

    fun applyHeader(level: Int) {
        val prefix = "#".repeat(level) + " "
        insertMarkdown(prefix)
    }

    fun applyChecklist() {
        insertMarkdown("- [ ] ")
    }

    fun applyBulletList() {
        insertMarkdown("• ")
    }

    fun applyNumberedList() {
        insertMarkdown("1. ")
    }

    fun applyQuote() {
        insertMarkdown("> ")
    }

    fun applyCodeBlock() {
        insertMarkdown("```\n", "\n```")
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
            AiAction.FLASHCARDS,
            AiAction.MINDMAP -> {
                _uiState.update { it.copy(isAiLoading = true, isAiSheetVisible = false, aiErrorMessage = null) }
                viewModelScope.launch {
                    val result = aiServiceManager.executeAction(action, content)
                    result.onSuccess { output ->
                        _uiState.update { it.copy(isAiLoading = false, flashcardsResult = output) }
                    }.onFailure { err ->
                        _uiState.update { it.copy(isAiLoading = false, aiErrorMessage = err.message) }
                    }
                }
            }
            AiAction.EXTRACT_REMINDERS -> {
                _uiState.update { it.copy(isAiLoading = true, isAiSheetVisible = false, aiErrorMessage = null) }
                viewModelScope.launch {
                    val result = aiServiceManager.executeAction(action, content)
                    result.onSuccess { output ->
                        _uiState.update { current ->
                            current.copy(
                                content = "${current.content}\n\n---\n### ⏰ Hatırlatıcılar & Randevular\n$output",
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

    fun toggleLock() {
        val newLocked = !_uiState.value.isLocked
        _uiState.update { it.copy(isLocked = newLocked) }
        viewModelScope.launch { saveNoteImmediately() }
    }

    fun setDrawingDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isDrawingDialogOpen = open) }
    }

    fun saveDrawing(path: String) {
        _uiState.update { it.copy(drawingPath = path, isDrawingDialogOpen = false) }
        viewModelScope.launch { saveNoteImmediately() }
    }

    fun deleteDrawing() {
        _uiState.update { it.copy(drawingPath = null) }
        viewModelScope.launch { saveNoteImmediately() }
    }

    fun setAudioPath(path: String?) {
        _uiState.update { it.copy(audioPath = path) }
        viewModelScope.launch { saveNoteImmediately() }
    }

    fun dismissFlashcardsDialog() {
        _uiState.update { it.copy(flashcardsResult = null) }
    }

    fun exportToPdf(context: android.content.Context) {
        val currentNote = Note(
            id = _uiState.value.noteId,
            title = _uiState.value.title,
            content = _uiState.value.content,
            tags = _uiState.value.tags,
            updatedAt = System.currentTimeMillis()
        )
        val file = com.applenotes.ai.core.export.NoteExporter.exportToPdf(context, currentNote)
        com.applenotes.ai.core.export.NoteExporter.shareFile(context, file, "application/pdf")
    }

    fun exportToImageCard(context: android.content.Context) {
        val currentNote = Note(
            id = _uiState.value.noteId,
            title = _uiState.value.title,
            content = _uiState.value.content,
            tags = _uiState.value.tags,
            updatedAt = System.currentTimeMillis()
        )
        val file = com.applenotes.ai.core.export.NoteExporter.exportToImageCard(context, currentNote)
        com.applenotes.ai.core.export.NoteExporter.shareFile(context, file, "image/png")
    }

    fun setAudioRecording(recording: Boolean) {
        _uiState.update { it.copy(isRecordingAudio = recording) }
    }

    fun setAudioPlaying(playing: Boolean) {
        _uiState.update { it.copy(isPlayingAudio = playing) }
    }

    fun transcribeAudioFile(audioFilePath: String) {
        val file = java.io.File(audioFilePath)
        if (!file.exists()) return

        _uiState.update { it.copy(isTranscribingAudio = true, aiErrorMessage = null) }
        viewModelScope.launch {
            val result = aiServiceManager.transcribeAudio(file)
            result.onSuccess { transcript ->
                val current = _uiState.value.content
                val updated = if (current.isNotBlank()) {
                    "$current\n\n### 🎙️ Ses Kaydı Transkripti\n$transcript"
                } else {
                    "### 🎙️ Ses Kaydı Transkripti\n$transcript"
                }
                onContentChange(updated)
                _uiState.update { it.copy(isTranscribingAudio = false) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isTranscribingAudio = false,
                        aiErrorMessage = "Transkripsiyon Hatası: ${err.message}"
                    )
                }
            }
        }
    }

    fun processImageOcr(imageBytes: ByteArray) {
        _uiState.update { it.copy(isOcrLoading = true, aiErrorMessage = null) }
        viewModelScope.launch {
            val result = aiServiceManager.extractTextFromImage(imageBytes)
            result.onSuccess { text ->
                val current = _uiState.value.content
                val updated = if (current.isNotBlank()) {
                    "$current\n\n### 📄 Taranan Belge Metni\n$text"
                } else {
                    "### 📄 Taranan Belge Metni\n$text"
                }
                onContentChange(updated)
                _uiState.update { it.copy(isOcrLoading = false) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isOcrLoading = false,
                        aiErrorMessage = "Belge Okuma Hatası: ${err.message}"
                    )
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

    fun setIcon(icon: String?) {
        _uiState.update { it.copy(icon = icon, isIconPickerVisible = false) }
        scheduleAutoSave()
    }

    fun setCoverUrl(coverUrl: String?) {
        _uiState.update { it.copy(coverUrl = coverUrl, isCoverPickerVisible = false) }
        scheduleAutoSave()
    }

    fun setKanbanColumn(column: String?) {
        _uiState.update { it.copy(kanbanColumn = column) }
        scheduleAutoSave()
    }

    fun setSlashMenuVisible(visible: Boolean) {
        _uiState.update { it.copy(isSlashMenuVisible = visible) }
    }

    fun setIconPickerVisible(visible: Boolean) {
        _uiState.update { it.copy(isIconPickerVisible = visible) }
    }

    fun setCoverPickerVisible(visible: Boolean) {
        _uiState.update { it.copy(isCoverPickerVisible = visible) }
    }

    fun setTemplatePickerVisible(visible: Boolean) {
        _uiState.update { it.copy(isTemplatePickerVisible = visible) }
    }

    fun applyTemplate(template: NoteTemplate) {
        _uiState.update { current ->
            val updatedTitle = if (current.title.isBlank()) template.title else current.title
            val updatedContent = if (current.content.isBlank()) template.content else "${current.content}\n\n${template.content}"
            val mergedTags = (current.tags + template.defaultTags).distinct()
            current.copy(
                title = updatedTitle,
                content = updatedContent,
                tags = mergedTags,
                icon = current.icon ?: template.icon,
                coverUrl = current.coverUrl ?: template.coverUrl,
                isTemplatePickerVisible = false
            )
        }
        scheduleAutoSave()
    }

    fun insertSlashCommand(command: SlashCommand) {
        setSlashMenuVisible(false)
        val current = _uiState.value.content
        val baseContent = if (current.endsWith("/")) current.dropLast(1) else current
        val snippet = command.snippet
        val updated = if (baseContent.isEmpty()) {
            snippet
        } else if (baseContent.endsWith("\n")) {
            "$baseContent$snippet"
        } else {
            "$baseContent\n$snippet"
        }
        onContentChange(updated)
    }

    fun insertBacklink(targetNoteTitle: String) {
        insertMarkdown("[[$targetNoteTitle]] ")
    }

    fun setVersionHistoryVisible(visible: Boolean) {
        _uiState.update { it.copy(isVersionHistoryVisible = visible) }
    }

    fun setZenModeOpen(open: Boolean) {
        _uiState.update { it.copy(isZenModeOpen = open) }
    }

    fun setPomodoroOpen(open: Boolean) {
        _uiState.update { it.copy(isPomodoroOpen = open) }
    }

    fun restoreVersion(version: com.applenotes.ai.data.local.model.NoteHistoryEntity) {
        _uiState.update {
            it.copy(
                title = version.title,
                content = version.content,
                isVersionHistoryVisible = false
            )
        }
        onContentChange(version.content)
    }

    private var lastHistorySaveTime: Long = 0L

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
            isLocked = state.isLocked,
            drawingPath = state.drawingPath,
            audioPath = state.audioPath,
            reminderTime = state.reminderTime,
            icon = state.icon,
            coverUrl = state.coverUrl,
            kanbanColumn = state.kanbanColumn,
            updatedAt = System.currentTimeMillis()
        )
        val savedId = repository.saveNote(note)
        val activeNoteId = if (state.noteId == 0L) savedId else state.noteId
        if (state.noteId == 0L && savedId > 0) {
            _uiState.update { it.copy(noteId = savedId) }
            loadBacklinks(state.title)
            loadVersionHistory(savedId)
        }

        // Save history snapshot if 1 minute elapsed since last snapshot
        val now = System.currentTimeMillis()
        if (activeNoteId > 0 && (now - lastHistorySaveTime > 60_000L || lastHistorySaveTime == 0L)) {
            repository.saveNoteHistory(activeNoteId, state.title, state.content)
            lastHistorySaveTime = now
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
