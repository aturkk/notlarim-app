package com.applenotes.ai.presentation.notes_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.haptic.rememberHapticFeedbackHelper
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteContextMenuBottomSheet(
    note: Note,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleLock: () -> Unit,
    onSharePdf: () -> Unit,
    onShareImageCard: () -> Unit,
    onMoveToFolder: () -> Unit,
    onEnterSelectMode: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val bgColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val haptic = rememberHapticFeedbackHelper()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        tonalElevation = 0.dp,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                color = if (isDark) Color(0xFF5A5A5E) else Color(0xFFD1D1D6),
                shape = RoundedCornerShape(3.dp)
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 5.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Mini Preview Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!note.icon.isNullOrBlank()) {
                            Text(text = note.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (note.title.isNotBlank()) note.title else "Başlıksız Not",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (note.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val cleanSnippet = note.content
                            .replace(Regex("#+\\s*"), "")
                            .replace(Regex("\\[[ xX]\\]\\s*"), "")
                            .replace(Regex("[*_~`]"), "")
                            .lines()
                            .firstOrNull { it.isNotBlank() } ?: ""
                        Text(
                            text = if (note.isLocked) "🔒 Kilitli Not İçeriği" else cleanSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Inset Grouped Menu Actions
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF2C2C2E) else Color(0xFFFFFFFF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Pin / Unpin
                    ContextMenuItem(
                        icon = Icons.Default.PushPin,
                        label = if (note.isPinned) "Sabitlemeyi Kaldır" else "Başa Sabitle",
                        iconTint = AppleYellow,
                        textColor = textPrimary,
                        onClick = {
                            haptic.tick()
                            onTogglePin()
                            onDismiss()
                        }
                    )

                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 48.dp)
                    )

                    // 2. Duplicate Note ("Notu Çoğalt")
                    ContextMenuItem(
                        icon = Icons.Default.ContentCopy,
                        label = "Notu Çoğalt",
                        iconTint = AppleYellow,
                        textColor = textPrimary,
                        onClick = {
                            haptic.success()
                            onDuplicate()
                            onDismiss()
                        }
                    )

                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 48.dp)
                    )

                    // 3. Lock / Unlock
                    ContextMenuItem(
                        icon = if (note.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        label = if (note.isLocked) "Kilidi Kaldır" else "Biyometrik Kilitle",
                        iconTint = AppleYellow,
                        textColor = textPrimary,
                        onClick = {
                            haptic.tick()
                            onToggleLock()
                            onDismiss()
                        }
                    )

                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 48.dp)
                    )

                    // 4. Share PDF
                    ContextMenuItem(
                        icon = Icons.Default.PictureAsPdf,
                        label = "PDF Olarak Paylaş",
                        iconTint = AppleYellow,
                        textColor = textPrimary,
                        onClick = {
                            haptic.tick()
                            onSharePdf()
                            onDismiss()
                        }
                    )

                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 48.dp)
                    )

                    // 5. Share Social Media Card
                    ContextMenuItem(
                        icon = Icons.Default.Image,
                        label = "Görsel Kartı (PNG) Paylaş",
                        iconTint = AppleYellow,
                        textColor = textPrimary,
                        onClick = {
                            haptic.tick()
                            onShareImageCard()
                            onDismiss()
                        }
                    )

                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 48.dp)
                    )

                    // 6. Move to Folder
                    ContextMenuItem(
                        icon = Icons.Default.DriveFileMove,
                        label = "Klasöre Taşı...",
                        iconTint = textSecondary,
                        textColor = textPrimary,
                        onClick = {
                            haptic.tick()
                            onMoveToFolder()
                            onDismiss()
                        }
                    )

                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 48.dp)
                    )

                    // 5. Enter Multi-Select Mode
                    ContextMenuItem(
                        icon = Icons.Default.CheckCircleOutline,
                        label = "Çoklu Seçim Modu",
                        iconTint = textSecondary,
                        textColor = textPrimary,
                        onClick = {
                            haptic.tick()
                            onEnterSelectMode()
                            onDismiss()
                        }
                    )

                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 48.dp)
                    )

                    // 6. Delete Note
                    ContextMenuItem(
                        icon = Icons.Default.DeleteOutline,
                        label = "Çöp Kutusuna Taşı",
                        iconTint = iOSRed,
                        textColor = iOSRed,
                        onClick = {
                            haptic.warning()
                            onDelete()
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}
