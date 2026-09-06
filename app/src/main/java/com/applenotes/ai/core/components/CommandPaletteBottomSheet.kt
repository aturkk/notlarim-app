package com.applenotes.ai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.haptic.rememberHapticFeedbackHelper
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.Note

data class CommandAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color? = null,
    val category: String = "Komutlar",
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandPaletteBottomSheet(
    allNotes: List<Note>,
    onDismiss: () -> Unit,
    onSelectNote: (Long) -> Unit,
    onNewNote: () -> Unit,
    onOpenDailyNote: () -> Unit,
    onOpenCloudSync: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAiHub: () -> Unit,
    onSelectSmartFolder: (String) -> Unit
) {
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()
    val bgCard = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val haptic = rememberHapticFeedbackHelper()

    var query by remember { mutableStateOf("") }

    val baseCommands = remember {
        listOf(
            CommandAction(
                id = "new_note",
                title = "Yeni Not Oluştur",
                subtitle = "Boş bir not açıp hemen yazmaya başla",
                icon = Icons.Default.AddCircle,
                iconTint = accentColor,
                action = { onNewNote(); onDismiss() }
            ),
            CommandAction(
                id = "daily_note",
                title = "Günün Notu",
                subtitle = "Bugünün tarihli günlüğünü aç veya oluştur",
                icon = Icons.Default.Today,
                iconTint = accentColor,
                action = { onOpenDailyNote(); onDismiss() }
            ),
            CommandAction(
                id = "ai_hub",
                title = "Yapay Zeka Asistanı & Sentez",
                subtitle = "Tüm not arşivin hakkında soru sor veya brifing al",
                icon = Icons.Default.AutoAwesome,
                iconTint = accentColor,
                action = { onOpenAiHub(); onDismiss() }
            ),
            CommandAction(
                id = "cloud_sync",
                title = "Kişisel Bulut Senkronizasyonu",
                subtitle = "WebDAV veya Google Drive ile notları yedekle/eşitle",
                icon = Icons.Default.CloudSync,
                iconTint = iOSBlue,
                action = { onOpenCloudSync(); onDismiss() }
            ),
            CommandAction(
                id = "smart_reminders",
                title = "Hatırlatıcılar (@hatırlatıcılar)",
                subtitle = "Zamanlanmış alarmı olan notları listele",
                icon = Icons.Default.Alarm,
                iconTint = AppleYellow,
                action = { onSelectSmartFolder("REMINDERS"); onDismiss() }
            ),
            CommandAction(
                id = "smart_pinned",
                title = "Sabitlenen Notlar (@sabitlenenler)",
                subtitle = "Başa tutturulmuş tüm notları görüntüle",
                icon = Icons.Default.PushPin,
                iconTint = AppleYellow,
                action = { onSelectSmartFolder("PINNED"); onDismiss() }
            ),
            CommandAction(
                id = "smart_urgent",
                title = "Acil Notlar (@acil)",
                subtitle = "Kırmızı öncelik işaretli acil görevleri listele",
                icon = Icons.Default.PriorityHigh,
                iconTint = iOSRed,
                action = { onSelectSmartFolder("URGENT"); onDismiss() }
            ),
            CommandAction(
                id = "trash",
                title = "Son Silinenler (Çöp Kutusu)",
                subtitle = "Silinen notları incele veya geri yükle",
                icon = Icons.Default.DeleteOutline,
                iconTint = iOSRed,
                action = { onOpenTrash(); onDismiss() }
            ),
            CommandAction(
                id = "settings",
                title = "Uygulama Ayarları",
                subtitle = "Tema, vurgu rengi, yazı tipleri ve güvenlik",
                icon = Icons.Default.Settings,
                iconTint = textSecondary,
                action = { onOpenSettings(); onDismiss() }
            )
        )
    }

    val filteredCommands = remember(query) {
        if (query.isBlank()) {
            baseCommands
        } else {
            val q = query.trim().lowercase()
            baseCommands.filter {
                it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q) || it.id.contains(q)
            }
        }
    }

    val matchingNotes = remember(query, allNotes) {
        if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            allNotes.filter {
                it.title.lowercase().contains(q) || it.content.lowercase().contains(q) || it.tags.any { t -> t.lowercase().contains(q) }
            }.take(8)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = bgCard,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = if (isDark) Color(0xFF545458) else Color(0xFFD1D1D6))
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Spotlight Komut Merkezi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Eylemleri çalıştırın veya notlarınızda hızla arayın",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Box
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Komut yazın veya not arayın...", fontSize = 14.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Temizle", tint = textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                    cursorColor = accentColor
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (matchingNotes.isNotEmpty()) {
                    item {
                        Text(
                            text = "📝 Eşleşen Notlar (${matchingNotes.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                    items(matchingNotes, key = { "note_${it.id}" }) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.tick()
                                    onSelectNote(note.id)
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = note.icon ?: "📝", fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title.ifBlank { "Başlıksız Not" },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = textPrimary,
                                    maxLines = 1
                                )
                                if (note.content.isNotBlank()) {
                                    Text(
                                        text = note.content.take(60).replace("\n", " "),
                                        fontSize = 12.sp,
                                        color = textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = textSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    item {
                        HorizontalDivider(
                            color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                if (filteredCommands.isNotEmpty()) {
                    item {
                        Text(
                            text = "⚡ Hızlı Eylemler & Filtreler",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                    items(filteredCommands, key = { it.id }) { cmd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.tick()
                                    cmd.action()
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cmd.icon,
                                contentDescription = null,
                                tint = cmd.iconTint ?: accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cmd.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = textPrimary
                                )
                                Text(
                                    text = cmd.subtitle,
                                    fontSize = 11.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
