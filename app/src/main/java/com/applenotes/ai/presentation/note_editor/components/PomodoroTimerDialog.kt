package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.applenotes.ai.core.theme.*
import kotlinx.coroutines.delay

@Composable
fun PomodoroTimerDialog(
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    val workDurationSeconds = 25 * 60
    val breakDurationSeconds = 5 * 60

    var isWorkMode by remember { mutableStateOf(true) }
    var timeLeftSeconds by remember { mutableIntStateOf(workDurationSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    val totalDuration = if (isWorkMode) workDurationSeconds else breakDurationSeconds

    LaunchedEffect(isRunning, timeLeftSeconds) {
        if (isRunning && timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds--
        } else if (isRunning && timeLeftSeconds == 0) {
            // Switch cycle
            isWorkMode = !isWorkMode
            timeLeftSeconds = if (isWorkMode) workDurationSeconds else breakDurationSeconds
            isRunning = false
        }
    }

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val progress = (totalDuration - timeLeftSeconds).toFloat() / totalDuration.toFloat()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = bgColor,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isWorkMode) "🎯 Odaklanma Zamanı" else "☕ Kısa Mola",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isWorkMode) AppleYellow else Color(0xFF34C759)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(170.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isWorkMode) AppleYellow else Color(0xFF34C759),
                        strokeWidth = 10.dp,
                        trackColor = if (isDark) iOSSeparatorDark else iOSSeparatorLight
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 38.sp
                            ),
                            color = textPrimary
                        )
                        Text(
                            text = if (isWorkMode) "25 Dakika" else "5 Dakika",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Control Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset
                    FilledTonalIconButton(
                        onClick = {
                            isRunning = false
                            timeLeftSeconds = if (isWorkMode) workDurationSeconds else breakDurationSeconds
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sıfırla")
                    }

                    // Start / Pause
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWorkMode) AppleYellow else Color(0xFF34C759)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRunning) "Duraklat" else "Başlat",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
