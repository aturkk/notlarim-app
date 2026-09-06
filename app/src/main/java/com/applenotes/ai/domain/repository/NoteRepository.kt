package com.applenotes.ai.domain.repository

import com.applenotes.ai.domain.model.Folder
import com.applenotes.ai.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNotesByFolder(folderId: Long): Flow<List<Note>>
    fun getDeletedNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun saveNote(note: Note): Long
    suspend fun moveToTrash(noteId: Long)
    suspend fun restoreFromTrash(noteId: Long)
    suspend fun deletePermanently(noteId: Long)
    suspend fun togglePin(noteId: Long)
    suspend fun toggleLock(noteId: Long)
    suspend fun moveToFolder(noteId: Long, folderId: Long?)
    suspend fun updateKanbanColumn(noteId: Long, column: String)
    suspend fun updateNoteIcon(noteId: Long, icon: String?)
    suspend fun updateNoteCover(noteId: Long, coverUrl: String?)
    suspend fun updateReminderTime(noteId: Long, reminderTime: Long?)
    suspend fun emptyTrash()

    fun getAllFolders(): Flow<List<Folder>>
    suspend fun createFolder(name: String, iconName: String = "folder"): Long
    suspend fun deleteFolder(folderId: Long)

    suspend fun saveNoteHistory(noteId: Long, title: String, content: String)
    fun getNoteHistory(noteId: Long): Flow<List<com.applenotes.ai.data.local.model.NoteHistoryEntity>>
}
