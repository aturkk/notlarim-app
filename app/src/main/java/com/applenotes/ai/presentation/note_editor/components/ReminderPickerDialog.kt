package com.applenotes.ai.presentation.note_editor.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.applenotes.ai.core.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReminderPickerDialog(
    currentReminderMillis: Long?,
    onDismissRequest: () -> Unit,
    onSetReminder: (Long) -> Unit,
    onClearReminder: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()

    val bgCard = if (isDark) iOSCardDark else iOSCardLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = bgCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Not Hatırlatıcısı",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                if (currentReminderMillis != null && currentReminderMillis > 0) {
                    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                    Text(
                        text = "Aktif: ${sdf.format(Date(currentReminderMillis))}",
                        fontSize = 13.sp,
                        color = accentColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Presets
                ReminderPresetRow(
                    icon = Icons.Default.Schedule,
                    title = "1 Saat Sonra",
                    subtitle = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() + 3600 * 1000)),
                    textColor = textPrimary,
                    subColor = textSecondary,
                    onClick = {
                        val target = System.currentTimeMillis() + 3600 * 1000
                        onSetReminder(target)
                        onDismissRequest()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ReminderPresetRow(
                    icon = Icons.Default.WbTwilight,
                    title = "Bu Akşam (20:00)",
                    subtitle = "Bugün saat 20:00",
                    textColor = textPrimary,
                    subColor = textSecondary,
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 20)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            if (timeInMillis <= System.currentTimeMillis()) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }
                        onSetReminder(cal.timeInMillis)
                        onDismissRequest()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ReminderPresetRow(
                    icon = Icons.Default.WbSunny,
                    title = "Yarın Sabah (09:00)",
                    subtitle = "Yarın sabah 09:00",
                    textColor = textPrimary,
                    subColor = textSecondary,
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, 9)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }
                        onSetReminder(cal.timeInMillis)
                        onDismissRequest()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Date & Time Picker
                ReminderPresetRow(
                    icon = Icons.Default.EditCalendar,
                    title = "Özel Tarih ve Saat Seç...",
                    subtitle = "Takvimden istediğin anı ayarla",
                    textColor = accentColor,
                    subColor = textSecondary,
                    onClick = {
                        val now = Calendar.getInstance()
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val timePicker = TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val pickedCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            set(Calendar.MINUTE, minute)
                                            set(Calendar.SECOND, 0)
                                        }
                                        if (pickedCal.timeInMillis > System.currentTimeMillis()) {
                                            onSetReminder(pickedCal.timeInMillis)
                                            onDismissRequest()
                                        }
                                    },
                                    now.get(Calendar.HOUR_OF_DAY),
                                    now.get(Calendar.MINUTE),
                                    true
                                )
                                timePicker.show()
                            },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                        )
                        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                        datePicker.show()
                    }
                )

                // Clear Reminder Button if active
                if (currentReminderMillis != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    TextButton(
                        onClick = {
                            onClearReminder()
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = iOSRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hatırlatıcıyı Kaldır", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vazgeç", color = textSecondary)
                }
            }
        }
    }
}

@Composable
private fun ReminderPresetRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color,
    subColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subColor
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = subColor.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
