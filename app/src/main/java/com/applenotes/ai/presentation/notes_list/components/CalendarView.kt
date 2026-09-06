package com.applenotes.ai.presentation.notes_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.*
import com.applenotes.ai.domain.model.Note
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarView(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onAddNoteForDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight

    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("tr")) }
    val dayHeaderFormat = remember { SimpleDateFormat("d MMMM EEEE", Locale("tr")) }

    val daysInMonth = remember(currentCalendar.timeInMillis) {
        val cal = currentCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        firstDayOfWeek to maxDays
    }

    val selectedDayNotes = remember(notes, selectedDateMillis) {
        val targetCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val targetYear = targetCal.get(Calendar.YEAR)
        val targetDayOfYear = targetCal.get(Calendar.DAY_OF_YEAR)

        notes.filter { note ->
            val noteCal = Calendar.getInstance().apply {
                timeInMillis = note.reminderTime ?: note.createdAt
            }
            noteCal.get(Calendar.YEAR) == targetYear && noteCal.get(Calendar.DAY_OF_YEAR) == targetDayOfYear
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Calendar Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = bgColor,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month Switcher Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthYearFormat.format(currentCalendar.time).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                val cal = currentCalendar.clone() as Calendar
                                cal.add(Calendar.MONTH, -1)
                                currentCalendar = cal
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Önceki Ay",
                                tint = AppleYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val cal = currentCalendar.clone() as Calendar
                                cal.add(Calendar.MONTH, 1)
                                currentCalendar = cal
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Sonraki Ay",
                                tint = AppleYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day Names Header (Pzt - Paz)
                val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayNames.forEach { name ->
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day Grid
                val (firstDayOffset, totalDays) = daysInMonth
                val totalSlots = firstDayOffset + totalDays
                val rows = (totalSlots + 6) / 7

                val todayCal = Calendar.getInstance()

                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        for (col in 0 until 7) {
                            val slotIndex = row * 7 + col
                            val dayNumber = slotIndex - firstDayOffset + 1

                            if (dayNumber in 1..totalDays) {
                                val cellCal = (currentCalendar.clone() as Calendar).apply {
                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                }
                                val isToday = todayCal.get(Calendar.YEAR) == cellCal.get(Calendar.YEAR) &&
                                        todayCal.get(Calendar.DAY_OF_YEAR) == cellCal.get(Calendar.DAY_OF_YEAR)

                                val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                                val isSelected = selCal.get(Calendar.YEAR) == cellCal.get(Calendar.YEAR) &&
                                        selCal.get(Calendar.DAY_OF_YEAR) == cellCal.get(Calendar.DAY_OF_YEAR)

                                // Has notes on this day
                                val hasNotes = notes.any { note ->
                                    val nCal = Calendar.getInstance().apply {
                                        timeInMillis = note.reminderTime ?: note.createdAt
                                    }
                                    nCal.get(Calendar.YEAR) == cellCal.get(Calendar.YEAR) &&
                                            nCal.get(Calendar.DAY_OF_YEAR) == cellCal.get(Calendar.DAY_OF_YEAR)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) AppleYellow
                                            else if (isToday) AppleYellow.copy(alpha = 0.2f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            selectedDateMillis = cellCal.timeInMillis
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$dayNumber",
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else textPrimary
                                        )
                                        if (hasNotes) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else AppleYellow)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Date Details & Notes List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayHeaderFormat.format(Date(selectedDateMillis)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Button(
                onClick = { onAddNoteForDate(selectedDateMillis) },
                colors = ButtonDefaults.buttonColors(containerColor = AppleYellow),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Not Ekle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedDayNotes.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = bgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier.padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu tarih için planlanmış veya oluşturulmuş bir not bulunmuyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedDayNotes, key = { it.id }) { note ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = bgColor,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNoteClick(note) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.icon ?: "📝",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title.ifBlank { "Başlıksız Not" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                if (note.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = note.content.take(70),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
