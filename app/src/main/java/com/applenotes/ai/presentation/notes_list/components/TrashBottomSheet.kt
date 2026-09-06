package com.applenotes.ai.presentation.notes_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Restore
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
import com.applenotes.ai.core.components.bouncyClickable
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashBottomSheet(
    deletedNotes: List<Note>,
    onDismiss: () -> Unit,
    onRestoreNote: (Long) -> Unit,
    onDeletePermanently: (Long) -> Unit,
    onEmptyTrash: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()
    val bgColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val itemBg = if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight

    var showEmptyTrashConfirm by remember { mutableStateOf(false) }
    var noteToDeletePermanently by remember { mutableStateOf<Note?>(null) }

    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("tr")) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = if (isDark) Color(0xFF545458) else Color(0xFFD1D1D6)
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iOSRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = iOSRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Son Silinenler",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "${deletedNotes.size} Not",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                }

                if (deletedNotes.isNotEmpty()) {
                    TextButton(
                        onClick = { showEmptyTrashConfirm = true }
                    ) {
                        Text(
                            text = "Tümünü Boşalt",
                            color = iOSRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Text(
                text = "Silinen notlar burada saklanır. Dilediğiniz notu kurtarabilir veya kalıcı olarak temizleyebilirsiniz.",
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (deletedNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Çöp Kutusu Boş",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                        Text(
                            text = "Sildiğiniz notlar burada listelenir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(deletedNotes, key = { it.id }) { note ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = itemBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = note.icon ?: "📝",
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = note.title.ifBlank { "Başlıksız Not" },
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = note.content.take(60).replace("`n", " "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = dateFormat.format(Date(note.updatedAt)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = textSecondary.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Restore Button
                                    IconButton(
                                        onClick = { onRestoreNote(note.id) },
                                        modifier = Modifier.bouncyClickable { onRestoreNote(note.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Restore,
                                            contentDescription = "Geri Yükle",
                                            tint = accentColor
                                        )
                                    }

                                    // Delete Permanently Button
                                    IconButton(
                                        onClick = { noteToDeletePermanently = note },
                                        modifier = Modifier.bouncyClickable { noteToDeletePermanently = note }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = "Kalıcı Sil",
                                            tint = iOSRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Single Note Permanent Delete Confirmation Dialog
    noteToDeletePermanently?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDeletePermanently = null },
            title = { Text("Notu Kalıcı Olarak Sil", fontWeight = FontWeight.Bold) },
            text = {
                Text("\"${note.title.ifBlank { "Başlıksız Not" }}\" kalıcı olarak silinecek. Bu işlem geri alınamaz.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePermanently(note.id)
                        noteToDeletePermanently = null
                    }
                ) {
                    Text("Kalıcı Olarak Sil", color = iOSRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDeletePermanently = null }) {
                    Text("Vazgeç", color = textSecondary)
                }
            },
            containerColor = bgColor
        )
    }

    // Empty Trash Confirmation Dialog
    if (showEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirm = false },
            title = { Text("Çöp Kutusunu Boşalt", fontWeight = FontWeight.Bold) },
            text = {
                Text("Çöp kutusundaki tüm notlar (${deletedNotes.size} adet) kalıcı olarak silinecektir. Bu işlem geri alınamaz.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEmptyTrash()
                        showEmptyTrashConfirm = false
                    }
                ) {
                    Text("Tümünü Sil", color = iOSRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashConfirm = false }) {
                    Text("Vazgeç", color = textSecondary)
                }
            },
            containerColor = bgColor
        )
    }
}
