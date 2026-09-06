package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
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
import com.applenotes.ai.data.local.model.NoteHistoryEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionHistoryBottomSheet(
    historyList: List<NoteHistoryEntity>,
    onDismiss: () -> Unit,
    onRestoreVersion: (NoteHistoryEntity) -> Unit
) {
    val isDark = isAppDarkTheme()
    val bgColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    var selectedVersion by remember { mutableStateOf<NoteHistoryEntity?>(historyList.firstOrNull()) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr")) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = AppleYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Zaman Makinesi (Sürümler)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                selectedVersion?.let { version ->
                    Button(
                        onClick = {
                            onRestoreVersion(version)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Geri Yükle", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu not için henüz kaydedilmiş bir geçmiş sürüm bulunmuyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: Version timeline list
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyList) { item ->
                            val isSelected = selectedVersion?.id == item.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AppleYellow.copy(alpha = 0.2f)
                                else if (isDark) iOSBackgroundDark else Color(0xFFF2F2F7),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedVersion = item }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = dateFormat.format(Date(item.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AppleYellowDark else textPrimary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${item.content.length} karakter",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Right: Version preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) iOSBackgroundDark else Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize()
                        ) {
                            Text(
                                text = selectedVersion?.title?.ifBlank { "Başlıksız Not" } ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = selectedVersion?.content ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
