package com.applenotes.ai.presentation.note_editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.applenotes.ai.core.theme.*

@Composable
fun TableEditorDialog(
    onDismissRequest: () -> Unit,
    onInsertTable: (String) -> Unit
) {
    val isDark = isAppDarkTheme()
    val accentColor = rememberAccentColor()

    val bgCard = if (isDark) iOSCardDark else iOSCardLight
    val textPrimary = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
    val textSecondary = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
    val cellBorder = if (isDark) iOSSeparatorDark else iOSSeparatorLight

    var columnCount by remember { mutableIntStateOf(3) }
    var rowCount by remember { mutableIntStateOf(3) }

    // cellData: row index -> (col index -> text)
    val cellData = remember {
        mutableStateMapOf<Pair<Int, Int>, String>().apply {
            put(Pair(0, 0), "Başlık 1")
            put(Pair(0, 1), "Başlık 2")
            put(Pair(0, 2), "Başlık 3")
            put(Pair(1, 0), "Veri 1")
            put(Pair(1, 1), "Veri 2")
            put(Pair(1, 2), "Veri 3")
            put(Pair(2, 0), "Veri 4")
            put(Pair(2, 1), "Veri 5")
            put(Pair(2, 2), "Veri 6")
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = bgCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Etkileşimli Tablo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row / Column controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column counter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sütun: $columnCount", fontSize = 13.sp, color = textSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        FilledTonalIconButton(
                            onClick = { if (columnCount > 1) columnCount-- },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = { if (columnCount < 6) columnCount++ },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    // Row counter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Satır: $rowCount", fontSize = 13.sp, color = textSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        FilledTonalIconButton(
                            onClick = { if (rowCount > 2) rowCount-- },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = { if (rowCount < 12) rowCount++ },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Grid Area
                val horizontalScrollState = rememberScrollState()
                val verticalScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) iOSBackgroundDark else iOSBackgroundLight)
                        .border(1.dp, cellBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .horizontalScroll(horizontalScrollState)
                            .verticalScroll(verticalScrollState)
                    ) {
                        for (r in 0 until rowCount) {
                            Row {
                                for (c in 0 until columnCount) {
                                    val isHeader = r == 0
                                    val cellText = cellData[Pair(r, c)] ?: ""
                                    Box(
                                        modifier = Modifier
                                            .width(96.dp)
                                            .height(40.dp)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isHeader) accentColor.copy(alpha = 0.18f)
                                                else if (isDark) iOSCardDark else iOSCardLight
                                            )
                                            .border(
                                                0.5.dp,
                                                if (isHeader) accentColor.copy(alpha = 0.4f) else cellBorder,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        BasicTextField(
                                            value = cellText,
                                            onValueChange = { newVal ->
                                                cellData[Pair(r, c)] = newVal
                                            },
                                            textStyle = TextStyle(
                                                color = textPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            singleLine = true,
                                            cursorBrush = SolidColor(accentColor),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Vazgeç", color = textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val sb = StringBuilder("\n\n")
                            // 1. Header row
                            sb.append("|")
                            for (c in 0 until columnCount) {
                                val text = cellData[Pair(0, c)]?.trim()?.ifBlank { "Başlık ${c + 1}" } ?: "Başlık ${c + 1}"
                                sb.append(" $text |")
                            }
                            sb.append("\n")

                            // 2. Separator row
                            sb.append("|")
                            for (c in 0 until columnCount) {
                                sb.append(" :--- |")
                            }
                            sb.append("\n")

                            // 3. Data rows
                            for (r in 1 until rowCount) {
                                sb.append("|")
                                for (c in 0 until columnCount) {
                                    val text = cellData[Pair(r, c)]?.trim() ?: ""
                                    sb.append(" $text |")
                                }
                                sb.append("\n")
                            }
                            sb.append("\n")

                            onInsertTable(sb.toString())
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nota Ekle", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
