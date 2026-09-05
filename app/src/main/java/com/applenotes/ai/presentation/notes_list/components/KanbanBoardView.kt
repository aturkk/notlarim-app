package com.applenotes.ai.presentation.notes_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.Note

enum class KanbanColumnType(val id: String, val title: String, val emoji: String) {
    TODO("TODO", "Yapılacaklar", "📋"),
    IN_PROGRESS("IN_PROGRESS", "Devam Edenler", "⚡"),
    DONE("DONE", "Tamamlandı", "✅")
}

@Composable
fun KanbanBoardView(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onMoveNote: (noteId: Long, targetColumn: String) -> Unit,
    onAddCard: (column: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val columns = KanbanColumnType.entries

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(columns) { col ->
            val columnNotes = remember(notes, col.id) {
                notes.filter { note ->
                    val c = note.kanbanColumn ?: "TODO"
                    c == col.id
                }
            }

            KanbanColumnCard(
                column = col,
                notes = columnNotes,
                onNoteClick = onNoteClick,
                onMoveNote = onMoveNote,
                onAddCard = { onAddCard(col.id) }
            )
        }
    }
}

@Composable
private fun KanbanColumnCard(
    column: KanbanColumnType,
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onMoveNote: (noteId: Long, targetColumn: String) -> Unit,
    onAddCard: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) iOSCardBackgroundDark else Color(0xFFF2F2F7)
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    val accentColor = when (column) {
        KanbanColumnType.TODO -> Color(0xFF007AFF)
        KanbanColumnType.IN_PROGRESS -> AppleYellow
        KanbanColumnType.DONE -> Color(0xFF34C759)
    }

    Surface(
        modifier = Modifier
            .width(290.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(18.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Column Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = column.emoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = column.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Count Badge
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${notes.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onAddCard,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Kart Ekle",
                        tint = AppleYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cards list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    KanbanItemCard(
                        note = note,
                        currentColumn = column,
                        onClick = { onNoteClick(note) },
                        onMovePrevious = {
                            val prev = when (column) {
                                KanbanColumnType.DONE -> KanbanColumnType.IN_PROGRESS.id
                                KanbanColumnType.IN_PROGRESS -> KanbanColumnType.TODO.id
                                KanbanColumnType.TODO -> null
                            }
                            prev?.let { onMoveNote(note.id, it) }
                        },
                        onMoveNext = {
                            val next = when (column) {
                                KanbanColumnType.TODO -> KanbanColumnType.IN_PROGRESS.id
                                KanbanColumnType.IN_PROGRESS -> KanbanColumnType.DONE.id
                                KanbanColumnType.DONE -> null
                            }
                            next?.let { onMoveNote(note.id, it) }
                        }
                    )
                }

                if (notes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Kart yok",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KanbanItemCard(
    note: Note,
    currentColumn: KanbanColumnType,
    onClick: () -> Unit,
    onMovePrevious: () -> Unit,
    onMoveNext: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) iOSBackgroundDark else Color.White
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                note.icon?.let { icon ->
                    Text(text = icon, fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                }
                Text(
                    text = note.title.ifBlank { "Başlıksız Not" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content.take(80),
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.tags.take(2).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppleYellow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = AppleYellowDark,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Move Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentColumn != KanbanColumnType.TODO) {
                    IconButton(
                        onClick = onMovePrevious,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Taşı",
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }

                Text(
                    text = "Taşı",
                    fontSize = 10.sp,
                    color = textSecondary.copy(alpha = 0.6f)
                )

                if (currentColumn != KanbanColumnType.DONE) {
                    IconButton(
                        onClick = onMoveNext,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "İleri Taşı",
                            tint = AppleYellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
