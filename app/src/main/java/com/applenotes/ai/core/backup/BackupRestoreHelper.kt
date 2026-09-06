package com.applenotes.ai.core.backup

import android.content.Context
import android.net.Uri
import com.applenotes.ai.domain.model.Note
import com.applenotes.ai.domain.repository.NoteRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Serializable
data class NoteBackupModel(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val folderId: Long? = null,
    val kanbanColumn: String? = null,
    val reminderTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val icon: String? = null,
    val coverUrl: String? = null
)

object BackupRestoreHelper {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * Creates a ZIP file containing manifest.json and Markdown files
     */
    fun createBackupZip(context: Context, notes: List<Note>): File {
        val backupDir = File(context.cacheDir, "backups").apply { if (!exists()) mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val zipFile = File(backupDir, "AppleNotes_Yedek_$timeStamp.zip")

        FileOutputStream(zipFile).use { fos ->
            writeNotesToZipStream(fos, notes)
        }

        return zipFile
    }

    /**
     * Exports backup directly to a SAF URI (e.g. Google Drive, external storage)
     */
    fun exportBackupToUri(context: Context, targetUri: Uri, notes: List<Note>): Result<Int> {
        return try {
            val outputStream = context.contentResolver.openOutputStream(targetUri)
                ?: return Result.failure(IOException("Dosya yazılamadı (SAF Uri açılamadı)"))

            outputStream.use { os ->
                writeNotesToZipStream(os, notes)
            }
            Result.success(notes.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun writeNotesToZipStream(outputStream: OutputStream, notes: List<Note>) {
        val zos = ZipOutputStream(outputStream)

        // 1. Write JSON Manifest
        val backupModels = notes.map { note ->
            NoteBackupModel(
                title = note.title,
                content = note.content,
                tags = note.tags,
                isPinned = note.isPinned,
                isLocked = note.isLocked,
                folderId = note.folderId,
                kanbanColumn = note.kanbanColumn,
                reminderTime = note.reminderTime,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                icon = note.icon,
                coverUrl = note.coverUrl
            )
        }
        val manifestJson = json.encodeToString(backupModels)
        zos.putNextEntry(ZipEntry("manifest.json"))
        zos.write(manifestJson.toByteArray())
        zos.closeEntry()

        // 2. Write Markdown directory
        notes.forEachIndexed { index, note ->
            val safeTitle = note.title
                .ifBlank { "Not_${note.id}" }
                .replace(Regex("[^a-zA-Z0-9çğıöşüÇĞİÖŞÜ_ -]"), "")
                .take(40)
                .trim()
            val fileName = "Markdown/${index + 1}_${safeTitle}.md"

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(note.updatedAt))
            val mdContent = buildString {
                appendLine("---")
                appendLine("title: \"${note.title.replace("\"", "\\\"")}\"")
                appendLine("date: $dateStr")
                if (note.tags.isNotEmpty()) {
                    appendLine("tags: [${note.tags.joinToString(", ") { "\"$it\"" }}]")
                }
                appendLine("pinned: ${note.isPinned}")
                appendLine("---")
                appendLine()
                appendLine(note.content)
            }

            zos.putNextEntry(ZipEntry(fileName))
            zos.write(mdContent.toByteArray())
            zos.closeEntry()
        }

        zos.finish()
    }

    /**
     * Restores notes from a SAF URI (ZIP archive or JSON file)
     */
    suspend fun restoreBackupFromUri(
        context: Context,
        sourceUri: Uri,
        repository: NoteRepository
    ): Result<Int> {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(IOException("Dosya okunamadı (SAF Uri açılamadı)"))

            val bytes = inputStream.use { it.readBytes() }
            var restoredCount = 0

            // Check if it is a ZIP archive
            if (isZipArchive(bytes)) {
                val zis = ZipInputStream(ByteArrayInputStream(bytes))
                var manifestBytes: ByteArray? = null
                val markdownNotes = mutableListOf<Note>()

                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        if (entry.name.endsWith("manifest.json") || entry.name == "notes_backup.json") {
                            manifestBytes = zis.readBytes()
                        } else if (entry.name.endsWith(".md")) {
                            val content = zis.readBytes().toString(Charsets.UTF_8)
                            val note = parseMarkdownNote(entry.name, content)
                            markdownNotes.add(note)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }

                if (manifestBytes != null) {
                    val manifestStr = manifestBytes.toString(Charsets.UTF_8)
                    try {
                        val models = json.decodeFromString<List<NoteBackupModel>>(manifestStr)
                        models.forEach { model ->
                            repository.saveNote(
                                Note(
                                    title = model.title,
                                    content = model.content,
                                    tags = model.tags,
                                    isPinned = model.isPinned,
                                    isLocked = model.isLocked,
                                    folderId = model.folderId,
                                    kanbanColumn = model.kanbanColumn,
                                    reminderTime = model.reminderTime,
                                    createdAt = model.createdAt,
                                    updatedAt = model.updatedAt,
                                    icon = model.icon,
                                    coverUrl = model.coverUrl
                                )
                            )
                            restoredCount++
                        }
                    } catch (_: Exception) {
                        // If model deserialization failed, fallback to Markdown notes if any
                        markdownNotes.forEach { note ->
                            repository.saveNote(note)
                            restoredCount++
                        }
                    }
                } else {
                    markdownNotes.forEach { note ->
                        repository.saveNote(note)
                        restoredCount++
                    }
                }
            } else {
                // Direct JSON file
                val contentStr = bytes.toString(Charsets.UTF_8)
                val models = json.decodeFromString<List<NoteBackupModel>>(contentStr)
                models.forEach { model ->
                    repository.saveNote(
                        Note(
                            title = model.title,
                            content = model.content,
                            tags = model.tags,
                            isPinned = model.isPinned,
                            isLocked = model.isLocked,
                            createdAt = model.createdAt,
                            updatedAt = model.updatedAt
                        )
                    )
                    restoredCount++
                }
            }

            Result.success(restoredCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isZipArchive(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        return bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()
    }

    private fun parseMarkdownNote(filePath: String, content: String): Note {
        var title = File(filePath).nameWithoutExtension
        if (title.contains("_")) {
            val parts = title.split("_", limit = 2)
            if (parts[0].toIntOrNull() != null && parts.size > 1) {
                title = parts[1]
            }
        }

        var body = content
        val tags = mutableListOf<String>()

        // Check for YAML front matter
        if (content.startsWith("---")) {
            val endIdx = content.indexOf("---", 3)
            if (endIdx != -1) {
                val header = content.substring(3, endIdx)
                body = content.substring(endIdx + 3).trim()
                header.lines().forEach { line ->
                    if (line.startsWith("title:")) {
                        title = line.removePrefix("title:").trim().trim('"')
                    } else if (line.startsWith("tags:")) {
                        val tagsRaw = line.removePrefix("tags:").trim().removePrefix("[").removeSuffix("]")
                        tagsRaw.split(",").forEach { t ->
                            val cleanTag = t.trim().trim('"')
                            if (cleanTag.isNotBlank()) tags.add(cleanTag)
                        }
                    }
                }
            }
        }

        return Note(
            title = title,
            content = body,
            tags = tags,
            updatedAt = System.currentTimeMillis()
        )
    }
}
