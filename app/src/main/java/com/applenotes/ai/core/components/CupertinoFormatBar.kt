package com.applenotes.ai.core.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*

@Composable
fun CupertinoFormatBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onStrikeClick: () -> Unit,
    onH1Click: () -> Unit,
    onH2Click: () -> Unit,
    onChecklistClick: () -> Unit,
    onBulletClick: () -> Unit,
    onNumberedClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onCodeClick: () -> Unit,
    onLinkClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val bgColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val separatorColor = if (isDark) iOSSeparatorDark else iOSSeparatorLight
    val activeColor = AppleYellow
    val inactiveColor = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bgColor,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = separatorColor, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Undo & Redo
                FormatIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Geri Al",
                    enabled = canUndo,
                    tint = if (canUndo) activeColor else inactiveColor.copy(alpha = 0.4f),
                    onClick = onUndo
                )
                FormatIconButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "İleri Al",
                    enabled = canRedo,
                    tint = if (canRedo) activeColor else inactiveColor.copy(alpha = 0.4f),
                    onClick = onRedo
                )

                VerticalDivider(
                    modifier = Modifier.height(24.dp).padding(horizontal = 4.dp),
                    color = separatorColor
                )

                // Headers
                FormatTextButton(text = "H1", onClick = onH1Click, tint = activeColor)
                FormatTextButton(text = "H2", onClick = onH2Click, tint = activeColor)

                VerticalDivider(
                    modifier = Modifier.height(24.dp).padding(horizontal = 4.dp),
                    color = separatorColor
                )

                // Inline styles
                FormatIconButton(
                    icon = Icons.Default.FormatBold,
                    contentDescription = "Kalın",
                    tint = inactiveColor,
                    onClick = onBoldClick
                )
                FormatIconButton(
                    icon = Icons.Default.FormatItalic,
                    contentDescription = "İtalik",
                    tint = inactiveColor,
                    onClick = onItalicClick
                )
                FormatIconButton(
                    icon = Icons.Default.FormatStrikethrough,
                    contentDescription = "Üstü Çizili",
                    tint = inactiveColor,
                    onClick = onStrikeClick
                )

                VerticalDivider(
                    modifier = Modifier.height(24.dp).padding(horizontal = 4.dp),
                    color = separatorColor
                )

                // Lists & Structures
                FormatIconButton(
                    icon = Icons.Default.CheckBox,
                    contentDescription = "Kontrol Listesi",
                    tint = activeColor,
                    onClick = onChecklistClick
                )
                FormatIconButton(
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    contentDescription = "Madde İmli Liste",
                    tint = inactiveColor,
                    onClick = onBulletClick
                )
                FormatIconButton(
                    icon = Icons.Default.FormatListNumbered,
                    contentDescription = "Numaralı Liste",
                    tint = inactiveColor,
                    onClick = onNumberedClick
                )
                FormatIconButton(
                    icon = Icons.Default.FormatQuote,
                    contentDescription = "Alıntı",
                    tint = inactiveColor,
                    onClick = onQuoteClick
                )
                FormatIconButton(
                    icon = Icons.Default.Code,
                    contentDescription = "Kod Bloğu",
                    tint = inactiveColor,
                    onClick = onCodeClick
                )
                FormatIconButton(
                    icon = Icons.Default.Link,
                    contentDescription = "Not Bağlantısı [[ ]]",
                    tint = activeColor,
                    onClick = onLinkClick
                )
            }
        }
    }
}

@Composable
private fun FormatIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun FormatTextButton(
    text: String,
    tint: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier.height(34.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = tint
        )
    }
}
