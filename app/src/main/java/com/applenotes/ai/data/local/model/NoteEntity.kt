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
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val summary: String? = null
) {
    fun toDomain(folderName: String? = null): Note = Note(
        id = id,
        title = title,
        content = content,
        folderId = folderId,
        folderName = folderName,
        isPinned = isPinned,
        isDeleted = isDeleted,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        summary = summary
    )

    companion object {
        fun fromDomain(note: Note): NoteEntity = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            folderId = note.folderId,
            isPinned = note.isPinned,
            isDeleted = note.isDeleted,
            tags = note.tags,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            summary = note.summary
        )
    }
}
