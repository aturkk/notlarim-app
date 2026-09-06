package com.applenotes.ai.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.haptic.rememberHapticFeedbackHelper
import com.applenotes.ai.core.theme.*

@Composable
fun CupertinoBottomBar(
    noteCountText: String,
    onFolderClick: () -> Unit,
    onNewNoteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()
    val pillBg = if (isDark) iOSCardBackgroundDark.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.94f)
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val borderColor = if (isDark) iOSSeparatorDark.copy(alpha = 0.6f) else iOSSeparatorLight
    val haptic = rememberHapticFeedbackHelper()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = pillBg,
            shadowElevation = 10.dp,
            border = BorderStroke(0.5.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Folder & Settings
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.tick()
                            onFolderClick()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Klasörler",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.tick()
                            onSettingsClick()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar & Profil",
                            tint = textSecondary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                // Center: Note Count Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)).copy(alpha = 0.8f)
                ) {
                    Text(
                        text = noteCountText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = textSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Right: New Note Action Button
                Surface(
                    shape = CircleShape,
                    color = accentColor,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                ) {
                    IconButton(
                        onClick = {
                            haptic.tick()
                            onNewNoteClick()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = "Yeni Not",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
