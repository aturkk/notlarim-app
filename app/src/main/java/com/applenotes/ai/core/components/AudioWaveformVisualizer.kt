package com.applenotes.ai.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.applenotes.ai.core.theme.AppleYellow
import com.applenotes.ai.core.theme.iOSRed
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AudioWaveformVisualizer(
    isRecording: Boolean,
    amplitudeProvider: () -> Int,
    modifier: Modifier = Modifier,
    barColor: Color = AppleYellow,
    barCount: Int = 24,
    maxHeight: androidx.compose.ui.unit.Dp = 28.dp
) {
    var amplitudes by remember { mutableStateOf(List(barCount) { 0.15f }) }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            val maxAmp = amplitudeProvider()
            val normalized = if (maxAmp > 100) {
                (maxAmp.toFloat() / 32767f).coerceIn(0.12f, 1f)
            } else {
                // Subtle ambient movement when quiet
                (0.12f + Random.nextFloat() * 0.18f).coerceIn(0.12f, 0.35f)
            }
            amplitudes = amplitudes.drop(1) + listOf(normalized)
            delay(80)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        amplitudes.forEachIndexed { index, amp ->
            val animatedHeight by animateFloatAsState(
                targetValue = if (isRecording) amp else 0.15f,
                animationSpec = tween(durationMillis = 80),
                label = "bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height((maxHeight * animatedHeight).coerceAtLeast(3.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isRecording) barColor else Color.Gray.copy(alpha = 0.3f))
            )
        }
    }
}
