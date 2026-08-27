package com.applenotes.ai.domain.model

data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val folderName: String? = null,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val summary: String? = null
)
