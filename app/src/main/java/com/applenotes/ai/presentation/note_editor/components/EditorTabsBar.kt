package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*

data class EditorTabItem(
    val id: Long,
    val title: String,
    val icon: String? = null
)

@Composable
fun EditorTabsBar(
    tabs: List<EditorTabItem>,
    activeNoteId: Long,
    onSelectTab: (Long) -> Unit,
    onCloseTab: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tabs.size <= 1) return

    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val activeBg = if (isDark) iOSCardDark else iOSCardLight
    val inactiveBg = if (isDark) iOSBackgroundDark else iOSBackgroundLight

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val isActive = tab.id == activeNoteId
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isActive) activeBg else inactiveBg,
                border = if (isActive) {
                    androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                } else {
                    androidx.compose.foundation.BorderStroke(0.5.dp, if (isDark) iOSSeparatorDark else iOSSeparatorLight)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectTab(tab.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tab.icon ?: "📄",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.title.ifBlank { "Yeni Not" },
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) textPrimary else textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 110.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { onCloseTab(tab.id) },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Sekmeyi Kapat",
                            tint = textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
