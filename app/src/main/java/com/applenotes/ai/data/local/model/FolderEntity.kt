package com.applenotes.ai.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.applenotes.ai.domain.model.Folder

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String = "folder",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(noteCount: Int = 0): Folder = Folder(
        id = id,
        name = name,
        iconName = iconName,
        noteCount = noteCount,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(folder: Folder): FolderEntity = FolderEntity(
            id = folder.id,
            name = folder.name,
            iconName = folder.iconName,
            createdAt = folder.createdAt
        )
    }
}
