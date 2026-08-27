package com.applenotes.ai.domain.model

data class Folder(
    val id: Long = 0,
    val name: String,
    val iconName: String = "folder",
    val noteCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
