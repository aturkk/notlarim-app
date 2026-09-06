package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*

data class TocHeading(
    val level: Int,
    val title: String,
    val lineIndex: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableOfContentsBottomSheet(
    noteContent: String,
    onDismiss: () -> Unit,
    onSelectHeading: (TocHeading) -> Unit,
    onInsertTocToNote: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    val headings = remember(noteContent) {
        val list = mutableListOf<TocHeading>()
        val headerRegex = Regex("""(?m)^(#+)\s+(.*)$""")
        noteContent.lines().forEachIndexed { index, line ->
            val match = headerRegex.find(line)
            if (match != null) {
                val hashes = match.groupValues[1]
                val title = match.groupValues[2].trim()
                if (title.isNotBlank()) {
                    list.add(TocHeading(level = hashes.length, title = title, lineIndex = index))
                }
            }
        }
        list
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
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
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = null,
                        tint = AppleYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "İçindekiler Tablosu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                if (headings.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onInsertTocToNote()
                            onDismiss()
                        }
                    ) {
                        Text("Nota Ekle", color = AppleYellow, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = if (headings.isNotEmpty()) "${headings.size} başlık tespit edildi. Bölüme gitmek için dokunun:" else "Bu notta henüz başlık (# Başlık) bulunmuyor.",
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            HorizontalDivider(color = if (isDark) iOSSeparatorDark else iOSSeparatorLight, thickness = 0.5.dp)

            if (headings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📑", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Notunuza '#' ile başlayan başlıklar eklediğinizde otomatik olarak burada listelenecektir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .padding(vertical = 8.dp)
                ) {
                    items(headings) { heading ->
                        val indent = ((heading.level - 1) * 16).coerceAtMost(48).dp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSelectHeading(heading)
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(indent))
                            Text(
                                text = when (heading.level) {
                                    1 -> "H1"
                                    2 -> "H2"
                                    else -> "H3"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (heading.level == 1) AppleYellow else textSecondary,
                                modifier = Modifier.width(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = heading.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (heading.level == 1) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = textSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        HorizontalDivider(
                            color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                            thickness = 0.5.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
