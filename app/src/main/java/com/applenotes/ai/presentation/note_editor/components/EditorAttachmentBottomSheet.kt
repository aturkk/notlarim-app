package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.components.bouncyClickable
import com.applenotes.ai.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorAttachmentBottomSheet(
    onDismiss: () -> Unit,
    onScanDocumentClick: () -> Unit,
    onDrawingClick: () -> Unit,
    onVoiceRecordClick: () -> Unit,
    onInsertTableClick: () -> Unit,
    onInsertFormulaClick: () -> Unit,
    onSlashMenuClick: () -> Unit,
    onAddWikiLinkClick: () -> Unit,
    onAddImagesClick: () -> Unit,
    onAddPdfClick: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val bgColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = if (isDark) Color(0xFF545458) else Color(0xFFD1D1D6)
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nota Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = textPrimary
                )
                Text(
                    text = "9 Araç",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
            }

            // 3x3 Apple Action Grid
            val items = listOf(
                GridAttachmentItem(
                    icon = Icons.Default.Collections,
                    iconBg = Color(0xFF34C759),
                    title = "Fotoğraflar",
                    subtitle = "Galeri",
                    onClick = { onDismiss(); onAddImagesClick() }
                ),
                GridAttachmentItem(
                    icon = Icons.Default.PictureAsPdf,
                    iconBg = Color(0xFFFF3B30),
                    title = "PDF Belgesi",
                    subtitle = "İliştir & Oku",
                    onClick = { onDismiss(); onAddPdfClick() }
                ),
                GridAttachmentItem(
                    icon = Icons.Default.Brush,
                    iconBg = Color(0xFFFF9500),
                    title = "Çizim Tuvali",
                    subtitle = "El Yazısı / Şema",
                    onClick = { onDismiss(); onDrawingClick() }
                ),
                GridAttachmentItem(
                    icon = Icons.Default.Mic,
                    iconBg = Color(0xFFFF2D55),
                    title = "Ses Kaydı",
                    subtitle = "Canlı Dalga",
                    onClick = { onDismiss(); onVoiceRecordClick() }
                ),
                GridAttachmentItem(
                    icon = Icons.Default.TableChart,
                    iconBg = Color(0xFF007AFF),
                    title = "Tablo",
                    subtitle = "Markdown Editör",
                    onClick = { onDismiss(); onInsertTableClick() }
                ),
                GridAttachmentItem(
                    icon = Icons.Default.Link,
                    iconBg = Color(0xFF5856D6),
                    title = "Not Linki",
                    subtitle = "[[Wiki-Bağlantı]]",
                    onClick = { onDismiss(); onAddWikiLinkClick() }
                ),
                GridAttachmentItem(
                    icon = Icons.Default.PhotoCamera,
                    iconBg = Color(0xFF00C7BE),
                    title = "Metin Tara",
                    subtitle = "Görsel OCR",
                    onClick = { onDismiss(); onScanDocumentClick() }
                ),
                GridAttachmentItem(
                    icon = Icons.Default.Code,
                    iconBg = Color(0xFFAF52DE),
                    title = "Formül / Kod",
                    subtitle = "KaTeX Matematik",
                    onClick = { onDismiss(); onInsertFormulaClick() }
                ),
                GridAttachmentItem(
                    icon = Icons.Default.MenuBook,
                    iconBg = AppleYellow,
                    title = "Blok Menüsü",
                    subtitle = "/ Komutları",
                    onClick = { onDismiss(); onSlashMenuClick() }
                )
            )

            // Render 3 items per row
            for (rowIndex in items.indices step 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (colIndex in 0..2) {
                        val itemIndex = rowIndex + colIndex
                        if (itemIndex < items.size) {
                            val item = items[itemIndex]
                            Box(modifier = Modifier.weight(1f)) {
                                AttachmentGridTile(
                                    item = item,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary,
                                    isDark = isDark
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private data class GridAttachmentItem(
    val icon: ImageVector,
    val iconBg: Color,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
private fun AttachmentGridTile(
    item: GridAttachmentItem,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(pressedScale = 0.94f, onClick = item.onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp
                ),
                color = textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

