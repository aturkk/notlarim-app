package com.applenotes.ai.presentation.notes_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.components.bouncyClickable
import com.applenotes.ai.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHubBottomSheet(
    onDismiss: () -> Unit,
    onMorningDigestClick: () -> Unit,
    onGlobalAiChatClick: () -> Unit,
    onSynthesisClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AppleYellow, Color(0xFFFF9500), Color(0xFFAF52DE))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Apple Intelligence",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = "Notlarınız için yapay zeka araçları",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }

            // Cards
            AiHubActionCard(
                icon = Icons.Default.WbSunny,
                iconTint = Color(0xFFFF9500),
                iconBg = Color(0xFFFF9500).copy(alpha = 0.15f),
                title = "Sabah Brifingi",
                subtitle = "Bugünün görevleri, ajandası ve günün öne çıkan notları",
                badge = "Günlük",
                onClick = {
                    onDismiss()
                    onMorningDigestClick()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AiHubActionCard(
                icon = Icons.Default.Psychology,
                iconTint = Color(0xFF007AFF),
                iconBg = Color(0xFF007AFF).copy(alpha = 0.15f),
                title = "Notlarımla Sohbet Et",
                subtitle = "Tüm notlarınızın hafızasını kullanarak arayın, soru sorun ve yanıt alın",
                badge = "Global AI",
                onClick = {
                    onDismiss()
                    onGlobalAiChatClick()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AiHubActionCard(
                icon = Icons.Default.AutoAwesome,
                iconTint = Color(0xFFAF52DE),
                iconBg = Color(0xFFAF52DE).copy(alpha = 0.15f),
                title = "Çoklu Not Sentezi",
                subtitle = "Seçilen notları birleştirin, ortak özet, bağlantılar veya rapor üretin",
                badge = "Sentez",
                onClick = {
                    onDismiss()
                    onSynthesisClick()
                }
            )
        }
    }
}

@Composable
private fun AiHubActionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    Surface(
        shape = RoundedCornerShape(16.dp),
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
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = iconTint.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconTint,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = textSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
