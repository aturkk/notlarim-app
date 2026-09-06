package com.applenotes.ai.presentation.note_editor.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.applenotes.ai.core.theme.*

@Composable
fun AiResultPreviewDialog(
    title: String,
    generatedText: String,
    isRegenerating: Boolean,
    activeTone: String?,
    onApplyAppend: (String) -> Unit,
    onApplyReplace: (String) -> Unit,
    onRegenerate: (tone: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val bgColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var editedText by remember(generatedText) { mutableStateOf(generatedText) }

    val toneOptions = listOf(
        "Kurumsal" to "👔 Kurumsal",
        "Samimi" to "😊 Samimi",
        "Gündelik" to "💬 Gündelik",
        "Kısa & Öz" to "✂️ Kısa & Öz",
        "Detaylı" to "📝 Detaylı"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppleYellow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AppleYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title.ifBlank { "Yapay Zeka Yanıtı" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "İnceleyin, üslup seçin veya düzenleyin",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = textSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                    thickness = 0.5.dp
                )

                // Tone Selector Chips Row
                Text(
                    text = "Farklı Üslupla Yeniden Oluştur:",
                    style = MaterialTheme.typography.labelMedium,
                    color = textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    toneOptions.forEach { (toneKey, toneLabel) ->
                        val isSelected = activeTone == toneKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { onRegenerate(toneKey) },
                            label = { Text(toneLabel, fontSize = 12.sp) },
                            enabled = !isRegenerating,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppleYellow,
                                selectedLabelColor = Color.Black,
                                containerColor = if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight,
                                labelColor = textPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Content Area (Scrollable editable or viewable)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) iOSSecondaryBackgroundDark else iOSSecondaryBackgroundLight)
                        .padding(12.dp)
                ) {
                    if (isRegenerating) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = AppleYellow,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Yapay zeka ${activeTone ?: "seçilen üslupla"} yazıyor...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondary
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = editedText,
                            onValueChange = { editedText = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = textPrimary,
                                lineHeight = 22.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = AppleYellow
                            )
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                    thickness = 0.5.dp
                )

                // Actions Bottom Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Quick tools (Copy, Re-run)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { onRegenerate(null) },
                            enabled = !isRegenerating
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yeniden Üret",
                                tint = AppleYellow
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(editedText))
                                Toast.makeText(context, "Kopyalandı", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Kopyala",
                                tint = textSecondary
                            )
                        }
                    }

                    // Right: Insertion Options ("Değiştir" vs "Nota Ekle")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { onApplyReplace(editedText) },
                            enabled = !isRegenerating && editedText.isNotBlank()
                        ) {
                            Text(
                                text = "Değiştir",
                                color = textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { onApplyAppend(editedText) },
                            enabled = !isRegenerating && editedText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nota Ekle",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
