package com.applenotes.ai.presentation.ai_assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
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

@Composable
fun AiFlashcardsDialog(
    rawText: String,
    onDismiss: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val cardSurface = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)

    val parsedCards = remember(rawText) {
        val pairs = mutableListOf<Pair<String, String>>()
        var currentQuestion: String? = null
        val answerBuilder = StringBuilder()

        rawText.lines().forEach { line ->
            val trimmed = line.trim().removePrefix("-").removePrefix("*").trim()
            val lower = trimmed.lowercase()
            when {
                lower.startsWith("soru") || lower.startsWith("**soru") || lower.startsWith("q:") || lower.startsWith("s:") -> {
                    if (currentQuestion != null && answerBuilder.isNotBlank()) {
                        pairs.add(currentQuestion!! to answerBuilder.toString().trim())
                        answerBuilder.clear()
                    }
                    val colonIdx = trimmed.indexOf(':')
                    currentQuestion = if (colonIdx != -1) {
                        trimmed.substring(colonIdx + 1).replace("**", "").trim()
                    } else {
                        trimmed.replace("**", "").trim()
                    }
                }
                lower.startsWith("cevap") || lower.startsWith("**cevap") || lower.startsWith("a:") || lower.startsWith("c:") -> {
                    val colonIdx = trimmed.indexOf(':')
                    val ans = if (colonIdx != -1) {
                        trimmed.substring(colonIdx + 1).replace("**", "").trim()
                    } else {
                        trimmed.replace("**", "").trim()
                    }
                    if (answerBuilder.isNotEmpty()) answerBuilder.append("\n")
                    answerBuilder.append(ans)
                }
                currentQuestion != null && trimmed.isNotBlank() && answerBuilder.isNotEmpty() -> {
                    answerBuilder.append("\n").append(trimmed)
                }
            }
        }
        if (currentQuestion != null && answerBuilder.isNotBlank()) {
            pairs.add(currentQuestion!! to answerBuilder.toString().trim())
        }
        pairs
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = AppleYellow,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Çalışma Kartları (Flashcards)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (parsedCards.isEmpty()) {
                    Text(
                        text = rawText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary,
                        lineHeight = 22.sp
                    )
                } else {
                    Text(
                        text = "Cevabı görmek için kartın üzerine dokunun:",
                        style = MaterialTheme.typography.labelMedium,
                        color = textSecondary
                    )
                    parsedCards.forEachIndexed { index, (question, answer) ->
                        var isAnswerVisible by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = cardSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isAnswerVisible = !isAnswerVisible }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Soru ${index + 1}: $question",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Cevabı Göster",
                                        tint = if (isAnswerVisible) AppleYellow else textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                AnimatedVisibility(visible = isAnswerVisible) {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = textSecondary.copy(alpha = 0.2f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "💡 $answer",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AppleYellow,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tamam", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
        shape = RoundedCornerShape(16.dp)
    )
}