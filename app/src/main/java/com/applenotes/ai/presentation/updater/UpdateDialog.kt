package com.applenotes.ai.presentation.updater

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.AppUpdateInfo

@Composable
fun UpdateDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    val isDark = isAppDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = AppleYellow,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Yeni Güncelleme Mevcut!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sürüm: ${updateInfo.latestVersion} (Mevcut: v${updateInfo.currentVersion})",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Yenilikler:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = updateInfo.changelog,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDownload()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppleYellow,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Şimdi Güncelle", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Daha Sonra",
                    color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                )
            }
        },
        containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
        shape = RoundedCornerShape(16.dp)
    )
}
