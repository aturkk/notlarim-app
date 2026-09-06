package com.applenotes.ai.presentation.ai_assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*

@Composable
fun AiFlashcardsDialog(
    rawText: String,
    onDismiss: () -> Unit
) {
    val isDark = isAppDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = AppleYellow,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Çalışma Kartları (Flashcards)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = rawText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight,
                    lineHeight = 22.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tamam", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
        shape = RoundedCornerShape(16.dp)
    )
}