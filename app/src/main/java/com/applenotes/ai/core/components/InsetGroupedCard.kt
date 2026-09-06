package com.applenotes.ai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*

@Composable
fun InsetGroupedSection(
    title: String? = null,
    footer: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isAppDarkTheme()
    val cardBg = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
                color = textSecondary,
                modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardBg),
            content = content
        )

        if (footer != null) {
            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = textSecondary,
                modifier = Modifier.padding(start = 12.dp, top = 6.dp)
            )
        }
    }
}

@Composable
fun InsetDivider(
    startIndent: Dp = 16.dp
) {
    val isDark = isAppDarkTheme()
    val color = if (isDark) iOSSeparatorDark else iOSSeparatorLight
    HorizontalDivider(
        modifier = Modifier.padding(start = startIndent),
        thickness = 0.5.dp,
        color = color
    )
}
