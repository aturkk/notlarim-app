package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    onAddWikiLinkClick: () -> Unit
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
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Nota Ekle",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                ),
                color = textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AttachmentItemCard(
                icon = Icons.Default.Link,
                iconTint = Color(0xFF007AFF),
                iconBg = Color(0xFF007AFF).copy(alpha = 0.14f),
                title = "Not Bağlantısı Ekle ([[...]])",
                subtitle = "Mevcut başka bir notunuza çift yönlü bağlantı kurun",
                onClick = {
                    onDismiss()
                    onAddWikiLinkClick()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AttachmentItemCard(
                icon = Icons.Default.PhotoCamera,
                iconTint = Color(0xFF007AFF),
                iconBg = Color(0xFF007AFF).copy(alpha = 0.14f),
                title = "Belge & Görsel Tara (OCR)",
                subtitle = "Fotoğraf yükleyin veya yapay zeka ile metne dönüştürün",
                onClick = {
                    onDismiss()
                    onScanDocumentClick()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AttachmentItemCard(
                icon = Icons.Default.Brush,
                iconTint = Color(0xFFFF9500),
                iconBg = Color(0xFFFF9500).copy(alpha = 0.14f),
                title = "Çizim Ekle",
                subtitle = "Serbest çizim, el yazısı veya şema tuvali",
                onClick = {
                    onDismiss()
                    onDrawingClick()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AttachmentItemCard(
                icon = Icons.Default.Mic,
                iconTint = Color(0xFFFF3B30),
                iconBg = Color(0xFFFF3B30).copy(alpha = 0.14f),
                title = "Sesli Not Kaydet",
                subtitle = "Ses kaydı başlatın ve notunuza ekleyin",
                onClick = {
                    onDismiss()
                    onVoiceRecordClick()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AttachmentItemCard(
                icon = Icons.Default.TableChart,
                iconTint = Color(0xFF34C759),
                iconBg = Color(0xFF34C759).copy(alpha = 0.14f),
                title = "Tablo Ekle",
                subtitle = "Satır ve sütunlardan oluşan Markdown tablosu",
                onClick = {
                    onDismiss()
                    onInsertTableClick()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AttachmentItemCard(
                icon = Icons.Default.Code,
                iconTint = Color(0xFFAF52DE),
                iconBg = Color(0xFFAF52DE).copy(alpha = 0.14f),
                title = "Matematik Formülü / Kod Bloğu",
                subtitle = "LaTeX formül veya syntax kod bloğu",
                onClick = {
                    onDismiss()
                    onInsertFormulaClick()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AttachmentItemCard(
                icon = Icons.Default.MenuBook,
                iconTint = AppleYellow,
                iconBg = AppleYellow.copy(alpha = 0.14f),
                title = "Notion Blok Komutları (/)",
                subtitle = "Başlıklar, alıntılar, kutular ve ayırıcılar ekleyin",
                onClick = {
                    onDismiss()
                    onSlashMenuClick()
                }
            )
        }
    }
}

@Composable
private fun AttachmentItemCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(pressedScale = 0.97f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 0.5.dp,
                    color = if (isDark) Color(0xFF38383A) else Color(0xFFE5E5EA),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = textSecondary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
