package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.applenotes.ai.core.theme.*

@Composable
fun ZenFocusModeDialog(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF121212) else Color(0xFFFAF9F6)
    val textColor = if (isDark) Color(0xFFECECEC) else Color(0xFF1E1E1E)
    val subtleColor = if (isDark) Color(0xFF757575) else Color(0xFF9E9E9E)

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Subtle Top Row: Word count & Exit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val wordCount = remember(content) {
                        content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                    }
                    Text(
                        text = "ZEN ODAK MODU · $wordCount kelime",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = subtleColor
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(subtleColor.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Zen Modundan Çık",
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Minimalist Title
                BasicTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    cursorBrush = SolidColor(AppleYellow),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                text = "Başlık...",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = subtleColor.copy(alpha = 0.4f),
                                    fontFamily = FontFamily.Serif
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Minimalist Content
                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = textColor,
                        lineHeight = 30.sp,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif
                    ),
                    cursorBrush = SolidColor(AppleYellow),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text(
                                text = "Tüm dikkat dağıtıcılar susturuldu. Sadece düşüncelerinizi yazın...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = subtleColor.copy(alpha = 0.4f),
                                    lineHeight = 30.sp,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 450.dp)
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
