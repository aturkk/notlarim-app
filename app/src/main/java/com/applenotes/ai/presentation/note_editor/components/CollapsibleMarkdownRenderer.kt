package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
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

data class MarkdownSection(
    val level: Int, // 1 for #, 2 for ##, 3 for ###
    val title: String,
    val contentLines: List<String>
)

fun parseMarkdownSections(markdown: String): List<MarkdownSection> {
    val lines = markdown.lines()
    val sections = mutableListOf<MarkdownSection>()
    var currentLevel = 0
    var currentTitle = "Giriş"
    val currentContent = mutableListOf<String>()

    for (line in lines) {
        val trimmed = line.trim()
        val headingMatch = Regex("^(#{1,3})\\s+(.+)").find(trimmed)
        if (headingMatch != null) {
            if (currentContent.isNotEmpty() || currentLevel > 0) {
                sections.add(MarkdownSection(currentLevel, currentTitle, currentContent.toList()))
                currentContent.clear()
            }
            currentLevel = headingMatch.groupValues[1].length
            currentTitle = headingMatch.groupValues[2]
        } else {
            currentContent.add(line)
        }
    }
    if (currentContent.isNotEmpty() || currentLevel > 0) {
        sections.add(MarkdownSection(currentLevel, currentTitle, currentContent))
    }
    return sections
}

@Composable
fun CollapsibleMarkdownView(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    val sections = remember(markdown) { parseMarkdownSections(markdown) }

    Column(modifier = modifier.fillMaxWidth()) {
        sections.forEachIndexed { index, section ->
            if (section.level == 0) {
                // Intro text without heading
                if (section.contentLines.any { it.isNotBlank() }) {
                    Text(
                        text = section.contentLines.joinToString("\n"),
                        fontSize = 15.sp,
                        color = textPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            } else {
                var isExpanded by remember { mutableStateOf(true) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                            contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = section.title,
                            fontSize = when (section.level) {
                                1 -> 18.sp
                                2 -> 16.sp
                                else -> 15.sp
                            },
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (!isExpanded) {
                            Text(
                                text = "${section.contentLines.size} satır gizlendi",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    }

                    // Content
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (section.level * 8).dp, top = 4.dp, bottom = 8.dp)
                        ) {
                            Text(
                                text = section.contentLines.joinToString("\n"),
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
