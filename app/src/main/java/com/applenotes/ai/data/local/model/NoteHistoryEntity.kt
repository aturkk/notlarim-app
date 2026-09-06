package com.applenotes.ai.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_history",
    indices = [Index(value = ["noteId"]), Index(value = ["timestamp"])]
)
data class NoteHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
