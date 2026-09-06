package com.applenotes.ai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*

data class SlashCommand(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val snippet: String
)

val defaultSlashCommands = listOf(
    SlashCommand(
        title = "Başlık 1 (H1)",
        description = "Büyük bölüm başlığı ekle",
        icon = Icons.Default.Title,
        snippet = "# "
    ),
    SlashCommand(
        title = "Başlık 2 (H2)",
        description = "Orta boy alt başlık ekle",
        icon = Icons.Default.FormatSize,
        snippet = "## "
    ),
    SlashCommand(
        title = "Başlık 3 (H3)",
        description = "Küçük alt başlık ekle",
        icon = Icons.Default.TextFields,
        snippet = "### "
    ),
    SlashCommand(
        title = "Yapılacaklar Listesi",
        description = "İşaretlenebilir onay kutuları ekle",
        icon = Icons.Default.CheckBox,
        snippet = "- [ ] "
    ),
    SlashCommand(
        title = "Öne Çıkan Not (Callout)",
        description = "Vurgulu bilgi veya dikkat kutusu",
        icon = Icons.Default.Lightbulb,
        snippet = "> 💡 **Not:** "
    ),
    SlashCommand(
        title = "Katlanabilir Bölüm (Toggle)",
        description = "Açılıp kapanabilir akordiyon metin",
        icon = Icons.Default.ExpandMore,
        snippet = "<details>\n<summary>Başlık</summary>\n\nBuraya detayları yazın...\n</details>\n"
    ),
    SlashCommand(
        title = "Tablo",
        description = "Düzenli 2 sütunlu Markdown tablosu",
        icon = Icons.Default.TableChart,
        snippet = "| Başlık 1 | Başlık 2 |\n| :--- | :--- |\n| Veri 1 | Veri 2 |\n| Veri 3 | Veri 4 |\n"
    ),
    SlashCommand(
        title = "Alıntı (Quote)",
        description = "Özel stilize edilmiş alıntı kutusu",
        icon = Icons.Default.FormatQuote,
        snippet = "> "
    ),
    SlashCommand(
        title = "Kod Bloğu",
        description = "Sözdizimi vurgulamalı kod alanı",
        icon = Icons.Default.Code,
        snippet = "```\n// Kodunuzu buraya yazın\n```\n"
    ),
    SlashCommand(
        title = "Madde İmleri",
        description = "Noktalı liste oluştur",
        icon = Icons.Default.FormatListBulleted,
        snippet = "• "
    ),
    SlashCommand(
        title = "Numaralı Liste",
        description = "1, 2, 3 sıralı liste oluştur",
        icon = Icons.Default.FormatListNumbered,
        snippet = "1. "
    ),
    SlashCommand(
        title = "Not Bağlantısı (Backlink)",
        description = "Başka bir nota çift köşeli bağlantı kur",
        icon = Icons.Default.Link,
        snippet = "[["
    ),
    SlashCommand(
        title = "Yatay Ayırıcı Çizgi",
        description = "Bölümleri estetik bir çizgiyle ayır",
        icon = Icons.Default.HorizontalRule,
        snippet = "\n---\n\n"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlashCommandBottomSheet(
    onDismiss: () -> Unit,
    onSelectCommand: (SlashCommand) -> Unit
) {
    val isDark = isAppDarkTheme()
    var searchQuery by remember { mutableStateOf("") }

    val filteredCommands = remember(searchQuery) {
        if (searchQuery.isBlank()) defaultSlashCommands
        else defaultSlashCommands.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
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
                Text(
                    text = "Blok Ekle (Komutlar)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "/",
                    color = AppleYellow,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Komut veya blok ara...", fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppleYellow,
                    unfocusedBorderColor = if (isDark) iOSSeparatorDark else iOSSeparatorLight
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                items(filteredCommands) { command ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectCommand(command) }
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppleYellow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = command.icon,
                                contentDescription = null,
                                tint = AppleYellowDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = command.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = command.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight,
                                fontSize = 12.sp
                            )
                        }
                    }
                    HorizontalDivider(
                        color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                        thickness = 0.5.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
