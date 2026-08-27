package com.applenotes.ai.core.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.applenotes.ai.domain.model.Note
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object NoteExporter {

    fun exportToPdf(context: Context, note: Note): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = AndroidColor.DKGRAY
            textSize = 12f
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = AndroidColor.GRAY
            textSize = 10f
            isAntiAlias = true
        }

        var y = 60f
        canvas.drawText(if (note.title.isNotBlank()) note.title else "Başlıksız Not", 50f, y, titlePaint)
        y += 24f

        val dateStr = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(note.updatedAt))
        canvas.drawText("Apple Notes AI · ", 50f, y, metaPaint)
        y += 30f

        canvas.drawLine(50f, y, 545f, y, metaPaint)
        y += 20f

        val lines = note.content.lines()
        for (line in lines) {
            val words = line.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else " "
                if (bodyPaint.measureText(testLine) < 495f) {
                    currentLine = testLine
                } else {
                    canvas.drawText(currentLine, 50f, y, bodyPaint)
                    y += 18f
                    currentLine = word
                    if (y > 800f) break
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, 50f, y, bodyPaint)
                y += 18f
            }
            if (y > 800f) break
        }

        document.finishPage(page)

        val dir = File(context.cacheDir, "pdf_exports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "Not_.pdf")
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        document.close()
        fos.flush()
        fos.close()

        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(context, ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Dışa Aktar / Paylaş"))
    }

    fun createBackupZip(context: Context, allNotes: List<Note>): File {
        val json = Json { prettyPrint = true }
        val notesJson = json.encodeToString(allNotes.map {
            mapOf(
                "title" to it.title,
                "content" to it.content,
                "tags" to it.tags.joinToString(","),
                "isPinned" to it.isPinned.toString(),
                "isLocked" to it.isLocked.toString(),
                "createdAt" to it.createdAt.toString(),
                "updatedAt" to it.updatedAt.toString()
            )
        })

        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()
        val zipFile = File(backupDir, "AppleNotes_Yedek_.zip")

        val zos = ZipOutputStream(FileOutputStream(zipFile))
        val entry = ZipEntry("notes_backup.json")
        zos.putNextEntry(entry)
        zos.write(notesJson.toByteArray())
        zos.closeEntry()
        zos.close()

        return zipFile
    }
}