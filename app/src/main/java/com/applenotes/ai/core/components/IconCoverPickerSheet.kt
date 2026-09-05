package com.applenotes.ai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.applenotes.ai.core.theme.*

val defaultPresetIcons = listOf(
    "📝", "💡", "🚀", "🎯", "📚", "💼", "⭐️", "🎨",
    "📊", "⚡", "🔔", "🏷️", "🗓️", "✈️", "☕", "💻",
    "🏆", "💰", "🎧", "🎬", "🌿", "🔥", "🛠️", "📌",
    "❤️", "🧘", "🍕", "🏔️", "🎓", "📖", "🔑", "✨"
)

val defaultPresetCovers = listOf(
    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&q=80" to "Modern Soyut",
    "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&q=80" to "Pastel Degrade",
    "https://images.unsplash.com/photo-1448375240586-882707db888b?w=1200&q=80" to "Sisli Orman",
    "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&q=80" to "Neon Şehir",
    "https://images.unsplash.com/photo-1497215728101-856f4ea42174?w=1200&q=80" to "Minimal Çalışma Alanı",
    "https://images.unsplash.com/photo-1506784983877-45594efa4cbe?w=1200&q=80" to "Planlama & Ajanda"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerBottomSheet(
    currentIcon: String?,
    onDismiss: () -> Unit,
    onSelectIcon: (String?) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var customEmoji by remember { mutableStateOf("") }

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
                    text = "Sayfa Simgesi Seç",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (currentIcon != null) {
                    TextButton(onClick = { onSelectIcon(null) }) {
                        Text("Simgesiz Yap", color = iOSRed, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom Emoji Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customEmoji,
                    onValueChange = { customEmoji = it.take(2) },
                    placeholder = { Text("Özel Emoji Yaz...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleYellow,
                        unfocusedBorderColor = if (isDark) iOSSeparatorDark else iOSSeparatorLight
                    )
                )
                Button(
                    onClick = {
                        if (customEmoji.isNotBlank()) {
                            onSelectIcon(customEmoji.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                    shape = RoundedCornerShape(12.dp),
                    enabled = customEmoji.isNotBlank()
                ) {
                    Text("Uygula", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ÖNERİLEN SİMGELER",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(defaultPresetIcons) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (currentIcon == emoji) AppleYellow.copy(alpha = 0.25f)
                                else if (isDark) iOSBackgroundDark else Color(0xFFF2F2F7)
                            )
                            .border(
                                width = if (currentIcon == emoji) 1.5.dp else 0.dp,
                                color = if (currentIcon == emoji) AppleYellow else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectIcon(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPickerBottomSheet(
    currentCoverUrl: String?,
    onDismiss: () -> Unit,
    onSelectCover: (String?) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var customUrl by remember { mutableStateOf("") }

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
                    text = "Kapak Görseli Seç",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (currentCoverUrl != null) {
                    TextButton(onClick = { onSelectCover(null) }) {
                        Text("Kapağı Kaldır", color = iOSRed, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom URL input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    placeholder = { Text("Görsel URL'si yapıştır...", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleYellow,
                        unfocusedBorderColor = if (isDark) iOSSeparatorDark else iOSSeparatorLight
                    )
                )
                Button(
                    onClick = {
                        if (customUrl.isNotBlank()) {
                            onSelectCover(customUrl.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                    shape = RoundedCornerShape(12.dp),
                    enabled = customUrl.isNotBlank()
                ) {
                    Text("Ekle", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HAZIR ESTETİK KAPAKLAR",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(defaultPresetCovers) { (url, label) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectCover(url) }
                            .border(
                                width = if (currentCoverUrl == url) 2.dp else 0.dp,
                                color = if (currentCoverUrl == url) AppleYellow else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
