package com.applenotes.ai.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applenotes.ai.core.theme.AppleYellow
import com.applenotes.ai.core.theme.iOSCardBackgroundDark
import com.applenotes.ai.core.theme.iOSCardBackgroundLight
import com.applenotes.ai.core.theme.iOSTextPrimaryDark
import com.applenotes.ai.core.theme.iOSTextPrimaryLight
import com.applenotes.ai.core.theme.iOSTextSecondaryDark
import com.applenotes.ai.core.theme.iOSTextSecondaryLight
import com.applenotes.ai.core.theme.isAppDarkTheme
import com.applenotes.ai.core.haptic.rememberHapticFeedbackHelper

enum class PaperType(val title: String, val subtitle: String, val icon: String) {
    BLANK("Düz Sayfa", "Standart sade ve temiz görünüm", "📄"),
    LINED("Çizgili Defter", "Klasik not defteri çizgileri", "📏"),
    GRID("Kareli Defter", "Matematik ve planlama için kareler", "📐"),
    DOT_GRID("Noktalı Defter", "Bullet journal stili estetik noktalar", "🔘"),
    SEPIA("Sıcak Parşömen", "Göz yormayan sıcak nostaljik doku", "📜")
}

@Composable
fun PaperBackground(
    paperType: PaperType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isAppDarkTheme()

    val sepiaBgColor = if (isDark) Color(0xFF231E19) else Color(0xFFFBF4E6)
    val bgColor = if (paperType == PaperType.SEPIA) sepiaBgColor else Color.Transparent

    val lineColor = if (isDark) {
        Color(0xFF38383A).copy(alpha = 0.55f)
    } else {
        when (paperType) {
            PaperType.SEPIA -> Color(0xFFBFA98A).copy(alpha = 0.45f)
            else -> Color(0xFFD1D1D6).copy(alpha = 0.55f)
        }
    }

    Box(
        modifier = modifier.background(bgColor)
    ) {
        if (paperType != PaperType.BLANK) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val stepPx = 32.dp.toPx()

                when (paperType) {
                    PaperType.LINED, PaperType.SEPIA -> {
                        var y = 48.dp.toPx()
                        while (y < height) {
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                            y += stepPx
                        }
                    }
                    PaperType.GRID -> {
                        // Horizontal lines
                        var y = 20.dp.toPx()
                        while (y < height) {
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 0.8.dp.toPx()
                            )
                            y += stepPx
                        }
                        // Vertical lines
                        var x = 20.dp.toPx()
                        while (x < width) {
                            drawLine(
                                color = lineColor,
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = 0.8.dp.toPx()
                            )
                            x += stepPx
                        }
                    }
                    PaperType.DOT_GRID -> {
                        val dotRadius = 1.2.dp.toPx()
                        var y = 24.dp.toPx()
                        while (y < height) {
                            var x = 24.dp.toPx()
                            while (x < width) {
                                drawCircle(
                                    color = lineColor,
                                    radius = dotRadius,
                                    center = Offset(x, y)
                                )
                                x += stepPx
                            }
                            y += stepPx
                        }
                    }
                    else -> Unit
                }
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperPickerBottomSheet(
    currentPaperType: PaperType,
    onSelect: (PaperType) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppDarkTheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = rememberHapticFeedbackHelper()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) iOSCardBackgroundDark else iOSCardBackgroundLight,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "📜 Kağıt Deseni ve Dokusu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Notunuz için dilediğiniz defter arka planını seçin",
                fontSize = 13.sp,
                color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PaperType.values().forEach { paper ->
                    val isSelected = paper == currentPaperType
                    val cardBg = if (isSelected) {
                        AppleYellow.copy(alpha = 0.12f)
                    } else {
                        if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
                    }
                    val borderColor = if (isSelected) AppleYellow else Color.Transparent

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cardBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                haptic.selection()
                                onSelect(paper)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = paper.icon,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 14.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = paper.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) AppleYellow else (if (isDark) iOSTextPrimaryDark else iOSTextPrimaryLight)
                                )
                                Text(
                                    text = paper.subtitle,
                                    fontSize = 12.sp,
                                    color = if (isDark) iOSTextSecondaryDark else iOSTextSecondaryLight
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçildi",
                                    tint = AppleYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
