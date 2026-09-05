package com.applenotes.ai.core.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.applenotes.ai.domain.model.Note
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object NoteExporter {

    fun exportToPdf(context: Context, note: Note): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = AndroidColor.parseColor("#1C1C1E")
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = AndroidColor.parseColor("#3A3A3C")
            textSize = 12f
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = AndroidColor.parseColor("#8E8E93")
            textSize = 10f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = AndroidColor.parseColor("#E5E5EA")
            strokeWidth = 1f
            isAntiAlias = true
        }

        var y = 60f
        val noteTitle = if (note.title.isNotBlank()) note.title else "Başlıksız Not"
        canvas.drawText(noteTitle, 50f, y, titlePaint)
        y += 24f

        val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.updatedAt))
        canvas.drawText("Apple Notes AI · $dateStr", 50f, y, metaPaint)
        y += 20f

        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 24f

        val cleanContent = note.content
            .replace(Regex("""\*\*(.*?)\*\*"""), "$1")
            .replace(Regex("""\*(.*?)\*"""), "$1")
            .replace(Regex("""~~(.*?)~~"""), "$1")

        val lines = cleanContent.lines()
        for (line in lines) {
            val words = line.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
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
        val file = File(dir, "Not_${note.id}_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        document.close()
        fos.flush()
        fos.close()

        return file
    }

    /**
     * Exports the note as a high-resolution aesthetic social/card image (PNG)
     */
    fun exportToImageCard(context: Context, note: Note): File {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(AndroidColor.parseColor("#F2F2F7"))

        // Card Container
        val cardPaint = Paint().apply {
            color = AndroidColor.WHITE
            isAntiAlias = true
            setShadowLayer(25f, 0f, 15f, AndroidColor.argb(40, 0, 0, 0))
        }
        val cardRect = RectF(60f, 80f, (width - 60).toFloat(), (height - 80).toFloat())
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        // Accent Header Pill
        val pillPaint = Paint().apply {
            color = AndroidColor.parseColor("#FFF4D6")
            isAntiAlias = true
        }
        val pillTextPaint = Paint().apply {
            color = AndroidColor.parseColor("#D49700")
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val pillRect = RectF(110f, 140f, 370f, 200f)
        canvas.drawRoundRect(pillRect, 30f, 30f, pillPaint)
        canvas.drawText("Apple Notes AI", 135f, 182f, pillTextPaint)

        // Title
        val titlePaint = Paint().apply {
            color = AndroidColor.parseColor("#1C1C1E")
            textSize = 48f
            isFakeBoldText = true
            isAntiAlias = true
        }
        var y = 270f
        val noteTitle = if (note.title.isNotBlank()) note.title else "Başlıksız Not"
        canvas.drawText(noteTitle, 110f, y, titlePaint)
        y += 40f

        // Date & Meta
        val dateStr = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("tr")).format(Date(note.updatedAt))
        val metaPaint = Paint().apply {
            color = AndroidColor.parseColor("#8E8E93")
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText(dateStr, 110f, y, metaPaint)
        y += 35f

        // Divider
        val linePaint = Paint().apply {
            color = AndroidColor.parseColor("#E5E5EA")
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawLine(110f, y, (width - 110).toFloat(), y, linePaint)
        y += 45f

        // Content
        val bodyPaint = Paint().apply {
            color = AndroidColor.parseColor("#2C2C2E")
            textSize = 30f
            isAntiAlias = true
        }

        val cleanContent = note.content
            .replace(Regex("""\*\*(.*?)\*\*"""), "$1")
            .replace(Regex("""\*(.*?)\*"""), "$1")
            .replace(Regex("""~~(.*?)~~"""), "$1")

        val maxTextWidth = (width - 220).toFloat()
        val lines = cleanContent.lines()
        for (line in lines) {
            val words = line.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (bodyPaint.measureText(testLine) < maxTextWidth) {
                    currentLine = testLine
                } else {
                    canvas.drawText(currentLine, 110f, y, bodyPaint)
                    y += 42f
                    currentLine = word
                    if (y > height - 160f) break
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, 110f, y, bodyPaint)
                y += 42f
            }
            if (y > height - 160f) break
        }

        // Footer Watermark
        val footerPaint = Paint().apply {
            color = AndroidColor.parseColor("#AEAEB2")
            textSize = 22f
            isAntiAlias = true
        }
        canvas.drawText("Bu not Apple Notes AI ile oluşturuldu", 110f, height - 120f, footerPaint)

        val dir = File(context.cacheDir, "image_exports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "Not_Kart_${note.id}_${System.currentTimeMillis()}.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String) {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Paylaş").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
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
        val zipFile = File(backupDir, "AppleNotes_Yedek_${System.currentTimeMillis()}.zip")

        val zos = ZipOutputStream(FileOutputStream(zipFile))
        val entry = ZipEntry("notes_backup.json")
        zos.putNextEntry(entry)
        zos.write(notesJson.toByteArray())
        zos.closeEntry()
        zos.close()

        return zipFile
    }
}