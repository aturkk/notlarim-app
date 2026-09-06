package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagePropertiesBar(
    priority: String?,
    status: String?,
    progress: Int?,
    noteContent: String = "",
    onPriorityChange: (String?) -> Unit,
    onStatusChange: (String?) -> Unit,
    onProgressChange: (Int?) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val cardBg = if (isDark) iOSCardBackgroundDark else Color(0xFFF2F2F7)

    var isPriorityMenuOpen by remember { mutableStateOf(false) }
    var isStatusMenuOpen by remember { mutableStateOf(false) }
    var isProgressSliderOpen by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(true) }

    // Calculate auto checklist progress if checklists exist
    val checklistStats = remember(noteContent) {
        val lines = noteContent.lines()
        val totalChecklists = lines.count { it.trimStart().startsWith("- [ ]") || it.trimStart().startsWith("- [x]") || it.trimStart().startsWith("- [X]") }
        val doneChecklists = lines.count { it.trimStart().startsWith("- [x]") || it.trimStart().startsWith("- [X]") }
        if (totalChecklists > 0) Pair(doneChecklists, totalChecklists) else null
    }

    val effectiveProgress = progress ?: checklistStats?.let { (done, total) -> (done * 100) / total }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            // Title Header with Collapse Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = AppleYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sayfa Özellikleri",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (effectiveProgress != null) {
                        Text(
                            text = "%$effectiveProgress",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleYellow,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    // Property Pills Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Priority Pill
                        Box {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when (priority) {
                                    "HIGH" -> iOSRed.copy(alpha = 0.18f)
                                    "MEDIUM" -> AppleYellow.copy(alpha = 0.20f)
                                    "LOW" -> Color(0xFF34C759).copy(alpha = 0.18f)
                                    else -> if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)
                                },
                                modifier = Modifier.clickable { isPriorityMenuOpen = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (priority) {
                                                    "HIGH" -> iOSRed
                                                    "MEDIUM" -> AppleYellow
                                                    "LOW" -> Color(0xFF34C759)
                                                    else -> textSecondary
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (priority) {
                                            "HIGH" -> "Acil"
                                            "MEDIUM" -> "Normal"
                                            "LOW" -> "Düşük"
                                            else -> "Öncelik Seç"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when (priority) {
                                            "HIGH" -> iOSRed
                                            "MEDIUM" -> AppleYellowDark
                                            "LOW" -> Color(0xFF248A3D)
                                            else -> textSecondary
                                        }
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isPriorityMenuOpen,
                                onDismissRequest = { isPriorityMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🔴 Acil / Yüksek") },
                                    onClick = { onPriorityChange("HIGH"); isPriorityMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("🟡 Normal") },
                                    onClick = { onPriorityChange("MEDIUM"); isPriorityMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("🟢 Düşük") },
                                    onClick = { onPriorityChange("LOW"); isPriorityMenuOpen = false }
                                )
                                if (priority != null) {
                                    DropdownMenuItem(
                                        text = { Text("Kaldır", color = iOSRed) },
                                        onClick = { onPriorityChange(null); isPriorityMenuOpen = false }
                                    )
                                }
                            }
                        }

                        // 2. Status Pill
                        Box {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when (status) {
                                    "DONE" -> Color(0xFF34C759).copy(alpha = 0.18f)
                                    "IN_PROGRESS" -> Color(0xFF007AFF).copy(alpha = 0.18f)
                                    "NOT_STARTED" -> if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)
                                    else -> if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)
                                },
                                modifier = Modifier.clickable { isStatusMenuOpen = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (status) {
                                            "DONE" -> Icons.Default.CheckCircle
                                            "IN_PROGRESS" -> Icons.Default.Pending
                                            else -> Icons.Default.RadioButtonUnchecked
                                        },
                                        contentDescription = null,
                                        tint = when (status) {
                                            "DONE" -> Color(0xFF34C759)
                                            "IN_PROGRESS" -> Color(0xFF007AFF)
                                            else -> textSecondary
                                        },
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (status) {
                                            "DONE" -> "Tamamlandı"
                                            "IN_PROGRESS" -> "Sürüyor"
                                            "NOT_STARTED" -> "Başlanmadı"
                                            else -> "Durum Seç"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when (status) {
                                            "DONE" -> Color(0xFF248A3D)
                                            "IN_PROGRESS" -> Color(0xFF007AFF)
                                            else -> textSecondary
                                        }
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isStatusMenuOpen,
                                onDismissRequest = { isStatusMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("⚪ Başlanmadı") },
                                    onClick = { onStatusChange("NOT_STARTED"); isStatusMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("🔵 Sürüyor") },
                                    onClick = { onStatusChange("IN_PROGRESS"); isStatusMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("🟢 Tamamlandı") },
                                    onClick = { onStatusChange("DONE"); isStatusMenuOpen = false }
                                )
                                if (status != null) {
                                    DropdownMenuItem(
                                        text = { Text("Kaldır", color = iOSRed) },
                                        onClick = { onStatusChange(null); isStatusMenuOpen = false }
                                    )
                                }
                            }
                        }

                        // 3. Progress Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (effectiveProgress != null) AppleYellow.copy(alpha = 0.20f) else (if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)),
                            modifier = Modifier.clickable { isProgressSliderOpen = !isProgressSliderOpen }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LinearScale,
                                    contentDescription = null,
                                    tint = if (effectiveProgress != null) AppleYellowDark else textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (effectiveProgress != null) "%$effectiveProgress İlerleme" else "+ İlerleme",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (effectiveProgress != null) AppleYellowDark else textSecondary
                                )
                            }
                        }
                    }

                    // Progress Bar & Slider (if active or expanded)
                    if (effectiveProgress != null || isProgressSliderOpen) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { (effectiveProgress ?: 0) / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if ((effectiveProgress ?: 0) >= 100) Color(0xFF34C759) else AppleYellow,
                                trackColor = if (isDark) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = checklistStats?.let { "${it.first}/${it.second}" } ?: "%${effectiveProgress ?: 0}",
                                fontSize = 11.sp,
                                color = textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isProgressSliderOpen) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Slider(
                                    value = (effectiveProgress ?: 0).toFloat(),
                                    onValueChange = { onProgressChange(it.toInt()) },
                                    valueRange = 0f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AppleYellow,
                                        activeTrackColor = AppleYellow
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onProgressChange(null); isProgressSliderOpen = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Kaldır", tint = iOSRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
