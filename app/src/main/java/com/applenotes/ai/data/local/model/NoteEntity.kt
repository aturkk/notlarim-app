package com.applenotes.ai.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.applenotes.ai.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val isLocked: Boolean = false,
    val drawingPath: String? = null,
    val audioPath: String? = null,
    val reminderTime: Long? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val summary: String? = null,
    val icon: String? = null,
    val coverUrl: String? = null,
    val kanbanColumn: String? = null,
    val priority: String? = null,
    val status: String? = null,
    val progress: Int? = null
) {
    fun toDomain(folderName: String? = null): Note = Note(
        id = id,
        title = title,
        content = content,
        folderId = folderId,
        folderName = folderName,
        isPinned = isPinned,
        isDeleted = isDeleted,
        isLocked = isLocked,
        drawingPath = drawingPath,
        audioPath = audioPath,
        reminderTime = reminderTime,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        summary = summary,
        icon = icon,
        coverUrl = coverUrl,
        kanbanColumn = kanbanColumn,
        priority = priority,
        status = status,
        progress = progress
    )

    companion object {
        fun fromDomain(note: Note): NoteEntity = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            folderId = note.folderId,
            isPinned = note.isPinned,
            isDeleted = note.isDeleted,
            isLocked = note.isLocked,
            drawingPath = note.drawingPath,
            audioPath = note.audioPath,
            reminderTime = note.reminderTime,
            tags = note.tags,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            summary = note.summary,
            icon = note.icon,
            coverUrl = note.coverUrl,
            kanbanColumn = note.kanbanColumn,
            priority = note.priority,
            status = note.status,
            progress = note.progress
        )
    }
}
