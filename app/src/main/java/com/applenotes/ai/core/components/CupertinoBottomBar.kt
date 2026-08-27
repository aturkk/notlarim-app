package com.applenotes.ai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*

@Composable
fun CupertinoBottomBar(
    noteCountText: String,
    onFolderClick: () -> Unit,
    onNewNoteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) iOSBlurOverlayDark else iOSBlurOverlayLight

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bgColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                color = if (isDark) iOSSeparatorDark else iOSSeparatorLight,
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onFolderClick) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Klasörler",
                            tint = AppleYellow
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar & Profil",
                            tint = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                        )
                    }
                }

                Text(
                    text = noteCountText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                )

                IconButton(onClick = onNewNoteClick) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Yeni Not",
                        tint = AppleYellow
                    )
                }
            }
        }
    }
}
