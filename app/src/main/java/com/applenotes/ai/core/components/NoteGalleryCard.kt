package com.applenotes.ai.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.Note
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteGalleryCard(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val cardBg = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    val dateFormatter = SimpleDateFormat("d MMM", Locale("tr"))
    val dateString = dateFormatter.format(Date(note.updatedAt))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        tonalElevation = 1.dp,
        shadowElevation = if (isSelected) 6.dp else 2.dp,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AppleYellow) else null,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: Selection or Pin/Lock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AppleYellow else Color.Transparent)
                            .then(
                                if (!isSelected) Modifier.background(
                                    Color.Gray.copy(alpha = 0.3f),
                                    CircleShape
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (note.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Sabitlendi",
                                tint = AppleYellow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (note.isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Kilitli",
                                tint = AppleYellow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = textSecondary
                )
            }

            // Drawing Thumbnail if present
            note.drawingPath?.let { path ->
                val drawingFile = File(path)
                if (drawingFile.exists()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = drawingFile,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            } ?: run {
                // Cover thumbnail if present and no drawing
                note.coverUrl?.let { cover ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = cover,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            val prefix = note.icon?.let { "$it " } ?: ""
            Text(
                text = if (note.isLocked) "🔒 Kilitli Not" else "$prefix${note.title.ifBlank { "Başlıksız Not" }}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Snippet
            if (!note.isLocked && note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                // Clean markdown symbols for thumbnail preview
                val cleanSnippet = note.content
                    .replace(Regex("""[#*_~`>-]"""), "")
                    .lines()
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .trim()

                Text(
                    text = cleanSnippet,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Badges row: Priority, Status, Progress, Tags
            if (note.tags.isNotEmpty() || note.priority != null || note.progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    note.priority?.let { p ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (p) {
                                "HIGH" -> iOSRed.copy(alpha = 0.18f)
                                "MEDIUM" -> AppleYellow.copy(alpha = 0.20f)
                                else -> Color(0xFF34C759).copy(alpha = 0.18f)
                            }
                        ) {
                            Text(
                                text = when (p) {
                                    "HIGH" -> "Acil"
                                    "MEDIUM" -> "Normal"
                                    else -> "Düşük"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (p) {
                                    "HIGH" -> iOSRed
                                    "MEDIUM" -> AppleYellowDark
                                    else -> Color(0xFF248A3D)
                                },
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    note.progress?.let { pr ->
                        Text(
                            text = "%$pr",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleYellow
                        )
                    }

                    note.tags.take(1).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppleYellow.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = AppleYellowDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
