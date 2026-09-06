package com.applenotes.ai.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.Note
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.clip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Link

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableNoteCard(
    note: Note,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    isCompact: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val cardBg = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val haptic = com.applenotes.ai.core.haptic.rememberHapticFeedbackHelper()

    var isDeleted by remember(note.id) { mutableStateOf(false) }

    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            delay(260)
            onDelete()
        }
    }

    val dismissState = key(note.id) {
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                when (value) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        haptic.tick()
                        onTogglePin()
                        false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        haptic.warning()
                        isDeleted = true
                        true
                    }
                    SwipeToDismissBoxValue.Settled -> false
                }
            }
        )
    }

    AnimatedVisibility(
        visible = !isDeleted,
        exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(200))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = !isSelectionMode,
            enableDismissFromEndToStart = !isSelectionMode,
            modifier = modifier,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val color by animateColorAsState(
                    when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> AppleYellow
                        SwipeToDismissBoxValue.EndToStart -> iOSRed
                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                    },
                    label = "swipe_bg"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    if (direction == SwipeToDismissBoxValue.StartToEnd) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Sabitle",
                            tint = Color.White
                        )
                    } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = Color.White
                        )
                    }
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = if (onLongClick != null) {
                            {
                                haptic.heavy()
                                onLongClick()
                            }
                        } else null
                    )
                    .padding(horizontal = 16.dp, vertical = if (isCompact) 8.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AppleYellow else Color.Transparent)
                        .then(
                            if (!isSelected) Modifier.background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                            else Modifier
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
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Text(
                    text = (note.icon?.let { "$it " } ?: "") + (if (note.title.isNotBlank()) note.title else "Başlıksız Not"),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.reminderTime != null && note.reminderTime > 0) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Hatırlatıcı",
                            tint = AppleYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (note.isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Kilitli",
                            tint = AppleYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Sabitlendi",
                            tint = AppleYellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateText = remember(note.updatedAt) {
                    formatDate(note.updatedAt)
                }

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = textSecondary
                )

                Spacer(modifier = Modifier.width(8.dp))

                val previewSnippet = remember(note.content, note.isLocked) {
                    if (note.isLocked) "🔒 Kilitli Not (Açmak için dokunun)"
                    else cleanMarkdownSnippet(note.content)
                }

                Text(
                    text = if (previewSnippet.isNotBlank()) previewSnippet else "Ek metin yok",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = if (note.isLocked) AppleYellowDark else textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            val detectedDomain = remember(note.content) {
                val regex = Regex("https?://([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})")
                val match = regex.find(note.content)
                match?.groupValues?.get(1)?.removePrefix("www.")
            }

            // Badges: Reminder, Tags, Priority, Status, Progress, Smart Link
            if (!isCompact && (note.tags.isNotEmpty() || note.priority != null || note.status != null || note.progress != null || (note.reminderTime != null && note.reminderTime > 0) || detectedDomain != null)) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Smart Link Preview Badge
                    detectedDomain?.let { domain ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppleYellow.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = AppleYellowDark,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = domain,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppleYellowDark
                                )
                            }
                        }
                    }

                    // Reminder Badge
                    if (note.reminderTime != null && note.reminderTime > 0) {
                        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppleYellow.copy(alpha = 0.20f)
                        ) {
                            Text(
                                text = "🔔 ${sdf.format(Date(note.reminderTime))}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleYellowDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Priority Badge
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
                                    "HIGH" -> "🔴 Acil"
                                    "MEDIUM" -> "🟡 Normal"
                                    else -> "🟢 Düşük"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (p) {
                                    "HIGH" -> iOSRed
                                    "MEDIUM" -> AppleYellowDark
                                    else -> Color(0xFF248A3D)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Status Badge
                    note.status?.let { s ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (s) {
                                "DONE" -> Color(0xFF34C759).copy(alpha = 0.18f)
                                "IN_PROGRESS" -> Color(0xFF007AFF).copy(alpha = 0.18f)
                                else -> if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)
                            }
                        ) {
                            Text(
                                text = when (s) {
                                    "DONE" -> "✓ Bitti"
                                    "IN_PROGRESS" -> "⏳ Sürüyor"
                                    else -> "○ Bekliyor"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (s) {
                                    "DONE" -> Color(0xFF248A3D)
                                    "IN_PROGRESS" -> Color(0xFF007AFF)
                                    else -> textSecondary
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Progress %
                    note.progress?.let { pr ->
                        Text(
                            text = "%$pr",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleYellow
                        )
                    }

                    // Tags
                    note.tags.take(2).forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = AppleYellow
                        )
                    }
                }
            }
        }
        }
    }
}
}

private fun formatDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val noteDate = Calendar.getInstance().apply { timeInMillis = timestamp }

    return if (now.get(Calendar.YEAR) == noteDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == noteDate.get(Calendar.DAY_OF_YEAR)
    ) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    } else {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun cleanMarkdownSnippet(markdown: String): String {
    return markdown
        .replace(Regex("#+\\s*"), "")
        .replace(Regex("\\[[ xX]\\]\\s*"), "")
        .replace(Regex("[*_~`]"), "")
        .lines()
        .firstOrNull { it.isNotBlank() } ?: ""
}
