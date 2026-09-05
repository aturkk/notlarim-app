package com.applenotes.ai.data.repository

import com.applenotes.ai.data.local.AppDatabase
import com.applenotes.ai.data.local.model.FolderEntity
import com.applenotes.ai.data.local.model.NoteEntity
import com.applenotes.ai.domain.model.Folder
import com.applenotes.ai.domain.model.Note
import com.applenotes.ai.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val db: AppDatabase
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return db.noteDao.getAllNotes().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getNotesByFolder(folderId: Long): Flow<List<Note>> {
        return db.noteDao.getNotesByFolder(folderId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getDeletedNotes(): Flow<List<Note>> {
        return db.noteDao.getDeletedNotes().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return db.noteDao.getNoteById(id)?.toDomain()
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return db.noteDao.searchNotes(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveNote(note: Note): Long {
        val entity = NoteEntity.fromDomain(note.copy(updatedAt = System.currentTimeMillis()))
        return if (note.id == 0L) {
            db.noteDao.insertNote(entity)
        } else {
            db.noteDao.updateNote(entity)
            note.id
        }
    }

    override suspend fun moveToTrash(noteId: Long) {
        db.noteDao.moveToTrash(noteId)
    }

    override suspend fun restoreFromTrash(noteId: Long) {
        db.noteDao.restoreFromTrash(noteId)
    }

    override suspend fun deletePermanently(noteId: Long) {
        val note = db.noteDao.getNoteById(noteId)
        if (note != null) {
            db.noteDao.deleteNotePermanently(note)
        }
    }

    override suspend fun togglePin(noteId: Long) {
        db.noteDao.togglePin(noteId)
    }

    override suspend fun toggleLock(noteId: Long) {
        db.noteDao.toggleLock(noteId)
    }

    override suspend fun moveToFolder(noteId: Long, folderId: Long?) {
        db.noteDao.moveToFolder(noteId, folderId)
    }

    override suspend fun emptyTrash() {
        db.noteDao.emptyTrash()
    }

    override fun getAllFolders(): Flow<List<Folder>> {
        return db.folderDao.getAllFolders().map { list ->
            list.map { entity ->
                val count = db.folderDao.getNoteCountForFolder(entity.id)
                entity.toDomain(noteCount = count)
            }
        }
    }

    override suspend fun createFolder(name: String, iconName: String): Long {
        return db.folderDao.insertFolder(FolderEntity(name = name, iconName = iconName))
    }

    override suspend fun deleteFolder(folderId: Long) {
        db.folderDao.deleteFolder(FolderEntity(id = folderId, name = ""))
    }
}
